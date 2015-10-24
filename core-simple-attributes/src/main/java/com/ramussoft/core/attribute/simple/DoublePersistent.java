package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Double;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

/**
 * Persistent object for long type.
 *
 * @author zdd
 */

@Table(name = "doubles", type = TableType.ONE_TO_ONE)
public class DoublePersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -6981569543579936955L;
    private double value;

    public DoublePersistent(double value) {
        this.value = value;
    }

    public DoublePersistent() {
    }

    @Double(id = 2)
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
