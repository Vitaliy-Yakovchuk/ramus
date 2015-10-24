package com.ramussoft.pb.data;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Vector;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.negine.NDataPlugin;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.data.negine.NStream;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.idef.elements.SectorRefactor;

public abstract class AbstractSector implements Sector {

    private boolean rec = false;

    protected NDataPlugin dataPlugin;

    protected Element element;

    public AbstractSector(final NDataPlugin dataPlugin, Element element) {
        this.element = element;
        this.dataPlugin = dataPlugin;
    }

    public static Sector[] toArray(final Vector v) {
        final int n = v.size();
        final Sector[] res = new Sector[n];
        for (int i = 0; i < n; i++)
            res[i] = (Sector) v.get(i);
        return res;
    }

    /**
     * Повертає масив потоків пов’язаних з масивом секторів.
     *
     * @param sectors Масив секторів, з якого буде виділений масив потоків.
     * @return Масив потоків, виділений з масиву секторів.
     */

    protected Stream[] getStreams(final Sector[] sectors) {
        final Vector<Stream> v = new Vector<Stream>();
        for (final Sector element : sectors)
            if (element.getStream() != null)
                if (v.indexOf(element.getStream()) < 0)
                    v.add(element.getStream());
        final Stream[] streams = new Stream[v.size()];
        for (int i = 0; i < v.size(); i++)
            streams[i] = v.get(i);
        return streams;
    }

    /**
     * Метод повертає набір під’єднаних секторв, які мають зв’язок з потоком.
     *
     * @param point
     * @param v
     */

    protected void getStreamedSectors(final Crosspoint point,
                                      final Vector<Sector> v) {
        if (point == null)
            return;
        final int oLen = v.size();
        point.getSectors(v);
        final int len = v.size();
        final Vector<Sector> r = new Vector<Sector>();
        for (int i = oLen; i < len; i++)
            if (!v.get(i).getFunction().equals(getFunction())
                    || v.get(i).getStream() == null
                    || !v.get(i).getStream().equals(getStream()))
                r.add(v.get(i));
        for (int i = 0; i < r.size(); i++)
            v.remove(r.get(i));
    }

    /**
     * Метод повертає набір секторів, приєднаних до стрілки, при чому їхні
     * потоки однакові (як пезпосередньо, так і без назви, але з однаковим
     * вмістом приєднаних елементів класифікаторів).
     *
     * @param v
     */

    protected void getAllStreamedSectors(final Vector<Sector> v,
                                         final boolean equalByAdded) {

        if (rec)
            return;
        rec = true;
        final int oLen = v.size();
        getStreamedAllSectors(getStart().getCrosspoint(), v, equalByAdded);
        getStreamedAllSectors(getEnd().getCrosspoint(), v, equalByAdded);

        final int len = v.size();

        for (int i = oLen; i < len; i++)
            ((AbstractSector) v.get(i)).getAllStreamedSectors(v, equalByAdded);
        rec = false;
    }

    private void getStreamedAllSectors(final Crosspoint point,
                                       final Vector<Sector> v, final boolean equalByAdded) {
        if (point == null)
            return;
        final Vector<Sector> srs = new Vector<Sector>();
        getChilds(point, srs);
        for (final Sector s : srs)
            if (v.indexOf(s) < 0) {
                if (s.getStream() == null)
                    v.add(s);
                else if ((equalByAdded) && (s.getStream().isEmptyName())
                        && (equalsStreamsByAdded(s.getStream(), getStream())))
                    v.add(s);
                else if (s.getStream().equals(this.getStream()))
                    v.add(s);
            }
        srs.clear();
        getParents(point, srs);
        for (final Sector s : srs)
            if (v.indexOf(s) < 0) {
                if (s.getStream() == null)
                    v.add(s);
                else if ((equalByAdded) && (s.getStream().isEmptyName())
                        && (equalsStreamsByAdded(s.getStream(), getStream())))
                    v.add(s);
            }
    }

    private boolean equalsStreamsByAdded(final Stream s1, final Stream s2) {
        if (s1 == null)
            return false;
        if (s2 == null)
            return false;
        return Arrays.equals(s1.getAdded(), s2.getAdded());
    }

    /**
     * Медод повертає масив секторів, які пов’язані між собою одним елементом
     * класифікатора потоків, крім того розташовані на тому ж функціональному
     * блоці. Результат включає самого себе також.
     *
     * @return Масив пов’язаних сеторів.
     */

    protected void getStreamedSectors(final Vector<Sector> v) {
        if (rec)
            return;
        rec = true;
        final int oLen = v.size();
        getStreamedSectors(getStart().getCrosspoint(), v);
        getStreamedSectors(getEnd().getCrosspoint(), v);

        final int len = v.size();

        for (int i = oLen; i < len; i++)
            ((AbstractSector) v.get(i)).getStreamedSectors(v);
        rec = false;
    }

    /**
     * Повертає масив батьківських сеторів, разом з батьківськими до
     * батьківських.
     *
     * @return
     */

    protected void getRecParents(final Vector<Sector> v) {
        if (rec)
            return;
        rec = true;
        final int oLen = v.size();
        getParents(v);
        final int len = v.size();
        for (int i = oLen; i < len; i++)
            ((AbstractSector) v.get(i)).getRecParents(v);
        rec = false;
    }

    /**
     * Метод повертає набір дочірніх до сектора секторів.
     *
     * @param point Точка дочірні сектори з якої беруться.
     * @return Масив дочірніх то сектора секторів, які пов’язані через точку.
     */

    protected void getChilds(final Crosspoint point, final Vector<Sector> v) {
        if (point == null || point.isChild(this))
            return;
        final Sector[] s = point.getOppozite(this);
        for (final Sector element : s)
            if (v.indexOf(element) < 0)
                v.add(element);
    }

    /**
     * Повертає масив секторів, які входять або виходять з даного сектора.
     *
     * @return Масив дочірніх секторів.
     */

    protected void getChilds(final Vector<Sector> v) {
        getChilds(getStart().getCrosspoint(), v);
        getChilds(getEnd().getCrosspoint(), v);
    }

    /**
     * Повертає масив дочірніх до сектора секторів разом з дочірніми до дочірніх
     * секторів.
     *
     * @return Дочірні сектори, разом з дочірніми до дочірніх.
     */

    protected void getRecChilds(final Vector<Sector> v) {
        if (rec)
            return;
        rec = true;
        final int oLen = v.size();
        getChilds(v);
        final int len = v.size();
        for (int i = oLen; i < len; i++)
            ((AbstractSector) v.get(i)).getRecChilds(v);
        rec = false;
    }

    protected void getParents(final Crosspoint point, final Vector<Sector> v) {
        if (point == null)
            return;
        final Sector[] s = point.getOppozite(this);
        if (s.length != 1)
            return;
        for (final Sector element : s)
            if (v.indexOf(element) < 0)
                v.add(element);
    }

    /**
     * Повертає масив батьківських секторів до сектора.
     *
     * @return Масив батьківських сеторів до сектора.
     */

    protected void getParents(final Vector<Sector> v) {
        getParents(getStart().getCrosspoint(), v);
        getParents(getEnd().getCrosspoint(), v);
    }

    public void afterCreate() {
        final Vector<Sector> v = new Vector<Sector>();

        getParents(v);
        for (int i = 0; i < v.size(); i++)
            if (v.get(i).getStream() != null) {
                setStream(v.get(i).getStream(), ReplaceStreamType.CHILDREN);
            }
    }

    private void getConnected(final Crosspoint point, final Vector<Sector> v) {
        if (point == null)
            return;
        point.getSectors(v);
    }

    /**
     * Повертає масив під’єднаних до сектора секторів, включно з цим сектором.
     *
     * @return Масив пов’язаних між собою секторів.
     */

    public void getConnected(final Vector<Sector> v) {
        if (rec)
            return;
        // if(v.indexOf(this)>=0)
        // return;
        rec = true;
        final int oLen = v.size();
        getConnected(getStart().getCrosspoint(), v);
        getConnected(getEnd().getCrosspoint(), v);
        final int len = v.size();
        for (int i = oLen; i < len; i++) {
            ((AbstractSector) v.get(i)).getConnected(v);
        }
        rec = false;
    }

    /**
     * Медот повертає масив пов’язаних секторів, при чому всі вона знаходяться
     * на одному фунціональному блоці.
     *
     * @return Масив пов’язаних секторів, включно з самим секторо.
     */

    private void getConnectedOnFunction(final Vector<Sector> v) {
        if (rec)
            return;
        rec = true;
        getConnectedOnFunction(getStart().getCrosspoint(), v);
        getConnectedOnFunction(getEnd().getCrosspoint(), v);
        rec = false;
    }

    /**
     * Метод повертає масив під’єднаних до точки секторів, при чому всі сектори
     * знаходяться на одному функціональному блоці.
     *
     * @param crosspoint Точка перетену секторів.
     * @return Масив секторів, що під’єднані до точки.
     */

    private void getConnectedOnFunction(final Crosspoint crosspoint,
                                        final Vector<Sector> v) {
        if (crosspoint == null)
            return;
        final Vector<Sector> ss = new Vector<Sector>();
        crosspoint.getSectors(ss);
        int i;
        final int n = ss.size();
        final int oLen = v.size();
        Sector s;
        for (i = 0; i < n; i++)
            if ((s = ss.get(i)).getFunction().equals(getFunction()))
                if (v.indexOf(s) < 0)
                    v.add(s);
        final int len = v.size();
        for (i = oLen; i < len; i++)
            ((AbstractSector) v.get(i)).getConnectedOnFunction(v);
    }

    /**
     * Метод призначений для розбивання потоку при розбитті сектору. Може
     * повертати переданий параметр.
     *
     * @param stream
     * @return
     */

    protected Stream splitStream(final Stream stream) {
        if (stream == null)
            return null;
        if (!stream.isEmptyName())
            return stream;
        final Stream res = (Stream) dataPlugin.createRow(
                dataPlugin.getBaseStream(), true);
        res.setRows(stream.getAdded());
        return res;
    }

    public Sector splitSector() {
        final AbstractSector sector = (AbstractSector) dataPlugin
                .createSector();
        sector.getEnd().setCrosspointA(getEnd().getCrosspoint());
        sector.getEnd().setFunctionA(getEnd().getFunction());
        sector.getEnd().setFunctionTypeA(getEnd().getFunctionType());
        sector.getEnd().setBorderTypeA(getEnd().getBorderType());
        sector.getEnd().commit();
        getEnd().setFunctionA(null);
        getEnd().setFunctionTypeA(-1);
        getEnd().setBorderTypeA(-1);
        final Crosspoint crosspoint = dataPlugin.createCrosspoint();
        sector.getStart().setCrosspointA(crosspoint);
        sector.getStart().commit();
        getEnd().setCrosspointA(crosspoint);
        getEnd().commit();
        sector.setFunction(getFunction());
        // sector.setStreamAddedByRefactor(isStreamAddedByRefactor());
        splitSectorStreams(sector);
        sector.setVisualAttributes(getVisualAttributes());
        return sector;
    }

    private void splitSectorStreams(final Sector sector) {
        final Stream my = getStream();
        final Stream other = splitStream(my);

        if (my == null)
            return;
        ((AbstractSector) sector).setThisStream(other);
    }

    public void joinSector(final Sector sector) {
        if (sector.getEnd().getCrosspoint() != null) {
            // getEnd().setCrosspointA(sector.getEnd().getCrosspoint());
            getEnd().setBorderTypeA(sector.getEnd().getBorderType());
            getEnd().setFunctionA(sector.getEnd().getFunction());
            getEnd().setFunctionTypeA(sector.getEnd().getFunctionType());
        }
        getEnd().setCrosspointA(sector.getEnd().getCrosspoint());
        getEnd().commit();
        if (!"".equals(sector.getAlternativeText())) {
            setAlternativeText(sector.getAlternativeText());
        }
        setShowText(true);
        if (sector.getStream() != null && sector.getStream().isEmptyName())
            ((NStream) sector.getStream()).remove();
        sector.remove();
        if (getStream() != null)
            setRows(getStream().getAdded());
    }

    public void reload() {
    }

    public void copyVisual(final int type) {
        synchronized (dataPlugin) {
            final Vector<Sector> v = new Vector<Sector>();
            getConnected(v);
            for (int i = 0; i < v.size(); i++) {
                v.get(i).setVisualAttributes(getVisualAttributes());
            }
        }
    }

    /**
     * Має задавати потік тільки для цього сектора і ні для яких інших.
     *
     * @param stream Потік, який буде задано тільки для цього сектора і ні для яких
     *               інших.
     */

    abstract public void setThisStream(Stream stream);

    public void setStream(final Stream stream, ReplaceStreamType type) {
        synchronized (dataPlugin) {
            if (sRec)
                return;

            if (rec || stream == null)
                return;
            sRec = true;

            if (stream != null) {
                final Vector<Sector> v = new Vector<Sector>();
                if (!stream.isEmptyName()) {
                    v.add(this);
                    getNullStreamedSectors(v);
                    addPart(v, getStart().getCrosspoint());
                    addPart(v, getEnd().getCrosspoint());

                    if (type.equals(ReplaceStreamType.SIMPLE)) {
                        getStreamChilds(v);
                        for (int i = v.size() - 1; i >= 0; i--) {
                            Sector s = v.get(i);
                            ((AbstractSector) s).getStreamChilds(v);
                        }
                    } else if (type.equals(ReplaceStreamType.CHILDREN)) {
                        for (int i = v.size() - 1; i >= 0; i--) {
                            Sector s = v.get(i);
                            ((AbstractSector) s).getRecEndChilds(v);
                        }
                    } else if (type.equals(ReplaceStreamType.ALL)) {
                        for (int i = v.size() - 1; i >= 0; i--) {
                            Sector s = v.get(i);
                            ((AbstractSector) s).getConnected(v);
                        }
                    }
                    for (Sector s : v)
                        ((AbstractSector) s).setThisStream(stream);
                } else {
                    getStreamChilds(v);
                    addPart(v, getStart().getCrosspoint());
                    addPart(v, getEnd().getCrosspoint());
                    for (final Sector s : v) {
                        setPersonalStream(s, stream);
                    }
                }

                v.clear();
                getConnected(v);
                for (Sector s : v)
                    if (s.getStream() != null) {
                        Row[] rows2 = s.getStream().getAdded();
                        Row[] rows = stream.getAdded();
                        boolean changed = false;
                        for (int i = 0; i < rows2.length; i++) {
                            Row r2 = rows2[i];
                            for (Row r : rows)
                                if (r.getElement().getId() == r2.getElement()
                                        .getId()) {
                                    rows2[i] = r;
                                    changed = true;
                                }
                        }
                        if (changed) {
                            s.getStream().addRows(rows2);
                            ((NStream) s.getStream()).saveAdded();
                        }
                    }
            }

            setThisStream(stream);
            sRec = false;
        }
    }

    private void getRecEndChilds(Vector<Sector> v) {
        if (rec)
            return;
        rec = true;
        final int oLen = v.size();
        getEndChilds(v);
        final int len = v.size();
        for (int i = oLen; i < len; i++)
            ((AbstractSector) v.get(i)).getRecEndChilds(v);
        rec = false;
    }

    private void getEndChilds(Vector<Sector> v) {
        getChilds(getEnd().getCrosspoint(), v);
    }

    private void getNullStreamedSectors(Vector<Sector> v) {
        if (rec)
            return;
        rec = true;
        try {
            if (getStream() == null) {
                if (v.indexOf(this) < 0)
                    v.add(this);
            } else
                return;

            final int oLen = v.size();
            getNullStreamedSectors(getStart().getCrosspoint(), v);
            getNullStreamedSectors(getEnd().getCrosspoint(), v);

            final int len = v.size();

            for (int i = oLen; i < len; i++)
                ((AbstractSector) v.get(i)).getNullStreamedSectors(v);
        } finally {
            rec = false;
        }
    }

    private void getNullStreamedSectors(Crosspoint crosspoint, Vector<Sector> v) {
        if (crosspoint == null)
            return;
        for (Sector s : crosspoint.getIns())
            ((AbstractSector) s).getNullStreamedSectors(v);
        for (Sector s : crosspoint.getOuts())
            ((AbstractSector) s).getNullStreamedSectors(v);
    }

    private void setPersonalStream(final Sector s, final Stream stream) {
        final Stream st = s.getStream();
        if (st == null || !st.isEmptyName())
            ((AbstractSector) s).setThisStream(SectorRefactor.cloneStream(
                    stream, dataPlugin, s));
        else
            st.setRows(stream.getAdded());
    }

    private void addPart(final Vector<Sector> v, final Crosspoint crosspoint) {
        if (crosspoint == null)
            return;
        final Sector[] op = crosspoint.getOppozite(this);
        if (op.length == 1 && !getFunction().equals(op[0].getFunction()))
            if (v.indexOf(op[0]) < 0)
                v.add(op[0]);
    }

    private void getStreamChilds(final Vector<Sector> v) {
        final Stream stream = getStream();
        final Vector<Sector> n = new Vector<Sector>(3);
        getChilds(getStart().getCrosspoint(), n);
        getChilds(getEnd().getCrosspoint(), n);
        for (final Sector s : n) {
            if ((s.getStream() == null || stream != null
                    && stream.equals(s.getStream()))
                    && (v.indexOf(s) < 0)) {
                v.add(s);
                ((AbstractSector) s).getStreamChilds(v);
            }
        }
    }

    boolean rFP = false;

    public void removeFromParent(Row[] rows, boolean start) {
        final Vector<Sector> v = new Vector<Sector>();
        if (start)
            getParents(getStart().getCrosspoint(), v);
        else
            getParents(getEnd().getCrosspoint(), v);
        for (int i = 0; i < v.size(); i++) {
            final AbstractSector s = (AbstractSector) v.get(i);
            final Stream ps = s.getStream();
            if (ps != null) {
                final Vector<Sector> v1 = new Vector<Sector>();
                if (s.getStart().getBorderType() < 0 && !start)
                    s.getChilds(s.getStart().getCrosspoint(), v1);
                if (s.getEnd().getBorderType() < 0 && start)
                    s.getChilds(s.getEnd().getCrosspoint(), v1);
                // v1.remove(this);
                for (int j = 0; j < v1.size(); j++) {
                    final Sector s1 = v1.get(j);
                    if (!equals(s1)) {
                        final Stream stream = s1.getStream();
                        if (stream != null) {
                            final Row[] rs = stream.getAdded();
                            rows = RowFactory.removeRows(rows, rs);
                            final Row[] add = RowFactory.removeRows(rs,
                                    ps.getAdded());
                            if (add.length > 0)
                                ps.addRows(add);
                        }
                    }
                }
                if (rows.length > 0) {
                    ps.removeRows(rows);
                    s.removeFromParent(rows, start);
                }
            }
        }
    }

    public void removeFromParent(final Row[] rows) {
        if (rFP)
            return;
        if (rows.length == 0)
            return;
        rFP = true;
        removeFromParent(rows, true);
        removeFromParent(rows, false);
        rFP = false;
    }

    private static boolean sRec = false;

    public void setRows(final Row[] rows) {
        synchronized (dataPlugin) {
            sRec = true;
            if (getStream() != null) {
                final Row[] dels = RowFactory.removeRows(
                        getStream().getAdded(), rows);
                if (dels.length > 0)
                    removeFromParent(dels);
                getStream().setRows(rows);
            }
            final Vector<Sector> v = new Vector();
            getRecParents(v);
            final Sector[] sectors = toArray(v);
            final Stream[] streams = getStreams(sectors);
            for (final Stream element : streams)
                element.addRows(rows);
            for (Sector s : sectors) {
                if (s.getStream() == null) {
                    Stream stream = (Stream) dataPlugin.createRow(
                            dataPlugin.getBaseStream(), true);
                    stream.addRows(rows);
                    ((NSector) s).setThisStream(stream);
                }
            }
            sRec = false;
        }
    }

    public void remove() {
        getEngine().deleteElement(element.getId());
    }

    public Engine getEngine() {
        return dataPlugin.getEngine();
    }

    public void loadRowAttributes(final Sector sector, final boolean start) {
        final Stream stream = sector.getStream();
        if (stream == null)
            return;
        /*
         * final Vector<Sector> add = new Vector<Sector>(); getConnected(add);
		 * final Row[] rows = stream.getAdded(); final GlobalId sId =
		 * sector.getGlobalId(); for (final Row row : rows) { final
		 * RowAttribute[] attrs = row.getAttributes(true); for (final
		 * RowAttribute attr : attrs) { final JoinTable table =
		 * dataPlugin.findJoinTableByGlobalId(attr .getJoinTableId()); final
		 * Object[][] sos = table.findRows(new int[] { 1, 3 }, new Object[] {
		 * row.getGlobalId(), sId }); for (final Object[] os : sos) { for (int i
		 * = 0; i < add.size(); i++) { final Sector s = add.get(i); if
		 * (!s.equals(sector)) table.addSetJoin(new Object[] {
		 * row.getGlobalId(), os[2], s.getGlobalId() }, new int[] { 1, 3 }); } }
		 * } }
		 */
    }

    public String getName() {
        final Stream stream = getStream();
        if (stream == null)
            return null;
        if (stream.isEmptyName()) {
            return NStream.getTitle(stream.getAdded());
        } else
            return stream.getName();
    }

    public boolean isConnectedOnFunction(final Sector sector) {
        final Vector<Sector> v = new Vector<Sector>();
        getConnectedOnFunction(v);
        return v.indexOf(sector) >= 0;
    }

    public static boolean equalsStreams(final Stream s1, final Stream s2) {
        if (s2 == null)
            return false;
        if (s1.equals(s2))
            return true;
        if (s1.isEmptyName() && s2.isEmptyName()) {
            final Row[] a = s1.getAdded();
            final Row[] b = s2.getAdded();
            return Arrays.equals(a, b);
        }
        return false;
    }

    private String toString(SectorBorder sb) {
        if (sb.getCrosspoint() == null)
            return "null";
        return Long.toString(sb.getCrosspoint().getGlobalId());
    }

    public String toDebugString() {
        return MessageFormat.format("ID: {0}, Start C: {1}, End C: {2}",
                getGlobalId(), toString(getStart()), toString(getEnd()));
    }

    public NDataPlugin getDataPlugin() {
        return dataPlugin;
    }

    public long getElementId() {
        return element.getId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractSector other = (AbstractSector) obj;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        return true;
    }

    public Element getElement() {
        return element;
    }
}
