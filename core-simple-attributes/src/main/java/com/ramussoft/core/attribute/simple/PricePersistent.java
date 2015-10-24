package com.ramussoft.core.attribute.simple;

import java.sql.Timestamp;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Date;
import com.ramussoft.common.persistent.Double;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

/**
 * Persistent object for long type.
 *
 * @author zdd
 */

@Table(name = "prices", type = TableType.ONE_TO_MANY)
public class PricePersistent extends AbstractPersistent implements
        Comparable<PricePersistent> {

    /**
     *
     */
    private static final long serialVersionUID = -6981569543579936955L;
    private java.lang.Double value;
    private Timestamp startDate;
    private int position;

    public PricePersistent() {
    }

    @Double(id = 2)
    public java.lang.Double getValue() {
        return value;
    }

    public void setValue(java.lang.Double value) {
        this.value = value;
    }

    /**
     * @return the startDate
     */
    @Date(id = 3)
    public Timestamp getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    @Override
    public int compareTo(PricePersistent o) {
        if (o.startDate == null && startDate == null)
            return 0;
        if (startDate == null)
            return -1;
        if (o.startDate == null)
            return 1;
        return startDate.compareTo(o.startDate);
    }

    /**
     * @return the position
     */
    @Integer(id = 4, primary = true)
    public int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

}
