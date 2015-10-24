package com.ramussoft.pb.dmaster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import com.dsoft.utils.Options;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

public class TemplateFactory {

    private static SimpleTemplate simpleModel = new SimpleTemplate();

    private static ClassicTemplate classicModel = new ClassicTemplate();

    private static DetailedTemplate ditalizatedModel = new DetailedTemplate();

    private static final String templateDir = Options.getOptionsExistsPath()
            + "templates";

    static {
        final File file = new File(templateDir);
        if (!file.exists())
            file.mkdir();
    }

    public static Vector<Template> getTemplates(Vector<Template> data,
                                                int decompositionType) {
        data.clear();
        data.add(simpleModel);
        data.add(classicModel);
        data.add(ditalizatedModel);
        data = getUserTemplates(data, decompositionType);
        return data;
    }

    private static UserTemplate findUserTemplate(final String name) {
        final Vector<Template> ts = getUserTemplates(new Vector<Template>(), -2);
        for (final Template t : ts) {
            if (t.toString().equals(name))
                return (UserTemplate) t;
        }
        return null;
    }

    public static Vector<Template> getUserTemplates(final Vector<Template> res,
                                                    int decompositionType) {
        final File dir = new File(templateDir);
        final Vector<UserTemplate> data = new Vector<UserTemplate>();
        final String[] list = dir.list();
        for (final String l : list) {
            boolean b = false;
            if (decompositionType != -2) {
                if (decompositionType == MovingArea.DIAGRAM_TYPE_DFD) {
                    if (l.endsWith(".dfd"))
                        b = true;
                } else {
                    if (!l.endsWith(".dfd"))
                        b = true;
                }
            } else
                b = true;
            if (b)
                try {
                    //Long.parseLong(l, 16);
                    try {
                        final String fn = templateDir + File.separator + l;
                        final FileInputStream fis = new FileInputStream(fn);
                        final UserTemplate ut = new UserTemplate(fis);
                        if (l.endsWith(".dfd"))
                            ut.setDecompositionType(MovingArea.DIAGRAM_TYPE_DFD);
                        fis.close();
                        ut.fileName = fn;
                        data.add(ut);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                } catch (final Exception e) {
                }
        }
        final UserTemplate[] uts = data.toArray(new UserTemplate[data.size()]);

        Arrays.sort(uts);
        for (final UserTemplate ut : uts)
            res.add(ut);
        return res;
    }

    public static UserTemplate renameUserTemplate(final String oldName,
                                                  final String newName) {
        final UserTemplate ut = findUserTemplate(oldName);
        if (ut != null) {
            ut.setName(newName);
            updateUserTemplate(ut);
        }
        return ut;
    }

    public static void updateUserTemplate(final UserTemplate ut) {
        try {
            final FileOutputStream fos = new FileOutputStream(ut.fileName);
            ut.saveToStream(fos);
            fos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeUserTemplate(final String name) {
        final UserTemplate ut = findUserTemplate(name);
        if (ut != null) {
            new File(ut.fileName).delete();
        }
    }

    public static boolean isPresent(final String name) {
        final Vector<Template> v = getTemplates(new Vector<Template>(), -2);
        for (final Template t : v) {
            if (t.toString().equals(name))
                return true;
        }
        return false;
    }

    public static void saveTemplate(final DataPlugin dataPlugin,
                                    final Function function, final String name,
                                    final SectorRefactor refactor) {
        final UserTemplate ut = new UserTemplate(function, dataPlugin, name,
                refactor);
        long c = System.currentTimeMillis();
        String fileName;
        File file;
        do {
            fileName = Long.toString(c, 16);
            if (function.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFD)
                fileName += ".dfd";
            c++;
        } while ((file = new File(templateDir + File.separator + fileName))
                .exists());

        try {
            final FileOutputStream fos = new FileOutputStream(file);
            ut.saveToStream(fos);
            fos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
