package com.ramussoft.navigator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.Request;
import com.ramussoft.web.Response;

public class NavigatorServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -7422099580981678016L;

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

    private void accept(HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        try {

            resp.setCharacterEncoding(HTTPParser.ENCODING);

            Navigator navigator = Navigator.getNavigator();

            HTTPParser parser = navigator.getParser();

            Response response = new Response(resp.getOutputStream()) {
                @Override
                public void setContentType(String string) {
                    resp.setContentType(string);
                }

                @Override
                public void setContentDisposition(String string) {
                    resp.setHeader("Content-disposition", string);
                }

                @Override
                public OutputStream getStream() {
                    return stream;
                }

                @Override
                public PrintStream writeHead() throws IOException {
                    return null;
                }
            };

            int cl = req.getContentLength();

            String uri = req.getRequestURI();
            String path = req.getContextPath();
            String location = uri.substring(path.length()
                    + "/navigator/".length());

            Hashtable<String, String> hashtable = new Hashtable<String, String>();

            for (Object o : req.getParameterMap().keySet()) {
                String value = req.getParameter(o.toString());
                hashtable.put(o.toString(), value);
            }

            Request request = new Request(true, location, cl, req
                    .getHeader("User-Agent"), hashtable);

            parser.accept(request, response);

        } catch (Exception e) {
            if (e instanceof IOException)
                throw (IOException) e;
            throw new RuntimeException(e);
        }

    }

}
