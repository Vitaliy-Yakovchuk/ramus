package com.ramussoft.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Server extends Thread {

    protected final int port;

    private static final int MIN_SESSION_TIME = 30;// minuts

    private static final long CHECK_TIME = 60000;// miliseconds

    private final Hashtable<String, Session> sessions = new Hashtable<String, Session>();

    private final Object lock = new Object();

    private ServerSocket server;

    public Server(final int port) {
        super();
        this.port = port;
        this.setName("IDEF0-Model-http-server");
    }

    private class SesionDisposer extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    sleep(CHECK_TIME);
                } catch (final InterruptedException e) {
                    break;
                }
                tryDispose();
                if (isInterrupted())
                    break;
            }
        }

        private void tryDispose() {
            synchronized (lock) {
                final Enumeration<String> keys = sessions.keys();
                final Vector<String> rems = new Vector<String>();
                final long time = System.currentTimeMillis();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    final Session session = sessions.get(key);
                    if ((time - session.getLastUpdate()) / 60000 > MIN_SESSION_TIME)
                        rems.add(key);
                }
                for (int i = 0; i < rems.size(); i++) {
                    sessions.remove(rems.get(i));
                }
            }
        }
    }

    ;

    private class MThread extends Thread {

        private final Socket socket;

        public MThread(final Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (request()) {
                InputStream stream;
                try {
                    stream = socket.getInputStream();
                    final Response response = new Response(socket
                            .getOutputStream());
                    final Request request = new Request(stream, response,
                            Server.this);
                    final Servlet servlet = getServlet(request);
                    servlet.accept(request, response);
                    response.close();
                    request.getInputStream().close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Викликається відразу після того, як клієнт дав запит.
     */

    protected synchronized boolean request() {
        return true;
    }

    protected void runServer() throws IOException {
        server = createServerSocket();
        final SesionDisposer disposer = new SesionDisposer();
        try {
            disposer.setName("http-server-disposer:" + server.getLocalPort());
            disposer.setPriority(getPriority());
            disposer.setDaemon(true);
            disposer.start();
            serverStarted();
            while (true) {
                final MThread t = new MThread(server.accept());
                if (isInterrupted()) {
                    server.close();
                }

                t.setPriority(Thread.MIN_PRIORITY);
                t.start();
            }

        } catch (final IOException e) {
        } finally {
            disposer.interrupt();
        }
    }

    protected ServerSocket createServerSocket() throws IOException {
        ServerSocket ss = new ServerSocket();
        InetSocketAddress address = new InetSocketAddress(InetAddress
                .getByName("localhost"), port);
        ss.bind(address);
        return ss;
    }

    protected void serverStarted() {
    }

    @Override
    public void run() {
        super.run();
        try {
            runServer();
        } catch (final IOException e) {

        }
    }

    protected Servlet getServlet(final Request request) {
        return new Servlet();
    }

    public Session getSesionbyId(final String id) {
        return sessions.get(id);
    }

    public void addSession(final Session session) {
        synchronized (lock) {
            sessions.put(session.getId(), session);
        }
    }

    public ServerSocket getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }
}
