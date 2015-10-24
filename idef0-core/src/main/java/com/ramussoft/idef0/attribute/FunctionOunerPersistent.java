package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Table;

@Table(name = "function_ouners")
public class FunctionOunerPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -4297845034379512369L;

    private long ounerId;

    /**
     * @param ounerId the ounerId to set
     */
    public void setOunerId(long ounerId) {
        this.ounerId = ounerId;
    }

    /**
     * @return the ounerId
     */
    @Long(id = 2)
    public long getOunerId() {
        return ounerId;
    }

}
