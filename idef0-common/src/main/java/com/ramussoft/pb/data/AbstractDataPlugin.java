package com.ramussoft.pb.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.IDEF0TabView;
import com.ramussoft.idef0.ModelParaleler;
import com.ramussoft.idef0.ModelsView;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.MatrixProjection;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.User;
import com.ramussoft.pb.data.negine.NCrosspoint;
import com.ramussoft.pb.data.negine.NDataPlugin;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.data.projections.MatrixProjectionIDEF0;
import com.ramussoft.pb.idef.frames.LoadFromParalelDialog;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.pb.print.xml.FastMatrixProjection;
import com.ramussoft.pb.types.GlobalId;

public abstract class AbstractDataPlugin implements DataPlugin {

    public static final String PROPERTIES = "/properties/idef0.xml";

    protected boolean dataChanged = false;

    // protected Vector<Row> nullChilds = new Vector<Row>();

    /**
     * Таблиця, в якій міститься інформація, про тимчасові файли.
     */

    protected Hashtable<String, File> files = new Hashtable<String, File>();

    /**
     * Назви всіх даних, які необхідно видалити.
     */

    protected Vector<String> removedNamedData = new Vector<String>();

    /**
     * Назви всіх двійкових даних, які необхідно оновити з тимчасових файлів.
     */

    protected Vector<String> updateNameData = new Vector<String>();

    protected Properties properties = null;

    private String tmpPath;

    protected Hashtable<Long, NCrosspoint> crosspoints = new Hashtable<Long, NCrosspoint>();

    private final FastMatrixProjection[] idef0s = new FastMatrixProjection[4];

    public AbstractDataPlugin() {
        super();
    }

    protected synchronized void setTmp() {
        tmpPath = Options.getTmpPath();
    }

    /**
     * Метод очищає всю інформацію про двікові дані, які є в програмі, в тому
     * числі видаляються всі створені тимчасові файли.
     */

    protected synchronized void clearAllNamedDataInformation() {
        final Collection<File> c = files.values();
        final Iterator<File> f = c.iterator();
        while (f.hasNext())
            f.next().delete();
        files.clear();
        removedNamedData.clear();
        updateNameData.clear();
    }

    public MatrixProjection getProjection(int type, final String string) {
        if (type == MatrixProjection.TYPE_IDEF0) {
            int functionType = -1;
            final StringTokenizer st = new StringTokenizer(string, " ");
            if (st.hasMoreElements()) {
                final String f = st.nextToken();
                if (f.equals("LEFT"))
                    type = MovingPanel.LEFT;
                else if (f.equals("RIGHT"))
                    type = MovingPanel.RIGHT;
                else if (f.equals("BOTTOM"))
                    type = MovingPanel.BOTTOM;
                else if (f.equals("TOP"))
                    type = MovingPanel.TOP;
                if (st.hasMoreTokens()) {
                    functionType = new Integer(st.nextToken()).intValue();
                }
            }
            return new MatrixProjectionIDEF0(type, functionType);
        }
        return null;
    }

    public int getLevel(final Row row) {
        final Row p = row.getParentRow();
        if (p == null)
            return 0;
        else
            return getLevel(p) + 1;
    }

    public int getElementLevel(final Row row) {
        final Row parentRow = row.getParentRow();
        if (parentRow == null)
            return 0;
        if (parentRow.isElement() != row.isElement())
            return 0;
        else
            return getElementLevel(parentRow) + 1;
    }

    public String getRowPerrmision(final Row row) {
        return null;
    }

    public String getNamedDataPerrmision(final String name) {
        return null;
    }

    /**
     * Повертає потік на дані.
     *
     * @param name Назва даних.
     * @return Вхідний потік.
     */

    abstract protected InputStream getNativeNamedData(String name);

    public InputStream getNamedData(final String name) {
        return getEngine().getInputStream(name);
    }

    public OutputStream setNamedData(final String name) {
        return getEngine().getOutputStream(name);
    }

    /**
     * Метод повертає файл, в який занесені проіменовані дані.
     *
     * @param nameData Назва даних.
     * @return Файл, з якого необхідно зчитати інформацію.
     */

    protected File getTmpFile(final String nameData) {
        return files.get(nameData);
    }

    /**
     * Повертає повний шлях, разом з символом / на кінці до папки з тимчасовими
     * файлами, Ramus.
     *
     * @return Шлях до папки.
     */

    public String getTmpPath() {
        return tmpPath;
    }

    /**
     * Метод копіює дані з вхідного потоку в вихідний.
     *
     * @param in  Вхідний потік.
     * @param out Вихідний потік
     * @throws IOException Коли виникає помилка зчитування/запису.
     */

    public static void copyStream(final InputStream in, final OutputStream out)
            throws IOException {
        final byte[] buff = new byte[1024 * 10];
        int r;
        while ((r = in.read(buff)) > 0)
            out.write(buff, 0, r);
    }

    public void removeNamedData(final String name) {
        updateNameData.remove(name);
        removedNamedData.add(name);
        final File f = files.get(name);
        if (f != null) {
            files.remove(name);
            f.delete();
        }
    }

    public void loadNamedDataFromFile(final String name, final File file) {
        synchronized (this) {
            updateNameData.remove(name);
            updateNameData.add(name);
            files.put(name, file);
        }
    }

    public boolean saveNamedDataToFile(final String name, final File file)
            throws IOException {
        synchronized (this) {
            if (updateNameData.indexOf(name) < 0)
                updateNameData.add(name);
            files.put(name, file);
            if (file.exists())
                return true;
            final InputStream is = getNamedData(name);
            if (is == null)
                return false;
            file.getParentFile().mkdirs();
            final FileOutputStream fos = new FileOutputStream(file);
            copyStream(is, fos);
            fos.close();
            return true;
        }
    }

    public void removeCrosspoint(final Crosspoint crosspoint) {
        synchronized (this) {
            crosspoints.remove(crosspoint.getGlobalId());
        }
    }

    public boolean removeRow(final Row row) {
        final boolean res = ((AbstractRow) row).remove();
        return res;
    }

    public void setParent(final Row row, final Row parent) {
        row.setParentRow(parent);
    }

    public Row refresh(final Row row) {
        return row;
    }

    public Crosspoint findCrosspointByGlobalId(final long globalId) {
        return crosspoints.get(globalId);
    }

    public void updateFile(final File file) {
    }

    public User createUser() {
        return null;
    }

    public User[] getUsers() {
        return null;
    }

    public void removeUser(final int userId) {

    }

    public void setUser(final User user) {

    }

    public void commit() {
    }

    public boolean createParalel(final Function base,
                                 final boolean copyAllRows, final boolean clearElements,
                                 final File file, final GUIFramework framework,
                                 final DataPlugin dataPlugin) throws IOException {
        Thread thread = new Thread("Model-paraleler") {
            public void run() {
                try {

                    framework.propertyChanged(ModelsView.SET_UPDATE_ALL_MODELS,
                            false);
                    framework.propertyChanged(
                            IDEF0TabView.DISABLE_SILENT_REFRESH, true);

                    framework.showAnimation(ResourceLoader
                            .getString("Wait.Message"));
                    MemoryDatabase md = new MemoryDatabase() {

                        protected String getJournalDirectoryName(String tmp) {
                            return null;
                        }

                        ;
                    };

                    final NDataPlugin fdp = new NDataPlugin(
                            md.getEngine("idef0"), md.getAccessRules("idef0"));
                    final ModelParaleler paraleler = new ModelParaleler(
                            AbstractDataPlugin.this, fdp, framework);
                    paraleler.createParalel(base, copyAllRows);
                    FileIEngineImpl impl = (FileIEngineImpl) md.getEngine(
                            "idef0").getDeligate();
                    impl.saveToFile(file);
                    impl.close();
                    if (clearElements) {
                        ((Journaled) getEngine()).startUserTransaction();
                        try {
                            paraleler.clear(base);
                        } finally {
                            ((Journaled) getEngine()).commitUserTransaction();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(framework.getMainFrame(),
                            e.getLocalizedMessage());
                } finally {
                    framework.propertyChanged(
                            IDEF0TabView.DISABLE_SILENT_REFRESH, false);
                    framework.propertyChanged(ModelsView.SET_UPDATE_ALL_MODELS,
                            true);
                    framework.propertyChanged(ModelsView.REFRESH_ALL_MODELS);
                    framework.hideAnimation();
                }
            }
        };
        thread.start();
        return true;
    }

    public void loadFromParalel(final DataPlugin dataPlugin,
                                final Function base, final File file, final GUIFramework framework)
            throws IOException {
        Thread t = new Thread("Paralel-DataLoader") {
            @Override
            public void run() {

                try {

                    framework.showAnimation(ResourceLoader
                            .getString("Wait.Message"));

                    MemoryDatabase md = new MemoryDatabase() {
                        protected String getJournalDirectoryName(String tmp) {
                            return null;
                        }

                        ;

                        @Override
                        protected File getFile() {
                            return file;
                        }
                    };

                    LoadFromParalelDialog dialog = new LoadFromParalelDialog(
                            framework.getMainFrame(), md.getEngine("idef0"));

                    if (dialog.showModal()) {
                        ((Journaled) getEngine()).startUserTransaction();
                        try {
                            framework.propertyChanged(
                                    ModelsView.SET_UPDATE_ALL_MODELS, false);
                            framework.propertyChanged(
                                    IDEF0TabView.DISABLE_SILENT_REFRESH, true);

                            DataPlugin fdp = NDataPluginFactory.getDataPlugin(
                                    null, md.getEngine("idef0"),
                                    md.getAccessRules("idef0"));
                            ModelParaleler paraleler = new ModelParaleler(fdp,
                                    dataPlugin, framework);
                            paraleler.loadFromParalel(base,
                                    dialog.isImportAll(), dialog.getSelected());
                        } finally {
                            framework.propertyChanged(
                                    IDEF0TabView.DISABLE_SILENT_REFRESH, false);
                            ((Journaled) getEngine()).commitUserTransaction();
                            framework.propertyChanged(
                                    ModelsView.SET_UPDATE_ALL_MODELS, true);
                            framework
                                    .propertyChanged(ModelsView.REFRESH_ALL_MODELS);
                        }
                    }
                    FileIEngineImpl impl = (FileIEngineImpl) md.getEngine(
                            "idef0").getDeligate();
                    impl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    framework.hideAnimation();
                }
            }
        };
        t.start();
    }

    public void loadFromParalel(final File file, final GUIFramework framework)
            throws IOException {
        Thread t = new Thread() {
            @Override
            public void run() {

                try {
                    framework.propertyChanged(ModelsView.SET_UPDATE_ALL_MODELS,
                            false);
                    framework.propertyChanged(
                            IDEF0TabView.DISABLE_SILENT_REFRESH, true);
                    framework.showAnimation(ResourceLoader
                            .getString("Wait.Message"));

                    MemoryDatabase md = new MemoryDatabase() {

                        protected String getJournalDirectoryName(String tmp) {
                            return null;
                        }

                        ;

                        @Override
                        protected File getFile() {
                            return file;
                        }
                    };

                    LoadFromParalelDialog dialog = new LoadFromParalelDialog(
                            framework.getMainFrame(), md.getEngine("idef0"));

                    if (dialog.showModal()) {
                        ((Journaled) getEngine()).startUserTransaction();
                        try {
                            DataPlugin fdp = NDataPluginFactory.getDataPlugin(
                                    null, md.getEngine("idef0"),
                                    md.getAccessRules("idef0"));
                            ModelParaleler paraleler = new ModelParaleler(fdp,
                                    AbstractDataPlugin.this, framework);
                            paraleler.loadFromParalel(dialog.isImportAll(),
                                    dialog.getSelected());
                        } finally {
                            ((Journaled) getEngine()).commitUserTransaction();
                        }
                    }
                    FileIEngineImpl impl = (FileIEngineImpl) md.getEngine(
                            "idef0").getDeligate();
                    impl.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    framework.propertyChanged(ModelsView.SET_UPDATE_ALL_MODELS,
                            true);
                    framework.propertyChanged(ModelsView.REFRESH_ALL_MODELS);
                    framework.propertyChanged(
                            IDEF0TabView.DISABLE_SILENT_REFRESH, false);

                    framework.hideAnimation();
                }
            }
        };
        t.start();
    }

    public FastMatrixProjection getFastMatrixProjectionIDEF0(final int type,
                                                             Function function) {
        // if (idef0s[type] == null) {
        Function base = function;
        while (base.getParentRow() != null) {
            base = (Function) base.getParentRow();
        }

        idef0s[type] = new FastMatrixProjection(this, type, -1, base);
        // }
        return idef0s[type];
    }

    public int indexOfFunction(final Function function) {
        if (((NFunction) function).isBase())
            return 0;
        class X {

            private int count = 0;

            private Function f;

            public int getIndex(final Function f) {
                this.f = f;
                calc(getBaseFunction(f));

                return count;
            }

            private boolean calc(final Function base) {
                final Enumeration cs = base.children();
                while (cs.hasMoreElements()) {
                    final Function c = (Function)cs.nextElement();
                    if (c.getType() >= Function.TYPE_EXTERNAL_REFERENCE)
                        continue;
                    if (c.equals(f)) {
                        count++;
                        return true;
                    }
                    if (c.isHaveRealChilds()) {
                        count++;
                        if (calc(c))
                            return true;
                    }
                }
                return false;
            }
        }

        return new X().getIndex(function);
    }

    public Function getBaseFunction(Function f) {
        while ((f != null) && (!((NFunction) f).isBase()))
            f = (Function) f.getParent();
        return f;
    }

    public void registerStatic(final GlobalId id) {
    }

    public boolean isStatic(final GlobalId id) {
        return false;
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            final InputStream is = getNamedData(PROPERTIES);
            if (is != null) {
                try {
                    properties.loadFromXML(is);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    public void setProperty(final String key, final String value) {
        getProperties().setProperty(key, value);
        final OutputStream out = setNamedData(PROPERTIES);
        try {
            properties.storeToXML(out, "");
            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(final String key) {
        return getProperties().getProperty(key);
    }

    @Override
    public Hashtable<Long, NCrosspoint> getCrosspoints() {
        return crosspoints;
    }
}
