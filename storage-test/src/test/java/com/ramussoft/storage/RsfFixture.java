package com.ramussoft.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Допоміжні засоби для тестів формату: пошук зразків, відкриття та перезапис
 * {@code .rsf} без GUI.
 */
public final class RsfFixture {

    private RsfFixture() {
    }

    /**
     * Ізолює тест від домашнього каталогу користувача: сесії та тимчасові файли
     * рушія підуть у {@code target}, а не в {@code ~/.ramus}.
     */
    public static void isolateHome(File target) {
        target.mkdirs();
        System.setProperty("user.home", target.getAbsolutePath());
        System.setProperty("user.ramus.application.name", "RamusStorageTest");
        System.setProperty("java.awt.headless", "true");
    }

    /**
     * Усі зразкові {@code .rsf} з {@code dest/doc}, відсортовані за шляхом,
     * щоб порядок тестів був відтворюваним.
     */
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

    /**
     * Корінь репозиторію. Gradle запускає тести з каталогу модуля, тож
     * піднімаємось, доки не побачимо {@code settings.gradle}.
     */
    public static File projectRoot() {
        File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (dir != null && !new File(dir, "settings.gradle").isFile())
            dir = dir.getParentFile();
        if (dir == null)
            throw new IllegalStateException(
                    "Не знайдено корінь проєкту (settings.gradle) від "
                            + System.getProperty("user.dir"));
        return dir;
    }

    /**
     * Відкриває {@code in}, зберігає під іменем {@code out} і закриває рушій.
     * Модель при цьому не змінюється — це чистий цикл читання/запису.
     */
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

    /**
     * Відкриває {@code .rsf} і записує його як проєкт нового формату.
     */
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

    /**
     * Відкриває проєкт нового формату і зберігає його як {@code .rsf}.
     */
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

    /**
     * Відкриває проєкт нового формату і зберігає його назад у каталог —
     * цикл читання/запису без проміжного {@code .rsf}.
     */
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

    /**
     * Чи здатна поточна версія відкрити файл. Зразки, збережені старшими
     * версіями, можуть посилатися на плагіни, яких у коді вже немає.
     *
     * @return {@code null}, якщо файл відкривається; інакше — причина відмови.
     */
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
                    // з'єднання вже могло не відкритись
                }
        }
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
