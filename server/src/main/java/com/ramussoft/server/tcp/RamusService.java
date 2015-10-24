package com.ramussoft.server.tcp;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.cached.CachedEngine;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.core.impl.IntegrityAccessorSuit;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.Logable;
import com.ramussoft.net.common.SuperInvoker;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;
import com.ramussoft.net.common.UserProvider;
import com.ramussoft.net.common.tcp.EngineInvocker;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.server.CheckableUserFactory;
import com.ramussoft.server.EngineFactory;
import com.ramussoft.server.ServerAccessRules;
import com.ramussoft.server.ServerConnection;
import com.ramussoft.server.ServerIEngineImpl;
import com.ramussoft.server.TcpServerConnection;

public class RamusService implements Logable {

    private String[] classes;

    private SuperInvoker superInvoker;

    private String login;

    private TcpServer server;

    private EngineFactory engineFactory;

    private DirectoryJournalFactory directoryJournalFactory;

    private UserFactory userFactory;

    private User user;

    private UserProvider userProvider;

    private CheckableUserFactory userFactoryClient;

    private UserEngineFactory userEngineFactory;

    private Thread myThread;

    private TcpServerConnection connection;

    private Engine engine;

    @SuppressWarnings("unchecked")
    public RamusService(EngineFactory engineFactory, UserFactory aUserFactory,
                        String tmpPath, ServerAccessRules rules, TcpServer server) {
        this.server = server;
        this.engineFactory = engineFactory;
        this.userFactory = aUserFactory;
        this.userProvider = new UserProvider() {

            @Override
            public User me() {
                return user;
            }

            @Override
            public void changePassword(String newPassword) {
                User me = me();
                if (me == null)
                    return;
                me.setPassword(newPassword);
                RamusService.this.userFactory.updateUser(me);
            }
        };

        this.userFactoryClient = new CheckableUserFactory(userFactory);

        this.userEngineFactory = new UserEngineFactory(this, tmpPath);

        engine = userEngineFactory.getEngine();

        List<Class> list = new ArrayList<Class>();
        List<Object> objects = new ArrayList<Object>();

        list.add(EngineInvocker.class);
        objects.add(new EngineInvockerImpl(userEngineFactory.getEngine(),
                new EventsFactory(engine)) {
            @Override
            protected void processEndInvoke(EvenstHolder holder) {
                RamusService.this.server.processEvents(holder,
                        RamusService.this);
            }
        });

        list.add(AccessRules.class);

        ((IntegrityAccessorSuit) (((IEngineImpl) engine.getDeligate())
                .getAccessor())).addAccessRules(rules);

        if (engine.getDeligate() instanceof ServerIEngineImpl) {
            ((ServerIEngineImpl) engine.getDeligate())
                    .setServerAccessRules(rules);

        }

        objects.add(((IEngineImpl) engine.getDeligate()).getAccessor());

        list.add(UserFactory.class);
        objects.add(userFactoryClient);

        list.add(UserProvider.class);
        objects.add(userProvider);

        list.add(Logable.class);
        objects.add(this);

        Class[] classes2 = list.toArray(new Class[list.size()]);

        this.superInvoker = new SuperInvoker(objects.toArray(new Object[objects
                .size()]), classes2);
        this.classes = new String[classes2.length];
        for (int i = 0; i < classes2.length; i++) {
            this.classes[i] = classes2[i].getName();
        }
    }

    public void adminAutologin() {
        this.user = userFactory.getUser("admin");
        setLogin("admin");
        userFactoryClient.setAdmin(true);
    }

    @Override
    public long login(String login, String password) {
        if (!Metadata.CORPORATE) {
            if (server.getConnectionsCount() >= 3)
                return -1l;
        } else {
            int cc = ServerConnection.getConnectionCount(EngineFactory
                    .getPropeties());
            if (cc >= 0) {
                if (server.getConnectionsCount() >= cc)
                    return -1l;
            }
        }

        User user = userFactory.getUser(login);
        if (user == null)
            return -1l;
        if (!user.getPassword().equals(password))
            user = null;
        if (user == null)
            return -1l;

        this.user = user;
        setLogin(login);

        for (Group group : user.getGroups()) {
            if (group.getName().equals("admin")) {
                userFactoryClient.setAdmin(true);
                break;
            }
        }

        return 1l;
    }

    public String[] getClasses() {
        return classes;
    }

    public Object createProxy() {
        return superInvoker.createProxy();
    }

    public void close() {
        if (myThread != null)
            server.getLogins().remove(myThread);
        Engine engine = userEngineFactory.getJournaledEngine();
        if (engine instanceof CachedEngine) {
            ((CachedEngine) engine).removeBranchCache();
        }
        IEngine deligate = engine.getDeligate();
        if (deligate instanceof IEngineImpl) {
            try {
                ((IEngineImpl) deligate).getTemplate().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (directoryJournalFactory != null) {
            directoryJournalFactory.close();
            delete(directoryJournalFactory.getDirectory());
        }
    }

    private void delete(File directory) {
        File[] fs = directory.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                if (f.getName().equals(".") || f.getName().equals("..")) {
                } else {
                    delete(f);
                }
            } else if (!f.delete())
                f.deleteOnExit();
        }
        if (!directory.delete())
            directory.deleteOnExit();
    }

    public EngineFactory getEngineFactory() {
        return engineFactory;
    }

    @Override
    public boolean isAdmin() {
        return userFactoryClient.isAdmin();
    }

    @Override
    public boolean canLogin() {
        if (!Metadata.CORPORATE) {
            if (server.getConnectionsCount() >= 3)
                return false;
        } else {
            int cc = ServerConnection.getConnectionCount(EngineFactory
                    .getPropeties());
            if (cc >= 0) {
                if (server.getConnectionsCount() >= cc)
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUndoRedo() {
        return engineFactory.isCanUndoRedo();
    }

    public void setLogin(String login) {
        this.login = login;
        this.myThread = Thread.currentThread();
        server.getLogins().put(Thread.currentThread(), login);
    }

    public String getLogin() {
        return login;
    }

    public void callback(EvenstHolder holder) {
        connection.writeAsynchrone(holder);
    }

    public void setConnection(TcpServerConnection connection) {
        this.connection = connection;
    }

    @Override
    public void closeConnection() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Engine getEngine() {
        return engine;
    }

    public void setDirectoryJournalFactory(
            DirectoryJournalFactory directoryJournalFactory) {
        this.directoryJournalFactory = directoryJournalFactory;
    }

    public DirectoryJournalFactory getDirectoryJournalFactory() {
        return directoryJournalFactory;
    }
}
