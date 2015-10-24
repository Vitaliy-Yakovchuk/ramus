package com.ramussoft.client;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.net.common.UserFactory;

public class AdminPanelPlugin extends AbstractViewPlugin {

    private UserFactory userFactory;

    private Engine engine;

    private ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.client.client");

    public AdminPanelPlugin(UserFactory userFactory, Engine engine) {
        this.userFactory = userFactory;
        this.engine = engine;
    }

    public String getName() {
        return "Net Admin";
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor editUsers = new ActionDescriptor();
        editUsers.setMenu("Net");

        editUsers.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 3226907200960843605L;

            {
                putValue(ACTION_COMMAND_KEY, "Action.EditUsers");
            }

            public void actionPerformed(ActionEvent e) {
                EditUsersDialog dialog = new EditUsersDialog(
                        AdminPanelPlugin.this.framework.getMainFrame(),
                        userFactory, AdminPanelPlugin.this, engine);
                dialog.setVisible(true);
                Options.saveOptions(dialog);
            }

        });

        return new ActionDescriptor[]{editUsers};
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
