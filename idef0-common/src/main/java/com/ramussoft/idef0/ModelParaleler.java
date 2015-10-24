package com.ramussoft.idef0;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.utils.DataLoader;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.attribute.EngineParalleler;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

public class ModelParaleler implements EngineParalleler {

    private DataPlugin fromDataPlugin;

    private DataPlugin toDataPlugin;

    private Hashtable<Long, Attribute> attrHash = new Hashtable<Long, Attribute>();

    private Hashtable<Long, Qualifier> qHash = new Hashtable<Long, Qualifier>();

    private Hashtable<Long, Row> rowHash = new Hashtable<Long, Row>();

    private Hashtable<Long, Row> rowForQualifiers = new Hashtable<Long, Row>();

    private Hashtable<Long, NSector> sectorsHash = new Hashtable<Long, NSector>();

    private Hashtable<Long, Long> crosspoinIds = new Hashtable<Long, Long>();

    private GUIFramework framework;

    private Engine fromEngine;

    private Engine toEngine;

    private Function function;

    private Function base;

    public ModelParaleler(final DataPlugin fromDataPlugin,
                          final DataPlugin toDataPlugin, GUIFramework framework) {
        super();
        this.fromDataPlugin = fromDataPlugin;
        this.toDataPlugin = toDataPlugin;
        this.fromEngine = fromDataPlugin.getEngine();
        this.toEngine = toDataPlugin.getEngine();
        this.framework = framework;
        init();
    }

    public void createParalel(final Function function,
                              final boolean createAllRows) {
        ModelParaleler.this.function = function;
        if (createAllRows)
            createAllRows();
        Qualifier qualifier = toEngine.createQualifier();
        copyQualifier(function.getQualifier(), qualifier);
        IDEF0Plugin.installFunctionAttributes(qualifier, toEngine);
        Qualifier q = IDEF0Plugin.getModelTree(toEngine);
        Element element = toEngine.createElement(q.getId());
        toEngine.setAttribute(element,
                StandardAttributesPlugin.getAttributeQualifierId(toEngine),
                qualifier.getId());

        toEngine.setAttribute(element,
                StandardAttributesPlugin.getAttributeNameAttribute(toEngine),
                function.getName());

        toDataPlugin = NDataPluginFactory.getDataPlugin(qualifier, toEngine,
                toDataPlugin.getAccessRules());
        ModelParaleler.this.base = toDataPlugin.getBaseFunction();
        Vector<Row> childs = fromDataPlugin.getRecChilds(function, true);

        ProjectOptions projectOptions = toDataPlugin.getBaseFunction()
                .getProjectOptions();

        projectOptions.getDeligate().setDiagramSize(
                function.getProjectOptions().getDeligate().getDiagramSize());

        toDataPlugin.getBaseFunction().setProjectOptions(projectOptions);

        for (Row row : childs) {
            NFunction dest = (NFunction) getRow(row);
            showMessageAnimation(dest.toString());
        }

        NFunction func = null;

        Vector<Row> v = toDataPlugin.getChilds(base, true);
        if (v.size() > 0)
            func = (NFunction) v.get(0);

        createSectorsOnUpperLevel(func);
    }

    private void createSectorsOnUpperLevel(NFunction func) {
        if (func != null) {
            MovingArea area = new MovingArea(toDataPlugin, func);
            area.setDataPlugin(toDataPlugin);
            SectorRefactor sr = area.getRefactor();
            sr.loadFromFunction(func, false);
            for (int i = 0; i < sr.getSectorsCount(); i++) {
                PaintSector ps = sr.getSector(i);
                if (ps.getSector().getStart().getBorderType() >= 0)
                    sr.createSectorOnIn(ps, true);
                if (ps.getSector().getEnd().getBorderType() >= 0)
                    sr.createSectorOnIn(ps, false);
            }
            sr.loadFromFunction((NFunction) func.getParentRow(), true);
            sr.saveToFunction();
        }
    }

    @SuppressWarnings("deprecation")
    private void init() {
        Vector<Row> v = fromDataPlugin.getRecChilds(null, false);
        for (Row r : v) {
            long qualifierId = StandardAttributesPlugin.getQualifierId(
                    fromEngine, r.getElement());
            rowForQualifiers.put(qualifierId, r);
        }

        Qualifier bs = IDEF0Plugin.getBaseStreamQualifier(fromEngine);
        rowForQualifiers.put(bs.getId(), fromDataPlugin.getBaseStream());

        List<Attribute> attrs = fromEngine.getSystemAttributes();
        List<Attribute> dAttrs = toEngine.getSystemAttributes();
        Hashtable<String, Attribute> hash = new Hashtable<String, Attribute>();
        for (Attribute attribute : dAttrs)
            hash.put(attribute.getName(), attribute);

        for (Attribute a : attrs) {
            Attribute d = hash.get(a.getName());
            if (d == null)
                System.err
                        .println("WARNING: System attribute not found in destination engine: "
                                + a.getName()
                                + " type: "
                                + a.getAttributeType());
            else
                attrHash.put(a.getId(), d);

        }
    }

    private void createAllRows() {
        Vector<com.ramussoft.pb.Row> qualifiers = fromDataPlugin.getRecChilds(
                null, false);
        for (com.ramussoft.pb.Row r : qualifiers) {
            Qualifier qualifier = StandardAttributesPlugin.getQualifier(
                    fromEngine, r.getElement());
            if (!IDEF0Plugin.isFunction(qualifier)) {
                Vector<Row> v = fromDataPlugin.getRecChilds(r, true);
                for (Row row2 : v)
                    getRow(row2);
            }
        }
    }

    private Row getRow(Row r) {
        if (r == fromDataPlugin.getBaseStream())
            return toDataPlugin.getBaseStream();
        if ((r == null) || (r.getElement() == null))
            return null;
        if ((function != null) && (r.equals(function.getParentRow())))
            return base;

        Row res = rowHash.get(r.getElement().getId());
        if (res == null) {
            res = toDataPlugin
                    .createRow(getRow(getParentRow(r)), r.isElement());
            rowHash.put(r.getElement().getId(), res);
            if (r instanceof Function)
                if (!(res instanceof Function))
                    System.out.println(res);
            copyRow(r, res);
        }

        return res;
    }

    private Row getRow(long rowId) {
        if (rowId < 0l)
            return null;
        Row res = rowHash.get(rowId);
        if (res == null) {
            Row row = fromDataPlugin.findRowByGlobalId(rowId);
            if (row == null)
                return null;
            Element element = row.getElement();
            if (!((row instanceof Stream) && (element.getName().equals("")))) {
                Qualifier qualifier = null;
                if (row instanceof Stream)
                    qualifier = IDEF0Plugin.getBaseStreamQualifier(toEngine);
                else
                    qualifier = getQualifier(element.getQualifierId());
                if (qualifier == null)
                    return null;
                Element e = toEngine.getElement(element.getName(),
                        qualifier.getId());
                if (e != null) {
                    Row row2 = toDataPlugin.findRowByGlobalId(e.getId());
                    rowHash.put(e.getId(), row2);
                    return row2;
                }
            }

            Row r = fromDataPlugin.findRowByGlobalId(rowId);
            res = toDataPlugin
                    .createRow(getRow(getParentRow(r)), r.isElement());
            rowHash.put(r.getElement().getId(), res);
            copyRow(r, res);
        }
        return res;
    }

    private Row getParentRow(Row r) {
        if (r.isElement()) {
            Row parent = r.getParentRow();
            if ((parent == null) || (parent.getElement() == null))
                return rowForQualifiers.get(r.getElement().getQualifierId());
        }
        return r.getParentRow();
    }

    private void copyRow(Row source, Row destination) {
        if (source.isElement()) {
            Qualifier qualifier = fromEngine.getQualifier(source.getElement()
                    .getQualifierId());
            Qualifier to = toEngine.getQualifier(destination.getElement()
                    .getQualifierId());
            checkAttributes(qualifier, to);
            copyAttributes(qualifier.getAttributes(), source.getElement(),
                    destination.getElement());
            copyAttributes(qualifier.getSystemAttributes(),
                    source.getElement(), destination.getElement());

            if (source instanceof NFunction)
                copyFunction((NFunction) source, (NFunction) destination);
        } else {
            Qualifier s = StandardAttributesPlugin.getQualifier(fromEngine,
                    source.getElement());
            Qualifier d = StandardAttributesPlugin.getQualifier(toEngine,
                    destination.getElement());
            copyQualifier(s, d);
        }
    }

    private void checkAttributes(Qualifier from, Qualifier to) {
        boolean update = false;
        List<Attribute> list = getAttributes(from.getAttributes());
        for (Attribute a : list) {
            if (to.getAttributes().indexOf(a) < 0) {
                to.getAttributes().add(a);
                update = true;
            }
        }
        if (update)
            toEngine.updateQualifier(to);
    }

    private void copyFunction(NFunction source, NFunction destination) {
        long l = source.getLink();
        if (l >= 0) {
            Row row = fromDataPlugin.findRowByGlobalId(l);
            if (row != null) {
                l = getRow(row).getElement().getId();
                destination.setLink(l);
            }
        }
        // destination.setName(source.getName());
        if (source.getChildCount() > 0) {
            SectorRefactor sr = new SectorRefactor(new MovingArea(
                    fromDataPlugin));
            sr.loadFromFunction(source, false);
            for (int i = 0; i < sr.getSectorsCount(); i++) {
                PaintSector ps = sr.getSector(i);
                PaintSector.save(ps, new DataLoader.MemoryData(), fromEngine);
                ps.setSector(getSector((NSector) ps.getSector()));
            }
            // sr.saveToFunction(destination);
        }
    }

    private Sector getSector(NSector sector) {
        NSector res = sectorsHash.get(sector.getElementId());
        if (res == null) {
            res = (NSector) toDataPlugin.createSector();
            sectorsHash.put(sector.getElementId(), res);
            copyAttributes(IDEF0Plugin.getBaseSectorQualifier(fromEngine)
                            .getSystemAttributes(), sector.getElement(),
                    res.getElement());
            copy(sector.getStart(), res.getStart());
            copy(sector.getEnd(), res.getEnd());
        }
        return res;
    }

    private void copy(NSectorBorder source, NSectorBorder destination) {
        destination.setBorderTypeA(source.getBorderType());
        destination.setFunctionTypeA(source.getFunctionType());
        destination.getSbp().setCrosspoint(
                getCrosspointId(source.getSbp().getCrosspoint()));
        destination.setFunctionA((Function) getRow(source.getFunction()));
        destination.commit();
    }

    private long getCrosspointId(long sourceId) {
        Long key = sourceId;
        Long res = crosspoinIds.get(key);
        if (res == null) {
            res = IDEF0Plugin.getNextCrosspointId(toEngine);
            crosspoinIds.put(key, res);
        }
        return res;
    }

    private void copyAttributes(List<Attribute> attributes, Element source,
                                Element destination) {
        for (Attribute attribute : attributes)
            if ((!attribute.getAttributeType().getTypeName()
                    .equals("Hierarchical"))
                    && (!attribute.getAttributeType().getTypeName()
                    .equals("VisualData"))
                    && (!attribute.getAttributeType().getTypeName()
                    .equals("SectorBorder"))) {
                AttributePlugin plugin = attribute.getAttributeType()
                        .getAttributePlugin(fromEngine);
                if (plugin == null)
                    System.err
                            .println("WARNING: Attribute plugin not found for type: "
                                    + attribute.getAttributeType());
                else
                    plugin.copyAttribute(fromEngine, toEngine, attribute,
                            getAttribute(attribute), source, destination, this);
            }
    }

    private void copyQualifier(Qualifier source, Qualifier destination) {
        qHash.put(source.getId(), destination);
        destination.setName(source.getName());
        destination.setAttributes(getAttributes(source.getAttributes()));
        destination.setAttributeForName(getId(getAttribute(source
                .getAttributeForName())));
        toEngine.updateQualifier(destination);
        showMessageAnimation(source.toString());
    }

    public void showMessageAnimation(String text) {
        if (framework != null)
            framework.showAnimation(ResourceLoader.getString("Wait.Message")
                    + " " + text);
        else
            System.out.println(text);
    }

    private long getId(Attribute attribute) {
        if (attribute == null)
            return -1l;
        return attribute.getId();
    }

    private List<Attribute> getAttributes(List<Attribute> attributes) {
        List<Attribute> res = new ArrayList<Attribute>();
        for (Attribute a : attributes)
            res.add(getAttribute(a));
        return res;
    }

    private Attribute getAttribute(Attribute source) {
        if (source == null)
            return null;
        Attribute res = attrHash.get(source.getId());
        if (res == null) {
            res = toEngine.createAttribute(source.getAttributeType());
            attrHash.put(source.getId(), res);
            res.setName(source.getName());
            AttributePlugin plugin = source.getAttributeType()
                    .getAttributePlugin(fromEngine);
            plugin.copyAttribute(fromEngine, toEngine, source, res, null, null,
                    this);
            toEngine.updateAttribute(res);
        }
        return res;
    }

    @Override
    public Attribute getAttribute(long attributeId) {
        return getAttribute(fromEngine.getAttribute(attributeId));
    }

    @Override
    public Qualifier getQualifier(long qualifierId) {
        Qualifier q = qHash.get(qualifierId);
        if (q != null)
            return q;
        Row row = rowForQualifiers.get(qualifierId);
        if (row == null)
            return null;
        if (q == null) {
            try {
                q = StandardAttributesPlugin.getQualifier(toEngine, getRow(row)
                        .getElement());
                qHash.put(qualifierId, q);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return q;
    }

    public boolean clear(final Function base) {
        clearX(base);
        return true;
    }

    private void clearX(final Function function) {
        final Vector<Row> v = fromDataPlugin.getChilds(function, true);
        for (int j = 0; j < v.size(); j++)
            clearX((Function) v.get(j));

        final Vector<Sector> ss = function.getSectors();
        for (int i = 0; i < ss.size(); i++)
            ss.get(i).remove();

        function.setSectorData(new byte[0]);
        if (!this.function.equals(function))
            fromDataPlugin.removeRow(function);
    }

    @Override
    public Element getElement(long sourceElementId) {
        Row res = getRow(sourceElementId);
        if (res == null)
            return null;
        return res.getElement();
    }

    public void loadFromParalel(Function base, boolean importAll,
                                List<Qualifier> selected) {
        this.base = base;

        List<Attribute> tAttrs = toEngine.getAttributes();
        List<Attribute> fAttrs = fromEngine.getAttributes();
        for (Attribute f : fAttrs)
            for (Attribute t : tAttrs)
                if ((f.getName().equals(t.getName()))
                        && (f.getAttributeType().equals(t.getAttributeType())))
                    attrHash.put(f.getId(), t);

        List<Qualifier> fList = fromEngine.getQualifiers();
        List<Qualifier> tList = toEngine.getQualifiers();
        for (Qualifier f : fList)
            if (!IDEF0Plugin.isFunction(f))
                for (Qualifier t : tList)
                    if (!IDEF0Plugin.isFunction(t))
                        if (f.getName().equals(t.getName()))
                            qHash.put(f.getId(), t);

        Vector<Row> fRows = fromDataPlugin.getRecChilds(null, false);
        Vector<Row> tRows = toDataPlugin.getRecChilds(null, false);

        for (Row f : fRows) {
            for (Row t : tRows) {
                if (f.getName().equals(t.getName())) {
                    rowHash.put(f.getElement().getId(), t);
                    Vector<Row> fCRows = fromDataPlugin.getRecChilds(f, true);
                    Vector<Row> tCRows = toDataPlugin.getRecChilds(t, true);
                    for (Row fc : fCRows)
                        for (Row tc : tCRows)
                            if (fc.getName().equals(tc.getName()))
                                rowHash.put(fc.getElement().getId(), tc);
                }
            }
        }

        if (importAll)
            createAllRows();

        for (Qualifier qualifier : selected) {
            fromDataPlugin = NDataPluginFactory.getDataPlugin(qualifier,
                    fromEngine, fromDataPlugin.getAccessRules());

            List<Attribute> list = getAttributes(qualifier.getAttributes());
            Qualifier baseFunction = toEngine.getQualifier(base.getElement()
                    .getQualifierId());
            for (Attribute a : list)
                if (baseFunction.getAttributes().indexOf(a) < 0)
                    baseFunction.getAttributes().add(a);

            toEngine.updateQualifier(baseFunction);

            int r;

            if ((r = fromDataPlugin.getBaseFunction().getChildCount()) > 0)
                for (int i = 0; i < r; i++) {
                    function = (Function) fromDataPlugin.getBaseFunction()
                            .getChildAt(i);
                    Vector<Row> childs = fromDataPlugin.getRecChilds(function,
                            true);
                    childs.add(0, function);
                    for (Row row : childs) {
                        NFunction dest = (NFunction) getRow(row);
                        showMessageAnimation(dest.toString());
                    }
                    NFunction func = (NFunction) getRow(function);
                    createSectorsOnUpperLevel(func);
                }
        }
    }

    public void loadFromParalel(boolean importAll, List<Qualifier> selected) {
        List<Attribute> tAttrs = toEngine.getAttributes();
        List<Attribute> fAttrs = fromEngine.getAttributes();
        for (Attribute f : fAttrs)
            for (Attribute t : tAttrs)
                if ((f.getName().equals(t.getName()))
                        && (f.getAttributeType().equals(t.getAttributeType())))
                    attrHash.put(f.getId(), t);

        List<Qualifier> fList = fromEngine.getQualifiers();
        List<Qualifier> tList = toEngine.getQualifiers();
        for (Qualifier f : fList)
            if (!IDEF0Plugin.isFunction(f))
                for (Qualifier t : tList)
                    if (!IDEF0Plugin.isFunction(t))
                        if (f.getName().equals(t.getName()))
                            qHash.put(f.getId(), t);

        Vector<Row> fRows = fromDataPlugin.getRecChilds(null, false);
        Vector<Row> tRows = toDataPlugin.getRecChilds(null, false);

        for (Row f : fRows)
            for (Row t : tRows)
                if (f.getName().equals(t.getName())) {
                    rowHash.put(f.getElement().getId(), t);
                    Vector<Row> fCRows = fromDataPlugin.getRecChilds(f, true);
                    Vector<Row> tCRows = toDataPlugin.getRecChilds(t, true);
                    for (Row fc : fCRows)
                        for (Row tc : tCRows)
                            if (fc.getName().equals(tc.getName()))
                                rowHash.put(fc.getElement().getId(), tc);
                }

        if (importAll) {
            createAllRows();
        }
        for (Qualifier qualifier : selected) {
            fromDataPlugin = NDataPluginFactory.getDataPlugin(qualifier,
                    fromEngine, fromDataPlugin.getAccessRules());

            Qualifier baseFunction = toEngine.createQualifier();

            baseFunction.setName(qualifier.getName());

            IDEF0Plugin.installFunctionAttributes(baseFunction, toEngine);

            toDataPlugin = NDataPluginFactory.getDataPlugin(baseFunction,
                    toEngine, toDataPlugin.getAccessRules());

            base = toDataPlugin.getBaseFunction();

            List<Attribute> list = getAttributes(qualifier.getAttributes());
            for (Attribute a : list)
                if (baseFunction.getAttributes().indexOf(a) < 0)
                    baseFunction.getAttributes().add(a);

            Attribute forName = fromEngine.getAttribute(qualifier
                    .getAttributeForName());
            if (forName != null)
                baseFunction.setAttributeForName(getAttribute(forName).getId());

            toEngine.updateQualifier(baseFunction);

            if (fromDataPlugin.getBaseFunction().getChildCount() > 0) {
                function = (Function) fromDataPlugin.getBaseFunction()
                        .getChildAt(0);
                Vector<Row> childs = fromDataPlugin
                        .getRecChilds(function, true);
                childs.add(0, function);
                for (Row row : childs) {
                    NFunction dest = (NFunction) getRow(row);
                    showMessageAnimation(dest.toString());
                }

                NFunction func = (NFunction) getRow(function);
                createSectorsOnUpperLevel(func);
            }
        }
    }
}
