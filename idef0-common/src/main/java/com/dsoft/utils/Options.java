/*
 * Created on 20/6/2005
 */
package com.dsoft.utils;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.bind.DatatypeConverter;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.frames.components.RowFindPanel;

/**
 * @author ZDD
 */
public class Options {
    private static final String OPTIONS_CONF = "idef0-options.conf";

    private static String PB_DIR = "ramus";

    public static void setUK() {
        UIManager.put("FileChooser.lookInLabelText", "Поточна папка:");
        UIManager.put("FileChooser.fileNameLabelText", "Ім’я фала:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файла:");
        UIManager.put("FileChooser.upFolderToolTipText",
                "Повернутись ввех на один рівень");
        UIManager.put("FileChooser.upFolderAccessibleName", "Наверх");
        UIManager.put("FileChooser.homeFolderToolTipText", "Додому");
        UIManager.put("FileChooser.homeFolderAccessibleName", "Дододу");
        UIManager.put("FileChooser.newFolderToolTipText", "Створити папку");
        UIManager.put("FileChooser.newFolderAccessibleName", "Нова папка");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Список");
        UIManager.put("FileChooser.listViewButtonAccessibleName", "Список");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Детально");
        UIManager
                .put("FileChooser.detailsViewButtonAccessibleName", "Детально");

        UIManager.put("FileChooser.newFolderErrorText",
                "Помилка створення нової папки");
        UIManager.put("FileChooser.newFolderErrorSeparator",
                "В імені папки присутні недопустимі символи");
        UIManager.put("FileChooser.fileDescriptionText", "Опис файла");
        UIManager.put("FileChooser.directoryDescriptionText", "Опис папки");
        UIManager.put("FileChooser.saveButtonTex", "Зберегти");
        UIManager.put("FileChooser.openButtonText", "Відкрити");
        UIManager.put("FileChooser.saveDialogTitleText", "Збереження файла");
        UIManager.put("FileChooser.openDialogTitleText", "Відкриття...");
        UIManager.put("FileChooser.cancelButtonText", "Відмінити");
        UIManager.put("FileChooser.updateButtonText", "Оновити");
        UIManager.put("FileChooser.helpButtonText", "Допомога");

        UIManager.put("FileChooser.acceptAllFileFilterText", "Всі типи");

        UIManager.put("FileChooser.saveButtonToolTipText", "Зберегти");
        UIManager.put("FileChooser.openButtonToolTipText", "Відкрити");
        UIManager.put("FileChooser.cancelButtonToolTipTex", "Відмінити");
        UIManager.put("FileChooser.updateButtonToolTipText", "Оновити");
        UIManager.put("FileChooser.helpButtonToolTipText", "Допомога");

        UIManager.put("OptionPane.yesButtonText", "Так");
        UIManager.put("OptionPane.noButtonText", "Ні");
        UIManager.put("OptionPane.cancelButtonText", "Відмінити");
        UIManager.put("OptionPane.titleText", "Дайте відповідь");
        UIManager.put("OptionPane.messageDialogTitle", "Повідомлення");
        UIManager.put("OptionPane.inputDialogTitle", "Ввід даних");

        UIManager.put("not_integer", "не являється цілим числом");
        UIManager.put("not_number", "не являється числом");
        UIManager.put("not_date", "не являється датою");
        UIManager.put("not_length", "перевищує максимальну довжину");
        UIManager.put("not_null", "перевищує максимальну довжину");
        UIManager.put("type.worning", "Попередження...");
    }

    private static final String FILE_SEPARATOR = System
            .getProperty("file.separator");

    private static Properties properties = new Properties();

    private static Hashtable propertiesTable = new Hashtable();

    private static final String VISUAL_OPTIONS = "visuals";

    private static String optionsPath = getOptionsExistsPathX();

    private static String tmpPath = getTmpPathX();

    private static FileLock locked = null;

    private static FileChannel channel = null;

    private static String getOptionsExistsPathX() {
        return com.ramussoft.gui.common.prefrence.Options.getPreferencesPath();
    }

    private static String getPBTmp() {
        String tmp = System.getProperty("java.io.tmpdir");
        if (!tmp.substring(tmp.length() - FILE_SEPARATOR.length()).equals(
                FILE_SEPARATOR))
            tmp += FILE_SEPARATOR;
        return tmp + getPB_DIR();
    }

    private synchronized static String getTmpPathX() {
        long l = System.currentTimeMillis();
        String res;
        do {
            res = getPBTmp() + FILE_SEPARATOR + l;
            l++;
        } while (new File(res).exists());
        res += FILE_SEPARATOR;
        return res;
    }

    public static String getOptionsExistsPath() {
        return optionsPath;
    }

    static {
        load(getOptionsExistsPath());
    }

    public static void save() {
        save(getOptionsExistsPath());
        if (channel == null)
            return;
        try {
            locked.release();
            channel.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                clearTmp();
            }
        });
    }

    public synchronized static void clearTmp() {
        final File dir = new File(tmpPath.substring(0, tmpPath.length() - 1));
        final File l = dir;
        if (l.isDirectory() && !".".equals(l.getName())
                && !"..".equals(l.getName())) {
            boolean rem = true;
            final File[] fs = l.listFiles();
            for (final File f : fs) {
                if (f.getName().equals(".locked")) {
                    if (!f.delete())
                        rem = false;
                }
            }
            if (rem)
                recRomove(l);
        }
        dir.delete();
    }

    private static void recRomove(final File l) {
        final File[] files = l.listFiles();
        for (final File file : files) {
            if (file.isDirectory()) {
                if (!".".equals(file.getName()) && !"..".equals(file.getName()))
                    recRomove(file);
            } else
                file.delete();
        }
        l.delete();
    }

    private Options() {
        super();
    }

    private static String getFileNameForKey(final String key) {
        return getOptionsExistsPath() + VISUAL_OPTIONS + "/" + key + ".conf";
    }

    public synchronized static void save(final String path) {
        save(path + OPTIONS_CONF, properties, "Main options configuration file");
        final String vDir = path + VISUAL_OPTIONS;
        final File f = new File(vDir);
        f.mkdirs();
        final Enumeration e = propertiesTable.keys();
        while (e.hasMoreElements()) {
            final String key = e.nextElement().toString();
            save(vDir + "/" + key + ".conf",
                    (Properties) propertiesTable.get(key), key);
        }
    }

    private static void save(final String fileName,
                             final Properties properties, final String comments) {
        try {
            final FileOutputStream f = new FileOutputStream(fileName);
            properties.store(f, comments);
        } catch (final Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    public static void load(final String path) {
        load(path + OPTIONS_CONF, properties);
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
        return new Double(properties.getProperty(name,
                Double.toString(defValue))).doubleValue();
    }

    public static int getInteger(final String name, final int defValue) {
        return getInteger(name, defValue, properties);
    }

    private static int getInteger(final String name, final int defValue,
                                  final Properties properties) {
        if (properties.getProperty(name) == null)
            setInteger(name, defValue, properties);
        return new Integer(properties.getProperty(name,
                Integer.toString(defValue))).intValue();
    }

    public static void setString(final String name, final String value) {
        setString(name, value, properties);
    }

    private static void setString(final String name, final String value,
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

    public static void loadOptions(final String name, final Container component) {
        final Properties properties = getProperties(name);
        loadOptions(name, component, properties);
    }

    public static void loadOptions(final String name,
                                   final Container component, final Properties properties) {

        if (ResourceLoader.isParent(JFrame.class, component.getClass())) {
            ((JFrame) component).setBounds(getRectangle(name,
                    ((JFrame) component).getBounds(), properties));
        }

        if (ResourceLoader.isParent(JDialog.class, component.getClass())) {
            ((JDialog) component).setBounds(getRectangle(name,
                    ((JDialog) component).getBounds(), properties));
        }
        if (ResourceLoader.isParent(RowFindPanel.class, component.getClass())) {
            component.setVisible(getBoolean(name + ".Visible",
                    component.isVisible(), properties));
            ((RowFindPanel) component).getJCheckBox().setSelected(
                    getBoolean(name + ".WO", ((RowFindPanel) component)
                            .getJCheckBox().isSelected(), properties));
        }

        for (int i = 0; i < component.getComponentCount(); i++) {
            if (ResourceLoader.isParent(JTable.class, component.getComponent(i)
                    .getClass()))
                getJTableOptions(name + "_" + i,
                        (JTable) component.getComponent(i), properties);
            if (ResourceLoader.isParent(JSplitPane.class, component
                    .getComponent(i).getClass())) {
                ((JSplitPane) component.getComponent(i))
                        .setDividerLocation(getInteger(name + "__" + i,
                                ((JSplitPane) component.getComponent(i))
                                        .getDividerLocation(), properties));

            }

            if (ResourceLoader.isParent(Container.class, component
                    .getComponent(i).getClass()))
                loadOptions(name + "X" + i,
                        (Container) component.getComponent(i), properties);
        }
    }

    public static void saveOptions(final String name, final Container component) {
        final Properties properties = getProperties(name);
        saveOptions(name, component, properties);
    }

    public static void saveOptions(final String name,
                                   final Container component, final Properties properties) {
        if (component == null)
            return;
        if (ResourceLoader.isParent(JFrame.class, component.getClass())) {
            setRectangle(name, ((JFrame) component).getBounds(), properties);
        }

        if (ResourceLoader.isParent(RowFindPanel.class, component.getClass())) {
            setBoolean(name + ".Visible", component.isVisible(), properties);
            setBoolean(name + ".WO", ((RowFindPanel) component).getJCheckBox()
                    .isSelected(), properties);
        }

        if (ResourceLoader.isParent(JDialog.class, component.getClass())) {
            setRectangle(name, ((JDialog) component).getBounds(), properties);
        }

        for (int i = 0; i < component.getComponentCount(); i++) {
            if (ResourceLoader.isParent(JTable.class, component.getComponent(i)
                    .getClass()))
                setJTableOptions(name + "_" + i,
                        (JTable) component.getComponent(i), properties);

            if (ResourceLoader.isParent(JSplitPane.class, component
                    .getComponent(i).getClass())) {
                setInteger(name + "__" + i,
                        ((JSplitPane) component.getComponent(i))
                                .getDividerLocation(), properties);

            }

            if (ResourceLoader.isParent(Container.class, component
                    .getComponent(i).getClass()))
                saveOptions(name + "X" + i,
                        (Container) component.getComponent(i), properties);
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
        final String cNames[] = new String[table.getColumnCount()];
        final Object cols[] = new Object[table.getColumnCount()];

        for (int i = 0; i < cNames.length; i++) {
            cNames[i] = table.getColumnName(i);
            cols[i] = table.getColumnModel().getColumn(i);
        }

        for (final String element : cNames) {
            final int width = getInteger(name + "_col_" + element + "_width",
                    table.getColumn(element).getWidth(), properties);
            table.getColumn(element).setPreferredWidth(width);
        }

        final TableColumnModel cm = table.getColumnModel();
        final int tci[] = new int[cNames.length];
        for (int i = 0; i < cNames.length; i++)
            cm.removeColumn((TableColumn) cols[i]);

        for (int i = 0; i < cNames.length; i++) {
            tci[i] = getInteger(name + "_col_" + cNames[i] + "_index", i,
                    properties);
        }

        for (int i = 0; i < cNames.length; i++)
            for (int j = 0; j < cNames.length; j++)
                if (tci[j] == i)
                    cm.addColumn((TableColumn) cols[j]);

    }

    public static void setJTableOptions(final String name, final JTable table,
                                        final Properties properties) {
        setInteger(name + "_col_count", table.getColumnCount(), properties);
        final String cNames[] = new String[table.getColumnCount()];
        for (int i = 0; i < cNames.length; i++)
            cNames[i] = table.getColumnName(i);

        for (final String element : cNames) {
            try {
                setInteger(name + "_col_" + element + "_width", table
                        .getColumn(element).getWidth(), properties);
                setInteger(name + "_col_" + element + "_index",
                        table.convertColumnIndexToView(table.getColumn(element)
                                .getModelIndex()), properties);
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

    private static boolean getBoolean(final String name, final boolean def,
                                      final Properties properties) {
        final int r = getInteger(name, def ? 1 : 0, properties);
        return r != 0;
    }

    public static void setBoolean(final String name, final boolean def) {
        setBoolean(name, def, properties);
    }

    private static void setBoolean(final String name, final boolean def,
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
            res[i] = (char) ((int) password.charAt(i) ^ (int) key[i
                    % key.length]);
        }
        return new String(res);
    }

    private static int i = 0;

    @SuppressWarnings("resource")
    public static String getTmpPath() {
        if (locked == null) {
            // clearTmp();
            new File(tmpPath).mkdirs();
            try {
                final File file = new File(tmpPath + ".locked");
                channel = new RandomAccessFile(file, "rw").getChannel();
                locked = channel.lock(0, Long.MAX_VALUE, true);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        String name;
        do {
            i++;
            name = tmpPath + i;
        } while (new File(name).exists());
        new File(name).mkdirs();
        return name + FILE_SEPARATOR;
    }

    public static void setPB_DIR(final String pB_DIR) {
        PB_DIR = pB_DIR;
        tmpPath = getTmpPathX();
    }

    public static String getPB_DIR() {
        return PB_DIR;
    }

    public static void setStroke(String name, Stroke stroke) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            DataSaver.saveStroke(os, stroke, new DataLoader.MemoryData());
            setString(name, DatatypeConverter.printHexBinary(os.toByteArray()));
        } catch (IOException e) {
        }
    }

    public static Stroke getStroke(String name, Stroke defaultStroke) {
        String val = getString(name);
        if (val == null)
            return defaultStroke;
        ByteArrayInputStream is = new ByteArrayInputStream(
                DatatypeConverter.parseHexBinary(val));
        try {
            return DataLoader.readStroke(is, new DataLoader.MemoryData());
        } catch (IOException e) {// shell never happen
            e.printStackTrace();
            return null;
        }
    }
}
