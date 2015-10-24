package com.ramussoft.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import com.ramussoft.http.CommandServlet;
import com.ramussoft.navigator.Navigator;
import com.ramussoft.server.tcp.TcpServer;

public class ContextListener implements javax.servlet.ServletContextListener {

    private Process cajoProcess;

    // private Process rmiProcess;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // rmiProcess.destroy();
        Navigator.close();
        if (cajoProcess != null)
            cajoProcess.destroy();
        try {
            EngineFactory.closeConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {

        Properties properties = EngineFactory.getPropeties();

        try {
            Connection connection = EngineFactory.getConnection();
            String url = connection.getMetaData().getURL();
            if (url.startsWith("jdbc:h2:")) {
                /*String path = url.substring("jdbc:h2:".length());
				StringBuffer sb = new StringBuffer();
				String tmp = "";
				for (int i = 0; i < path.length(); i++) {
					tmp += path.charAt(i);
					if (File.separatorChar == path.charAt(i)) {
						sb.append(tmp);
						tmp = "";
					}
				}
				File file = new File(sb.toString());
				if ((file.exists()) && (file.isDirectory())) {*/
                try {
                    new TcpServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
                //}
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        String property = properties.getProperty("Lang");
        if (property != null)
            Locale.setDefault(new Locale(property));

        property = properties.getProperty("SameProcess");
        if ("true".equals(property)) {
            try {
                CommandServlet.tcpServer = new TcpServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {

                File lib = new File(event.getServletContext().getRealPath(
                        "/WEB-INF/lib"));

                StringBuffer sb = null;
                for (File file : lib.listFiles()) {
                    if (file.getName().endsWith(".jar")) {
                        if (sb == null) {
                            sb = new StringBuffer();
                            sb.append(file.getAbsolutePath());
                        } else {
                            sb.append(File.pathSeparator);
                            sb.append(file.getAbsolutePath());
                        }
                    }
                }

                String classPath = sb.toString();
                // ProcessBuilder rmiServer = new ProcessBuilder(getVM(),
                // "-Xmx512m",
                // "-Dcatalina.base=" + System.getProperty("catalina.base"),
                // "-classpath", classPath,
                // "com.ramussoft.server.rmi.RMIServer");
                int i = 0;
                ArrayList<String> params = new ArrayList<String>();
                String param;
                while ((param = properties.getProperty("Arg_" + i)) != null) {
                    params.add(param);
                    i++;
                }

                List<String> command = new ArrayList<String>();
                command.add(getVM());
                for (String s : params)
                    command.add(s);
                command.add("-Xmx512m");
                command.add("-Dcatalina.base="
                        + System.getProperty("catalina.base"));
                command.add("-classpath");
                command.add(classPath);
                command.add("com.ramussoft.server.tcp.TcpServer");

                ProcessBuilder cajoServer = new ProcessBuilder(command);
                try {

                    // rmiProcess = rmiServer.start();
                    cajoProcess = cajoServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                try {
                    new TcpServer();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    protected String getVM() {
        return System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "java";
    }

}
