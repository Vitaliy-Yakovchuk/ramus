package com.ramussoft.gui.spell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.ramussoft.gui.common.prefrence.Options;

public class SpellFactory {

    private static Language[] languages = new Language[]{};

    private static ResourceBundle langs = ResourceBundle
            .getBundle("com.ramussoft.gui.spell.languages");

    static {
        try {

            String path = Options.getPreferencesPath() + "dictionaries";
            File file = new File(path);
            file.mkdirs();
            SpellFactory spellFactory = new SpellFactory();
            spellFactory.tryCopyDictionaries(path);
            languages = spellFactory.loadDictionaries(file);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    for (Language language : languages) {
                        try {
                            language.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Language[] getLanguages() {
        return languages;
    }

    private Language[] loadDictionaries(File dir) throws ZipException,
            IOException {
        List<Language> list = new ArrayList<Language>(3);
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                ZipFile zipFile = new ZipFile(file);
                ZipEntry entry = zipFile.getEntry("language.properties");
                InputStream is = zipFile.getInputStream(entry);
                Properties properties = new Properties();
                if (is != null)
                    properties.load(is);
                is.close();
                String name = null;
                int dotPos = file.getName().indexOf(".");
                if (dotPos >= 0) {
                    String fn = file.getName().substring(0, dotPos);
                    if (fn.length() > 0) {
                        try {
                            name = langs.getString(fn);
                        } catch (Exception e) {

                        }
                    }
                }
                if (name == null)
                    name = properties.getProperty("default");

                list.add(new Language(name, zipFile));
            }
        }
        return list.toArray(new Language[list.size()]);
    }

    private void tryCopyDictionaries(String aPath) throws IOException {
        String path = aPath + File.separator;
        InputStream is = getClass().getResourceAsStream(
                "/com/ramussoft/gui/spell/languages.properties");
        Properties ps = new Properties();
        ps.load(is);
        is.close();
        Enumeration<Object> keys = ps.keys();
        while (keys.hasMoreElements()) {
            Object next = keys.nextElement();
            String next2 = (String) next;
            String resource = next2 + ".zip";
            File file = new File(path + resource);
            if (!file.exists()) {
                is = getClass().getResourceAsStream(
                        "/com/ramussoft/gui/spell/" + resource);
                copyStream(is, new FileOutputStream(file));
            }
        }

    }

    private void copyStream(InputStream is, FileOutputStream out)
            throws IOException {
        byte[] buff = new byte[1024 * 64];
        int r;
        while ((r = is.read(buff)) > 0) {
            out.write(buff, 0, r);
        }
        out.close();
        is.close();
    }

    public static Language findLanguage(Locale locale) {
        Language res = null;
        for (Language l : languages) {
            if (l.getLanguage().equals(locale.getLanguage())) {
                if (res == null)
                    res = l;
                String c = l.getCountry();
                if ((c != null) && (c.equals(locale.getCountry())))
                    res = l;
            }
        }
        return res;
    }

    public static Language findLanguage(String languageName) {
        for (Language l : languages) {
            if (l.getName().equals(languageName))
                return l;
        }
        return null;
    }

    public static Language getDefaultLanguage() {
        Language language = findLanguage(Locale.getDefault());
        if ((language == null) && (languages.length > 0))
            language = languages[0];
        return language;
    }

}
