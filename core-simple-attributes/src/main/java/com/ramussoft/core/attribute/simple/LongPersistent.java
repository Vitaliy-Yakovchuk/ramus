package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

/**
 * Persistent object for long type.
 *
 * @author zdd
 */

@Table(name = "longs", type = TableType.ONE_TO_ONE)
public class LongPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 362906835285599732L;
    private long value;

    public LongPersistent(java.lang.Long value) {
        this.value = value;
    }

    public LongPersistent() {
    }

    @Long(id = 2)
    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

}
