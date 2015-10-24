package com.ramussoft.report.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.Source;

import com.ramussoft.common.AdditionalPluginLoader;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.report.Query;
import com.ramussoft.report.ReportPlugin;
import com.ramussoft.report.ReportQueryImpl;
import com.ramussoft.report.data.plugin.Connection;
import com.ramussoft.report.data.plugin.ConnectionPlugin;
import com.ramussoft.report.data.plugin.ConnectionPluginProvider;
import com.ramussoft.report.xml.NoRowsException;

@SuppressWarnings("unchecked")
public class Data extends Hashtable<Object, Object> {

    /**
     *
     */
    private static final long serialVersionUID = 2902722326346711300L;

    private final static ConnectionPlugin[] plugins;

    public static final String QUALIFIER_DELIMETER = "|";

    private final Engine engine;

    private Hashtable<Long, Qualifier> qualifierByIds = new Hashtable<Long, Qualifier>();

    private Hashtable<Long, RowSet> rowSets = new Hashtable<Long, RowSet>();

    private Hashtable<Long, RowSet> rowSetsNoFilers = new Hashtable<Long, RowSet>();

    private Query query;

    private Rows baseRows;

    private ReportQueryImpl reportQuery;

    static {
        Iterator<ConnectionPluginProvider> iterator = AdditionalPluginLoader
                .loadProviders(ConnectionPluginProvider.class);
        List<ConnectionPlugin> list = new ArrayList<ConnectionPlugin>();
        while (iterator.hasNext()) {
            ConnectionPluginProvider provider = iterator.next();
            for (ConnectionPlugin plugin : provider.getConnectionPlugins())
                list.add(plugin);
        }
        plugins = list.toArray(new ConnectionPlugin[list.size()]);
    }

    private List<Attribute> attributes;

    public Data(Engine engine) {
        this(engine, null);
    }

    public Data(Engine engine, Query query) {
        this(engine, query, null);
    }

    public Data(Engine engine, Query query, ReportQueryImpl reportQuery) {
        this.engine = engine;
        this.query = query;
        this.reportQuery = reportQuery;
    }

    public Engine getEngine() {
        return engine;
    }

    public Rows getRows(String qualifierName) {
        for (ConnectionPlugin plugin : plugins) {
            Rows rows = plugin.getVirtualQualifier(this, qualifierName);
            if (rows != null)
                return rows;
        }
        Qualifier qualifier = getQualifier(qualifierName);
        return getRows(qualifier);
    }

    public Rows getRows(Qualifier qualifier) {
        // Rows rows = qualifiers.get(qualifier);
        // if (rows == null) {
        RowSet rowSet = getRowSet(qualifier);
        Rows rows = new Rows(rowSet, this);
        for (Row r : rows) {
            for (com.ramussoft.database.common.Row c : r.getChildren())
                c.setNativeParent(r);
        }
        rows.setQualifierName(qualifier.getName());
        // qualifiers.put(qualifier, rows);
        // }
        return rows;
    }

    public Qualifier getQualifier(String qualifierName) {
        Qualifier qualifier = engine.getQualifierByName(qualifierName);
        if (qualifier == null) {
            throw new DataException("Error.qualifierNoFound", "Qualifier \""
                    + qualifierName + "\" not found!!!", qualifierName);
        }
        return qualifier;
    }

    public RowSet getRowSet(Qualifier qualifier) {
        RowSet rowSet = rowSets.get(qualifier.getId());
        if (rowSet == null) {
            rowSet = new RowSet(engine, qualifier, this);
            rowSets.put(qualifier.getId(), rowSet);
        }
        return rowSet;
    }

    public RowSet getRowSet(Qualifier qualifier, boolean filter) {
        if (filter)
            return getRowSet(qualifier);
        RowSet rs = rowSetsNoFilers.get(qualifier.getId());
        if (rs == null) {
            rs = new RowSet(engine, qualifier, this) {
                @Override
                protected boolean filter(Element element) {
                    return false;
                }
            };
            rowSetsNoFilers.put(qualifier.getId(), rs);
        }
        return rs;
    }

    public RowSet getRowSet(long qualifierId) {
        RowSet rowSet = rowSets.get(qualifierId);
        if (rowSet == null) {
            rowSet = new RowSet(engine, getQualifier(qualifierId), this);
            rowSets.put(qualifierId, rowSet);
        }
        return rowSet;
    }

    public List<Attribute> getAttributes() {
        if (attributes == null)
            attributes = engine.getAttributes();
        return attributes;
    }

    public Qualifier getQualifier(long qualifierId) {
        Qualifier qualifier = qualifierByIds.get(qualifierId);
        if (qualifier == null) {
            qualifier = engine.getQualifier(qualifierId);
            if (qualifier == null)
                return null;
            qualifierByIds.put(qualifierId, qualifier);
        }
        return qualifier;
    }

    public Connection getConnection(Rows rows, Qualifier qualifier, String name) {
        for (ConnectionPlugin plugin : plugins) {
            Connection connection = plugin.getConnection(this, qualifier, name);
            if (connection != null) {
                return connection;
            }
        }
        throw new DataException("Error.matrixNotFound", "Connection " + name
                + " not found.", name);
    }

    public Rows getConnection(Row row, Qualifier qualifier, String name) {
        for (ConnectionPlugin plugin : plugins) {
            Connection connection = plugin.getConnection(this, qualifier, name);
            if (connection != null) {
                Rows connected = connection.getConnected(this, row);
                return connected;
            }
        }
        throw new DataException("Error.matrixNotFound", "Connection " + name
                + " not found.", name);
    }

    public Rows getRowsByQuery(String query) {
        String[] words = query.split("\\.");
        if (words.length < 1)
            throw new DataException(
                    "Error.queryWordsCount",
                    "Query "
                            + query
                            + " does not contain a qualifier's name or an attribute's name",
                    query);
        Rows rows = getRows(words[0]);
        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            Rows rows2 = rows.getConnection(word);
            rows2.setParent(rows);
            rows = rows2;
        }
        return rows;
    }

    public Rows getRowsByQuery(Row current, String query) {
        String[] words = query.split("\\.");
        if (words.length < 1)
            throw new DataException(
                    "Error.queryWordsCount",
                    "Query "
                            + query
                            + " does not contain a qualifier's name or an attribute's name",
                    query);
        Rows rows = current.getConnection(words[0]);
        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            Rows rows2 = rows.getConnection(word);
            rows2.setParent(rows);
            rows = rows2;
        }
        return rows;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * @return the query
     */
    public Query getQuery() {
        if (query == null)
            query = new Query(new HashMap<String, String>());
        return query;
    }

    public JDBCTemplate getTemplate() {
        return ((IEngineImpl) engine.getDeligate()).getTemplate();
    }

    public Row getBaseRowByQualifier(String baseQualifierName) {
        if (baseRows == null) {
            baseRows = getRows(baseQualifierName);
            if (query != null) {
                List<Element> elements = query.getElements();
                if (elements != null) {
                    for (int i = baseRows.size() - 1; i >= 0; i--) {
                        Row row = baseRows.get(i);
                        if (elements.indexOf(row.getElement()) < 0)
                            baseRows.remove(i);
                    }
                }
            }
            if (baseRows.size() == 0)
                throw new NoRowsException();
            baseRows.next();
        } else {
            if (!baseQualifierName.equals(baseRows.getQualifierName())) {
                throw new DataException(
                        "Error.differentBaseQualifiers",
                        "Report contains diffetents base qualifiers in queries",
                        baseQualifierName, baseRows.getQualifierName());
            }
        }
        return baseRows.getCurrent();
    }

    public Rows getBaseRows() {
        return baseRows;
    }

    public Rows getBaseRowsByQuery(String query) {
        if (isSameBaseQualifier())
            return getRowsByQuery(query);

        int qBegin = query.length();
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '.') {
                qBegin = i;
                break;
            }
        }

        Row base = getBaseRowByQualifier(query.substring(0, qBegin));
        if (qBegin >= query.length()) {
            Rows rows = new Rows(base.getRowSet(), this, false);
            rows.add(base);
            return rows;
        }
        return base.getRowsByQuery(query.substring(qBegin + 1));
    }

    public boolean isPrintFor(String printFor) {
        if (baseRows == null)
            return true;
        Row r = baseRows.getCurrent();
        if (r == null)
            return true;
        if ("haveChilds".equals(printFor))
            return r.getChildCount() > 0;
        if ("haveNoChilds".equals(printFor))
            return r.getChildCount() == 0;
        return true;
    }

    public boolean isSameBaseQualifier() {
        if (query == null)
            return false;
        return "true".equals(query.getAttribute("SameBaseQualifier"));
    }

    public Source getReport(String name) {
        return getReport(name, null);
    }

    public Source getReport(String name, Query query) {
        Element element = engine.getElement(name, ReportPlugin
                .getReportsQualifier(engine).getId());
        if (element == null)
            throw new DataException("Error.reportNotFound", "Report " + name
                    + " not found", name);
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (query != null)
            map.put("query", query);
        String htmlReport = reportQuery.getHTMLReport(element, map);
        Source source = new Source(htmlReport);
        source.fullSequentialParse();
        return source;
    }

    public void setModelName(String modelName) {
        getQuery().setAttribute("ReportFunction", modelName);
    }

    public Row findRow(long eleemntId) {
        long id = engine.getQualifierIdForElement(eleemntId);
        Qualifier qualifier = getQualifier(id);
        if (qualifier == null)
            return null;
        return (Row) getRowSet(id).findRow(eleemntId);
    }

    public long getBranchId() {
        return getEngine().getActiveBranch();
    }

    public Object getAttribute(Row row, String name) {
        Object value;
        for (ConnectionPlugin plugin : plugins) {
            value = plugin.getAttribute(this, row, name);
            if (value != null)
                return value;
        }
        return null;
    }

}
