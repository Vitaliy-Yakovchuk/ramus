package com.ramussoft.local;

import java.awt.Label;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.ai.AiDiagramPluginProvider;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.common.journal.Journal;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.StopUndoPointCommand;
import com.ramussoft.common.journal.command.Command;
import com.ramussoft.common.journal.command.EndUserTransactionCommand;
import com.ramussoft.common.journal.command.StartUserTransactionCommand;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.core.impl.FileMinimumVersionException;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.gui.common.AbstractGUIPluginFactory;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.SplashScreen;
import com.ramussoft.gui.common.UndoRedoPlugin;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.CloseMainFrameAdapter;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.core.GUIPluginFactory;
import com.ramussoft.gui.core.simple.SimleGUIPluginFactory;
import com.ramussoft.gui.qualifier.QualifierPluginSuit;

public class Runner implements Commands {

    private static final String CORE = "Core";

    public static void main(String[] args) {
        new Runner().load(args);
    }

    /**
     * @param args
     */
    public void load(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--hide-splash")) {
                Metadata.HIDE_SPLASH = true;
                String[] strings = new String[args.length - 1];
                int k = 0;
                for (int j = 0; j < args.length; j++) {
                    if (j != i) {
                        strings[k] = args[j];
                        k++;
                    }
                }
                args = strings;
                break;
            }
        }

        try {

            try {

                final DesktopComunication comunication = createDesktopComunication();

                if (comunication.isClient()) {
                    comunication.send(args);
                    System.exit(0);
                    return;
                } else {
                    Thread hook = new Thread() {
                        @Override
                        public void run() {
                            try {
                                comunication.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Runtime.getRuntime().addShutdownHook(hook);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Metadata.DEBUG) {

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
                                String name = Options.getPreferencesPath()
                                        + "log";
                                new File(name).mkdir();
                                name += File.separator + c.get(Calendar.YEAR)
                                        + "_" + c.get(Calendar.MONTH) + "_"
                                        + c.get(Calendar.DAY_OF_MONTH) + "_"
                                        + c.get(Calendar.HOUR_OF_DAY) + "_"
                                        + c.get(Calendar.MINUTE) + "_"
                                        + c.get(Calendar.SECOND) + "_"
                                        + c.get(Calendar.MILLISECOND) + ".log";
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

            try {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                String lookAndFeel = Options.getString("LookAndFeel");

                String lang = Options.getString("LANG");

                if (lang != null) {
                    try {
                        Locale.setDefault(new Locale(lang));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (lookAndFeel != null)
                    UIManager.setLookAndFeel(lookAndFeel);
                else {
                    if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                            .equals(UIManager.getSystemLookAndFeelClassName()))
                        UIManager.setLookAndFeel(UIManager
                                .getCrossPlatformLookAndFeelClassName());
                    else
                        UIManager.setLookAndFeel(UIManager
                                .getSystemLookAndFeelClassName());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            start(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    protected DesktopComunication createDesktopComunication()
            throws IOException {
        final DesktopComunication comunication = new DesktopComunication() {
            @Override
            public void applyArgs(String[] args) {
                recoveredCount = 0;
                run(args);
            }
        };
        return comunication;
    }

    protected int recoveredCount = 0;

    private void recoveryFiles() {
        String path = FileIEngineImpl.getSessionsPath();
        File file = new File(path);
        if (file.exists())
            for (File session : file.listFiles()) {
                try {
                    String lockName = session + File.separator + ".lock";
                    if (new File(lockName).exists()) {
                        RandomAccessFile rf = new RandomAccessFile(lockName,
                                "rw");
                        FileChannel channel = rf.getChannel();
                        FileLock lock = channel.tryLock();
                        if (lock != null) {
                            lock.release();
                            rf.close();
                            FileInputStream fis = new FileInputStream(lockName);
                            byte[] bs = new byte[fis.available()];
                            fis.read(bs);
                            fis.close();
                            String fileName;
                            if (bs.length > 0)
                                fileName = new String(bs, "UTF8");
                            else
                                fileName = null;
                            File sourceFile = (fileName == null) ? null
                                    : new File(fileName);
                            try {
                                if (!recoverySession(session.getAbsolutePath(),
                                        sourceFile))
                                    clear(session);
                            } catch (Exception e) {
                                clear(session);
                            }
                        }
                        rf.close();
                    } else {
                        clear(session);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    private void clear(File session) {
        FileIEngineImpl.deleteRec(session);
    }

    private void start(String[] args) {
        if (args.length > 0) {
            for (String arg : args)
                if (arg.equals("--close")) {
                    System.exit(0);
                    return;
                }
        }
        recoveryFiles();
        run(args);
    }

    protected void run(String[] args) {
        if (Metadata.DEMO) {
            String regVer = Options.getString("REGISTERED_VERSION");
            if (regVer == null)
                Metadata.DEMO_REGISTERED = false;
            else {
                Metadata.DEMO_REGISTERED = Boolean.valueOf(regVer);
            }
            Metadata.REGISTERED_FOR = Options.getString("REG_NAME");
        }

        if (args.length > 0) {
            for (String arg : args)
                if (arg.equals("--close")) {
                    System.exit(0);
                    return;
                }
            File file = null;
            try {
                URI uri = new URI(args[0]);
                File f = new File(uri);
                if (f.exists())
                    file = f;
            } catch (Exception e) {
            }

            if (file == null)
                file = new File(args[0]);
            open(file);
        } else {
            if (recoveredCount == 0) {
                startupLauncher(false);
            }
        }
    }

    public void startupLauncher(boolean showAnyway) {
        FirstSwitchFrame frame = new FirstSwitchFrame() {
            /**
             *
             */
            private static final long serialVersionUID = -7348079857187669414L;

            @Override
            public void setVisible(boolean b) {
                super.setVisible(b);
                if (!b) {
                    if (isOk()) {
                        Thread thread = new Thread() {
                            public void run() {
                                open(getFile());
                            }

                            ;
                        };
                        thread.start();
                    }
                }
            }
        };
        if (!showAnyway) {
            if (!frame.isDoNotShow())
                frame.setVisible(true);
        } else
            frame.setVisible(true);
    }

    private void openFile(File file) {
        saveFileToHistory(file);

        MemoryDatabase database = createDatabase(file);
        Engine engine = database.getEngine(null);

        AccessRules accessor = database.getAccessRules(null);

        openInNewWindows(engine, accessor, file, false);
    }

    public static void saveFileToHistory(File file) {
        if (file != null) {
            ArrayList<String> files = new ArrayList<String>();
            files.add(file.getAbsolutePath());
            String[] lasts = getLastOpenedFiles();
            int count = 0;
            for (String last : lasts) {
                if (count > 10)
                    break;
                if (files.indexOf(last) < 0)
                    files.add(last);
                count++;
            }

            setLastOpenedFiles(files.toArray(new String[files.size()]));
            Options.setString("LAST_FILE", file.getAbsolutePath());
            Options.setString("LAST_FILE_FIRST", file.getAbsolutePath());
        }
    }

    static String[] getLastOpenedFiles() {
        try {
            int count = Options.getInteger("LastFileCount", 0);
            String[] res = new String[count];
            for (int i = 0; i < count; i++) {
                res[i] = Options.getString("LastFile_" + i);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{};
        }
    }

    static void setLastOpenedFiles(String[] files) {
        Options.setInteger("LastFileCount", files.length);
        for (int i = 0; i < files.length; i++) {
            Options.setString("LastFile_" + i, files[i]);
        }
    }

    public MemoryDatabase createDatabase(final File file) {
        MemoryDatabase database = new MemoryDatabase(true) {
            @Override
            protected Collection<? extends PluginProvider> getAdditionalSuits() {
                ArrayList<PluginProvider> ps = new ArrayList<PluginProvider>(1);
                initAdditionalPluginSuits(ps);
                return ps;
            }

            @Override
            protected File getFile() {
                return file;
            }

        };
        return database;
    }

    public boolean open(File afile) {

        JFrame frame = null;

        if (afile != null)

            for (FilePlugin plugin : FilePlugin.plugins) {
                if ((plugin.getFile() != null)
                        && (plugin.getFile().equals(afile))) {
                    frame = plugin.getFramework().getMainFrame();
                    break;
                }
            }

        if (frame != null) {
            frame.setVisible(true);
            return false;
        } else {

            SplashScreen screen = null;
            if (FilePlugin.plugins.size() < 1 && !Metadata.HIDE_SPLASH) {
                screen = new SplashScreen() {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -8194442573188103621L;

                    @Override
                    protected String getImageName() {
                        return Runner.this.getSplashImageName();
                    }
                };
                screen.setLocationRelativeTo(null);
                screen.setVisible(true);
            }
            try {
                openFile(afile);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                if (screen != null)
                    screen.setVisible(false);
                if (e instanceof FileMinimumVersionException) {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    MessageFormat.format(
                                            GlobalResourcesManager
                                                    .getString("MinimumApplicationVersionToOpenFile"),
                                            ((FileMinimumVersionException) e)
                                                    .getMinimumVersion()));
                } else
                    JOptionPane
                            .showMessageDialog(null, e.getLocalizedMessage());
                return false;
            } finally {
                if (FilePlugin.plugins.size() < 2) {
                    if (screen != null)
                        screen.setVisible(false);
                }
            }
        }
    }

    protected String getSplashImageName() {
        return "/com/ramussoft/gui/about.png";
    }

    @SuppressWarnings("unchecked")
    public JFrame openInNewWindows(final Engine engine,
                                   final AccessRules rules, final File file, final boolean recovered) {
        List<GUIPlugin> list = new ArrayList<GUIPlugin>();
        FilePlugin filePlugin = new FilePlugin(
                (FileIEngineImpl) engine.getDeligate(), engine, rules, file,
                this);
        list.add(filePlugin);
        list.add(new UndoRedoPlugin(engine));
        initAdditionalGUIPlugins(list, engine, rules);
        final AbstractGUIPluginFactory factory = createGUIPluginFactory(engine,
                rules, list);
        final JFrame frame = factory.getMainFrame();
        engine.setPluginProperty(CORE, "MainFrame", frame);
        String title = getApplicationTitle();
        if (file != null)
            title += " - " + file.getName();
        frame.setTitle(title);
        factory.getFramework().addCloseMainFrameListener(
                new CloseMainFrameAdapter() {
                    @Override
                    public void afterClosed() {

                        List<JFrame> list = (List<JFrame>) engine
                                .getPluginProperty(CORE, "AdditionalWindows");
                        if (list != null) {
                            JFrame[] frames = list.toArray(new JFrame[list
                                    .size()]);
                            for (JFrame frame : frames) {
                                frame.setVisible(false);
                                frame.dispose();
                            }
                        }

                        FileIEngineImpl impl = (FileIEngineImpl) engine
                                .getDeligate();
                        try {
                            impl.getTemplate().getConnection().close();
                            if (engine instanceof Journaled)
                                ((Journaled) engine).close();
                            impl.close();
                            impl.clear();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        Object changed = engine.getPluginProperty(CORE, "Changed");
        if (changed != null)
            filePlugin.changed();

        InputStream is = engine.getInputStream("/user/gui/session.binary");

        if (is != null) {
            try {
                ObjectInputStream ois = new ObjectInputStream(is);
                try {
                    final List<ActionEvent> session = (List<ActionEvent>) ois
                            .readObject();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            for (ActionEvent e : session)
                                if (e != null)
                                    factory.getFramework().propertyChanged(e);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        beforeMainFrameShow(frame);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
                if (recovered) {
                    factory.getFramework().propertyChanged("FileRecovered");
                } else
                    factory.getFramework().propertyChanged("FileOpened", file);

                postShowVisibaleMainFrame(engine, factory.getFramework());

            }
        });

        return frame;
    }

    private AbstractGUIPluginFactory createGUIPluginFactory(
            final Engine engine, final AccessRules rules, List<GUIPlugin> list) {
        AbstractGUIPluginFactory factory;
        String ws = Options.getString("WindowsControl", "classic");
        if (ws.equals("simple"))
            factory = new SimleGUIPluginFactory(list, engine, rules, null, null);
        else
            factory = new GUIPluginFactory(list, engine, rules, null, null);
        return factory;
    }

    protected void beforeMainFrameShow(JFrame frame) {
    }

    public static String getApplicationTitle() {
        String title = Metadata.getApplicationName();

        return title;
    }

    @SuppressWarnings("unchecked")
    public void openInNewWindows(final Engine engine, AccessRules rules) {
        List<GUIPlugin> list = new ArrayList<GUIPlugin>();
        initAdditionalGUIPlugins(list, engine, rules);
        list.add(new UndoRedoPlugin(engine));
        final AbstractGUIPluginFactory factory = createGUIPluginFactory(engine,
                rules, list);
        final JFrame frame = factory.getMainFrame();

        List<JFrame> frames = (List<JFrame>) engine.getPluginProperty(CORE,
                "AdditionalWindows");
        if (frames == null) {
            frames = new ArrayList<JFrame>();
            engine.setPluginProperty(CORE, "AdditionalWindows", frames);
        }
        frames.add(frame);

        final JFrame mainFrame = (JFrame) engine.getPluginProperty(CORE,
                "MainFrame");
        frame.setTitle("[" + mainFrame.getTitle() + "]");
        final PropertyChangeListener titleListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                frame.setTitle("[" + (String) evt.getNewValue() + "]");
            }
        };

        factory.getFramework().addCloseMainFrameListener(
                new CloseMainFrameAdapter() {
                    @Override
                    public void afterClosed() {
                        mainFrame.removePropertyChangeListener("title",
                                titleListener);
                        List<JFrame> list = (List<JFrame>) engine
                                .getPluginProperty(CORE, "AdditionalWindows");
                        list.remove(frame);
                    }
                });

        mainFrame.addPropertyChangeListener("title", titleListener);

        beforeMainFrameShow(frame);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
                postShowVisibaleMainFrame(engine, factory.getFramework());
            }
        });
    }

    protected void postShowVisibaleMainFrame(Engine engine,
                                             GUIFramework framework) {
        JFrame frame = framework.getMainFrame();
        framework.propertyChanged("MainFrameShown");
        for (String name : engine.getStreamNames()) {
            if (name.startsWith("/script/")) {
                if (JOptionPane.showConfirmDialog(frame,
                        GlobalResourcesManager.getString("Scripts.Warning"),
                        UIManager.getString("OptionPane.titleText"),
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    engine.setPluginProperty("Scripting", "Disable",
                            Boolean.TRUE);
                }
                break;
            }
        }
    }

    public boolean recoverySession(final String sessionPath,
                                   final File sourceFile) {

        Window rFrame = new Window(null);

        String recovering = GlobalResourcesManager.getString("File.Recovering");

        rFrame.add(new Label(MessageFormat.format(
                recovering,
                ((sourceFile == null) ? GlobalResourcesManager
                        .getString("Session.NoName") : sourceFile.getName()))));
        rFrame.pack();
        rFrame.setLocationRelativeTo(null);
        rFrame.setVisible(true);

        final String s = sessionPath + File.separator + "source.rms";

        MemoryDatabase database = new MemoryDatabase() {
            @Override
            protected Collection<? extends PluginProvider> getAdditionalSuits() {
                ArrayList<PluginProvider> ps = new ArrayList<PluginProvider>(1);
                initAdditionalPluginSuits(ps);
                return ps;
            }

            @Override
            protected File getFile() {
                File file = new File(s);
                if (file.exists())
                    return file;
                return null;
            }

            protected FileIEngineImpl createFileIEngine(PluginFactory factory)
                    throws ClassNotFoundException, ZipException, IOException {
                return new FileIEngineImpl(0, template, factory, sessionPath);
            }

        };

        final Engine engine = database.getEngine(null);

        DirectoryJournalFactory factory = database.getJournalFactory();
        Journal[] journals = factory
                .loadJournals(database.getJournaledEngine());
        if (journals.length == 0)
            return false;
        boolean exist = false;
        Journal.RedoCallback redoCallback = new Journal.RedoCallback() {

            boolean hadStartUserTransaction;

            @Override
            public boolean execute(Command command) {
                if (command instanceof StartUserTransactionCommand) {
                    hadStartUserTransaction = true;
                }
                return hadStartUserTransaction;
            }
        };
        for (Journal journal : journals) {
            try {
                Command command = null;
                while (journal.canRedo()) {
                    command = journal.redo(redoCallback);
                }
                if ((journal.getPointer() == 0l)
                        || (command instanceof StopUndoPointCommand)) {
                    rFrame.setVisible(false);
                    continue;
                } else
                    exist = true;
                if (!(command instanceof EndUserTransactionCommand))
                    throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
                while (journal.canUndo()) {
                    if (journal.undo() instanceof StartUserTransactionCommand)
                        break;
                }
            }
        }
        if (!exist) {
            try {
                ((FileIEngineImpl) engine.getDeligate()).close();
                for (Journal journal : journals)
                    journal.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        ((FileIEngineImpl) engine.getDeligate()).recoveryStreams();

        engine.setPluginProperty(CORE, "Changed", Boolean.TRUE);
        engine.setActiveBranch(-1l);

        final AccessRules accessor = database.getAccessRules(null);
        rFrame.setVisible(false);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SplashScreen screen = new SplashScreen() {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -2727237354089088151L;

                    @Override
                    protected String getImageName() {
                        return Runner.this.getSplashImageName();
                    }
                };
                screen.setLocationRelativeTo(null);
                screen.setVisible(true);

                final JFrame frame = openInNewWindows(engine, accessor,
                        sourceFile, true);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String recovered = GlobalResourcesManager
                                .getString("File.Recovered");
                        frame.setTitle(frame.getTitle() + " " + recovered);
                    }
                });

                screen.setVisible(false);
            }
        };
        recoveredCount++;
        Thread thread = new Thread(runnable);
        thread.start();
        return true;
    }

    protected void initAdditionalGUIPlugins(List<GUIPlugin> list,
                                            Engine engine, AccessRules rules) {
        QualifierPluginSuit.addPlugins(list, engine, rules);
    }

    protected void initAdditionalPluginSuits(ArrayList<PluginProvider> ps) {
        // Register AI Diagram plugin provider so the AI service is available in the engine
        ps.add(new AiDiagramPluginProvider());
    }
}
