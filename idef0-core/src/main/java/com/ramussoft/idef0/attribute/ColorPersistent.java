package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;

@Table(name = "colors")
public class ColorPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -3460278775582968657L;

    private int color;

    /**
     * @param color the color to set
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * @return the color
     */
    @Integer(id = 2)
    public int getColor() {
        return color;
    }
}
