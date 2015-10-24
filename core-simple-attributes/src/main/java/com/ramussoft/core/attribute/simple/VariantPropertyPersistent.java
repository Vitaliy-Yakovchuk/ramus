package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;
import com.ramussoft.common.persistent.Text;

@Table(name = "variant_properties", type = TableType.ONE_TO_MANY)
public class VariantPropertyPersistent extends Persistent implements Comparable<VariantPropertyPersistent> {

    /**
     *
     */
    private static final long serialVersionUID = 4062386285761076535L;

    private long attribute;

    private long variantId;

    private String value;

    private int position;

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
     * @param variantId the variantId to set
     */
    public void setVariantId(long variantId) {
        this.variantId = variantId;
    }

    /**
     * @return the variantId
     */
    @Long(id = 1, primary = true)
    public long getVariantId() {
        return variantId;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    @Text(id = 2)
    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(VariantPropertyPersistent o) {
        if (position < o.position)
            return -1;
        if (position > o.position)
            return 1;
        return 0;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(java.lang.Integer position) {
        if (position == null)
            this.position = 0;
        else
            this.position = position;
    }

    /**
     * @return the position
     */
    @Integer(id = 3)
    public java.lang.Integer getPosition() {
        return position;
    }

}
