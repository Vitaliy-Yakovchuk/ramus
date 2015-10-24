package com.ramussoft.http;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ramussoft.server.StartCommand;
import com.ramussoft.server.tcp.TcpServer;

public class CommandServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -7331687966506009861L;

    public static TcpServer tcpServer;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        accept(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        accept(req, resp);
    }

    private void accept(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html");

        PrintWriter writer = resp.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<pre>");
        String uri = req.getRequestURI();
        int i = uri.lastIndexOf('/') + 1;
        String command = uri.substring(i);
        startAnother(req, resp, command);

        writer.println("</pre>");
        writer.println("</body>");
        writer.println("</html>");
    }

    private void startAnother(HttpServletRequest req, HttpServletResponse resp,
                              String jsCommand) {
        try {
            File lib = new File(getServletContext().getRealPath("/WEB-INF/lib"));

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
            sb.append(File.pathSeparator);
            sb.append(new File(new File(getServletContext().getRealPath("/WEB-INF")), "classes").getAbsolutePath());

            String classPath = sb.toString();
            ArrayList<String> params = new ArrayList<String>();

            List<String> command = new ArrayList<String>();
            command.add(getVM());
            for (String s : params)
                command.add(s);
            command.add("-Xmx1512m");
            command.add("-Dcatalina.base="
                    + System.getProperty("catalina.base"));
            command.add("-classpath");
            command.add(classPath);
            command.add("com.ramussoft.server.StartCommand");
            String catalinaBase = System.getProperty("catalina.base");
            command.add(catalinaBase);
            command.add(jsCommand);

            sb = new StringBuffer();
            for (String s : command) {
                sb.append("\"");
                sb.append(s);
                sb.append("\" ");
            }

            System.out.println(sb);

            ProcessBuilder cajoServer = new ProcessBuilder(command);
            try {

                cajoServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            try {
                StartCommand.main(new String[]{jsCommand});
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    protected String getVM() {
        return System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "java";
    }

    public static boolean deleteFile(File f) {
        return f.delete();
    }
}
