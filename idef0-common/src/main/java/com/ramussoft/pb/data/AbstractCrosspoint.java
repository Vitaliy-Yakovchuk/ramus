/**
 *
 */
package com.ramussoft.pb.data;

import java.util.Vector;

import com.ramussoft.idef0.attribute.SectorBorderPersistent;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.negine.NSector;

/**
 * @author ZDD
 */
public abstract class AbstractCrosspoint implements Crosspoint {

    protected long id;

    /**
     * Масив секторів які входять в вузол.
     */

    protected Sector[] ins = new Sector[0];

    /**
     * Масив секторів, які виходять з вузла.
     */

    protected Sector[] outs = new Sector[0];

    protected AbstractDataPlugin dataPlugin;

    public AbstractCrosspoint(final AbstractDataPlugin dataPlugin) {
        this.dataPlugin = dataPlugin;
    }

    /**
     * Метод перевіряє чи може бути під’єднаний до точки сектор, який виходить з
     * неї.
     *
     * @return <code>true</code> доданий сектор може виходити з точки <br>
     * <code>false</code> доданий сектор не може виходити з точки.
     */

    public boolean isCanAddOut() {
        if (getIns().length == 1 && getOuts().length == 1)
            return false;
        return getType() == TYPE_ONE_IN;
    }

    /**
     * Метод перевіряє чи може бути під’єднаний до точки сектор, який входить з
     * неї.
     *
     * @return <code>true</code> доданий сектор може входити в точку <br>
     * <code>false</code> доданий сектор не може входити в точку.
     */

    public boolean isCanAddIn() {
        if (getIns().length == 1 && getOuts().length == 1)
            return false;
        return getType() == TYPE_ONE_OUT;
    }

    public boolean isDLevel() {
        Function f = null;
        final Sector[] ins = getIns();
        final Sector[] outs = getOuts();
        if (ins.length == 0 || outs.length == 0)
            return true;
        for (int i = 0; i < ins.length; i++) {
            if (ins[i].getFunction() == null)
                return false;
            if (!ins[i].getFunction().equals(f)) {
                if (f == null)
                    f = ins[i].getFunction();
                else
                    return true;
            }
        }

        for (int i = 0; i < outs.length; i++) {
            if (outs[i].getFunction() == null)
                return false;
            if (!outs[i].getFunction().equals(f)) {
                if (f == null)
                    f = outs[i].getFunction();
                else
                    return true;
            }
        }
        return false;
    }

    public boolean isOne(final Sector sector) {
        if (isIn(sector)) {
            if (getIns().length == 1)
                return true;
        } else {
            if (getOuts().length == 1)
                return true;
        }
        return false;
    }

    public boolean isRemoveable() {
        try {
            final Sector[] ins = getIns();
            final Sector[] outs = getOuts();
            return ins.length == 1 && outs.length == 1
                    && ins[0].getFunction().equals(outs[0].getFunction());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isChild(final Sector sector) {
        if (isIn(sector))
            return getIns().length > 1;
        else
            return getOuts().length > 1;
    }

    private void copyVisualData(final Sector from, final Sector to) {
        final byte[] bs = from.getVisualAttributes();
        if (bs.length > 0)
            to.setVisualAttributes(bs);
    }

    protected void copyVisulaToNewSector(final Sector sector) {
        Sector[] ins;
        Sector[] outs;
        if ((outs = getOuts()).length > 0)
            copyVisualData(outs[0], sector);
        else if ((ins = getIns()).length > 0)
            copyVisualData(ins[0], sector);
    }

    public void getSectors(final Vector<Sector> v) {
        final Sector[] ins = getIns();
        final Sector[] outs = getOuts();
        for (final Sector element : ins) {
            if (v.indexOf(element) < 0)
                v.add(element);
        }
        for (final Sector element : outs) {
            if (v.indexOf(element) < 0)
                v.add(element);
        }
    }

    protected boolean isPresent(final Sector[] sectors, final Sector sector) {
        for (final Sector element : sectors)
            if (element.equals(sector))
                return true;
        return false;
    }

    public void removeData(final Sector sector) {
        if (isPresent(ins, sector))
            ins = removeElement(ins, sector);
        else if (isPresent(outs, sector))
            outs = removeElement(outs, sector);
    }

    public void remove1(final Sector sector) {
        // synchronized (dataPlugin) {
        final boolean b = isDLevel();
        removeData(sector);
        final Sector[] ins = getIns();
        final Sector[] outs = getOuts();
        if (b) {
            if (ins.length == 0 && outs.length == 1) {
                if (outs[0].getEnd().getCrosspoint() == null)
                    if (outs[0].getStart().getBorderType() >= 0)
                        outs[0].remove();
                    else {
                        /*Function function = outs[0].getStart().getFunction();
                        if (function != null
								&& function.getType() == Function.TYPE_DFDS_ROLE) {
							((NFunction) function).remove();
							outs[0].remove();
						}*/
                    }
            } else if (ins.length == 1 && outs.length == 0) {
                if (ins[0].getStart().getCrosspoint() == null)
                    if (ins[0].getEnd().getBorderType() >= 0)
                        ins[0].remove();
                    else {
                        Function function = ins[0].getEnd().getFunction();
                        if (function != null
                                && function.getType() == Function.TYPE_DFDS_ROLE) {
                            ((NFunction) function).remove();
                            ins[0].remove();
                        }
                    }
            }
        }
        if (getIns().length == 0 && getOuts().length == 0) {
            dataPlugin.removeCrosspoint(this);
        }
        // }
    }

    public int getType() {
        if (ins.length == 1)
            return TYPE_ONE_IN;
        else
            return TYPE_ONE_OUT;
    }

    /**
     * Видаляє сектор з масиву секторів.
     *
     * @param sectors Масив секторів.
     * @param sector  Елемент, який необхідно видалити.
     * @return Новий масив, в якому не має елемента sector.
     */
    private Sector[] removeElement(final Sector[] sectors, final Sector sector) {
        if (sectors.length == 0)
            return sectors;
        if (!isPresent(sectors, sector))
            return sectors;
        final Sector[] res = new Sector[sectors.length - 1];
        for (int i = 0; i < sectors.length; i++)
            if (sectors[i].equals(sector)) {
                for (int j = i + 1; j < sectors.length; j++)
                    res[j - 1] = sectors[j];
                break;
            } else
                res[i] = sectors[i];
        return res;
    }

    public Sector[] getIns() {
        synchronized (dataPlugin) {
            return ins;
        }
    }

    public Sector[] getOuts() {
        synchronized (dataPlugin) {
            return outs;
        }
    }

    /**
     * Метод додає сектор в кінець масива секторів і повертає новостворений
     * масив секторів. Перевірка на те чи сектор вже присутній в масиві не
     * робиться.
     *
     * @param array  Вхідний масив секторів.
     * @param sector Сектор, який буде доданий до масиву.
     * @return Вихідний масив секторів, який включає всі сектори вхідного масиву
     * і додає елемент в кінець.
     */

    protected Sector[] addSector(final Sector[] array, final Sector sector) {
        for (Sector s : array)
            if (((NSector) s).getElementId() == ((NSector) sector)
                    .getElementId())
                return array;

        final Sector[] tmp = new Sector[array.length + 1];
        for (int i = 0; i < array.length; i++)
            tmp[i] = array[i];
        tmp[array.length] = sector;
        return tmp;
    }

    public void addIn(final Sector sector) {
        ins = addSector(ins, sector);
    }

    public void addOut(final Sector sector) {
        outs = addSector(outs, sector);
    }

    public long getGlobalId() {
        return id;
    }

    /**
     * Перевиряє, чи являється сектор входом для даного вузла.
     *
     * @param sector Сектор на перевірку.
     * @return true, якщо сектор являється входом, false, якщо сектор не
     * являється входом.
     */
    public boolean isIn(final Sector sector) {
        for (final Sector element : ins)
            if (element.equals(sector))
                return true;
        return false;
    }

    /**
     * Перевиряє, чи являється сектор виходом для даного вузла.
     *
     * @param sector Сектор на перевірку.
     * @return true, якщо сектор являється виходом, false, якщо сектор не
     * являється входом.
     */
    public boolean isOut(final Sector sector) {
        for (final Sector element : outs)
            if (element.equals(sector))
                return true;
        return false;
    }

    public int getTunnelType(SectorBorderPersistent sbp) {
        if (isDLevel()) {
            if (ins.length > 0 && outs.length > 0)
                return TUNNEL_NONE;
            return sbp.getTunnelSoft();
        }
        return TUNNEL_NONE;
    }

    public Sector[] getOppozite(final Sector sector) {
        if (isIn(sector))
            return outs;
        else
            for (final Sector element : outs) {
                if (element.equals(sector))
                    return ins;
            }
        return new Sector[0];
    }

    public boolean isTunnelSoft(SectorBorderPersistent sbp) {
        if (ins.length > 0 && outs.length > 0)
            return false;
        return sbp.getTunnelSoft() == TUNNEL_SOFT
                || sbp.getTunnelSoft() == TUNNEL_SIMPLE_SOFT;
    }

    @Override
    public String toString() {
        return "ID: " + getGlobalId() + " INS COUNT: " + getIns().length
                + " OUTS COUNT: " + getOuts().length;
    }

    public boolean isOneInOut() {
        return ((ins.length == 1) && (outs.length == 1));
    }
}
