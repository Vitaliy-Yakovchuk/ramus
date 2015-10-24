package com.ramussoft.gui.core;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.ActionDescriptor;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.ViewPlugin;
import com.ramussoft.gui.common.prefrence.Preferences;

public class PreferenciesPlugin extends AbstractViewPlugin {

    private List<ViewPlugin> list;

    private Engine engine;

    public PreferenciesPlugin(List<ViewPlugin> list, Engine engine) {
        this.list = list;
        this.engine = engine;
    }

    @Override
    public String getName() {
        return "ApplicationPreferencies";
    }

    private class ApplicationPreferencies extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 5467170360697334008L;

        private PreferenciesDialog dialog = null;

        {
            putValue(ACTION_COMMAND_KEY, "ApplicationPreferences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getDialog().setVisible(true);
            dialog = null;
        }

        public PreferenciesDialog getDialog() {
            if (dialog == null) {
                List<Preferences> pList = new ArrayList<Preferences>();
                for (ViewPlugin p : list) {
                    Preferences[] ps = p.getApplicationPreferences();
                    if (ps != null)
                        for (Preferences pr : ps)
                            pList.add(pr);
                }
                dialog = new PreferenciesDialog(framework.getMainFrame(),
                        pList, null) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = 3923523895674781982L;
                };
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setTitle(GlobalResourcesManager
                        .getString("ApplicationPreferences"));

            }
            return dialog;
        }

    }

    ;

    private class ProjectPreferencies extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -1902019757981754973L;

        private PreferenciesDialog dialog = null;

        {
            putValue(ACTION_COMMAND_KEY, "ProjectPreferences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getDialog().setVisible(true);
            dialog = null;
        }

        public PreferenciesDialog getDialog() {
            if (dialog == null) {
                List<Preferences> pList = new ArrayList<Preferences>();
                for (ViewPlugin p : list) {
                    Preferences[] ps = p.getProjectPreferences();
                    if (ps != null)
                        for (Preferences pr : ps)
                            pList.add(pr);
                }
                dialog = new PreferenciesDialog(framework.getMainFrame(),
                        pList, engine) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = 3661287588074004318L;
                };
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setTitle(GlobalResourcesManager
                        .getString("ProjectPreferences"));
            }
            return dialog;
        }

    }

    ;

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor project = new ActionDescriptor();
        project.setAction(new ProjectPreferencies());
        project.setMenu("Tools");
        ActionDescriptor application = new ActionDescriptor();
        application.setAction(new ApplicationPreferencies());
        application.setMenu("Tools");
        ActionDescriptor separator = new ActionDescriptor();
        separator.setMenu("Tools");
        return new ActionDescriptor[]{separator, project, separator,
                application};
    }
}
