package com.ramussoft.local;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Metadata;

public abstract class DesktopComunication {

    private boolean client;

    private RandomAccessFile file;

    private FileLock lock;

    private String lockFileName;

    private int port;

    private InetAddress local;

    public DesktopComunication() throws IOException {
        lockFileName = getLockFileName();
        file = new RandomAccessFile(lockFileName, "rws");
        lock = file.getChannel().tryLock(0, 1, false);
        local = InetAddress.getByName("localhost");

        if (lock == null) {
            client = true;

            file.seek(1);
            ByteArrayOutputStream stream = new ByteArrayOutputStream(10);
            int b;
            while ((b = file.read()) >= 0) {
                stream.write(b);
            }
            port = Integer.parseInt(new String(stream.toByteArray()));
            file.close();
            file = null;
        } else {
            client = false;
            InetSocketAddress address = new InetSocketAddress(local, 0);
            final ServerSocket ss = new ServerSocket();
            ss.bind(address);
            port = ss.getLocalPort();
            file.write("X".getBytes());
            file.seek(1);
            file.writeBytes(Integer.toString(port));
            Thread listenerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            Socket socket = ss.accept();
                            try {
                                InetAddress client = socket.getInetAddress();
                                if (!local.equals(client)) {
                                    System.err.println("Attack? " + client);
                                } else {
                                    BufferedReader rdr = new BufferedReader(
                                            new InputStreamReader(socket
                                                    .getInputStream(), "UTF8"),
                                            1);
                                    List<String> input = new ArrayList<String>();
                                    String line;
                                    while ((line = rdr.readLine()) != null) {
                                        input.add(line);
                                    }

                                    final String[] args = input
                                            .toArray(new String[input.size()]);
                                    applyArgs(args);

                                    rdr.close();
                                }
                            } finally {
                                socket.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            listenerThread.setName("Local-Application-Listener:" + port);
            listenerThread.start();
        }
    }

    private String getLockFileName() {
        String tmp = System.getProperty("java.io.tmpdir");
        if (!tmp.endsWith(File.separator))
            tmp += File.separator;
        StringBuffer sb = new StringBuffer();
        String dstring = System.getenv("DISPLAY");
        if (dstring == null)
            dstring = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getIDstring();
        else
            dstring += GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getIDstring();
        for (char c : dstring.toCharArray()) {
            if ((Character.isLetterOrDigit(c)) || (c == '.'))
                sb.append(c);
            else
                sb.append((int) c);
        }
        String res = tmp + getFileTitle() + "-"
                + System.getProperty("user.name") + "-" + sb.toString()
                + Metadata.getApplicationVersion() + ".xlock";

        return res;
    }

    protected String getFileTitle() {
        return Metadata.getApplicationName().toLowerCase().replaceAll(" ", "-");
    }

    public abstract void applyArgs(String[] args);

    public boolean isClient() {
        return client;
    }

    public void close() throws IOException {
        if (!isClient()) {
            lock.release();
            file.close();
            new File(lockFileName).delete();
        }
    }

    public void send(String[] args) throws IOException {
        if (!isClient())
            throw new RuntimeException("This object is a server");
        Socket client = new Socket(local, port);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(client
                .getOutputStream(), "UTF8"));
        for (String arg : args) {
            writer.println(arg);
        }

        writer.close();
        client.close();
    }

}
