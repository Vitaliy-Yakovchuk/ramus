package com.ramussoft.common.persistent;

import java.io.Serializable;

/**
 * All data type constants must be the same as {@link PersistentWrapper}
 * .ANNOTATIONS indexes.
 *
 * @author zdd
 */

public class PersistentField extends PersistentStatus implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5544760309765088774L;

    public static final int ELEMENT = 0;

    public static final int ATTRIBUTE = 1;

    public static final int TEXT = 2;

    public static final int LONG = 3;

    public static final int DATE = 4;

    public static final int DOUBLE = 5;

    public static final int ID = 6;

    public static final int QUALIFIER = 7;

    public static final int BINARY = 8;

    public static final int INTEGER = 9;

    public static final int VALUE_BRANCH_ID = 10;

    /**
     * Must be equivalent to the constants.
     */

    private static final String[] DATABASE_TYPES = new String[]{"BIGINT",
            "BIGINT", "TEXT", "BIGINT", "TIMESTAMP", "DOUBLE PRECISION",
            "BIGINT", "BIGINT", "BYTEA", "INTEGER", "BIGINT"};

    private long id;

    private String databaseName;

    private int fieldId;

    private boolean exists;

    private int type;

    private boolean autoset = false;

    private boolean primary = false;

    private String defaultValue = null;

    private String className;

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param name the fieldName to set
     */
    public void setDatabaseName(String name) {
        this.databaseName = name;
    }

    /**
     * @return the fieldName
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @param fieldId the fieldId to set
     */
    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    /**
     * @return the fieldId
     */
    public int getFieldId() {
        return fieldId;
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
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param autoset the auto-set to set
     */
    public void setAutoset(boolean autoset) {
        this.autoset = autoset;
    }

    /**
     * @return the auto-set
     */
    public boolean isAutoset() {
        return autoset;
    }

    public String getDatabaseType() {
        return DATABASE_TYPES[getType()];
    }

    public String getAlterTableCommand(String prefix, String tableName) {
        StringBuffer sb = new StringBuffer("ALTER TABLE " + tableName
                + " ADD COLUMN " + getDatabaseName() + " " + getDatabaseType());
        if (isPrimary()) {
            sb.append(" NOT NULL");
        }
        if (getDefaultValue() != null) {
            sb.append(" DEFAULT " + getDefaultValue());
        }
        sb.append(";\n");
        if (getType() == ELEMENT) {
            sb.append("ALTER TABLE " + tableName + " ADD CONSTRAINT "
                    + tableName + getDatabaseName() + "_" + prefix
                    + "elements_id_fkay FOREIGN KEY(" + getDatabaseName()
                    + ") REFERENCES " + prefix + "elements(ELEMENT_ID);\n");
        } else if (getType() == ATTRIBUTE) {
            sb.append("ALTER TABLE " + tableName + " ADD CONSTRAINT "
                    + tableName + getDatabaseName() + "_" + prefix
                    + "attributes_id_fkay FOREIGN KEY(" + getDatabaseName()
                    + ") REFERENCES " + prefix + "attributes(ATTRIBUTE_ID);\n");
        } else if (getType() == QUALIFIER) {
            sb.append("ALTER TABLE " + tableName + " ADD CONSTRAINT "
                    + tableName + getDatabaseName() + "_" + prefix
                    + "qualifiers_id_fkay FOREIGN KEY(" + getDatabaseName()
                    + ") REFERENCES " + prefix + "qualifiers(QUALIFIER_ID);\n");
        } else if (getType() == ID) {
            String sequenceName = getSequenceName(tableName);
            sb.append("CREATE SEQUENCE " + sequenceName + " START 1;\n");
        }

        return sb.toString();
    }

    public String getSequenceName(String tableName) {
        return tableName + "_" + getDatabaseName();
    }

    /**
     * @param primary the primary to set
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * @return the primary
     */
    public boolean isPrimary() {
        return primary;
    }

    public String getName() {
        return className;
    }

    public void setName(String className) {
        this.className = className;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

}
