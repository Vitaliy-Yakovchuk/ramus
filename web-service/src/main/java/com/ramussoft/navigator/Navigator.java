package com.ramussoft.navigator;

import java.io.IOException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.client.TcpClientConnection;
import com.ramussoft.client.TcpClientEngine;
import com.ramussoft.common.Engine;
import com.ramussoft.common.LocalAccessor;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.SuperEngineFactory;
import com.ramussoft.common.journal.event.JournalListener;
import com.ramussoft.core.attribute.simple.SimpleAttributePluginSuit;
import com.ramussoft.idef0.IDEF0PluginProvider;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.net.common.Metadata;
import com.ramussoft.net.common.tcp.EngineInvocker;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.report.ReportPluginProvider;
import com.ramussoft.server.EngineFactory;
import com.ramussoft.server.ServerIEngineImpl;
import com.ramussoft.web.HTTPParser;

public class Navigator {

    private static Navigator navigator;

    private static Object lock = new Object();

    private TcpClientEngine tcpClientEngine;

    private DataPlugin plugin;

    private ServerIEngineImpl impl;

    public static Navigator getNavigator() throws Exception {
        synchronized (lock) {
            if (navigator == null) {
                navigator = new Navigator();
            }
            return navigator;
        }
    }

    public static void close() {
        if (navigator != null) {
            try {
                navigator.dispose();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            navigator = null;
        }
    }

    @SuppressWarnings("unchecked")
    private Navigator() throws Exception {
        Connection jdbcConnection = EngineFactory.createNewConnection();
        JDBCTemplate template = new JDBCTemplate(jdbcConnection);
        String password = template
                .queryForObjects(
                        "SELECT \"password\" FROM users WHERE \"login\"=?",
                        new RowMapper() {
                            @Override
                            public Object mapRow(ResultSet rs, int index)
                                    throws SQLException {
                                return rs.getString(1);
                            }
                        }, new Object[]{"admin"}, true).toString().trim();

        final TcpClientConnection connection = new TcpClientConnection(
                "127.0.0.1", Metadata.TCP_PORT) {
            @Override
            protected void objectReaded(Object object) {
                if (tcpClientEngine != null)
                    tcpClientEngine.call((EvenstHolder) object);
            }
        };

        connection.start();

        connection.invoke("login", new Object[]{"admin", password});

        tcpClientEngine = new TcpClientEngine(
                (EngineInvocker) Proxy.newProxyInstance(getClass()
                                .getClassLoader(),
                        new Class[]{EngineInvocker.class},
                        new InvocationHandler() {

                            @Override
                            public Object invoke(Object proxy, Method method,
                                                 Object[] args) throws Throwable {
                                return connection.invoke(method.getName(), args);
                            }
                        }), connection);
        List<Class> interfaces = new ArrayList<Class>();
        interfaces.add(Engine.class);
        interfaces.add(Journaled.class);

        List<PluginProvider> list = new ArrayList<PluginProvider>();

        list.add(new SimpleAttributePluginSuit());
        list.add(new IDEF0PluginProvider());
        list.add(new ReportPluginProvider());

        PluginFactory factory = createPluginFactory(list);
        LocalAccessor rules = new LocalAccessor(tcpClientEngine) {

            @Override
            public boolean isBranchLeaf() {
                return false;
            }

        };

        for (Plugin plugin : factory.getPlugins())
            if (plugin.getFunctionalInterface() != null)
                interfaces.add(plugin.getFunctionalInterface());

        Engine engine = (Engine) Proxy.newProxyInstance(getClass()
                .getClassLoader(), interfaces.toArray(new Class[interfaces
                .size()]), tcpClientEngine);

        tcpClientEngine.setEngine(engine);

        for (Plugin plugin : factory.getPlugins())
            plugin.init(engine, rules);

        impl = new ServerIEngineImpl(0, template, "ramus_", factory);

        Journaled journaled2 = new Journaled() {

            @Override
            public void undoUserTransaction() {
            }

            @Override
            public void startUserTransaction() {
            }

            @Override
            public void setNoUndoPoint() {
            }

            @Override
            public void setEnable(boolean arg0) {
            }

            @Override
            public void rollbackUserTransaction() {
            }

            @Override
            public void removeJournalListener(JournalListener arg0) {
            }

            @Override
            public void redoUserTransaction() {
            }

            @Override
            public boolean isUserTransactionStarted() {
                return false;
            }

            @Override
            public boolean isEnable() {
                return false;
            }

            @Override
            public JournalListener[] getJournalListeners() {
                return null;
            }

            @Override
            public void commitUserTransaction() {
            }

            @Override
            public void close() {
            }

            @Override
            public boolean canUndo() {
                return false;
            }

            @Override
            public boolean canRedo() {
                return false;
            }

            @Override
            public void addJournalListener(JournalListener arg0) {
            }

            @Override
            public long getBranch() {
                return 0;
            }
        };

        plugin = NDataPluginFactory.getDataPlugin(null,
                (Engine) SuperEngineFactory.createTransactionalEngine(
                        engine, journaled2, impl, factory), rules);
    }

    private PluginFactory createPluginFactory(List<PluginProvider> list) {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();

        for (PluginProvider suit : list) {
            plugins.addAll(suit.getPlugins());
        }

        PluginFactory factory = new PluginFactory(plugins);
        return factory;
    }

    private void dispose() throws SQLException {
        tcpClientEngine.close();
        impl.getTemplate().close();
        impl.clear();
    }

    public HTTPParser getParser() {
        return new HTTPParser(plugin, null);
    }

    public HTTPParser getInnerParser() {
        return new HTTPParser(plugin, null) {
            @Override
            protected void printTop() {
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void printOpenInNewWindow() throws IOException {
                String l = "../navigator/"
                        + getCurrentPageQuary(new Hashtable());
                printStartATeg(l, true);
                htmlStream.print(RES.getString("OpenInNewWindow"));
                printEndATeg();
            }
        };
    }
}
