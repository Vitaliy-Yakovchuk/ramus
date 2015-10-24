package com.ramussoft.server.tcp;

import java.io.File;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ramussoft.common.journal.Journal;
import com.ramussoft.net.common.Metadata;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.server.EngineFactory;
import com.ramussoft.server.ServerAccessRules;
import com.ramussoft.server.TcpServerConnection;
import com.ramussoft.server.UserFactoryImpl;

public class TcpServer {

    private List<RamusService> services = new ArrayList<RamusService>();

    private EngineFactory engineFactory = new EngineFactory();

    private ServerAccessRules serverAccessRules;

    private UserFactoryImpl userFactory;

    private String tmpPath = System.getProperty("java.io.tmpdir")
            + File.separator + "ramus-server";

    private Object servicesLock = new Object();

    private Hashtable<Thread, String> logins = new Hashtable<Thread, String>();

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new TcpServer();
    }

    public TcpServer() throws IOException {
        start();
    }

    @SuppressWarnings("unused")
    public void start() throws IOException {
        File file = new File(tmpPath);
        if (!file.mkdirs()) {
            for (File f : file.listFiles())
                f.delete();
        }

        Properties ps = EngineFactory.getPropeties();

        if ((ps == null) && (!com.ramussoft.common.Metadata.CORPORATE))
            ps = new Properties();

        String host = ps.getProperty("hostname");
        if (host != null) {
            System.setProperty("java.rmi.server.hostname", host);
        }

        Journal.checkThreads = false;

        final ServerSocket ss = createServerSocket();

        userFactory = new UserFactoryImpl(engineFactory.getTemplate());

        serverAccessRules = new ServerAccessRules(engineFactory.getEngine(),
                userFactory) {
            @Override
            protected String getLogin() {
                return logins.get(Thread.currentThread());
            }
        };

        final ExecutorService executorService = Executors.newCachedThreadPool();

        Thread acceptionThread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Ramus Server ready");
                while (true) {
                    try {
                        final Socket socket = ss.accept();
                        final Thread thread;
                        final RamusService service = createRamusService();
                        Object proxy = service.createProxy();
                        final TcpServerConnection connection = new TcpServerConnection(
                                socket, service.getClasses(), proxy) {
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

                        Runnable runnable = new Runnable() {

                            @Override
                            public void run() {
                                connection.run();
                            }
                        };

                        service.setConnection(connection);

                        startAcceptionThread(executorService, runnable);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }, "server-main");

        acceptionThread.start();
    }

    protected void startAcceptionThread(ExecutorService service,
                                        Runnable runnable) {
        service.execute(runnable);
    }

    protected ServerSocket createServerSocket() throws IOException {
        final ServerSocket ss = new ServerSocket();

        ss.bind(new InetSocketAddress("0.0.0.0", Metadata.TCP_PORT));
        return ss;
    }

    public RamusService createRamusService() {
        final RamusService service = new RamusService(engineFactory,
                userFactory, tmpPath, serverAccessRules, TcpServer.this);
        return service;
    }

    public Hashtable<Thread, String> getLogins() {
        return logins;
    }

    public void processEvents(EvenstHolder holder, RamusService aService) {
        synchronized (servicesLock) {
            for (RamusService service : services)
                if (service != aService)
                    service.callback(holder);
        }
    }

    public int getConnectionsCount() {
        synchronized (servicesLock) {
            return services.size();
        }
    }
}
