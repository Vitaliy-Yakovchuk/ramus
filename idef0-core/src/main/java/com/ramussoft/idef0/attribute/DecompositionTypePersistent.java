package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;

@Table(name = "decomposition_types")
public class DecompositionTypePersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -5952211405313900151L;

    private int type;

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    @Integer(id = 2)
    public int getType() {
        return type;
    }

}
