package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;
import com.ramussoft.common.persistent.Text;

@Table(name = "table_column_groups", type = TableType.ONE_TO_MANY)
public class TableGroupablePropertyPersistent extends Persistent {
    /**
     *
     */
    private static final long serialVersionUID = -1838770905291070116L;

    private long attribute;

    private long otherAttribute;

    private String name;

    private String subName;

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
     * @param otherAttribute the otherAttribute to set
     */
    public void setOtherAttribute(long otherAttribute) {
        this.otherAttribute = otherAttribute;
    }

    /**
     * @return the otherAttribute
     */
    @Attribute(id = 1, autoset = false, primary = true)
    public long getOtherAttribute() {
        return otherAttribute;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    @Text(id = 2)
    public String getName() {
        return name;
    }

    /**
     * @param subName the subName to set
     */
    public void setSubName(String subName) {
        this.subName = subName;
    }

    /**
     * Reserved just in case
     *
     * @return the subName
     */
    @Text(id = 3)
    public String getSubName() {
        return subName;
    }

}
