package com.ramussoft.pb.data.negine;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import com.dsoft.utils.Options;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.LocalAccessor;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.DFDSName;
import com.ramussoft.idef0.attribute.DFDSNamePlugin;
import com.ramussoft.idef0.attribute.SectorBorderPersistent;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.AbstractDataPlugin;
import com.ramussoft.pb.data.RowNameComparator;
import com.ramussoft.pb.types.GlobalId;

public class NDataPlugin extends AbstractDataPlugin {

    public static final int FILE_VERSION = 26;

    public final Attribute sectorBorderStart;

    public final Attribute sectorBorderEnd;

    private Hashtable<GlobalId, Object> properties = null;

    private final Attribute[] functionAttributes;

    private Attribute qualifierId;

    private Qualifier streams;

    private Qualifier sectors;

    private final NStream baseStream;

    /**
     * Файл, в якому міститься вся інформація.
     */

    private int version = FILE_VERSION;

    Hashtable<GlobalId, String> newVersionRowsNames = null;

    public final Attribute addedRows;

    public final Attribute sectorAttribute;

    public final Attribute sectorFunction;

    public final Attribute sectorStream;

    public final Attribute sectorPointsAttribute;

    public final Attribute sectorPropertiesAttribute;

    private final Hashtable<Long, NSector> sectorsRowSet = new Hashtable<Long, NSector>();

    public int getVersion() {
        return version;
    }

    private boolean loading = false;

    public void setLoading(final boolean loading) {
        this.loading = loading;
    }

    /**
     * @return Returns the loading.
     */
    public boolean isLoading() {
        return loading;
    }

    protected Vector matrixes = new Vector();

    private final Vector<String> namedData = new Vector<String>();

    private Hashtable<Long, RowSet> rowSets = new Hashtable<Long, RowSet>();

    private final RowSet qualifiers;

    private final Engine engine;

    private AccessRules accessRules;

    private final RowSet streamsRowSet;

    private final Attribute[] streamAttributes;

    private ElementListener listener = new ElementAdapter() {
        public void elementCreated(com.ramussoft.common.event.ElementEvent event) {
            Element element = event.getNewElement();
            sectorsRowSet.put(element.getId(), new NSector(NDataPlugin.this,
                    element));

        }

        ;

        @Override
        public void elementDeleted(ElementEvent event) {
            sectorsRowSet.remove(event.getOldElement().getId());
        }
    };

    private ElementAttributeListener crosspointsListener = new ElementAttributeListener() {

        @Override
        public void attributeChanged(AttributeEvent event) {
            if (event.getAttribute().equals(sectorBorderStart)) {
                NSector sector = sectorsRowSet.get(event.getElement().getId());
                if (sector == null)
                    return;
                SectorBorderPersistent sbp = (SectorBorderPersistent) event
                        .getNewValue();
                if (sbp == null)
                    return;
                NCrosspoint crosspoint = crosspoints.get(sbp.getCrosspoint());
                if (crosspoint == null) {
                    crosspoint = new NCrosspoint(NDataPlugin.this,
                            sbp.getCrosspoint());
                    crosspoints.put(sbp.getCrosspoint(), crosspoint);
                }
                crosspoint.addXOut(sector);
                sector.getStart().crosspoint = crosspoint;
                sector.setStartP(sbp);
            } else if (event.getAttribute().equals(sectorBorderEnd)) {
                NSector sector = sectorsRowSet.get(event.getElement().getId());
                if (sector == null)
                    return;
                SectorBorderPersistent sbp = (SectorBorderPersistent) event
                        .getNewValue();
                if (sbp == null)
                    return;
                NCrosspoint crosspoint = crosspoints.get(sbp.getCrosspoint());
                if (crosspoint == null) {
                    crosspoint = new NCrosspoint(NDataPlugin.this,
                            sbp.getCrosspoint());
                    crosspoints.put(sbp.getCrosspoint(), crosspoint);
                }
                crosspoint.addXIn(sector);
                sector.getEnd().crosspoint = crosspoint;
                sector.setEndP(sbp);
            }
        }
    };

    public Row createRow(final Row parent, boolean element,
                         final GlobalId globalId) {
        assert parent == null && !element || parent != null;

        if (parent instanceof Stream) {
            return (Row) streamsRowSet.createRow(baseStream);
        }

        if (parent instanceof Function)
            throw new RuntimeException(
                    "Method can not be called to create functions!");
        Row res;
        if (!element) {
            res = (Row) qualifiers
                    .createRow((com.ramussoft.database.common.Row) parent);
        } else {
            com.ramussoft.database.common.Row row = qualifiers
                    .findRow(((com.ramussoft.database.common.Row) parent)
                            .getElementId());

            if (row != null) {
                long qId = (Long) engine.getAttribute(row.getElement(),
                        qualifierId);
                RowSet rowSet = getRowSet(qId);
                res = (Row) rowSet
                        .createRow((com.ramussoft.database.common.Row) parent);
            } else {
                RowSet rowSet = findRowSetByRowId(((com.ramussoft.database.common.Row) parent)
                        .getElementId());
                res = (Row) rowSet
                        .createRow((com.ramussoft.database.common.Row) parent);
            }
        }
        return res;
    }

    public Stream getBaseStream() {
        return baseStream;
    }

    public Function getBaseFunction() {
        return null;// throw new
        // RuntimeException("Method getBaseFunction can not be called");
    }

    public void clearAll() {
        synchronized (this) {

            clearAllNamedDataInformation();

            streamsRowSet.close();
            qualifiers.close();

            matrixes.clear();

            namedData.clear();

            crosspoints.clear();

            newVersionRowsNames = null;
        }
    }

    public NDataPlugin(Engine engine, AccessRules accessRules) {
        super();

        functionAttributes = new Attribute[NFunction.PROPERTIES.length];

        for (int i = 0; i < functionAttributes.length; i++) {
            functionAttributes[i] = (Attribute) engine.getPluginProperty(
                    "IDEF0", NFunction.PROPERTIES[i]);
        }

        this.accessRules = accessRules;
        qualifierId = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.QUALIFIER_ID);
        Qualifier q = (Qualifier) engine.getPluginProperty("Core",
                StandardAttributesPlugin.QUALIFIERS_QUALIFIER);
        qualifiers = new RowSet(engine, q, q.getAttributes().toArray(
                new Attribute[q.getAttributes().size()]),
                new RowSet.RowCreater() {
                    @Override
                    public com.ramussoft.database.common.Row createRow(
                            Element element, RowSet data,
                            Attribute[] attributes, Object[] objects) {
                        NRow row = new NRow(NDataPlugin.this, element, data,
                                attributes, objects);
                        row.setElement(false);
                        return row;
                    }
                });

        this.engine = engine;

        this.addedRows = (Attribute) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_STREAM_ADDED);

        this.sectorFunction = (Attribute) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_SECTOR_FUNCTION);

        this.sectorStream = (Attribute) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_SECTOR_STREAM);

        this.sectorAttribute = (Attribute) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_SECTOR_ATTRIBUTE);

        this.sectorPointsAttribute = (Attribute) engine.getPluginProperty(
                "IDEF0", IDEF0Plugin.F_SECTOR_POINTS);

        this.sectorPropertiesAttribute = (Attribute) engine.getPluginProperty(
                "IDEF0", IDEF0Plugin.F_SECTOR_PROPERTIES);

        this.sectorBorderStart = (Attribute) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_SECTOR_BORDER_START);

        this.sectorBorderEnd = (Attribute) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_SECTOR_BORDER_END);

        sectors = (Qualifier) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_SECTORS);

        streams = (Qualifier) engine.getPluginProperty("IDEF0",
                IDEF0Plugin.F_STREAMS);

        streamAttributes = streams.getSystemAttributes().toArray(
                new Attribute[streams.getSystemAttributes().size()]);

        streamsRowSet = new RowSet(engine, streams, streamAttributes,
                new RowSet.RowCreater() {
                    @Override
                    public com.ramussoft.database.common.Row createRow(
                            Element element, RowSet data,
                            Attribute[] attributes, Object[] objects) {
                        return new NStream(NDataPlugin.this, element, data,
                                attributes, objects) {
                            @Override
                            public com.ramussoft.database.common.Row getParent() {
                                com.ramussoft.database.common.Row parent = super
                                        .getParent();
                                if (parent == null)
                                    return (com.ramussoft.database.common.Row) getBaseStream();
                                return parent;
                            }
                        };
                    }
                }) {

            @Override
            protected void attributeChanged(AttributeEvent event) {
                super.attributeChanged(event);
                if (event.isJournaled()) {
                    if (event.getAttribute().equals(addedRows)) {
                        ((NStream) findRow(event.getElement().getId())).rows = null;
                    }
                }
            }

            @Override
            protected void removedFromChildren(
                    com.ramussoft.database.common.Row parent,
                    com.ramussoft.database.common.Row row, int index) {
                for (NSector sector : sectorsRowSet.values()) {
                    Stream stream = sector.getStream();
                    if (stream != null
                            && stream.getElement() != null
                            && stream.getElement().getId() == row
                            .getElementId())
                        sector.freeStreamValue();
                }
            }
        };

        baseStream = (NStream) streamsRowSet.getRoot();

        loadSectors();

        engine.addElementListener(sectors, listener);
        engine.addElementAttributeListener(sectors, crosspointsListener);
    }

    public boolean isCanHaveChilds(final Row row) {
        return true;
    }

    public Sector createSector() {
        final Sector s = createSector(null);
        return s;
    }

    public Sector createSector(Function function) {
        Element element = getEngine().createElement(sectors.getId());
        // engine.setAttribute(element, sectorFunction, function.getElement()
        // .getId());
        return sectorsRowSet.get(element.getId());
    }

    @Override
    public OutputStream setNamedData(final String name) {
        return engine.getOutputStream(name);
    }

    @Override
    public void removeNamedData(final String name) {
        super.removeNamedData(name);
        namedData.remove(name);
    }

    public Row createStaticRow(final int id) {
        return createRow(null, false, new GlobalId(id));
    }

    public Row createRow(final Row parent, final boolean element) {
        return createRow(parent, element, null);
    }

    public String[] getNameDataNames() {
        return engine.getStreamNames();
    }

    @Override
    protected InputStream getNativeNamedData(final String name) {
        synchronized (this) {
            int i = name.lastIndexOf('/');
            String dir = "";
            if (i >= 0) {
                dir = name.substring(0, i);
            }
            String fName = name;
            if (i >= 0) {
                fName = name.substring(i + 1);
            }

            return engine.getInputStream(dir + fName);
        }
    }

    protected int getCompressionLevel() {
        return Options.getInteger("COMPRESS", 0);
    }

    private Hashtable<GlobalId, Object> getProperties() {
        if (properties == null)
            properties = new Hashtable<GlobalId, Object>();
        return properties;
    }

    void put(final GlobalId id, final Object o) {
        getProperties().put(id, o);
    }

    Object get(final GlobalId id) {
        return getProperties().get(id);
    }

    public Crosspoint createCrosspoint(long id) {
        synchronized (this) {
            NCrosspoint value = new NCrosspoint(this, id);
            crosspoints.put(id, value);
            return value;
        }
    }

    public void setNewVersionRowsNames(
            final Hashtable<GlobalId, String> newVersionRowsNames) {
        this.newVersionRowsNames = newVersionRowsNames;
    }

    @Override
    public boolean removeRow(Row row) {
        boolean res = super.removeRow(row);
        return res;
    }

    @Override
    public Row findRowByGlobalId(GlobalId rowId) {
        return findRowByGlobalId(rowId.getLocalId());
    }

    public Row findRowByGlobalId(long elementId) {
        if (elementId == -1l)
            return null;
        RowSet rowSet = findRowSetByRowId(elementId);
        if (rowSet != null)
            return (Row) rowSet.findRow(elementId);
        return null;
    }

    private RowSet findRowSetByRowId(long id) {
        if (qualifiers.findRow(id) != null)
            return qualifiers;
        long qId = engine.getQualifierIdForElement(id);
        if (streamsRowSet != null)
            if (qId == streamsRowSet.getQualifier().getId())
                return streamsRowSet;
        if (qId >= 0)
            return getRowSet(qId);
        return null;
    }

    public RowSet getRowSet(final long id) {
        RowSet res = rowSets.get(id);
        if (res == null) {
            final Qualifier qualifier = engine.getQualifier(id);
            boolean create = true;
            if (IDEF0Plugin.isFunction(qualifier)) {
                create = false;
                List<Attribute> list = new ArrayList<Attribute>(
                        qualifier.getSystemAttributes());
                list.addAll(qualifier.getAttributes());

                final Qualifier baseFunctions = IDEF0Plugin
                        .getBaseFunctions(engine);

                res = new RowSet(engine, qualifier,
                        list.toArray(new Attribute[list.size()]),
                        new RowSet.RowCreater() {
                            @Override
                            public com.ramussoft.database.common.Row createRow(
                                    Element element, RowSet data,
                                    Attribute[] attributes, Object[] objects) {
                                if (element != null)
                                    return new NFunction(NDataPlugin.this,
                                            element, data, attributes, objects) {
                                        @Override
                                        public long getQualifierId() {
                                            if (id == baseFunctions.getId()) {
                                                return IDEF0Plugin
                                                        .getBaseQualifierId(
                                                                engine,
                                                                getElement());
                                            }
                                            return id;
                                        }
                                    };
                                return new NFunction(NDataPlugin.this,
                                        IDEF0Plugin.findElementForBaseFunction(
                                                id, engine), data, attributes,
                                        objects) {
                                    @Override
                                    public String getName() {
                                        return qualifier.getName();
                                    }

                                    @Override
                                    public boolean isBase() {
                                        return true;
                                    }

                                    @Override
                                    public boolean isElement() {
                                        return false;
                                    }

                                    @Override
                                    public long getQualifierId() {
                                        return id;
                                    }

                                };
                            }
                        }) {
                    @Override
                    protected boolean filter(Element element) {
                        return false;
                    }
                };
            }

            if (create)
                res = new RowSet(engine, qualifier,
                        qualifier.getAttributes()
                                .toArray(
                                        new Attribute[qualifier.getAttributes()
                                                .size()]),
                        new RowSet.RowCreater() {
                            @Override
                            public com.ramussoft.database.common.Row createRow(
                                    Element element, RowSet data,
                                    Attribute[] attributes, Object[] objects) {
                                NRow row = new NRow(NDataPlugin.this, element,
                                        data, attributes, objects);
                                row.setElement(true);
                                return row;
                            }
                        });
            rowSets.put(id, res);
        }
        return res;
    }

    @Override
    public Row findRowByName(String rowName) {
        for (com.ramussoft.database.common.Row row : qualifiers.getAllRows()) {
            if (rowName.equals(row.getName())) {
                return (Row) row;
            }
        }
        return null;
    }

    @Override
    public void loadFromFile(File file) throws IOException {
    }

    @Override
    public void saveToFile(File file) throws IOException {
    }

    private void getRecChilds(Row row, final boolean element, final Vector res) {
        Vector<Row> v = getChilds(row, element);
        for (Row r : v) {
            res.add(r);
            getRecChilds(r, element, res);
        }
    }

    public Vector<Row> getRecChilds(Row parent, final boolean element) {
        final Vector res = new Vector();
        getRecChilds(parent, element, res);
        return res;
    }

    public Vector getChilds(Row row, final boolean element) {
        assert row == null && element == false || row != null;

        final Vector res = new Vector();
        Row tmp;
        if (row == null)
            for (com.ramussoft.database.common.Row r : qualifiers.getAllRows()) {
                tmp = (Row) r;
                if ((tmp.isElement() == element)
                        && (tmp.getParent().equals(qualifiers.getRoot())))
                    res.add(tmp);
            }
        else {
            row = replaceParent(row, element);

            for (int i = 0; i < row.getChildCount(); i++) {
                tmp = (Row) row.getChildAt(i);
                if (tmp.isElement() == element)
                    res.add(tmp);
            }
        }
        return res;
    }

    private Row replaceParent(Row row, final boolean element) {
        if ((!row.isElement()) && (element) && (!(row instanceof Stream))
                && (!(row instanceof Function))) {
            long id = (Long) ((com.ramussoft.database.common.Row) row)
                    .getAttribute(qualifierId);
            RowSet rs = getRowSet(id);
            row = (Row) rs.getRoot();
        }
        return row;
    }

    public void sortByName(final Row row, final boolean element) {
        if (row == null) {
            final Vector v = getChilds(null, element);
            final Object o[] = v.toArray();
            Arrays.sort(o, new RowNameComparator<Object>());

        } else
            row.sortByName(element);
    }

    public Engine getEngine() {
        return engine;
    }

    @Override
    public Sector findSectorByGlobalId(GlobalId globalId) {
        return findSectorByGlobalId(globalId.getLocalId());
    }

    public Sector findSectorByGlobalId(long id) {
        return (NSector) sectorsRowSet.get(id);
    }

    @Override
    public Vector<Sector> getAllSectors() {
        return new Vector(sectorsRowSet.values());
    }

    public AccessRules getAccessRules() {
        if (accessRules == null)
            accessRules = new LocalAccessor(engine.getDeligate()) {

                @Override
                public boolean isBranchLeaf() {
                    return false;
                }
            };
        return accessRules;
    }

    @Override
    public Qualifier getBaseFunctionQualifier() {
        throw new RuntimeException(
                "Method can not be called directly from this class.");
    }

    public Vector getSectors(final Function function) {
        final Vector res = new Vector();
        for (NSector o : sectorsRowSet.values()) {
            if (function.equals(o.getFunction()))
                res.add(o);
        }
        return res;
    }

    @Override
    public synchronized void clear() {
        for (RowSet rs : rowSets.values())
            rs.close();
        rowSets.clear();
        sectorsRowSet.clear();
        loadSectors();
        crosspoints.clear();
        qualifiers.refresh();
    }

    private void loadSectors() {
        for (Element element : getEngine().getElements(sectors.getId()))
            sectorsRowSet.put(element.getId(), new NSector(this, element));
    }

    public void close() {
        for (RowSet rs : rowSets.values())
            rs.close();
        rowSets.clear();
        sectorsRowSet.clear();
        crosspoints.clear();
        qualifiers.close();
        streamsRowSet.close();
        engine.removeElementListener(sectors, listener);
        engine.removeElementAttributeListener(sectors, crosspointsListener);
    }

    public Attribute getFunctionAttribute(int i) {
        return functionAttributes[i];
    }

    public Crosspoint createCrosspoint() {
        long id = IDEF0Plugin.getNextCrosspointId(engine);
        final Crosspoint c = createCrosspoint(id);
        return c;
    }

    @Override
    public boolean isReadOnly() {
        throw new RuntimeException(
                "Method can not be called directly from NDataPlugin");
    }

    @Override
    public boolean isParent(Row row, Row parent) {
        if (row == null)
            return false;
        if ((row instanceof Stream) && (parent instanceof Stream))
            return true;

        if (row.equals(parent))
            return true;

        if (parent == null)
            return true;

        if ((parent.isElement()) || (row instanceof Function)) {
            return isParent(row.getParentRow(), parent);
        } else {
            long qId = (Long) engine.getAttribute(parent.getElement(),
                    qualifierId);
            return qId == engine.getQualifierIdForElement(row.getElement()
                    .getId());
        }
    }

    @Override
    public Hashtable<Long, Sector> getSectorHash() {
        return (Hashtable) sectorsRowSet;
    }

    public com.ramussoft.database.common.Row getNBaseFunction(long id) {
        return getRowSet(id).getRoot();
    }

    @Override
    public Vector<Row> getRecChildren(Qualifier qualifier) {
        return new Vector(getRowSet(qualifier.getId()).getAllRows());
    }

    @Override
    public void refresh(GUIFramework framework) {
        throw new RuntimeException(
                "refresh(GUIFramework framework) can not be called directly for NDataPlugin");
    }

    @Override
    public void exportToIDL(Function baseFunction,
                            OutputStream fileOutputStream, String encoding) throws IOException {
        IDLExporter exporter = new IDLExporter((NFunction) baseFunction, this,
                encoding);
        exporter.export(fileOutputStream);
    }

    @Override
    public void importFromIDL(DataPlugin plugin, String encoding,
                              InputStream inputStream) throws IOException {
        IDLImporter importer = new IDLImporter(this, plugin.getBaseFunction(),
                encoding, plugin);
        importer.importFromIDL(inputStream);
        inputStream.close();
    }

    public RowSet getStreamsRowSet() {
        return streamsRowSet;
    }

    public void startUserTransaction() {
        if (engine instanceof Journaled)
            ((Journaled) engine).startUserTransaction();
    }

    @Override
    public Function createFunction(Function activeFunction, int type) {
        throw new RuntimeException("Cannot call method directly");
    }

    @Override
    public void compileDFDSName(DFDSName name, Function function) {
        Row row2 = function.getOwner();
        String string = "";
        if (row2 != null)
            string = row2.toString();

        String sName = name.getShortNameSource();
        String lName = name.getLongNameSource();
        if (sName == null || lName == null)
            return;

        lName = compileLinks(lName);
        sName = compileLinks(sName);

        lName = lName.replace(DFDSNamePlugin.ROLE, string);
        name.setLongName(lName);
        sName = sName.replace(DFDSNamePlugin.ROLE, string);
        name.setShortName(sName);

        String term = function.getTerm();
        if (term != null && term.trim().length() > 0) {
            if (name.getLongName() != null)
                name.setLongName(lName.replace(DFDSNamePlugin.TERM, term));
            if (name.getShortName() != null)
                name.setShortName(sName.replace(DFDSNamePlugin.TERM, term));
        }
    }

    private String compileLinks(String source) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch == '\\') {
                String code = "\\";
                for (i++; i < source.length(); i++) {
                    ch = source.charAt(i);
                    if (Character.isDigit(ch) || ch == '.')
                        code += ch;
                    else
                        break;
                }
                if (code.endsWith(".")) {
                    code = code.substring(0, code.length() - 1);
                    i--;
                }
                String ih = getDNameLink(code);
                if (ih != null) {
                    sb.append(ih);
                } else
                    sb.append(code);
                i--;
            } else
                sb.append(ch);
        }
        return sb.toString();
    }

    private String getDNameLink(String code) {
        StringTokenizer st = new StringTokenizer(code, "\\.");
        String f = null;
        if (st.hasMoreElements())
            f = st.nextToken();
        String s = null;
        if (st.hasMoreElements())
            s = st.nextToken();
        if (f == null)
            return null;
        Object row;
        if (s == null)
            row = findSectorByGlobalId(Long.parseLong(f));
        else
            row = findRowByGlobalId(Long.parseLong(s));
        if (row == null)
            return null;
        return row.toString();
    }
}
