package com.ramussoft.local;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.FormulaListener;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.event.StreamListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.event.JournalListener;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.ActionLevel;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.View;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.CloseMainFrameAdapter;
import com.ramussoft.gui.common.prefrence.AbstractPreferences;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.prefrence.Preferences;
import com.ramussoft.gui.qualifier.Commands;

public class FilePlugin extends AbstractViewPlugin implements Commands {

    public static final String BEFORE_FILE_SAVE = "BeforeFileSave";

    private static final String LINK2 = "Link: ";

    private static final String VERSION2 = "Version: ";

    private static final String LAST_UPDATE_CHECK = "LAST_UPDATE_CHECK";

    private static final String CHECK_FOR_UPDATES = "CHECK_FOR_UPDATES";

    private static final String RSF = ".rsf";

    private static final String LAST_FILE = "LAST_FILE";

    private FileIEngineImpl engine;

    private Engine engine2;

    private AccessRules accessRules;

    private File file;

    private boolean changed = false;

    static ArrayList<FilePlugin> plugins = new ArrayList<FilePlugin>();

    private Object startup = new Object();

    private Runner runner;

    private AbstractAction saveAction = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 1208060203043744604L;

        {
            putValue(ACTION_COMMAND_KEY, "FileSave");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/file-save.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            saveFile();
        }

    };

    private AbstractAction saveFileAsAction = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 1208060203043744604L;

        {
            putValue(ACTION_COMMAND_KEY, "FileSaveAs");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveFileAs();
        }

    };

    private FileFilter fileFilter = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isFile()) {
                if (f.getName().toLowerCase().endsWith(getRSF()))
                    return true;
                else
                    return false;
            }
            return true;
        }

        @Override
        public String getDescription() {
            return "*" + getRSF();
        }

    };

    @SuppressWarnings("unused")
    @Override
    public void setFramework(final GUIFramework framework) {
        super.setFramework(framework);
        framework.addCloseMainFrameListener(new CloseMainFrameAdapter() {
            @Override
            public boolean close() {
                boolean close = closeFrame(framework);
                return close;
            }

            @Override
            public void closed() {
                plugins.remove(FilePlugin.this);
            }
        });

        setFile(file);
        if ((!Metadata.CORPORATE)
                && (Options.getBoolean(CHECK_FOR_UPDATES, true))
                && (isTimeToUpdate())) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(20000);
                        checkForUpdates();
                    } catch (Exception e) {
                    }
                }
            }, "Update-checker");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        if (Metadata.DEMO) {
            framework.put("FilePlugin", this);
            framework.addActionListener("DisableSaveActions",
                    new ActionListener() {

                        @Override
                        public void onAction(
                                com.ramussoft.gui.common.event.ActionEvent event) {
                            saveAction.setEnabled(false);
                            saveFileAsAction.setEnabled(false);
                        }
                    });
            framework.addActionListener("EnableSaveActions",
                    new ActionListener() {

                        @Override
                        public void onAction(
                                com.ramussoft.gui.common.event.ActionEvent event) {
                            saveAction.setEnabled(true);
                            saveFileAsAction.setEnabled(true);
                        }
                    });
        }
    }

    private void checkForUpdates() throws IOException {

        URL url;

        if (Metadata.EDUCATIONAL)
            url = new URL("http://ramussoftware.com/ramus-educational.php?v="
                    + Metadata.getApplicationVersion());
        else
            url = new URL("http://ramussoftware.com/ramus-pro.php?v="
                    + Metadata.getApplicationVersion());

        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestProperty("User-agent", "Mozilla/2.0.0.11");

        InputStreamReader bis = new InputStreamReader(urlConn.getInputStream());
        BufferedReader br = new BufferedReader(bis);
        String line;
        String version = Metadata.getApplicationVersion();
        String link = "http://ramussoft.co.cc";
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith(VERSION2)) {
                version = line.substring(VERSION2.length());
            }
            if (line.startsWith(LINK2)) {
                link = line.substring(LINK2.length());
            }
        }

        br.close();

        if (FileIEngineImpl.isOlderVersion(version, Metadata
                .getApplicationVersion())) {
            if (JOptionPane.showConfirmDialog(framework.getMainFrame(),
                    MessageFormat.format(GlobalResourcesManager
                            .getString("Message.NewerVersion"), Metadata
                            .getApplicationName()
                            + " " + version), UIManager
                            .getString("OptionPane.titleText"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                try {
                    Desktop.getDesktop().browse(new URI(link));
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(framework.getMainFrame(), e1
                            .getLocalizedMessage());
                }
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Options.setString(LAST_UPDATE_CHECK, year + "." + month + "." + day);
    }

    private boolean isTimeToUpdate() {
        Calendar calendar = Calendar.getInstance();
        String lastUpdate = Options.getString(LAST_UPDATE_CHECK);
        if (lastUpdate == null)
            return true;
        StringTokenizer st = new StringTokenizer(lastUpdate, ".");
        int year = Integer.parseInt(st.nextToken());
        int month = Integer.parseInt(st.nextToken());
        int day = Integer.parseInt(st.nextToken());
        if (year != calendar.get(Calendar.YEAR))
            return true;
        if (month != calendar.get(Calendar.MONTH))
            return true;

        return calendar.get(Calendar.DAY_OF_MONTH) - day >= 7;
    }

    public FilePlugin(FileIEngineImpl engine, Engine engine2,
                      AccessRules accessRules, File file, Runner runner) {
        this.engine = engine;
        this.engine2 = engine2;
        this.accessRules = accessRules;
        this.runner = runner;
        this.setFile(file);

        plugins.add(this);

        engine2
                .addAttributeListener((AttributeListener) createChangeListener(AttributeListener.class));

        engine2
                .addElementAttributeListener(
                        null,
                        (ElementAttributeListener) createChangeListener(ElementAttributeListener.class));

        engine2.addElementListener(null,
                (ElementListener) createChangeListener(ElementListener.class));

        engine2
                .addQualifierListener((QualifierListener) createChangeListener(QualifierListener.class));

        engine2
                .addStreamListener((StreamListener) createChangeListener(StreamListener.class));

        engine2
                .addFormulaListener((FormulaListener) createChangeListener(FormulaListener.class));

        if (engine2 instanceof Journaled) {
            ((Journaled) engine2)
                    .addJournalListener((JournalListener) createChangeListener(JournalListener.class));
        }

    }

    private Object createChangeListener(Class<?> clazz) {
        return Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{clazz}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {
                        changed();
                        return null;
                    }
                });
    }

    public void changed() {
        if ((framework == null) || (framework.getMainFrame() == null))
            return;

        if (!changed)
            framework.getMainFrame().setTitle(
                    "*" + framework.getMainFrame().getTitle());
        changed = true;
    }

    @Override
    public String getName() {
        return "File";
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor newProject = new ActionDescriptor();
        newProject.setActionLevel(ActionLevel.GLOBAL);
        newProject.setMenu("File");
        newProject.setToolBar("File");
        newProject.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 1877755929565770275L;

            {
                putValue(ACTION_COMMAND_KEY, "NewProject");
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/gui/new.png")));
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
                        KeyEvent.CTRL_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                newProject();
            }
        });

        ActionDescriptor open = new ActionDescriptor();
        open.setActionLevel(ActionLevel.GLOBAL);
        open.setMenu("File");
        open.setToolBar("File");
        open.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 1208060203043744604L;

            {
                putValue(ACTION_COMMAND_KEY, "FileOpen");
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/gui/file-open.png")));
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
                        KeyEvent.CTRL_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }

        });

        ActionDescriptor save = new ActionDescriptor();
        save.setActionLevel(ActionLevel.GLOBAL);
        save.setMenu("File");
        save.setToolBar("File");

        save.setAction(saveAction);

        ActionDescriptor saveFileAs = new ActionDescriptor();
        saveFileAs.setActionLevel(ActionLevel.GLOBAL);
        saveFileAs.setMenu("File");
        saveFileAs.setAction(saveFileAsAction);

        ActionDescriptor separator = new ActionDescriptor();
        separator.setMenu("File");

        ActionDescriptor openInNewWindow = new ActionDescriptor();
        openInNewWindow.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 6711155074141852381L;

            {
                putValue(ACTION_COMMAND_KEY, "OpenInNewWindows");
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
                        KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                runner.openInNewWindows(engine2, accessRules);
            }
        });
        openInNewWindow.setMenu("File");

        return new ActionDescriptor[]{newProject, separator, open, separator,
                save, saveFileAs, separator, openInNewWindow, separator};
    }

    protected void newProject() {
        runner.open(null);
    }

    private void open() {
        JPanel contentPane = new JPanel();
        contentPane.setDoubleBuffered(true);
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(fileFilter);
        if (FilePlugin.this.getFile() == null) {
            String file = Options.getString(LAST_FILE);
            if (file != null) {
                chooser.setSelectedFile(new File(file));
            }
        } else
            chooser.setSelectedFile(FilePlugin.this.getFile());
        int r = chooser.showOpenDialog(framework.getMainFrame());
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            loadFile(f);

        }

    }

    private void loadFile(final File f) {
        synchronized (startup) {
            framework.showAnimation(MessageFormat.format(GlobalResourcesManager
                    .getString("Message.Loading"), f.getName()));
        }

        Thread load = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    openFile(f);
                } finally {
                    synchronized (startup) {
                        framework.hideAnimation();
                    }
                }
            }

        }, "File-loading");

        load.setPriority(Thread.MIN_PRIORITY);
        load.start();

        Options.setString(LAST_FILE, f.getAbsolutePath());

    }

    private void openFile(File f) {
        if (runner.open(f)) {
            if ((this.getFile() == null) && (!changed))
                exit();
        }
    }

    private void exit() {
        framework.exit();
    }

    protected boolean saveFile() {
        if (getFile() == null)
            return saveFileAs();
        else
            try {
                saveToFile(getFile());
                Options.setString(LAST_FILE, getFile().getAbsolutePath());
                return true;
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(framework.getMainFrame(), e1
                        .getLocalizedMessage());
                return false;
            }
    }

    private boolean saveFileAs() {
        JFileChooser chooser = new JFileChooser() {
            /**
             *
             */
            private static final long serialVersionUID = -2539273621585333693L;

            @Override
            public void approveSelection() {
                if (getSelectedFile().exists()) {
                    if (JOptionPane
                            .showConfirmDialog(
                                    framework.getMainFrame(),
                                    GlobalResourcesManager
                                            .getString("File.Exists"),
                                    UIManager
                                            .getString("OptionPane.messageDialogTitle"),
                                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                }
                super.approveSelection();
            }
        };
        chooser.setFileFilter(fileFilter);
        if (FilePlugin.this.getFile() == null) {
            String file = Options.getString(LAST_FILE);
            if (file != null) {
                chooser.setSelectedFile(new File(file));
            }
        } else
            chooser.setSelectedFile(FilePlugin.this.getFile());
        int r = chooser.showSaveDialog(framework.getMainFrame());
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                if (f.getName().toLowerCase().endsWith(getRSF())) {
                } else
                    f = new File(f.getAbsolutePath() + getRSF());
                saveToFile(f);
                FilePlugin.this.setFile(f);
                Options.setString(LAST_FILE, f.getAbsolutePath());

                refreshMainFrameTitle();

                return true;

            } catch (IOException e1) {
                JOptionPane.showMessageDialog(framework.getMainFrame(), e1
                        .getLocalizedMessage());
                return false;
            }
        } else
            return false;
    }

    private void refreshMainFrameTitle() {
        String title = Runner.getApplicationTitle();
        if (getFile() != null)
            title += " - " + getFile().getName();
        framework.getMainFrame().setTitle(title);
    }

    private void saveToFile(File f) throws IOException {
        framework.propertyChanged(BEFORE_FILE_SAVE, f);
        OutputStream out = engine2.getOutputStream("/user/gui/session.binary");
        ObjectOutputStream oos = new ObjectOutputStream(out);
        try {
            List<TabView> views = framework.getTabViews();
            List<com.ramussoft.gui.common.event.ActionEvent> session = new ArrayList<com.ramussoft.gui.common.event.ActionEvent>();
            for (View view : views) {
                com.ramussoft.gui.common.event.ActionEvent event = view
                        .getOpenAction();
                session.add(event);
            }
            oos.writeObject(session);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            oos.close();
        }
        engine.saveToFile(f);
        Runner.saveFileToHistory(f);
        setChangedFalse();
    }

    private void setChangedFalse() {
        if (changed) {
            String title = framework.getMainFrame().getTitle();
            framework.getMainFrame().setTitle(title.substring(1));
        }
        changed = false;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    private boolean closeFrame(final GUIFramework framework) {
        if (changed) {
            if (Metadata.DEMO) {
                if ((saveAction.isEnabled()) && (saveFileAsAction.isEnabled())) {
                    int r = JOptionPane.showConfirmDialog(framework
                                    .getMainFrame(),
                            getString("FileChange.ConfirmSave"));
                    if (r == JOptionPane.YES_OPTION) {
                        return saveFile();
                    } else if (r == JOptionPane.CANCEL_OPTION)
                        return false;
                } else {
                    int r = JOptionPane.showConfirmDialog(framework
                                    .getMainFrame(),
                            getString("FileChange.ConfirmClose"), UIManager
                                    .getString("OptionPane.titleText"),
                            JOptionPane.YES_NO_OPTION);
                    return r == JOptionPane.YES_OPTION;
                }
            } else {
                int r = JOptionPane.showConfirmDialog(framework.getMainFrame(),
                        getString("FileChange.ConfirmSave"));
                if (r == JOptionPane.YES_OPTION) {
                    return saveFile();
                } else if (r == JOptionPane.CANCEL_OPTION)
                    return false;

            }
        }
        return true;
    }

    private static class IntHolder {
        int value;
    }

    ;

    protected int showOptionDialog(JFrame mainFrame, String message,
                                   String title, String[] options, int defaultValue) {
        final IntHolder holder = new IntHolder();
        final JDialog dialog = new JDialog(mainFrame, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        holder.value = defaultValue;
        JPanel panel = new JPanel(new BorderLayout());
        JPanel m = new JPanel(new FlowLayout());
        m.add(new JLabel(message));
        panel.add(m, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new GridLayout(1, 3, 5, 7));
        int i = 0;
        for (String s : options) {
            final int r = i;
            JButton button = new JButton(new AbstractAction(s) {
                /**
                 *
                 */
                private static final long serialVersionUID = 7269041268620864162L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    holder.value = r;
                    dialog.setVisible(false);
                }
            });
            i++;
            bottom.add(button);
        }
        JPanel p = new JPanel(new FlowLayout());
        p.add(bottom);

        panel.add(p, BorderLayout.SOUTH);
        JPanel pane = new JPanel(new FlowLayout());
        pane.add(panel);
        dialog.setTitle(title);
        dialog.setContentPane(pane);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return holder.value;
    }

    @Override
    public Preferences[] getApplicationPreferences() {
        return new Preferences[]{new AbstractPreferences() {

            private JPanel panel;

            private JCheckBox checkBox = new JCheckBox();

            private JCheckBox checkForUpdates = new JCheckBox(
                    GlobalResourcesManager.getString("Action.CheckForUpdates"));

            private JCheckBox startup = new JCheckBox(GlobalResourcesManager
                    .getString("ShowStartupLauncher"));

            private JComboBox langsBox;

            {
                double[][] size = {
                        {5, TableLayout.MINIMUM, 5,
                                TableLayout.MINIMUM, 5},
                        {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                                TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5}};
                panel = new JPanel(new TableLayout(size));
                checkBox.setText(GlobalResourcesManager
                        .getString("SaveProgramInTheMemory"));
                checkBox.setToolTipText(GlobalResourcesManager
                        .getString("SaveProgramInTheMemory.Describe"));
                checkBox.setSelected(!Options.getBoolean("EXIT_IF_NO_FILE",
                        true));

                checkForUpdates.setSelected(Options.getBoolean(
                        CHECK_FOR_UPDATES, true));

                startup.setSelected(!Options.getBoolean(
                        "DO_NOT_ASK_AGAIN_FIRST_LOAD", false));
                panel.add(checkBox, "1,1");
                panel.add(startup, "1,3");

                JLabel label = new JLabel(GlobalResourcesManager.getString("GUI.Language"));
                panel.add(label, "1,5");

                langsBox = new JComboBox();

                langsBox.addItem(new Lang("en", "en_GB"));
                langsBox.addItem(new Lang("uk", "uk_UA"));
                langsBox.addItem(new Lang("ru", "ru"));

                for (int i = 0; i < langsBox.getItemCount(); i++) {
                    if (((Lang) langsBox.getItemAt(i)).isActive()) {
                        langsBox.setSelectedIndex(i);
                    }
                }

                panel.add(langsBox, "3,5");

                if (!Metadata.CORPORATE)
                    panel.add(checkForUpdates, "1,7");
            }

            @Override
            public JComponent createComponent() {
                return panel;
            }

            @Override
            public String getTitle() {
                return GlobalResourcesManager.getString("Main.Preferences");
            }

            @Override
            public boolean save(JDialog dialog) {
                Options.setBoolean("EXIT_IF_NO_FILE", !checkBox.isSelected());
                Options.setBoolean("DO_NOT_ASK_AGAIN_FIRST_LOAD", !startup
                        .isSelected());
                Options.setBoolean(CHECK_FOR_UPDATES, checkForUpdates
                        .isSelected());
                Options.setString("LANG", ((Lang) langsBox.getSelectedItem()).key);
                Locale newLocale = new Locale(((Lang) langsBox.getSelectedItem()).key);
                Locale.setDefault(newLocale);
                return true;
            }

        }};
    }


    final ResourceBundle langs = ResourceBundle
            .getBundle("com.ramussoft.gui.spell.languages");

    class Lang {

        private String key;

        private String name;

        public Lang(String key, String name) {
            this.name = name;
            this.key = key;
        }

        @Override
        public String toString() {
            return langs.getString(name);
        }

        public boolean isActive() {
            return Locale.getDefault().getLanguage().equals(key);
        }
    }

    ;

    /**
     * @return the rSF
     */
    public static String getRSF() {
        return System.getProperty("user.ramus.application.extension", RSF);
    }
}
