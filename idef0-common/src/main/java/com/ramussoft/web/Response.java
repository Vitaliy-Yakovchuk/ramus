package com.ramussoft.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;


public class Response {

    private String contentType = null;

    private final Hashtable<String, String> cookies = new Hashtable<String, String>();

    protected OutputStream stream = null;

    private java.io.PrintStream writer = null;

    private String contentDisposition = null;

    private String ramusContentDisposition = null;

    public Response(final OutputStream stream) {
        super();
        this.stream = stream;
    }

    public void setContentType(final String string) {
        contentType = string;
    }

    public void setCookie(final String key, final String value) {
        cookies.put(key, value);
    }

    public java.io.PrintStream writeHead() throws IOException {
        final java.io.PrintStream out = new java.io.PrintStream(stream, false, HTTPParser.ENCODING);
        writer = out;
        out.println("HTTP/1.1 200 OK");
        out.println("Server: RamusLightServer");
        out.println("Expires: Mon, 01 Jan 1990 00:00:00 GMT");
        if (contentType != null)
            out.println("Content-type: " + contentType);
        if (contentDisposition != null) {
            out.println("Content-disposition: " + contentDisposition);
        }
        if (ramusContentDisposition != null)
            out.println("Ramus-content-disposition: " + ramusContentDisposition);
        if (cookies.size() > 0) {
            String s = "";
            final Enumeration<String> e = cookies.keys();
            while (e.hasMoreElements()) {
                final String key = e.nextElement();
                s += key + "=" + cookies.get(key) + ";";
                if (e.hasMoreElements())
                    s += " ";
            }
            out.println("Set-Cookie: " + s);
        }
        out.println();
        return out;
    }

    public java.io.PrintStream getWriter() throws IOException {
        return writeHead();
    }

    public OutputStream getStream() throws IOException {
        //writeHead().flush();
        return stream;
    }

    public void close() throws IOException {
        if (writer != null)
            writer.flush();
        stream.close();
    }

    public void setContentDisposition(String string) {
        this.contentDisposition = string;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public void setRamusContentDisposition(String ramusContentDisposition) {
        this.ramusContentDisposition = ramusContentDisposition;
    }
}
