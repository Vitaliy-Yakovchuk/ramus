package com.ramussoft.idef0.attribute;

import java.sql.Timestamp;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Date;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "model_preferences")
public class IDEF0ModelPreferencesPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 1363818316841154928L;

    private String projectName = "";

    private String projectAutor = "";

    private String definition = "";

    private Timestamp createDate = new Timestamp(System.currentTimeMillis());

    private Timestamp changeDate = new Timestamp(System.currentTimeMillis());

    private String usedAt = "";

    private String modelLetter = null;

    private String diagramSize = "A4";

    private Long termAttribute;

    /**
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the projectName
     */
    @Text(id = 2)
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectAutor the projectAutor to set
     */
    public void setProjectAutor(String projectAutor) {
        this.projectAutor = projectAutor;
    }

    /**
     * @return the projectAutor
     */
    @Text(id = 3)
    public String getProjectAutor() {
        return projectAutor;
    }

    /**
     * @param definition the definition to set
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * @return the definition
     */
    @Text(id = 4)
    public String getDefinition() {
        return definition;
    }

    /**
     * @param createDate the createDate to set
     */
    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    /**
     * @return the createDate
     */
    @Date(id = 5)
    public Timestamp getCreateDate() {
        return createDate;
    }

    /**
     * @param changeDate the changeDate to set
     */
    public void setChangeDate(Timestamp changeDate) {
        this.changeDate = changeDate;
    }

    /**
     * @return the changeDate
     */
    @Date(id = 6)
    public Timestamp getChangeDate() {
        return changeDate;
    }

    /**
     * @param usedAt the usedAt to set
     */
    public void setUsedAt(String usedAt) {
        this.usedAt = usedAt;
    }

    /**
     * @return the usedAt
     */
    @Text(id = 7)
    public String getUsedAt() {
        return usedAt;
    }

    /**
     * @param modelLetter the modelLetter to set
     */
    public void setModelLetter(String modelLetter) {
        this.modelLetter = modelLetter;
    }

    /**
     * @return the modelLetter
     */
    @Text(id = 8)
    public String getModelLetter() {
        return modelLetter;
    }

    @Text(id = 9)
    public String getDiagramSize() {
        return diagramSize;
    }

    public void setDiagramSize(String diagramSize) {
        this.diagramSize = diagramSize;
    }

    @com.ramussoft.common.persistent.Long(id = 10)
    public Long getTermAttribute() {
        return termAttribute;
    }

    public void setTermAttribute(Long termAttribute) {
        this.termAttribute = termAttribute;
    }
}
