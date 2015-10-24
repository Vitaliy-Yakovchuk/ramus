package com.ramussoft.gui.core;

import java.awt.FlowLayout;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.ActionLevel;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.StringGetter;
import com.ramussoft.gui.common.ViewPlugin;

import static com.ramussoft.gui.common.GUIFramework.*;

public class PlugableFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1542248330155382728L;

    private List<ViewPlugin> plugins;

    private Hashtable<String, JToolBar> toolBars = new Hashtable<String, JToolBar>();

    private TreeList mainMenu = new TreeList();

    private TreeList toolBarActions = new TreeList();

    private Hashtable<String, ButtonGroup> radioGroups = new Hashtable<String, ButtonGroup>();

    private Hashtable<String, Action> viewActions = new Hashtable<String, Action>();

    private JPanel toolBarsPanel;

    public PlugableFrame(List<ViewPlugin> plugins) {
        this.plugins = plugins;
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/com/ramussoft/gui/application.png")));
        init();
    }

    private String getActionShortDescriptionKey(String key) {
        return getActionString(key + ".Short");
    }

    private String getActionName(String key) {
        return getActionString(key);
    }

    private String getActionLongDescriptionKey(String key) {
        return getActionString(key + ".Long");
    }

    private String getActionString(String key) {
        return key;
    }

    private void init() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initActions();
        createJMenu();
        createToolBars();
    }

    private void initActions() {
        for (ViewPlugin plugin : plugins) {
            ActionDescriptor[] actionDescriptors = plugin
                    .getActionDescriptors();
            for (ActionDescriptor actionDescriptor : actionDescriptors) {

                Action action = actionDescriptor.getAction();

                if (action != null) {
                    String command = (String) action
                            .getValue(Action.ACTION_COMMAND_KEY);
                    String des = plugin
                            .getString(getActionShortDescriptionKey(command));
                    if (des == null) {
                        des = plugin.getString(getActionName(command));
                        if (des == null) {
                            StringGetter getter = (StringGetter) action
                                    .getValue(StringGetter.ACTION_STRING_GETTER);
                            if (getter != null)
                                des = getter.getString(getActionName(command));
                        }
                    }
                    action.putValue(Action.SHORT_DESCRIPTION, des);
                    String value = plugin.getString(getActionName(command));
                    if (value == null)
                        value = command;
                    action.putValue(Action.NAME, value);
                    des = plugin
                            .getString(getActionLongDescriptionKey(command));
                    if (des == null)
                        des = (String) action
                                .getValue(Action.SHORT_DESCRIPTION);
                    action.putValue(Action.LONG_DESCRIPTION, des);
                    if (actionDescriptor.getActionLevel().equals(
                            ActionLevel.VIEW)) {
                        viewActions.put(command, action);
                        action.setEnabled(false);
                    }
                }

                add(actionDescriptor, plugin);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void add(ActionDescriptor actionDescriptor, ViewPlugin plugin) {

        final Action action = actionDescriptor.getAction();

        if (actionDescriptor.isSelective()) {

            String group = actionDescriptor.getButtonGroup();

            if (group != null) {
                ButtonGroup group2 = (ButtonGroup) action
                        .getValue(BUTTON_GROUP);
                if (group2 != null)
                    radioGroups.put(group, group2);
                else {
                    if ((group2 = radioGroups.get(group)) == null) {
                        group2 = new ButtonGroup();
                        radioGroups.put(group, group2);
                    }
                    action.putValue(BUTTON_GROUP, group2);
                }
            }

            if (action.getValue(Action.SELECTED_KEY) == null)
                action.putValue(Action.SELECTED_KEY, Boolean.FALSE);
        }

        if (actionDescriptor.getMenu() != null)
            addTreeItem(actionDescriptor.getMenu(), action, plugin);
        String toolBar = actionDescriptor.getToolBar();
        if (toolBar != null) {
            String key = "ToolBar." + toolBar;
            toolBar = plugin.getString(key);
            if (toolBar == null)
                toolBar = key;

            ArrayList<Action> list = (ArrayList<Action>) toolBarActions
                    .get(toolBar);
            if (list == null) {
                list = new ArrayList<Action>();
                toolBarActions.put(toolBar, list);
            }
            list.add(action);
        }

    }

    @SuppressWarnings("unchecked")
    private void addTreeItem(String menu, Action action, ViewPlugin plugin) {
        String[] strings = menu.split("/");
        for (int i = 0; i < strings.length; i++) {
            strings[i] = plugin.getString("Menu." + strings[i]);
        }

        TreeList map = mainMenu;
        for (int i = 0; i < strings.length - 1; i++) {
            TreeList m = (TreeList) map.get(strings[i]);
            if (m == null) {
                m = new TreeList();
                map.put(strings[i], m);
            }
            map = m;
        }
        ArrayList<Action> actions = (ArrayList<Action>) map
                .get(strings[strings.length - 1]);
        if (actions == null) {
            actions = new ArrayList<Action>();
            map.put(strings[strings.length - 1], actions);
        }
        actions.add(action);
    }

    @SuppressWarnings("unchecked")
    private void createJMenu() {
        JMenuBar bar = new JMenuBar();

        List<JMenu> menus = new ArrayList<JMenu>();

        for (Object object : mainMenu) {
            Entry<String, Object> entry = (Entry<String, Object>) object;
            JMenu menu = new JMenu(entry.getKey());
            initMenu(menu, entry.getValue());
            menus.add(menu);
        }

        String[] firsts = {GlobalResourcesManager.getString("Menu.File"),
                GlobalResourcesManager.getString("Menu.Edit"),
                GlobalResourcesManager.getString("Menu.View")};

        String[] lasts = {GlobalResourcesManager.getString("Menu.Windows"),
                GlobalResourcesManager.getString("Menu.Tools"),
                GlobalResourcesManager.getString("Menu.Help")};
        for (String s : firsts) {
            for (int i = 0; i < menus.size(); i++) {
                if (menus.get(i).getText().equals(s)) {
                    bar.add(menus.get(i));
                    menus.remove(i);
                    break;
                }
            }
        }

        for (int i = menus.size() - 1; i >= 0; i--) {
            JMenu jMenu = menus.get(i);
            boolean add = true;
            for (String s : lasts) {
                if (jMenu.getText().equals(s)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                bar.add(jMenu);
                menus.remove(i);
            }
        }

        for (String s : lasts) {
            for (int i = 0; i < menus.size(); i++) {
                if (menus.get(i).getText().equals(s)) {
                    bar.add(menus.get(i));
                    menus.remove(i);
                    break;
                }
            }
        }

        this.setJMenuBar(bar);
    }

    @SuppressWarnings("unchecked")
    private void initMenu(JMenu menu, Object value) {
        if (value == null) {
            menu.addSeparator();
        } else if (value instanceof TreeList) {
            TreeList list = (TreeList) value;
            for (Object object : list) {
                if (object instanceof Entry) {
                    Entry<String, Object> entry = (Entry<String, Object>) object;
                    JMenu menu2 = new JMenu(entry.getKey());
                    initMenu(menu2, entry.getValue());
                    menu.add(menu2);
                } else
                    addMenuItem(menu, (Action) object);
            }
        } else if (value instanceof ArrayList) {
            ArrayList<Action> actions = (ArrayList) value;
            for (Action action : actions) {
                addMenuItem(menu, action);
            }
        } else {
            throw new RuntimeException("Unknow type of value");
        }

    }

    private void addMenuItem(JMenu menu, Action action) {
        if (action != null) {
            if (action.getValue(Action.SELECTED_KEY) == null)
                menu.add(action);
            else {
                JMenuItem item;
                ButtonGroup group = (ButtonGroup) action.getValue(BUTTON_GROUP);
                if (group == null) {
                    item = new JCheckBoxMenuItem(action);
                } else {
                    item = new JRadioButtonMenuItem(action);
                    Boolean added = (Boolean) action
                            .getValue(BUTTON_GROUP_ADDED);
                    if (added == null)
                        added = Boolean.FALSE;
                    if (!added) {
                        group.add(item);
                        action.putValue(BUTTON_GROUP_ADDED, Boolean.TRUE);
                    }
                }
                menu.add(item);
            }
        } else
            menu.addSeparator();
    }

    @SuppressWarnings("unchecked")
    private void createToolBars() {
        toolBarsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Object object : toolBarActions) {
            Entry<String, Object> entry = (Entry<String, Object>) object;
            List<Action> list = (List) entry.getValue();
            JToolBar toolBar = new JToolBar();
            for (Action a : list) {
                if (a == null)
                    toolBar.addSeparator();
                else {
                    AbstractButton button;
                    if (a.getValue(Action.SELECTED_KEY) == null)
                        button = toolBar.add(a);
                    else {
                        ButtonGroup group = (ButtonGroup) a
                                .getValue(BUTTON_GROUP);
                        button = new JToggleButton(a);
                        button.setToolTipText(button.getText());
                        button.setText(null);
                        toolBar.add(button);
                        Boolean added = (Boolean) a
                                .getValue(BUTTON_GROUP_ADDED);
                        if (added == null)
                            added = Boolean.FALSE;
                        if ((group != null) && (!added.booleanValue())) {
                            group.add(button);
                            a.putValue(BUTTON_GROUP_ADDED, Boolean.TRUE);
                        }
                    }
                    button.setFocusable(false);
                }
            }
            toolBars.put(entry.getKey(), toolBar);
            toolBarsPanel.add(toolBar);
        }
        for (ViewPlugin p : plugins) {
            for (JToolBar bar : p.getToolBars()) {
                toolBarsPanel.add(bar);
            }
        }
    }

    public JPanel getToolBarsPanel() {
        return toolBarsPanel;
    }

    private static class TreeList extends ArrayList<Object> {

        /**
         *
         */
        private static final long serialVersionUID = -4063350955871122394L;

        @SuppressWarnings("unchecked")
        public Object get(String key) {
            for (Object o : this)
                if (o instanceof Entry) {
                    Entry<String, Object> e = (Entry<String, Object>) o;
                    if (e.getKey().equals(key))
                        return e.getValue();
                }
            return null;
        }

        public void put(final String key, final Object object) {
            Entry<String, Object> e = new Entry<String, Object>() {

                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public Object getValue() {
                    return object;
                }

                @Override
                public Object setValue(Object value) {
                    return null;
                }

            };
            super.add(e);
        }

    }

    @Override
    public void dispose() {
        removeAll();
        this.plugins = null;
        this.mainMenu = null;
        this.toolBarActions = null;
        this.toolBars = null;
        super.dispose();
        GUIPluginFactory.frames.remove(this);
    }

    public void setViewActions(String[] globalActions) {
        for (Entry<String, Action> entry : viewActions.entrySet()) {
            if (isPresent(entry.getKey(), globalActions))
                entry.getValue().setEnabled(true);
            else
                entry.getValue().setEnabled(false);
        }
    }

    private boolean isPresent(String key, String[] globalActions) {
        for (String a : globalActions) {
            if (a.equals(key))
                return true;
        }
        return false;
    }

}
