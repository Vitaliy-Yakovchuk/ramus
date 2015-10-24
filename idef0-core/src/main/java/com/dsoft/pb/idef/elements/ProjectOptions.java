/*
 * Created on 13/8/2005
 */
package com.dsoft.pb.idef.elements;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.dsoft.pb.idef.elements.Readed;
import com.dsoft.pb.idef.elements.ReadedModel;
import com.ramussoft.idef0.attribute.IDEF0ModelPreferencesPersistent;

/**
 * @author ZDD
 */
public class ProjectOptions implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2640407071683859566L;

    private final ReadedModel readedModel = new ReadedModel();

    private IDEF0ModelPreferencesPersistent deligate = new IDEF0ModelPreferencesPersistent();

    public ProjectOptions() {
    }

    public ProjectOptions(IDEF0ModelPreferencesPersistent deligate) {
        this.deligate = deligate;
    }

    /**
     * @param usedAt The usedAt to set.
     */
    public void setUsedAt(final String usedAt) {
        deligate.setUsedAt(usedAt);
    }

    /**
     * @return Returns the usedAt.
     */
    public String getUsedAt() {
        return deligate.getUsedAt();
    }

    /**
     * @param changeDate The changeDate to set.
     */
    public void setChangeDate(final Date changeDate) {
        deligate.setChangeDate(new Timestamp(changeDate.getTime()));
    }

    /**
     * @return Returns the projectAutor.
     */
    public String getProjectAutor() {
        return deligate.getProjectAutor();
    }

    /**
     * @return Returns the projectName.
     */
    public String getProjectName() {
        return deligate.getProjectName();
    }

    /**
     * @param projectAutor The projectAutor to set.
     */
    public void setProjectAutor(final String projectAutor) {
        deligate.setProjectAutor(projectAutor);
    }

    /**
     * @param projectName The projectName to set.
     */
    public void setProjectName(final String projectName) {
        deligate.setProjectName(projectName);
    }

    /**
     * @param definition The definition to set.
     */
    public void setDefinition(final String definition) {
        deligate.setDefinition(definition);
    }

    /**
     * @return Returns the definition.
     */
    public String getDefinition() {
        return deligate.getDefinition();
    }

    /**
     * @return Returns the readedModel.
     */
    public ReadedModel getReadedModel() {
        return readedModel;
    }

    public String getCreateDate() {
        return Readed.dateFormat.format(deligate.getCreateDate());
    }

    public String getChangeDate() {
        return Readed.dateFormat.format(deligate.getChangeDate());
    }

    public Date getDateCreateDate() {
        return deligate.getCreateDate();
    }

    public Date getDateChangeDate() {
        return deligate.getChangeDate();
    }

    public IDEF0ModelPreferencesPersistent getDeligate() {
        return deligate;
    }
}
