package com.ramussoft;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class Startup {

    private static final String JAVA_ARGUMENT = "--java-argument";

    public static void main(String[] args) {
        try {
            new Startup().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final boolean portable = false;

    public void start(String[] args) throws IOException {

        String version = System.getProperty("java.specification.version");
        if (version == null) {
            JOptionPane.showMessageDialog(null,
                    "Unknow java version, application may not work");
        } else {
            StringTokenizer st = new StringTokenizer(version, ".");
            String f = st.nextToken();
            String s = st.nextToken();
            if (f.equals("1")) {
                try {
                    if (Integer.parseInt(s) < 6) {
                        JOptionPane.showMessageDialog(null,
                                "Java version must be 1.6 or higher");
                        System.exit(1);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Unknow java version, application may not work");
                }
            }
        }

        String homePath = getParentFile(decode(getInstallLocation().getFile()))
                .getAbsolutePath();

        String cp = homePath
                + File.separator + "lib";
        List al = new ArrayList();
        loadLibs(new File(cp).listFiles(), al);
        ArrayList command = new ArrayList();
        command.add(getVM());
        command.add("-Xmx512m");
        command.add("-classpath");
        command.add(createClassPath(al.toArray()));
        command.add("-Dcom.sun.script.java.classpath="
                + createClassPath(al.toArray()));
        if (portable) {//portable
            command.add("-Duser.ramus.options="
                    + homePath + File.separator + "conf");
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals(JAVA_ARGUMENT)) {
                i++;
                if (i < args.length)
                    command.add(args[i]);
            }
        }

        boolean write = false;
        boolean close = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--write-command"))
                write = true;
            else if (args[i].equals("--close-startup"))
                close = true;
            else if (args[i].equals(JAVA_ARGUMENT)) {
                i++;
            } else
                command.add(args[i]);
        }
        if (write) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < command.size(); i++) {
                sb.append("\"" + command.get(i) + "\" ");
            }
            System.out.println(sb);
            return;
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();
        addHooks(close, p);
    }

    protected void addHooks(boolean close, Process p) {
        if (!close) {
            addHook(System.err, p.getErrorStream());
            addHook(System.out, p.getInputStream());
        }
    }

    private void addHook(final OutputStream out, final InputStream input) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                int r;
                try {
                    while ((r = input.read()) >= 0) {
                        out.write(r);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void loadLibs(File[] fs, List al) {
        for (int i = 0; i < fs.length; i++) {
            File c = fs[i];
            if (c.isFile()) {
                String name = c.getName().toLowerCase();
                if ((name.endsWith(".jar")) && (c.isFile())) {
                    al.add(c);
                }
            } else if ((c.isDirectory()) && (!c.getName().equals(".."))
                    && (!c.getName().equals("."))) {
                loadLibs(c.listFiles(), al);
            }
        }
    }

    public String createClassPath(Object[] paths) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < paths.length; i++) {
            sb.append(((File) paths[i]).getAbsolutePath());
            if (i < paths.length - 1)
                sb.append(File.pathSeparator);
        }
        return sb.toString();
    }

    protected String getVM() {
        return System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "java";
    }

    private File getParentFile(String s) {
        File f = new File(s);
        if (f.getName().equals(".."))
            return f;
        while (f.getName().equals("."))
            f = f.getParentFile();
        return f.getParentFile();
    }

    private URL getInstallLocation() throws MalformedURLException {

        URL installLocation = null;

        String pbl = System.getProperty("pb.launcher");
        if (pbl != null) {
            File file = new File(pbl);
            if (file.exists()) {
                return file.getParentFile().toURI().toURL();
            }
        }

        ProtectionDomain domain = getClass().getProtectionDomain();
        CodeSource source = null;
        URL result = null;
        if (domain != null)
            source = domain.getCodeSource();
        if (source == null || domain == null) {
            try {
                result = new URL("file:startup.jar"); //$NON-NLS-1$
            } catch (Exception e2) {
                // Ignore
            }
        }

        if (result == null)
            result = new File("x").toURI().toURL();

        if (source != null)
            result = source.getLocation();

        String path = decode(result.getFile());
        // normalize to not have leading / so we can check the form
        File file = new File(path);
        path = file.toString().replace('\\', '/');
        // If on Windows then canonicalize the drive letter to be lowercase.
        // remember that there may be UNC paths
        if (File.separatorChar == '\\')
            if (Character.isUpperCase(path.charAt(0))) {
                char[] chars = path.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                path = new String(chars);
            }
        if (path.toLowerCase().endsWith(".jar")) //$NON-NLS-1$
            path = path.substring(0, path.lastIndexOf("/") + 1); //$NON-NLS-1$
        try {
            try {
                // create a file URL (via File) to normalize the form (e.g., put
                // the leading / on if necessary)
                path = new File(path).toURI().toURL().getFile();
            } catch (MalformedURLException e1) {
                // will never happen. The path is straight from a URL.
            }
            installLocation = new URL(result.getProtocol(), result.getHost(),
                    result.getPort(), path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return installLocation;
    }

    /**
     * Returns a string representation of the given URL String. This converts
     * escaped sequences (%..) in the URL into the appropriate characters. NOTE:
     * due to class visibility there is a copy of this method in
     * InternalBootLoader
     */
    protected String decode(String urlString) {
        // try to use Java 1.4 method if available
        try {
            Class clazz = URLDecoder.class;
            Method method = clazz.getDeclaredMethod(
                    "decode", new Class[]{String.class, String.class}); //$NON-NLS-1$
            // first encode '+' characters, because URLDecoder incorrectly
            // converts
            // them to spaces on certain class library implementations.
            if (urlString.indexOf('+') >= 0) {
                int len = urlString.length();
                StringBuffer buf = new StringBuffer(len);
                for (int i = 0; i < len; i++) {
                    char c = urlString.charAt(i);
                    if (c == '+')
                        buf.append("%2B"); //$NON-NLS-1$
                    else
                        buf.append(c);
                }
                urlString = buf.toString();
            }
            Object result = method.invoke(null, new Object[]{urlString,
                    "UTF-8"}); //$NON-NLS-1$
            if (result != null)
                return (String) result;
        } catch (Exception e) {
            // JDK 1.4 method not found -- fall through and decode by hand
        }
        // decode URL by hand
        boolean replaced = false;
        byte[] encodedBytes = urlString.getBytes();
        int encodedLength = encodedBytes.length;
        byte[] decodedBytes = new byte[encodedLength];
        int decodedLength = 0;
        for (int i = 0; i < encodedLength; i++) {
            byte b = encodedBytes[i];
            if (b == '%') {
                if (i + 2 >= encodedLength)
                    throw new IllegalArgumentException(
                            "Malformed URL (\"" + urlString + "\"): % must be followed by 2 digits."); //$NON-NLS-1$//$NON-NLS-2$
                byte enc1 = encodedBytes[++i];
                byte enc2 = encodedBytes[++i];
                b = (byte) ((hexToByte(enc1) << 4) + hexToByte(enc2));
                replaced = true;
            }
            decodedBytes[decodedLength++] = b;
        }
        if (!replaced)
            return urlString;
        try {
            return new String(decodedBytes, 0, decodedLength, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            // use default encoding
            return new String(decodedBytes, 0, decodedLength);
        }
    }

    /**
     * Converts an ASCII character representing a hexadecimal value into its
     * integer equivalent.
     */
    private int hexToByte(byte b) {
        switch (b) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
            case 'a':
                return 10;
            case 'B':
            case 'b':
                return 11;
            case 'C':
            case 'c':
                return 12;
            case 'D':
            case 'd':
                return 13;
            case 'E':
            case 'e':
                return 14;
            case 'F':
            case 'f':
                return 15;
            default:
                throw new IllegalArgumentException("Switch error decoding URL"); //$NON-NLS-1$
        }
    }
}
