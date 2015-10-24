package com.ramussoft.common.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class contain meta information about one persistent class.
 *
 * @author zdd
 */
public class PersistentRow extends PersistentStatus implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5153541203329580767L;

    public static final int ONE_TO_ONE = 0;

    public static final int ONE_TO_MANY = 1;

    public static final int MANY_TO_MANY = 2;

    private int id;

    private String tableName;

    private int tableType;

    private String className;

    private boolean exists;

    private String pluginName;

    private String typeName;

    private List<PersistentField> fields = new ArrayList<PersistentField>();

    private List<PersistentField> keyFields = null;

    private List<PersistentField> dataFields = null;

    private ClassLoader classLoader;

    public PersistentRow(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public PersistentRow() {
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableType the tableType to set
     */
    public void setTableType(int tableType) {
        this.tableType = tableType;
    }

    /**
     * @return the tableType
     */
    public int getTableType() {
        return tableType;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param exists the exists to set
     */
    public void setExists(boolean exists) {
        this.exists = exists;
    }

    /**
     * @return the exists
     */
    public boolean isExists() {
        return exists;
    }

    /**
     * @param list the fields to set
     */
    public void setFields(List<PersistentField> list) {
        this.fields = list;
    }

    /**
     * @return the fields
     */
    public List<PersistentField> getFields() {
        return fields;
    }

    public List<PersistentField> getKeyFields() {
        if (keyFields == null) {
            keyFields = new ArrayList<PersistentField>();
            for (PersistentField field : fields) {
                if (field.isPrimary())
                    keyFields.add(field);
            }
        }
        return keyFields;
    }

    public List<PersistentField> getDataFields() {
        if (dataFields == null) {
            dataFields = new ArrayList<PersistentField>();
            for (PersistentField field : fields) {
                if (!field.isPrimary())
                    dataFields.add(field);
            }
        }
        return dataFields;
    }

    /**
     * @param pluginName the pluginName to set
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * @return the pluginName
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }

    public PersistentField getFieldById(int fieldId) {
        for (PersistentField field : fields) {
            if (field.getFieldId() == fieldId)
                return field;
        }
        throw new RuntimeException("Field with id: " + fieldId + " not found.");
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null)
            return getClass().getClassLoader();
        return classLoader;
    }

}
