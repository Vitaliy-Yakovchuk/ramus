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

@Table(name = "variants", type = TableType.ONE_TO_ONE)
public class VariantPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 362906835285599732L;
    private long variantId;

    public VariantPersistent(java.lang.Long variantId) {
        this.variantId = variantId;
    }

    public VariantPersistent() {
    }

    @Long(id = 2)
    public long getVariantId() {
        return variantId;
    }

    public void setVariantId(long variantId) {
        this.variantId = variantId;
    }

}
