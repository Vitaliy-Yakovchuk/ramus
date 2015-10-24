package com.ramussoft.gui.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.server.EngineFactory;
import com.sun.net.httpserver.HttpServer;

public class JNLPServer {

    private HttpServer httpServer;

    private JNLPServer(int port, String hostname) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        Properties ps = new Properties();
        InputStream stream = getClass().getResourceAsStream(
                "/com/ramussoft/lib.conf");
        ps.load(stream);
        stream.close();

        httpServer.createContext("/", new ResourceServlet(hostname, new Date(
                Long.parseLong(ps.getProperty("LastModified")))));
        httpServer.setExecutor(Executors.newCachedThreadPool());
    }

    public static JNLPServer createServer() {
        Properties ps = EngineFactory.getPropeties();
        if (ps == null)
            ps = new Properties();
        String sPort = ps.getProperty("WebPort");
        int port = 8080;
        if (sPort != null) {
            try {
                port = Integer.parseInt(sPort);
            } catch (Exception e) {
                e.printStackTrace();
                System.err
                        .println(sPort
                                + " can not be converted to integer, using default (8080) port");
            }
        }
        try {
            return new JNLPServer(port, ps.getProperty("hostname"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }

    public static void main(String[] args) {
        String path = Options.getPreferencesPath();
        if ((path.endsWith(File.separator)) && (path.length() > 1))
            path = path.substring(0, path.length() - 1);
        System.setProperty("ramus.server.base", path);

        JNLPServer server = createServer();
        if (server != null)
            server.start();
        Object t = new Object();
        synchronized (t) {
            try {
                t.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        httpServer.start();
    }
}
