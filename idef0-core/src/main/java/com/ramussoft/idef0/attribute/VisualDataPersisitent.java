package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Binary;
import com.ramussoft.common.persistent.Table;

@Table(name = "visual_datas")
public class VisualDataPersisitent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -7670962008293190624L;

    private byte[] data;

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the data
     */
    @Binary(id = 2)
    public byte[] getData() {
        return data;
    }
}
