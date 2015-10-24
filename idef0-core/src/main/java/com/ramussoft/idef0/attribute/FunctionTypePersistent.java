package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;

@Table(name = "function_types")
public class FunctionTypePersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 3757077084309986437L;

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
