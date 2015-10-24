package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "currency_properties")
public class CurrencyPropertyPersistent extends Persistent {

    /**
     *
     */
    private static final long serialVersionUID = 2978306465790880449L;

    private long attribute;

    private String code;

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
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the code
     */
    @Text(id = 1)
    public String getCode() {
        return code;
    }

}
