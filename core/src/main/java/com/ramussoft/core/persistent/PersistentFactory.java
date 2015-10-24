package com.ramussoft.core.persistent;

import static com.ramussoft.common.persistent.PersistentStatus.CREATED;
import static com.ramussoft.common.persistent.PersistentStatus.LOADED;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ramussoft.common.AttributeType;
import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.persistent.Binary;
import com.ramussoft.common.persistent.Date;
import com.ramussoft.common.persistent.Element;
import com.ramussoft.common.persistent.Id;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.common.persistent.Qualifier;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;
import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class PersistentFactory {

    protected JDBCTemplate template;

    protected List<AttributePlugin> plugins;

    protected List<PersistentRow> rows = new ArrayList<PersistentRow>();

    protected String prefix;

    public PersistentFactory(String prefix, List<AttributePlugin> plugins,
                             JDBCTemplate template) {
        this.plugins = plugins;
        this.prefix = prefix;
        this.template = template;
    }

    public void reinit() {
        for (AttributePlugin plugin : plugins) {
            Class<? extends Persistent> classes[] = plugin.getPersistents();
            for (Class<? extends Persistent> p : classes) {
                checkClass(plugin.getClass().getClassLoader(), p,
                        plugin.getName(), plugin.getTypeName());
            }
        }
    }

    protected void checkClass(ClassLoader classLoader, Class<?> p,
                              String pluginName, String typeName) {
        PersistentWrapper wrapper = new PersistentWrapper(p);

        Table table = p.getAnnotation(Table.class);
        String className = p.getName();
        PersistentRow row = getRow(className);
        if (row == null) {
            row = new PersistentRow(classLoader);
            rows.add(row);
        } else
            row.setClassLoader(classLoader);

        row.setClassName(className);
        row.setExists(true);
        row.setPluginName(pluginName);
        fillTable(table, row, p);
        if (table != null)
            row.setTableType(table.type().ordinal());
        row.setTypeName(typeName);

        String[] fields = wrapper.getFields();
        fields = Arrays.copyOf(fields, fields.length + 1);
        String valueBranchId = "valueBranchId";
        fields[fields.length - 1] = valueBranchId;
        for (String fieldName : fields) {
            String databaseField = wrapper.toDatabaseField(fieldName);
            PersistentField field = getField(row, databaseField);
            if (field == null) {
                field = new PersistentField();
                row.getFields().add(field);
            }
            int type = wrapper.getAnnotationType(fieldName);
            if (valueBranchId.equals(fieldName)) {
                field.setAutoset(true);
                field.setFieldId(-2);
                field.setPrimary(true);
                field.setDefaultValue("0");
            } else {

                Class<? extends Annotation> annotation = PersistentWrapper.ANNOTATIONS[type];
                if (Attribute.class.isAssignableFrom(annotation)) {
                    Attribute attr = wrapper.getAnnotation(fieldName,
                            Attribute.class);
                    field.setAutoset(attr.autoset());
                    field.setFieldId(attr.id());
                    field.setPrimary(attr.primary());
                } else if (Element.class.isAssignableFrom(annotation)) {
                    Element attr = wrapper.getAnnotation(fieldName,
                            Element.class);
                    field.setAutoset(attr.autoset());
                    field.setFieldId(attr.id());
                    field.setPrimary(attr.primary());
                } else if (com.ramussoft.common.persistent.Long.class
                        .isAssignableFrom(annotation)) {
                    com.ramussoft.common.persistent.Long attr = wrapper
                            .getAnnotation(fieldName,
                                    com.ramussoft.common.persistent.Long.class);
                    field.setFieldId(attr.id());
                    field.setPrimary(attr.primary());
                    if (attr.setDefaultValue()) {
                        field.setDefaultValue(Long.toString(attr.defaultValue()));
                    }
                } else if (com.ramussoft.common.persistent.Double.class
                        .isAssignableFrom(annotation)) {
                    com.ramussoft.common.persistent.Double attr = wrapper
                            .getAnnotation(
                                    fieldName,
                                    com.ramussoft.common.persistent.Double.class);
                    field.setFieldId(attr.id());
                    field.setPrimary(attr.primary());
                } else if (Date.class.isAssignableFrom(annotation)) {
                    Date attr = wrapper.getAnnotation(fieldName, Date.class);
                    field.setFieldId(attr.id());
                    field.setPrimary(attr.primary());
                } else if (Text.class.isAssignableFrom(annotation)) {
                    Text attr = wrapper.getAnnotation(fieldName, Text.class);
                    field.setFieldId(attr.id());
                    field.setPrimary(attr.primary());
                } else if (Id.class.isAssignableFrom(annotation)) {
                    Id attr = wrapper.getAnnotation(fieldName, Id.class);
                    field.setFieldId(attr.id());
                    field.setPrimary(true);
                    field.setAutoset(true);
                } else if (Qualifier.class.isAssignableFrom(annotation)) {
                    Qualifier attr = wrapper.getAnnotation(fieldName,
                            Qualifier.class);
                    field.setFieldId(attr.id());
                    field.setPrimary(attr.primary());
                } else if (Binary.class.isAssignableFrom(annotation)) {
                    Binary binary = wrapper.getAnnotation(fieldName,
                            Binary.class);
                    field.setFieldId(binary.id());
                } else if (com.ramussoft.common.persistent.Integer.class
                        .isAssignableFrom(annotation)) {
                    com.ramussoft.common.persistent.Integer integer = wrapper
                            .getAnnotation(
                                    fieldName,
                                    com.ramussoft.common.persistent.Integer.class);
                    field.setFieldId(integer.id());
                    field.setPrimary(integer.primary());
                } else {
                    throw new RuntimeException("Unknown annotation class: "
                            + annotation.getName());
                }
            }

            field.setDatabaseName(databaseField);
            field.setType(type);
            field.setName(fieldName);
        }
    }

    protected void fillTable(Table table, PersistentRow row, Class<?> p) {
        row.setTableName(prefix + "attribute_" + table.name());
    }

    public PersistentField getField(PersistentRow row, String fieldName) {
        for (Object object : row.getFields()) {
            PersistentField field = (PersistentField) object;
            if (field.getDatabaseName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    protected PersistentRow getRow(String className) {
        for (Object object : rows) {
            PersistentRow row = (PersistentRow) object;
            if (row.getClassName().equals(className))
                return row;
        }
        return null;
    }

    /**
     * Method rebuild existing meta information.
     *
     * @throws SQLException
     */

    public void rebuild() throws SQLException {
        try {
            load();
            reinit();
            store();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void store() throws SQLException {
        for (Object object : rows) {
            final PersistentRow row = (PersistentRow) object;

            if (row.getStatus() == CREATED) {
                final long next = template.nextVal(prefix
                        + "persistents_sequence");
                row.setId((int) next);
                template.update(
                        "INSERT INTO "
                                + prefix
                                + "persistents(PERSISTENT_ID, TABLE_NAME, TABLE_TYPE, CLASS_NAME, PLUGIN_NAME, TYPE_NAME) "
                                + "VALUES(?, ?, ?, ?, ?, ?);\n"
                                + "CREATE TABLE " + row.getTableName() + " ();",
                        new Object[]{next, row.getTableName(),
                                row.getTableType(), row.getClassName(),
                                row.getPluginName(), row.getTypeName()}, false);
            }

            List<PersistentField> primaryKays = new ArrayList<PersistentField>();

            boolean addPrimry = false;

            for (Object object2 : row.getFields()) {
                PersistentField field = (PersistentField) object2;
                if (field.getStatus() == CREATED) {
                    final long next = template.nextVal(prefix
                            + "persistent_fields_sequence");
                    field.setId((int) next);
                    template.update(
                            "INSERT INTO "
                                    + prefix
                                    + "persistent_fields(PERSISTENT_FIELD_ID, PERSISTENT_ID, FIELD_NAME, FIELD_DATABASE_NAME, FIELD_ID, FIELD_TYPE, FIELD_AUTOSET, FIELD_PRIMARY) VALUES"
                                    + "(?, ?, ?, ?, ?, ?, ?, ?);\n"
                                    + extracted(row, field),
                            new Object[]{next, (long) row.getId(),
                                    field.getName(), field.getDatabaseName(),
                                    field.getFieldId(), field.getType(),
                                    field.isAutoset(), field.isPrimary()},
                            false);
                    field.setStatus(LOADED);
                    if (field.isPrimary()) {
                        addPrimry = true;
                    }
                }
                if (field.isPrimary()) {
                    primaryKays.add(field);
                }
            }
            if ((addPrimry) && (primaryKays.size() > 0)) {
                String pkeyName = row.getTableName() + "_pkey";
                String indexName = row.getTableName() + "_pkey_index";
                if (row.getStatus() == LOADED) {
                    try {
                        template.execute("ALTER TABLE " + row.getTableName()
                                + " DROP CONSTRAINT " + pkeyName + ";\n"
                                + "DROP INDEX " + indexName + ";\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                StringBuffer sb = new StringBuffer("("
                        + primaryKays.get(0).getDatabaseName());
                for (int i = 1; i < primaryKays.size(); i++) {
                    sb.append(", ");
                    sb.append(primaryKays.get(i).getDatabaseName());
                }
                sb.append(")");

                String keyFields = sb.toString();
                try {
                    template.execute("ALTER TABLE " + row.getTableName()
                            + " ADD CONSTRAINT " + pkeyName + " PRIMARY KEY "
                            + keyFields + ";\n" + "CREATE UNIQUE INDEX "
                            + indexName + " ON " + row.getTableName()
                            + keyFields + ";\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            row.setStatus(LOADED);
        }
    }

    private String extracted(final PersistentRow row, PersistentField field) {
        return field.getAlterTableCommand(prefix,
                row.getTableName());
    }

    @SuppressWarnings("unchecked")
    protected void load() throws SQLException {
        List list = template.query("SELECT * FROM " + prefix + "persistents",
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        PersistentRow row = new PersistentRow();
                        row.setStatus(LOADED);
                        row.setClassName(rs.getString("CLASS_NAME").trim());
                        row.setExists(rs.getBoolean("PERSISTENT_EXISTS"));
                        row.setId((int) rs.getLong("PERSISTENT_ID"));
                        row.setTableName(rs.getString("TABLE_NAME").trim());
                        row.setTableType(rs.getInt("TABLE_TYPE"));
                        row.setPluginName(rs.getString("PLUGIN_NAME").trim());
                        row.setTypeName(rs.getString("TYPE_NAME").trim());
                        row.setFields(c(loadFields(row.getId())));
                        return row;
                    }
                });
        rows = list;
    }

    protected List<PersistentField> c(List<Object> loadFields) {
        List<PersistentField> list = new ArrayList<PersistentField>(
                loadFields.size());
        for (Object object : loadFields) {
            list.add((PersistentField) object);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    protected List loadFields(int id) throws SQLException {
        return template.query("SELECT * FROM " + prefix
                + "persistent_fields WHERE PERSISTENT_ID=?", new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                PersistentField field = new PersistentField();
                field.setStatus(LOADED);
                field.setAutoset(rs.getBoolean("FIELD_AUTOSET"));
                field.setExists(rs.getBoolean("FIELD_EXISTS"));
                field.setFieldId(rs.getInt("FIELD_ID"));
                field.setName(rs.getString("FIELD_NAME").trim());
                field.setDatabaseName(rs.getString("FIELD_DATABASE_NAME")
                        .trim());
                field.setId(rs.getLong("PERSISTENT_FIELD_ID"));
                field.setType(rs.getInt("FIELD_TYPE"));
                field.setPrimary(rs.getBoolean("FIELD_PRIMARY"));
                return field;
            }

        }, new Object[]{Long.valueOf(id)}, true);
    }

    public void dropTables() throws SQLException {
        load();

        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                Statement st = null;
                try {
                    st = connection.createStatement();
                    for (Object object : rows) {
                        PersistentRow row = (PersistentRow) object;
                        if (row.getStatus() == LOADED) {

                            for (PersistentField field : row.getFields()) {
                                if (field.getType() == PersistentField.ID) {
                                    st.execute("DROP SEQUENCE "
                                            + field.getSequenceName(row
                                            .getTableName()) + ";");
                                }
                            }

                            st.execute("DROP TABLE " + row.getTableName() + ";");
                            st.execute("DELETE FROM " + prefix
                                    + "persistent_fields");
                            st.execute("DELETE FROM " + prefix + "persistents");
                        }
                    }
                } finally {
                    if (st != null)
                        st.close();
                }
                return null;
            }
        });
    }

    public List<PersistentRow> getRows() {
        return rows.subList(0, rows.size());
    }

    public PersistentRow getRowByAttributeType(AttributeType attributeType) {
        for (PersistentRow row : rows) {
            if ((row.getPluginName().equals(attributeType.getPluginName()))
                    && (row.getTypeName().equals(attributeType.getTypeName()))) {
                return row;
            }
        }
        throw new RuntimeException("No metadata for type: " + attributeType
                + " not found.");
    }

    public void setTemplate(JDBCTemplate template) {
        this.template = template;
    }

    public JDBCTemplate getTemplate() {
        return template;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
