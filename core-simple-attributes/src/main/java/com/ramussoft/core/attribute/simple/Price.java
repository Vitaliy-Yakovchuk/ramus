package com.ramussoft.core.attribute.simple;

import java.io.Serializable;
import java.util.List;

public class Price implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3507118511928251185L;

    public Price() {
    }

    public Price(List<PricePersistent> data) {
        this.data = data.toArray(new PricePersistent[data.size()]);
    }

    private PricePersistent[] data;

    /**
     * @return the data
     */
    public PricePersistent[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(PricePersistent[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "";
    }

}
