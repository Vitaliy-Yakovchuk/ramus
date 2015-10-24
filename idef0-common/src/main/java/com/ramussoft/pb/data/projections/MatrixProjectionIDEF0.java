/*
 * Created on 11/8/2005
 */
package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.idef.visual.MovingPanel;

/**
 * Клас - реалізує інтерфейс матричної проекції класифікатора потоків і
 * класифікатора робіт.
 *
 * @author ZDD
 */
public class MatrixProjectionIDEF0 extends AbstractMatrixProjection {

    private int type = -1;

    private int functionType = -1;

    public void setType(final int type) {
        this.type = type;
    }

    public MatrixProjectionIDEF0(final int type) {
        super();
        setType(type);
    }

    public MatrixProjectionIDEF0(final int type, final int functionType) {
        super();
        setType(type);
        this.functionType = functionType;
    }

    /**
     * Метод повертає вектор елементів класифікатора робіт, які пов’язані з
     * відповідним елементом класифікатора потоків.
     */

    public Vector<Row> getRight(final Row row) {
        final Vector<Row> res = new Vector<Row>();

        final Object[] functions = Main.dataPlugin.getRecChilds(
                Main.dataPlugin.getBaseFunction(), true).toArray();

        Function f;
        Function pF;

        final Vector<Function> softs = new Vector<Function>();

        if (functionType == -1)
            for (final Object element : functions) {
                boolean add = true;
                f = (Function) element;
                for (int j = 0; j < softs.size(); j++)
                    if (Main.dataPlugin.isParent(f, softs.get(j))) {
                        res.add(f);
                        add = false;
                        break;
                    }
                if (add) {
                    pF = (Function) f.getParentRow();
                    final Vector v = pF.getSectors();
                    for (int j = 0; j < v.size(); j++) {
                        final Sector sector = (Sector) v.get(j);
                        if (type == MovingPanel.RIGHT) {
                            if (f.equals(sector.getStart().getFunction())
                                    && row.equals(sector.getStream())) {
                                res.add(f);
                                if (sector.getStart().getTunnelSoft() == Crosspoint.TUNNEL_SOFT)
                                    softs.add(f);
                            }
                        } else {
                            if (sector.getEnd().getFunctionType() == type
                                    && f
                                    .equals(sector.getStart()
                                            .getFunction())
                                    && row.equals(sector.getStream())) {
                                res.add(f);
                                if (sector.getEnd().getTunnelSoft() == Crosspoint.TUNNEL_SOFT)
                                    softs.add(f);
                            }
                        }
                    }
                }
            }
        else
            for (final Object element : functions) {
                f = (Function) element;

                if (f.getType() == functionType) {

                    boolean add = true;

                    for (int j = 0; j < softs.size(); j++)
                        if (Main.dataPlugin.isParent(f, softs.get(j))) {
                            res.add(f);
                            add = false;
                            break;
                        }
                    if (add) {
                        pF = (Function) f.getParentRow();
                        final Vector v = pF.getSectors();
                        for (int j = 0; j < v.size(); j++) {
                            final Sector sector = (Sector) v.get(j);
                            if (type == MovingPanel.RIGHT) {
                                if (f.equals(sector.getStart().getFunction())
                                        && row.equals(sector.getStream())) {
                                    res.add(f);
                                    if (sector.getStart().getTunnelSoft() == Crosspoint.TUNNEL_SOFT)
                                        softs.add(f);
                                }
                            } else {
                                if (sector.getEnd().getFunctionType() == type
                                        && f.equals(sector.getStart()
                                        .getFunction())
                                        && row.equals(sector.getStream())) {
                                    res.add(f);
                                    if (sector.getEnd().getTunnelSoft() == Crosspoint.TUNNEL_SOFT)
                                        softs.add(f);
                                }
                            }
                        }
                    }
                }
            }
        return res;
    }

    /**
     * Метод повертає вектор елементів класифікатора потоків, які пов’язані з
     * відповідним елементом класифікатора робіт.
     */

    public Vector<Row> getLeft(final Row row) {
        Function parent;
        if ((parent = (Function) row.getParentRow()) == null)
            return new Vector<Row>();
        final Vector<Row> res = new Vector<Row>();
        final Function f = (Function) row;
        final Vector sectors = parent.getSectors();
        Sector sector;
        Stream stream;
        final int l = sectors.size();
        for (int i = 0; i < l; i++) {
            sector = (Sector) sectors.get(i);
            if ((stream = sector.getStream()) != null) {
                if (type == MovingPanel.RIGHT) {
                    if (f.equals(sector.getStart().getFunction()))
                        res.add(stream);
                } else {
                    if (f.equals(sector.getEnd().getFunction())
                            && sector.getEnd().getFunctionType() == type)
                        res.add(stream);
                }

            }
        }
        return res;
    }

    /**
     * @see com.ramussoft.pb.MatrixProjection#getRow1()
     */
    public Row getRow1() {
        return Main.dataPlugin.getBaseFunction();
    }

    /**
     * @see com.ramussoft.pb.MatrixProjection#getRow2()
     */
    public Row getRow2() {
        return Main.dataPlugin.getBaseStream();
    }

    /**
     * @see com.ramussoft.pb.MatrixProjection#isHere(com.ramussoft.pb.Row)
     */
    public boolean isHere(final Row row) {
        if (row.getClass() == Function.class)
            return !getLeft(row).isEmpty();
        return !getRight(row).isEmpty();
    }

    /**
     * @return
     */
    public int getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getName()
     */
    @Override
    public String getName() {
        return ResourceLoader.getString("idf0_matrix");
    }

}
