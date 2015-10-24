package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

@Table(name = "other_elements", type = TableType.ONE_TO_MANY)
public class OtherElementPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 4062386285761076535L;

    private long otherElement;

    public OtherElementPersistent() {
    }

    public OtherElementPersistent(long otherElement) {
        this.otherElement = otherElement;
    }

    /**
     * @param otherElement the otherElement to set
     */
    public void setOtherElement(long otherElement) {
        this.otherElement = otherElement;
    }

    /**
     * @return the otherElement
     */
    @com.ramussoft.common.persistent.Long(id = 2, primary = true)
    public long getOtherElement() {
        return otherElement;
    }
}
