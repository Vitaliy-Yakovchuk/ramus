package com.ramussoft.gui.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ResourceServlet implements HttpHandler {
    public static final ResourceBundle RES = ResourceBundle
            .getBundle("com.ramussoft.gui.server.gui");

    private String hostname;

    private Date date;

    private SimpleDateFormat format;

    public ResourceServlet(String hostname, Date date) {
        this.hostname = hostname;
        this.date = date;
        format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",
                Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

    }

    private void copy(InputStream is, OutputStream stream) {
        try {
            byte[] buff = new byte[1024 * 64];
            int r;
            while ((r = is.read(buff)) > 0) {
                stream.write(buff, 0, r);
            }
        } catch (IOException e) {

        }

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String url = exchange.getRequestURI().toString();
        System.out.println(url);
        Headers responseHeaders = exchange.getResponseHeaders();
        if (url.equals("/download/ramus-client.jnlp")) {
            responseHeaders.set("Content-Type", "application/x-java-jnlp-file");
            exchange.sendResponseHeaders(200, 0);
            String address = exchange.getLocalAddress().getHostName();

            if (hostname != null)
                address = hostname;

            String port = Integer
                    .toString(exchange.getLocalAddress().getPort());

            String path = "http://" + address + ":" + port + "/download/";
            InputStream is = getClass().getResourceAsStream(
                    "/com/ramussoft/gui/server/ramus-client.jnlp");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int r;
            while ((r = is.read()) >= 0)
                out.write(r);
            String string = MessageFormat.format(new String(out.toByteArray(),
                    "UTF8"), path);
            responseHeaders.set("Last-Modified", format.format(date));

            OutputStream o = exchange.getResponseBody();
            o.write(string.getBytes("UTF8"));
            o.close();
        } else if (url.startsWith("/download/")) {
            InputStream is = getClass().getResourceAsStream(
                    "/com/ramussoft/" + url.substring("/download/".length()));
            try {
                responseHeaders.set("Content-Length", String.valueOf(is
                        .available()));
                responseHeaders.set("Content-Type",
                        "application/x-java-archive");
                responseHeaders.set("Last-Modified", format.format(date));
                exchange.sendResponseHeaders(200, 0);
                OutputStream responseBody = exchange.getResponseBody();
                copy(is, responseBody);
                responseBody.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            responseHeaders.set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);
            PrintWriter pw = new PrintWriter(exchange.getResponseBody());
            pw.println("<html><head><title>Ramus Client</title></head>");
            pw.println("<body>");
            pw.println(RES.getString("Action.Run"));
            pw
                    .println("<a href=\"download/ramus-client.jnlp\">Ramus Client</a><br>");
            pw.println(RES.getString("Java.Needed"));
            pw.println("<a href=\"http://java.com\">Java</a>");
            pw.println("</body>");
            pw.println("</html>");
            pw.close();
        }
    }
}
