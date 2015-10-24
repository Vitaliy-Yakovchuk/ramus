package com.ramussoft.server.internet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.core.impl.IntegrityAccessorSuit;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.net.common.Metadata;
import com.ramussoft.net.common.SuperInvoker;
import com.ramussoft.net.common.UserFactory;
import com.ramussoft.net.common.UserProvider;
import com.ramussoft.net.common.internet.ClientService;
import com.ramussoft.net.common.internet.RedoObject;
import com.ramussoft.net.common.internet.UndoObject;
import com.ramussoft.server.CheckableUserFactory;
import com.ramussoft.server.EngineFactory;
import com.ramussoft.server.ServerAccessRules;
import com.ramussoft.server.TcpServerConnection;
import com.ramussoft.server.UserFactoryImpl;

public class InternetServer {

    private List<ClientServiceImpl> services = new ArrayList<ClientServiceImpl>();

    private Object servicesLock = new Object();

    private EngineFactory engineFactory;

    private ServerAccessRules serverAccessRules;

    private UserFactoryImpl userFactory;

    private String tmpPath = System.getProperty("java.io.tmpdir")
            + File.separator + "ramus-server";

    private Hashtable<Thread, String> logins = new Hashtable<Thread, String>();

    private Hashtable<Thread, byte[]> calls = new Hashtable<Thread, byte[]>();

    private Object saveLock = new Object();

    public static void main(String[] args) throws IOException {
        new InternetServer().start(args);
    }

    private void start(String[] args) throws IOException {
        run(args);
    }

    private void run(String[] args) throws IOException {
        new File(tmpPath).mkdirs();

        engineFactory = new EngineFactory() {
            /*@Override
			protected Journal createJournal() {
				BinaryAccessFile accessFile;
				try {
					accessFile = new BinaryAccessFile(tmpPath + File.separator
							+ String.valueOf(System.currentTimeMillis())
							+ ".server", "rw");
					InternetHookJournal journal = new InternetHookJournal(
							accessFile) {

						@Override
						public void onRedo(byte[] bs) {
							calls.put(Thread.currentThread(), bs);
						}

						@Override
						public void onUndo(byte[] bs) {
						}

					};
					journal.setEnable(true);
					return journal;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}*/
        };

        final ServerSocket ss = new ServerSocket();

        ss.bind(new InetSocketAddress("0.0.0.0", Metadata.INTERNET_PORT));

        userFactory = new UserFactoryImpl(engineFactory.getTemplate());

        serverAccessRules = new ServerAccessRules(engineFactory.getEngine(),
                userFactory) {
            @Override
            protected String getLogin() {
                return logins.get(Thread.currentThread());
            }
        };

        Thread acceptionThread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Ramus Interner Server ready");
                while (true) {
                    try {
                        final Thread thread;
                        final Socket socket = ss.accept();
                        CheckableUserFactory userFactoryClient = new CheckableUserFactory(
                                userFactory);

                        IntegrityAccessorSuit suit = new IntegrityAccessorSuit();

                        suit.addAccessRules(serverAccessRules);
                        suit.addAccessRules(((IEngineImpl) getEngine()
                                .getDeligate()).getAccessor());

                        final ClientServiceImpl service = new ClientServiceImpl(
                                InternetServer.this, userFactory,
                                userFactoryClient);
                        SuperInvoker invoker = new SuperInvoker(new Object[]{
                                service, suit, userFactoryClient, service},
                                new Class[]{ClientService.class,
                                        AccessRules.class, UserFactory.class,
                                        UserProvider.class});

                        final TcpServerConnection connection = new TcpServerConnection(
                                socket, new String[]{
                                ClientService.class.getName(),
                                AccessRules.class.getName(),
                                UserFactory.class.getName(),
                                UserProvider.class.getName()}, invoker
                                .createProxy()) {
                            public void close() throws IOException {
                                synchronized (servicesLock) {
                                    services.remove(service);
                                }
                                service.close();
                                super.close();
                            }

                            ;
                        };
                        synchronized (servicesLock) {
                            services.add(service);
                        }

                        thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                connection.run();
                            }
                        }, "server-client");

                        service.setConnection(connection);

                        thread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, "inet-server-main");

        acceptionThread.start();

    }

    public void redo(byte[] bs, ClientServiceImpl clientServiceImpl) {
        synchronized (saveLock) {
            clientServiceImpl.getJournal().redo(bs);
        }

        hookRedo(bs, clientServiceImpl);
    }

    private void hookRedo(byte[] bs, ClientServiceImpl clientServiceImpl) {
        synchronized (servicesLock) {
            RedoObject redoObject = new RedoObject();
            redoObject.data = bs;
            for (ClientServiceImpl impl : services)
                if (impl != clientServiceImpl)
                    impl.getConnection().writeAsynchrone(redoObject);
        }
    }

    public void undo(byte[] bs, ClientServiceImpl clientServiceImpl) {
        synchronized (saveLock) {
            clientServiceImpl.getJournal().undo(bs);
        }

        hookUndo(bs, clientServiceImpl);
    }

    private void hookUndo(byte[] bs, ClientServiceImpl clientServiceImpl) {
        synchronized (servicesLock) {
            UndoObject undoObject = new UndoObject();
            undoObject.data = bs;
            for (ClientServiceImpl impl : services)
                if (impl != clientServiceImpl) {
                    impl.getConnection().writeAsynchrone(undoObject);
                }
        }
    }

    public String getTmpPath() {
        return tmpPath;
    }

    public Engine getEngine() {
        return engineFactory.getJournaledEngine();
    }

    public byte[] loadAllData() {
        synchronized (saveLock) {
            MemoryDatabase database = new MemoryDatabase() {
                @Override
                protected void loadSuits(List<PluginProvider> suits) {
                    suits.addAll(engineFactory.getSuits());
                }

                @Override
                protected File getFile() {
                    return null;
                }

                @Override
                public Connection createConnection() throws SQLException {
                    return engineFactory.createConnection();
                }

                @Override
                protected String getJournalDirectoryName(String tmp) {
                    return null;
                }

                @Override
                protected FileIEngineImpl createFileIEngine(
                        PluginFactory factory) throws ClassNotFoundException,
                        ZipException, IOException {
                    return new FileIEngineImpl(0, template, factory, null);
                }
            };
            Engine s = database.getEngine(null);

            FileIEngineImpl impl = (FileIEngineImpl) s.getDeligate();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ZipOutputStream out;
            try {
                out = impl.writeToStream(stream);
                out.close();
                return stream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean deleteStream(String path) {
        return getEngine().deleteStream(path);
    }

    public byte[] getStream(String path) {
        return getEngine().getStream(path);
    }

    public void setStream(String path, byte[] bytes) {
        getEngine().setStream(path, bytes);
    }

    public Hashtable<Thread, byte[]> getCalls() {
        return calls;
    }

    public int getConnectionsCount() {
        return services.size();
    }

    public Hashtable<Thread, String> getLogins() {
        return logins;
    }
}
