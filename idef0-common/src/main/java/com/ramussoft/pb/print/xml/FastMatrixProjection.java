package com.ramussoft.pb.print.xml;

import java.util.Arrays;
import java.util.Vector;

import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.data.projections.AbstractMatrixProjection;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.pb.types.GlobalId;

public class FastMatrixProjection extends AbstractMatrixProjection {

    protected int type;

    protected int functionType;

    private DataPlugin dataPlugin;

    public int getType() {
        return type;
    }

    public int getFunctionType() {
        return functionType;
    }

    private class GlobalIdVector implements Comparable {

        public GlobalId globalId;

        public Vector vector;

        public GlobalIdVector(final GlobalId globalId, final Vector vector) {
            super();
            this.globalId = globalId;
            this.vector = vector;
        }

        ;

        public GlobalIdVector() {
            super();
        }

        public int compareTo(final Object arg0) {
            final GlobalIdVector vector = (GlobalIdVector) arg0;
            return globalId.compareTo(vector.globalId);
        }
    }

    ;

    private static final int SMALL_BUFF_LENGTH = 20;

    private class GlobalIdTable {

        private final Vector data = new Vector();

        private final GlobalIdVector cmp = new GlobalIdVector();

        private Object[] buff = null;

        private final Vector vectorBuff = new Vector();

        public void put(final GlobalId key, final Vector object) {
            final GlobalIdVector g = new GlobalIdVector(key, object);
            if (vectorBuff.size() < SMALL_BUFF_LENGTH) {
                vectorBuff.add(g);
            } else {
                data.add(g);
                buff = null;
            }
        }

        private void sort() {
            for (int i = 0; i < vectorBuff.size(); i++)
                data.add(vectorBuff.get(i));
            buff = data.toArray();
            Arrays.sort(buff);
            vectorBuff.clear();
        }

        public Vector get(final GlobalId key) {
            if (buff == null)
                sort();
            cmp.globalId = key;
            for (int i = 0; i < vectorBuff.size(); i++) {
                final GlobalIdVector g = (GlobalIdVector) vectorBuff.get(i);
                if (g.globalId.equals(key))
                    return g.vector;
            }
            final int r = Arrays.binarySearch(buff, cmp);
            if (r < 0 || r >= buff.length) {
                final Vector res = new Vector();
                put(key, res);
                return res;
            }
            return ((GlobalIdVector) buff[r]).vector;
        }

    }

    ;

    private final GlobalIdTable lefts = new GlobalIdTable();

    private final GlobalIdTable rights = new GlobalIdTable();

    public FastMatrixProjection(DataPlugin dataPlugin, final int type,
                                final int functionType) {
        this(dataPlugin, type, functionType, dataPlugin.getBaseFunction());
    }

    public FastMatrixProjection(DataPlugin dataPlugin, final int type,
                                final int functionType, Function baseFunction) {
        this.type = type;
        this.functionType = functionType;
        this.dataPlugin = dataPlugin;
        initArrays(baseFunction);
    }

    private boolean isOk(final Function function) {
        if (functionType == -1)
            return true;
        return functionType == function.getType();
    }

    private void add(final Vector v, final Object o) {
        v.add(o);
    }

    public class SectorFunction {
        Sector sector;
        Function function;

        public SectorFunction(final Sector sector, final Function function) {
            this.sector = sector;
            this.function = function;
        }
    }

    private void initArrays(final Function parent) {
        final Vector sectors = parent.getSectors();
        final Vector childs = dataPlugin.getChilds(parent, true);
        boolean dfd = parent.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFD;
        Function child;
        Sector sector;
        Stream stream;
        Vector v;
        for (int i = 0; i < childs.size(); i++) {
            child = (Function) childs.get(i);
            v = lefts.get(child.getGlobalId());
            if (isOk(child))
                for (int j = 0; j < sectors.size(); j++) {
                    sector = (Sector) sectors.get(j);
                    stream = sector.getStream();
                    if (stream != null) {
                        if (dfd) {
                            if (type == MovingPanel.RIGHT) {
                                if (isEqualsStart(sector, child)) {
                                    add(
                                            child,
                                            sector,
                                            stream,
                                            v,
                                            sector.getStart().getTunnelSoft() == Crosspoint.TUNNEL_SOFT);
                                }
                            } else {
                                if (type == MovingPanel.LEFT)
                                    if (isEqualsEnd(sector, child, type)) {
                                        add(
                                                child,
                                                sector,
                                                stream,
                                                v,
                                                sector.getEnd().getTunnelSoft() == Crosspoint.TUNNEL_SOFT);
                                    }
                            }
                        } else {
                            if (type == MovingPanel.RIGHT) {
                                if (isEqualsStart(sector, child)) {
                                    add(
                                            child,
                                            sector,
                                            stream,
                                            v,
                                            sector.getStart().getTunnelSoft() == Crosspoint.TUNNEL_SOFT);
                                }
                            } else if (isEqualsEnd(sector, child, type)) {
                                add(
                                        child,
                                        sector,
                                        stream,
                                        v,
                                        sector.getEnd().getTunnelSoft() == Crosspoint.TUNNEL_SOFT);
                            }
                        }
                    }
                }
            initArrays(child);
        }
    }

    private void add(final Function child, final Sector sector,
                     final Stream stream, final Vector v, final boolean soft) {
        add(v, sector);
        add(rights.get(stream.getGlobalId()), new SectorFunction(sector, child));
        if (soft) {
            final Vector<Row> childs = dataPlugin.getChilds(child, true);
            for (int i = 0; i < childs.size(); i++) {
                final Function f = (Function) childs.get(i);
                add(f, sector, stream, lefts.get(f.getGlobalId()), soft);
            }
        }
    }

    private boolean isEqualsStart(final Sector sector, final Function child) {
        return child.equals(sector.getStart().getFunction());
    }

    protected boolean isEqualsEnd(final Sector s, final Function f,
                                  final int type) {
        final SectorBorder end = s.getEnd();
        if (f.equals(end.getFunction())) {
            if (((Function) f.getParentRow()).getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFD)
                return true;
            final int ft = end.getFunctionType();
            if (ft == type)
                return true;
            switch (type) {
                case MovingPanel.LEFT_TOP:
                    return ft == MovingPanel.LEFT || ft == MovingPanel.TOP;
                case MovingPanel.LEFT_BOTTOM:
                    return ft == MovingPanel.LEFT || ft == MovingPanel.BOTTOM;
                case MovingPanel.LEFT_TOP_BOTTOM:
                    return ft == MovingPanel.LEFT || ft == MovingPanel.TOP
                            || ft == MovingPanel.BOTTOM;
            }
        }
        return false;
    }

    public Vector getLeft(final Row row) {
        return lefts.get(row.getGlobalId());
    }

    public Vector getRight(final Row row) {
        return rights.get(row.getGlobalId());
    }

    public Row getRow1() {
        return dataPlugin.getBaseFunction();
    }

    public Row getRow2() {
        return dataPlugin.getBaseStream();
    }

    public boolean isHere(final Row row) {
        if (row instanceof Function)
            return !getLeft(row).isEmpty();
        return !getRight(row).isEmpty();
    }

}
