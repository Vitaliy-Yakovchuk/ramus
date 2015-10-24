package com.ramussoft.common.persistent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import com.ramussoft.common.attribute.Attribute;

@SuppressWarnings("unchecked")
public class PersistentWrapper {

    private String[] fields;

    private Hashtable<String, Method> getters = new Hashtable<String, Method>();

    private Hashtable<String, Method> setters = new Hashtable<String, Method>();

    private Class<?> clazz;

    public static final Class<? extends Annotation>[] ANNOTATIONS = new Class[]{
            Element.class, Attribute.class, Text.class, Long.class, Date.class,
            Double.class, Id.class, Qualifier.class, Binary.class,
            Integer.class};

    public PersistentWrapper(Class<?> clazz) {
        this.clazz = clazz;
        try {
            initFields();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initFields() throws SecurityException, NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        ArrayList<String> list = new ArrayList<String>();

        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                String field = getterToField(method.getName());
                String setter = fieldToSetter(field);
                try {
                    Method str = clazz
                            .getMethod(setter, method.getReturnType());
                    if (isAnnotationPresent(method)) {
                        list.add(field);
                    }
                    setters.put(field, str);
                    getters.put(field, method);
                } catch (NoSuchMethodException e) {
                }
            }
        }
        fields = list.toArray(new String[list.size()]);
        Arrays.sort(fields);
    }

    private boolean isAnnotationPresent(Method method) {
        for (Class<? extends Annotation> c : ANNOTATIONS)
            if (method.isAnnotationPresent(c))
                return true;
        return false;
    }

    public String[] getFields() {
        return fields;
    }

    public void setField(Object persistent, String field, Object value) {
        try {
            getSetter(field).invoke(persistent, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getField(Object persistent, String field) {
        try {
            return getGetter(field).invoke(persistent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Method getGetter(String field) {
        return getters.get(field);
    }

    public Method getSetter(String field) {
        return setters.get(field);
    }

    public boolean isAnnotationPresent(String field,
                                       Class<? extends Annotation> class1) {
        return getGetter(field).isAnnotationPresent(class1);
    }

    /**
     * Convert method name getter to field name, for example:
     * getterToField("getName")=="name"
     */

    private String getterToField(String getter) {
        if ((getter.startsWith("get")) && (getter.length() > 3)) {
            StringBuffer sb = new StringBuffer(getter.substring(3, 4)
                    .toLowerCase());
            sb.append(getter.substring(4));
            return sb.toString();
        } else {
            throw new RuntimeException(MessageFormat.format(
                    "wrong getter format \"{0}\"", getter));
        }
    }

    /**
     * Convert method name getter to field name, for example:
     * getterToField("getName")=="name"
     */

    private String fieldToSetter(String field) {
        StringBuffer sb = new StringBuffer("set");
        sb.append(field.substring(0, 1).toUpperCase());
        sb.append(field.substring(1));
        return sb.toString();
    }

    public String toDatabaseField(String fieldName) {
        StringBuffer sb = new StringBuffer();
        for (char c : fieldName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else
                sb.append(c);
        }
        return sb.toString();
    }

    public String[] getDatabaseFields() {
        String[] fields = getFields();
        String[] res = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            res[i] = toDatabaseField(fields[i]);
        }
        return res;
    }

    public <A extends Annotation> A getAnnotation(String field,
                                                  Class<A> annotationClass) {
        return getGetter(field).getAnnotation(annotationClass);
    }

    public int getAnnotationType(String field) {
        Method method = getGetter(field);
        for (int i = 0; i < ANNOTATIONS.length; i++) {
            if (method.isAnnotationPresent(ANNOTATIONS[i]))
                return i;
        }
        if ("valueBranchId".equals(field))
            return PersistentField.VALUE_BRANCH_ID;
        return -1;
    }

    public void setDatabaseField(Object persistent, PersistentField field,
                                 ResultSet rs) throws SQLException {

        if (rs.getObject(field.getDatabaseName()) == null) {
            setField(persistent, field.getName(), null);
            return;
        }

        switch (field.getType()) {
            case PersistentField.ATTRIBUTE:
            case PersistentField.ID:
            case PersistentField.QUALIFIER:
            case PersistentField.ELEMENT:
            case PersistentField.LONG:
                setField(persistent, field.getName(),
                        rs.getLong(field.getDatabaseName()));
                break;
            case PersistentField.DATE:
                Timestamp timestamp = rs.getTimestamp(field.getDatabaseName());
                setField(persistent, field.getName(), timestamp);
                break;
            case PersistentField.DOUBLE:
                setField(persistent, field.getName(),
                        rs.getDouble(field.getDatabaseName()));
                break;
            case PersistentField.TEXT:
                setField(persistent, field.getName(),
                        rs.getString(field.getDatabaseName()));
                break;
            case PersistentField.BINARY:
                setField(persistent, field.getName(),
                        rs.getBytes(field.getDatabaseName()));
                break;
            case PersistentField.INTEGER:
                setField(persistent, field.getName(),
                        rs.getInt(field.getDatabaseName()));
                break;
            case PersistentField.VALUE_BRANCH_ID:
                setField(persistent, field.getName(),
                        rs.getLong(field.getDatabaseName()));
                break;

            default:
                throw new RuntimeException("Unknow field type to set.");
        }

    }

    public Class<?> getClazz() {
        return clazz;
    }
}
