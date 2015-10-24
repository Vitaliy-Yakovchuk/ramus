package com.ramussoft.navigator;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.Request;
import com.ramussoft.web.Response;
import com.ramussoft.web.Server;
import com.ramussoft.web.Servlet;

import edu.stanford.ejalbert.BrowserLauncher;

public class Navigator {

    protected static final String ENCODING = HTTPParser.ENCODING;

    private ArrayList<FileNavigator> navigators = new ArrayList<FileNavigator>();

    private Server httpServer;

    private Hashtable<String, FileNavigator> hash = new Hashtable<String, FileNavigator>();

    public Navigator(final int aPort) {
        httpServer = new Server(aPort) {
            @Override
            protected ServerSocket createServerSocket() throws IOException {
                return new ServerSocket(aPort);
            }

            @Override
            protected Servlet getServlet(Request request) {
                return Navigator.this.getServlet(request);
            }
        };
        httpServer.start();
    }

    protected Servlet getServlet(Request request) {
        if (navigators.size() == 0) {
            return new Servlet() {
                @Override
                public void accept(Request request, Response response) throws IOException {
                    response.setContentType("text/html; charset=" + ENCODING);
                    PrintStream pw = response.getWriter();
                    pw.println("<html><head>");
                    pw.println("<title>" + ProjectNavigator.getString("Error")
                            + "</title>");
                    pw.println("</head><body>");
                    pw.println(ProjectNavigator
                            .getString("Error.NoFileSelected"));
                    pw.println("</body></html>");
                }
            };
        } else if (navigators.size() == 1) {
            return navigators.get(0).getServlet(request, null);
        } else {
            String location = request.getLocation();
            int index = location.indexOf('/');
            if (index < 0) {
                Collections.sort(navigators);
                return new Servlet() {
                    @Override
                    public void accept(Request request, Response response) throws IOException {
                        response.setContentType("text/html; charset=" + ENCODING);
                        PrintStream pw = response.getWriter();
                        pw.println("<html><head>");
                        pw.println("<title>Ramus Web Navigator</title>");
                        pw.println("</head><body>");
                        for (FileNavigator navigator : navigators) {
                            pw.println("<a href=\"" + navigator.getPrefix()
                                    + "/\">" + navigator.getModelName()
                                    + "</a><br>");
                        }
                        pw.println("</body></html>");
                    }
                };
            } else {
                String prefix = location.substring(0, index);
                FileNavigator fn = hash.get(prefix);
                request.setLocation(location.substring(index + 1));
                return fn.getServlet(request, prefix);
            }
        }
    }

    public void loadModels(String fileProperties) {
        Properties properties = new Properties();
        try {
            FileInputStream is = new FileInputStream(fileProperties);
            properties.load(is);
            is.close();
            loadModels(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModels(Properties properties) {
        closeNavigators();
        int modelCount = Integer.parseInt(properties.getProperty("ModelCount"));
        for (int i = 0; i < modelCount; i++) {
            String fileName = properties.getProperty("FileName_" + i);
            String modelName = properties.getProperty("ModelName_" + i);
            FileNavigator e = new FileNavigator(fileName, modelName,
                    getPrefix(fileName));
            navigators.add(e);
            hash.put(e.getPrefix(), e);
        }
    }

    private String getPrefix(String fileName) {
        int fileNameHash = fileName.hashCode();
        String s = Integer.toString(fileNameHash);
        String prefix = s;
        int add = 0;
        while (hash.get(prefix) != null) {
            add++;
            prefix = s + "_" + add;
        }
        return prefix;
    }

    public void stop() {
        closeNavigators();
        try {
            httpServer.getServer().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpServer.interrupt();
    }

    private void closeNavigators() {
        for (FileNavigator navigator : navigators) {
            navigator.close();
        }
        navigators.clear();
        hash.clear();
    }

    public void openBrowser() {
        String url = "http://127.0.0.1:"
                + httpServer.getPort() + "/";
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e1) {
            try {
                new BrowserLauncher().openURLinBrowser(url);
            } catch (Exception e2) {
                e1.printStackTrace();
                e2.printStackTrace();
                JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
            }
        }
    }

}
