package com.ramussoft.client;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.ramussoft.common.Engine;
import com.ramussoft.common.cached.Cached;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserProvider;

public class ClientPlugin extends AbstractViewPlugin implements Commands {

    private UserProvider provider;

    public static ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.client.client");

    private boolean refreshSupport;

    public ClientPlugin(UserProvider provider, boolean refreshSupport) {
        this.provider = provider;
        this.refreshSupport = refreshSupport;
    }

    public String getName() {
        return "Net";
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor changePassword = new ActionDescriptor();
        changePassword.setAction(new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 1388478861191637977L;

            {
                putValue(ACTION_COMMAND_KEY, "Action.ChangePassword");
            }

            public void actionPerformed(ActionEvent e) {
                final JPasswordField oldPassword = new JPasswordField();
                final JPasswordField newPassword = new JPasswordField();
                final JPasswordField conformNewPassword = new JPasswordField();
                double[][] size = {
                        {5, TableLayout.FILL, 5},
                        {5, TableLayout.FILL, 5, TableLayout.FILL, 5,
                                TableLayout.FILL, 5, TableLayout.FILL, 5,
                                TableLayout.FILL, 5, TableLayout.FILL, 5}};
                final JPanel panel = new JPanel(new TableLayout(size));
                panel.add(new JLabel(getString("Message.OldPassword")), "1,1");
                panel.add(oldPassword, "1,3");
                panel.add(new JLabel(getString("Message.NewPassword")), "1,5");
                panel.add(newPassword, "1,7");
                panel.add(new JLabel(getString("Message.ConformNewPassword")),
                        "1,9");
                panel.add(conformNewPassword, "1,11");

                BaseDialog dialog = new BaseDialog(framework.getMainFrame(),
                        true) {

                    {
                        setTitle(getString("ChangePasswordDialog.Title"));
                        setMainPane(panel);
                    }

                    /**
                     *
                     */
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onOk() {
                        if (!Arrays.equals(newPassword.getPassword(),
                                conformNewPassword.getPassword())) {
                            JOptionPane
                                    .showMessageDialog(
                                            framework.getMainFrame(),
                                            getString("Message.ConforPasswordsFailded"));
                            return;
                        }

                        User user = provider.me();
                        if (Arrays.equals(user.getPassword().toCharArray(),
                                oldPassword.getPassword())) {
                            provider.changePassword(new String(newPassword
                                    .getPassword()));
                            super.onOk();
                        } else {
                            JOptionPane.showMessageDialog(framework
                                            .getMainFrame(),
                                    getString("Message.OldPasswordWrong"));
                        }
                    }
                };
                dialog.pack();
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        changePassword.setMenu("Net");

        ActionDescriptor refresh = new ActionDescriptor();
        refreshAction.putValue(Action.ACTION_COMMAND_KEY, "Refresh");
        refreshAction.putValue(Action.SMALL_ICON,
                getIcon("/com/ramussoft/gui/refresh.png"));
        // refreshAction.putValue(Action.LARGE_ICON_KEY,
        // getIcon("/com/ramussoft/gui/22x22/refresh.png"));
        refresh.setAction(refreshAction);
        refresh.setMenu("Edit");
        refresh.setToolBar("Edit");

        if (refreshSupport)
            return new ActionDescriptor[]{changePassword, refresh};
        else
            return new ActionDescriptor[]{changePassword};
    }

    private AbstractAction refreshAction = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = -4072851007034587989L;

        public void actionPerformed(java.awt.event.ActionEvent e) {
            Engine engine = framework.getEngine();
            if (engine instanceof Cached)
                ((Cached) engine).clearCache();
            framework.propertyChanged(FULL_REFRESH);
        }
    };

    private ImageIcon getIcon(String resourceName) {
        return new ImageIcon(getClass().getResource(resourceName));
    }

    @Override
    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return GlobalResourcesManager.getString(key);
        }
    }
}
