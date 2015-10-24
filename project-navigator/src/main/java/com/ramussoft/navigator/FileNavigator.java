package com.ramussoft.navigator;

import static com.ramussoft.navigator.ProjectNavigator.getString;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.Collator;
import java.text.MessageFormat;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.Request;
import com.ramussoft.web.Response;
import com.ramussoft.web.Servlet;

public class FileNavigator implements Comparable<FileNavigator> {

    private String fileName;

    private String modelName;

    private String prefix;

    private Object lock = new Object();

    private long lastModified = -100;

    private DataPlugin dataPlugin;

    private File tmpFile;

    private class FileLoader extends Thread {
        public FileLoader(String name) {
            super(name);
        }

        @Override
        public void run() {
            FileNavigator.this.run();
        }
    }

    ;

    private FileLoader fileLoader;

    public FileNavigator(String fileName, String modelName, String prefix) {
        this.fileName = fileName;
        if (modelName == null)
            modelName = "";
        this.modelName = modelName;
        this.prefix = prefix;
        fileLoader = new FileLoader(fileName);
        fileLoader.start();
    }

    public void run() {
        do {
            if (fileLoader.isInterrupted())
                return;
            synchronized (lock) {
                tryLoadFile();
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        } while (true);
    }

    private void tryLoadFile() {
        File file = new File(fileName);
        if (file.lastModified() > lastModified) {
            closeModel();
            try {
                tmpFile = File.createTempFile("navigator-file-copy", null);
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(tmpFile);
                copyStream(fis, fos);
                fis.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            MemoryDatabase database = new MemoryDatabase() {
                @Override
                protected File getFile() {
                    return tmpFile;
                }
            };

            Engine engine = database.getEngine(null);
            AccessRules rules = database.getAccessRules(null);
            dataPlugin = NDataPluginFactory.getDataPlugin(null, engine, rules);

            lastModified = file.lastModified();
        }
    }

    public void close() {
        synchronized (lock) {
            if (dataPlugin != null) {
                fileLoader.interrupt();
                closeModel();
            }
        }
    }

    private void closeModel() {
        if (dataPlugin != null)
            try {
                ((FileIEngineImpl) dataPlugin.getEngine().getDeligate())
                        .close();
                dataPlugin = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        if ((tmpFile != null) && (tmpFile.exists())) {
            if (!tmpFile.delete())
                tmpFile.deleteOnExit();
            tmpFile = null;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public Servlet getServlet(Request request, final String prefix) {
        synchronized (lock) {
            if (dataPlugin == null) {
                return new Servlet() {
                    @Override
                    public void accept(Request request, Response response)
                            throws IOException {
                        response.setContentType("text/html; charset="
                                + HTTPParser.ENCODING);
                        PrintStream pw = response.getWriter();
                        pw.println("<html><head>");
                        pw.println("<title>" + getString("Error.FileLoad")
                                + "</title>");
                        pw.println("<head><body>");
                        pw.println("<h3>" + getString("Error.FileLoad")
                                + "</h3><br>");
                        pw.println(MessageFormat.format(
                                getString("Error.CheckFile"), fileName));
                        pw.println("</body></html>");
                    }
                };
            }
            return new HTTPParser(dataPlugin, null);
        }
    }

    public String getModelName() {
        return modelName;
    }

    @Override
    public int compareTo(FileNavigator o) {
        Collator collator = Collator.getInstance();
        return collator.compare(this.modelName, o.modelName);
    }

    public String getFileName() {
        return fileName;
    }

    public static void copyStream(final InputStream in, final OutputStream out)
            throws IOException {
        final byte[] buff = new byte[1024 * 128];
        int r;
        while ((r = in.read(buff)) > 0)
            out.write(buff, 0, r);
    }
}
