package com.ramussoft.core.format;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.core.impl.XmlDates;

public final class PersistentCodec {

    public static final String ELEMENT = "element";

    public static final String QUALIFIER = "qualifier";

    public static final String ATTRIBUTE = "attribute";

    private static final Map<String, String> REFERENCE_EXCEPTIONS =
            new LinkedHashMap<String, String>();

    static {
        String simple = "com.ramussoft.core.attribute.simple.";
        REFERENCE_EXCEPTIONS.put(simple + "HierarchicalPersistent"
                + "#parentElementId", ELEMENT);
        REFERENCE_EXCEPTIONS.put(simple + "HierarchicalPersistent"
                + "#previousElementId", ELEMENT);
        REFERENCE_EXCEPTIONS.put(simple + "OtherElementPersistent"
                + "#otherElement", ELEMENT);
        REFERENCE_EXCEPTIONS.put("com.ramussoft.idef0.attribute"
                + ".SectorBorderPersistent#function", ELEMENT);
        REFERENCE_EXCEPTIONS.put("com.ramussoft.idef0.attribute"
                + ".AnyToAnyPersistent#otherElement", ELEMENT);
        REFERENCE_EXCEPTIONS.put("com.ramussoft.chart.core"
                + ".ChartLinkPersistent#otherElementId", ELEMENT);
        REFERENCE_EXCEPTIONS.put("com.ramussoft.chart.core"
                + ".TableChartPersistent#otherElementId", ELEMENT);
    }

    private static final String ELEMENT_ID = "elementId";

    private static final String ATTRIBUTE_ID = "attributeId";

    private static final String VALUE_BRANCH_ID = "valueBranchId";

    private final Map<Class<?>, PersistentWrapper> wrappers =
            new LinkedHashMap<Class<?>, PersistentWrapper>();

    private final IEngine engine;

    private final Map<String, Boolean> exists = new HashMap<String, Boolean>();

    public PersistentCodec() {
        this(null);
    }

    public PersistentCodec(IEngine engine) {
        this.engine = engine;
    }

    public PersistentWrapper wrapper(Class<?> clazz) {
        PersistentWrapper wrapper = wrappers.get(clazz);
        if (wrapper == null) {
            wrapper = new PersistentWrapper(clazz);
            wrappers.put(clazz, wrapper);
        }
        return wrapper;
    }

    public Map<String, Object> toMap(Persistent persistent) {
        PersistentWrapper wrapper = wrapper(persistent.getClass());
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (String field : wrapper.getFields()) {
            if (isContextual(field))
                continue;
            Object value = wrapper.getField(persistent, field);
            if (value == null)
                continue;
            String kind = referenceKind(persistent.getClass(), field,
                    wrapper.getAnnotationType(field));
            if (kind != null)
                result.put(field, encodeReference(kind, value));
            else
                result.put(field, encode(wrapper.getAnnotationType(field),
                        value));
        }
        return result;
    }

    public Object toValue(Persistent persistent) {
        Map<String, Object> map = toMap(persistent);
        String single = singleField(persistent.getClass());
        if (single != null && map.size() == 1 && map.containsKey(single))
            return map.get(single);
        return map;
    }

    @SuppressWarnings("unchecked")
    public Persistent fromValue(Class<? extends Persistent> clazz, Object value) {
        if (value instanceof Map)
            return fromMap(clazz, (Map<String, Object>) value);
        String single = singleField(clazz);
        if (single == null)
            throw new IllegalArgumentException("Type " + clazz.getName()
                    + " has several fields, expected a map, not " + value);
        Map<String, Object> map = new LinkedHashMap<String, Object>(1);
        map.put(single, value);
        return fromMap(clazz, map);
    }

    public String singleField(Class<?> clazz) {
        String found = null;
        for (String field : wrapper(clazz).getFields()) {
            if (isContextual(field))
                continue;
            if (found != null)
                return null;
            found = field;
        }
        return found;
    }

    public Persistent fromMap(Class<? extends Persistent> clazz,
                              Map<String, Object> values) {
        PersistentWrapper wrapper = wrapper(clazz);
        Persistent persistent;
        try {
            persistent = clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can not create "
                    + clazz.getName() + "; a public constructor without"
                    + " arguments is required", e);
        }
        for (String field : wrapper.getFields()) {
            if (isContextual(field))
                continue;
            Method setter = wrapper.getSetter(field);
            Class<?> target = setter.getParameterTypes()[0];
            Object raw = values.get(field);
            if (raw == null) {
                if (!target.isPrimitive())
                    wrapper.setField(persistent, field, null);
                continue;
            }
            String kind = referenceKind(clazz, field,
                    wrapper.getAnnotationType(field));
            Object decoded = kind != null
                    ? decodeReference(kind, raw, target)
                    : decode(target, raw);
            wrapper.setField(persistent, field, decoded);
        }
        return persistent;
    }

    private String referenceKind(Class<?> clazz, String field,
                                 int annotationType) {
        if (engine == null)
            return null;
        switch (annotationType) {
            case PersistentField.ELEMENT:
                return ELEMENT;
            case PersistentField.QUALIFIER:
                return QUALIFIER;
            case PersistentField.ATTRIBUTE:
                return ATTRIBUTE;
            default:
                return REFERENCE_EXCEPTIONS.get(clazz.getName() + "#" + field);
        }
    }

    private Object encodeReference(String kind, Object value) {
        long numericId = ((Number) value).longValue();
        if (numericId < 0 || !exists(kind, numericId))
            return Long.valueOf(-1L);
        return StableIds.of(kind, numericId);
    }

    private boolean exists(String kind, long numericId) {
        String key = kind + '#' + numericId;
        Boolean cached = exists.get(key);
        if (cached != null)
            return cached.booleanValue();
        boolean found;
        try {
            if (ELEMENT.equals(kind))
                found = engine.getElement(numericId) != null;
            else if (QUALIFIER.equals(kind))
                found = engine.getQualifier(numericId) != null;
            else
                found = engine.getAttribute(numericId) != null;
        } catch (RuntimeException e) {
            found = false;
        }
        exists.put(key, Boolean.valueOf(found));
        return found;
    }

    private Object decodeReference(String kind, Object raw, Class<?> target) {
        long numericId;
        if (raw instanceof Number)
            numericId = ((Number) raw).longValue();
        else
            numericId = StableIds.toNumericId(kind, raw.toString());
        return decode(target, Long.valueOf(numericId));
    }

    private static boolean isContextual(String field) {
        return ELEMENT_ID.equals(field) || ATTRIBUTE_ID.equals(field)
                || VALUE_BRANCH_ID.equals(field);
    }

    private static Object encode(int annotationType, Object value) {
        switch (annotationType) {
            case PersistentField.BINARY:
                return java.util.Base64.getEncoder()
                        .encodeToString((byte[]) value);
            case PersistentField.DATE:
                return XmlDates.format(value);
            default:
                return value;
        }
    }

    private static Object decode(Class<?> target, Object raw) {
        if (target == byte[].class)
            return java.util.Base64.getDecoder().decode(raw.toString());
        if (target == Date.class || target == java.sql.Timestamp.class
                || target == java.sql.Date.class) {
            Date parsed;
            try {
                parsed = XmlDates.parse(raw.toString());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unparsable date: "
                        + raw, e);
            }
            if (target == java.sql.Timestamp.class)
                return new java.sql.Timestamp(parsed.getTime());
            if (target == java.sql.Date.class)
                return new java.sql.Date(parsed.getTime());
            return parsed;
        }
        if (target == long.class || target == Long.class)
            return Long.valueOf(((Number) toNumber(raw)).longValue());
        if (target == int.class || target == java.lang.Integer.class)
            return java.lang.Integer.valueOf(((Number) toNumber(raw)).intValue());
        if (target == double.class || target == java.lang.Double.class)
            return java.lang.Double.valueOf(((Number) toNumber(raw))
                    .doubleValue());
        if (target == boolean.class || target == Boolean.class)
            return raw instanceof Boolean ? raw : Boolean.valueOf(raw
                    .toString());
        if (target == String.class)
            return raw.toString();
        return raw;
    }

    private static Object toNumber(Object raw) {
        if (raw instanceof Number)
            return raw;
        return new java.math.BigDecimal(raw.toString());
    }
}
