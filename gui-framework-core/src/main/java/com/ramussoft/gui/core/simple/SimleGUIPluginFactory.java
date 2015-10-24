package com.ramussoft.gui.core.simple;

import java.awt.BorderLayout;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractGUIPluginFactory;
import com.ramussoft.gui.common.AdditionalGUIPluginLoader;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.StringGetter;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.TabbedView;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.View;
import com.ramussoft.gui.common.ViewPlugin;
import com.ramussoft.gui.common.event.ActionChangeAdapter;
import com.ramussoft.gui.common.event.ActionChangeEvent;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.CloseMainFrameAdapter;
import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.common.event.TabbedListener;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.common.event.ViewTitleListener;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.print.PrintPreviewComponent;
import com.ramussoft.gui.common.print.RamusPrintable;
import com.ramussoft.gui.core.AboutPlugin;
import com.ramussoft.gui.core.LookAndFeelPlugin;
import com.ramussoft.gui.core.PlugableFrame;
import com.ramussoft.gui.core.PreferenciesPlugin;
import com.ramussoft.gui.core.ShowViewPlugin;
import com.ramussoft.gui.core.ShowWorkspacePlugin;

public class SimleGUIPluginFactory extends AbstractGUIPluginFactory {

    private GUIFramework framework;

    private PlugableFrame plugableFrame;

    private List<UniqueView> uniqueViews = new ArrayList<UniqueView>();

    private List<TabbedView> tabbedViews = new ArrayList<TabbedView>();

    private List<UniqueDFrame> uniqueDockables = new ArrayList<UniqueDFrame>();

    private List<Area> uniqueWorkingAreas = new ArrayList<Area>();

    private List<String> workspaces = new ArrayList<String>();

    private Hashtable<String, GUIPlugin> pluginsForViewIds = new Hashtable<String, GUIPlugin>();

    private Hashtable<TabView, TabFrame> tabDockables = new Hashtable<TabView, TabFrame>();

    private Hashtable<Thread, Boolean> recHash = new Hashtable<Thread, Boolean>();

    private Hashtable<String, GUIPlugin> workspacePlugins = new Hashtable<String, GUIPlugin>();

    private String currentWorkspace;

    private Control control;

    private ContentArea contentArea;

    private View lastActiveView;

    static List<PlugableFrame> frames = new ArrayList<PlugableFrame>();

    private Hashtable<String, View> workspaceViews = new Hashtable<String, View>();

    public SimleGUIPluginFactory(List<GUIPlugin> plugins, Engine engine,
                                 AccessRules rules, String clientType, String[] groups) {
        this(plugins, engine, rules, clientType, groups, true);
    }

    public SimleGUIPluginFactory(List<GUIPlugin> plugins, Engine engine,
                                 AccessRules rules, String clientType, String[] groups,
                                 boolean loadPlugins) {
        super(plugins);
        if (loadPlugins)
            AdditionalGUIPluginLoader.loadAdditionalGUIPlugins(plugins,
                    clientType, groups);
        framework = new GUIFramework(plugins, engine, rules) {
            /**
             *
             */
            private static final long serialVersionUID = 7346790823231506249L;

            @Override
            public boolean openView(ActionEvent actionEvent) {
                return SimleGUIPluginFactory.this.openView(actionEvent);
            }

            @Override
            public View getActiveView() {
                if (control == null)
                    return null;
                DFrame frame = control.getActiveFrame();
                if (frame == null)
                    return null;
                return frame.getView();
            }

            @Override
            public List<View> getAllViews() {
                List<View> result = new ArrayList<View>();
                Enumeration<TabView> keys = tabDockables.keys();
                while (keys.hasMoreElements())
                    result.add(keys.nextElement());

                List<DFrame> frames = control.getFrames();
                for (DFrame frame : frames)
                    if (frame instanceof UniqueDFrame)
                        result.add(findUniqueView(((UniqueDFrame) frame)
                                .getId()));
                return result;
            }

            @Override
            public List<TabView> getTabViews() {
                List<TabView> result = new ArrayList<TabView>();
                Enumeration<TabView> keys = tabDockables.keys();
                while (keys.hasMoreElements())
                    result.add(keys.nextElement());
                return result;
            }

            @Override
            public View getLastActiveView() {
                View view = getActiveView();
                if (view == null) {
                    view = lastActiveView;
                }
                return view;
            }

            @Override
            public String getCurrentWorkspace() {
                return SimleGUIPluginFactory.this.getCurrentWorkspace();
            }

            @Override
            public void setCurrentWorkspace(String workspace) {
                SimleGUIPluginFactory.this.setCurrentWorkspace(workspace);
            }

            @Override
            public void printPreview(RamusPrintable printable) {
                final Container container = getMainFrame().getContentPane();

                final List<WindowListener> listeners = new ArrayList<WindowListener>();

                for (WindowListener listener : getMainFrame()
                        .getWindowListeners()) {
                    listeners.add(listener);
                }

                for (WindowListener listener : listeners) {
                    getMainFrame().removeWindowListener(listener);
                }

                final JMenuBar menuBar = getMainFrame().getJMenuBar();

                getMainFrame().addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        restorePane(container, menuBar, listeners);
                    }
                });

                final JPanel panel = new JPanel(new BorderLayout());
                final PrintPreviewComponent ppc = new PrintPreviewComponent(
                        printable, 1, this) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = -760051491417475526L;

                    @Override
                    public Dimension getAreaSize() {
                        return panel.getSize();
                    }
                };

                JMenuBar jMenuBar = new JMenuBar();
                JMenu file = new JMenu(
                        GlobalResourcesManager.getString("Menu.File"));
                for (Action action : ppc.getFileActions()) {
                    if (action == null)
                        file.addSeparator();
                    else
                        file.add(action);
                }

                AbstractAction closeAction = new AbstractAction(
                        GlobalResourcesManager.getString("close")) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = 7221174463620935860L;

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        restorePane(container, menuBar, listeners);
                    }
                };

                closeAction.putValue(Action.ACCELERATOR_KEY,
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

                file.addSeparator();

                file.add(closeAction);

                jMenuBar.add(file);

                JMenu view = new JMenu(
                        GlobalResourcesManager.getString("Menu.View"));

                for (Action action : ppc.getViewActions()) {
                    if (action == null)
                        view.addSeparator();
                    else {
                        if (action.getValue(Action.SELECTED_KEY) == null)
                            view.add(action);
                        else {
                            JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                                    action);
                            view.add(item);
                        }
                    }
                }

                jMenuBar.add(view);

                getMainFrame().setJMenuBar(jMenuBar);

                JToolBar toolBar = ppc.createToolBar();

                final JButton button = new JButton(closeAction);
                toolBar.add(new JPanel(new FlowLayout()));
                toolBar.add(button);

                JPanel jPanel = new JPanel(
                        new FlowLayout(FlowLayout.LEFT, 0, 0));

                jPanel.add(toolBar);

                toolBar.setFloatable(false);

                panel.add(jPanel, BorderLayout.NORTH);
                JScrollPane pane = new JScrollPane();
                pane.setViewportView(ppc);
                pane.setWheelScrollingEnabled(false);
                panel.add(pane, BorderLayout.CENTER);
                panel.add(ppc.createStatusBar(), BorderLayout.SOUTH);
                ppc.setFocusable(true);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        button.requestFocus();
                    }
                });

                getMainFrame().setContentPane(panel);
                ((JComponent) getMainFrame().getContentPane()).revalidate();
                ((JComponent) getMainFrame().getContentPane()).repaint();
                getMainFrame().repaint();
            }

            private void restorePane(final Container container,
                                     final JMenuBar menuBar, List<WindowListener> listeners) {
                getMainFrame().removeWindowListener(
                        getMainFrame().getWindowListeners()[0]);
                for (WindowListener listener : listeners)
                    getMainFrame().addWindowListener(listener);
                getMainFrame().setContentPane(container);
                getMainFrame().setJMenuBar(menuBar);
                ((JComponent) getMainFrame().getContentPane()).revalidate();
                ((JComponent) getMainFrame().getContentPane()).repaint();
                getMainFrame().repaint();
            }

            @Override
            public View getLastDinamicView() {
                if (currentWorkspace == null)
                    return null;
                return workspaceViews.get(currentWorkspace);
            }

            @Override
            public void updateViewActions() {
                View view = getActiveView();
                if (view == null) {
                    plugableFrame.setViewActions(new String[]{});
                } else {
                    plugableFrame.setViewActions(view.getGlobalActions());
                }

            }
        };

        UIManager.put(
                "TextArea.font",
                new Font(Options.getString("TEXT_AREA_DEF_FONT",
                        "Tymes New Roman"), 0, Options.getInteger(
                        "TEXT_AREA_DEF_FONT_SIZE", 14)));

        for (GUIPlugin p : plugins) {
            p.setFramework(framework);
            if (p instanceof ViewPlugin) {
                ViewPlugin plugin = (ViewPlugin) p;
                plugin.addTabbedListener(new TabbedListener() {
                    @Override
                    public void tabCreated(TabbedEvent event) {
                        SimleGUIPluginFactory.this.tabCreated(event);
                    }

                    @Override
                    public void tabRemoved(TabbedEvent event) {
                        SimleGUIPluginFactory.this.tabRemoved(event);
                    }
                });

                for (UniqueView view : plugin.getUniqueViews()) {
                    uniqueViews.add(view);
                    pluginsForViewIds.put(view.getId(), plugin);
                    String s = view.getDefaultWorkspace();
                    if (s != null) {
                        StringTokenizer st = new StringTokenizer(s, "|");
                        s = st.nextToken();
                    }
                    if ((s != null) && (workspaces.indexOf(s) < 0)) {
                        workspaces.add(s);
                        workspacePlugins.put(s, plugin);
                    }
                }

                for (TabbedView view : plugin.getTabbedViews()) {
                    tabbedViews.add(view);
                    pluginsForViewIds.put(view.getId(), plugin);
                    String s = view.getDefaultWorkspace();
                    if ((s != null) && (workspaces.indexOf(s) < 0)) {
                        workspaces.add(s);
                        workspacePlugins.put(s, plugin);
                    }
                }
            }
        }
        List<ViewPlugin> list = new ArrayList<ViewPlugin>();

        AboutPlugin aboutPlugin = new AboutPlugin(engine);
        list.add(aboutPlugin);

        for (GUIPlugin plugin : plugins) {
            if (plugin instanceof ViewPlugin) {
                list.add((ViewPlugin) plugin);
            }
        }
        ShowViewPlugin showViewPlugin = new ShowViewPlugin(uniqueViews, this);
        showViewPlugin.setFramework(framework);
        list.add(showViewPlugin);
        ShowWorkspacePlugin showWorkspacePlugin = new ShowWorkspacePlugin(this);
        showWorkspacePlugin.setFramework(framework);
        list.add(showWorkspacePlugin);
        PreferenciesPlugin preferenciesPlugin = new PreferenciesPlugin(list,
                engine);
        preferenciesPlugin.setFramework(framework);
        list.add(preferenciesPlugin);

        LookAndFeelPlugin lookAndFeelPlugin = new LookAndFeelPlugin();
        lookAndFeelPlugin.setFramework(framework);
        list.add(lookAndFeelPlugin);

        engine.setPluginProperty("GUI", "PluginList", plugins);

        aboutPlugin.setFramework(framework);

        plugableFrame = new PlugableFrame(list);

        frames.add(plugableFrame);

        if (workspaces.size() > 0)
            currentWorkspace = workspaces.get(0);
        currentWorkspace = Options.getString("CurrentWorkspace",
                currentWorkspace);
        initContent();

        framework.propertyChanged("MainFrameCreated");

        showWorkspacePlugin.createWorkspaceToolBar();

        getContentArea()
                .setNorthWestComponent(plugableFrame.getToolBarsPanel());

        plugableFrame.setSize(1024, 800);
        plugableFrame.setLocationRelativeTo(null);
        Options.loadOptions(plugableFrame);
        loadCurrentWorkspase();
        plugableFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

            }
        });

        framework.addCloseMainFrameListener(new CloseMainFrameAdapter() {
            @Override
            public void closed() {
                Enumeration<TabView> enumeration = tabDockables.keys();
                while (enumeration.hasMoreElements()) {
                    TabView view = enumeration.nextElement();
                    try {
                        view.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Options.saveOptions(plugableFrame);
                Options.setString("CurrentWorkspace", currentWorkspace);
                saveCurrentWorkspace();
                Options.save();

                for (UniqueView view : uniqueViews) {
                    view.close();
                }
            }
        });
    }

    protected boolean openView(ActionEvent actionEvent) {
        for (UniqueView view : uniqueViews) {
            if (actionEvent.equals(view.getOpenAction())) {
                UniqueDFrame dockable = findUniqueDockable(view.getId());
                control.setActive(dockable);
                return true;
            }
        }

        Enumeration<TabView> enumeration = tabDockables.keys();
        while (enumeration.hasMoreElements()) {
            TabView view = enumeration.nextElement();
            if (actionEvent.equals(view.getOpenAction())) {
                TabFrame dockable = tabDockables.get(view);
                control.setActive(dockable);
                return true;
            }
        }
        return false;
    }

    // @SuppressWarnings("deprecation")
    private void initContent() {

        control = new Control(plugableFrame, this);

        // control.setTheme(control.getThemes().getFactory(2).create());

        control.addControlListener(new ControlListener() {

            @Override
            public void closed(Control control, DFrame dockable) {
                Boolean b = recHash.get(Thread.currentThread());
                if (b != null)
                    return;
                recHash.put(Thread.currentThread(), true);
                List<TabView> list = new ArrayList<TabView>();
                for (Entry<TabView, TabFrame> e : tabDockables.entrySet()) {
                    if (e.getValue().equals(dockable)) {
                        TabView view = e.getKey();
                        list.add(view);
                    }

                }

                for (TabView view : list)
                    view.close();

                recHash.remove(Thread.currentThread());
            }

        });

        control.addFocusListener(new FrameFocusListener() {

            @Override
            public void focusGained(DFrame dockable) {
                List<View> list = new ArrayList<View>();
                for (Entry<TabView, TabFrame> e : tabDockables.entrySet()) {
                    if (e.getValue().equals(dockable)) {
                        TabView view = e.getKey();
                        list.add(view);
                    }

                }

                if (dockable instanceof UniqueDFrame) {
                    list.add(findUniqueView(((UniqueDFrame) dockable)
                            .getUniqueId()));
                }

                for (View view : list) {
                    lastActiveView = view;
                    view.focusGained();
                    plugableFrame.setViewActions(view.getGlobalActions());
                    if ((!(view instanceof UniqueView))
                            && (view instanceof TabView)) {
                        if (currentWorkspace != null)
                            workspaceViews.put(currentWorkspace, view);
                    }
                }
            }

            @Override
            public void focusLost(DFrame dockable) {
                List<View> list = new ArrayList<View>();
                for (Entry<TabView, TabFrame> e : tabDockables.entrySet()) {
                    if (e.getValue().equals(dockable)) {
                        TabView view = e.getKey();
                        list.add(view);
                    }

                }

                if (dockable instanceof UniqueDFrame) {
                    list.add(findUniqueView(((UniqueDFrame) dockable)
                            .getUniqueId()));
                }

                for (View view : list)
                    view.focusLost();
                plugableFrame.setViewActions(new String[]{});
            }

        });

        // control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
        contentArea = control.getContentArea();
        plugableFrame.add(contentArea, BorderLayout.CENTER);

        for (UniqueView view : uniqueViews) {
            String id = view.getId();
            final UniqueDFrame dockable = new UniqueDFrame(control, id,
                    findPluginForViewId(id).getString(id), view);
            dockable.setCloseable(true);
            initActions(view.getId(), view, dockable);
            control.add(dockable);
            uniqueDockables.add(dockable);
            framework.addActionListener(id, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    dockable.setVisible(true);
                }
            });
            if (currentWorkspace.equals(view.getDefaultWorkspace()))
                dockable.setVisible(true);
        }

        control.commitUniqueViews();

        for (TabbedView view : tabbedViews) {

            String id = view.getId();
            if (getWorkingArea(id) != null)
                continue;
            Area area = control.createWorkingArea(id);
            uniqueWorkingAreas.add(area);
            area.setVisible(true);

        }
        framework.setMainFrame(plugableFrame);
    }

    private void initActions(final String id, final View view,
                             final DFrame dockable) {
        Action[] actions = view.getActions();
        addActions(id, view, dockable, actions);
        dockable.addSeparator();
        view.addViewTitleListener(new ViewTitleListener() {
            @Override
            public void titleChanged(ViewTitleEvent event) {
                dockable.setTitleText(event.getNewTitle());
            }
        });

        view.addActionChangeListener(new ActionChangeAdapter() {
            @Override
            public void actionsAdded(ActionChangeEvent event) {
                Action[] actions = event.getActions();
                List<Action> list = new ArrayList<Action>();
                for (Action a : actions) {
                    if (a != null)
                        list.add(a);
                }

                addActions(id, view, dockable,
                        list.toArray(new Action[list.size()]));
            }

            @Override
            public void actionsRemoved(ActionChangeEvent event) {
                for (Action a : event.getActions())
                    if (a != null) {
                        dockable.removeAction(a);
                    }
            }
        });
    }

    private void addActions(String id, final View view, final DFrame dockable,
                            Action[] actions) {
        for (final Action action : actions)
            if (action == null) {
                dockable.addSeparator();
            } else {

                String text = null;
                String command = (String) action
                        .getValue(Action.ACTION_COMMAND_KEY);

                if (view instanceof TabView) {
                    text = ((TabView) view).getString(command);
                }
                if (text == null)
                    text = findPluginForViewId(id).getString(command);

                if (text == null) {
                    StringGetter getter = (StringGetter) action
                            .getValue(StringGetter.ACTION_STRING_GETTER);
                    if (getter != null)
                        text = getter.getString(command);
                }

                ImageIcon icon = (ImageIcon) action.getValue(Action.SMALL_ICON);
                if (icon == null) {
                    icon = new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/icon.png"));
                }
                String tooltip = (String) action
                        .getValue(Action.LONG_DESCRIPTION);

                if (tooltip == null)
                    tooltip = (String) action
                            .getValue(Action.SHORT_DESCRIPTION);

                if (tooltip == null)
                    tooltip = text;
                dockable.addAction(action, tooltip);
            }
    }

    public TabbedView findTabbedView(String id) {
        for (TabbedView view : tabbedViews) {
            if (view.getId().equals(id))
                return view;
        }
        return null;
    }

    public UniqueView findUniqueView(String id) {
        for (UniqueView view : uniqueViews) {
            if (view.getId().equals(id))
                return view;
        }
        return null;
    }

    public UniqueDFrame findUniqueDockable(String id) {
        for (UniqueDFrame dockable : uniqueDockables) {
            if (dockable.getUniqueId().equals(id))
                return dockable;
        }
        return null;
    }

    public GUIPlugin findPluginForViewId(String id) {
        return pluginsForViewIds.get(id);
    }

    protected void tabCreated(TabbedEvent event) {
        TabView view = event.getTabableView();

        TabFrame dockable = tabDockables.get(event.getTabableView());
        if (dockable != null) {
            control.setActive(dockable);
            return;
        }

        String id = event.getTabViewId();
        Area area = getWorkingArea(id);
        if (!area.isVisible())
            area.setVisible(true);
        dockable = new TabFrame(control, area, view);
        tabDockables.put(view, dockable);
        dockable.setRemoveOnClose(true);

        initActions(id, view, dockable);
        // dockable.setTitleText(view.getTitle());
        dockable.setCloseable(true);

        dockable.setVisible(true);
    }

    protected void tabRemoved(TabbedEvent event) {
        Boolean b = recHash.get(Thread.currentThread());
        TabFrame dockable = tabDockables.get(event.getTabableView());
        if (b == null) {
            recHash.put(Thread.currentThread(), true);

            if (dockable != null) {
                control.closeTab(dockable);
            }
            recHash.remove(Thread.currentThread());
        }
        tabDockables.remove(event.getTabableView());
    }

    private Area getWorkingArea(String id) {
        for (Area area : uniqueWorkingAreas) {
            if (area.getUniqueId().equals(id))
                return area;
        }
        return null;
    }

    @Override
    public JFrame getMainFrame() {
        return plugableFrame;
    }

    public List<String> getWorkspaces() {
        return workspaces;
    }

    public void setCurrentWorkspace(String currentWorkspace) {
        saveCurrentWorkspace();
        this.currentWorkspace = currentWorkspace;
        String[] ss = control.layouts();
        for (String s : ss) {
            if (s.equals(currentWorkspace)) {
                loadCurrentWorkspase();
                break;
            }
        }
    }

    private void loadCurrentWorkspase() {
        control.load(this.currentWorkspace);
        if (this.currentWorkspace != null) {
            View view = workspaceViews.get(this.currentWorkspace);
            if (view != null) {
                Enumeration<TabView> enumeration = tabDockables.keys();
                while (enumeration.hasMoreElements()) {
                    TabView tabView = enumeration.nextElement();
                    if (view == tabView) {
                        TabFrame dockable = tabDockables.get(view);
                        control.setActive(dockable);
                    }
                }
            }
        }
    }

    private void saveCurrentWorkspace() {
        control.save(this.currentWorkspace);
    }

    public String getCurrentWorkspace() {
        return currentWorkspace;
    }

    public GUIPlugin getPluginForWorkspace(String workspace) {
        return workspacePlugins.get(workspace);
    }

    public GUIFramework getFramework() {
        return framework;
    }

    public ContentArea getContentArea() {
        return contentArea;
    }

    @Override
    public void setNorthEastCornerComponent(JComponent component) {
        getContentArea().setNorthEastComponent(component);
    }
}
