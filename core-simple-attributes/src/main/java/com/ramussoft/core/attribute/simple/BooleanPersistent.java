package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

@Table(name = "booleans", type = TableType.ONE_TO_ONE)
public class BooleanPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 7964118322770010056L;

    private int value;

    public BooleanPersistent(int value) {
        this.value = value;
    }

    public BooleanPersistent() {
    }

    @com.ramussoft.common.persistent.Integer(id = 2)
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
