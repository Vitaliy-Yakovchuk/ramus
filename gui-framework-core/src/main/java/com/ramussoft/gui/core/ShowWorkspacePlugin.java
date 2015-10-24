package com.ramussoft.gui.core;

import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.ramussoft.gui.common.AbstractGUIPluginFactory;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.prefrence.Options;

public class ShowWorkspacePlugin extends AbstractViewPlugin {

    private AbstractGUIPluginFactory factory;

    private ActionDescriptor[] actions;

    public ShowWorkspacePlugin(AbstractGUIPluginFactory factory) {
        this.factory = factory;
    }

    @Override
    public void setFramework(GUIFramework framework) {
        super.setFramework(framework);
        framework.addActionListener("ShowWorkspace", new ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                for (ActionDescriptor action : actions)
                    if (action.getAction() instanceof ShowWorkspaceAction) {
                        if (((ShowWorkspaceAction) action.getAction())
                                .getWorkspace().equals(event.getValue())) {
                            action.getAction().actionPerformed(null);
                            action.getAction().putValue(Action.SELECTED_KEY,
                                    Boolean.TRUE);
                        }
                    }

            }

        });
    }

    private class ShowWorkspaceAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -3262823744480612331L;

        private String workspace;

        public ShowWorkspaceAction(String workspace) {
            this.workspace = workspace;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            factory.setCurrentWorkspace(workspace);
        }

        public String getWorkspace() {
            return workspace;
        }

    }

    ;

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        if (actions == null) {
            List<String> workspaces = factory.getWorkspaces();
            actions = new ActionDescriptor[workspaces.size() + 2];

            for (int i = 0; i < actions.length - 2; i++) {
                ActionDescriptor descriptor = new ActionDescriptor();
                descriptor.setSelective(true);
                descriptor.setButtonGroup("ActiveWorkspase");
                actions[i] = descriptor;
                String workspace = workspaces.get(i);
                Action action = new ShowWorkspaceAction(workspace);

                action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_1 + i, KeyEvent.CTRL_MASK
                                | KeyEvent.SHIFT_MASK));

                URL resource = getClass().getResource(
                        "/com/ramussoft/gui/" + workspace + ".png");
                if (resource != null)
                    action.putValue(Action.SMALL_ICON, new ImageIcon(resource));

                action.putValue(Action.ACTION_COMMAND_KEY, factory
                        .getPluginForWorkspace(workspace).getString(workspace));
                String ws = Options.getString("CurrentWorkspace", "");
                action.putValue(Action.SELECTED_KEY, ws.equals(workspace));

                descriptor.setAction(action);
                descriptor.setMenu("Windows/ShowWorkspace");
            }

            int i = actions.length - 2;
            ActionDescriptor descriptor = new ActionDescriptor();
            descriptor.setButtonGroup("ActiveWindowsControl");
            descriptor.setSelective(true);
            actions[i] = descriptor;
            Action action = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = 2506489781529559163L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    Options.setString("WindowsControl", "simple");
                    JOptionPane
                            .showMessageDialog(
                                    factory.getMainFrame(),
                                    GlobalResourcesManager
                                            .getString("LookAndFeelWillApplyAfterProgramReboot"));
                }
            };

            action.putValue(Action.ACTION_COMMAND_KEY, "WindowsControl.simple");
            String ws = Options.getString("WindowsControl", "simple");
            action.putValue(Action.SELECTED_KEY, ws.equals("simple"));

            descriptor.setAction(action);
            descriptor.setMenu("Windows/WindowsControl");

            i = actions.length - 1;
            descriptor = new ActionDescriptor();
            descriptor.setSelective(true);
            descriptor.setButtonGroup("ActiveWindowsControl");
            actions[i] = descriptor;
            action = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = 2506489781529559163L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    Options.setString("WindowsControl", "classic");
                    JOptionPane
                            .showMessageDialog(
                                    factory.getMainFrame(),
                                    GlobalResourcesManager
                                            .getString("LookAndFeelWillApplyAfterProgramReboot"));
                }
            };

            action
                    .putValue(Action.ACTION_COMMAND_KEY,
                            "WindowsControl.classic");
            ws = Options.getString("WindowsControl", "classic");
            action.putValue(Action.SELECTED_KEY, ws.equals("classic"));

            descriptor.setAction(action);
            descriptor.setMenu("Windows/WindowsControl");

        }
        return actions;
    }

    public void createWorkspaceToolBar() {
        JToolBar workspaceToolBar = new JToolBar();
        workspaceToolBar.setFloatable(false);
        for (ActionDescriptor descriptor : getActionDescriptors()) {
            final Action action = descriptor.getAction();
            if (action instanceof ShowWorkspaceAction) {
                JToggleButton button = new JToggleButton(action);
                button.setText(cut((String) action
                        .getValue(Action.ACTION_COMMAND_KEY)));
                button.setToolTipText((String) action
                        .getValue(Action.ACTION_COMMAND_KEY));
                button.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        action.actionPerformed(e);
                    }
                });
                button.setFocusable(false);
                workspaceToolBar.add(button);
            }
        }
        factory.setNorthEastCornerComponent(workspaceToolBar);
    }

    private String cut(String value) {
        if (value.length() > 6) {
            value = value.substring(0, 4) + "...";
        }
        return value;
    }

    @Override
    public String getName() {
        return "ShowWorkspace";
    }

}
