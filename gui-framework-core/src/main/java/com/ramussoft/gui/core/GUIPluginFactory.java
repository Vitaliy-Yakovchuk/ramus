package com.ramussoft.gui.core;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

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

import bibliothek.gui.DockController;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CContentArea.Corner;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.action.CCheckBox;
import bibliothek.gui.dock.common.action.CPanelPopup;
import bibliothek.gui.dock.common.action.panel.PanelPopupWindow;
import bibliothek.gui.dock.common.event.CControlListener;
import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.intern.action.CDecorateableAction;
import bibliothek.gui.dock.title.DockTitle.Orientation;
import bibliothek.util.xml.XElement;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractGUIPluginFactory;
import com.ramussoft.gui.common.AdditionalGUIPluginLoader;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.PopupTrigger;
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

public class GUIPluginFactory extends AbstractGUIPluginFactory {

    private static final String WORKSPACES_XML = "workspaces.xml";

    private GUIFramework framework;

    private PlugableFrame plugableFrame;

    private List<UniqueView> uniqueViews = new ArrayList<UniqueView>();

    private List<TabbedView> tabbedViews = new ArrayList<TabbedView>();

    private List<DefaultSingleCDockable> uniqueDockables = new ArrayList<DefaultSingleCDockable>();

    private List<CWorkingArea> uniqueWorkingAreas = new ArrayList<CWorkingArea>();

    private List<String> workspaces = new ArrayList<String>();

    private Hashtable<String, GUIPlugin> pluginsForViewIds = new Hashtable<String, GUIPlugin>();

    private Hashtable<String, TabbedFactory> factories = new Hashtable<String, TabbedFactory>();

    private Hashtable<TabView, TabDockable> tabDockables = new Hashtable<TabView, TabDockable>();

    private Hashtable<Thread, Boolean> recHash = new Hashtable<Thread, Boolean>();

    private Hashtable<String, GUIPlugin> workspacePlugins = new Hashtable<String, GUIPlugin>();

    private Hashtable<Action, CDecorateableAction> actionButtons = new Hashtable<Action, CDecorateableAction>();

    private String currentWorkspace;

    private CControl control;

    private CContentArea contentArea;

    private View lastActiveView;

    static List<PlugableFrame> frames = new ArrayList<PlugableFrame>();

    private Hashtable<String, View> workspaceViews = new Hashtable<String, View>();

    public GUIPluginFactory(List<GUIPlugin> plugins, Engine engine,
                            AccessRules rules, String clientType, String[] groups) {
        this(plugins, engine, rules, clientType, groups, true);
    }

    public GUIPluginFactory(List<GUIPlugin> plugins, Engine engine,
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
                return GUIPluginFactory.this.openView(actionEvent);
            }

            @Override
            public View getActiveView() {
                if (control == null)
                    return null;
                int count = control.getCDockableCount();
                if (count < 1)
                    return null;
                DockController controller = control.intern().getController();
                for (int i = 0; i < count; i++) {
                    CDockable dockable = control.getCDockable(i);
                    if (dockable == null)
                        continue;
                    try {
                        if (controller.isFocused(dockable.intern())) {
                            if (dockable instanceof TabDockable) {
                                for (Entry<TabView, TabDockable> e : tabDockables
                                        .entrySet()) {
                                    if (e.getValue().equals(dockable))
                                        return e.getKey();
                                }
                            }
                            if (dockable instanceof CWorkingArea) {
                            } else {
                                String id = ((SingleCDockable) dockable)
                                        .getUniqueId();
                                return findUniqueView(id);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            public List<View> getAllViews() {
                List<View> result = new ArrayList<View>();
                Enumeration<TabView> keys = tabDockables.keys();
                while (keys.hasMoreElements())
                    result.add(keys.nextElement());

                int count = control.getCDockableCount();
                if (count < 1)
                    return null;
                for (int i = 0; i < count; i++) {
                    CDockable dockable = control.getCDockable(i);
                    if (dockable.isVisible()) {
                        if (dockable instanceof TabDockable) {
                        } else if (dockable instanceof CWorkingArea) {
                        } else {
                            String id = ((SingleCDockable) dockable)
                                    .getUniqueId();
                            result.add(findUniqueView(id));
                        }
                    }
                }

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
                return GUIPluginFactory.this.getCurrentWorkspace();
            }

            @Override
            public void setCurrentWorkspace(String workspace) {
                GUIPluginFactory.this.setCurrentWorkspace(workspace);
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
                        GUIPluginFactory.this.tabCreated(event);
                    }

                    @Override
                    public void tabRemoved(TabbedEvent event) {
                        GUIPluginFactory.this.tabRemoved(event);
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

        getContentArea().setCornerComponent(plugableFrame.getToolBarsPanel(),
                Corner.NORTH_WEST, true);

        plugableFrame.setSize(1024, 800);
        plugableFrame.setLocationRelativeTo(null);
        Options.loadOptions(plugableFrame);
        plugableFrame.setExtendedState(Options.getInteger("EXTENDED_STATE",
                plugableFrame.getExtendedState()));
        String fn = Options.getPreferencesPath() + WORKSPACES_XML;
        try {
            File file = new File(fn);
            if (file.exists())
                control.readXML(file);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
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
                Options.setInteger("EXTENDED_STATE",
                        plugableFrame.getExtendedState());
                String fn = Options.getPreferencesPath() + WORKSPACES_XML;
                try {
                    saveCurrentWorkspace();
                    control.writeXML(new File(fn));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
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
                DefaultSingleCDockable dockable = findUniqueDockable(view
                        .getId());
                dockable.setVisible(true);
                dockable.intern().getController()
                        .setFocusedDockable(dockable.intern(), false);
                return true;
            }
        }

        Enumeration<TabView> enumeration = tabDockables.keys();
        while (enumeration.hasMoreElements()) {
            TabView view = enumeration.nextElement();
            if (actionEvent.equals(view.getOpenAction())) {
                TabDockable dockable = tabDockables.get(view);
                dockable.intern().getController()
                        .setFocusedDockable(dockable.intern(), false);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void initContent() {

        control = new CControl(plugableFrame, false);

        // control.setTheme(control.getThemes().getFactory(2).create());

        control.addControlListener(new CControlListener() {

            @Override
            public void added(CControl control, CDockable dockable) {
            }

            @Override
            public void closed(CControl control, CDockable dockable) {
                Boolean b = recHash.get(Thread.currentThread());
                if (b != null)
                    return;
                recHash.put(Thread.currentThread(), true);
                List<TabView> list = new ArrayList<TabView>();
                for (Entry<TabView, TabDockable> e : tabDockables.entrySet()) {
                    if (e.getValue().equals(dockable)) {
                        TabView view = e.getKey();
                        list.add(view);
                    }

                }

                for (TabView view : list)
                    view.close();

                recHash.remove(Thread.currentThread());
            }

            @Override
            public void opened(CControl control, CDockable dockable) {
            }

            @Override
            public void removed(CControl control, CDockable dockable) {
            }

        });

        control.addFocusListener(new CFocusListener() {

            @Override
            public void focusGained(CDockable dockable) {
                List<View> list = new ArrayList<View>();
                for (Entry<TabView, TabDockable> e : tabDockables.entrySet()) {
                    if (e.getValue().equals(dockable)) {
                        TabView view = e.getKey();
                        list.add(view);
                    }

                }

                if (dockable instanceof DefaultSingleCDockable) {
                    list.add(findUniqueView(((DefaultSingleCDockable) dockable)
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
            public void focusLost(CDockable dockable) {
                List<View> list = new ArrayList<View>();
                for (Entry<TabView, TabDockable> e : tabDockables.entrySet()) {
                    if (e.getValue().equals(dockable)) {
                        TabView view = e.getKey();
                        list.add(view);
                    }

                }

                if (dockable instanceof DefaultSingleCDockable) {
                    list.add(findUniqueView(((DefaultSingleCDockable) dockable)
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
            final DefaultSingleCDockable dockable = new DefaultSingleCDockable(
                    id, findPluginForViewId(id).getString(id),
                    view.createComponent());
            dockable.setCloseable(true);
            initActions(view.getId(), view, dockable);
            control.addDockable(dockable);
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

        for (TabbedView view : tabbedViews) {

            String id = view.getId();
            if (getWorkingArea(id) != null)
                continue;
            CWorkingArea area = control.createWorkingArea(id);
            uniqueWorkingAreas.add(area);
            // area.setTitleShown(false);
            // if (currentWorkspace.equals(view.getDefaultWorkspace()))
            area.setVisible(true);

            TabbedFactory factory = new TabbedFactory();

            factories.put(id, factory);
            control.addMultipleDockableFactory(id, factory);

            // dockable.setWorkingArea(area);
            // dockable1.setWorkingArea(area);

            // control.add(dockable1);
            // control.add(dockable);

            // dockable.setLocation(CLocation.base().)

        }
        framework.setMainFrame(plugableFrame);
    }

    private void initActions(final String id, final View view,
                             final DefaultCDockable dockable) {
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
                        CDecorateableAction button = actionButtons.remove(a);
                        if (button != null)
                            dockable.removeAction(button);
                    }
            }
        });
    }

    private void addActions(String id, final View view,
                            final DefaultCDockable dockable, Action[] actions) {
        for (final Action action : actions)
            if (action == null) {
                dockable.addSeparator();
            } else {
                ImageIcon icon = (ImageIcon) action.getValue(Action.SMALL_ICON);
                if (icon == null) {
                    icon = new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/icon.png"));
                }
                final String command = (String) action
                        .getValue(Action.ACTION_COMMAND_KEY);
                String text = null;

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

                final CDecorateableAction button;

                if (action.getValue(Action.SELECTED_KEY) == null) {
                    final PopupTrigger trigger = (PopupTrigger) action
                            .getValue(POPUP_MENU);
                    if (trigger == null)
                        button = new CButton(text, icon) {

                            @Override
                            protected void action() {
                                java.awt.event.ActionEvent e = new java.awt.event.ActionEvent(
                                        view, 0, command);
                                action.actionPerformed(e);
                            }
                        };
                    else {
                        button = new CPanelPopup() {
                            @Override
                            public void openPopup(PanelPopupWindow window) {
                                super.openPopup(window);
                            }

                            @Override
                            protected void openDialog(JComponent item,
                                                      Orientation orientation) {
                                JPanel menu = (JPanel) getContent();
                                menu.removeAll();

                                for (Action action : trigger.getPopupActions())
                                    menu.add(new JButton(action));
                                menu.revalidate();
                                menu.repaint();
                                super.openDialog(item, orientation);
                            }
                        };

                        ((CPanelPopup) button).setContent(new JPanel(
                                new GridLayout(0, 1, 0, 0)));
                        button.setText(text);
                        button.setIcon(icon);

                    }
                } else {
                    button = new CCheckBox(text, icon) {
                        @Override
                        protected void changed() {
                            java.awt.event.ActionEvent e = new java.awt.event.ActionEvent(
                                    view, 0, command);
                            action.putValue(Action.SELECTED_KEY, isSelected());
                            action.actionPerformed(e);
                        }
                    };
                    action.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (Action.SELECTED_KEY.equals(evt
                                    .getPropertyName())) {
                                ((CCheckBox) button).setSelected((Boolean) evt
                                        .getNewValue());
                            }
                        }
                    });
                    ((CCheckBox) button).setSelected((Boolean) action
                            .getValue(Action.SELECTED_KEY));
                }

                String tooltip = (String) action
                        .getValue(Action.LONG_DESCRIPTION);

                if (tooltip == null)
                    tooltip = (String) action
                            .getValue(Action.SHORT_DESCRIPTION);

                if (tooltip == null)
                    tooltip = text;

                if (tooltip != null)
                    button.setTooltip(tooltip);

                KeyStroke stroke = (KeyStroke) action
                        .getValue(Action.ACCELERATOR_KEY);
                if (stroke != null)
                    button.setAccelerator(stroke);

                actionButtons.put(action, button);

                button.setEnabled(action.isEnabled());
                dockable.addAction(button);
                action.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals("enabled")) {
                            button.setEnabled((Boolean) evt.getNewValue());
                        }
                    }
                });
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

    public DefaultSingleCDockable findUniqueDockable(String id) {
        for (DefaultSingleCDockable dockable : uniqueDockables) {
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

        TabDockable dockable = tabDockables.get(event.getTabableView());
        if (dockable != null) {
            dockable.intern().getController()
                    .setFocusedDockable(dockable.intern(), false);
            return;
        }

        String id = event.getTabViewId();
        CWorkingArea area = getWorkingArea(id);
        if (!area.isVisible())
            area.setVisible(true);
        dockable = new TabDockable(factories.get(id), view.createComponent());
        tabDockables.put(view, dockable);
        dockable.setRemoveOnClose(true);

        initActions(id, view, dockable);
        dockable.setLocation(CLocation.working(area).rectangle(0, 0, 1, 1));
        dockable.setTitleText(view.getTitle());
        dockable.setCloseable(true);

        area.add(dockable);
        dockable.setVisible(true);
        dockable.intern().getController()
                .setFocusedDockable(dockable.intern(), false);
    }

    protected void tabRemoved(TabbedEvent event) {
        Boolean b = recHash.get(Thread.currentThread());
        TabDockable dockable = tabDockables.get(event.getTabableView());
        if (b == null) {
            recHash.put(Thread.currentThread(), true);

            if (dockable != null) {

                dockable.setVisible(false);
            }
            recHash.remove(Thread.currentThread());
        }
        tabDockables.remove(event.getTabableView());
    }

    private CWorkingArea getWorkingArea(String id) {
        for (CWorkingArea area : uniqueWorkingAreas) {
            if (area.getUniqueId().equals(id))
                return area;
        }
        return null;
    }

    @Override
    public JFrame getMainFrame() {
        return plugableFrame;
    }

    private class TabbedFactory implements
            MultipleCDockableFactory<TabDockable, TabbedLayout> {
        public TabbedLayout create() {
            return new TabbedLayout();
        }

        public TabDockable read(TabbedLayout layout) {
            return null;
        }

        public TabbedLayout write(TabDockable dockable) {
            TabbedLayout layout = new TabbedLayout();
            return layout;
        }

        @Override
        public boolean match(TabDockable dockable, TabbedLayout layout) {
            return false;
        }

    }

    private static class TabbedLayout implements MultipleCDockableLayout {

        public void readStream(DataInputStream in) throws IOException {
            // name = in.readUTF();
        }

        public void readXML(XElement element) {
            // name = element.getString();
        }

        public void writeStream(DataOutputStream out) throws IOException {
            // out.writeUTF(name);
        }

        public void writeXML(XElement element) {
            // element.setString(name);
        }
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
                        TabDockable dockable = tabDockables.get(view);
                        dockable.intern().getController()
                                .setFocusedDockable(dockable.intern(), false);
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

    public CContentArea getContentArea() {
        return contentArea;
    }

    @Override
    public void setNorthEastCornerComponent(JComponent component) {
        getContentArea().setCornerComponent(component, Corner.NORTH_EAST, true);
    }
}
