package com.ramussoft.pb.data.negine;

import static com.ramussoft.idef0.IDEF0Plugin.F_AUTHOR;

import static com.ramussoft.idef0.IDEF0Plugin.F_BACKGROUND;
import static com.ramussoft.idef0.IDEF0Plugin.F_BOUNDS;
import static com.ramussoft.idef0.IDEF0Plugin.F_CREATE_DATE;
import static com.ramussoft.idef0.IDEF0Plugin.F_DECOMPOSITION_TYPE;
import static com.ramussoft.idef0.IDEF0Plugin.F_FONT;
import static com.ramussoft.idef0.IDEF0Plugin.F_FOREGROUND;
import static com.ramussoft.idef0.IDEF0Plugin.F_LINK;
import static com.ramussoft.idef0.IDEF0Plugin.F_OUNER_ID;
import static com.ramussoft.idef0.IDEF0Plugin.F_REV_DATE;
import static com.ramussoft.idef0.IDEF0Plugin.F_STATUS;
import static com.ramussoft.idef0.IDEF0Plugin.F_SYSTEM_REV_DATE;
import static com.ramussoft.idef0.IDEF0Plugin.F_TYPE;
import static com.ramussoft.idef0.IDEF0Plugin.F_VISUAL_DATA;
import static com.ramussoft.idef0.IDEF0Plugin.F_PAGE_SIZE;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.idef.elements.Status;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.utils.Options;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.DFDSName;
import com.ramussoft.idef0.attribute.DFDSNamePlugin;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.MatrixProjection;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.RowFactory;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.frames.IDEFPanel;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.types.GlobalId;

public class NFunction extends NRow implements Function {

    public NFunction(NDataPlugin dataPlugin, Element element,
                     com.ramussoft.database.common.RowSet rowSet,
                     Attribute[] attributes, Object[] objects) {
        super(dataPlugin, element, rowSet, attributes, objects);
        rowType = TYPE_FUNCTION;
    }

    private static final int VISUAL_DATA = 0;

    private static final int BACKGROUND = 1;

    private static final int FOREGROUND = 2;

    private static final int BOUNDS = 3;

    private static final int FONT = 4;

    private static final int STATUS = 5;

    private static final int TYPE = 6;

    private static final int OWNER_ID = 7;

    private static final int DIAGRAM_TYPE = 8;

    private static final int AUTHOR = 9;

    private static final int CREATE_DATE = 10;

    private static final int REV_DATE = 11;

    private static final int SYSTEM_REV_DATE = 12;

    private static final int LINK = 13;

    private static final int PAGE_SIZE = 14;

    private Vector sectors = null;

    public static final String[] PROPERTIES = new String[]{F_VISUAL_DATA,
            F_BACKGROUND, F_FOREGROUND, F_BOUNDS, F_FONT, F_STATUS, F_TYPE,
            F_OUNER_ID, F_DECOMPOSITION_TYPE, F_AUTHOR, F_CREATE_DATE,
            F_REV_DATE, F_SYSTEM_REV_DATE, F_LINK, F_PAGE_SIZE};

    private Object getObject(int i) {
        return getAttribute(getFunctionAttribute(i));
    }

    @Override
    public Object getAttribute(Attribute attribute) {
        if (isBase())
            return engine.getAttribute(getElement(), attribute);
        return super.getAttribute(attribute);
    }

    private Attribute getFunctionAttribute(int i) {

        return ((NDataPlugin) dataPlugin).getFunctionAttribute(i);

    }

    private void setObject(int i, Object value) {
        try {
            Attribute attribute = getFunctionAttribute(i);
            if (isBase())
                engine.setAttribute(getElement(), attribute, value);
            else
                setAttribute(attribute, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDefaultValues() {
        boolean inTransaction = false;
        if (engine instanceof Journaled) {
            inTransaction = ((Journaled) engine).isUserTransactionStarted();
            if (!inTransaction)
                ((Journaled) engine).startUserTransaction();
        }
        setSectorData(new byte[0]);
        setBackground(Options.getColor("DEFAULD_FUNCTIONAL_BLOCK_COLOR",
                Color.white));
        setForeground(Options.getColor("DEFAULD_FUNCTIONAL_BLOCK_TEXT_COLOR",
                Color.black));

        setBounds(new FRectangle(IDEFPanel.DEFAULT_X, IDEFPanel.DEFAULT_Y,
                IDEFPanel.DEFAULT_WIDTH * 1.2, IDEFPanel.DEFAULT_HEIGHT * 1.2));

        setFont(Options.getFont("DEFAULT_FUNCTIONAL_BLOCK_FONT", new Font(
                "Dialog", 0, 10)));

        setStatus(new Status());

        if (engine instanceof Journaled)
            if (!inTransaction)
                ((Journaled) engine).commitUserTransaction();
    }

    public FRectangle getBounds() {
        FRectangle rectangle = (FRectangle) getObject(BOUNDS);
        if (rectangle == null) {
            setDefaultValues();
            return (FRectangle) getObject(BOUNDS);
        }
        return rectangle;
    }

    public Color getBackground() {
        return (Color) getObject(BACKGROUND);
    }

    public Color getForeground() {
        return (Color) getObject(FOREGROUND);
    }

    public Status getStatus() {
        Status status = (Status) getObject(STATUS);
        if (status == null)
            return new Status();
        return status;
    }

    public Font getFont() {
        return (Font) getObject(FONT);
    }

    public int getType() {
        Integer object = (Integer) getObject(TYPE);
        return (object == null) ? TYPE_PROCESS : object;
    }

    public Row getOwner() {
        // Row row = dataPlugin.findRowByGlobalId(getLink());
        // if (row != null)
        // return row;
        Row owner = getNativeOwner();
        if (getType() >= Function.TYPE_EXTERNAL_REFERENCE)
            return owner;

        if (owner == null) {
            Function par = (Function) getParent();
            if (par == null)
                return null;
            Row ow = null;
            if (par.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
                for (com.ramussoft.database.common.Row r : ((NFunction) par)
                        .getChildren()) {
                    Function function = (Function) r;
                    if (function.getType() == TYPE_DFDS_ROLE
                            && this.equals(function.getOwner())) {
                        Stream stream = (Stream) dataPlugin
                                .findRowByGlobalId(function.getLink());
                        if (stream != null) {
                            Row[] rows = stream.getAdded();
                            if (rows.length > 1) {
                                ow = null;
                                break;
                            } else if (rows.length == 1) {
                                if (ow != null) {
                                    ow = null;
                                    break;
                                } else
                                    ow = rows[0];
                            }
                        }
                    }
                }
            }
            if (ow != null)
                return ow;
        }

        if (owner == null && getParentRow() != null)
            return ((Function) getParentRow()).getOwner();
        return owner;
    }

    public void setBounds(final FRectangle rectangle) {
        setObject(BOUNDS, rectangle);
        if (getParentRow() != null) {
            final int res = getFId();
            if (res != super.getId()) {
                ((NFunction) getParentRow()).sortChilds();
            }
        }
    }

    public void setBackground(final Color color) {
        setObject(BACKGROUND, color);
    }

    public void setForeground(final Color color) {
        setObject(FOREGROUND, color);
    }

    public void setStatus(final Status status) {
        setObject(STATUS, status);
    }

    public void setFont(final Font font) {
        setObject(FONT, font);
    }

    public void setType(final int type) {
        setObject(TYPE, type);
    }

    public void setOwner(final Row owner) {
        Function ow = null;
        if (getType() == Function.TYPE_DFDS_ROLE) {
            ow = (Function) owner;
            if (ow == null)
                ow = (Function) getOwner();
        }

        if (owner == null) {
            setObject(OWNER_ID, null);
        } else {
            setObject(OWNER_ID, ((NRow) owner).getElementId());
        }
        Function function = (Function) getParent();
        if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
            HashSet<Sector> toUpdate = new HashSet<Sector>();
            if (getType() == Function.TYPE_DFDS_ROLE) {
                if (ow != null) {
                    for (Sector sector : function.getSectors()) {
                        if (ow.equals(sector.getStart().getFunction())
                                || ow.equals(sector.getEnd().getFunction()))
                            if (!toUpdate.contains(sector))
                                toUpdate.add(sector);
                    }
                }
            } else {
                for (Sector sector : function.getSectors()) {
                    if (this.equals(sector.getStart().getFunction())
                            || this.equals(sector.getEnd().getFunction()))
                        if (!toUpdate.contains(sector))
                            toUpdate.add(sector);
                }
            }
            for (Sector sector : toUpdate)
                SectorRefactor.fixOwners(sector, getDataPlugin());
        }
        for (Attribute attribute : engine.getQualifier(
                getElement().getQualifierId()).getAttributes())
            if (attribute.getAttributeType().equals(DFDSNamePlugin.type)) {
                DFDSName dfdsName = (DFDSName) engine.getAttribute(
                        getElement(), attribute);
                if (dfdsName != null)
                    dataPlugin.compileDFDSName(dfdsName, this);
            }
    }

    public Row[] getOwners() {
        Function function = (Function) getParentRow();
        if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
            if (function != null) {
                List<Row> res = new ArrayList<Row>();
                int c = function.getChildCount();
                for (int i = 0; i < c; i++) {
                    Function function2 = (Function) function.getChildAt(i);
                    if (function2.getType() == Function.TYPE_DFDS_ROLE
                            && this.equals(function2.getOwner())) {
                        Stream stream = (Stream) dataPlugin
                                .findRowByGlobalId(function2.getLink());
                        if (stream != null) {
                            for (Row row : stream.getAdded())
                                if (!res.contains(row))
                                    res.add(row);
                        }
                    }
                }
                Row[] rows = res.toArray(new Row[res.size()]);
                RowFactory.sortByName(rows);
                return rows;
            }
        }

        String s = dataPlugin.getProperty(DataPlugin.PROPERTY_OUNERS);
        if (s == null)
            s = "";

        final StringTokenizer st = new StringTokenizer(s);
        final Vector<GlobalId> ids = new Vector<GlobalId>();
        while (st.hasMoreTokens()) {
            ids.add(GlobalId.convert(st.nextToken()));
        }

        final MatrixProjection projection = dataPlugin.getProjection(
                MatrixProjection.TYPE_IDEF0, "BOTTOM");
        final Vector left = projection.getLeft(this);
        final Vector<Row> res = new Vector<Row>();
        Row[] rows;
        for (int i = 0; i < left.size(); i++) {
            final Stream r = (Stream) left.get(i);
            if ((rows = r.getAdded()).length == 0)
                addRow(res, ids, r);
            else
                for (final Row element2 : rows)
                    addRow(res, ids, element2);
        }
        rows = res.toArray(new Row[res.size()]);
        RowFactory.sortByName(rows);
        return rows;
    }

    private void addRow(final Vector<Row> res, Vector<GlobalId> ids,
                        final Row row) {
        if (ids.indexOf(GlobalId.create(engine
                .getQualifierIdForElement(((com.ramussoft.database.common.Row) row)
                        .getElementId()))) >= 0)
            res.add(row);
    }

    public void setLookForChildrens() {
        final Vector v = dataPlugin.getRecChilds(this, true);
        for (int i = 0; i < v.size(); i++) {
            NFunction nFunction = (NFunction) v.get(i);
            if (nFunction.getType() < Function.TYPE_EXTERNAL_REFERENCE)
                nFunction.loadLookOptions(this);
        }
    }

    public void setLookForChildrensFont() {
        final Vector v = dataPlugin.getRecChilds(this, true);
        for (int i = 0; i < v.size(); i++) {
            NFunction nFunction = (NFunction) v.get(i);
            if (nFunction.getType() < Function.TYPE_EXTERNAL_REFERENCE)
                nFunction.setFont(this.getFont());
        }
    }

    public void setLookForChildrensForeground() {
        final Vector v = dataPlugin.getRecChilds(this, true);
        for (int i = 0; i < v.size(); i++) {
            NFunction nFunction = (NFunction) v.get(i);
            if (nFunction.getType() < Function.TYPE_EXTERNAL_REFERENCE)
                nFunction.setForeground(this.getForeground());
        }
    }

    public void setLookForChildrensBackground() {
        final Vector v = dataPlugin.getRecChilds(this, true);
        for (int i = 0; i < v.size(); i++) {
            NFunction nFunction = (NFunction) v.get(i);
            if (nFunction.getType() < Function.TYPE_EXTERNAL_REFERENCE)
                nFunction.setBackground(this.getBackground());
        }
    }

    private void loadLookOptions(final NFunction function) {
        this.setFont(function.getFont());
        this.setBackground(function.getBackground());
        this.setForeground(function.getForeground());
    }

    public byte[] getSectorData() {
        return (byte[]) getObject(VISUAL_DATA);
    }

    public void setSectorData(final byte[] data) {
        setObject(VISUAL_DATA, data);
    }

    public Vector getSectors() {
        synchronized (this) {
            if (sectors == null) {
                sectors = ((NDataPlugin) dataPlugin).getSectors(this);
            }
            return sectors;
        }
    }

    @Override
    public boolean remove() {
        clearSectorsBuffer();
        final Vector<Sector> v = getSectors();
        for (int i = 0; i < v.size(); i++) {
            v.get(i).remove();
        }
        if (dataPlugin.getEngine().getElement(getElementId()) == null)
            return true;
        for (int i = getChildCount() - 1; i >= 0; i--)
            ((NFunction) getChildAt(i)).remove();
        return super.remove();
    }

    public void clearSectorsBuffer() {
        this.sectors = null;
    }

    @Override
    public boolean isMoveable() {
        return false;
    }

    public boolean lock() {
        return true;
    }

    public void unlock() {

    }

    public boolean isLocked() {
        return false;
    }

    private int getFId() {
        Row parentRow = getParentRow();
        if (parentRow == null)
            return -1;
        final Enumeration e = parentRow.children();
        int res = 1;
        while (e.hasMoreElements()) {
            final NFunction function = (NFunction) e.nextElement();
            if (function.getBounds().getX() < getBounds().getX())
                res++;
            else if (function.getBounds().getX() == getBounds().getX())
                if (function.getBounds().getY() < getBounds().getY())
                    res++;
        }
        return res;
    }

    private synchronized void sortChilds() {
        final NFunction[] os = children.toArray(new NFunction[children.size()]);
        if (os.length < 1)
            return;
        Arrays.sort(os, new Comparator<NFunction>() {

            public int compare(final NFunction o1, final NFunction o2) {
                return ((NFunction) o1).getFId() - ((NFunction) o2).getFId();
            }

        });

        HierarchicalPersistent hp = os[0].getHierarchicalPersistent();
        if (hp.getPreviousElementId() != -1l) {
            hp.setPreviousElementId(-1l);
            os[0].setHierarchicalPersistent(hp);
        }

        for (int i = 1; i < os.length; i++) {
            hp = os[i].getHierarchicalPersistent();
            if (hp.getPreviousElementId() != os[i - 1].getElementId()) {
                hp.setPreviousElementId(os[i - 1].getElementId());
                os[i].setHierarchicalPersistent(hp);
            }
        }

    }

    private Row getNativeOwner() {
        Long object = (Long) getObject(OWNER_ID);
        if (object == null)
            return null;
        return ((NDataPlugin) dataPlugin).findRowByGlobalId(object);
    }

    @Override
    public boolean isRemoveable() {
        if (isLocked())
            return false;
        return super.isRemoveable();
    }

    @Override
    public boolean isCanHaveChilds() {
        return false;
    }

    public boolean isBase() {
        return false;
    }

    @Override
    public int getDecompositionType() {
        Integer object = (Integer) getObject(DIAGRAM_TYPE);
        if (object == null)
            return -1;
        return object.intValue();
    }

    @Override
    public void setDecompositionType(int type) {
        checkAllAttributes();
        if (type == 0)
            setObject(DIAGRAM_TYPE, null);
        else
            setObject(DIAGRAM_TYPE, type);
    }

    @Override
    public ProjectOptions getProjectOptions() {
        NFunction f = this;
        while (f.getParent() != null) {
            f = (NFunction) f.getParentRow();
        }

        ProjectOptions options = (ProjectOptions) f.getAttribute(IDEF0Plugin
                .getProjectPreferencesAttrtibute(dataPlugin.getEngine()));
        if (options == null)
            options = new ProjectOptions();
        return options;
    }

    @Override
    public void setProjectOptions(ProjectOptions project) {
        NFunction f = this;
        while (f.getParent() != null) {
            f = (NFunction) f.getParentRow();
        }
        f.setAttribute(IDEF0Plugin.getProjectPreferencesAttrtibute(dataPlugin
                .getEngine()), project);
    }

    @Override
    public String getAuthor() {
        String author = (String) getObject(AUTHOR);
        if (author == null) {
            if (getParentRow() == null)
                return getProjectOptions().getProjectAutor();
            else
                return ((NFunction) getParentRow()).getAuthor();
        }
        return author;
    }

    @Override
    public Date getCreateDate() {
        Date date = (Date) getObject(CREATE_DATE);
        if (date == null) {
            if (getParentRow() == null)
                return getProjectOptions().getDateCreateDate();
            return ((NFunction) getParentRow()).getCreateDate();
        }
        return date;
    }

    @Override
    public Date getRevDate() {
        Date date = (Date) getObject(REV_DATE);
        if (date == null) {
            date = getSystemRevDate();
            if (date != null)
                return date;
            if (getParentRow() == null)
                return getProjectOptions().getDateChangeDate();
            return ((NFunction) getParentRow()).getRevDate();
        }
        return date;
    }

    @Override
    public Date getSystemRevDate() {
        Date date = (Date) getObject(SYSTEM_REV_DATE);
        if (date == null) {
            if (getParentRow() != null)
                return ((NFunction) getParentRow()).getSystemRevDate();
        }
        return date;
    }

    @Override
    public void setCreateDate(Date date) {
        checkAllAttributes();
        setObject(CREATE_DATE, date);
    }

    private void checkAllAttributes() {
        Qualifier qualifier = getQualifier();
        IDEF0Plugin.checkIDEF0Attributes(engine, qualifier);
    }

    @Override
    public void setRevDate(Date date) {
        checkAllAttributes();
        setObject(REV_DATE, date);
    }

    @Override
    public void setSystemRevDate(Date date) {
        checkAllAttributes();
        setObject(SYSTEM_REV_DATE, date);
    }

    @Override
    public void setAuthor(String author) {
        checkAllAttributes();
        setObject(AUTHOR, author);
    }

    @Override
    public long getLink() {
        Long link = (Long) getObject(LINK);
        if (link == null)
            return -1;
        return link;
    }

    @Override
    public void setLink(long link) {
        checkAllAttributes();
        setObject(LINK, link);
    }

    @Override
    public String getName() {
        Row row = dataPlugin.findRowByGlobalId(getLink());
        if (row != null)
            return row.getName();
        return super.getName();
    }

    @Override
    public String getNativeName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        Row row = dataPlugin.findRowByGlobalId(getLink());
        if (row != null)
            row.setName(name);
        else {
            Attribute attribute = engine.getAttribute(rowSet.getQualifier()
                    .getAttributeForName());
            if (attribute.getAttributeType().toString()
                    .equals("IDEF0.DFDSName")) {
                DFDSName dfdsName = new DFDSName();
                int r = name.indexOf('\n');
                if (r >= 0) {
                    dfdsName.setShortName(name.substring(0, r));
                    dfdsName.setLongName(name.substring(r + 1));
                } else {
                    dfdsName.setShortName(name);
                    dfdsName.setLongName("");
                }
                setNameObject(dfdsName);
            } else
                super.setName(name);
        }
    }

    @Override
    public void setNativeName(String name) {
        super.setName(name);
    }

    @Override
    public int getId() {
        if (getParent() == null)
            return 0;
        List<com.ramussoft.database.common.Row> list = getParent()
                .getChildren();
        int i = 0;
        for (com.ramussoft.database.common.Row r : list) {
            NFunction child = (NFunction) r;
            if (child.getType() < TYPE_EXTERNAL_REFERENCE)
                i++;
            if (r == this)
                break;
        }
        return i;
    }

    @Override
    public boolean isHaveRealChilds() {
        List<com.ramussoft.database.common.Row> rows = getChildren();
        if (rows.size() == 0)
            return false;
        for (com.ramussoft.database.common.Row row : rows) {
            if (((NFunction) row).getType() < TYPE_EXTERNAL_REFERENCE)
                return true;
        }
        return false;
    }

    @Override
    public int getRealChildCount() {
        List<com.ramussoft.database.common.Row> rows = getChildren();
        if (rows.size() == 0)
            return 0;
        int s = 0;
        for (com.ramussoft.database.common.Row row : rows) {
            if (((NFunction) row).getType() < TYPE_EXTERNAL_REFERENCE)
                s++;
        }
        return s;
    }

    @Override
    public Function getRealChildAt(int i) {
        List<com.ramussoft.database.common.Row> rows = getChildren();
        if (rows.size() == 0)
            return null;
        int s = 0;
        for (com.ramussoft.database.common.Row row : rows) {
            if (((NFunction) row).getType() < TYPE_EXTERNAL_REFERENCE) {
                if (i == s)
                    return (Function) row;
                s++;
            }
        }
        return null;
    }

    @Override
    public String getTerm() {
        Long trm = getProjectOptions().getDeligate().getTermAttribute();
        if (trm == null)
            return null;
        for (Attribute attr : getRowAttributes()) {
            if (attr.getId() == trm.longValue()) {
                Object name = getAttribute(attr);
                if (name != null)
                    return name.toString();
            }
        }
        return null;
    }

    @Override
    public String getPageSize() {
        String pageSize = (String) getObject(PAGE_SIZE);
        if (pageSize == null)
            return getProjectOptions().getDeligate().getDiagramSize();
        return pageSize;
    }

    @Override
    public void setPageSize(String pageSize) {
        String size = getProjectOptions().getDeligate().getDiagramSize();
        if (size != null && size.equals(pageSize))
            setObject(PAGE_SIZE, null);
        else
            setObject(PAGE_SIZE, pageSize);
    }

}
