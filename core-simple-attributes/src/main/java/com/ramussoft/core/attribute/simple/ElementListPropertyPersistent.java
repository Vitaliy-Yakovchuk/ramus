package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Qualifier;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;
import com.ramussoft.common.persistent.Text;

/**
 * Persistent object for string (text) type.
 *
 * @author zdd
 */

@Table(name = "element_list_properties", type = TableType.ONE_TO_MANY)
public class ElementListPropertyPersistent extends Persistent {
    /**
     *
     */
    private static final long serialVersionUID = -2271188469405481682L;

    private long attribute = -1;

    private long qualifier1 = -1;

    private long qualifier2 = -1;

    private String connectionTypes;

    public void setAttributeId(long attribute) {
        this.attribute = attribute;
    }

    @Attribute(id = 0, autoset = true, primary = true)
    public long getAttributeId() {
        return attribute;
    }

    /**
     * @param qualifier1 the qualifier1 to set
     */
    public void setQualifier1(long qualifier1) {
        this.qualifier1 = qualifier1;
    }

    /**
     * @return the qualifier1
     */
    @Qualifier(id = 1)
    public long getQualifier1() {
        return qualifier1;
    }

    /**
     * @param qualifier2 the qualifier2 to set
     */
    public void setQualifier2(long qualifier2) {
        this.qualifier2 = qualifier2;
    }

    /**
     * @return the qualifier2
     */
    @Qualifier(id = 2)
    public long getQualifier2() {
        return qualifier2;
    }

    @Text(id = 3)
    public String getConnectionTypes() {
        return connectionTypes;
    }

    public void setConnectionTypes(String connectionTypes) {
        this.connectionTypes = connectionTypes;
    }

}
