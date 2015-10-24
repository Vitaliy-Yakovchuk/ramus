package com.ramussoft.core.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;

public class BranchesPersistentFactory extends UniversalPersistentFactory {

    private long activeBranchId = -1l;

    protected PreparedStatement ACTIVE_BRANCH_PS;

    protected String sqlBranchCondition;

    public BranchesPersistentFactory(JDBCTemplate template, String prefix) {
        super(template);
        this.prefix = prefix;

        try {
            this.ACTIVE_BRANCH_PS = template.getPreparedStatement(
                    "SELECT MAX(branch_id) FROM " + prefix
                            + "branches WHERE branch_type=0", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.sqlBranchCondition = " (created_branch_id <=? "
                + "AND (removed_branch_id IS NULL OR removed_branch_id>?))";
    }

    @Override
    public void setPrefix(String prefix) {
        super.setPrefix(prefix);
        /*this.sqlBranchCondition = "(created_branch_id IN (SELECT parent_branch_id FROM "
                + prefix
				+ "branch_tree WHERE branch_id=?) "
				+ "AND (removed_branch_id IS NULL OR removed_branch_id NOT IN "
				+ "(SELECT parent_branch_id FROM "
				+ prefix
				+ "branch_tree WHERE branch_id=?)) )";*/

        this.sqlBranchCondition = " (created_branch_id <=? "
                + "AND (removed_branch_id IS NULL OR removed_branch_id>?))";
    }

    @Override
    protected String getPersistentClassesTableName() {
        return prefix + "branch_persistent_classes";
    }

    @Override
    public void delete(final Object object) {
        template.execute(new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                deleteInTransaction(object);
                return null;
            }
        });
    }

    @Override
    public void deleteInTransaction(Object object) throws SQLException {
        Class clazz = object.getClass();
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getKeyFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer delete = new StringBuffer("UPDATE " + row.getTableName()
                + " SET removed_branch_id=? WHERE ");

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
        delete.append(" AND ");

        delete.append(getSQLBranchCondition());

        String sql = delete.toString();
        PreparedStatement ps = template.getPreparedStatement(sql, true);
        long id = getActiveBranchId();
        ps.setLong(1, id);
        for (int i = 0; i < objects.length; i++)
            template.setParam(ps, i + 2, objects[i]);

        Object[] objects2 = addBranchWhereObjects(id, new Object[]{});
        for (int i = 0; i < objects2.length; i++) {
            template.setParam(ps, objects.length + i + 2, objects2[i]);
        }
        ps.executeUpdate();
    }

    @Override
    protected String getPersistentFieldsTableName() {
        return prefix + "branch_persistent_fields";
    }

    @Override
    protected String getTableName(Table table) {
        return prefix + super.getTableName(table);
    }

    protected String[] primaryKeysToStrings(List<PersistentField> primaryKays) {
        String[] r = super.primaryKeysToStrings(primaryKays);
        r = Arrays.copyOf(r, r.length + 1);
        r[r.length - 1] = "created_branch_id";
        return r;
    }

    protected String getCreateTableBody() {
        return " (created_branch_id BIGINT NOT NULL, removed_branch_id BIGINT);";
    }

    /**
     * @param activeBranchId the activeBranchId to set
     */
    public void setActiveBranchId(long activeBranchId) {
        this.activeBranchId = (activeBranchId == getMainBranch()) ? -1l
                : activeBranchId;
    }

    /**
     * @return the activeBranchId
     */
    public long getActiveBranchId() {
        if (activeBranchId == -1l)
            return getMainBranch();
        return activeBranchId;
    }

    private long getMainBranch() {
        try {
            ResultSet rs = ACTIVE_BRANCH_PS.executeQuery();
            rs.next();
            try {
                return rs.getLong(1);
            } finally {
                rs.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/*
	 * public String getStaticSQLBranchCondition() { long id =
	 * getActiveBranchId(); return
	 * "(created_branch_id IN (SELECT parent_branch_id FROM " + prefix +
	 * "branch_tree WHERE branch_id=" + id +
	 * ") AND (removed_branch_id IS NULL OR removed_branch_id NOT IN (SELECT parent_branch_id FROM "
	 * + prefix + "branch_tree WHERE branch_id=" + id + ")) )"; }
	 */

    /**
     * Needs two parameters of active branch
     *
     * @return
     */
    public String getSQLBranchCondition() {
        return sqlBranchCondition;
    }

    public Object getObject(Object primaryKey) {
        Class clazz = primaryKey.getClass();
        long id = getActiveBranchId();
        PersistentRow row = rowHash.get(clazz);
        PersistentWrapper wrapper = rowWrappers.get(clazz);
        List<PersistentField> fields = row.getKeyFields();

        Object[] objects = new Object[fields.size()];

        StringBuffer select = new StringBuffer("SELECT * FROM "
                + row.getTableName() + " WHERE ");

        boolean first = true;

        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (first)
                first = false;
            else {
                select.append(" AND ");
            }
            select.append(field.getDatabaseName());
            select.append("=?");
            objects[i] = wrapper.getField(primaryKey, field.getName());
        }

        select.append("AND");
        select.append(getSQLBranchCondition());
        objects = addBranchWhereObjects(id, objects);

        return template.queryForObjects(select.toString(), new Mapper(clazz),
                objects, true);
    }

    public Object[] addBranchWhereObjects(long id, Object[] objects) {
        objects = Arrays.copyOf(objects, objects.length + 2);
        objects[objects.length - 2] = id;
        objects[objects.length - 1] = id;
        return objects;
    }

    @Override
    public Object save(Object object, boolean cached, JDBCCallback callback) {
        return update(object, cached, callback);
    }

    @Override
    public void saveInTransaction(Object object, boolean cached)
            throws SQLException {
        updateInTransaction(object, cached);
    }

    @Override
    public Object update(final Object object, final boolean cached,
                         final JDBCCallback callback, final Class clazz) {
        return template.execute(new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                updateInTransaction(object, cached, clazz);
                return callback.execute(connection);
            }
        });
    }

    @Override
    public void updateInTransaction(Object object, boolean cached, Class clazz)
            throws SQLException {
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
        int keyCount = 0;
        for (int i = 0; i < objects.length; i++) {
            PersistentField field = fields.get(i);
            if (field.isPrimary()) {
                keyCount++;
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
        String sql = set.toString() + key.toString()
                + " AND created_branch_id=?";
        PreparedStatement ps = template.getPreparedStatement(sql, cached);
        synchronized (ps) {
            template.setParams(ps, objects);
            long id = getActiveBranchId();
            ps.setLong(objects.length + 1, id);
            if (ps.executeUpdate() == 0) {
                if (!cached)
                    ps.close();
                sql = "UPDATE " + row.getTableName()
                        + " SET removed_branch_id=?" + key.toString() + " AND "
                        + getSQLBranchCondition();
                ps = template.getPreparedStatement(sql, cached);
                ps.setLong(1, id);
                for (int i = objects.length - keyCount; i < objects.length; i++) {
                    template.setParam(ps, i + 2 - objects.length + keyCount,
                            objects[i]);
                }
                Object[] objects2 = addBranchWhereObjects(id, new Object[]{});
                for (int i = 0; i < objects2.length; i++)
                    template.setParam(ps, keyCount + 2 + i, objects2[i]);
                ps.executeUpdate();

                if (!cached)
                    ps.close();

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
                insert.append(", created_branch_id");
                values.append(", ?");

                sql = insert.toString() + values.toString() + ")";

                ps = template.getPreparedStatement(sql, cached);

                template.setParams(ps, objects);
                ps.setLong(objects.length + 1, id);

                ps.executeUpdate();

                if (!cached)
                    ps.close();

            } else if (!cached)
                ps.close();
        }
    }

    public List<Object> query(Class objectsClass, String[] databaseFields,
                              Object[] objects) {
        return query(objectsClass, databaseFields, objects, false);
    }

    public List<Object> query(Class clazz, String[] databaseFields,
                              Object[] objects, boolean cached) {
        StringBuffer sb = new StringBuffer();
        PersistentRow row = rowHash.get(clazz);
        sb.append("SELECT * FROM " + row.getTableName() + " WHERE ");
        boolean fist = true;
        for (String f : databaseFields) {
            if (fist)
                fist = false;
            else
                sb.append("AND ");
            sb.append(f);
            sb.append("=? ");
        }
        if (!fist)
            sb.append("AND ");
        sb.append(getSQLBranchCondition());
        long id = getActiveBranchId();
        objects = addBranchWhereObjects(id, objects);
        return template
                .query(sb.toString(), new Mapper(clazz), objects, cached);
    }
}
