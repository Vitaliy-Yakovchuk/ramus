package com.ramussoft.gui.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Plugin;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.ActionLevel;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class AboutPlugin extends AbstractViewPlugin {

    private Engine engine;

    private Object lock = new Object();

    private ActionListener showHelpContext = null;

    public AboutPlugin(Engine engine) {
        this.engine = engine;
    }

    @Override
    public String getName() {
        return "About";
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor about = new ActionDescriptor();

        about.setActionLevel(ActionLevel.GLOBAL);
        about.setMenu("Help");
        about.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -7354349852168425066L;

            {
                putValue(ACTION_COMMAND_KEY, "About");
            }

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog dialog = new AboutDialog(framework.getMainFrame(),
                        (List<Plugin>) engine.getPluginProperty("Core",
                                "PluginList"), (List<GUIPlugin>) engine
                        .getPluginProperty("GUI", "PluginList"));
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setVisible(true);
                dialog.dispose();
            }
        });

        ActionDescriptor content = new ActionDescriptor();

        content.setActionLevel(ActionLevel.GLOBAL);
        content.setMenu("Help");
        content.setAction(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -7354349852168425066L;

            {
                putValue(ACTION_COMMAND_KEY, "Help.Content");
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_F1, 0));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                openHelp(e);
            }
        });

        ActionDescriptor separator = new ActionDescriptor();
        separator.setMenu("Help");

        return new ActionDescriptor[]{content, separator, about};
    }

    protected void openHelp(ActionEvent e) {
        synchronized (lock) {
            if (showHelpContext == null) {

                HelpSet hs = null;
                try {
                    URL hsURL = getClass().getResource(
                            GlobalResourcesManager.getString("Help.Path"));
                    hs = new HelpSet(null, hsURL);
                    final HelpBroker hb = hs.createHelpBroker();
                    hb.setLocale(Locale.getDefault());
                    showHelpContext = new CSH.DisplayHelpFromSource(hb);
                } catch (final Exception ee) {
                    ee.printStackTrace();
                }

            }
            showHelpContext.actionPerformed(e);
        }
    }

}
