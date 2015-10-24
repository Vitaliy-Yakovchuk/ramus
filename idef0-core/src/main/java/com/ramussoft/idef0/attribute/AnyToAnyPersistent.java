package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "any_to_any_elements")
public class AnyToAnyPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 3146158149996579489L;

    private long otherElement;

    private String elementStatus;

    public AnyToAnyPersistent() {

    }

    public AnyToAnyPersistent(long otheElement) {
        this.otherElement = otheElement;
    }

    /**
     * @param otherElement the otherElement to set
     */
    public void setOtherElement(long otherElement) {
        this.otherElement = otherElement;
    }

    /**
     * @return the otherElement
     */
    @Long(id = 2, primary = true)
    public long getOtherElement() {
        return otherElement;
    }

    @Text(id = 3)
    public String getElementStatus() {
        return elementStatus;
    }

    public void setElementStatus(String elementStatus) {
        this.elementStatus = elementStatus;
    }

}
