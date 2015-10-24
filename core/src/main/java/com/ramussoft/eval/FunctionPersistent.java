package com.ramussoft.eval;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "functions")
public class FunctionPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -6712618085025626079L;

    private String function;

    private long qualifierAttributeId;

    private long qualifierTableAttributeId = -1l;

    private int autochange;

    public FunctionPersistent() {
    }

    public FunctionPersistent(String function, long qualifierAttributeId,
                              long qualifierTableAttributeId, int autochange) {
        this.function = function;
        this.qualifierAttributeId = qualifierAttributeId;
        this.qualifierTableAttributeId = qualifierTableAttributeId;
        this.autochange = autochange;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(String function) {
        this.function = function;
    }

    /**
     * @return the function
     */
    @Text(id = 2)
    public String getFunction() {
        return function;
    }

    /**
     * @param qualifierAttributeId the qualifierAttributeId to set
     */
    public void setQualifierAttributeId(long qualifierAttributeId) {
        this.qualifierAttributeId = qualifierAttributeId;
    }

    /**
     * @return the qualifierAttributeId
     */

    @Long(id = 3, primary = true)
    public long getQualifierAttributeId() {
        return qualifierAttributeId;
    }

    /**
     * @param autochange the autochange to set
     */
    public void setAutochange(int autochange) {
        this.autochange = autochange;
    }

    /**
     * @return the autochange
     */
    @Integer(id = 4)
    public int getAutochange() {
        return autochange;
    }

    /**
     * @param qualifierTableAttributeId the qualifierTableAttributeId to set
     */
    public void setQualifierTableAttributeId(long qualifierTableAttributeId) {
        this.qualifierTableAttributeId = qualifierTableAttributeId;
    }

    /**
     * @return the qualifierTableAttributeId
     */
    @Long(id = 5, primary = true, setDefaultValue = true)
    public long getQualifierTableAttributeId() {
        return qualifierTableAttributeId;
    }

}
