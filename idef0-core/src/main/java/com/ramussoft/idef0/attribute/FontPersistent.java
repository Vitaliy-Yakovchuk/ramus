package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "fonts")
public class FontPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -4961644603714678509L;

    private String name;

    private int style;

    private int size;

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
     * @param style the type to set
     */
    public void setStyle(int style) {
        this.style = style;
    }

    /**
     * @return the type
     */
    @Integer(id = 3)
    public int getStyle() {
        return style;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return the size
     */
    @Integer(id = 4)
    public int getSize() {
        return size;
    }
}
