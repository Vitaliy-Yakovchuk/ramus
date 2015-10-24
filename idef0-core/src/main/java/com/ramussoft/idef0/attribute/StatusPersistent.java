package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "statuses")
public class StatusPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -4232778429114769568L;

    private int type;

    private String otherName;

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    @Integer(id = 2)
    public int getType() {
        return type;
    }

    /**
     * @param otherName the otherReason to set
     */
    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    /**
     * @return the otherReason
     */
    @Text(id = 3)
    public String getOtherName() {
        return otherName;
    }
}
