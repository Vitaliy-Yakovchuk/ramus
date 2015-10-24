package com.ramussoft.core.attribute.simple;

import java.sql.Timestamp;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

/**
 * Persistent object for string (text) type.
 *
 * @author zdd
 */

@Table(name = "dates", type = TableType.ONE_TO_ONE)
public class DatePersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -2271188469405481682L;
    private Timestamp value;

    public DatePersistent(Timestamp value) {
        this.value = value;
    }

    public DatePersistent() {
    }

    @com.ramussoft.common.persistent.Date(id = 2)
    public Timestamp getValue() {
        return value;
    }

    public void setValue(Timestamp value) {
        this.value = value;
    }

}
