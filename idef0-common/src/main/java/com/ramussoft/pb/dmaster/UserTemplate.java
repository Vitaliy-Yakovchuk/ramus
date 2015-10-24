package com.ramussoft.pb.dmaster;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import com.dsoft.utils.DataLoader;
import com.dsoft.utils.DataSaver;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.DFDSName;
import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.idef0.attribute.SectorPropertiesPersistent;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.RowNameComparator;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.data.negine.NCrosspoint;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

public class UserTemplate extends AbstractTemplate implements
        Comparable<UserTemplate> {

    private String name;

    private byte[] data = null;

    private static final int VERSION = 2;

    private Vector<String> functionNames = null;

    private Vector<Integer> functionTypes = new Vector<Integer>();

    private Vector<Long> functionLinks = new Vector<Long>();

    String fileName;

    public UserTemplate(final InputStream stream) throws IOException {
        int version = DataLoader.readInteger(stream);// Version 1
        name = DataLoader.readString(stream);
        functionNames = new Vector<String>(3);
        int l = DataLoader.readInteger(stream);
        for (int i = 0; i < l; i++) {
            functionNames.add(DataLoader.readString(stream));
        }

        if (version > 1) {
            l = DataLoader.readInteger(stream);
            for (int i = 0; i < l; i++)
                functionTypes.add(DataLoader.readInteger(stream));
            l = DataLoader.readInteger(stream);
            for (int i = 0; i < l; i++)
                functionLinks.add(DataLoader.readLong(stream));
        }

        l = DataLoader.readInteger(stream);
        data = new byte[l];
        stream.read(data);
    }

    public UserTemplate(final Function function, final DataPlugin dataPlugin,
                        final String name, final SectorRefactor refactor) {
        this(function, dataPlugin, name, refactor, null);
    }

    public UserTemplate(final Function function, final DataPlugin dataPlugin,
                        final String name, final SectorRefactor refactor,
                        List<Function> functions) {
        this.name = name;
        try {
            final ByteArrayOutputStream streamr = new ByteArrayOutputStream();
            final ObjectOutputStream stream = new ObjectOutputStream(streamr);
            final MemoryData data = new MemoryData();
            functionNames = new Vector<String>(3);
            int l = function.getChildCount();

            List<Function> fns = new ArrayList<Function>(l);
            for (int i = 0; i < l; i++) {
                final NFunction c = (NFunction) function.getChildAt(i);
                if (functions != null) {
                    if (functions.contains(c))
                        fns.add(c);
                } else
                    fns.add(c);
            }
            l = fns.size();
            DataSaver.saveInteger(stream, l);
            for (int i = 0; i < l; i++) {
                final NFunction c = (NFunction) fns.get(i);
                Object o = c.getNameObject();
                if (o instanceof DFDSName) {
                    DFDSName dfdsName = (DFDSName) o;
                    functionNames.add(dfdsName.getShortName() + "\n"
                            + dfdsName.getLongName());
                } else
                    functionNames.add(c.getName());
                functionTypes.add(c.getType());
                functionLinks.add(c.getLink());

                DataSaver.saveLong(stream, c.getElement().getId());
                DataSaver.saveFRectangle(stream, c.getBounds());
                DataSaver.saveFont(stream, c.getFont(), data);
                DataSaver.saveColor(stream, c.getBackground(), data);
                DataSaver.saveColor(stream, c.getForeground(), data);
            }

            DataSaver.saveInteger(stream, SectorRefactor.BIN_VERSION);

            l = refactor.getSectorsCount();
            List<PaintSector> paintSectors = new ArrayList<PaintSector>(l);
            for (int i = 0; i < l; i++)
                paintSectors.add(refactor.getSector(i));

            if (functions != null)
                clearSectors(paintSectors, functions, dataPlugin);

            DataSaver.saveInteger(stream, paintSectors.size());
            for (int i = 0; i < paintSectors.size(); i++) {
                final PaintSector ps = paintSectors.get(i);
                final Sector s = ps.getSector();
                DataSaver.saveLong(stream, ((NSector) s).getElementId());

                PaintSector.save(ps, data, dataPlugin.getEngine());

                final byte[] va = s.getVisualAttributes();
                DataSaver.saveBytes(stream, va);

                save(s.getSectorPointPersistents(), stream);
                save(s.getSectorProperties(), stream);

                Crosspoint c;
                if ((c = s.getStart().getCrosspoint()) != null) {
                    DataSaver.saveBoolean(stream, true);
                    DataSaver.saveLong(stream, c.getGlobalId());
                } else {
                    DataSaver.saveBoolean(stream, false);
                }
                saveBorder(s.getStart(), stream);

                if ((c = s.getEnd().getCrosspoint()) != null) {
                    DataSaver.saveBoolean(stream, true);
                    DataSaver.saveLong(stream, c.getGlobalId());
                } else {
                    DataSaver.saveBoolean(stream, false);
                }
                saveBorder(s.getEnd(), stream);
                if (s.getStream() != null && s.getStream().getAdded() != null) {
                    DataSaver.saveBoolean(stream, true);
                    Row[] added = s.getStream().getAdded();
                    DataSaver.saveInteger(stream, added.length);
                    for (Row row : added)
                        saveRow(stream, row);
                } else
                    DataSaver.saveBoolean(stream, false);
            }
            stream.close();
            this.data = streamr.toByteArray();
        } catch (final IOException e) {

        }
    }

    public static void clearSectors(List<PaintSector> paintSectors,
                                    List<Function> functions, DataPlugin dataPlugin) {
        boolean remed;
        do {
            remed = false;
            for (int i = 0; i < paintSectors.size(); i++) {
                if (remove(paintSectors, i, functions, dataPlugin)) {
                    remed = true;
                    break;
                }
            }
        } while (remed);

    }

    private static boolean remove(List<PaintSector> paintSectors, int i,
                                  List<Function> functions, DataPlugin dataPlugin) {
        PaintSector ps = paintSectors.get(i);
        if (hasFunction(functions, ((NSector) ps.getSector()).getStart()
                .getFunction())
                && hasFunction(functions, ((NSector) ps.getSector()).getEnd()
                .getFunction()))
            return false;

        paintSectors.remove(i);

        recRemove(ps, paintSectors);

        return true;
    }

    private static void recRemove(PaintSector ps, List<PaintSector> paintSectors) {
        boolean remed;
        do {
            remed = false;
            for (int i = 0; i < paintSectors.size(); i++) {
                PaintSector s = paintSectors.get(i);
                if (hasCross(s, ps.getStart())) {
                    remed = true;
                    recRemove(paintSectors.remove(i), paintSectors);
                    break;
                }
                if (hasCross(s, ps.getEnd())) {
                    remed = true;
                    recRemove(paintSectors.remove(i), paintSectors);
                    break;
                }
            }
        } while (remed);
    }

    private static boolean hasCross(PaintSector ps, Crosspoint c) {
        if (c != null) {
            if (((NCrosspoint) c).isOut(ps.getSector())
                    || (c.isIn(ps.getSector())))
                return true;
        }
        return false;
    }

    private static boolean hasFunction(List<Function> functions, Function f) {
        if (f == null)
            return true;
        return functions.contains(f);
    }

    private void save(Object obj, ObjectOutputStream stream) throws IOException {
        stream.writeObject(obj);
    }

    private void saveBorder(final SectorBorder border, final OutputStream stream)
            throws IOException {
        DataSaver.saveInteger(stream, border.getBorderType());
        DataSaver.saveInteger(stream, border.getFunctionType());
        if (border.getFunction() != null) {
            DataSaver.saveLong(stream, border.getFunction().getElement()
                    .getId());
        } else {
            DataSaver.saveLong(stream, -1l);
        }
    }

    public void saveToStream(final OutputStream stream) throws IOException {
        DataSaver.saveInteger(stream, VERSION);
        DataSaver.saveString(stream, name);
        DataSaver.saveInteger(stream, functionNames.size());
        for (final String s : functionNames)
            DataSaver.saveString(stream, s);

        DataSaver.saveInteger(stream, functionTypes.size());
        for (final Integer s : functionTypes)
            DataSaver.saveInteger(stream, s);

        DataSaver.saveInteger(stream, functionLinks.size());
        for (final Long s : functionLinks)
            DataSaver.saveLong(stream, s);

        DataSaver.saveInteger(stream, data.length);
        stream.write(data);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void createChilds(final Function function,
                             final DataPlugin dataPlugin) {
        Hashtable<Long, Long> trans = new Hashtable<Long, Long>();
        final MovingArea ma = new MovingArea(dataPlugin);
        ma.setDataPlugin(dataPlugin);
        ma.setActiveFunction(function);
        final SectorRefactor refactor = ma.getRefactor();

        try {
            final ByteArrayInputStream streamr = new ByteArrayInputStream(data);
            final ObjectInputStream stream = new ObjectInputStream(streamr);
            final MemoryData data = new MemoryData();
            int l = DataLoader.readInteger(stream);
            for (int i = 0; i < l; i++) {
                final Function c = (Function) dataPlugin.createRow(function,
                        true);
                c.setName(functionNames.get(i));
                if (i < functionTypes.size())
                    c.setType(functionTypes.get(i));
                if (i < functionLinks.size()) {
                    Row r = dataPlugin.findRowByGlobalId(functionLinks.get(i));
                    if (r != null) {
                        if (r instanceof Stream) {
                            Stream st = (Stream) r;
                            Stream s = (Stream) dataPlugin.createRow(
                                    dataPlugin.getBaseStream(), true);
                            s.setAttachedStatus(st.getAttachedStatus());
                            s.setEmptyName(st.isEmptyName());
                            s.setRows(st.getAdded());
                            c.setLink(s.getElement().getId());
                        }
                    }
                }
                final long id = DataLoader.readLong(stream);
                trans.put(id, c.getElement().getId());
                c.setBounds(DataLoader.readFRectangle(stream));
                c.setFont(DataLoader.readFont(stream, data));
                c.setBackground(DataLoader.readColor(stream, data));
                c.setForeground(DataLoader.readColor(stream, data));
            }

            final int ver = DataLoader.readInteger(stream);// Read version of
            // Paint
            // Sector format

            l = DataLoader.readInteger(stream);

            HashMap<Sector, List<SectorPointPersistent>> pointsCache = new HashMap<Sector, List<SectorPointPersistent>>();
            HashMap<Long, Long> pointIds = new HashMap<Long, Long>();
            for (int i = 0; i < l; i++) {
                final Sector s = dataPlugin.createSector();
                s.setFunction(function);
                final long id = DataLoader.readLong(stream);
                trans.put(id, ((NSector) s).getElementId());
                final byte[] va = DataLoader.readBytes(stream);
                s.setVisualAttributes(va);
                List<SectorPointPersistent> points = (List) stream.readObject();
                pointsCache.put(s, points);
                normalize(points, dataPlugin.getEngine(), pointIds);
                s.setSectorPointPersistents(points);
                s.setSectorProperties((SectorPropertiesPersistent) stream
                        .readObject());

                // final byte bs[] = DataLoader.readBytes(stream);
                // final ByteArrayInputStream ba = new ByteArrayInputStream(bs);
                final PaintSector ps = PaintSector.loadFromSector(ver, ma,
                        data, dataPlugin, s);
                refactor.addSector(ps);

                Crosspoint c;
                if (DataLoader.readBoolean(stream)) {
                    c = getCrosspoint(dataPlugin, trans, stream);

                    s.getStart().setCrosspointA(c);
                }

                loadBorder(s.getStart(), stream, trans, dataPlugin, refactor,
                        ps, true);

                s.getStart().commit();

                if (DataLoader.readBoolean(stream)) {
                    c = getCrosspoint(dataPlugin, trans, stream);

                    s.getEnd().setCrosspointA(c);
                }

                loadBorder(s.getEnd(), stream, trans, dataPlugin, refactor, ps,
                        false);
                s.getEnd().commit();

                if (DataLoader.readBoolean(stream)) {
                    int count = DataLoader.readInteger(stream);
                    List<Row> rows = new ArrayList<Row>();
                    for (int j = 0; j < count; j++) {
                        Row row = loadRow(stream, dataPlugin);
                        if (row != null)
                            rows.add(row);
                    }
                    if (rows.size() > 0) {
                        Stream stream2 = (Stream) dataPlugin.createRow(
                                dataPlugin.getBaseStream(), true);
                        stream2.setRows(rows.toArray(new Row[rows.size()]));
                        ((NSector) s).setThisStream(stream2);
                    }
                }
            }

            setPoints(pointsCache);

            refactor.saveToFunction();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRow(ObjectOutputStream stream, Row row) throws IOException {
        if (row == null) {
            DataSaver.saveString(stream, null);
            DataSaver.saveString(stream, null);
        } else {
            DataSaver.saveString(stream, row.getQualifier().getName());
            DataSaver.saveString(stream, row.getName());
        }
    }

    private Row loadRow(ObjectInputStream stream, DataPlugin dataPlugin)
            throws IOException {
        String qName = DataLoader.readString(stream);
        String eName = DataLoader.readString(stream);
        if (qName == null)
            return null;
        Qualifier q = dataPlugin.getEngine().getQualifierByName(qName);
        if (q != null) {
            Element e = dataPlugin.getEngine().getElement(eName, q.getId());
            if (e != null)
                return dataPlugin.findRowByGlobalId(e.getId());
        }
        return null;
    }

    private void setPoints(
            HashMap<Sector, List<SectorPointPersistent>> pointsCache) {
        for (Entry<Sector, List<SectorPointPersistent>> entry : pointsCache
                .entrySet())
            entry.getKey().setSectorPointPersistents(entry.getValue());
    }

    private void normalize(List<SectorPointPersistent> list, Engine engine,
                           HashMap<Long, Long> pointIds) {
        for (SectorPointPersistent spp : list) {
            Long id = pointIds.get(spp.getXOrdinateId());
            if (id == null) {
                id = IDEF0Plugin.getNextOrdinateId(engine);
                pointIds.put(spp.getXOrdinateId(), id);
            }
            spp.setXOrdinateId(id);
            id = pointIds.get(spp.getYOrdinateId());
            if (id == null) {
                id = IDEF0Plugin.getNextOrdinateId(engine);
                pointIds.put(spp.getYOrdinateId(), id);
            }
            spp.setYOrdinateId(id);
        }
    }

    private Crosspoint getCrosspoint(final DataPlugin dataPlugin,
                                     final Hashtable<Long, Long> trans, final InputStream stream)
            throws IOException {
        long id;
        Crosspoint c;
        id = DataLoader.readLong(stream);
        final Long cId = trans.get(id);
        if (cId == null) {
            c = dataPlugin.createCrosspoint();
            trans.put(id, c.getGlobalId());
        } else
            c = dataPlugin.findCrosspointByGlobalId(cId);
        return c;
    }

    private void loadBorder(final NSectorBorder border,
                            final InputStream stream, final Hashtable<Long, Long> trans,
                            final DataPlugin dataPlugin, final SectorRefactor refactor,
                            final PaintSector sector, boolean start) throws IOException {
        border.setBorderTypeA(DataLoader.readInteger(stream));
        border.setFunctionTypeA(DataLoader.readInteger(stream));
        final long id = DataLoader.readLong(stream);
        if (id > 0l) {
            final Long fId = trans.get(id);
            if (fId != null) {
                border.setFunctionA((Function) dataPlugin
                        .findRowByGlobalId(fId));
                if (border.getFunction() != null) {
                    refactor.createSectorOnIn(border.getCrosspoint(), border,
                            sector, start);
                }
            }
        }
    }

    public int getFunctionCount() {
        return functionNames.size();
    }

    public String getFunctionName(final int function) {
        return functionNames.get(function);
    }

    public void setFunctionName(final int function, final String name) {
        functionNames.set(function, name);
        dataPlugin = null;
    }

    @Override
    public String toString() {
        return getName();
    }

    public int compareTo(final UserTemplate obj) {
        return RowNameComparator.StringCollator.compare(toString(),
                obj.toString());
    }

}
