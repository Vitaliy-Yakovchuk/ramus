package com.ramussoft.jnlp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ramussoft.server.EngineFactory;

public class JNLPSeasonInternetServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -4553365442507458842L;

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

    private void accept(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String localAddr = req.getLocalAddr();
            Properties properties = EngineFactory.getPropeties();
            if (properties.getProperty("hostname") != null) {
                localAddr = properties.getProperty("hostname");
            }
            String path = "http://" + localAddr + ":" + req.getLocalPort()
                    + req.getContextPath();
            InputStream is = getClass().getResourceAsStream(
                    "/com/ramussoft/jnlp/season-internet-client.jnlp");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int r;
            while ((r = is.read()) >= 0)
                out.write(r);
            String string = MessageFormat.format(new String(out.toByteArray(),
                    "UTF8"), path);
            resp.setContentType("application/x-java-jnlp-file");
            OutputStream o = resp.getOutputStream();
            o.write(string.getBytes("UTF8"));
            o.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
