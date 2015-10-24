package com.ramussoft.core.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Branch;
import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.LocalAccessor;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.PlugableEngineAccessor;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.attribute.FindObject;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Transaction;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.eval.Eval;
import com.ramussoft.eval.MetaValue;
import com.ramussoft.eval.Util;
import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public abstract class IEngineImpl extends AbstractIEngine implements IEngine {

    protected JDBCTemplate template;

    protected String prefix;

    protected PluginFactory factory;

    private PersistentFactory persistentFactory;

    private Hashtable<Class<? extends Persistent>, PersistentWrapper> wrappers = new Hashtable<Class<? extends Persistent>, PersistentWrapper>();

    private Hashtable<Class<? extends Persistent>, PersistentRow> metadata = new Hashtable<Class<? extends Persistent>, PersistentRow>();

    private IntegrityAccessorSuit accessor = new IntegrityAccessorSuit();

    private long activeBranchId = -1l;

    private String ACTIVE_BRANCH_SQL;

    private PreparedStatement ACTIVE_BRANCH_PS;

    private Object branchCreationLock = new Object();

    /**
     * Do not set this value to true, as cached will broke undo redo functions
     */
    private boolean cached = true;

    private PreparedStatement IS_BRANCH_LEAF_PS;

    private static Hashtable<Long, Qualifier> qualifeirsCache = new Hashtable<Long, Qualifier>();

    private static Hashtable<Long, Attribute> attributesCache = new Hashtable<Long, Attribute>();

    public IEngineImpl(int id, JDBCTemplate template, String prefix,
                       PluginFactory factory) throws ClassNotFoundException {
        this(id, template, prefix, factory, false);
    }

    @SuppressWarnings("unchecked")
    public IEngineImpl(int id, JDBCTemplate template, String prefix,
                       PluginFactory factory, boolean cached)
            throws ClassNotFoundException {
        super(id);
        this.cached = cached;
        this.template = template;
        this.prefix = prefix;
        this.factory = factory;
        PlugableEngineAccessor accessor = new PlugableEngineAccessor(this,
                new LocalAccessor(this) {
                    @Override
                    public boolean isBranchLeaf() {
                        return IEngineImpl.this.isBranchLeaf();
                    }
                }, factory);
        this.accessor.addAccessRules(accessor);
        List<AttributePlugin> plugins = factory.getAttributePlugins();
        persistentFactory = new PersistentFactory(prefix, plugins, template);
        try {
            persistentFactory.rebuild();
            try {
                template.executeResource("/com/ramussoft/core/impl/elist-fix.sql");
            } catch (Exception e) {
            }
            initStartBranch(template);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (PersistentRow row : persistentFactory.getRows()) {
            try {
                Class<? extends Persistent> clazz = (Class<? extends Persistent>) row
                        .getClassLoader().loadClass(row.getClassName());
                metadata.put(clazz, row);
                wrappers.put(clazz, new PersistentWrapper(clazz));
            } catch (ClassNotFoundException e) {
                System.err.println("WARNING: Class not found "
                        + row.getClassName());
            }
        }
        for (Plugin p : factory.getPlugins()) {
            for (String sequence : p.getSequences()) {
                createSequence(sequence);
            }
        }

        this.ACTIVE_BRANCH_SQL = "SELECT MAX(branch_id) FROM " + prefix
                + "branches WHERE branch_type=0";
        try {
            this.ACTIVE_BRANCH_PS = template.getPreparedStatement(
                    ACTIVE_BRANCH_SQL, true);
            this.IS_BRANCH_LEAF_PS = template.getPreparedStatement(
                    "SELECT COUNT(*) FROM " + prefix
                            + "branches WHERE branch_id>?", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void initStartBranch(JDBCTemplate template) {
        template.execute(new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection
                        .prepareStatement("SELECT * FROM "
                                + IEngineImpl.this.prefix
                                + "branches WHERE branch_id=?");
                ps.setLong(1, 0);
                ResultSet rs = ps.executeQuery();
                boolean create = !rs.next();
                rs.close();
                ps.close();
                if (create) {
                    ps = connection
                            .prepareStatement("INSERT INTO "
                                    + IEngineImpl.this.prefix
                                    + "branches(branch_id, creation_user, reason, branch_type, module_name, creation_time) VALUES(?, ?, ?, ?, 'core', ?)");

                    ps.setLong(1, 0l);
                    String user = "admin";

                    ps.setString(2, user);
                    ps.setString(3, "initial");
                    ps.setInt(4, 0);
                    ps.setTimestamp(5,
                            new Timestamp(System.currentTimeMillis()));

                    ps.execute();

                    ps.close();
                }

                return null;
            }
        });
    }

    public void clearCache() {
        qualifeirsCache.clear();
        attributesCache.clear();
    }

    private void createSequence(String sequence) {
        try {
            template.execute("CREATE SEQUENCE " + prefix + sequence
                    + " START 1;");
        } catch (Exception e) {
        }
    }

    private void throwExaptionIfNotCan(boolean can, String text) {
        if (!can)
            throw new RuntimeException(text);
    }

    @Override
    public Attribute createAttribute(long attributeId,
                                     AttributeType attributeType, boolean system) {

        throwExaptionIfNotCan(getAccessor().canCreateAttribute(),
                "Can not create attribute.");

        if (attributeId == -1) {
            attributeId = nextValue("attributes_sequence");
            long id = template.queryForLong("SELECT MAX(ATTRIBUTE_ID) FROM "
                    + prefix + "attributes;");
            while (id >= attributeId) {
                attributeId = template.nextVal(prefix + "attributes_sequence");
            }
        }

        Attribute attribute = new Attribute();
        attribute.setId(attributeId);
        attribute.setAttributeType(attributeType);
        attribute.setSystem(system);
        long branchId = getActiveBranchId();
        Long l = (Long) template.queryForObjects("SELECT COUNT(*) FROM "
                        + prefix
                        + "attributes WHERE ATTRIBUTE_ID=? AND removed_branch_id=?",
                new RowMapper() {

                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return rs.getLong(1);
                    }
                }, new Object[]{attributeId, branchId}, false);
        if (l.longValue() > 0)
            template.update(
                    "UPDATE "
                            + prefix
                            + "attributes SET ATTRIBUTE_NAME=?, ATTRIBUTE_SYSTEM=?, ATTRIBUTE_TYPE_PLUGIN_NAME=?, ATTRIBUTE_TYPE_NAME=?, ATTRIBUTE_TYPE_COMPARABLE=?, "
                            + "removed_branch_id=? WHERE ATTRIBUTE_ID = ? AND removed_branch_id=?",
                    new Object[]{attribute.getName(), system,
                            attributeType.getPluginName(),
                            attributeType.getTypeName(),
                            attributeType.isComparable(), Integer.MAX_VALUE,
                            attribute.getId(), branchId}, true);
        else
            template.update(
                    "INSERT INTO "
                            + prefix
                            + "attributes(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_SYSTEM, ATTRIBUTE_TYPE_PLUGIN_NAME, ATTRIBUTE_TYPE_NAME, ATTRIBUTE_TYPE_COMPARABLE, created_branch_id) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{attribute.getId(), attribute.getName(),
                            system, attributeType.getPluginName(),
                            attributeType.getTypeName(),
                            attributeType.isComparable(), branchId}, true);
        return attribute;
    }

    @Override
    public Element createElement(long qualifierId, long elementId) {

        throwExaptionIfNotCan(getAccessor().canCreateElement(qualifierId),
                "Can not create element.");

        if (elementId == -1) {
            elementId = nextValue("elements_sequence");
            long id = template.queryForLong("SELECT MAX(ELEMENT_ID) FROM "
                    + prefix + "elements;");
            while (id >= elementId) {
                elementId = template.nextVal(prefix + "elements_sequence");
            }
        }

        Element element = new Element(elementId, qualifierId, "");
        element.setId(elementId);
        long branchId = getActiveBranchId();
        Long l = (Long) template.queryForObjects("SELECT COUNT(*) FROM "
                        + prefix
                        + "elements WHERE element_id=? AND removed_branch_id=?",
                new RowMapper() {

                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return rs.getLong(1);
                    }
                }, new Object[]{elementId, branchId}, false);
        if (l.longValue() == 0)
            template.update(
                    "INSERT INTO "
                            + prefix
                            + "elements (ELEMENT_ID, ELEMENT_NAME, QUALIFIER_ID, created_branch_id) VALUES (?, ?, ?, ?)",
                    new Object[]{elementId, element.getName(), qualifierId,
                            branchId}, true);
        else {
            template.update(
                    "UPDATE "
                            + prefix
                            + "elements SET removed_branch_id=? WHERE ELEMENT_ID=? AND removed_branch_id=?",
                    new Object[]{Integer.MAX_VALUE, elementId, branchId}, false);
        }
        return element;
    }

    @Override
    protected Qualifier createQualifier(long qualifierId, boolean system) {
        if (!system)
            throwExaptionIfNotCan(getAccessor().canCreateQualifier(),
                    "Can not create qualifier.");

        if (qualifierId == -1) {
            qualifierId = nextValue("qualifiers_sequence");
            long id = template.queryForLong("SELECT MAX(QUALIFIER_ID) FROM "
                    + prefix + "qualifiers;");
            while (id >= qualifierId) {
                qualifierId = template.nextVal(prefix + "qualifiers_sequence");
            }
        }

        Qualifier qualifier = new Qualifier();
        qualifier.setId(qualifierId);
        qualifier.setSystem(system);

        long branchId = getActiveBranchId();
        Long l = (Long) template.queryForObjects("SELECT COUNT(*) FROM "
                        + prefix
                        + "qualifiers WHERE qualifier_id=? AND removed_branch_id=?",
                new RowMapper() {

                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return rs.getLong(1);
                    }
                }, new Object[]{qualifierId, branchId}, false);
        if (l.longValue() == 0l)
            template.update(
                    "INSERT INTO "
                            + prefix
                            + "qualifiers(QUALIFIER_ID, QUALIFIER_NAME, QUALIFIER_SYSTEM, created_branch_id) VALUES(?, ?, ?, ?)",
                    new Object[]{qualifierId, qualifier.getName(), system,
                            branchId}, true);
        else
            template.update(
                    "UPDATE "
                            + prefix
                            + "qualifiers SET removed_branch_id=? WHERE QUALIFIER_ID=? AND removed_branch_id=?",
                    new Object[]{Integer.MAX_VALUE, qualifierId, branchId}, false);
        return qualifier;
    }

    @Override
    public void deleteAttribute(final long id) {

        throwExaptionIfNotCan(getAccessor().canDeleteAttribute(id),
                "Can not delete attribute.");

        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                Transaction transaction = getAttributePropertyWhatWillBeDeleted(id);

                executeTransaction(transaction, connection);

                long branch = getActiveBranchId();

                PreparedStatement ps = connection
                        .prepareStatement("UPDATE "
                                + prefix
                                + "attributes SET removed_branch_id=? WHERE ATTRIBUTE_ID=?");
                ps.setLong(1, branch);
                ps.setLong(2, id);
                ps.execute();
                ps.close();
                return null;
            }
        });
    }

    @Override
    public void deleteElement(final long id) {

        throwExaptionIfNotCan(getAccessor()
                        .canDeleteElements(new long[]{id}),
                "Can not delete element.");

        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {

                Transaction[] transactions = getAttributeWhatWillBeDeleted(id);

                for (Transaction transaction : transactions) {
                    executeTransaction(transaction, connection);
                }

                PreparedStatement ps;

                ps = connection.prepareStatement("DELETE FROM " + prefix
                        + "formula_dependences WHERE source_element_id=?");
                ps.setLong(1, id);
                ps.execute();
                ps.close();

                ps = connection.prepareStatement("DELETE FROM " + prefix
                        + "formulas WHERE element_id=?");
                ps.setLong(1, id);
                ps.execute();
                ps.close();

                ps = connection
                        .prepareStatement("UPDATE "
                                + prefix
                                + "elements SET removed_branch_id=? WHERE ELEMENT_ID=?");
                long branch = getActiveBranchId();
                ps.setLong(1, branch);
                ps.setLong(2, id);
                ps.execute();
                ps.close();
                return null;
            }
        });

    }

    protected boolean insertIfNotPresent(String table, Object[] objects,
                                         Object[] values) throws SQLException {
        if (((Long) values[values.length - 1]).longValue() == 0)
            return false;
        StringBuffer select = new StringBuffer("SELECT " + objects[0]
                + " FROM " + table + " WHERE ");
        boolean f = true;
        for (int i = 0; i < values.length; i++) {
            if (f) {
                f = false;
            } else {
                select.append(" AND ");
            }
            select.append(objects[i]);
            select.append("=?");
        }
        PreparedStatement ps = template.getPreparedStatement(select.toString(),
                true);
        for (int i = 0; i < values.length; i++)
            ps.setObject(i + 1, values[i]);

        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            rs.close();
            StringBuffer insert = new StringBuffer("INSERT INTO " + table + "(");
            StringBuffer sb = new StringBuffer(") VALUES(");
            f = true;
            for (int i = 0; i < objects.length; i++) {
                if (f)
                    f = false;
                else {
                    insert.append(", ");
                    sb.append(", ");
                }
                insert.append(objects[i]);
                sb.append('?');
            }
            sb.append(")");
            insert.append(sb.toString());
            ps = template.getPreparedStatement(insert.toString(), true);
            for (int i = 0; i < values.length; i++)
                ps.setObject(i + 1, values[i]);
            ps.execute();
            return true;
        } else
            rs.close();
        return false;
    }

    @Override
    public void deleteQualifier(final long id) {

        throwExaptionIfNotCan(getAccessor().canDeleteQualifier(id),
                "Can not delete qualifier.");

        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                long branch = getActiveBranchId();
                PreparedStatement ps = connection
                        .prepareStatement("UPDATE "
                                + prefix
                                + "qualifiers SET removed_branch_id=? WHERE QUALIFIER_ID=?");
                ps.setLong(1, branch);
                ps.setLong(2, id);
                ps.execute();
                ps.close();
                return null;
            }
        });

        if (cached)
            qualifeirsCache.remove(id);

    }

    private class AttributeRowMapper implements RowMapper {

        private final long branch;

        public AttributeRowMapper(long branch) {
            this.branch = branch;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Attribute attribute = new Attribute();

            attribute.setId(rs.getLong("ATTRIBUTE_ID"));
            attribute.setName(rs.getString("ATTRIBUTE_NAME"));
            attribute.setSystem(rs.getBoolean("attribute_system"));

            if (rs.getObject("ATTRIBUTE_TYPE_PLUGIN_NAME") != null) {
                AttributeType type = new AttributeType(rs.getString(
                        "ATTRIBUTE_TYPE_PLUGIN_NAME").trim(), rs.getString(
                        "ATTRIBUTE_TYPE_NAME").trim(),
                        rs.getBoolean("ATTRIBUTE_TYPE_COMPARABLE"));
                AttributeType findType = findType(type.getPluginName(),
                        type.getTypeName());
                if (findType == null)
                    findType = type;
                attribute.setAttributeType(findType);
            }
            PreparedStatement ps = template
                    .getPreparedStatement(
                            "SELECT * FROM "
                                    + prefix
                                    + "attributes_history ah WHERE attribute_id=? AND created_branch_id IN (SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "attributes_history WHERE attribute_id=ah.attribute_id AND created_branch_id <=?)",
                            true);

            ps.setLong(1, attribute.getId());
            ps.setLong(2, branch);

            ResultSet rs1 = ps.executeQuery();
            if (rs1.next()) {
                attribute.setName(rs1.getString("attribute_name"));
            }
            rs1.close();

            return attribute;
        }
    }

    ;

    protected boolean isBranchLeaf() {
        if (activeBranchId == -1)
            return true;
        try {
            IS_BRANCH_LEAF_PS.setLong(1, activeBranchId);
            ResultSet rs = IS_BRANCH_LEAF_PS.executeQuery();
            rs.next();
            long c = rs.getLong(1);
            rs.close();
            return c <= 1l;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Attribute getAttribute(long attributeId) {
        if (cached) {
            Attribute attribute = attributesCache.get(attributeId);
            if (attribute != null)
                return attribute;
        }
        long branch = getActiveBranchId();
        Attribute attribute = (Attribute) template
                .queryForObjects(
                        "SELECT * FROM "
                                + prefix
                                + "attributes WHERE ATTRIBUTE_ID=? AND removed_branch_id>? "
                                + "AND created_branch_id <=?",
                        new AttributeRowMapper(branch), new Object[]{
                                attributeId, branch, branch}, true);
        if (attribute != null && cached) {
            attributesCache.put(attributeId, attribute);
        }
        return attribute;
    }

    public AttributeType findType(String pluginName, String typeName) {
        for (AttributeType type : getAttributeTypes()) {
            if ((type.getTypeName().equals(typeName))
                    && (type.getPluginName().equals(pluginName)))
                return type;
        }

        for (AttributeType type : getSystemAttributeTypes()) {
            if ((type.getTypeName().equals(typeName))
                    && (type.getPluginName().equals(pluginName)))
                return type;
        }
        System.err.println("WARNING: Type not found: " + pluginName + "."
                + typeName);
        return null;
    }

    @Override
    public AttributeType[] getAttributeTypes() {
        return factory.getAttributeTypes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Attribute> getAttributes() {
        long branch = getActiveBranchId();
        List<Attribute> res = template
                .query("SELECT * FROM "
                                + prefix
                                + "attributes WHERE ATTRIBUTE_SYSTEM=FALSE AND removed_branch_id>? "
                                + "AND created_branch_id<=? ORDER BY attribute_name",
                        new AttributeRowMapper(branch), new Object[]{branch,
                                branch}, true);
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Persistent>[] getBinaryAttribute(long elementId,
                                                 long attributeId) {

        throwExaptionIfNotCan(
                getAccessor().canReadElement(elementId, attributeId),
                "Can not get attribute.");

        Attribute attribute = getAttribute(attributeId);

        AttributePlugin plugin = factory.getAttributePlugin(attribute
                .getAttributeType());

        final Class<? extends Persistent>[] classes;

        if (elementId >= 0)
            classes = plugin.getAttributePersistents();
        else
            classes = plugin.getAttributePropertyPersistents();

        List<Persistent>[] lists = new List[classes.length];

        for (int i = 0; i < lists.length; i++) {

            final Class<? extends Persistent> clazz = classes[i];

            final PersistentRow row = metadata.get(clazz);
            final PersistentWrapper wrapper = wrappers.get(clazz);
            ArrayList<Object> params = new ArrayList<Object>(2);
            ArrayList<String> paramFields = new ArrayList<String>(2);

            plugin.fillAttributeQuery(row, attributeId, elementId, params,
                    paramFields, this);

            if (elementId >= 0l
                    && attribute.getAttributeType().getTypeName()
                    .equals("ElementList")
                    && attribute.getAttributeType().getPluginName()
                    .equals("Core")) {
                return getEListFixed(row, clazz, wrapper, paramFields, params,
                        getActiveBranchId());
            }

            long branchId = 0l;

            try {
                branchId = getBranch(prefix + "attributes_data_metadata",
                        new Object[]{"element_id", "attribute_id"},
                        new Object[]{elementId, attributeId},
                        getActiveBranchId());

            } catch (SQLException e1) {
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }

            params.add(branchId);
            paramFields.add("value_branch_id");

            StringBuffer sb = new StringBuffer("SELECT * FROM "
                    + row.getTableName());

            if (params.size() > 0) {
                sb.append(" WHERE ");
            }

            boolean first = true;

            for (int j = 0; j < params.size(); j++) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" AND ");
                }
                sb.append(paramFields.get(j));
                sb.append("=?");
            }

            List<Persistent> list = template.query(sb.toString(),
                    new RowMapper() {
                        @Override
                        public Object mapRow(ResultSet rs, int rowNum)
                                throws SQLException {
                            try {
                                Persistent persistent = clazz.newInstance();
                                for (PersistentField field : row.getFields()) {
                                    wrapper.setDatabaseField(persistent, field,
                                            rs);
                                }
                                return persistent;
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            throw new RuntimeException();
                        }
                    }, params.toArray(new Object[params.size()]), true);
            lists[i] = list;

        }
        return lists;
    }

    private List<Persistent>[] getEListFixed(final PersistentRow row,
                                             final Class<? extends Persistent> clazz,
                                             final PersistentWrapper wrapper, ArrayList<String> paramFields,
                                             ArrayList<Object> params, long branchId) {
        String sqlBranchCondition = " AND value_branch_id<=? "
                + "AND (removed_branch_id > ? OR removed_branch_id is NULL)";

        StringBuffer sb = new StringBuffer("SELECT * FROM "
                + row.getTableName());

        if (params.size() > 0) {
            sb.append(" WHERE ");
        }

        boolean first = true;

        for (int j = 0; j < params.size(); j++) {
            if (first) {
                first = false;
            } else {
                sb.append(" AND ");
            }
            sb.append(paramFields.get(j));
            sb.append("=?");
        }

        sb.append(sqlBranchCondition);
        params.add(branchId);
        params.add(branchId);

        return new List[]{template.query(sb.toString(), new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
                    Persistent persistent = clazz.newInstance();
                    for (PersistentField field : row.getFields()) {
                        wrapper.setDatabaseField(persistent, field, rs);
                    }
                    return persistent;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException();
            }
        }, params.toArray(), true)};
    }

    private long getBranch(String table, Object[] objects, Object[] values,
                           long branchId) throws SQLException {
        StringBuffer sql = new StringBuffer("SELECT MAX(branch_id) FROM "
                + table + " WHERE branch_id<=?");
        for (int i = 0; i < objects.length; i++) {
            sql.append(" AND ");
            sql.append(objects[i]);
            sql.append("=?");
        }

        PreparedStatement ps = template.getPreparedStatement(sql.toString(),
                true);
        ps.setLong(1, branchId);
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 2, values[i]);
        }

        ResultSet rs = ps.executeQuery();
        try {
            if (rs.next())
                return rs.getLong(1);
            return 0l;
        } finally {
            rs.close();
        }
    }

    @Override
    public List<Persistent>[] getBinaryBranchAttribute(long elementId,
                                                       long attributeId, long branchId) {
        throwExaptionIfNotCan(
                getAccessor().canReadElement(elementId, attributeId),
                "Can not get attribute.");

        Attribute attribute = getAttribute(attributeId);

        AttributePlugin plugin = factory.getAttributePlugin(attribute
                .getAttributeType());

        final Class<? extends Persistent>[] classes;

        if (elementId >= 0)
            classes = plugin.getAttributePersistents();
        else
            classes = plugin.getAttributePropertyPersistents();

        List<Persistent>[] lists = new List[classes.length];

        for (int i = 0; i < lists.length; i++) {

            final Class<? extends Persistent> clazz = classes[i];

            final PersistentRow row = metadata.get(clazz);
            final PersistentWrapper wrapper = wrappers.get(clazz);
            ArrayList<Object> params = new ArrayList<Object>(3);
            ArrayList<String> paramFields = new ArrayList<String>(3);

            plugin.fillAttributeQuery(row, attributeId, elementId, params,
                    paramFields, this);
            if (elementId >= 0l
                    && attribute.getAttributeType().getTypeName()
                    .equals("ElementList")
                    && attribute.getAttributeType().getPluginName()
                    .equals("Core")) {
                return getEListFixed(row, clazz, wrapper, paramFields, params,
                        getActiveBranchId());
            }
            params.add(branchId);
            paramFields.add("value_branch_id");

            StringBuffer sb = new StringBuffer("SELECT * FROM "
                    + row.getTableName());

            if (params.size() > 0) {
                sb.append(" WHERE ");
            }

            boolean first = true;

            for (int j = 0; j < params.size(); j++) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" AND ");
                }
                sb.append(paramFields.get(j));
                sb.append("=?");
            }

            List<Persistent> list = template.query(sb.toString(),
                    new RowMapper() {
                        @Override
                        public Object mapRow(ResultSet rs, int rowNum)
                                throws SQLException {
                            try {
                                Persistent persistent = clazz.newInstance();
                                for (PersistentField field : row.getFields()) {
                                    wrapper.setDatabaseField(persistent, field,
                                            rs);
                                }
                                return persistent;
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            throw new RuntimeException();
                        }
                    }, params.toArray(new Object[params.size()]), true);
            lists[i] = list;

        }
        return lists;
    }

    @Override
    public Hashtable<Element, List<Persistent>[][]> getBinaryElements(
            long qualifierId, final long[] attributeIds) {

        for (long attributeId : attributeIds)
            throwExaptionIfNotCan(
                    getAccessor().canReadAttribute(qualifierId, attributeId),
                    "Can not get attribute for qualifier.");

        List<Element> elements = getElements(qualifierId);
        Hashtable<Element, List<Persistent>[][]> result = new Hashtable<Element, List<Persistent>[][]>();
        final Hashtable<Long, List<Persistent>[][]> values = new Hashtable<Long, List<Persistent>[][]>();

        int index = 0;

        final int[] persistentCount = new int[attributeIds.length];

        for (int i = 0; i < attributeIds.length; i++) {
            Attribute attribute = getAttribute(attributeIds[i]);

            AttributePlugin plugin = factory.getAttributePlugin(attribute
                    .getAttributeType());

            persistentCount[i] = plugin.getAttributePersistents().length;
        }

        for (long attributeId : attributeIds) {
            final int attrIndex = index;
            Attribute attribute = getAttribute(attributeId);

            AttributePlugin plugin = factory.getAttributePlugin(attribute
                    .getAttributeType());

            final Class<? extends Persistent>[] classes;

            classes = plugin.getAttributePersistents();

            for (int i = 0; i < classes.length; i++) {

                final Class<? extends Persistent> clazz = classes[i];

                final int listIndex = i;

                final PersistentRow row = metadata.get(clazz);
                final PersistentWrapper wrapper = wrappers.get(clazz);
                ArrayList<Object> params = new ArrayList<Object>(2);
                ArrayList<String> paramFields = new ArrayList<String>(2);

                plugin.fillAttributeQuery(row, attributeId, -1l, params,
                        paramFields, this);

                StringBuffer sb = new StringBuffer("SELECT * FROM "
                        + row.getTableName() + " main_table");

                StringBuffer where = new StringBuffer(" AND attribute_id=?");
                for (int j = 0; j < params.size(); j++)
                    if (paramFields.get(j).startsWith("element")
                            && paramFields.get(j).endsWith("id")) {
                        where.append(" AND main_table.");
                        where.append(paramFields.get(j));
                        where.append("=element_id");
                    }

                for (PersistentField field : row.getFields()) {
                    if (field.isAutoset()) {
                        if (field.getType() == PersistentField.ELEMENT) {
                            where.append(" AND main_table.");
                            where.append(field.getDatabaseName());
                            where.append("=element_id");

                        }
                    }
                }

                sb.append(" WHERE (value_branch_id IN (SELECT MAX(branch_id) FROM "
                        + prefix
                        + "attributes_data_metadata WHERE branch_id<=? "
                        + where
                        + ") OR ((SELECT MAX(branch_id) FROM "
                        + prefix
                        + "attributes_data_metadata WHERE branch_id<=? "
                        + where + ") IS NULL AND value_branch_id=0))");

                for (int j = 0; j < params.size(); j++) {
                    sb.append(" AND ");
                    sb.append(paramFields.get(j));
                    sb.append("=?");
                }

                params.add(0, getActiveBranchId());
                params.add(1, attributeId);
                params.add(2, getActiveBranchId());
                params.add(3, attributeId);

                template.query(sb.toString(), new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        try {
                            Persistent persistent = clazz.newInstance();
                            long id = -1;
                            for (PersistentField field : row.getFields()) {
                                wrapper.setDatabaseField(persistent, field, rs);
                                if ((field.isAutoset())
                                        && (field.getType() == PersistentField.ELEMENT)) {
                                    id = rs.getLong(field.getDatabaseName());
                                }
                            }

                            getLists(values, id, attributeIds.length,
                                    persistentCount)[attrIndex][listIndex]
                                    .add(persistent);

                            return null;
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        throw new RuntimeException();
                    }
                }, params.toArray(new Object[params.size()]), false);
            }
            index++;
        }

        for (Element element : elements) {
            result.put(
                    element,
                    getLists(values, element.getId(), attributeIds.length,
                            persistentCount));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<Persistent>[][] getLists(
            final Hashtable<Long, List<Persistent>[][]> values, long id,
            int attrCount, int[] persistentCount) {
        List<Persistent>[][] res = values.get(id);
        if (res == null) {
            res = new List[attrCount][];
            for (int i = 0; i < attrCount; i++) {
                List[] lists = new List[persistentCount[i]];
                res[i] = lists;
                for (int j = 0; j < persistentCount[i]; j++)
                    lists[j] = new ArrayList<Persistent>(1);
            }
            values.put(id, res);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Element> getElements(long qualifierId) {
        throwExaptionIfNotCan(getAccessor().canReadQualifier(qualifierId),
                "Can not get qualifier.");

        Qualifier qualifier = getQualifier(qualifierId);
        if (qualifier == null)
            return new ArrayList<Element>(0);
        long attr = qualifier.getAttributeForName();

        String where = " AND attribute_id=" + attr
                + " AND elmts.element_id=element_id";

        long branchId = getActiveBranchId();
        return template
                .query("SELECT elmts.ELEMENT_ID, elmts.QUALIFIER_ID, (SELECT value FROM "
                                + prefix
                                + "attribute_texts WHERE element_id=elmts.element_id AND attribute_id="
                                + attr
                                + " "
                                + " AND (value_branch_id IN (SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id<=? "
                                + where
                                + ") OR ((SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id <= ?"
                                + where
                                + ") IS NULL AND value_branch_id=0))) AS ELEMENT_TEXT_NAME FROM "
                                + prefix
                                + "elements elmts, "
                                + prefix
                                + "qualifiers q WHERE elmts.QUALIFIER_ID=? AND q.qualifier_id=elmts.qualifier_id AND elmts.created_branch_id <=? "
                                + "AND (elmts.removed_branch_id >?) ORDER BY ELEMENT_ID",
                        new ElementRowMapper(), new Object[]{branchId,
                                branchId, qualifierId, branchId, branchId},
                        true);
    }

    private class ElementRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Element(rs.getLong("ELEMENT_ID"),
                    rs.getLong("QUALIFIER_ID"),
                    rs.getString("ELEMENT_TEXT_NAME"));
        }

    }

    @Override
    public Qualifier getQualifier(long qualifierId) {
        throwExaptionIfNotCan(getAccessor().canReadQualifier(qualifierId),
                "Can not get qualifier.");
        long branch = getActiveBranchId();
        return (Qualifier) template
                .queryForObjects(
                        "SELECT * FROM "
                                + prefix
                                + "qualifiers WHERE QUALIFIER_ID=? AND removed_branch_id>? AND created_branch_id<=?",
                        new QualifierRowMapper(branch), new Object[]{
                                qualifierId, branch, branch}, true);
    }

    private class QualifierRowMapper implements RowMapper {

        private final long branch;

        public QualifierRowMapper(long branch) {
            this.branch = branch;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            if (cached) {
                Qualifier q = qualifeirsCache.get(rs.getLong("QUALIFIER_ID"));
                if (q != null)
                    return q.createSaveCopy();
            }

            Qualifier qualifier = new Qualifier();
            fillFields(rs, qualifier);
            qualifier.setSystem(rs.getBoolean("QUALIFIER_SYSTEM"));

            long branch1 = 0l;

            if (rs.getObject("ATTRIBUTE_FOR_NAME") != null) {
                qualifier.setAttributeForName(rs.getLong("ATTRIBUTE_FOR_NAME"));
            }

            PreparedStatement ps = template
                    .getPreparedStatement(
                            "SELECT * FROM "
                                    + prefix
                                    + "qualifiers_history qh WHERE qualifier_id=? AND created_branch_id IN (SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "qualifiers_history WHERE qualifier_id=qh.qualifier_id AND created_branch_id <=?)",
                            true);

            ps.setLong(1, qualifier.getId());
            ps.setLong(2, branch);

            ResultSet rs1 = ps.executeQuery();
            if (rs1.next()) {
                branch1 = rs1.getLong("created_branch_id");
                fillFields(rs1, qualifier);
            }

            rs1.close();

            loadAttributes(qualifier.getAttributes(), false, qualifier.getId(),
                    branch1);
            loadAttributes(qualifier.getSystemAttributes(), true,
                    qualifier.getId(), branch1);
            if (cached) {
                qualifeirsCache.put(qualifier.getId(), qualifier);
                return qualifier.createSaveCopy();
            } else
                return qualifier;
        }

        private void fillFields(ResultSet rs, Qualifier qualifier)
                throws SQLException {
            qualifier.setId(rs.getLong("QUALIFIER_ID"));
            qualifier.setName(rs.getString("QUALIFIER_NAME"));
            qualifier.setAttributeForName(rs.getLong("attribute_for_name"));
        }
    }

    ;

    @Override
    public long getQualifierIdForElement(long elementId) {
        Long res = (Long) template.queryForObject("SELECT * FROM " + prefix
                + "elements WHERE ELEMENT_ID=?", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getLong("QUALIFIER_ID");
            }
        }, elementId, true);
        if (res == null)
            return -1l;
        return res;
    }

    private void loadAttributes(final List<Attribute> attributes,
                                boolean system, long id, long branch) {
        attributes
                .addAll(template
                        .query("SELECT * FROM "
                                        + prefix
                                        + "qualifiers_attributes qh WHERE ATTRIBUTE_SYSTEM=? AND QUALIFIER_ID=? AND created_branch_id IN "
                                        + "(SELECT MAX(created_branch_id) FROM "
                                        + prefix
                                        + "qualifiers_attributes WHERE qualifier_id=qh.qualifier_id AND ATTRIBUTE_SYSTEM=qh.ATTRIBUTE_SYSTEM AND created_branch_id <=?)"
                                        + " ORDER BY attribute_position",
                                new RowMapper() {
                                    @Override
                                    public Object mapRow(ResultSet rs,
                                                         int rowNum) throws SQLException {
                                        return getAttribute(rs
                                                .getLong("ATTRIBUTE_ID"));
                                    }
                                }, new Object[]{system, id, branch}, true));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Qualifier> getQualifiers(boolean system) {
        long branch = getActiveBranchId();
        List<Qualifier> list = template
                .query("SELECT * FROM "
                                + prefix
                                + "qualifiers WHERE QUALIFIER_SYSTEM=? AND created_branch_id <=? "
                                + "AND removed_branch_id>? ORDER BY qualifier_name",
                        new QualifierRowMapper(branch), new Object[]{system,
                                branch, branch}, true);

        List<Qualifier> res = new ArrayList<Qualifier>();

        for (Qualifier qualifier : list)
            if (getAccessor().canReadQualifier(qualifier.getId()))
                res.add(qualifier);
        return res;
    }

    @Override
    public AttributeType[] getSystemAttributeTypes() {
        return factory.getSystemAttributeTypes();
    }

    // @SuppressWarnings("unused")
    @Override
    public boolean setBinaryAttribute(final long elementId,
                                      final long attributeId, final Transaction transaction) {

        if (elementId >= 0) {

            long qualifierId = getQualifierIdForElement(elementId);

            Qualifier qualifier = getQualifier(qualifierId);

            boolean attr = false;

            if ((qualifier == null) && (Metadata.DEBUG)) {
                System.err
                        .println("WARNING: Can not set attribute for not existing element");
                throw new RuntimeException(
                        "WARNING: Can not set attribute for not existing element");

            }

            for (Attribute attribute : qualifier.getAttributes()) {
                if (attributeId == attribute.getId()) {
                    attr = true;
                }
            }

            for (Attribute attribute : qualifier.getSystemAttributes()) {
                if (attributeId == attribute.getId()) {
                    attr = true;
                }
            }

            if (!attr) {
                throw new RuntimeException("Attribute with id: " + attributeId
                        + " not found for qualifier: " + qualifier);
            }
        }

        Attribute propertyAttribute = getAttribute(attributeId);
        if (elementId < 0l
                && propertyAttribute != null
                && propertyAttribute.getAttributeType().toString()
                .equals("Core.Variant")) {
        } else if (!getAccessor().canUpdateElement(elementId, attributeId))
            return false;

        process(transaction.getDelete(), elementId, attributeId);
        process(transaction.getUpdate(), elementId, attributeId);
        process(transaction.getSave(), elementId, attributeId);

        if (elementId >= 0
                && propertyAttribute.getAttributeType().getTypeName()
                .equals("ElementList")
                && propertyAttribute.getAttributeType().getPluginName()
                .equals("Core")) {
            try {
                setElistFix(transaction);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        return (Boolean) template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                executeTransaction(transaction, connection);
                long branch = getActiveBranchId();
                Boolean res = Boolean.FALSE;
                if (branch > 0l) {
                    PreparedStatement ps = template
                            .getPreparedStatement(
                                    "SELECT branch_id FROM "
                                            + prefix
                                            + "attributes_data_metadata WHERE branch_id=? AND attribute_id=? AND element_id=?",
                                    true);
                    ps.setLong(1, branch);
                    ps.setLong(2, attributeId);
                    ps.setLong(3, elementId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next() && !transaction.isRemoveBranchInfo()) {
                        rs.close();
                        ps = template
                                .getPreparedStatement(
                                        "INSERT INTO "
                                                + prefix
                                                + "attributes_data_metadata(element_id, attribute_id, branch_id) VALUES(?, ?, ?)",
                                        true);
                        ps.setLong(1, elementId);
                        ps.setLong(2, attributeId);
                        ps.setLong(3, branch);
                        ps.execute();
                        res = Boolean.TRUE;
                    } else {
                        rs.close();
                        if (transaction.isRemoveBranchInfo()) {
                            ps = template
                                    .getPreparedStatement(
                                            "DELETE FROM "
                                                    + prefix
                                                    + "attributes_data_metadata WHERE branch_id=? AND attribute_id=? AND element_id=?",
                                            false);
                            ps.setLong(1, branch);
                            ps.setLong(2, attributeId);
                            ps.setLong(3, elementId);
                            ps.execute();
                            ps.close();
                        }
                    }
                }
                return res;
            }
        });

    }

    private void setElistFix(Transaction transaction) {
        long branchId = getActiveBranchId();
        String delete = "DELETE FROM "
                + prefix
                + "attribute_element_lists WHERE attribute_id=? AND element1_id=? AND element2_id = ? AND value_branch_id=?";
        String remove = "UPDATE "
                + prefix
                + "attribute_element_lists SET connection_type=?, removed_branch_id=? WHERE attribute_id=? AND element1_id=? AND element2_id = ? AND value_branch_id=?";
        String insert = "INSERT INTO "
                + prefix
                + "attribute_element_lists (connection_type, attribute_id, element1_id, element2_id, value_branch_id) VALUES (?, ?, ?, ?, ?)";

        for (Persistent p : transaction.getDelete()) {
            Class<? extends Persistent> clazz = p.getClass();
            PersistentWrapper wrapper = wrappers.get(clazz);
            long bId = (Long) wrapper.getField(p, "valueBranchId");
            long aId = (Long) wrapper.getField(p, "attributeId");
            long e1Id = (Long) wrapper.getField(p, "element1Id");
            long e2Id = (Long) wrapper.getField(p, "element2Id");
            String connectionType = (String) wrapper.getField(p,
                    "connectionType");
            if (bId == branchId) {
                template.update(delete, new Object[]{aId, e1Id, e2Id, bId},
                        false);
            } else {
                template.update(remove, new Object[]{connectionType,
                        branchId, aId, e1Id, e2Id, bId}, false);
            }
        }

        for (Persistent p : transaction.getSave()) {
            Class<? extends Persistent> clazz = p.getClass();
            PersistentWrapper wrapper = wrappers.get(clazz);
            long aId = (Long) wrapper.getField(p, "attributeId");
            long e1Id = (Long) wrapper.getField(p, "element1Id");
            long e2Id = (Long) wrapper.getField(p, "element2Id");
            String connectionType = (String) wrapper.getField(p,
                    "connectionType");
            template.update(insert, new Object[]{connectionType, aId, e1Id,
                    e2Id, branchId}, false);
        }

    }

    private void process(List<Persistent> list, long elementId, long attributeId) {
        for (Persistent persistent : list) {
            process(elementId, attributeId, persistent);
        }
    }

    private void process(long elementId, long attributeId, Persistent persistent) {
        Class<? extends Persistent> clazz = persistent.getClass();
        PersistentRow row = metadata.get(clazz);
        PersistentWrapper wrapper = wrappers.get(clazz);
        for (PersistentField field : row.getFields()) {
            if (field.isAutoset()) {
                if (field.getType() == PersistentField.ELEMENT) {
                    wrapper.setField(persistent, field.getName(), elementId);
                } else if (field.getType() == PersistentField.ATTRIBUTE) {
                    wrapper.setField(persistent, field.getName(), attributeId);
                } else if (field.getType() == PersistentField.ID) {
                    long id = (Long) wrapper.getField(persistent,
                            field.getName());
                    if (id <= 0) {
                        wrapper.setField(persistent, field.getName(), template
                                .nextVal(field.getSequenceName(row
                                        .getTableName())));
                    }
                }
            }
        }
    }

    private void executeTransaction(final Transaction transaction,
                                    Connection connection) throws SQLException {
        for (Persistent key : transaction.getUpdate()) {
            PersistentRow row = metadata.get(key.getClass());
            PersistentWrapper wrapper = wrappers.get(key.getClass());

            StringBuffer sb = new StringBuffer("UPDATE " + row.getTableName()
                    + " SET ");

            boolean first = true;

            List<Object> list = new ArrayList<Object>(5);

            for (PersistentField field : row.getDataFields()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(field.getDatabaseName());
                sb.append("=?");
                list.add(wrapper.getField(key, field.getName()));
            }

            sb.append(" WHERE ");

            first = true;

            for (PersistentField field : row.getKeyFields()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" AND ");
                }
                sb.append(field.getDatabaseName());
                sb.append("=?");
                list.add(wrapper.getField(key, field.getName()));
            }
            PreparedStatement ps = template.getPreparedStatement(sb.toString(),
                    true);

            template.setParams(ps, list.toArray(new Object[list.size()]));
            ps.execute();
            // ps.close();
        }

        for (Persistent key : transaction.getDelete()) {
            PersistentRow row = metadata.get(key.getClass());
            PersistentWrapper wrapper = wrappers.get(key.getClass());

            StringBuffer sb = new StringBuffer("DELETE FROM "
                    + row.getTableName() + " WHERE ");

            boolean first = true;
            first = true;

            List<Object> list = new ArrayList<Object>(2);

            for (PersistentField field : row.getKeyFields()) {
                if (first) {
                    first = false;
                } else {
                    sb.append("AND ");
                }
                sb.append(field.getDatabaseName());
                sb.append("=?");
                list.add(wrapper.getField(key, field.getName()));
            }
            PreparedStatement ps = template.getPreparedStatement(sb.toString(),
                    true);// connection.prepareStatement(sb.toString());

            template.setParams(ps, list.toArray(new Object[list.size()]));
            ps.execute();
            // ps.close();
        }

        for (Persistent key : transaction.getSave()) {
            PersistentRow row = metadata.get(key.getClass());
            PersistentWrapper wrapper = wrappers.get(key.getClass());

            StringBuffer sb = new StringBuffer("INSERT INTO "
                    + row.getTableName() + "(");

            boolean first = true;

            List<Object> list = new ArrayList<Object>(5);

            for (PersistentField field : row.getFields()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(field.getDatabaseName());
                list.add(wrapper.getField(key, field.getName()));
            }

            sb.append(") VALUES(");
            first = true;
            for (int i = 0; i < list.size(); i++) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append("?");
            }

            sb.append(")");

            PreparedStatement ps = template.getPreparedStatement(sb.toString(),
                    true);// connection.prepareStatement(sb.toString());

            template.setParams(ps, list.toArray(new Object[list.size()]));
            ps.execute();
            // ps.close();
        }
    }

    @Override
    public void updateAttribute(Attribute attribute) {

        throwExaptionIfNotCan(
                getAccessor().canUpdateAttribute(attribute.getId()),
                "Can not update attribute.");

        long branch = getActiveBranchId();

        if (getActiveBranchId() == 0l) {
            template.update(
                    "UPDATE "
                            + prefix
                            + "attributes SET ATTRIBUTE_NAME=?, ATTRIBUTE_TYPE_PLUGIN_NAME=?, "
                            + "ATTRIBUTE_TYPE_NAME=?, ATTRIBUTE_TYPE_COMPARABLE=? WHERE ATTRIBUTE_ID=?",
                    new Object[]{attribute.getName(),
                            attribute.getAttributeType().getPluginName(),
                            attribute.getAttributeType().getTypeName(),
                            attribute.getAttributeType().isComparable(),
                            attribute.getId()}, true);
        } else {
            if (needInsert(
                    "SELECT COUNT(*) FROM "
                            + prefix
                            + "attributes_history WHERE attribute_id=? AND created_branch_id=?",
                    new Object[]{attribute.getId(), branch})) {
                template.update(
                        "INSERT INTO "
                                + prefix
                                + "attributes_history(ATTRIBUTE_ID, ATTRIBUTE_NAME, created_branch_id)"
                                + " VALUES(?, ?, ?)",
                        new Object[]{attribute.getId(), attribute.getName(),
                                branch}, true);
            } else {
                template.update(
                        "UPDATE "
                                + prefix
                                + "attributes_history SET ATTRIBUTE_NAME=? WHERE ATTRIBUTE_ID=? AND created_branch_id=?",
                        new Object[]{attribute.getName(), attribute.getId(),
                                branch}, true);
            }
        }
    }

    @Override
    public void updateQualifier(Qualifier qualifier) {

        throwExaptionIfNotCan(
                getAccessor().canUpdateQualifier(qualifier.getId()),
                "Can not update qualifier.");
        if (cached)
            qualifeirsCache.remove(qualifier.getId());
        long branch = getActiveBranchId();
        if (branch == 0l)
            template.update(
                    "UPDATE "
                            + prefix
                            + "qualifiers SET QUALIFIER_NAME=?, ATTRIBUTE_FOR_NAME=? WHERE QUALIFIER_ID=?",
                    new Object[]{qualifier.getName(),
                            qualifier.getAttributeForName(), qualifier.getId()},
                    true);
        else {
            if (needInsert(
                    "SELECT COUNT(*) FROM "
                            + prefix
                            + "qualifiers_history WHERE qualifier_id=? AND created_branch_id=?",
                    new Object[]{qualifier.getId(), branch})) {
                template.update(
                        "INSERT INTO "
                                + prefix
                                + "qualifiers_history(QUALIFIER_ID, QUALIFIER_NAME, ATTRIBUTE_FOR_NAME, created_branch_id)"
                                + " VALUES(?, ?, ?, ?)", new Object[]{
                                qualifier.getId(), qualifier.getName(),
                                qualifier.getAttributeForName(), branch}, true);
            } else {
                template.update(
                        "UPDATE "
                                + prefix
                                + "qualifiers_history SET QUALIFIER_NAME=?, ATTRIBUTE_FOR_NAME=? WHERE QUALIFIER_ID=? AND created_branch_id=?",
                        new Object[]{qualifier.getName(),
                                qualifier.getAttributeForName(),
                                qualifier.getId(), branch}, true);
            }
        }
        updateAttributes(qualifier.getAttributes(), false, qualifier.getId(),
                branch);
        updateAttributes(qualifier.getSystemAttributes(), true,
                qualifier.getId(), branch);
        if (cached)
            qualifeirsCache.put(qualifier.getId(), qualifier.createSaveCopy());
    }

    private void updateAttributes(List<Attribute> attributes,
                                  final boolean system, final long qualifierId, final long branch) {
        List<Attribute> olds = new ArrayList<Attribute>();
        loadAttributes(olds, system, qualifierId, branch);
        for (final Attribute old : olds) {
            if (attributes.indexOf(old) < 0) {

                template.execute(new JDBCCallback() {
                    @Override
                    public Object execute(Connection connection)
                            throws SQLException {

                        Hashtable<Element, Transaction> hash = getAttributeWhatWillBeDeleted(
                                qualifierId, old.getId());

                        for (Transaction transaction : hash.values()) {
                            executeTransaction(transaction, connection);
                        }

                        PreparedStatement ps;

                        ps = connection
                                .prepareStatement("DELETE FROM "
                                        + prefix
                                        + "formula_dependences WHERE source_attribute_id=? AND source_element_id IN (SELECT element_id FROM "
                                        + prefix
                                        + "elements WHERE qualifier_id=?)");
                        ps.setLong(1, old.getId());
                        ps.setLong(2, qualifierId);
                        ps.execute();
                        ps.close();

                        ps = connection
                                .prepareStatement("DELETE FROM "
                                        + prefix
                                        + "formulas WHERE attribute_id=? AND element_id IN (SELECT element_id FROM "
                                        + prefix
                                        + "elements WHERE qualifier_id=?)");
                        ps.setLong(1, old.getId());
                        ps.setLong(2, qualifierId);
                        ps.execute();
                        ps.close();

                        return null;
                    }
                });

            }
        }
        long position = 0;
        template.execute(new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection
                        .prepareStatement("DELETE FROM "
                                + prefix
                                + "qualifiers_attributes WHERE QUALIFIER_ID=? AND ATTRIBUTE_SYSTEM=? AND created_branch_id=?");
                ps.setLong(1, qualifierId);
                ps.setBoolean(2, system);
                ps.setLong(3, branch);
                ps.execute();
                ps.close();
                return null;
            }
        });
        for (Attribute a : attributes) {

            // if (olds.indexOf(a) < 0) {
            template.update(
                    "INSERT INTO "
                            + prefix
                            + "qualifiers_attributes(QUALIFIER_ID, ATTRIBUTE_ID, ATTRIBUTE_SYSTEM, ATTRIBUTE_POSITION, created_branch_id) "
                            + "VALUES(?, ?, ?, ?, ?)", new Object[]{
                            qualifierId, a.getId(), system, position, branch},
                    true);
            /*
             * } else { template.update( "UPDATE " + prefix +
			 * "qualifiers_attributes SET ATTRIBUTE_POSITION=? WHERE QUALIFIER_ID=? AND ATTRIBUTE_ID=? AND created_branch_id=?"
			 * , new Object[] { position, qualifierId, a.getId(), branch },
			 * true); }
			 */
            position++;
        }
    }

    private boolean needInsert(String sql, Object[] keys) {
        return (Boolean) template.queryForObjects(sql, new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getLong(1) == 0l;
            }
        }, keys, true);
    }

    private Transaction getAttributeWhatWillBeDeleted(long elementId,
                                                      Attribute attribute, boolean attributeDeteting) {
        final Transaction transaction = new Transaction();

        AttributePlugin plugin = factory.getAttributePlugin(attribute
                .getAttributeType());

        final Class<? extends Persistent>[] classes;

        if (elementId >= 0)
            classes = plugin.getAttributePersistents();
        else
            classes = plugin.getAttributePropertyPersistents();

        for (int i = 0; i < classes.length; i++) {
            final Class<? extends Persistent> clazz = classes[i];
            final PersistentRow row = metadata.get(clazz);
            final PersistentWrapper wrapper = wrappers.get(clazz);

            ArrayList<Object> params = new ArrayList<Object>(2);
            ArrayList<String> paramFields = new ArrayList<String>(2);

            boolean delete = false;

            for (PersistentField field : row.getFields()) {
                if (field.isAutoset()) {
                    if (field.getType() == PersistentField.ELEMENT) {
                        params.add(elementId);
                        paramFields.add(field.getDatabaseName());
                        if (!attributeDeteting)
                            delete = true;
                    } else if (field.getType() == PersistentField.ATTRIBUTE) {
                        params.add(attribute.getId());
                        paramFields.add(field.getDatabaseName());
                        if (attributeDeteting)
                            delete = true;
                    } else if (field.getType() == PersistentField.VALUE_BRANCH_ID) {
                        params.add(getActiveBranchId());
                        paramFields.add(field.getDatabaseName());
                        if (attributeDeteting)
                            delete = true;
                    }
                }
            }
            if (delete) {
                StringBuffer sb = new StringBuffer("SELECT * FROM "
                        + row.getTableName() + " WHERE ");

                boolean first = true;

                for (int x = 0; x < params.size(); x++) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(" AND ");
                    }
                    sb.append(paramFields.get(x));
                    sb.append("=?");
                }

                template.query(sb.toString(), new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        try {
                            Persistent persistent = clazz.newInstance();
                            for (PersistentField field : row.getFields()) {
                                wrapper.setDatabaseField(persistent, field, rs);
                            }

                            transaction.getDelete().add(persistent);
                            return null;
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        throw new RuntimeException();
                    }
                }, params.toArray(new Object[params.size()]), true);
            }
        }

        transaction.setRemoveBranchInfo(true);
        return transaction;
    }

    @Override
    public Transaction[] getAttributeWhatWillBeDeleted(long elementId) {

        throwExaptionIfNotCan(
                getAccessor().canDeleteElements(new long[]{elementId}),
                "Can not get attributes what will be deleted, as you can not delete element.");

        Qualifier qualifier = getQualifier(getQualifierIdForElement(elementId));
        if (qualifier == null)
            return new Transaction[]{};

        List<Attribute> attrs = qualifier.getAttributes();

        attrs.addAll(qualifier.getSystemAttributes());

        Transaction[] res = new Transaction[attrs.size()];

        for (int i = 0; i < attrs.size(); i++) {

            Attribute attribute = attrs.get(i);
            res[i] = getAttributeWhatWillBeDeleted(elementId, attribute, false);

        }

        return res;
    }

    @Override
    public Transaction getAttributePropertyWhatWillBeDeleted(long attributeId) {
        throwExaptionIfNotCan(getAccessor().canDeleteAttribute(attributeId),
                "Can not get attributes what will be deleted, as can not delete element.");

        Attribute attribute = getAttribute(attributeId);

        return getAttributeWhatWillBeDeleted(-1, attribute, true);
    }

    @Override
    public Hashtable<Element, Transaction> getAttributeWhatWillBeDeleted(
            long qualifierId, long attributeId) {

        throwExaptionIfNotCan(getAccessor().canUpdateQualifier(qualifierId),
                "Can not get attributes what will be deleted, as can not update qualifier.");

        List<Element> elements = getElements(qualifierId);
        Hashtable<Element, Transaction> transactions = new Hashtable<Element, Transaction>();
        Attribute attribute = getAttribute(attributeId);

        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            Transaction transaction = getAttributeWhatWillBeDeleted(
                    element.getId(), attribute, true);
            if (transaction.getDelete().size() > 0) {
                transactions.put(element, transaction);
            }
        }

        return transactions;
    }

    /**
     * @return the accessor
     */
    public AccessRules getAccessor() {
        return accessor;
    }

    @Override
    public Element getElement(long id) {
        throwExaptionIfNotCan(getAccessor().canReadQualifier(id),
                "Can not get element.");

        Qualifier qualifier = getQualifier(getQualifierIdForElement(id));
        if (qualifier == null)
            return null;
        long attr = qualifier.getAttributeForName();

        String where = " AND attribute_id=" + attr
                + " AND elmts.element_id=element_id";

        long branchId = getActiveBranchId();

        return (Element) template
                .queryForObjects(
                        "SELECT ELEMENT_ID, elmts.QUALIFIER_ID, (SELECT value FROM "
                                + prefix
                                + "attribute_texts WHERE element_id=elmts.element_id AND attribute_id="
                                + attr
                                + " "
                                + " AND (value_branch_id IN (SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id <=?"
                                + where
                                + ") OR ((SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id<=? "
                                + where
                                + ") IS NULL AND value_branch_id=0))) AS ELEMENT_TEXT_NAME  FROM "
                                + prefix
                                + "elements elmts, "
                                + prefix
                                + "qualifiers q WHERE ELEMENT_ID=? AND elmts.qualifier_id=q.qualifier_id AND elmts.created_branch_id<= ? "
                                + "AND elmts.removed_branch_id >?",
                        new ElementRowMapper(), new Object[]{branchId,
                                branchId, id, branchId, branchId}, true);
    }

    @Override
    public boolean deleteStream(String path) {
        if (path.startsWith("/elements")) {
            if (!canUpdateElement(path)) {
                throw new RuntimeException("You can not update path " + path);
            }
        }

        throwExaptionIfNotCan(getAccessor().canUpdateStream(path),
                "Can not update stream.");

        boolean res = deleteStreamBytes(path);
        if (res) {
            template.update("DELETE FROM " + prefix
                    + "streams WHERE STREAM_ID=?", new Object[]{path}, true);
        }
        return res;
    }

    private boolean canUpdateElement(String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        st.nextElement();
        long elementId = Long.parseLong(st.nextToken());
        long attributeId = Long.parseLong(st.nextToken());
        return accessor.canUpdateElement(elementId, attributeId);
    }

    protected boolean canReadElement(String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        st.nextElement();
        long elementId = Long.parseLong(st.nextToken());
        long attributeId = Long.parseLong(st.nextToken());
        return accessor.canReadElement(elementId, attributeId);
    }

    protected abstract boolean deleteStreamBytes(String path);

    @SuppressWarnings("unchecked")
    @Override
    public String[] getStreamNames() {
        List<String> list = template.query("SELECT * FROM " + prefix
                + "streams", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("STREAM_ID");
            }
        });
        return list.toArray(new String[list.size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setStream(String path, byte[] bytes) {
        if (!path.startsWith("/"))
            throw new RuntimeException("Path should start with / symbol.");

        if (path.startsWith("/elements")) {
            if (!canUpdateElement(path)) {
                throw new RuntimeException("You can not update path " + path);
            }
        }

        throwExaptionIfNotCan(getAccessor().canUpdateStream(path),
                "Can not update stream.");

        writeStream(path, bytes);
        List<String> list = template.query("SELECT * FROM " + prefix
                + "streams WHERE STREAM_ID=?", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("STREAM_ID");
            }
        }, new Object[]{path}, true);
        if (list.size() == 0) {
            template.update("INSERT INTO " + prefix
                            + "streams (STREAM_ID) VALUES (?)", new Object[]{path},
                    true);
        }
    }

    protected abstract void writeStream(String path, byte[] bytes);

    public void clear() {
        accessor.clearAccessors();
        factory.clear();
    }

    @Override
    public long getElementCountForQualifier(long qialifierId) {
        return (Long) template.queryForObject("SELECT COUNT(*) FROM " + prefix
                + "elements WHERE qualifier_id=?", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getLong(1);
            }
        }, qialifierId, true);
    }

    @Override
    public long nextValue(String sequence) {
        long res = nextValueA(sequence);
        return res;
    }

    public long nextValueA(String sequence) {
        Connection c = template.getConnection();
        try {
            return next(c, sequence);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long next(Connection c, String sequence) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = c.createStatement();
            rs = st.executeQuery("SELECT nextval(\'" + prefix + sequence
                    + "\')");
            c.commit();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            st.close();
            throw e;
        } finally {
            if (rs != null)
                rs.close();
            if (st != null)
                st.close();
        }

    }

    @Override
    public Qualifier getSystemQualifier(String qualifierName) {
        long branch = getActiveBranchId();
        Qualifier qualifier = (Qualifier) template
                .queryForObjects(
                        "SELECT * FROM "
                                + prefix
                                + "qualifiers WHERE QUALIFIER_NAME=? AND qualifier_system=TRUE AND removed_branch_id>? AND created_branch_id <=? ",
                        new QualifierRowMapper(branch), new Object[]{
                                qualifierName, branch, branch}, true);
        if (qualifier != null) {
            Qualifier q = getQualifier(qualifier.getId());
            if (!q.getName().equals(qualifierName))
                qualifier = null;
        }
        if (branch > 0l) {
            Long l = (Long) template
                    .queryForObjects(
                            "SELECT QUALIFIER_ID FROM "
                                    + prefix
                                    + "qualifiers_history qh WHERE QUALIFIER_NAME=? AND created_branch_id IN ("
                                    + "SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "qualifiers_history WHERE QUALIFIER_NAME=qh.QUALIFIER_NAME AND created_branch_id<=?)",
                            new RowMapper() {

                                @Override
                                public Object mapRow(ResultSet rs, int rowNum)
                                        throws SQLException {
                                    return rs.getLong(1);
                                }
                            }, new Object[]{qualifierName, branch}, false);
            if (l != null)
                return getQualifier(l);
        }
        if (qualifier == null)
            return null;

        throwExaptionIfNotCan(
                getAccessor().canReadQualifier(qualifier.getId()),
                "Can not get qualifier.");
        return qualifier;
    }

    public JDBCTemplate getTemplate() {
        return template;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setCalculateInfo(final CalculateInfo formula) {
        if (formula.getFormula() == null) {
            template.execute(new JDBCCallback() {
                @Override
                public Object execute(Connection connection)
                        throws SQLException {
                    PreparedStatement ps = connection
                            .prepareStatement("DELETE FROM "
                                    + prefix
                                    + "formula_dependences WHERE element_id=? AND attribute_id=?");
                    ps.setLong(1, formula.getElementId());
                    ps.setLong(2, formula.getAttributeId());
                    ps.execute();
                    ps.close();

                    ps = connection.prepareStatement("DELETE FROM " + prefix
                            + "formulas WHERE element_id=? AND attribute_id=?");
                    ps.setLong(1, formula.getElementId());
                    ps.setLong(2, formula.getAttributeId());
                    ps.execute();
                    ps.close();

                    return null;
                }
            });
        } else {
            template.execute(new JDBCCallback() {
                @Override
                public Object execute(Connection connection)
                        throws SQLException {
                    Util utils = new Util();
                    Eval eval = new Eval(formula.getFormula());
                    MetaValue[] values = utils.toMetaValues(eval.getValues());

                    PreparedStatement ps = connection
                            .prepareStatement("DELETE FROM "
                                    + prefix
                                    + "formula_dependences WHERE element_id=? AND attribute_id=?");
                    ps.setLong(1, formula.getElementId());
                    ps.setLong(2, formula.getAttributeId());
                    ps.execute();
                    ps.close();

                    ps = connection.prepareStatement("DELETE FROM " + prefix
                            + "formulas WHERE element_id=? AND attribute_id=?");
                    ps.setLong(1, formula.getElementId());
                    ps.setLong(2, formula.getAttributeId());
                    ps.execute();
                    ps.close();

                    ps = connection
                            .prepareStatement("INSERT INTO "
                                    + prefix
                                    + "formulas (element_id, attribute_id, autorecalculate, formula) "
                                    + "VALUES(?, ?, ?, ?)");
                    ps.setLong(1, formula.getElementId());
                    ps.setLong(2, formula.getAttributeId());
                    ps.setBoolean(3, formula.isAutoRecalculate());
                    ps.setString(4, formula.getFormula());
                    ps.execute();
                    ps.close();

                    ps = connection
                            .prepareStatement("INSERT INTO "
                                    + prefix
                                    + "formula_dependences (source_element_id, source_attribute_id, element_id, attribute_id) "
                                    + "VALUES(?, ?, ?, ?)");
                    ps.setLong(3, formula.getElementId());
                    ps.setLong(4, formula.getAttributeId());
                    for (MetaValue value : values) {
                        ps.setLong(1, value.getElementId());
                        ps.setLong(2, value.getAttributeId());
                        ps.execute();
                    }
                    ps.close();

                    return null;
                }
            });

        }
    }

    @Override
    public CalculateInfo getCalculateInfo(final long elementId,
                                          final long attributeId) {
        return (CalculateInfo) template.queryForObjects("SELECT * FROM "
                        + prefix + "formulas WHERE element_id=? AND attribute_id=?",
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return new CalculateInfo(elementId, attributeId, rs
                                .getString("formula"), rs
                                .getBoolean("autorecalculate"));
                    }
                }, new Object[]{elementId, attributeId}, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CalculateInfo> findCalculateInfos(String reg,
                                                  boolean autoRecalculate) {
        String sql = "SELECT " + prefix + "formulas.element_id, " + prefix
                + "formulas.attribute_id, " + prefix
                + "formulas.autorecalculate, " + prefix
                + "formulas.formula FROM " + prefix + "formulas " + "WHERE "
                + "formula LIKE ?";
        if (autoRecalculate) {
            sql += " AND " + prefix + "formulas.autorecalculate=?";
            return template.query(sql, new RowMapper() {
                @Override
                public Object mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    return new CalculateInfo(rs.getLong(1), rs.getLong(2), rs
                            .getString(4), rs.getBoolean(3));
                }
            }, new Object[]{reg, autoRecalculate}, true);
        } else
            return template.query(sql, new RowMapper() {
                @Override
                public Object mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    return new CalculateInfo(rs.getLong(1), rs.getLong(2), rs
                            .getString(4), rs.getBoolean(3));
                }
            }, new Object[]{reg}, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CalculateInfo> getDependences(long elementId, long attributeId,
                                              boolean autoRecalculate) {
        String sql = "SELECT "
                + prefix
                + "formulas.element_id, "
                + prefix
                + "formulas.attribute_id, "
                + prefix
                + "formulas.autorecalculate, "
                + prefix
                + "formulas.formula FROM "
                + prefix
                + "formulas, "
                + prefix
                + "formula_dependences "
                + "WHERE "
                + prefix
                + "formulas.element_id="
                + prefix
                + "formula_dependences.element_id AND "
                + prefix
                + "formulas.attribute_id="
                + prefix
                + "formula_dependences.attribute_id AND source_element_id=? AND source_attribute_id=?";
        if (autoRecalculate) {
            sql += " AND " + prefix + "formulas.autorecalculate=?";
            return template.query(sql, new RowMapper() {
                @Override
                public Object mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    return new CalculateInfo(rs.getLong(1), rs.getLong(2), rs
                            .getString(4), rs.getBoolean(3));
                }
            }, new Object[]{elementId, attributeId, autoRecalculate}, true);
        } else
            return template.query(sql, new RowMapper() {
                @Override
                public Object mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    return new CalculateInfo(rs.getLong(1), rs.getLong(2), rs
                            .getString(4), rs.getBoolean(3));
                }
            }, new Object[]{elementId, attributeId}, true);
    }

    @Override
    public Attribute getAttributeByName(String attributeName) {
        final long branch = getActiveBranchId();
        Attribute attribute = (Attribute) template
                .queryForObjects(
                        "SELECT * FROM "
                                + prefix
                                + "attributes WHERE ATTRIBUTE_NAME=? AND attribute_system=FALSE AND removed_branch_id>?",
                        new AttributeRowMapper(branch), new Object[]{
                                attributeName, branch}, true);
        if (attribute != null
                && !attributeName.equals(getAttribute(attribute.getId())
                .getName()))
            attribute = null;

        if (branch > 0l) {
            Long l = (Long) template
                    .queryForObjects(
                            "SELECT ATTRIBUTE_ID FROM "
                                    + prefix
                                    + "attributes_history qh WHERE ATTRIBUTE_NAME=? AND created_branch_id IN ("
                                    + "SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "attributes_history WHERE ATTRIBUTE_NAME=qh.ATTRIBUTE_NAME AND created_branch_id<=?)",
                            new RowMapper() {

                                @Override
                                public Object mapRow(ResultSet rs, int rowNum)
                                        throws SQLException {
                                    return rs.getLong(1);
                                }
                            }, new Object[]{attributeName, branch}, false);
            if (l != null)
                return getAttribute(l);
        }

        return attribute;
    }

    @Override
    public Element getElement(final String elementName, final long qualifierId) {
        throwExaptionIfNotCan(getAccessor().canReadQualifier(qualifierId),
                "Can not get element.");
        long branchId = getActiveBranchId();
        String where = " AND attribute_id=attribute_for_name AND elmts.element_id=element_id";

        return (Element) template
                .queryForObjects(
                        "SELECT ELEMENT_ID FROM "
                                + prefix
                                + "elements elmts, "
                                + prefix
                                + "qualifiers q  WHERE (SELECT value FROM "
                                + prefix
                                + "attribute_texts WHERE element_id=elmts.element_id AND attribute_id=attribute_for_name "
                                + " AND (value_branch_id IN (SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id<=?"
                                + where
                                + ") OR ((SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id<=?"
                                + where
                                + ") IS NULL AND value_branch_id=0)))=? AND elmts.QUALIFIER_ID=? AND elmts.QUALIFIER_ID=q.qualifier_id AND elmts.created_branch_id<=? "
                                + "AND (elmts.removed_branch_id >?)",
                        new RowMapper() {

                            @Override
                            public Object mapRow(ResultSet rs, int rowNum)
                                    throws SQLException {
                                return new Element(rs.getLong("ELEMENT_ID"),
                                        qualifierId, elementName);
                            }

                        }, new Object[]{branchId, branchId, elementName,
                                qualifierId, branchId, branchId}, true);
    }

    @Override
    public Qualifier getQualifierByName(String qualifierName) {
        long branch = getActiveBranchId();
        Qualifier qualifier = (Qualifier) template
                .queryForObjects(
                        "SELECT * FROM "
                                + prefix
                                + "qualifiers WHERE QUALIFIER_NAME=? AND qualifier_system=FALSE AND removed_branch_id>? "
                                + "AND created_branch_id <=?",
                        new QualifierRowMapper(branch), new Object[]{
                                qualifierName, branch, branch}, true);
        if (qualifier != null) {
            Qualifier q = getQualifier(qualifier.getId());
            if (!q.getName().equals(qualifierName))
                qualifier = null;
        }
        if (branch > 0l) {
            Long l = (Long) template
                    .queryForObjects(
                            "SELECT QUALIFIER_ID FROM "
                                    + prefix
                                    + "qualifiers_history qh WHERE QUALIFIER_NAME=? AND created_branch_id IN ("
                                    + "SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "qualifiers_history WHERE QUALIFIER_NAME=qh.QUALIFIER_NAME AND created_branch_id <=? )",
                            new RowMapper() {

                                @Override
                                public Object mapRow(ResultSet rs, int rowNum)
                                        throws SQLException {
                                    return rs.getLong(1);
                                }
                            }, new Object[]{qualifierName, branch}, false);
            if (l != null)
                return getQualifier(l);
        }
        if (qualifier == null)
            return null;
        throwExaptionIfNotCan(
                getAccessor().canReadQualifier(qualifier.getId()),
                "Can not get qualifier.");
        return qualifier;
    }

    @Override
    public Attribute getSystemAttribute(String attributeName) {
        Attribute a = (Attribute) template
                .queryForObjects(
                        "SELECT * FROM "
                                + prefix
                                + "attributes WHERE ATTRIBUTE_NAME=? AND attribute_system=TRUE",
                        new AttributeRowMapper(getActiveBranchId()),
                        new Object[]{attributeName}, true);
        if (a != null)
            return a;

        final long branch = getActiveBranchId();
        Attribute attribute = (Attribute) template
                .queryForObjects(
                        "SELECT * FROM "
                                + prefix
                                + "attributes WHERE ATTRIBUTE_NAME=? AND attribute_system=TRUE AND removed_branch_id>?",
                        new AttributeRowMapper(branch), new Object[]{
                                attributeName, branch}, true);
        if (attribute != null
                && !attributeName.equals(getAttribute(attribute.getId())
                .getName()))
            attribute = null;

        if (branch > 0l) {
            Long l = (Long) template
                    .queryForObjects(
                            "SELECT ATTRIBUTE_ID FROM "
                                    + prefix
                                    + "attributes_history qh WHERE ATTRIBUTE_NAME=? AND created_branch_id IN ("
                                    + "SELECT MAX(created_branch_id) FROM "
                                    + prefix
                                    + "attributes_history WHERE ATTRIBUTE_NAME=qh.ATTRIBUTE_NAME AND created_branch_id <=?)",
                            new RowMapper() {

                                @Override
                                public Object mapRow(ResultSet rs, int rowNum)
                                        throws SQLException {
                                    return rs.getLong(1);
                                }
                            }, new Object[]{attributeName, branch}, false);
            if (l != null)
                return getAttribute(l);
        }

        return attribute;

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Attribute> getSystemAttributes() {
        return template.query("SELECT * FROM " + prefix
                        + "attributes WHERE ATTRIBUTE_SYSTEM=TRUE",
                new AttributeRowMapper(getActiveBranchId()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Element> getElements(long qualifierId, Attribute attribute,
                                     FindObject[] findObjects) {
        AttributePlugin plugin = factory.getAttributePlugin(attribute
                .getAttributeType());

        Class<? extends Persistent> clazz = plugin.getAttributePersistents()[0];
        String name = prefix + "attribute_"
                + clazz.getAnnotation(Table.class).name();

        String w = findObjects[0].getField() + "=?";

        for (int i = 1; i < findObjects.length; i++) {
            w += " AND " + findObjects[i].getField() + "=?";
        }

        List<Object> objects = new ArrayList<Object>(findObjects.length + 1);

        long branchId = getActiveBranchId();
        objects.add(branchId);
        objects.add(branchId);
        objects.add(qualifierId);
        for (int i = 0; i < findObjects.length; i++) {
            objects.add(findObjects[i].getObject());
        }

        Qualifier qualifier = getQualifier(qualifierId);
        long attr = qualifier.getAttributeForName();

        String where = " AND attribute_id=" + attr
                + " AND elmts.element_id=element_id";
        objects.add(branchId);
        objects.add(branchId);

        return template
                .query("SELECT ELEMENT_ID, elmts.QUALIFIER_ID, (SELECT value FROM "
                                + prefix
                                + "attribute_texts WHERE element_id=elmts.element_id AND attribute_id="
                                + attr
                                + " "
                                + " AND (value_branch_id IN (SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id<=? "
                                + where
                                + ") OR ((SELECT MAX(branch_id) FROM "
                                + prefix
                                + "attributes_data_metadata WHERE branch_id <=?"
                                + where
                                + ") IS NULL AND value_branch_id=0))) AS ELEMENT_TEXT_NAME FROM "
                                + prefix
                                + "elements elmts, "
                                + prefix
                                + "qualifiers q WHERE elmts.QUALIFIER_ID=? AND elmts.qualifier_id=q.qualifier_id "
                                + "AND elmts.ELEMENT_ID IN (SELECT ELEMENT_ID FROM "
                                + name
                                + " WHERE "
                                + w
                                + ") AND elmts.created_branch_id <=? "
                                + "AND (elmts.removed_branch_id>? ) ORDER BY ELEMENT_ID",
                        new ElementRowMapper(), objects.toArray(), true);
    }

    @Override
    public void setElementQualifier(final long elementId, final long qualifierId) {
        throwExaptionIfNotCan(
                getAccessor().canDeleteElements(new long[]{elementId}),
                "Can not update element's qualifier.");
        throwExaptionIfNotCan(getAccessor().canCreateElement(qualifierId),
                "Can not update element's qualifier.");

        Element element = getElement(elementId);

        Qualifier current = getQualifier(element.getQualifierId());

        Qualifier newQualifier = getQualifier(qualifierId);

        final List<Attribute> attrs = new ArrayList<Attribute>();
        addNotPresentAttributes(current.getAttributes(),
                newQualifier.getAttributes(), attrs);
        addNotPresentAttributes(current.getSystemAttributes(),
                newQualifier.getSystemAttributes(), attrs);

        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {

                Transaction[] transactions = getAttributesWhatWillBeDeleted(
                        elementId, attrs);

                for (Transaction transaction : transactions) {
                    executeTransaction(transaction, connection);
                }

                PreparedStatement ps = connection.prepareStatement("UPDATE "
                        + prefix
                        + "elements SET QUALIFIER_ID=? WHERE ELEMENT_ID=?");
                ps.setLong(1, qualifierId);
                ps.setLong(2, elementId);
                ps.execute();
                ps.close();

                return null;
            }
        });

    }

    private void addNotPresentAttributes(List<Attribute> attributes,
                                         List<Attribute> attributes2, List<Attribute> attrs) {
        for (Attribute attribute : attributes)
            if (attributes2.indexOf(attribute) < 0)
                attrs.add(attribute);
    }

    @Override
    public Transaction[] getAttributesWhatWillBeDeleted(long elementId,
                                                        List<Attribute> attrs) {
        throwExaptionIfNotCan(
                getAccessor().canDeleteElements(new long[]{elementId}),
                "Can not get attributes what will be deleted, as you can not delete element.");

        Transaction[] res = new Transaction[attrs.size()];

        for (int i = 0; i < attrs.size(); i++) {

            Attribute attribute = attrs.get(i);
            res[i] = getAttributeWhatWillBeDeleted(elementId, attribute, false);

        }

        return res;
    }

    public PersistentFactory getPersistentFactory() {
        return persistentFactory;
    }

    @Override
    public long createBranch(long parent, String reason, int type, String module) {
        long branchId = template.queryForLong("SELECT MAX(branch_id) FROM "
                + prefix + "branches") + 1;
        createBranch(parent, branchId, reason, type, module);
        return getActiveBranch();
    }

    @Override
    public void createBranch(long parent, long branchId, String reason,
                             int type, String module) {
        qualifeirsCache.clear();
        synchronized (branchCreationLock) {
            if (parent == -1l)
                parent = getMainBranch();

            if (reason == null)
                throw new NullPointerException("Reason is null");
            try {
                List<Branch> data = loadBranches();

                Hashtable<Long, Branch> hash = new Hashtable<Long, Branch>();

                for (Branch branch : data)
                    hash.put(branch.getBranchId(), branch);

                PreparedStatement ps = template
                        .getPreparedStatement(
                                "INSERT INTO "
                                        + IEngineImpl.this.prefix
                                        + "branches(branch_id, creation_user, reason, branch_type, module_name, creation_time) VALUES(?, ?, ?, ?, ?, ?)",
                                false);
                ps.setLong(1, branchId);
                String user = "admin";

                ps.setString(2, user);
                ps.setString(3, reason);
                ps.setInt(4, type);
                ps.setString(5, module);
                ps.setTimestamp(6, new Timestamp(new Date().getTime()));

                ps.execute();

                ps.close();

                setActiveBranch(branchId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void deleteBranch(long branch) {
        qualifeirsCache.clear();
        try {
            PreparedStatement ps = template.getPreparedStatement("DELETE FROM "
                            + IEngineImpl.this.prefix + "branches WHERE branch_id=?",
                    false);
            ps.setLong(1, branch);
            ps.execute();
            ps.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getActiveBranch() {
        return getActiveBranchId();
    }

    @Override
    public void setActiveBranch(long branchToActivate) {
        qualifeirsCache.clear();
        setActiveBranchId(branchToActivate);
    }

    @Override
    public Branch getRootBranch() {
        List<Branch> data = loadBranches();

        Hashtable<Long, Branch> hash = new Hashtable<Long, Branch>();

        for (Branch branch : data)
            hash.put(branch.getBranchId(), branch);

        Branch root = null;
        for (Branch branch : data) {
            if (branch.getBranchId() == 0l && branch.getParentBranchId() == 0l)
                root = branch;
            if (branch.getBranchId() != 0l)
                hash.get(branch.getParentBranchId()).getChildren().add(branch);
        }

        return root;
    }

    @SuppressWarnings({"cast"})
    protected List<Branch> loadBranches() {
        List<Branch> data = (List<Branch>) template
                .query("SELECT branch_id, reason, branch_type, creation_user, module_name, creation_time FROM "
                        + prefix + "branches a ", new RowMapper() {

                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        Branch branch = new Branch();
                        branch.setBranchId(rs.getLong("branch_id"));
                        branch.setReason(rs.getString("reason"));
                        branch.setType(rs.getInt("branch_type"));
                        branch.setUser(rs.getString("creation_user"));
                        branch.setModule(rs.getString("module_name"));
                        Timestamp ts = rs.getTimestamp("creation_time");
                        branch.setCreationTime(new Date(ts.getTime()));
                        return branch;
                    }
                });
        return data;
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

    @Override
    public void updateBranch(Branch branch) {
        template.update("UPDATE " + IEngineImpl.this.prefix
                + "branches SET reason=? WHERE branch_id=?", new Object[]{
                branch.getReason(), branch.getBranchId()}, false);

    }
}
