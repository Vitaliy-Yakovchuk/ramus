package com.ramussoft.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.pb.Function;

public final class RsfFixture {

    private RsfFixture() {
    }

    public static void isolateHome(File target) {
        target.mkdirs();
        System.setProperty("user.home", target.getAbsolutePath());
        System.setProperty("user.ramus.application.name", "RamusStorageTest");
        System.setProperty("java.awt.headless", "true");
    }

    public static List<File> sampleFiles() {
        File docs = new File(projectRoot(), "dest/doc");
        List<File> result = new ArrayList<File>();
        collectRsf(docs, result);
        Collections.sort(result, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getAbsolutePath().compareTo(b.getAbsolutePath());
            }
        });
        return result;
    }

    private static void collectRsf(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null)
            return;
        for (File child : children) {
            if (child.isDirectory())
                collectRsf(child, out);
            else if (child.getName().toLowerCase().endsWith(".rsf"))
                out.add(child);
        }
    }

    public static File projectRoot() {
        File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (dir != null && !new File(dir, "settings.gradle").isFile())
            dir = dir.getParentFile();
        if (dir == null)
            throw new IllegalStateException(
                    "No project root (settings.gradle) found from "
                            + System.getProperty("user.dir"));
        return dir;
    }

    public static void resave(File in, File out) throws IOException {
        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(in);
        try {
            Engine engine = database.getEngine(null);
            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveToFile(out);
            impl.close();
        } finally {
            database.close();
        }
    }

    public static void exportProject(File rsf, File directory)
            throws IOException {
        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(rsf);
        try {
            Engine engine = database.getEngine(null);
            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveProject(directory);
            impl.close();
        } finally {
            database.close();
        }
    }

    public static void importProject(File directory, File rsf)
            throws IOException {
        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(directory);
        try {
            Engine engine = database.getEngine(null);
            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveToFile(rsf);
            impl.close();
        } finally {
            database.close();
        }
    }

    public static void resaveProject(File source, File target)
            throws IOException {
        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(source);
        try {
            Engine engine = database.getEngine(null);
            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveProject(target);
            impl.close();
        } finally {
            database.close();
        }
    }

    public static String openFailure(File file) {
        MemoryDatabase database = null;
        try {
            database = (MemoryDatabase) FileDatabaseFactory
                    .createDatabase(file);
            Engine engine = database.getEngine(null);
            ((FileIEngineImpl) engine.getDeligate()).close();
            return null;
        } catch (Throwable e) {
            Throwable root = e;
            while (root.getCause() != null)
                root = root.getCause();
            return root.getClass().getSimpleName() + ": " + root.getMessage();
        } finally {
            if (database != null)
                try {
                    database.close();
                } catch (Exception ignore) {
                }
        }
    }

    public static final Date FIXED_DATE = fixedDate();

    private static Date fixedDate() {
        Calendar calendar = new GregorianCalendar(2020, Calendar.JANUARY, 1,
                12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static void freezeDates(Function function) {
        function.setCreateDate(FIXED_DATE);
        function.setRevDate(FIXED_DATE);
        for (int i = 0; i < function.getChildCount(); i++)
            freezeDates((Function) function.getChildAt(i));
    }

    public static void copy(File from, File to) throws IOException {
        InputStream in = new FileInputStream(from);
        try {
            OutputStream os = new FileOutputStream(to);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) > 0)
                    os.write(buffer, 0, read);
            } finally {
                os.close();
            }
        } finally {
            in.close();
        }
    }
}
