package com.ramussoft.server.internet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.cached.CachedEngine;
import com.ramussoft.common.journal.BinaryAccessFile;
import com.ramussoft.common.journal.Journal;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserProvider;
import com.ramussoft.net.common.internet.ClientService;
import com.ramussoft.net.common.internet.InternetSyncJournal;
import com.ramussoft.server.CheckableUserFactory;
import com.ramussoft.server.EngineFactory;
import com.ramussoft.server.ServerConnection;
import com.ramussoft.server.TcpServerConnection;
import com.ramussoft.server.UserFactoryImpl;

public class ClientServiceImpl implements ClientService, UserProvider {

    private InternetServer server;

    private TcpServerConnection connection;

    private BinaryAccessFile file;

    private String journalName;

    private InternetSyncJournal journal;

    private UserFactoryImpl userFactory;

    private CheckableUserFactory userFactoryClient;

    private Thread myThread;

    private User user;

    public ClientServiceImpl(InternetServer server, UserFactoryImpl impl,
                             CheckableUserFactory userFactoryClient)
            throws FileNotFoundException {
        this.server = server;
        this.userFactory = impl;
        this.userFactoryClient = userFactoryClient;
        journalName = server.getTmpPath() + File.separator
                + System.currentTimeMillis() + ".session";
        while (new File(journalName).exists())
            journalName = server.getTmpPath() + File.separator
                    + System.currentTimeMillis() + ".session";

        file = new BinaryAccessFile(journalName, "rw");
        journal = new InternetSyncJournal(file);
        Engine engine = server.getEngine();
        if (engine instanceof JournaledEngine) {
            journal.registerEngine((JournaledEngine) engine);
        } else {
            journal.registerEngine((JournaledEngine) ((CachedEngine) engine)
                    .getSource());
        }
    }

    public void close() {
        server.getLogins().remove(myThread);
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new File(journalName).delete();
    }

    public BinaryAccessFile getFile() {
        return file;
    }

    @Override
    public Long login(String login, String password) {
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

        for (Group group : user.getGroups()) {
            if (group.getName().equals("admin")) {
                userFactoryClient.setAdmin(true);
                break;
            }
        }

        setLogin(login);

        return 1l;
    }

    @Override
    public Boolean canLogin() {
        return true;
    }

    @Override
    public void redo(byte[] bs) {
        server.redo(bs, this);
    }

    @Override
    public void undo(byte[] bs) {
        server.undo(bs, this);
    }

    public void setConnection(TcpServerConnection connection) {
        this.connection = connection;
    }

    public TcpServerConnection getConnection() {
        return connection;
    }

    public InternetSyncJournal getJournal() {
        return journal;
    }

    @Override
    public byte[] loadAllData() {
        return server.loadAllData();
    }

    @Override
    public boolean deleteStream(String path) {
        return server.deleteStream(path);
    }

    @Override
    public byte[] getStream(String path) {
        return server.getStream(path);
    }

    @Override
    public void setStream(String path, byte[] bytes) {
        server.setStream(path, bytes);
    }

    @Override
    public byte[] replaceElements(Element[] oldElements, Element newElement) {
        Journal journal;
        Engine engine = server.getEngine();
        if (engine instanceof JournaledEngine) {
            journal = ((JournaledEngine) engine).getJournal();
        } else {
            journal = ((JournaledEngine) ((CachedEngine) engine).getSource())
                    .getJournal();
        }
        synchronized (server) {
            try {
                journal.startUserTransaction();
                server.getEngine().replaceElements(oldElements, newElement);
                journal.commitUserTransaction();
                byte[] remove = server.getCalls()
                        .remove(Thread.currentThread());
                journal.undoUserTransaction();
                return remove;
            } catch (Exception e) {
                server.getCalls().remove(Thread.currentThread());
                journal.rollbackUserTransaction();
                throw new RuntimeException(e);
            }
        }
    }

    public void setLogin(String login) {
        this.myThread = Thread.currentThread();
        server.getLogins().put(Thread.currentThread(), login);
    }

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
        userFactory.updateUser(me);
    }

    @Override
    public boolean isAdmin() {
        return userFactoryClient.isAdmin();
    }

    @Override
    public long nextValue(String s) {
        return server.getEngine().nextValue(s);
    }
}
