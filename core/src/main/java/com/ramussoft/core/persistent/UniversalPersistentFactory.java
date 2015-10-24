package com.ramussoft.core.persistent;

import static com.ramussoft.common.persistent.PersistentStatus.CREATED;
import static com.ramussoft.common.persistent.PersistentStatus.LOADED;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Binary;
import com.ramussoft.common.persistent.Date;
import com.ramussoft.common.persistent.Element;
import com.ramussoft.common.persistent.Id;
import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.common.persistent.Qualifier;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;
import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

/**
 * На відміну від класу {@link PersistentFactory}, даний клас ніяк прив’язаний
 * до модулів і атрибутів класифікаторів і може бути використаний для
 * автоматичного створення і модифікації таблиць.
 */

public class UniversalPersistentFactory extends PersistentFactory {

    protected final List<Class> persistentClasses = new ArrayList<Class>();

    protected final Hashtable<Class, PersistentRow> rowHash = new Hashtable<Class, PersistentRow>();

    protected final Hashtable<Class, PersistentWrapper> rowWrappers = new Hashtable<Class, PersistentWrapper>();

    public void addClasses(List<Class> pcs) {
        persistentClasses.addAll(pcs);
    }

    public UniversalPersistentFactory(JDBCTemplate template) {
        super(null, null, template);
    }

    @Override
    public void reinit() {
        for (Class<?> c : persistentClasses) {
            checkClass(null, c, null, null);
        }
    }

    @Override
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
        for (String fieldName : fields) {
            String databaseField = wrapper.toDatabaseField(fieldName);
            PersistentField field = getField(row, databaseField);
            if (field == null) {
                field = new PersistentField();
                row.getFields().add(field);
            }
            int type = wrapper.getAnnotationType(fieldName);

            Class<? extends Annotation> annotation = PersistentWrapper.ANNOTATIONS[type];
            if (Attribute.class.isAssignableFrom(annotation)) {
                Attribute attr = wrapper.getAnnotation(fieldName,
                        Attribute.class);
                field.setAutoset(attr.autoset());
                field.setFieldId(attr.id());
                field.setPrimary(attr.primary());
            } else if (Element.class.isAssignableFrom(annotation)) {
                Element attr = wrapper.getAnnotation(fieldName, Element.class);
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
                        .getAnnotation(fieldName,
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
                Binary binary = wrapper.getAnnotation(fieldName, Binary.class);
                field.setFieldId(binary.id());
            } else if (com.ramussoft.common.persistent.Integer.class
                    .isAssignableFrom(annotation)) {
                com.ramussoft.common.persistent.Integer integer = wrapper
                        .getAnnotation(fieldName,
                                com.ramussoft.common.persistent.Integer.class);
                field.setFieldId(integer.id());
                field.setPrimary(integer.primary());
            } else {
                throw new RuntimeException("Unknown annotation class: "
                        + annotation.getName());
            }

            field.setDatabaseName(databaseField);
            field.setType(type);
            field.setName(fieldName);
        }
    }

    @Override
    protected void fillTable(Table table, PersistentRow row, Class<?> p) {
        if (table != null)
            row.setTableName(getTableName(table));
        rowHash.put(p, row);
        rowWrappers.put(p, new PersistentWrapper(p));
    }

    protected String getTableName(Table table) {
        return table.name();
    }

    @Override
    protected void load() throws SQLException {
        List list = template.query("SELECT * FROM "
                + getPersistentClassesTableName(), new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                PersistentRow row = new PersistentRow();
                row.setStatus(LOADED);
                row.setClassName(rs.getString("CLASS_NAME").trim());
                row.setTableName(rs.getString("TABLE_NAME").trim());
                row.setExists(rs.getBoolean("PERSISTENT_EXISTS"));
                row.setFields(c(loadFields(row.getClassName())));
                return row;
            }
        });

        rows = list;
    }

    protected List loadFields(String className) throws SQLException {
        return template.query("SELECT * FROM " + getPersistentFieldsTableName()
                + " WHERE class_name=?", new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                PersistentField field = new PersistentField();
                field.setStatus(LOADED);
                field.setName(rs.getString("FIELD_NAME").trim());
                field.setDatabaseName(rs.getString("column_name").trim());
                field.setType(rs.getInt("FIELD_TYPE"));
                field.setExists(rs.getBoolean("FIELD_EXISTS"));
                field.setPrimary(rs.getBoolean("FIELD_PRIMARY"));
                return field;
            }

        }, new Object[]{className}, true);
    }

    @Override
    protected void store() throws SQLException {
        for (Object object : rows)
            if (((PersistentRow) object).getTableName() != null) {
                final PersistentRow row = (PersistentRow) object;
                if (row.getStatus() == CREATED) {
                    template.update(
                            "INSERT INTO "
                                    + getPersistentClassesTableName()
                                    + "(CLASS_NAME, TABLE_NAME, persistent_exists) "
                                    + "VALUES(?, ?, ?);\n" + "CREATE TABLE "
                                    + row.getTableName() + getCreateTableBody(),
                            new Object[]{row.getClassName(),
                                    row.getTableName(), true}, false);
                }

                List<PersistentField> primaryKays = new ArrayList<PersistentField>();

                boolean addPrimry = false;

                for (Object object2 : row.getFields()) {
                    PersistentField field = (PersistentField) object2;
                    if (field.getStatus() == CREATED) {
                        String sql = "INSERT INTO "
                                + getPersistentFieldsTableName()
                                + "(CLASS_NAME, FIELD_NAME, COLUMN_NAME, FIELD_TYPE, FIELD_PRIMARY) VALUES"
                                + "(?, ?, ?, ?, ?);\n"
                                + field.getAlterTableCommand("",
                                row.getTableName());
                        template.update(
                                sql,
                                new Object[]{row.getClassName(),
                                        field.getName(),
                                        field.getDatabaseName(),
                                        field.getFieldId(), field.isPrimary()},
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
                        template.execute("ALTER TABLE " + row.getTableName()
                                + " DROP CONSTRAINT " + pkeyName + ";\n"
                                + "DROP INDEX " + indexName + ";\n");
                    }

                    String[] keys = primaryKeysToStrings(primaryKays);

                    StringBuffer sb = new StringBuffer("(" + keys[0]);
                    for (int i = 1; i < keys.length; i++) {
                        sb.append(", ");
                        sb.append(keys[i]);
                    }
                    sb.append(")");

                    String keyFields = sb.toString();

                    template.execute("ALTER TABLE " + row.getTableName()
                            + " ADD CONSTRAINT " + pkeyName + " PRIMARY KEY "
                            + keyFields + ";\n" + "CREATE UNIQUE INDEX "
                            + indexName + " ON " + row.getTableName()
                            + keyFields + ";\n");
                }
                row.setStatus(LOADED);
            }
    }

    protected String[] primaryKeysToStrings(List<PersistentField> primaryKays) {
        String[] r = new String[primaryKays.size()];
        for (int i = 0; i < r.length; i++)
            r[i] = primaryKays.get(i).getDatabaseName();
        return r;
    }

    protected String getCreateTableBody() {
        return " ();";
    }

    protected String getPersistentFieldsTableName() {
        return "persistent_fields";
    }

    protected String getPersistentClassesTableName() {
        return "persistent_classes";
    }

    public List<Object> query(String sql, Class objectsClass) {
        return template.query(sql, new Mapper(objectsClass));
    }

    public List<Object> query(String sql, Class objectsClass, Object[] objects) {
        return template.query(sql, new Mapper(objectsClass), objects, true);
    }

    public List<Object> query(String sql, Class objectsClass, Object[] objects,
                              boolean cached) {
        return template.query(sql, new Mapper(objectsClass), objects, cached);
    }

    public void save(Object object) {
        if (object instanceof List)
            saveList((List) object);
        else
            save(object, true);
    }

    public void save(Object object, boolean cached) {
        if (object instanceof List)
            saveList((List) object, cached);
        else
            save(object, cached, new JDBCCallback() {

                @Override
                public Object execute(Connection connection)
                        throws SQLException {
                    return null;
                }
            });
    }

    public Object save(Object object, JDBCCallback callback) {
        if (object instanceof List)
            return saveList((List) object, callback);
        return save(object, true, callback);
    }

    public Object save(Object object, boolean cached, JDBCCallback callback) {
        if (object instanceof List)
            saveList((List) object, cached, callback);
        Class clazz = object.getClass();
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer insert = new StringBuffer("INSERT INTO "
                + row.getTableName() + " (");
        StringBuffer values = new StringBuffer(") VALUES (");

        boolean first = true;

        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (first)
                first = false;
            else {
                insert.append(", ");
                values.append(", ");
            }
            insert.append(field.getDatabaseName());
            values.append('?');
            objects[i] = wrapper.getField(object, field.getName());
        }
        String sql = insert.toString() + values.toString() + ")";
        return template.update(sql, objects, cached, callback);
    }

    public void saveList(final List<Object> objects, final boolean cached) {
        saveList(objects, cached, new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                return null;
            }
        });
    }

    public void saveList(final List<Object> objects) {
        saveList(objects, true);
    }

    public Object saveList(List<Object> objects, JDBCCallback callback) {
        return saveList(objects, true, callback);
    }

    public Object saveList(final List<Object> objects, final boolean cached,
                           final JDBCCallback callback) {
        return template.execute(new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                for (Object object : objects) {
                    saveInTransaction(object, cached);
                }
                return callback.execute(connection);
            }

        });
    }

    public void update(Object object) {
        if (object instanceof List)
            updateList((List) object);
        else
            update(object, true);
    }

    public void update(Object object, boolean cached) {
        if (object instanceof List)
            updateList((List) object, cached);
        else

            update(object, cached, new JDBCCallback() {

                @Override
                public Object execute(Connection connection)
                        throws SQLException {
                    return null;
                }
            });
    }

    public Object update(Object object, JDBCCallback callback) {
        if (object instanceof List)
            return updateList((List) object, true, callback);
        else

            return update(object, true, callback);
    }

    public Object update(Object object, boolean cached, JDBCCallback callback) {
        return update(object, cached, callback, object.getClass());
    }

    public Object update(Object object, boolean cached, JDBCCallback callback,
                         Class clazz) {
        if (object instanceof List)
            return updateList((List) object, cached, callback);
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer set = new StringBuffer("UPDATE " + row.getTableName()
                + " SET ");
        StringBuffer key = new StringBuffer(" WHERE ");

        boolean firstP = true;
        boolean firstS = true;

        int k = 0;

        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (field.isPrimary()) {
                if (firstP)
                    firstP = false;
                else
                    key.append(" AND ");
            } else {
                if (firstS)
                    firstS = false;
                else
                    set.append(", ");
            }
            if (field.isPrimary()) {
                key.append(field.getDatabaseName());
                key.append("=?");
            } else {
                set.append(field.getDatabaseName());
                set.append("=?");
            }
            if (!field.isPrimary()) {
                objects[k] = wrapper.getField(object, field.getName());
                k++;
            }
        }
        for (int i = 0; i < objects.length; i++)
            if (fields.get(i).isPrimary()) {
                objects[k] = wrapper.getField(object, fields.get(i).getName());
                k++;
            }
        String sql = set.toString() + key.toString();
        return template.update(sql, objects, cached, callback);
    }

    public void updateList(final List<Object> objects, final boolean cached) {
        update(objects, cached, new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                return null;
            }
        });
    }

    public Object updateList(final List<Object> objects,
                             final JDBCCallback callback) {
        return update(objects, true, callback);
    }

    public void updateList(final List<Object> objects) {
        updateList(objects, true);
    }

    public Object updateList(final List<Object> objects, final boolean cached,
                             final JDBCCallback callback) {
        return template.execute(new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                for (Object object : objects) {
                    updateInTransaction(object, cached);
                }
                return callback.execute(connection);
            }

        });
    }

    public void saveInTransaction(Object object) throws SQLException {
        saveInTransaction(object, true);
    }

    public void saveInTransaction(Object object, final boolean cached)
            throws SQLException {
        Class clazz = object.getClass();
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer insert = new StringBuffer("INSERT INTO "
                + row.getTableName() + " (");
        StringBuffer values = new StringBuffer(") VALUES (");

        boolean first = true;

        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (first)
                first = false;
            else {
                insert.append(", ");
                values.append(", ");
            }
            insert.append(field.getDatabaseName());
            values.append('?');
            objects[i] = wrapper.getField(object, field.getName());
        }
        String sql = insert.toString() + values.toString() + ")";
        PreparedStatement ps = template.getPreparedStatement(sql, cached);
        synchronized (ps) {
            template.setParams(ps, objects);
            if (!cached)
                ps.close();
            ps.execute();
        }
    }

    public void updateInTransaction(Object object) throws SQLException {
        updateInTransaction(object, true);
    }

    public void updateInTransaction(Object object, final boolean cached)
            throws SQLException {
        updateInTransaction(object, cached, object.getClass());
    }

    public void updateInTransaction(Object object, final boolean cached,
                                    Class clazz) throws SQLException {
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer set = new StringBuffer("UPDATE " + row.getTableName()
                + " SET ");
        StringBuffer key = new StringBuffer(" WHERE ");

        boolean firstP = true;
        boolean firstS = true;

        int k = 0;

        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (field.isPrimary()) {
                if (firstP)
                    firstP = false;
                else
                    key.append(" AND ");
            } else {
                if (firstS)
                    firstS = false;
                else
                    set.append(", ");
            }
            if (field.isPrimary()) {
                key.append(field.getDatabaseName());
                key.append("=?");
            } else {
                set.append(field.getDatabaseName());
                set.append("=?");
            }
            if (!field.isPrimary()) {
                objects[k] = wrapper.getField(object, field.getName());
                k++;
            }
        }
        for (int i = 0; i < objects.length; i++)
            if (fields.get(i).isPrimary()) {
                objects[k] = wrapper.getField(object, fields.get(i).getName());
                k++;
            }
        String sql = set.toString() + key.toString();
        PreparedStatement ps = template.getPreparedStatement(sql, cached);
        synchronized (ps) {
            template.setParams(ps, objects);
            ps.execute();
            if (!cached)
                ps.close();
        }
    }

    public class Mapper implements RowMapper {

        private Class clazz;

        public Mapper(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Object ob;
            try {
                ob = clazz.newInstance();
                PersistentRow row = rowHash.get(clazz);
                PersistentWrapper wrapper = rowWrappers.get(clazz);
                for (PersistentField field : row.getFields()) {
                    wrapper.setDatabaseField(ob, field, rs);
                }
                return ob;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return new Object();
        }
    }

    public void deleteInTransaction(Object object) throws SQLException {
        Class clazz = object.getClass();
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getKeyFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer delete = new StringBuffer("DELETE FROM "
                + row.getTableName() + " WHERE ");

        boolean first = true;

        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (first)
                first = false;
            else {
                delete.append(" AND ");
            }
            delete.append(field.getDatabaseName());
            delete.append("=?");
            objects[i] = wrapper.getField(object, field.getName());
        }
        String sql = delete.toString();
        PreparedStatement ps = template.getPreparedStatement(sql, true);

        template.setParams(ps, objects);
        ps.execute();
    }

    public void delete(Object object) {
        Class clazz = object.getClass();
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getKeyFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer delete = new StringBuffer("DELETE FROM "
                + row.getTableName() + " WHERE ");

        boolean first = true;

        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (first)
                first = false;
            else {
                delete.append(" AND ");
            }
            delete.append(field.getDatabaseName());
            delete.append("=?");
            objects[i] = wrapper.getField(object, field.getName());
        }
        String sql = delete.toString();
        template.update(sql, objects, true);
    }

    @Override
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

                        }
                    }
                    st.execute("DELETE FROM " + getPersistentFieldsTableName());
                    st.execute("DELETE FROM " + getPersistentClassesTableName());
                } finally {
                    if (st != null)
                        st.close();
                }
                return null;
            }
        });
    }

}
