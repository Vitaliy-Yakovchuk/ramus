package com.ramussoft.client;

import java.awt.Label;
import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarOutputStream;
import com.ramussoft.core.Pack200;
import com.ramussoft.core.Pack200.Unpacker;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JOptionPane;

public class WebStartClient {

    private ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.client.client");

    private WebStartClient() {
    }

    public static void main(String[] args) throws UnavailableServiceException,
            IOException {
        new WebStartClient().run(args);
    }

    /**
     * @param args
     * @throws UnavailableServiceException
     * @throws IOException
     */
    private void run(String[] args) throws UnavailableServiceException,
            IOException {

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

        BasicService bs = (BasicService) ServiceManager
                .lookup("javax.jnlp.BasicService");
        URL url = bs.getCodeBase();

        // URL url = new URL("http://localhost:8080/");
        String hostPort;
        if (url.getPort() == 80)
            hostPort = url.getHost();
        else
            hostPort = url.getHost() + ":" + url.getPort();
        String server = MessageFormat.format("http://{0}/ramus/remoting/",
                hostPort);

        // JOptionPane.showMessageDialog(null, server);

        String string = System.getProperty("user.home");
        if (!string.endsWith(File.separator))
            string += File.separator;

        string += ".ramus" + File.separator + "client-libs";

        File file = new File(string);

        File libConf = new File(file, "lib.conf");

        long lm = -1;
        if ((file.exists()) && (libConf.exists())) {
            Properties cp = new Properties();
            FileInputStream fileInputStream = new FileInputStream(libConf);
            cp.load(fileInputStream);
            fileInputStream.close();
            String property = cp.getProperty("LastModified");
            if (property != null) {
                lm = Long.parseLong(property);
            }
        } else
            file.mkdirs();

        InputStream is = getClass().getResourceAsStream(
                "/com/ramussoft/client/lib.conf");
        Properties ps = new Properties();
        if (is != null) {
            ps.load(is);
            is.close();
        } else {
            lm = 0l;
        }
        String property = ps.getProperty("LastModified");
        long cur = (property == null) ? 0l : Long.parseLong(property);
        if (cur != lm) {

            if (!deleteDir(file)) {
                JOptionPane.showMessageDialog(null, bundle
                        .getString("CannotUpdate"));
                return;
            }

            file.mkdir();

            is = getClass()
                    .getResourceAsStream("/com/ramussoft/client/lib.zip");
            Unpacker unpacker = Pack200.newUnpacker();

            Window window = new Window(null);
            window.add(new Label(bundle.getString("Client.Unpacking")));

            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            if (is != null) {
                ZipInputStream zis = new ZipInputStream(is);
                ZipEntry ze;
                byte[] buff = new byte[1024 * 128];
                while ((ze = zis.getNextEntry()) != null) {
                    String name = string + File.separator + ze.getName();
                    if (ze.isDirectory()) {
                        File c = new File(name);
                        c.mkdirs();
                    } else {

                        if (name.endsWith(".pack")) {
                            String fileName = name.substring(0,
                                    name.length() - 5)
                                    + ".jar";

                            int r;
                            ByteArrayOutputStream fos = new ByteArrayOutputStream();
                            while ((r = zis.read(buff)) > 0) {
                                fos.write(buff, 0, r);
                            }
                            fos.close();

                            FileOutputStream fostream = new FileOutputStream(
                                    fileName);
                            JarOutputStream jostream = new JarOutputStream(
                                    fostream);
                            unpacker.unpack(new ByteArrayInputStream(fos
                                    .toByteArray()), jostream);
                            jostream.close();
                        } else {
                            String fileName = name;

                            int r;
                            FileOutputStream fos = new FileOutputStream(
                                    fileName);
                            while ((r = zis.read(buff)) > 0) {
                                fos.write(buff, 0, r);
                            }
                            fos.close();
                        }
                    }
                }
                zis.close();
                file.setLastModified(cur);
            }
            FileOutputStream fos = new FileOutputStream(libConf);
            ps.store(fos, "Version on client (in miliseconds)");
            fos.close();
            window.setVisible(false);
        }

        List<File> al = new ArrayList<File>();
        loadLibs(file.listFiles(), al);

        ArrayList<String> command = new ArrayList<String>();
        command.add(getVM());
        command.add("-Xmx512m");
        command.add("-classpath");
        command.add(createClassPath(al.toArray()));
        command.add("-Dcom.sun.script.java.classpath="
                + createClassPath(al.toArray()));
        command.add("com.ramussoft.client.LightClient");
        command.add(server);
        ProcessBuilder pb = new ProcessBuilder(command);
        // JOptionPane.showMessageDialog(null, command);
        pb.start();
        System.exit(0);
    }

    private void loadLibs(File[] fs, List<File> al) {
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

    private boolean deleteDir(File file) {
        return remDir(file);
    }

    private class RN {
        File a;
        File b;

        public RN(File a, File b) {
            this.a = a;
            this.b = b;
        }

        public boolean move() {
            if (b == null)
                return a.delete();
            return a.renameTo(b);
        }

        public void rallBack() {
            if (b == null)
                a.mkdir();
            else
                b.renameTo(a);
        }

        public void remove() {
            if (b != null)
                b.delete();
        }
    }

    private Vector<RN> rns = new Vector<RN>();
    private int packageName;
    private int WORKING_PATH;

    private boolean remDir(File f) {
        rns.clear();
        boolean res = true;
        if (f.exists()) {
            recRemove(f);
            for (int i = 0; i < rns.size(); i++) {
                res = rns.get(i).move();
                if (!res) {
                    for (int j = i - 1; j >= 0; j--) {
                        rns.get(j).rallBack();
                    }
                    System.err.println("Error, can't remove " + f
                            + " for updating, rallback");
                    return false;
                }
            }
        }
        for (RN rn : rns)
            rn.remove();
        return res;
    }

    private void recRemove(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (File c : files) {
                if ((!".".equals(c.getName())) && (!"..".equals(c.getName()))) {
                    recRemove(c);

                }
            }
            rns.add(new RN(f, null));
        } else
            rns.add(new RN(f, new File(WORKING_PATH + packageName + "_"
                    + rns.size() + "_" + System.currentTimeMillis())));

    }

    private String getVM() {
        return System.getProperty("java.home") + File.separator + "bin"
                + File.separator + "java";
    }
}
