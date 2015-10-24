package com.ramussoft.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

public class Request {

    private final Hashtable<String, String> params = new Hashtable<String, String>();

    private static final String GET = "GET /";

    private static final String POST = "POST /";

    private static final String COOKIE = "Cookie: ";

    private static final String CONTENT_LENGTH = "Content-Length: ";

    private static final String USER_AGENT = "User-Agent: ";

    private static final String ID = "SID";

    private String location;

    private Hashtable<String, String> cookies = null;

    boolean get = true;

    private final InputStream stream;

    private final Response response;

    private final Server server;

    private int contentLength;

    private String userAgent;

    private static int getHex(final char c) {
        if (c >= '0' && c <= '9')
            return c - '0';
        else
            return c - 'A' + 10;
    }

    private static String decoderString(final String line) {
        byte[] bytes = line.getBytes();
        try {
            bytes = new String(bytes, "UTF8").getBytes();
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final byte[] buff = new byte[line.length()];
        int pos = 0;

        byte c;
        int i = 0;
        while (i < bytes.length) {
            c = bytes[i];
            if (c == '%' && i < bytes.length - 2) {
                final char c1 = (char) bytes[i + 1];
                final char c2 = (char) bytes[i + 2];
                c = (byte) (getHex(c1) * 16);
                c += getHex(c2);
                i += 2;
            } else if (c == '+')
                c = ' ';
            buff[pos] = c;
            pos++;
            i++;
        }
        return new String(buff, 0, pos);
    }

    private static void readParams(final String line,
                                   final Hashtable<String, String> params) {
        params.clear();
        final StringTokenizer st = new StringTokenizer(line, "&");
        try {
            while (st.hasMoreTokens()) {
                final StringTokenizer tmp = new StringTokenizer(st.nextToken(),
                        "=");
                final String key = tmp.nextToken();
                final String value = decoderString(tmp.nextToken());
                params.put(key, value);
            }
        } catch (final Exception e) {

        }
    }

    public static String getParams(final String line,
                                   final Hashtable<String, String> params) {
        char c;
        int j = line.length();
        params.clear();
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                j = i;
                break;
            }
        }
        String res = null;
        for (int i = 0; i < j; i++) {
            c = line.charAt(i);
            if (c == '#') {
                res = line.substring(0, i);
            } else if (c == '?') {
                if (res == null) {
                    res = line.substring(0, i);
                }
                readParams(line.substring(i + 1, j), params);
                break;
            }
        }
        if (res == null) {
            res = line.substring(0, j);
        }
        return res;
    }

    private class LineReader {
        private final InputStream stream;

        private boolean readed = false;

        public LineReader(final InputStream stream) {
            this.stream = stream;
        }

        public String readLine() throws IOException {
            int b;
            final StringBuffer sb = new StringBuffer();
            while (true) {
                b = stream.read();
                if (b == -1) {
                    if (!readed)
                        return null;
                    else
                        break;
                } else {
                    if (b == '\n') {
                        break;
                    } else {
                        readed = true;
                        if (b != '\r')
                            sb.append((char) b);
                    }
                }
            }
            readed = false;
            return sb.toString();
        }
    }

    ;

    public Request(boolean get, String location, int contentLength,
                   String userAgent, Map<String, String> params) {
        this.stream = null;
        this.response = null;
        this.server = null;
        this.get = get;
        this.location = location;
        this.contentLength = contentLength;
        this.userAgent = userAgent;
        this.params.putAll(params);
    }

    public Request(final InputStream stream, final Response response,
                   final Server server) throws IOException {
        super();
        this.stream = stream;
        this.server = server;
        this.response = response;
        contentLength = -1;
        final LineReader br = new LineReader(stream);
        String tmp;
        get = true;
        while ((tmp = br.readLine()) != null) {
            if (tmp.equals("")) {
                if (!get && contentLength > 0) {
                    final byte[] bs = new byte[contentLength];
                    stream.read(bs);
                    tmp = new String(bs);
                    readParams(tmp, params);
                }
                break;
            }

            // System.err.println(tmp);

            if (tmp.length() > GET.length()
                    && tmp.substring(0, GET.length()).equals(GET)) {
                get = true;
                location = getParams(tmp.substring(GET.length()), params);
            } else if (tmp.length() > POST.length()
                    && tmp.substring(0, POST.length()).equals(POST)) {
                get = false;
                location = getParams(tmp.substring(POST.length()), params);
            } else if (tmp.length() > COOKIE.length()
                    && tmp.substring(0, COOKIE.length()).equals(COOKIE)) {
                cookies = getCookies(tmp.substring(COOKIE.length()));
            } else if (tmp.length() > CONTENT_LENGTH.length()
                    && CONTENT_LENGTH.equals(tmp.substring(0, CONTENT_LENGTH
                    .length())))
                contentLength = new Integer(tmp.substring(CONTENT_LENGTH
                        .length()));
            else if (tmp.startsWith(USER_AGENT)) {
                this.userAgent = tmp.substring(USER_AGENT.length());
            }
        }

        // System.out.println("------------------------------");

    }

    private Hashtable<String, String> getCookies(final String string) {
        final StringTokenizer st = new StringTokenizer(string, "; ");
        final Hashtable<String, String> res = new Hashtable<String, String>();
        while (st.hasMoreTokens()) {
            final String tmp = st.nextToken();
            final StringTokenizer t = new StringTokenizer(tmp, "=");
            final String key = t.nextToken();
            final String value = t.nextToken();
            res.put(key, value);
        }
        return res;
    }

    public boolean isGet() {
        return get;
    }

    public String getLocation() {
        return location;
    }

    public Hashtable<String, String> getCookies() {
        return cookies;
    }

    public InputStream getInputStream() {
        return stream;
    }

    public Session getSession() {
        String id = null;
        if (cookies != null)
            id = cookies.get(ID);
        Session res = null;
        if (id != null)
            res = server.getSesionbyId(id);
        if (res == null) {
            res = new Session();
            server.addSession(res);
            response.setCookie(ID, res.getId());
        } else
            res.setLastUpdate(System.currentTimeMillis());
        return res;
    }

    public Hashtable<String, String> getParams() {
        return params;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
