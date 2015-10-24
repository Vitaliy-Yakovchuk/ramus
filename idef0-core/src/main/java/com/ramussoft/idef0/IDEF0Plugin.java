package com.ramussoft.idef0;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.core.attribute.simple.HierarchicalPlugin;
import com.ramussoft.core.attribute.simple.OtherElementPropertyPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.StringCollator;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.idef0.attribute.RectangleVisualOptions;

public class IDEF0Plugin extends AbstractPlugin {

    private static final String IDEF0 = "IDEF0";

    public static final String F_PAGE_SIZE = "F_PAGE_SIZE";

    public static final String F_VISUAL_DATA = "F_VISUAL_DATA";

    public static final String F_BACKGROUND = "F_BACKGROUND";

    public static final String F_FOREGROUND = "F_FOREGROUND";

    public static final String F_BOUNDS = "F_BOUNDS";

    public static final String F_FONT = "F_FONT";

    public static final String F_STATUS = "F_STATUS";

    public static final String F_TYPE = "F_TYPE";

    public static final String F_OUNER_ID = "F_OUNER_ID";

    public static final String F_DECOMPOSITION_TYPE = "F_DECOMPOSITION_TYPE";

    public static final String F_SECTOR_FUNCTION = "F_FUNCTION_SECTOR";

    public static final String F_SECTOR_STREAM = "F_SECTOR_STREAM";

    public static final String F_SECTOR_POINTS = "F_SECTOR_POINTS";

    public static final String F_SECTOR_PROPERTIES = "F_SECTOR_PROPERTIES";

    public static final String F_SECTOR_ATTRIBUTE = "F_SECTOR_ATTRIBUTE";

    public static final String F_SECTOR_BORDER_START = "F_SECTOR_BORDER_START";

    public static final String F_SECTOR_BORDER_END = "F_SECTOR_BORDER_END";

    public static final String F_SECTORS = "F_SECTORS";

    public static final String F_STREAMS = "F_STREAMS";

    public static final String F_STREAM_NAME = "F_STREAM_NAME";

    public static final String F_STREAM_ADDED = "F_STREAM_ADDED";

    public static final String F_AUTHOR = "F_AUTHOR";

    public static final String F_CREATE_DATE = "F_CREATE_DATE";

    public static final String F_REV_DATE = "F_REV_DATE";

    public static final String F_SYSTEM_REV_DATE = "F_SYSTEM_REV_DATE";

    public static final String F_BASE_FUNCTIONS = "F_BASE_FUNCTIONS";

    public static final String F_PROJECT_PREFERENCES = "F_PROJECT_PREFERENCES";

    public static final String F_BASE_FUNCTION_QUALIFIER_ID = "F_BASE_FUNCTION_QUALIFIER_ID";

    public static final String F_LINK = "F_LINK";

    private static final String PLUGIN = "IDEF0_PLUGIN";

    private static final String CROSSPOINTS = "F_QUALIFIER_CROSSPOINT";

    private static final String CROSSPOINTS_SEQUENCE = "crosspoint_sequence";

    private static final String ORDINATES_SEQUENCE = "ordinates__sequence";

    private static final String CROSSPOINT_ID_ATTRIBUTE = "F_CROSSPOINT_ID_ATTRIBUTE";

    public static final String F_MODEL_TREE = "F_MODEL_TREE";

    private List<Attribute> functionAttributes = new ArrayList<Attribute>();

    private List<Attribute> visualAttributes = new ArrayList<Attribute>();

    private boolean justCreated;

    private Attribute baseFunctionQualifierId;

    private Qualifier baseFunctions;

    private Attribute projectPreferencesAttrtibute;

    @Override
    public String getName() {
        return IDEF0;
    }

    @Override
    public void init(final Engine engine, AccessRules accessor) {
        super.init(engine, accessor);
        engine.setPluginProperty(IDEF0, PLUGIN, this);

        Qualifier crosspoints = getQualifier(CROSSPOINTS);
        if (crosspoints != null) {
            List<Element> elements = engine.getElements(crosspoints.getId());
            Attribute crosspointId = getSysteAttribute(CROSSPOINT_ID_ATTRIBUTE);
            if (elements.size() > 0) {
                Element element = elements.get(0);

                try {
                    long long1 = (Long) engine.getAttribute(element,
                            crosspointId);
                    while ((long1 > engine.nextValue(CROSSPOINTS_SEQUENCE)))
                        ;
                } catch (NullPointerException e) {

                }
                engine.deleteElement(element.getId());
            }
            crosspoints.getSystemAttributes().clear();
            engine.updateQualifier(crosspoints);
            try {
                engine.deleteAttribute(crosspointId.getId());
            } catch (Exception e) {

            }
            engine.deleteQualifier(crosspoints.getId());
        }

        functionAttributes.add(createAttribute(F_VISUAL_DATA,
                new AttributeType(IDEF0, "VisualData", false)));
        functionAttributes.add(createAttribute(F_PAGE_SIZE, new AttributeType(
                "Core", "Text", true)));
        functionAttributes.add(createAttribute(F_BACKGROUND, new AttributeType(
                IDEF0, "Color", false)));
        functionAttributes.add(createAttribute(F_FOREGROUND, new AttributeType(
                IDEF0, "Color", false)));
        functionAttributes.add(createAttribute(F_BOUNDS, new AttributeType(
                IDEF0, "FRectangle", false)));
        functionAttributes.add(createAttribute(F_FONT, new AttributeType(IDEF0,
                "Font", false)));
        functionAttributes.add(createAttribute(F_STATUS, new AttributeType(
                IDEF0, "Status", false)));
        functionAttributes.add(createAttribute(F_TYPE, new AttributeType(IDEF0,
                "Type", false)));
        functionAttributes.add(createAttribute(F_OUNER_ID, new AttributeType(
                IDEF0, "OunerId", false)));

        functionAttributes.add(createAttribute(F_DECOMPOSITION_TYPE,
                new AttributeType(IDEF0, "DecompositionType", false)));

        functionAttributes.add(createAttribute(F_AUTHOR, new AttributeType(
                "Core", "Text", true)));

        functionAttributes.add(createAttribute(F_CREATE_DATE,
                new AttributeType("Core", "Date", false)));

        functionAttributes.add(createAttribute(F_REV_DATE, new AttributeType(
                "Core", "Date", false)));

        functionAttributes.add(createAttribute(F_SYSTEM_REV_DATE,
                new AttributeType("Core", "Date", false)));

        functionAttributes.add(createAttribute(F_LINK, new AttributeType(
                "Core", "Long", false)));

        Attribute sectorFunction = createAttribute(F_SECTOR_FUNCTION,
                new AttributeType("Core", "OtherElement", false));
        Attribute sStream = createAttribute(F_SECTOR_STREAM, new AttributeType(
                "Core", "OtherElement", false));

        Attribute sPoints = createAttribute(F_SECTOR_POINTS, new AttributeType(
                IDEF0, "SectorPoint", false));

        Attribute sProperties = createAttribute(F_SECTOR_PROPERTIES,
                new AttributeType(IDEF0, "SectorProperties", false));

        Attribute sStreamName = createAttribute(F_STREAM_NAME,
                new AttributeType("Core", "Text", true));

        Attribute aSector = createAttribute(F_SECTOR_ATTRIBUTE,
                new AttributeType(IDEF0, "Sector", false));

        Attribute aSectorBorderStart = createAttribute(F_SECTOR_BORDER_START,
                new AttributeType(IDEF0, "SectorBorder", false));

        Attribute aSectorBorderEnd = createAttribute(F_SECTOR_BORDER_END,
                new AttributeType(IDEF0, "SectorBorder", false));

        Attribute aStreamAdded = createAttribute(F_STREAM_ADDED,
                new AttributeType(IDEF0, "AnyToAny", false));

        baseFunctionQualifierId = createAttribute(F_BASE_FUNCTION_QUALIFIER_ID,
                new AttributeType("Core", "Long", true));

        visualAttributes.add(getAttribute(engine, F_BOUNDS));
        visualAttributes.add(getAttribute(engine, F_FONT));
        visualAttributes.add(getAttribute(engine, F_BACKGROUND));
        visualAttributes.add(getAttribute(engine, F_FOREGROUND));

        Qualifier sectors = createQualifier(F_SECTORS);
        if (justCreated) {
            sectors.getSystemAttributes().add(aSector);
            sectors.getSystemAttributes().add(aSectorBorderStart);
            sectors.getSystemAttributes().add(aSectorBorderEnd);
            sectors.getSystemAttributes().add(sectorFunction);
        }

        Qualifier streams = createQualifier(F_STREAMS);

        boolean updateSectors = false;

        if (justCreated) {
            Attribute hierarchical = (Attribute) engine.getPluginProperty(
                    "Core", HierarchicalPlugin.HIERARHICAL_ATTRIBUTE);

            OtherElementPropertyPersistent p = new OtherElementPropertyPersistent();
            p.setQualifier(sectors.getId());
            p.setQualifierAttribute(sStreamName.getId());
            engine.setAttribute(null, sStream, p);
            streams.getSystemAttributes().add(aStreamAdded);
            streams.getSystemAttributes().add(hierarchical);
            streams.getSystemAttributes().add(sStreamName);
            streams.setAttributeForName(sStreamName.getId());
            engine.updateQualifier(streams);
            sectors.getSystemAttributes().add(0, sStream);
            sectors.getSystemAttributes().add(hierarchical);
            updateSectors = true;
        }

        if (sectors.getSystemAttributes().indexOf(sStream) != 0) {
            sectors.getSystemAttributes().remove(sStream);
            sectors.getSystemAttributes().add(0, sStream);
            updateSectors = true;
        }

        if (sectors.getSystemAttributes().indexOf(sPoints) < 0) {
            sectors.getSystemAttributes().add(sPoints);
            sectors.getSystemAttributes().add(sProperties);
            updateSectors = true;
        }

        if (updateSectors)
            engine.updateQualifier(sectors);

        projectPreferencesAttrtibute = createAttribute(F_PROJECT_PREFERENCES,
                new AttributeType(IDEF0, "ProjectPreferences", false));

        baseFunctions = createQualifier(F_BASE_FUNCTIONS);
        if (justCreated) {
            baseFunctions.getSystemAttributes().add(baseFunctionQualifierId);
            baseFunctions.getSystemAttributes().add(
                    (Attribute) engine.getPluginProperty("Core",
                            HierarchicalPlugin.HIERARHICAL_ATTRIBUTE));
            baseFunctions.getSystemAttributes().add(
                    projectPreferencesAttrtibute);
            engine.updateQualifier(baseFunctions);
            installFunctionAttributes(baseFunctions, engine);
        } else {
            if (baseFunctions.getSystemAttributes().indexOf(
                    projectPreferencesAttrtibute) < 0) {
                baseFunctions.getSystemAttributes().add(
                        projectPreferencesAttrtibute);
                engine.updateQualifier(baseFunctions);
            }
            checkIDEF0Attributes(engine, baseFunctions);
        }

        Qualifier modelTree = createQualifier(F_MODEL_TREE);
        final Attribute name = StandardAttributesPlugin
                .getAttributeNameAttribute(engine);
        if (justCreated) {
            modelTree.getSystemAttributes().add(
                    (Attribute) engine.getPluginProperty("Core",
                            HierarchicalPlugin.HIERARHICAL_ATTRIBUTE));

            modelTree.getSystemAttributes().add(
                    StandardAttributesPlugin.getAttributeQualifierId(engine));
            modelTree.getAttributes().add(name);
            modelTree.setAttributeForName(name.getId());
            engine.updateQualifier(modelTree);
            checkModelTree(modelTree);
        }
        if (!StandardAttributesPlugin.isDisableAutoupdate(engine)) {
            engine.addElementAttributeListener(modelTree,
                    new ElementAttributeListener() {

                        @Override
                        public void attributeChanged(AttributeEvent event) {
                            if (event.isJournaled())
                                return;
                            if (name.equals(event.getAttribute())) {
                                Long id = (Long) engine.getAttribute(event
                                        .getElement(), StandardAttributesPlugin
                                        .getAttributeQualifierId(engine));
                                if (id != null) {
                                    Qualifier model = engine.getQualifier(id);
                                    if (model != null) {
                                        model.setName(String.valueOf(event
                                                .getNewValue()));
                                        engine.updateQualifier(model);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private void checkModelTree(Qualifier modelTree) {
        List<Qualifier> models = new ArrayList<Qualifier>();
        List<Qualifier> qList = engine.getQualifiers();
        for (Qualifier qualifier : qList) {
            if (IDEF0Plugin.isFunction(qualifier)) {
                models.add(qualifier);
            }
        }
        Collections.sort(models, new Comparator<Qualifier>() {

            @Override
            public int compare(Qualifier o1, Qualifier o2) {
                return StringCollator.compare(o1.getName(), o2.getName());
            }
        });

        Attribute attribute = StandardAttributesPlugin
                .getAttributeQualifierId(engine);
        RowSet rowSet = new RowSet(engine, modelTree,
                new Attribute[]{attribute}, null, true);

        for (Row row : rowSet.getAllRows()) {
            for (int i = models.size() - 1; i >= 0; --i) {
                Qualifier model = models.get(i);
                Long id = (Long) row.getAttribute(attribute);
                if (id != null && model.getId() == id.longValue()) {
                    models.remove(i);
                    break;
                }
            }
        }

        for (Qualifier model : models) {
            Row row = rowSet.createRow(null);
            row.setName(model.getName());
            row.setAttribute(attribute, model.getId());
        }
    }

    public static Element findElementForBaseFunction(long qualifierId,
                                                     Engine engine) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        List<Element> list = engine.getElements(plugin.baseFunctions.getId());
        for (Element element : list) {
            if (engine.getAttribute(element, plugin.baseFunctionQualifierId)
                    .equals(qualifierId)) {
                return element;
            }
        }
        return null;
    }

    private Qualifier createQualifier(String name) {
        justCreated = false;
        Qualifier q = getQualifier(name);
        if (q != null)
            return q;
        justCreated = true;
        q = engine.createSystemQualifier();
        q.setName(name);
        engine.updateQualifier(q);
        engine.setPluginProperty(getName(), name, q);
        return q;
    }

    private Qualifier getQualifier(String name) {
        Qualifier qualifier = engine.getSystemQualifier(name);
        if (qualifier == null)
            return null;

        engine.setPluginProperty(getName(), name, qualifier);
        return qualifier;
    }

    private Attribute createAttribute(String attributeName,
                                      AttributeType attributeType) {
        Attribute attribute = getSysteAttribute(attributeName);
        if (attribute != null) {
            engine.setPluginProperty(getName(), attributeName, attribute);
            return attribute;
        }
        attribute = engine.createSystemAttribute(attributeType);
        attribute.setName(attributeName);
        engine.updateAttribute(attribute);
        engine.setPluginProperty(getName(), attributeName, attribute);
        justCreated = true;
        return attribute;
    }

    private Attribute getSysteAttribute(String attributeName) {
        return engine.getSystemAttribute(attributeName);
    }

    @Override
    public String[] getRequiredPlugins() {
        return new String[]{"Core"};
    }

    public static void installFunctionAttributes(Qualifier qualifier,
                                                 Engine engine) {
        if (isFunction(qualifier))
            throw new RuntimeException("Qualifier is allready function.");
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        for (Attribute a : plugin.functionAttributes) {
            qualifier.getSystemAttributes().add(a);
        }
        engine.updateQualifier(qualifier);
        Element e = engine.createElement(plugin.baseFunctions.getId());
        engine.setAttribute(e, plugin.baseFunctionQualifierId,
                qualifier.getId());
        engine.setAttribute(e, (Attribute) engine.getPluginProperty("Core",
                HierarchicalPlugin.HIERARHICAL_ATTRIBUTE),
                new HierarchicalPersistent());
    }

    public static boolean isFunction(Qualifier qualifier) {
        for (Attribute attr : qualifier.getSystemAttributes()) {
            if (attr.getAttributeType().getTypeName().equals("VisualData"))
                return true;
        }
        return false;
    }

    public static Attribute getSectorFunctionAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_SECTOR_FUNCTION);
    }

    public static Attribute getSectorStreamAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_SECTOR_STREAM);
    }

    public static Qualifier getBaseStreamQualifier(Engine engine) {
        return (Qualifier) engine.getPluginProperty(IDEF0, F_STREAMS);
    }

    public static Qualifier getBaseSectorQualifier(Engine engine) {
        return (Qualifier) engine.getPluginProperty(IDEF0, F_SECTORS);
    }

    public static long getNextCrosspointId(Engine engine) {
        return engine.nextValue(CROSSPOINTS_SEQUENCE);
    }

    public static Attribute getFunctionVisualDataAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_VISUAL_DATA);
    }

    public static Attribute getPageSizeAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_PAGE_SIZE);
    }

    public static Qualifier getBaseFunctions(Engine engine) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        return plugin.baseFunctions;
    }

    public static List<Qualifier> getBaseQualifiers(Engine engine) {
        List<Qualifier> result = new ArrayList<Qualifier>();
        for (Qualifier q : engine.getQualifiers())
            if (isFunction(q))
                result.add(q);
        return result;
    }

    public static Attribute getProjectPreferencesAttrtibute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0,
                F_PROJECT_PREFERENCES);
    }

    public static Qualifier getBaseQualifier(Engine engine, Element element) {
        return engine.getQualifier(getBaseQualifierId(engine, element));
    }

    public static Long getBaseQualifierId(Engine engine, Element element) {
        return (Long) engine.getAttribute(element,
                getAttribute(engine, F_BASE_FUNCTION_QUALIFIER_ID));
    }

    public static Attribute getAttribute(Engine engine, String attrName) {
        return (Attribute) engine.getPluginProperty(IDEF0, attrName);
    }

    public static boolean isStreams(Engine engine, Qualifier qualifier) {
        return getBaseStreamQualifier(engine).equals(qualifier);
    }

    public static Attribute getStreamAddedAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_STREAM_ADDED);
    }

    @Override
    public String[] getSequences() {
        return new String[]{CROSSPOINTS_SEQUENCE, ORDINATES_SEQUENCE};
    }

    public static void checkIDEF0Attributes(Engine engine, Qualifier qualifier) {
        if (qualifier == null)
            return;
        boolean update = false;
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        List<Attribute> systemAttributes = qualifier.getSystemAttributes();
        for (Attribute a : plugin.functionAttributes) {
            if (systemAttributes.indexOf(a) < 0) {
                systemAttributes.add(a);
                update = true;
            }
        }
        if (update)
            engine.updateQualifier(qualifier);
    }

    public static Attribute getBackgroundColorAttribute(Engine engine) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        for (Attribute attr : plugin.functionAttributes) {
            if (attr.getName().equals(F_BACKGROUND))
                return attr;
        }
        return null;
    }

    public static Attribute getForegroundColorAttribute(Engine engine) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        for (Attribute attr : plugin.functionAttributes) {
            if (attr.getName().equals(F_FOREGROUND))
                return attr;
        }
        return null;
    }

    public static Attribute getFontAttribute(Engine engine) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        for (Attribute attr : plugin.functionAttributes) {
            if (attr.getName().equals(F_FONT))
                return attr;
        }
        return null;
    }

    public static Attribute getFunctionTypeAttribute(Engine engine) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        for (Attribute attr : plugin.functionAttributes) {
            if (attr.getName().equals(F_TYPE))
                return attr;
        }
        return null;
    }

    public static Attribute getFunctionOunerAttribute(Engine engine) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        for (Attribute attr : plugin.functionAttributes) {
            if (attr.getName().equals(F_OUNER_ID))
                return attr;
        }
        return null;
    }

    public static Attribute getStreamAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_SECTOR_STREAM);
    }

    public static Attribute getSectorBorderStartAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0,
                F_SECTOR_BORDER_START);
    }

    public static Attribute getSectorBorderEndAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_SECTOR_BORDER_END);
    }

    public static Attribute getBaseFunctionQualifierId(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_BASE_FUNCTION_QUALIFIER_ID);
    }

    public static Attribute getDecompositionTypeAttribute(Engine engine) {
        return (Attribute) engine
                .getPluginProperty(IDEF0, F_DECOMPOSITION_TYPE);
    }

    public static Attribute getLinkAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty(IDEF0, F_LINK);
    }

    public static long getNextOrdinateId(Engine engine) {
        return engine.nextValue(ORDINATES_SEQUENCE);
    }

    public static Qualifier getModelTree(Engine engine) {
        return (Qualifier) engine.getPluginProperty(IDEF0, F_MODEL_TREE);
    }

    /**
     * Додае до класифікатора
     * "візуальні атрибути, але не оновлюе класифікатор в СУБД"
     */
    public static void addVisualAttributes(Engine engine, Qualifier qualifier) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        qualifier.getSystemAttributes().addAll(plugin.visualAttributes);
    }

    public static void removeVisualAttributes(Engine engine, Qualifier qualifier) {
        IDEF0Plugin plugin = (IDEF0Plugin) engine.getPluginProperty(IDEF0,
                PLUGIN);
        qualifier.getSystemAttributes().removeAll(plugin.visualAttributes);
    }

    public static boolean hasVisualAttributes(Engine engine, Element element) {
        return hasVisualAttributes(engine,
                engine.getQualifier(element.getQualifierId()));
    }

    public static boolean hasVisualAttributes(Engine engine, Qualifier qualifier) {
        for (Attribute attr : qualifier.getSystemAttributes())
            if (attr.getAttributeType().getTypeName().equals("FRectangle"))
                return true;
        return false;
    }

    /**
     * Can return null if there is no visual options
     */
    public static RectangleVisualOptions getDefaultRectangleVisualOptions(
            Engine engine, Element element) {
        if (hasVisualAttributes(engine, element)) {
            RectangleVisualOptions ops = new RectangleVisualOptions();
            ops.bounds = (FRectangle) engine.getAttribute(element,
                    getAttribute(engine, F_BOUNDS));
            if (ops.bounds == null)
                return null;
            ops.background = (Color) engine.getAttribute(element,
                    getAttribute(engine, F_BACKGROUND));
            ops.foreground = (Color) engine.getAttribute(element,
                    getAttribute(engine, F_FOREGROUND));
            ops.font = (Font) engine.getAttribute(element,
                    getAttribute(engine, F_FONT));
            return ops;
        }
        return null;
    }

    public static void setDefaultRectangleVisualOptions(Engine engine,
                                                        Element element, RectangleVisualOptions ops) {
        Qualifier qualifier = engine.getQualifier(element.getQualifierId());
        if (!hasVisualAttributes(engine, qualifier)) {
            addVisualAttributes(engine, qualifier);
            engine.updateQualifier(qualifier);
        }
        engine.setAttribute(element, getAttribute(engine, F_BOUNDS), ops.bounds);
        engine.setAttribute(element, getAttribute(engine, F_BACKGROUND),
                ops.background);
        engine.setAttribute(element, getAttribute(engine, F_FOREGROUND),
                ops.foreground);
        engine.setAttribute(element, getAttribute(engine, F_FONT), ops.font);
    }
}
