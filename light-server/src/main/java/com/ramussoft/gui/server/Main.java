package com.ramussoft.gui.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.server.tcp.TcpServer;

public class Main {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.setProperty("user.ramus.application.name", "Ramus Light Server");
        new Main().start(args);
    }

    public void start(String[] args) throws IOException {
        init();
        start();
    }

    private void init() {
        final PrintStream old = System.err;
        System.setErr(new PrintStream(new OutputStream() {

            FileOutputStream fos = null;

            private boolean err = false;

            @Override
            public void write(final int b) throws IOException {
                getFos();
                if (!err)
                    fos.write(b);
                old.write(b);
            }

            private FileOutputStream getFos() throws IOException {
                if (fos == null) {
                    try {
                        final Calendar c = Calendar.getInstance();
                        String name = Options.getPreferencesPath() + "log";
                        new File(name).mkdir();
                        name += File.separator + c.get(Calendar.YEAR) + "_"
                                + c.get(Calendar.MONTH) + "_"
                                + c.get(Calendar.DAY_OF_MONTH) + "_"
                                + c.get(Calendar.HOUR_OF_DAY) + "_"
                                + c.get(Calendar.MINUTE) + "_"
                                + c.get(Calendar.SECOND) + "_"
                                + c.get(Calendar.MILLISECOND) + "-server.log";
                        fos = new FileOutputStream(name);
                    } catch (final IOException e) {
                        err = true;
                        e.printStackTrace();
                    }
                }
                return fos;
            }

        }));
    }

    private void start() throws IOException {
        String path = Options.getPreferencesPath();
        if ((path.endsWith(File.separator)) && (path.length() > 1))
            path = path.substring(0, path.length() - 1);
        System.setProperty("ramus.server.base", path);

        JNLPServer server = JNLPServer.createServer();
        if (server != null)
            server.start();
        new TcpServer();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("stop")) {
                System.out.println("Server stop");
                System.exit(0);
                return;
            }
        }
    }

}
