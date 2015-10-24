package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Double;
import com.ramussoft.common.persistent.Table;

@Table(name = "currencies")
public class CurrencyPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 5955298446236277775L;

    private double value;

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    @Double(id = 2)
    public double getValue() {
        return value;
    }

}
