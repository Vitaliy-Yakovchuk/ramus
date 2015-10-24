package com.ramussoft.http;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.ramussoft.client.TcpClientConnection;
import com.ramussoft.client.TcpClientEngine;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.AdditionalPluginLoader;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.common.cached.Cached;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.SimpleAttributePluginSuit;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.core.impl.IntegrityAccessorSuit;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.net.common.Metadata;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;
import com.ramussoft.net.common.tcp.CallParameters;
import com.ramussoft.net.common.tcp.EngineInvocker;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.net.common.tcp.Result;
import com.ramussoft.server.EngineFactory;
import com.ramussoft.server.ServerAccessRules;
import com.ramussoft.server.UserFactoryImpl;

public class EngineConnection {
    public static final String LOCAL_HOST = "127.0.0.1";

    private Engine engine;

    private JDBCTemplate template;

    private AccessRules rules;

    private UserFactoryImpl userFactory;

    private TcpClientEngine tcpClientEngine;

    private TcpClientConnection connection;

    /**
     * Конструктор по замовчуванню. Зв’язок створюється на етапі створення
     * даного об’кту. Тому метод getEngine() по суті повертає вже створений
     * об’єкт.
     *
     * @param args Перелік атрибутів, що можуть налаштовуватись для створення
     *             підключення до базі даних. На даний момент параметри не
     *             використовуються і можуть бути <code>null</code>.
     */

    public EngineConnection() {
        try {
            StandardAttributesPlugin.setDefaultDisableAutoupdate(true);
            init(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(String[] args) throws Exception {
        try {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            java.sql.Connection jdbcConnection = null;

            Properties ps = EngineFactory.getPropeties();
            if (ps != null) {
                try {
                    Class.forName(ps.getProperty("driver"));
                    jdbcConnection = DriverManager.getConnection(
                            ps.getProperty("url"), ps.getProperty("user"),
                            ps.getProperty("password"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (jdbcConnection == null)
                jdbcConnection = DriverManager.getConnection(
                        "jdbc:postgresql://127.0.0.1/ramus_public", "postgres",
                        "postgres");

            template = new JDBCTemplate(jdbcConnection);

            userFactory = new UserFactoryImpl(template);

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
                            }, new Object[]{"admin"}, true).toString()
                    .trim();

            connection = new TcpClientConnection("127.0.0.1", Metadata.TCP_PORT) {
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
                                public Object invoke(Object proxy,
                                                     Method method, Object[] args)
                                        throws Throwable {
                                    return connection.invoke(method.getName(),
                                            args);
                                }
                            }), connection);
            List<Class> interfaces = new ArrayList<Class>();
            interfaces.add(Engine.class);
            interfaces.add(Journaled.class);

            List<PluginProvider> suits = new ArrayList<PluginProvider>();

            suits.add(new SimpleAttributePluginSuit());

            initAdditionalPluginSuits(suits);

            PluginFactory factory = createPluginFactory(suits);

            for (Plugin plugin : factory.getPlugins())
                if (plugin.getFunctionalInterface() != null)
                    interfaces.add(plugin.getFunctionalInterface());

            final Engine engine1 = (Engine) Proxy.newProxyInstance(getClass()
                    .getClassLoader(), interfaces.toArray(new Class[interfaces
                    .size()]), tcpClientEngine);

            final Engine cachedEngine = engine1;// new CachedEngine(engine1);

            final Hashtable<Method, Object> hashtable = new Hashtable<Method, Object>();

            for (Method m : Engine.class.getMethods()) {
                hashtable.put(m, cachedEngine);
            }

            for (Method m : Cached.class.getMethods()) {
                hashtable.put(m, cachedEngine);
            }
            interfaces.add(Cached.class);

            Engine engine = (Engine) Proxy.newProxyInstance(getClass()
                    .getClassLoader(), interfaces.toArray(new Class[interfaces
                    .size()]), new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    Object o = hashtable.get(method);
                    if (o == null)
                        return tcpClientEngine.invoke(proxy, method, args);
                    return method.invoke(o, args);
                }
            });

            tcpClientEngine.setEngine(engine);

            for (Plugin plugin : factory.getPlugins())
                plugin.init(engine, rules);
            rules = (AccessRules) createDeligate(AccessRules.class);
            this.engine = engine;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User login(String login, String aPassword) {

        String password = (String) template.queryForObjects(
                "SELECT \"password\" FROM users WHERE \"login\"=?",
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int index)
                            throws SQLException {
                        return rs.getString(1);
                    }
                }, new Object[]{login}, false);
        if (password != null) {
            if (password.trim().equals(aPassword)) {
                return userFactory.getUser(login);
            }
        }
        return null;
    }

    private PluginFactory createPluginFactory(List<PluginProvider> list) {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();

        for (PluginProvider suit : list) {
            plugins.addAll(suit.getPlugins());
        }

        PluginFactory factory = new PluginFactory(plugins);
        return factory;
    }

    protected void initAdditionalPluginSuits(List<PluginProvider> suits) {
        AdditionalPluginLoader.loadAdditionalSuits(suits);
    }

    private Object createDeligate(@SuppressWarnings("rawtypes") Class[] classes) {
        return Proxy.newProxyInstance(getClass().getClassLoader(), classes,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {

                        CallParameters parameters = new CallParameters(method
                                .getName(), args);

                        Result result = connection.call(parameters);
                        if (result.exception != null)
                            throw result.exception;
                        return result.result;
                    }
                });
    }

    private Object createDeligate(@SuppressWarnings("rawtypes") Class class1) {
        return createDeligate(new Class[]{class1});
    }

    /**
     * Повертає повнофункціональний рушій Ramus. На даний момент створюється
     * простий журнальний рушій без додаткових реалізації
     */
    public Engine getEngine() {
        return engine;
    }

    public static void main(String[] args) throws Exception {
        EngineConnection connection = new EngineConnection();
        Engine engine = connection.getEngine();
        System.out.println(engine.getQualifiers());
        connection.close();
    }

    /**
     * Метод нормально закриває з’єднання з сервером.
     */

    public void close() {
        try {
            template.close();
            connection.setClosed(true);
            connection.invoke("closeConnection", new Object[]{});
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JDBCTemplate getTemplate() {
        return template;
    }

    public AccessRules getAccessRules(final User finalUser) {
        if (finalUser == null)
            return rules;
        IntegrityAccessorSuit suit = new IntegrityAccessorSuit();
        suit.addAccessRules(rules);
        suit.addAccessRules(new ServerAccessRules(engine, userFactory) {
            @Override
            public User getUser() {
                return finalUser;
            }
        });
        return suit;
    }

    public UserFactory getUserFactory() {
        return userFactory;
    }
}
