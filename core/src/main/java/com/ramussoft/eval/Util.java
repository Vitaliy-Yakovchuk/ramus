package com.ramussoft.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;

public class Util {

    public static final String EVAL = "Eval";

    public static final String UTILS = "Utils";

    public static final String ATTRIBUTE_PREFIX = "A";

    public static final String ELEMENT_PREFIX = "E";

    protected Engine engine;

    private ScriptHolders scriptHolders;

    protected Util(Engine engine) {
        this.engine = engine;
    }

    public Util() {
    }

    public static Util getUtils(Engine engine) {
        Util utils = (Util) engine.getPluginProperty(EVAL, UTILS);
        if (utils == null) {
            utils = new Util(engine);
            engine.setPluginProperty(EVAL, UTILS, utils);
        }
        return utils;
    }

    public String compile(Qualifier qualifier, String userFunction) {
        List<Attribute> attrs = qualifier.getAttributes();
        final Hashtable<String, String> hash = new Hashtable<String, String>(
                attrs.size());
        for (Attribute attr : attrs) {
            AttributeType type = attr.getAttributeType();
            if (type.toString().equals("Core.Table")) {
                Qualifier table = engine.getSystemQualifier("TableQualifier_"
                        + attr.getId());
                for (Attribute tAttr : table.getAttributes()) {
                    hash.put(toCanonicalValue(attr.getName() + "."
                            + tAttr.getName()), getAttributeEId(attr.getId(),
                            tAttr.getId()));
                }
            }
            hash.put(toCanonicalValue(attr.getName()), getAttributeEId(attr
                    .getId()));
        }

        Eval eval = new Eval(userFunction);

        eval.replaceValueNames(new Replacementable() {
            @Override
            public String getNewName(String oldName) {
                if ("ELEMENT".equals(oldName))
                    return oldName;
                String name = hash.get(oldName);
                if (name == null) {
                    name = compileValue(null, oldName);
                }
                return name;
            }
        });
        return eval.toString();
    }

    public static String toCanonicalValue(String value) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (char c : value.toCharArray()) {
            if (c == ']')
                sb.append("\\]");
            else
                sb.append(c);

        }
        sb.append(']');
        return sb.toString();
    }

    public static String replaceDots(String value) {
        return value.replace(".", "\\.");
    }

    public static String toUserValue(String value) {
        if ((value.length() > 0) && (value.charAt(0) != '['))
            return value;
        StringBuffer sb = new StringBuffer();
        int len = value.length() - 1;
        for (int i = 1; i < len; i++) {
            char c = value.charAt(i);
            if (c == '\\') {
                if (value.charAt(i + 1) == ']') {
                    sb.append(value.charAt(i + 1));
                    i++;
                } else
                    sb.append(c);
            } else
                sb.append(c);
        }
        return sb.toString();
    }

    public String decompile(Qualifier qualifier, String function) {
        List<Attribute> attrs = qualifier.getAttributes();
        final Hashtable<String, String> hash = new Hashtable<String, String>(
                attrs.size());
        for (Attribute attr : attrs) {
            if (attr.getAttributeType().toString().equals("Core.Table")) {
                Qualifier table = engine.getSystemQualifier("TableQualifier_"
                        + attr.getId());
                for (Attribute tAttr : table.getAttributes()) {
                    hash.put(getAttributeEId(attr.getId(), tAttr.getId()),
                            toCanonicalValue(attr.getName() + "."
                                    + tAttr.getName()));
                }
            }
            hash.put(getAttributeEId(attr.getId()), toCanonicalValue(attr
                    .getName()));
        }

        Eval eval = new Eval(function);

        eval.replaceValueNames(new Replacementable() {
            @Override
            public String getNewName(String oldName) {
                if ("ELEMENT".equals(oldName))
                    return oldName;
                String name = hash.get(oldName);
                if (name == null) {
                    if (oldName.startsWith(ELEMENT_PREFIX)) {
                        MetaValue metaValue = toMetaValue(oldName);
                        Element element = engine.getElement(metaValue
                                .getElementId());
                        Attribute attribute = engine.getAttribute(metaValue
                                .getAttributeId());
                        Qualifier qualifier = engine.getQualifier(element
                                .getQualifierId());
                        return toCanonicalValue(qualifier.getName() + "."
                                + element.getName() + "." + attribute.getName());
                    }
                }
                return name;
            }
        });
        return eval.toString();
    }

    private String getAttributeEId(long id) {
        return ATTRIBUTE_PREFIX + id;
    }

    public boolean isAttributePresent(Eval eval, long attributeId) {
        String value = getAttributeEId(attributeId);
        for (String s : eval.getValues()) {
            if (value.equals(s))
                return true;
        }
        return false;
    }

    public void fillAttributes(Eval eval, Qualifier qualifier, Element element,
                               Attribute tableAttribute, Qualifier tableQualifier,
                               Element tableElement, boolean toUserValue) {
        String[] values = eval.getValues();
        for (String value : values) {
            for (Attribute attribute : qualifier.getAttributes()) {
                if (value.equals(getAttributeEId(attribute.getId()))) {
                    eval.setValue(value, new EObject(engine.getAttribute(
                            element, attribute), element, attribute, engine, toUserValue));
                }
            }
            if (tableAttribute != null) {
                for (Attribute attribute : tableQualifier.getAttributes()) {
                    if (value.equals(getAttributeEId(tableAttribute.getId(),
                            attribute.getId()))) {
                        eval.setValue(value, new EObject(engine.getAttribute(
                                tableElement, attribute), tableElement,
                                attribute, engine, toUserValue));
                    }
                }
            }
        }
        fillMetaValues(eval, toUserValue);
        for (String value : eval.getValues()) {
            if ("ELEMENT".equals(value))
                eval.setValue(value, new EObject(
                        (tableElement == null) ? element : tableElement));
        }
        eval.fillEmpty();
    }

    public void fillMetaValues(Eval eval) {
        fillMetaValues(eval, true);
    }

    public void fillMetaValues(Eval eval, boolean toUserValue) {
        String[] values = eval.getValues();
        MetaValue[] mv = toMetaValues(values);
        for (int i = 0; i < mv.length; i++) {
            MetaValue metaValue = mv[i];
            if (metaValue != null) {
                Attribute attribute = engine.getAttribute(metaValue
                        .getAttributeId());
                Element element2 = engine.getElement(metaValue.getElementId());
                eval.setValue(values[i], new EObject(engine.getAttribute(
                        element2, attribute), element2, attribute, engine, toUserValue));
            }
        }
        for (String functionName : eval.getFunctions()) {
            eval.setFunction(functionName, new ScriptFunction(functionName));
        }
    }

    private String getAttributeEId(long tableAttributeId, long attributeId) {
        return tableAttributeToValue(tableAttributeId, attributeId);
    }

    public void fillResult(long attributeId, Exception e, Element element) {
        Object object = null;
        e.printStackTrace();
        Attribute attribute = engine.getAttribute(attributeId);
        AttributeType attributeType = attribute.getAttributeType();
        if (attributeType.getPluginName().equals("Core")) {
            String typeName = attributeType.getTypeName();
            if (typeName.equals("Text")) {
                object = e.getLocalizedMessage();
            }
        }
        engine.setAttribute(element, attribute, object);
    }

    public Object fillResult(long attributeId, Eval eval, Element element) {
        EObject object = eval.calculate();
        Object value = object.getValue();
        Attribute attribute = engine.getAttribute(attributeId);
        AttributeType attributeType = attribute.getAttributeType();
        if ((attributeType.getPluginName().equals("Core")) && (value != null)) {
            String typeName = attributeType.getTypeName();
            if (typeName.equals("Text")) {
                value = object.stringValue();
            } else if (typeName.equals("Long")) {
                value = object.longValue();
            } else if (typeName.equals("Double")) {
                value = object.doubleValue();
            } else if (typeName.equals("Boolean")) {
                value = object.booleanValue();
            } else if (typeName.equals("Date")) {
                value = object.dateValue();
            }

        }
        engine.setAttribute(element, attribute, value);
        return object;
    }

    public List<Attribute> getAttributes(Qualifier qualifier, Eval eval) {
        List<Attribute> result = new ArrayList<Attribute>();
        String[] values = eval.getValues();
        for (String value : values) {
            for (Attribute attr : qualifier.getAttributes()) {
                if (value.equals(getAttributeEId(attr.getId()))) {
                    result.add(attr);
                }
            }
        }
        return result;
    }

    public void addAttributes(List<Attribute> sources, Eval eval,
                              List<Attribute> attributes) {
        String[] values = eval.getValues();
        for (Attribute attribute : attributes) {
            if (sources.indexOf(attribute) < 0) {
                for (String value : values) {
                    if (getAttributeEId(attribute.getId()).equals(value)) {
                        sources.add(attribute);
                        break;
                    }
                }
            }
        }
    }

    public void fillAttributes(Eval eval, Element element,
                               List<Attribute> sources, Object[] values, boolean toUserValue) {
        for (String value : eval.getValues()) {
            for (int i = 0; i < values.length; i++) {
                Attribute attribute = sources.get(i);
                if (value.equals(getAttributeEId(attribute.getId()))) {
                    eval.setValue(value, new EObject(values[i], element,
                            attribute, engine, toUserValue));
                    break;
                }
            }
        }
        fillMetaValues(eval, toUserValue);
    }

    private class FEval implements Comparable<FEval> {

        public FEval(FunctionPersistent fp) {
            this.fp = fp;
            this.eval = new Eval(fp.getFunction());
        }

        @Override
        public int compareTo(FEval o) {
            if (in(this.fp.getQualifierAttributeId(), o.eval))
                return -1;
            if (in(o.fp.getQualifierAttributeId(), this.eval))
                return 1;
            if (inTable(this.fp.getQualifierAttributeId(), this.fp
                    .getQualifierTableAttributeId(), o.eval))
                return -1;
            if (inTable(o.fp.getQualifierTableAttributeId(), o.fp
                    .getQualifierTableAttributeId(), this.eval))
                return 1;
            return 0;
        }

        private boolean in(long qualifierAttributeId, Eval eval) {
            for (String value : eval.getValues()) {
                if (value.equals(ATTRIBUTE_PREFIX + qualifierAttributeId))
                    return true;
            }
            return false;
        }

        private boolean inTable(long qualifierAttributeId,
                                long qualifierTableAttributeId, Eval eval) {
            for (String value : eval.getValues()) {
                if (value.equals(ATTRIBUTE_PREFIX + qualifierAttributeId
                        + ATTRIBUTE_PREFIX + qualifierTableAttributeId))
                    return true;
            }
            return false;
        }

        FunctionPersistent fp;

        Eval eval;

    }

    ;

    public Eval[] sort(List<FunctionPersistent> functions) {
        FEval[] evals = new FEval[functions.size()];
        for (int i = 0; i < evals.length; i++) {
            evals[i] = new FEval(functions.get(i));
        }
        Arrays.sort(evals);
        functions.clear();
        Eval[] result = new Eval[evals.length];
        int i = 0;
        for (FEval eval : evals) {
            functions.add(eval.fp);
            result[i] = eval.eval;
            i++;
        }
        return result;
    }

    public MetaValue[] toMetaValues(String[] values) {
        MetaValue[] result = new MetaValue[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = toMetaValue(values[i]);
        }
        return result;
    }

    public MetaValue toMetaValue(String value) {
        if ("ELEMENT".equals(value))
            return null;
        if (value.startsWith(ELEMENT_PREFIX)) {
            int end = value.indexOf(ATTRIBUTE_PREFIX.charAt(0));
            return new MetaValue(Long.parseLong(value.substring(1, end)), Long
                    .parseLong(value.substring(end + 1)));
        }
        return null;
    }

    public Eval createAndFillEval(CalculateInfo info, boolean toUserValue) {
        Hashtable<Long, Element> elements = new Hashtable<Long, Element>();
        Hashtable<Long, Qualifier> qualifiers = new Hashtable<Long, Qualifier>();

        Eval eval = new Eval(info.getFormula());

        for (String value : eval.getValues()) {
            eval.setValue(value, getObject(value, qualifiers, elements, toUserValue));
        }

        for (String functionName : eval.getFunctions()) {
            eval.setFunction(functionName, new ScriptFunction(functionName));
        }

        return eval;
    }

    private EObject getObject(String value,
                              Hashtable<Long, Qualifier> qualifiers,
                              Hashtable<Long, Element> elements, boolean toUserValue) {
        MetaValue metaValue = toMetaValue(value);
        Element element = getElement(elements, metaValue.getElementId());
        Qualifier qualifier = getQualifier(qualifiers, element.getQualifierId());
        Attribute attribute = null;
        for (Attribute attr : qualifier.getAttributes()) {
            if (attr.getId() == metaValue.getAttributeId())
                attribute = attr;
        }
        return new EObject(engine.getAttribute(element, attribute), element,
                attribute, engine, toUserValue);
    }

    private Qualifier getQualifier(Hashtable<Long, Qualifier> qualifiers,
                                   long qualifierId) {
        Qualifier qualifier = qualifiers.get(qualifierId);
        if (qualifier == null) {
            qualifier = engine.getQualifier(qualifierId);
            qualifiers.put(qualifierId, qualifier);
        }
        return qualifier;
    }

    private Element getElement(Hashtable<Long, Element> elements, long elementId) {
        Element element = elements.get(elementId);
        if (element == null) {
            element = engine.getElement(elementId);
            elements.put(elementId, element);
        }
        return element;
    }

    public String decompile(String formula, final Element element) {
        Eval eval = new Eval(formula);
        eval.replaceValueNames(new Replacementable() {
            @Override
            public String getNewName(String oldName) {
                return decompileValue(element, oldName);
            }

        });
        return eval.toString();
    }

    protected String decompileValue(final Element element, String oldName) {
        MetaValue value = toMetaValue(oldName);
        Attribute attribute = engine.getAttribute(value.getAttributeId());
        if ((element != null) && (value.getElementId() == element.getId())) {
            return toCanonicalValue(attribute.getName());
        } else {
            Element e = engine.getElement(value.getElementId());
            if (e.getName().length() == 0)
                return toCanonicalValue(ELEMENT_PREFIX + e.getId() + "."
                        + attribute.getName());
            if ((element != null)
                    && (e.getQualifierId() == element.getQualifierId())) {
                return toCanonicalValue(e.getName() + "." + attribute.getName());
            } else {
                Qualifier qualifier = engine.getQualifier(e.getQualifierId());
                return toCanonicalValue(replaceDots(qualifier.getName()) + "."
                        + replaceDots(e.getName()) + "."
                        + replaceDots(attribute.getName()));
            }
        }
    }

    public String compile(String text, final Element element,
                          final Attribute attribute) {
        Eval eval = new Eval(text);
        eval.replaceValueNames(new Replacementable() {
            @Override
            public String getNewName(String oldName) {
                return compileValue(element, oldName);
            }
        });
        return eval.toString();
    }

    protected String compileValue(Element element, String value) {
        return compileValue(element, value, null);
    }

    protected String compileValue(Element element, String value,
                                  Qualifier aQualifier) {
        if (!value.startsWith("["))
            throw new UnknownValuesException(new String[]{value});
        String oldName = toUserValue(value);
        ArrayList<String> list = new ArrayList<String>(3);
        char c;
        int i = 0;
        Qualifier qualifier = aQualifier;
        if (element != null)
            qualifier = engine.getQualifier(element.getQualifierId());
        while (i < oldName.length()) {
            StringBuffer sb = new StringBuffer();
            while ((i < oldName.length()) && ((c = oldName.charAt(i)) != '.')) {
                if (c == '\\') {
                    i++;
                    if (i < oldName.length())
                        c = oldName.charAt(i);
                }
                sb.append(c);
                i++;
            }
            i++;
            list.add(sb.toString());
        }
        Attribute attr;
        if (list.size() == 1) {
            attr = getAttribute(qualifier, list.get(0));
            if (attr != null) {
                return ELEMENT_PREFIX + element.getId() + ATTRIBUTE_PREFIX
                        + attr.getId();
            }
        } else if (list.size() == 2) {
            Element e = null;
            if (qualifier == null) {
                String s = list.get(0);
                if ((s.startsWith(ELEMENT_PREFIX)) && (s.length() > 1)) {
                    long id = Long.parseLong(s.substring(ELEMENT_PREFIX
                            .length()));
                    e = engine.getElement(id);
                    if (e != null)
                        qualifier = engine.getQualifier(e.getQualifierId());
                }
            }
            if (e == null)
                e = engine.getElement(list.get(0), qualifier.getId());
            if ((e == null) && (isElementLink(list.get(0)))) {
                e = engine.getElement(Long.parseLong(list.get(0).substring(1)));
                if (e != null)
                    qualifier = engine.getQualifier(e.getQualifierId());
            }
            if (e != null) {
                attr = getAttribute(qualifier, list.get(1));
                if (attr != null) {
                    return ELEMENT_PREFIX + e.getId() + ATTRIBUTE_PREFIX
                            + attr.getId();
                }
            }
        } else if (list.size() == 3) {
            qualifier = engine.getQualifierByName(list.get(0));
            if (qualifier != null) {
                Element e = engine.getElement(list.get(1), qualifier.getId());
                if (e != null) {
                    attr = getAttribute(qualifier, list.get(2));
                    if (attr != null) {
                        return ELEMENT_PREFIX + e.getId() + ATTRIBUTE_PREFIX
                                + attr.getId();
                    }
                }
            }
        }
        throw new UnknownValuesException(new String[]{oldName});
    }

    private boolean isElementLink(String name) {
        if (name.length() < 2)
            return false;
        if (!name.startsWith(ELEMENT_PREFIX))
            return false;
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i)))
                return false;
        }
        return true;
    }

    protected Attribute getAttribute(Qualifier qualifier, String attributeName) {
        for (Attribute attribute : qualifier.getAttributes())
            if (attribute.getName().equals(attributeName))
                return attribute;
        return null;
    }

    public String toValue(long elementId, long attributeId) {
        return ELEMENT_PREFIX + elementId + ATTRIBUTE_PREFIX + attributeId;
    }

    public static String attributeIdToValue(long id) {
        return ATTRIBUTE_PREFIX + id;
    }

    public static String tableAttributeToValue(long tableAttributeId,
                                               long attributeId) {
        return ATTRIBUTE_PREFIX + tableAttributeId + ATTRIBUTE_PREFIX
                + attributeId;
    }

    public static String elementIdToValue(long elementId, long attributeId) {
        return ELEMENT_PREFIX + elementId + ATTRIBUTE_PREFIX + attributeId;
    }

    public List<Attribute> getTableAttributes(Qualifier tableQualifier,
                                              long qualifierAttributeId, Eval eval) {
        List<Attribute> res = new ArrayList<Attribute>();

        String start = ATTRIBUTE_PREFIX + qualifierAttributeId
                + ATTRIBUTE_PREFIX;

        int sLength = start.length();

        for (String value : eval.getValues()) {
            if ((value.length() > sLength) && (value.startsWith(start))) {
                long id = Long.valueOf(value.substring(sLength));
                res.add(engine.getAttribute(id));
            }
        }

        return res;
    }

    public String toValue(long attributeId) {
        return ATTRIBUTE_PREFIX + attributeId;
    }

    public ScriptHolders getScriptHolders() {
        if (scriptHolders == null) {
            scriptHolders = new ScriptHolders(engine);
        }
        return scriptHolders;
    }

    private class ScriptFunction implements Function {

        private String functionName;

        public ScriptFunction(String functionName) {
            this.functionName = functionName;
        }

        @Override
        public EObject calculate(EObject[] params) {
            if (isDisableScripts())
                return new EObject(null);
            return getScriptHolders().tryInvoke(functionName, params);
        }

    }

    public boolean isFunctionExists(String function) {
        return getScriptHolders().isFunctionExists(function);
    }

    public boolean isDisableScripts() {
        Object object = engine.getPluginProperty("Scripting", "Disable");
        if (object == null)
            return false;
        return Boolean.TRUE.equals(object);
    }
}
