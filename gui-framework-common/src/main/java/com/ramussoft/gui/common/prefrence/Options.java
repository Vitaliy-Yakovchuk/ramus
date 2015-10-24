/*
 * Created on 20/6/2005
 */
package com.ramussoft.gui.common.prefrence;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.ramussoft.common.Metadata;

/**
 * @author ZDD
 */
public class Options {

    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");

    private static Properties properties = new Properties();

    @SuppressWarnings("unchecked")
    private static Hashtable propertiesTable = new Hashtable();

    private static final String VISUAL_OPTIONS = "visuals";

    private static String optionsPath = getOptionsExistsPathX();

    private static FileLock locked = null;

    private static FileChannel channel = null;

    private static final String LANDSCAPE = "LANDSCAPE";

    private static final String PORTRAIT = "PORTRAIT";

    private static final String REVERSE_LANDSCAPE = "REVERSE_LANDSCAPE";

    private static final String ORIENTATION = "orientation";

    private static final String PAPER_IMAGEABLE_HEIGHT = "paper.ImageableHeight";

    private static final String PAPER_IMAGEABLE_WIDTH = "paper.ImageableWidth";

    private static final String PAPER_HEIGHT = "paper.Height";

    private static final String PAPER_WIDTH = "paper.Width";

    private static final String PAPER_IMAGEABLE_X = "paper.ImageableX";

    private static final String PAPER_IMAGEABLE_Y = "paper.ImageableY";

    private static String getOptionsExistsPathX() {
        String homeName = System.getProperty("user.ramus.options");
        if (homeName == null)
            homeName = new OptionsDirectoryGetter() {
                @Override
                public String getProgramName() {
                    String name = System
                            .getProperty("user.ramus.application.name");
                    if (name == null)
                        return Metadata.getApplicationName();
                    return name;
                }

            }.getDirectoryName();

        final String path = homeName;

        new File(path).mkdirs();

        createDefauldOptions(path, homeName);

        String res = path + FILE_SEPARATOR;
        load(res);
        return res;
    }

    private static void createDefauldOptions(final String path,
                                             final String homeName) {
        String r = "ramus";
        if ("".equals(r))
            return;
        final InputStream is = r.getClass().getResourceAsStream(
                "/com/ramussoft/gui/" + r + ".zip");
        if (is == null)
            return;
        try {
            final ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                final String name = ze.getName();
                if (name.endsWith("/")) {
                    final String dir = path + FILE_SEPARATOR
                            + name.substring(0, name.length() - 1);
                    new File(dir).mkdirs();
                } else {
                    final String file = path + FILE_SEPARATOR + name;
                    if (!new File(file).exists()) {
                        System.out.println("Creating " + file);
                        final FileOutputStream fos = new FileOutputStream(file);
                        copy(zis, fos);
                        fos.close();
                    }
                }
            }
            zis.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void copy(final ZipInputStream zis,
                             final FileOutputStream fos) throws IOException {
        final byte[] buff = new byte[1024 * 64];
        int r;
        while ((r = zis.read(buff)) > 0) {
            fos.write(buff, 0, r);
        }
    }

    public static String getPreferencesPath() {
        return optionsPath;
    }

    public static void save() {
        save(getPreferencesPath());
        if (channel == null)
            return;
        try {
            locked.release();
            channel.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected Options() {
        super();
    }

    private static String getFileNameForKey(final String key) {
        return getPreferencesPath() + VISUAL_OPTIONS + "/" + key + ".conf";
    }

    @SuppressWarnings("unchecked")
    public synchronized static void save(final String path) {
        save(path + "options.conf", properties,
                "Main options configuration file");
        final String vDir = path + VISUAL_OPTIONS;
        final File f = new File(vDir);
        f.mkdirs();
        final Enumeration e = propertiesTable.keys();
        while (e.hasMoreElements()) {
            final String key = e.nextElement().toString();
            save(vDir + "/" + key + ".conf", (Properties) propertiesTable
                    .get(key), key);
        }
    }

    private static void save(final String fileName,
                             final Properties properties, final String comments) {
        try {
            final FileOutputStream f = new FileOutputStream(fileName);
            properties.store(f, comments);
            f.close();
        } catch (final Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    public static void load(final String path) {
        load(path + "options.conf", properties);
    }

    private static void load(final String fileName, final Properties properties) {
        try {
            final File file = new File(fileName);
            if (file.exists()) {
                final FileInputStream f = new FileInputStream(file);
                properties.load(f);
            }
        } catch (final Exception e) {
            System.out.println(e);
        }
    }

    public static String getString(final String name) {
        return getString(name, properties);
    }

    private static String getString(final String name,
                                    final Properties properties) {
        return properties.getProperty(name);
    }

    public static String getString(final String name, final String defValue) {
        return getString(name, defValue, properties);
    }

    public static String getString(final String name, final String defValue,
                                   final Properties properties) {
        if (properties.getProperty(name) == null)
            setString(name, defValue, properties);
        return properties.getProperty(name, defValue);
    }

    private static Integer getObjectInteger(final String name,
                                            final Properties properties) {
        final String tmp = properties.getProperty(name);
        if (tmp == null)
            return null;
        return new Integer(tmp);
    }

    private static Double getObjectDouble(final String name,
                                          final Properties properties) {
        final String tmp = properties.getProperty(name);
        if (tmp == null)
            return null;
        return new Double(tmp);
    }

    public static int getInteger(final String name) {
        return getInteger(name, properties);
    }

    private static int getInteger(final String name, final Properties properties) {
        return getObjectInteger(name, properties).intValue();
    }

    public static double getDouble(final String name) {
        return getDouble(name, properties);
    }

    private static double getDouble(final String name,
                                    final Properties properties) {
        return getObjectDouble(name, properties).doubleValue();
    }

    public static double getDouble(final String name, final double defValue) {
        return getDouble(name, defValue, properties);
    }

    private static double getDouble(final String name, final double defValue,
                                    final Properties properties) {
        if (properties.getProperty(name) == null)
            setDouble(name, defValue, properties);
        return new Double(properties.getProperty(name, Double
                .toString(defValue))).doubleValue();
    }

    public static int getInteger(final String name, final int defValue) {
        return getInteger(name, defValue, properties);
    }

    private static int getInteger(final String name, final int defValue,
                                  final Properties properties) {
        if (properties.getProperty(name) == null)
            setInteger(name, defValue, properties);
        return new Integer(properties.getProperty(name, Integer
                .toString(defValue))).intValue();
    }

    public static void setString(final String name, final String value) {
        setString(name, value, properties);
    }

    public static void setString(final String name, final String value,
                                 final Properties properties) {
        properties.setProperty(name, value);
    }

    public static void setInteger(final String name, final int value) {
        setInteger(name, value, properties);
    }

    private static void setInteger(final String name, final int value,
                                   final Properties properties) {
        properties.setProperty(name, Integer.toString(value));
    }

    public static void setDouble(final String name, final double value) {
        setDouble(name, value, properties);
    }

    private static void setDouble(final String name, final double value,
                                  final Properties properties) {
        properties.setProperty(name, Double.toString(value));
    }

    public static void loadOptions(final Container component) {
        loadOptions(component.getClass().getName(), component);
    }

    public static void loadOptions(final String name, final Container component) {
        final Properties properties = getProperties(name);
        loadOptions(name, component, properties);
    }

    public static void loadOptions(final String name,
                                   final Container component, final Properties properties) {
        loadOptions(name, component, properties, true);
    }

    public static void loadOptions(final String name,
                                   final Container component, final Properties properties,
                                   boolean loadSizes) {
        if (loadSizes) {
            if (isParent(JFrame.class, component.getClass())) {
                ((JFrame) component).setBounds(getRectangle(name,
                        ((JFrame) component).getBounds(), properties));
            }

            if (isParent(JDialog.class, component.getClass())) {
                ((JDialog) component).setBounds(getRectangle(name,
                        ((JDialog) component).getBounds(), properties));
            }
        }

        for (int i = 0; i < component.getComponentCount(); i++) {
            if (isParent(JTable.class, component.getComponent(i).getClass()))
                getJTableOptions(name + "_" + i, (JTable) component
                        .getComponent(i), properties);
            if (isParent(JSplitPane.class, component.getComponent(i).getClass())) {

                String s = properties.getProperty(name + "__" + i);

                if (s != null) {

                    ((JSplitPane) component.getComponent(i))
                            .setDividerLocation(getInteger(name + "__" + i,
                                    ((JSplitPane) component.getComponent(i))
                                            .getDividerLocation(), properties));
                }

            }

            if (isParent(Container.class, component.getComponent(i).getClass()))
                loadOptions(name + "X" + i, (Container) component
                        .getComponent(i), properties);
        }
    }

    public static void saveOptions(final String name, final Container component) {
        final Properties properties = getProperties(name);
        saveOptions(name, component, properties);
    }

    public static void saveOptions(Container component) {
        saveOptions(component.getClass().getName(), component);
    }

    public static void saveOptions(final String name,
                                   final Container component, final Properties properties) {
        if (component == null)
            return;
        if (isParent(JFrame.class, component.getClass())) {
            setRectangle(name, ((JFrame) component).getBounds(), properties);
        }

        if (isParent(JDialog.class, component.getClass())) {
            setRectangle(name, ((JDialog) component).getBounds(), properties);
        }

        for (int i = 0; i < component.getComponentCount(); i++) {
            if (isParent(JTable.class, component.getComponent(i).getClass()))
                setJTableOptions(name + "_" + i, (JTable) component
                        .getComponent(i), properties);

            if (isParent(JSplitPane.class, component.getComponent(i).getClass())) {
                setInteger(name + "__" + i, ((JSplitPane) component
                        .getComponent(i)).getDividerLocation(), properties);

            }

            if (isParent(Container.class, component.getComponent(i).getClass()))
                saveOptions(name + "X" + i, (Container) component
                        .getComponent(i), properties);
        }
    }

    public static void setRectangle(final String name, final Rectangle rectangle) {
        setRectangle(name, rectangle, properties);
    }

    public static void setRectangle(final String name,
                                    final Rectangle rectangle, final Properties properties) {
        setInteger(name + ".x", rectangle.x, properties);
        setInteger(name + ".y", rectangle.y, properties);
        setInteger(name + ".width", rectangle.width, properties);
        setInteger(name + ".height", rectangle.height, properties);
    }

    public static Rectangle getRectangle(final String name,
                                         final Rectangle rectangle) {
        return getRectangle(name, rectangle, properties);
    }

    public static Rectangle getRectangle(final String name,
                                         Rectangle rectangle, final Properties properties) {
        if (rectangle == null)
            rectangle = new Rectangle();
        rectangle = new Rectangle(rectangle);
        rectangle.x = getInteger(name + ".x", rectangle.x, properties);
        rectangle.y = getInteger(name + ".y", rectangle.y, properties);
        rectangle.width = getInteger(name + ".width", rectangle.width,
                properties);
        rectangle.height = getInteger(name + ".height", rectangle.height,
                properties);
        return rectangle;
    }

    public static Point getCenter(final Dimension size) {
        final Dimension screenSize = Toolkit.getDefaultToolkit()
                .getScreenSize();
        final Point p = new Point();
        p.x = (screenSize.width - size.width) / 2;
        p.y = (screenSize.height - size.height) / 2;
        return p;
    }

    public static void getJTableOptions(final String name, final JTable table,
                                        final Properties properties) {
        final Integer colCount = getObjectInteger(name + "_col_count",
                properties);
        if (colCount == null || colCount.intValue() != table.getColumnCount())
            return;
        final Object cols[] = new Object[table.getColumnCount()];

        for (int i = 0; i < colCount.intValue(); i++) {
            cols[i] = table.getColumnModel().getColumn(
                    table.convertColumnIndexToView(i));
        }

        for (int i = 0; i < colCount.intValue(); i++) {
            try {
                int index = table.convertColumnIndexToView(i);
                final int width = getInteger(name + "_col_" + i + "_width",
                        table.getColumnModel().getColumn(index).getWidth(),
                        properties);
                table.getColumnModel().getColumn(index)
                        .setPreferredWidth(width);
            } catch (Exception e) {

            }
        }

        final TableColumnModel cm = table.getColumnModel();
        final int tci[] = new int[colCount.intValue()];
        for (int i = 0; i < colCount.intValue(); i++)
            cm.removeColumn((TableColumn) cols[i]);

        for (int i = 0; i < colCount.intValue(); i++) {
            tci[i] = getInteger(name + "_col_" + i + "_index", i, properties);
        }

        for (int i = 0; i < colCount.intValue(); i++)
            for (int j = 0; j < colCount.intValue(); j++)
                if (tci[j] == i)
                    cm.addColumn((TableColumn) cols[j]);

    }

    public static void setJTableOptions(final String name, final JTable table,
                                        final Properties properties) {
        setInteger(name + "_col_count", table.getColumnCount(), properties);
        for (int i = 0; i < table.getColumnCount(); i++) {
            try {
                int index = table.convertColumnIndexToView(i);
                setInteger(name + "_col_" + i + "_width", table
                                .getColumnModel().getColumn(index).getWidth(),
                        properties);
                setInteger(name + "_col_" + i + "_index", index, properties);
            } catch (final Exception e) {

            }
        }
    }

    public static Color getColor(final String name, final Color def) {
        return getColor(name, def, properties);
    }

    private static Color getColor(final String name, final Color def,
                                  final Properties properties) {
        final int a = getInteger(name + ".ALFA", def.getAlpha(), properties);
        final int r = getInteger(name + ".RED", def.getRed(), properties);
        final int g = getInteger(name + ".GREEN", def.getGreen(), properties);
        final int b = getInteger(name + ".BLUE", def.getBlue(), properties);
        return new Color(r, g, b, a);
    }

    public static void setColor(final String name, final Color def) {
        setColor(name, def, properties);
    }

    public static void setColor(final String name, final Color def,
                                final Properties properties) {
        setInteger(name + ".ALFA", def.getAlpha(), properties);
        setInteger(name + ".RED", def.getRed(), properties);
        setInteger(name + ".GREEN", def.getGreen(), properties);
        setInteger(name + ".BLUE", def.getBlue(), properties);
    }

    public static boolean getBoolean(final String name, final boolean def) {
        return getBoolean(name, def, properties);
    }

    public static boolean getBoolean(final String name, final boolean def,
                                     final Properties properties) {
        final int r = getInteger(name, def ? 1 : 0, properties);
        return r != 0;
    }

    public static void setBoolean(final String name, final boolean def) {
        setBoolean(name, def, properties);
    }

    public static void setBoolean(final String name, final boolean def,
                                  final Properties properties) {
        setInteger(name, def ? 1 : 0, properties);
    }

    public static void setFont(final String name, final Font font) {
        setFont(name, font, properties);
    }

    private static void setFont(final String name, final Font font,
                                final Properties properties) {
        setString(name + ".font.name", font.getName(), properties);
        setInteger(name + ".font.style", font.getStyle(), properties);
        setInteger(name + ".font.size", font.getSize(), properties);
    }

    public static Font getFont(final String name, final Font def) {
        return getFont(name, def, properties);
    }

    private static Font getFont(final String name, final Font def,
                                final Properties properties) {
        final String fontName = getString(name + ".font.name", def.getName(),
                properties);
        final int style = getInteger(name + ".font.style", def.getStyle(),
                properties);
        final int size = getInteger(name + ".font.size", def.getSize(),
                properties);
        return new Font(fontName, style, size);
    }

    @SuppressWarnings("unchecked")
    public static Properties getProperties(final String key) {
        Properties res = (Properties) propertiesTable.get(key);
        if (res == null) {
            res = new Properties();
            final File file = new File(getFileNameForKey(key));
            if (file.exists()) {
                try {
                    res.load(new FileInputStream(file));
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            propertiesTable.put(key, res);
        }
        return res;
    }

    public static String decode(final String password) {
        return code(password);
    }

    public static String code(final String password) {
        final char[] res = new char[password.length()];
        final char[] key = "Крутий ключ and крутий алгоритм".toCharArray();
        for (int i = 0; i < res.length; i++) {
            res[i] = (char) (password.charAt(i) ^ key[i % key.length]);
        }
        return new String(res);
    }

    @SuppressWarnings("unchecked")
    public static boolean isParent(final Class parent, final Class child) {
        Class par = child;
        do {
            if (par == parent)
                return true;
            par = par.getSuperclass();
        } while (par != null);
        return false;
    }

    public static void saveOptions(Container component, Properties properties) {
        saveOptions(component.getClass().getName(), component, properties);

    }

    public static void loadOptions(Container component, Properties properties) {
        loadOptions(component.getClass().getName(), component, properties);
    }

    public static PageFormat getPageFormat(String name, PageFormat pageFormat) {
        Properties properties = getProperties(name);
        if (properties == null)
            return pageFormat;
        final String orientation = properties.getProperty(ORIENTATION);
        if (LANDSCAPE.equals(orientation))
            pageFormat.setOrientation(PageFormat.LANDSCAPE);
        else if (PORTRAIT.equals(orientation))
            pageFormat.setOrientation(PageFormat.PORTRAIT);
        else if (REVERSE_LANDSCAPE.equals(orientation))
            pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);

        String s = properties.getProperty(PAPER_IMAGEABLE_HEIGHT);
        if (s == null)
            return pageFormat;
        double iHeight = Double.parseDouble(s);
        s = properties.getProperty(PAPER_IMAGEABLE_WIDTH);
        if (s == null)
            return pageFormat;
        double iWidth = Double.parseDouble(s);

        s = properties.getProperty(PAPER_HEIGHT);
        if (s == null)
            return pageFormat;
        double height = Double.parseDouble(s);
        s = properties.getProperty(PAPER_WIDTH);
        if (s == null)
            return pageFormat;
        double width = Double.parseDouble(s);

        s = properties.getProperty(PAPER_IMAGEABLE_X);
        if (s == null)
            return pageFormat;
        final double x = Double.parseDouble(s);
        s = properties.getProperty(PAPER_IMAGEABLE_Y);
        if (s == null)
            return pageFormat;
        final double y = Double.parseDouble(s);
        final Paper paper = pageFormat.getPaper();
        paper.setImageableArea(x, y, iWidth, iHeight);
        paper.setSize(width, height);
        pageFormat.setPaper(paper);
        return pageFormat;
    }

    public static void setPageFormat(String name, PageFormat pageFormat) {
        Properties properties = getProperties(name);
        switch (pageFormat.getOrientation()) {
            case PageFormat.LANDSCAPE:
                properties.setProperty(ORIENTATION, LANDSCAPE);
                break;
            case PageFormat.PORTRAIT:
                properties.setProperty(ORIENTATION, PORTRAIT);
                break;
            case PageFormat.REVERSE_LANDSCAPE:
                properties.setProperty(ORIENTATION, REVERSE_LANDSCAPE);
                break;
            default:
                properties.setProperty(ORIENTATION, "EMPTY");
        }
        ;
        final Paper paper = pageFormat.getPaper();
        properties.setProperty(PAPER_IMAGEABLE_HEIGHT, Double.toString(paper
                .getImageableHeight()));
        properties.setProperty(PAPER_IMAGEABLE_WIDTH, Double.toString(paper
                .getImageableWidth()));
        properties.setProperty(PAPER_IMAGEABLE_X, Double.toString(paper
                .getImageableX()));
        properties.setProperty(PAPER_IMAGEABLE_Y, Double.toString(paper
                .getImageableY()));
        properties
                .setProperty(PAPER_HEIGHT, Double.toString(paper.getHeight()));
        properties.setProperty(PAPER_WIDTH, Double.toString(paper.getWidth()));
    }
}
