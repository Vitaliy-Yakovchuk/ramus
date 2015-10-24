package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

@Table(name = "other_element_properties", type = TableType.ONE_TO_MANY)
public class OtherElementPropertyPersistent extends Persistent {
    /**
     *
     */
    private static final long serialVersionUID = 4062386285761076535L;

    private long attribute;

    private long qualifier = -1;

    private long qualifierAttribute;

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(long attribute) {
        this.attribute = attribute;
    }

    /**
     * @return the attribute
     */
    @Attribute(id = 0, autoset = true, primary = true)
    public long getAttribute() {
        return attribute;
    }

    /**
     * @param qualifier the qualifier to set
     */
    public void setQualifier(long qualifier) {
        this.qualifier = qualifier;
    }

    /**
     * @return the qualifier
     */
    @Long(id = 1)
    public long getQualifier() {
        return qualifier;
    }

    /**
     * @param qualfierAttribute the qualfierAttribute to set
     */
    public void setQualifierAttribute(long qualifierAttribute) {
        this.qualifierAttribute = qualifierAttribute;
    }

    /**
     * @return the qualfierAttribute
     */
    @Long(id = 2)
    public long getQualifierAttribute() {
        return qualifierAttribute;
    }

}
