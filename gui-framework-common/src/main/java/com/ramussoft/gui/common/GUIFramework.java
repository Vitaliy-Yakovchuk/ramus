package com.ramussoft.gui.common;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileFilter;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.CloseMainFrameListener;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.print.RamusPrintable;

public abstract class GUIFramework extends CommandPull {

    public static final String PRINT_JOB_INIT = "PrintJobInit";

    /**
     *
     */
    private static final long serialVersionUID = 7342708769343418649L;

    public static final String BUTTON_GROUP = "GUIButtonGroup";

    public static final String BUTTON_GROUP_ADDED = "GUIButtonGroupAdded";

    private EventListenerList listeners = new EventListenerList();

    private Hashtable<String, EventListenerList> keyListeners = new Hashtable<String, EventListenerList>();

    private Hashtable<AttributeType, AttributePlugin> attributePlugins = new Hashtable<AttributeType, AttributePlugin>();

    protected List<GUIPlugin> plugins;

    private JFrame mainFrame;

    protected EventListenerList closeListeners = new EventListenerList();

    private Engine engine;

    private AccessRules accessRules;

    private static final Vector<GUIFramework> frameworks = new Vector<GUIFramework>();

    private ActionEvent openDinamikViewEvent = null;

    private StartupPanel startupPanel = null;

    private List<QualifierSetupPlugin> qualifierSetupPlugins = new ArrayList<QualifierSetupPlugin>();

    private Hashtable<Attribute, String> systemAttributeNames = new Hashtable<Attribute, String>();

    private Hashtable<Attribute, AttributePlugin> systemAttributePlugins = new Hashtable<Attribute, AttributePlugin>();

    private Hashtable<String, PrinterJob> jobs = new Hashtable<String, PrinterJob>();

    public void addCloseMainFrameListener(CloseMainFrameListener listener) {
        closeListeners.add(CloseMainFrameListener.class, listener);
    }

    public List<GUIPlugin> getPlugins() {
        return plugins;
    }

    public GUIFramework(List<GUIPlugin> plugins, Engine engine,
                        AccessRules accessRules) {
        this.plugins = plugins;
        this.engine = engine;
        this.accessRules = accessRules;
        for (GUIPlugin plugin : plugins) {
            if (plugin instanceof AttributePlugin) {
                AttributePlugin p = (AttributePlugin) plugin;
                attributePlugins.put(p.getAttributeType(), p);
            }
            if (plugin instanceof QualifierSetupPlugin) {
                qualifierSetupPlugins.add((QualifierSetupPlugin) plugin);
            }
        }
        if (engine instanceof Journaled) {
            new History(this, engine);
        }
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(ActionListener.class, listener);
    }

    public ActionListener[] getActionListeners() {
        return listeners.getListeners(ActionListener.class);
    }

    public String getSystemAttributeName(Attribute systemSttribute) {
        return systemAttributeNames.get(systemSttribute);
    }

    public void setSystemAttributeName(Attribute systemAttribute, String name) {
        systemAttributeNames.put(systemAttribute, name);
    }

    public void propertyChanged(String key, final Object value, Object metadata) {
        ActionEvent event = new ActionEvent(key, value, metadata);
        put(key, event);
        for (ActionListener listener : getActionListeners()) {
            listener.onAction(event);
        }
        EventListenerList list = keyListeners.get(key);
        if (list != null) {
            for (ActionListener listener : list
                    .getListeners(ActionListener.class)) {
                listener.onAction(event);
            }
        }
        View activeView = getActiveView();
        if (activeView != null)
            for (String a : activeView.getGlobalActions()) {
                if (a.equals(key)) {
                    activeView.onAction(event);
                }
            }
    }

    public void propertyChanged(String key, Object value) {
        propertyChanged(key, value, null);
    }

    public void propertyChanged(String key) {
        propertyChanged(key, null);
    }

    public AttributePlugin findAttributePlugin(Attribute attribute) {
        AttributePlugin plugin = getSystemAttributePlugin(attribute);
        if (plugin != null)
            return plugin;
        return findAttributePlugin(attribute.getAttributeType());
    }

    public AttributePlugin findAttributePlugin(AttributeType type) {
        AttributePlugin plugin = attributePlugins.get(type);
        if (plugin == null)
            return new EmptyPlugin(type);
        return plugin;
    }

    public GUIPlugin findPlugin(String pluginName) {
        for (GUIPlugin plugin : plugins)
            if (plugin.getName().equals(pluginName))
                return plugin;
        return null;
    }

    public void addActionListener(String key, ActionListener actionListener) {
        EventListenerList list = keyListeners.get(key);
        if (list == null) {
            list = new EventListenerList();
            keyListeners.put(key, list);
        }
        list.add(ActionListener.class, actionListener);
    }

    public PrinterJob getPrinterJob(String name) {
        PrinterJob job = jobs.get(name);
        if (job == null) {
            job = PrinterJob.getPrinterJob();
            jobs.put(name, job);
            propertyChanged(PRINT_JOB_INIT, job, name);
        }
        return job;
    }

    /**
     * @param mainFrame the mainFrame to set
     */
    public void setMainFrame(JFrame mainFrame) {
        if (this.mainFrame != null) {
            throw new RuntimeException("Main frame already seted.");
        }
        this.mainFrame = mainFrame;
        mainFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                boolean close = true;

                for (CloseMainFrameListener listener : closeListeners
                        .getListeners(CloseMainFrameListener.class)) {
                    if (!listener.close())
                        close = false;
                }

                if (!close)
                    return;

                frameworks.remove(GUIFramework.this);

                for (CloseMainFrameListener listener : closeListeners
                        .getListeners(CloseMainFrameListener.class)) {
                    try {
                        listener.closed();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                GUIFramework.this.mainFrame.setVisible(false);
                GUIFramework.this.mainFrame.dispose();

                for (CloseMainFrameListener listener : closeListeners
                        .getListeners(CloseMainFrameListener.class)) {
                    listener.afterClosed();
                }

                if (frameworks.size() == 0) {
                    if ((Options.getBoolean("EXIT_IF_NO_FILE", true))
                            || (System.getProperty("AnywayExit") != null))
                        System.exit(0);
                }

            }
        });
        frameworks.add(this);
    }

    /**
     * @return the mainFrame
     */
    public JFrame getMainFrame() {
        return mainFrame;
    }

    public void removeActionListener(String key, ActionListener listener) {
        keyListeners.get(key).remove(ActionListener.class, listener);
    }

    public Engine getEngine() {
        return engine;
    }

    public static GUIFramework[] getFrameworks() {
        return frameworks.toArray(new GUIFramework[frameworks.size()]);
    }

    public void propertyChanged(ActionEvent actionEvent) {
        propertyChanged(actionEvent.getKey(), actionEvent.getValue(),
                actionEvent.getMetadata());
    }

    /**
     * @param openDinamikViewEvent the openDinamikViewEvent to set
     */
    public void setOpenDynamikViewEvent(ActionEvent openDinamikViewEvent) {
        this.openDinamikViewEvent = openDinamikViewEvent;
    }

    /**
     * @return the openDinamikViewEvent
     */
    public ActionEvent getOpenDynamicViewEvent() {
        return openDinamikViewEvent;
    }

    private StartupPanel getStartupPanel() {
        if (startupPanel == null) {
            startupPanel = new StartupPanel();
            getMainFrame().getRootPane().setGlassPane(startupPanel);
        }
        return startupPanel;
    }

    public void showAnimation(String text) {
        if (text.length() > 60)
            text = text.substring(0, 59) + "...";
        getStartupPanel().showAnimation(text);
        getMainFrame().setEnabled(false);
    }

    public void hideAnimation() {
        getStartupPanel().hideAnimation();
        getMainFrame().setEnabled(true);
    }

    public static void loadAdditionalPlugins(String pluginNames,
                                             List<GUIPlugin> plugins) {
        StringTokenizer st = new StringTokenizer(pluginNames, ", ");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            try {
                Class<?> clazz = Class.forName(token);
                GUIPlugin plugin = (GUIPlugin) clazz.newInstance();
                plugins.add(plugin);
            } catch (Exception e) {
                System.out.println("Plugin " + token + " not found");
                // throw new RuntimeException(e);
            }
        }

    }

    public AccessRules getAccessRules() {
        return accessRules;
    }

    public QualifierSetupPlugin[] getQualifierSetupPlugins() {
        return qualifierSetupPlugins
                .toArray(new QualifierSetupPlugin[qualifierSetupPlugins.size()]);
    }

    public void setSystemAttributePlugin(Attribute attribute,
                                         AttributePlugin plugin) {
        systemAttributePlugins.put(attribute, plugin);
    }

    public AttributePlugin getSystemAttributePlugin(Attribute attribute) {
        return systemAttributePlugins.get(attribute);
    }

    public File showSaveDialog(String name, final String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "*" + extension;
            }

            @Override
            public boolean accept(File f) {
                return (f.isDirectory()) || (f.getName().endsWith(extension));
            }
        });
        String fn = Options.getString(name);
        if (fn != null)
            chooser.setSelectedFile(new File(fn));
        if (chooser.showSaveDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(extension)) {
                file = new File(file.getParent(), file.getName() + extension);
            }
            Options.setString(name, file.getAbsolutePath());
            return file;
        }
        return null;
    }

    public File showOpenDialog(String name, final String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "*" + extension;
            }

            @Override
            public boolean accept(File f) {
                return (f.isDirectory()) || (f.getName().endsWith(extension));
            }
        });
        String fn = Options.getString(name);
        if (fn != null)
            chooser.setSelectedFile(new File(fn));
        if (chooser.showOpenDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Options.setString(name, file.getAbsolutePath());
            return file;
        }
        return null;
    }

    public void exit() {
        for (WindowListener l : getMainFrame().getWindowListeners())
            l.windowClosing(new WindowEvent(getMainFrame(), 0));
    }

    public abstract List<TabView> getTabViews();

    public abstract List<View> getAllViews();

    public abstract View getLastActiveView();

    public abstract String getCurrentWorkspace();

    public abstract void setCurrentWorkspace(String workspace);

    public abstract boolean openView(ActionEvent actionEvent);

    public abstract View getActiveView();

    public abstract void printPreview(RamusPrintable printable);

    public abstract View getLastDinamicView();

    public abstract void updateViewActions();

}
