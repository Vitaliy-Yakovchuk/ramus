package com.ramussoft.core.attribute.standard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Delete;
import com.ramussoft.common.DeleteStatus;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.FormulaEvent;
import com.ramussoft.common.event.FormulaListener;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.eval.EObject;
import com.ramussoft.eval.Eval;
import com.ramussoft.eval.FunctionPersistent;
import com.ramussoft.eval.MetaValue;
import com.ramussoft.eval.Replacementable;
import com.ramussoft.eval.Util;
import com.ramussoft.eval.event.FunctionsChangeEvent;
import com.ramussoft.eval.event.FunctionsChangeListener;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class EvalPlugin extends AbstractPlugin {

    private static final String EVAL = "Eval";

    private static final String FUNCTION_ATTRIBUTE = "FunctionAttribute";

    private static final String QUALIFIER_EVAL_FUNCTION_DEPENDENCES = "QUALIFIER_EVAL_FUNCTION_DEPENDENCES";

    private static final String ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE = "ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE";

    private static final String ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE_SOURCE_ATTRIBUTE = "ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE_SOURCE_ATTRIBUTE";

    private static final String ATTRIBUTE_EVAL_DEPENDENCE_QUALIFIER = "ATTRIBUTE_EVAL_DEPENDENCE_QUALIFIER";

    private static final String ATTRIBUTE_EVAL_DEPENDENCE_ATTRIBUTE = "ATTRIBUTE_EVAL_DEPENDENCE_ATTRIBUTE";

    private Attribute function;

    private Qualifier functionDependences;

    private Attribute functionDependence;

    private Attribute functionDependenceSourceAttribute;

    private Attribute functionDependenceQualifier;

    private Attribute functionDependenceAttribute;

    private Hashtable<Thread, List<MetaValue>> hashtable = new Hashtable<Thread, List<MetaValue>>();

    private boolean DISABLE_RECALC = false;// true;

    @Override
    public String getName() {
        return EVAL;
    }

    @Override
    public void init(final Engine engine, AccessRules rules) {
        super.init(engine, rules);
        Qualifier qualifier = StandardAttributesPlugin
                .getQualifiersQualifier(engine);
        engine.setPluginProperty(getName(), "Plugin", this);
        function = getFunction(qualifier);
        if (function == null) {
            function = engine.createSystemAttribute(new AttributeType(EVAL,
                    "Function", false));
            function.setName(FUNCTION_ATTRIBUTE);
            engine.updateAttribute(function);
            qualifier.getSystemAttributes().add(function);
            engine.updateQualifier(qualifier);
        }

        functionDependences = engine
                .getSystemQualifier(QUALIFIER_EVAL_FUNCTION_DEPENDENCES);
        if (functionDependences == null) {
            createEvalObjects(engine);
        } else {
            functionDependence = engine
                    .getSystemAttribute(ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE);

            functionDependenceQualifier = engine
                    .getSystemAttribute(ATTRIBUTE_EVAL_DEPENDENCE_QUALIFIER);

            functionDependenceAttribute = engine
                    .getSystemAttribute(ATTRIBUTE_EVAL_DEPENDENCE_ATTRIBUTE);

            functionDependenceSourceAttribute = engine
                    .getSystemAttribute(ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE_SOURCE_ATTRIBUTE);
        }

        if (!StandardAttributesPlugin.isDisableAutoupdate(engine)) {
            final Util util = Util.getUtils(engine);

            util.getScriptHolders().addFunctionsChangeListener(
                    new FunctionsChangeListener() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public void functionsChanged(FunctionsChangeEvent event) {

                            IEngine iEngine = engine.getDeligate();
                            if ((iEngine != null)
                                    && (iEngine instanceof IEngineImpl)) {
                                JDBCTemplate template = ((IEngineImpl) iEngine)
                                        .getTemplate();
                                String prefix = ((IEngineImpl) iEngine)
                                        .getPrefix();
                                for (String function : event.getFunctionNames()) {
                                    List<FunctionPersistent> list = template
                                            .query("SELECT function, qualifier_attribute_id, qualifier_table_attribute_id, autochange, attribute_id, element_id FROM "
                                                            + prefix
                                                            + "attribute_functions WHERE function LIKE ? AND autochange=1",
                                                    new RowMapper() {

                                                        @Override
                                                        public Object mapRow(
                                                                ResultSet rs,
                                                                int rowNum)
                                                                throws SQLException {
                                                            FunctionPersistent persistent = new FunctionPersistent(
                                                                    rs.getString(1),
                                                                    rs.getLong(2),
                                                                    rs.getLong(3),
                                                                    rs.getInt(4));
                                                            persistent
                                                                    .setAttributeId(rs
                                                                            .getLong(5));
                                                            persistent
                                                                    .setElementId(rs
                                                                            .getLong(6));
                                                            return persistent;
                                                        }
                                                    }, new Object[]{"%"
                                                            + function + "%"},
                                                    true);
                                    for (FunctionPersistent fp : list) {
                                        recalculateQualifierAttribute(
                                                engine,
                                                StandardAttributesPlugin
                                                        .getQualifier(
                                                                engine,
                                                                engine.getElement(fp
                                                                        .getElementId())),
                                                util, fp);
                                    }
                                }
                            }

                            for (String function : event.getFunctionNames()) {
                                for (CalculateInfo info : engine
                                        .findCalculateInfos("%" + function
                                                + "%", true)) {
                                    recalculate(engine, info);
                                }
                            }
                        }
                    });

            engine.addElementAttributeListener(null,
                    new ElementAttributeListener() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public void attributeChanged(AttributeEvent event) {
                            if (event.isJournaled())
                                return;
                            if (DISABLE_RECALC)
                                return;

                            if (event.getElement() == null) {
                                IEngine d = engine.getDeligate();

                                List<Element> allElements = null;

                                if ((d != null) && (d instanceof IEngineImpl)) {
                                    JDBCTemplate template = ((IEngineImpl) d)
                                            .getTemplate();
                                    String prefix = ((IEngineImpl) d)
                                            .getPrefix();
                                    allElements = template
                                            .query("SELECT * FROM "
                                                            + prefix
                                                            + "elements WHERE qualifier_id in\n"
                                                            + "(SELECT qualifier_id FROM "
                                                            + prefix
                                                            + "qualifiers_attributes WHERE attribute_id=?)",
                                                    new RowMapper() {

                                                        @Override
                                                        public Object mapRow(
                                                                ResultSet rs,
                                                                int rowNum)
                                                                throws SQLException {
                                                            return new Element(
                                                                    rs.getLong("element_id"),
                                                                    rs.getLong("qualifier_id"),
                                                                    rs.getString("element_name"));
                                                        }
                                                    }, new Object[]{event
                                                            .getAttribute()
                                                            .getId()}, true);
                                } else {
                                    Attribute attribute = event.getAttribute();
                                    allElements = new ArrayList<Element>();
                                    for (Qualifier qualifier : engine
                                            .getQualifiers()) {
                                        if (qualifier.getAttributes().indexOf(
                                                attribute) >= 0) {
                                            allElements.addAll(engine
                                                    .getElements(qualifier
                                                            .getId()));
                                        }
                                    }
                                }
                                Attribute attribute = event.getAttribute();
                                for (Element element : allElements) {
                                    recalculateElement(engine,
                                            new AttributeEvent(engine, element,
                                                    attribute, null, null));
                                }
                            } else
                                recalculateElement(engine, event);
                        }

                        private void recalculateElement(final Engine engine,
                                                        AttributeEvent event) {
                            MetaValue metaValue = new MetaValue(event
                                    .getElement().getId(), event.getAttribute()
                                    .getId());

                            List<MetaValue> metaValueList = hashtable
                                    .get(Thread.currentThread());
                            if (metaValueList == null) {
                                metaValueList = new ArrayList<MetaValue>();
                                hashtable.put(Thread.currentThread(),
                                        metaValueList);
                            } else {
                                if (metaValueList.indexOf(metaValue) >= 0)
                                    return;
                            }
                            metaValueList.add(metaValue);

                            try {
                                for (CalculateInfo info : engine
                                        .getDependences(event.getElement()
                                                .getId(), event.getAttribute()
                                                .getId(), true)) {
                                    recalculate(engine, info);
                                }

                                List<Element> elements = engine.findElements(
                                        functionDependences.getId(),
                                        functionDependence, event.getElement()
                                                .getId());
                                for (Element e : elements) {
                                    Qualifier qualifier = engine.getQualifier((Long) engine
                                            .getAttribute(e,
                                                    functionDependenceQualifier));
                                    Attribute attribute = engine.getAttribute((Long) engine
                                            .getAttribute(e,
                                                    functionDependenceAttribute));

                                    if ((qualifier != null)
                                            && (attribute != null)) {
                                        Element el = StandardAttributesPlugin
                                                .getElement(event.getEngine(),
                                                        qualifier.getId());
                                        if (el != null)
                                            for (Element child : engine
                                                    .getElements(qualifier
                                                            .getId())) {
                                                recalculateInQualifier(
                                                        engine,
                                                        Util.elementIdToValue(
                                                                event.getElement()
                                                                        .getId(),
                                                                event.getAttribute()
                                                                        .getId()),
                                                        null, el, child);
                                            }
                                    }
                                }

                                Element element = StandardAttributesPlugin
                                        .getElement(event.getEngine(), event
                                                .getElement().getQualifierId());

                                if (element == null) {
                                    Qualifier qualifier = engine
                                            .getQualifier(event.getElement()
                                                    .getQualifierId());
                                    if (StandardAttributesPlugin
                                            .isTableQualifier(qualifier)) {
                                        Element parent = StandardAttributesPlugin
                                                .getElementForTableElement(
                                                        engine,
                                                        event.getElement());

                                        if (parent == null)
                                            return;
                                        Qualifier main = engine
                                                .getQualifier(parent
                                                        .getQualifierId());

                                        Attribute tableAttribute = null;
                                        for (Attribute attr : main
                                                .getAttributes()) {
                                            if (StandardAttributesPlugin
                                                    .getTableQualifeirName(attr)
                                                    .equals(qualifier.getName())) {
                                                tableAttribute = attr;
                                                break;
                                            }
                                        }
                                        if (tableAttribute == null)
                                            return;

                                        element = StandardAttributesPlugin.getElement(
                                                event.getEngine(),
                                                parent.getQualifierId());
                                        if (element != null) {
                                            if (event
                                                    .getAttribute()
                                                    .equals(StandardAttributesPlugin
                                                            .getTableElementIdAttribute(engine)))
                                                recalculateInQualifier(engine,
                                                        null,
                                                        event.getElement(),
                                                        element, parent);
                                            else

                                                recalculateInQualifier(
                                                        engine,
                                                        Util.tableAttributeToValue(
                                                                tableAttribute
                                                                        .getId(),
                                                                event.getAttribute()
                                                                        .getId()),
                                                        event.getElement(),
                                                        element, parent);
                                        }
                                    }
                                    return;
                                }

                                recalculateInQualifier(engine, Util
                                                .attributeIdToValue(event
                                                        .getAttribute().getId()), null,
                                        element, event.getElement());
                            } finally {
                                metaValueList.remove(metaValueList.size() - 1);
                            }
                        }

                    });
            engine.addElementListener(null, new ElementAdapter() {
                @SuppressWarnings("unchecked")
                @Override
                public void elementCreated(ElementEvent event) {
                    if (event.isJournaled())
                        return;

                    Element element = StandardAttributesPlugin.getElement(event
                            .getEngine(), event.getNewElement()
                            .getQualifierId());

                    if (element == null) {
                        return;
                    }

                    List<FunctionPersistent> list = getFunctionAttribute(
                            engine, element);
                    if (list == null)
                        return;

                    Util utils = Util.getUtils(engine);

                    for (FunctionPersistent fp : list)
                        if (fp.getAutochange() != 0) {

                            Eval eval = new Eval(fp.getFunction());

                            try {

                                utils.fillAttributes(eval, engine
                                                .getQualifier(event.getNewElement()
                                                        .getQualifierId()), event
                                                .getNewElement(), null, null, null,
                                        true);
                                utils.fillResult(fp.getQualifierAttributeId(),
                                        eval, event.getNewElement());
                            } catch (Exception e) {
                                utils.fillResult(fp.getQualifierAttributeId(),
                                        e, event.getNewElement());
                                if (e instanceof RuntimeException)
                                    throw (RuntimeException) e;
                                throw new RuntimeException(e);
                            } finally {

                            }
                        }
                }

                @SuppressWarnings("unchecked")
                @Override
                public void beforeElementDeleted(final ElementEvent event) {
                    if ((event.isJournaled())
                            || (event.getNewElement() != null))
                        return;

                    Element oldElement = event.getOldElement();
                    Qualifier qualifier = engine.getQualifier(oldElement
                            .getQualifierId());

                    for (Attribute attribute : qualifier.getAttributes()) {
                        removeAttributeFromCalculateInfo(engine, oldElement,
                                attribute);
                        CalculateInfo info = engine.getCalculateInfo(
                                oldElement.getId(), attribute.getId());
                        if (info != null) {
                            info.setFormula(null);
                            engine.setCalculateInfo(info);
                        }
                    }

                    List<Element> elements = engine.findElements(
                            functionDependences.getId(), functionDependence,
                            oldElement.getId());
                    Vector<Long> qualifiers = new Vector<Long>(elements.size());
                    final String start = Util.ELEMENT_PREFIX
                            + oldElement.getId();

                    for (Element element : elements) {
                        Long q = (Long) engine.getAttribute(element,
                                functionDependenceQualifier);
                        if (qualifiers.indexOf(q) < 0) {
                            qualifiers.add(q);
                            Element qElement = StandardAttributesPlugin
                                    .getElement(engine, q.longValue());
                            List<FunctionPersistent> fpl = (List<FunctionPersistent>) engine
                                    .getAttribute(qElement, function);
                            for (FunctionPersistent fp : fpl) {
                                try {
                                    Eval eval = new Eval(fp.getFunction());
                                    eval.replaceValueNames(new Replacementable() {
                                        @Override
                                        public String getNewName(String oldName) {
                                            if (oldName.startsWith(start))
                                                return "NULL";
                                            return oldName;
                                        }
                                    });
                                    fp.setFunction(eval.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            engine.setAttribute(qElement, function, fpl);
                        }
                        // engine.deleteElement(element.getId());
                    }
                }

            });

            engine.addQualifierListener(new QualifierAdapter() {

                @SuppressWarnings("unchecked")
                @Override
                public void beforeQualifierUpdated(QualifierEvent event) {
                    List<Attribute> rem = new ArrayList<Attribute>();
                    for (Attribute a : event.getOldQualifier().getAttributes()) {
                        if (event.getNewQualifier().getAttributes().indexOf(a) < 0)
                            rem.add(a);
                    }
                    if (rem.size() > 0) {
                        List<Element> list = engine.getElements(event
                                .getOldQualifier().getId());
                        for (Attribute attribute : rem) {
                            for (Element element : list) {
                                removeAttributeFromCalculateInfo(engine,
                                        element, attribute);
                                CalculateInfo info = engine.getCalculateInfo(
                                        element.getId(), attribute.getId());
                                if (info != null) {
                                    info.setFormula(null);
                                    engine.setCalculateInfo(info);
                                }
                            }

                            List<Element> elements = engine.findElements(
                                    functionDependences.getId(),
                                    functionDependenceSourceAttribute,
                                    attribute.getId());
                            final String end = Util.ATTRIBUTE_PREFIX
                                    + attribute.getId();

                            for (Element element : elements) {
                                Long id = (Long) engine.getAttribute(element,
                                        functionDependence);
                                if (engine.getQualifierIdForElement(id) == event
                                        .getNewQualifier().getId()) {
                                    Long qId = (Long) engine.getAttribute(
                                            element,
                                            functionDependenceQualifier);
                                    Element element2 = StandardAttributesPlugin
                                            .getElement(engine, qId);
                                    List<FunctionPersistent> fpl = (List<FunctionPersistent>) engine
                                            .getAttribute(element2, function);
                                    for (FunctionPersistent fp : fpl) {
                                        Eval eval = new Eval(fp.getFunction());
                                        eval.replaceValueNames(new Replacementable() {
                                            @Override
                                            public String getNewName(
                                                    String oldName) {
                                                if (oldName.endsWith(end)) {
                                                    return "NULL";
                                                }
                                                return oldName;
                                            }
                                        });
                                        fp.setFunction(eval.toString());
                                    }
                                    engine.setAttribute(element2, function, fpl);
                                }
                            }

                        }
                    }
                }

                @Override
                public void qualifierDeleted(QualifierEvent event) {
                    Qualifier qualifier = event.getOldQualifier();
                    for (Element element : engine.findElements(
                            functionDependences.getId(),
                            functionDependenceQualifier, qualifier.getId())) {
                        engine.deleteElement(element.getId());
                    }
                }
            });

            Qualifier qq = StandardAttributesPlugin
                    .getQualifiersQualifier(engine);
            engine.addElementAttributeListener(qq,
                    new ElementAttributeListener() {
                        @Override
                        public void attributeChanged(AttributeEvent event) {
                            if (event.isJournaled())
                                return;
                            if (function.equals(event.getAttribute())) {

                                recalculateFunctionOfQualifier(engine, event);
                            }
                        }

                    });
            engine.addFormulaListener(new FormulaListener() {

                @Override
                public void formulaChanged(FormulaEvent event) {
                    if (event.isJournaled())
                        return;
                    CalculateInfo info = event.getNewFormula();
                    recalculate(engine, info);
                }

            });

        }
    }

    @SuppressWarnings("unchecked")
    private void recalculateFunctionOfQualifier(final Engine engine,
                                                AttributeEvent event) {
        Qualifier q = StandardAttributesPlugin.getQualifier(engine,
                event.getElement());

        List<Attribute> attrs = new ArrayList<Attribute>();
        attrs.add(functionDependenceAttribute);
        attrs.add(functionDependenceQualifier);

        Hashtable<Element, Object[]> deps = engine.getElements(
                functionDependences, attrs);

        for (Attribute qa : q.getAttributes()) {
            for (Entry<Element, Object[]> entry : deps.entrySet()) {
                Object[] value = entry.getValue();
                if ((((((Long) value[0]).longValue() == qa.getId()) || ((Long) value[0])
                        .longValue() == -1l))
                        && (((Long) value[1]).longValue() == q.getId())) {
                    engine.deleteElement(entry.getKey().getId());
                }
            }
        }

        if (event.getNewValue() != null) {

            Util utils = Util.getUtils(engine);

            List<FunctionPersistent> list = (List<FunctionPersistent>) event
                    .getNewValue();
            for (FunctionPersistent fp : list) {
                if (fp.getAutochange() != 0) {
                    Eval eval = new Eval(fp.getFunction());
                    for (MetaValue value : utils.toMetaValues(eval.getValues())) {
                        if (value != null) {
                            Element element = engine
                                    .createElement(functionDependences.getId());
                            engine.setAttribute(element, functionDependence,
                                    value.getElementId());
                            engine.setAttribute(element,
                                    functionDependenceSourceAttribute,
                                    value.getAttributeId());
                            engine.setAttribute(element,
                                    functionDependenceAttribute,
                                    fp.getQualifierAttributeId());
                            engine.setAttribute(element,
                                    functionDependenceQualifier, q.getId());
                        }
                    }
                    recalculateQualifierAttribute(engine, q, utils, fp);
                }
            }
        }
    }

    private void recalculateQualifierAttribute(final Engine engine,
                                               Qualifier q, Util utils, FunctionPersistent fp) {
        Eval eval = new Eval(fp.getFunction());

        if (fp.getQualifierTableAttributeId() == -1l) {
            List<Attribute> attributes = utils.getAttributes(q, eval);
            Hashtable<Element, Object[]> data = engine.getElements(q,
                    attributes);
            for (Entry<Element, Object[]> entry : data.entrySet()) {
                Object[] objects = entry.getValue();
                Element element = entry.getKey();
                for (int i = 0; i < objects.length; i++) {
                    eval.setValue(utils.toValue(attributes.get(i).getId()),
                            new EObject(objects[i], element, attributes.get(i),
                                    engine));
                }

                utils.fillMetaValues(eval);

                try {
                    utils.fillResult(fp.getQualifierAttributeId(), eval,
                            element);
                } catch (Exception e) {
                    utils.fillResult(fp.getQualifierAttributeId(), e, element);
                }
            }
        } else {
            List<Attribute> attributes = utils.getAttributes(q, eval);
            Hashtable<Element, Object[]> data = engine.getElements(q,
                    attributes);
            for (Entry<Element, Object[]> entry : data.entrySet()) {
                Object[] objects = entry.getValue();
                Element element = entry.getKey();
                for (int i = 0; i < objects.length; i++) {
                    eval.setValue(utils.toValue(attributes.get(i).getId()),
                            new EObject(objects[i], element, attributes.get(i),
                                    engine));
                }

                utils.fillMetaValues(eval);

                Qualifier tableQualifier = StandardAttributesPlugin
                        .getTableQualifierForAttribute(engine,
                                fp.getQualifierAttributeId());

                List<Element> table = StandardAttributesPlugin
                        .getTableElements(engine, engine.getAttribute(fp
                                .getQualifierAttributeId()), element);

                List<Attribute> tableAttributes = utils.getTableAttributes(
                        tableQualifier, fp.getQualifierAttributeId(), eval);

                for (Element tableElement : table)

                    try {
                        for (Attribute tableAttribute : tableAttributes) {
                            Object value = engine.getAttribute(tableElement,
                                    tableAttribute);
                            eval.setValue(Util.tableAttributeToValue(
                                    fp.getQualifierAttributeId(),
                                    tableAttribute.getId()), new EObject(value,
                                    tableElement, tableAttribute, engine));
                        }

                        utils.fillResult(fp.getQualifierTableAttributeId(),
                                eval, tableElement);
                    } catch (Exception e) {
                        utils.fillResult(fp.getQualifierTableAttributeId(), e,
                                tableElement);
                    }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void recalculateInQualifier(final Engine engine,
                                        String attributeValue, Element tableElement2, Element element,
                                        Element resElement) {
        List<FunctionPersistent> list = getFunctionAttribute(engine, element);

        Util utils = Util.getUtils(engine);

        for (FunctionPersistent fp : list)
            if (fp.getAutochange() != 0) {

                Eval eval = new Eval(fp.getFunction());
                if ((attributeValue == null)
                        || (eval.isValuePresent(attributeValue))
                        || (isElementPesent(eval))) {
                    if (fp.getQualifierTableAttributeId() == -1l) {

                        try {

                            utils.fillAttributes(eval, engine
                                            .getQualifier(resElement.getQualifierId()),
                                    resElement, null, null, null, true);
                            utils.fillResult(fp.getQualifierAttributeId(),

                                    eval, resElement);
                        } catch (Exception e) {
                            utils.fillResult(fp.getQualifierAttributeId(), e,
                                    resElement);
                            if (e instanceof RuntimeException)
                                throw (RuntimeException) e;
                            throw new RuntimeException(e);
                        } finally {

                        }
                    } else {
                        Attribute tableAttribute = engine.getAttribute(fp
                                .getQualifierAttributeId());
                        Qualifier tableQualifier = StandardAttributesPlugin
                                .getTableQualifierForAttribute(engine,
                                        tableAttribute);
                        for (Element tableElement : StandardAttributesPlugin
                                .getTableElements(engine, tableAttribute,
                                        resElement))
                            if ((tableElement2 == null)
                                    || (tableElement2.equals(tableElement))) {
                                try {

                                    utils.fillAttributes(eval, engine
                                                    .getQualifier(resElement
                                                            .getQualifierId()),
                                            resElement, tableAttribute,
                                            tableQualifier, tableElement, true);
                                    utils.fillResult(
                                            fp.getQualifierTableAttributeId(),
                                            eval, tableElement);
                                } catch (Exception e) {
                                    utils.fillResult(
                                            fp.getQualifierAttributeId(), e,
                                            tableElement);
                                    if (e instanceof RuntimeException)
                                        throw (RuntimeException) e;
                                    throw new RuntimeException(e);
                                } finally {

                                }
                            }
                    }
                }
            }
    }

    private boolean isElementPesent(Eval eval) {
        for (String value : eval.getValues()) {
            if ("ELEMENT".equals(value))
                return true;
        }
        return false;
    }

    private void createEvalObjects(Engine engine) {
        functionDependence = engine.createSystemAttribute(new AttributeType(
                "Core", "Long"));
        functionDependence.setName(ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE);
        engine.updateAttribute(functionDependence);

        functionDependenceQualifier = engine
                .createSystemAttribute(new AttributeType("Core", "Long"));
        functionDependenceQualifier
                .setName(ATTRIBUTE_EVAL_DEPENDENCE_QUALIFIER);
        engine.updateAttribute(functionDependenceQualifier);

        functionDependenceAttribute = engine
                .createSystemAttribute(new AttributeType("Core", "Long"));
        functionDependenceAttribute
                .setName(ATTRIBUTE_EVAL_DEPENDENCE_ATTRIBUTE);
        engine.updateAttribute(functionDependenceAttribute);

        functionDependenceSourceAttribute = engine
                .createSystemAttribute(new AttributeType("Core", "Long"));
        functionDependenceSourceAttribute
                .setName(ATTRIBUTE_EVAL_FUNCTION_DEPENDENCE_SOURCE_ATTRIBUTE);
        engine.updateAttribute(functionDependenceSourceAttribute);

        functionDependences = engine.createSystemQualifier();
        functionDependences.setName(QUALIFIER_EVAL_FUNCTION_DEPENDENCES);
        functionDependences.getAttributes().add(functionDependence);
        functionDependences.getAttributes().add(functionDependenceQualifier);
        functionDependences.getAttributes().add(functionDependenceAttribute);
        functionDependences.getAttributes().add(
                functionDependenceSourceAttribute);
        engine.updateQualifier(functionDependences);
    }

    private void recalculate(final Engine engine, CalculateInfo info) {
        if (info.getFormula() != null) {
            Util utils = Util.getUtils(engine);
            Element element = engine.getElement(info.getElementId());
            Eval eval = utils.createAndFillEval(info, true);
            try {
                utils.fillResult(info.getAttributeId(), eval, element);
            } catch (Exception e) {
                utils.fillResult(info.getAttributeId(), e, element);
            }
        }
    }

    private Attribute getFunction(Qualifier qualifier) {
        for (Attribute attr : qualifier.getSystemAttributes()) {
            if (attr.getName().equals(FUNCTION_ATTRIBUTE))
                return attr;
        }
        return null;
    }

    public static void setFunctionAttribute(Engine engine, Element element,
                                            Object value) {
        if (value != null)
            engine.setAttribute(element, getFunctionAttribute(engine),
                    new ArrayList((ArrayList) value));
    }

    public static List<FunctionPersistent> getFunctionAttribute(Engine engine,
                                                                Element element) {
        List<FunctionPersistent> v = (List<FunctionPersistent>) engine
                .getAttribute(element, getFunctionAttribute(engine));
        if (v == null)
            return new ArrayList<FunctionPersistent>(0);
        return new ArrayList<FunctionPersistent>(v);
    }

    public static Attribute getFunctionAttribute(Engine engine) {
        EvalPlugin plugin = getPlugin(engine);
        return plugin.function;
    }

    public static Attribute getFunctionDependence(Engine engine) {
        EvalPlugin plugin = getPlugin(engine);
        return plugin.functionDependence;
    }

    public static Attribute getFunctionDependenceSourceAttribute(Engine engine) {
        EvalPlugin plugin = getPlugin(engine);
        return plugin.functionDependenceSourceAttribute;
    }

    public static Attribute getFunctionDependenceAttribute(Engine engine) {
        EvalPlugin plugin = getPlugin(engine);
        return plugin.functionDependenceAttribute;
    }

    public static Attribute getFunctionDependenceQualifier(Engine engine) {
        EvalPlugin plugin = getPlugin(engine);
        return plugin.functionDependenceQualifier;
    }

    public static Qualifier getFunctionDependences(Engine engine) {
        EvalPlugin plugin = getPlugin(engine);
        return plugin.functionDependences;
    }

    private static EvalPlugin getPlugin(Engine engine) {
        EvalPlugin plugin = (EvalPlugin) engine.getPluginProperty(EVAL,
                "Plugin");
        return plugin;
    }

    @SuppressWarnings("unchecked")
    public static List<FunctionPersistent> getFunctions(Engine engine,
                                                        Qualifier qualifier) {
        Element element = StandardAttributesPlugin.getElement(engine,
                qualifier.getId());
        List<FunctionPersistent> res = getFunctionAttribute(engine, element);
        if (res == null)
            return new ArrayList<FunctionPersistent>(0);
        return res;
    }

    public static void calculate(Engine engine, Qualifier qualifier,
                                 List<FunctionPersistent> functions) {

        Util utils = Util.getUtils(engine);

        Eval[] evals = utils.sort(functions);
        List<Attribute> sources = new ArrayList<Attribute>();
        for (Eval eval : evals) {
            utils.addAttributes(sources, eval, qualifier.getAttributes());
        }
        Hashtable<Element, Object[]> hash = engine.getElements(qualifier,
                sources);
        for (Entry<Element, Object[]> entry : hash.entrySet()) {
            Element element = entry.getKey();
            Object[] values = entry.getValue();
            int i = 0;
            for (Eval eval : evals) {
                utils.fillAttributes(eval, element, sources, values, true);
                FunctionPersistent fp = functions.get(i);
                if (fp.getQualifierTableAttributeId() == -1l) {
                    try {
                        Object object = utils.fillResult(
                                fp.getQualifierAttributeId(), eval, element);
                        for (int j = 0; j < values.length; j++) {
                            if (sources.get(j).getId() == fp
                                    .getQualifierAttributeId())
                                values[j] = object;
                        }
                    } catch (Exception e) {
                        utils.fillResult(fp.getQualifierAttributeId(), e,
                                element);
                    }
                } else {
                    Attribute tableAttribute = engine.getAttribute(fp
                            .getQualifierAttributeId());
                    Qualifier tableQualifier = StandardAttributesPlugin
                            .getTableQualifierForAttribute(engine,
                                    tableAttribute);
                    List<Element> tableElements = StandardAttributesPlugin
                            .getTableElements(engine, tableAttribute, element);
                    List<Attribute> tableAttributes = utils.getTableAttributes(
                            tableQualifier, fp.getQualifierAttributeId(), eval);
                    for (Element tableElement : tableElements) {
                        for (Attribute attr : tableAttributes) {
                            Object value = engine.getAttribute(tableElement,
                                    attr);
                            eval.setValue(
                                    Util.tableAttributeToValue(
                                            fp.getQualifierAttributeId(),
                                            attr.getId()), new EObject(value,
                                            tableElement, attr, engine));
                        }
                        try {
                            utils.fillResult(fp.getQualifierTableAttributeId(),
                                    eval, tableElement);
                        } catch (Exception e) {
                            utils.fillResult(fp.getQualifierTableAttributeId(),
                                    e, tableElement);
                        }

                    }
                }
                i++;
            }
        }
    }

    private void removeAttributeFromCalculateInfo(final Engine engine,
                                                  final Element element, final Attribute attribute) {
        for (CalculateInfo info : engine.getDependences(element.getId(),
                attribute.getId(), false)) {
            try {
                Eval eval = new Eval(info.getFormula());
                if (eval.replaceValueNames(new Replacementable() {

                    private String value = Util.ELEMENT_PREFIX
                            + element.getId() + Util.ATTRIBUTE_PREFIX
                            + attribute.getId();

                    @Override
                    public String getNewName(String oldName) {
                        if (oldName.equals(value))
                            return "NULL";
                        return null;
                    }
                })) {
                    info.setFormula(eval.toString());
                    engine.setCalculateInfo(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public DeleteStatus getElementsDeleteStatus(long[] elementIds,
                                                IEngine engine) {
        if (elementIds.length == 0)
            return null;
        Qualifier qualifier = engine.getQualifier(engine
                .getQualifierIdForElement(elementIds[0]));
        StringBuffer sb = new StringBuffer();
        List<MetaValue> list = new ArrayList<MetaValue>();

        for (long elementId : elementIds)
            for (Attribute attr : qualifier.getAttributes()) {
                for (CalculateInfo info : engine.getDependences(elementId,
                        attr.getId(), false))
                    if (info.getElementId() != elementId) {
                        MetaValue value = new MetaValue(info.getElementId(),
                                info.getAttributeId());
                        if (list.indexOf(value) < 0) {
                            list.add(value);
                            sb.append("<tr>");
                            Element element = engine.getElement(info
                                    .getElementId());
                            Qualifier qualifier2 = qualifier;
                            if (element.getQualifierId() != qualifier.getId()) {
                                qualifier2 = engine.getQualifier(element
                                        .getQualifierId());
                            }
                            Attribute attribute = engine.getAttribute(info
                                    .getAttributeId());
                            sb.append("<td>");
                            sb.append(qualifier2.getName());
                            sb.append("</td>");
                            sb.append("<td>");
                            sb.append(element.getName());
                            sb.append("</td>");
                            sb.append("<td>");
                            sb.append(attribute.getName());
                            sb.append("</td>");
                            sb.append("</tr>");
                        }
                    }
            }
        String string = sb.toString();
        if (string.equals(""))
            return null;

        sb = new StringBuffer();
        sb.append("{Eval.Function.Warning}");
        sb.append("<br>");
        sb.append("<table>");
        sb.append("<tr><td><b>{OtherElement.Qualifier}</b></td><td><b>{AttributeType.Core.OtherElement}</b></td><td><b>{OtherElement.Attribute}</b></td></tr>");
        sb.append(string);
        sb.append("</table>");
        DeleteStatus status = new DeleteStatus();
        status.setDelete(Delete.WARNING);
        status.setPluginName("Core");
        status.setPluginAnswer(sb.toString());
        return status;
    }

    @Override
    public void replaceElements(Engine engine, Element[] oldElements,
                                Element newElement) {
        Hashtable<Long, Qualifier> qualifiers = new Hashtable<Long, Qualifier>();
        Qualifier main = getQualifier(newElement.getQualifierId(), qualifiers,
                engine);
        for (Element element : oldElements) {
            Qualifier qualifier = getQualifier(element.getQualifierId(),
                    qualifiers, engine);
            for (Attribute attr : qualifier.getAttributes()) {
                for (CalculateInfo info : engine.getDependences(
                        element.getId(), attr.getId(), false)) {
                    Eval eval = new Eval(info.getFormula());
                    final String old = Util.ELEMENT_PREFIX + element.getId()
                            + Util.ATTRIBUTE_PREFIX + attr.getId();
                    Attribute attribute = null;
                    for (Attribute a : main.getAttributes())
                        if (a.getId() == attr.getId())
                            attribute = a;
                    final String newValue;
                    if (attribute == null)
                        newValue = "NULL";
                    else
                        newValue = Util.ELEMENT_PREFIX + newElement.getId()
                                + Util.ATTRIBUTE_PREFIX + attribute.getId();
                    eval.replaceValueNames(new Replacementable() {
                        @Override
                        public String getNewName(String oldName) {
                            if (oldName.equals(old))
                                return newValue;
                            return null;
                        }
                    });
                    info.setFormula(eval.toString());
                    engine.setCalculateInfo(info);
                }
            }
        }
    }

    private Qualifier getQualifier(long qualifierId,
                                   Hashtable<Long, Qualifier> qualifiers, Engine engine) {
        Qualifier qualifier = qualifiers.get(qualifierId);
        if (qualifier == null) {
            qualifier = engine.getQualifier(qualifierId);
            qualifiers.put(qualifierId, qualifier);
        }
        return qualifier;
    }
}
