package com.ramussoft.script;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ramussoft.common.Engine;
import com.ramussoft.common.event.StreamEvent;
import com.ramussoft.common.event.StreamListener;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;

public class JSModulesEditor extends AbstractUniqueView implements UniqueView,
        Constants {

    /**
     *
     */

    private Engine engine;

    private JList jList = new JList(new DefaultListModel());

    private AddModuleAction addModuleAction = new AddModuleAction();

    private RemoveModuleAction removeModuleAction = new RemoveModuleAction();

    private OpenModuleAction openModuleAction = new OpenModuleAction();

    private class Script {

        String scriptName;

        public Script(String scriptName) {
            this.scriptName = scriptName;
        }

        @Override
        public String toString() {
            return scriptName.substring(PREFIX.length());
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result
                    + ((scriptName == null) ? 0 : scriptName.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof Script))
                return false;
            Script other = (Script) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (scriptName == null) {
                if (other.scriptName != null)
                    return false;
            } else if (!scriptName.equals(other.scriptName))
                return false;
            return true;
        }

        private JSModulesEditor getOuterType() {
            return JSModulesEditor.this;
        }

    }

    ;

    public JSModulesEditor(GUIFramework framework) {
        super(framework);
        this.engine = framework.getEngine();
        loadModules();
        framework.addActionListener(Commands.FULL_REFRESH,
                new ActionListener() {

                    @Override
                    public void onAction(
                            com.ramussoft.gui.common.event.ActionEvent event) {
                        loadModules();
                    }
                });
        this.engine.addStreamListener(new StreamListener() {

            @Override
            public void streamUpdated(StreamEvent event) {
                if (event.getPath().startsWith(PREFIX)) {
                    DefaultListModel model = (DefaultListModel) jList
                            .getModel();
                    Script obj = new Script(event.getPath());
                    if (model.indexOf(obj) < 0)
                        model.addElement(obj);
                }
            }

            @Override
            public void streamDeleted(StreamEvent event) {
                if (event.getPath().startsWith(PREFIX)) {
                    DefaultListModel model = (DefaultListModel) jList
                            .getModel();
                    model.removeElement(new Script(event.getPath()));
                }
            }
        });
        jList.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        Script script = (Script) jList.getSelectedValue();
                        removeModuleAction.setEnabled((script != null)
                                && (JSModulesEditor.this.framework
                                .getAccessRules()
                                .canUpdateStream(script.scriptName)));
                        openModuleAction.setEnabled(script != null);
                    }
                });
        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1)
                    openModule();
            }
        });

        addModuleAction
                .setEnabled(framework.getAccessRules().canCreateStript());
    }

    public void loadModules() {
        String[] streamNames = engine.getStreamNames();
        DefaultListModel model = (DefaultListModel) jList.getModel();
        model.clear();

        Arrays.sort(streamNames);

        for (String streamName : streamNames)
            if (streamName.startsWith(PREFIX))
                model.addElement(new Script(streamName));

    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.Qualifiers";
    }

    @Override
    public String getId() {
        return "JavaScripts";
    }

    @Override
    public JComponent createComponent() {
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(jList);
        return pane;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{addModuleAction, openModuleAction,
                removeModuleAction};
    }

    private class AddModuleAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 5785539119436693300L;

        public AddModuleAction() {
            putValue(ACTION_COMMAND_KEY, "Action.AddModule");
            putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/add.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NewJavaScriptDialog dialog = new NewJavaScriptDialog(framework);
            dialog.setVisible(true);
            engine.setStream(dialog.getModuleName(), new byte[]{});
        }

    }

    ;

    private class RemoveModuleAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -4837180640407894283L;

        public RemoveModuleAction() {
            putValue(ACTION_COMMAND_KEY, "Action.RemoveModule");
            putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/delete.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Script script = (Script) jList.getSelectedValue();
            if (script != null) {
                if (JOptionPane.showConfirmDialog(jList, MessageFormat.format(
                        ScriptPlugin.getBundle().getString(
                                "DoYouReallyWantToRemoveJavaScript"), script
                                .toString()), UIManager
                                .getString("OptionPane.titleText"),
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    engine.deleteStream(script.scriptName);
            }
        }

    }

    ;

    private class OpenModuleAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -4837180640407894283L;

        public OpenModuleAction() {
            putValue(ACTION_COMMAND_KEY, "Action.OpenModule");
            putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/open.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openModule();
        }

    }

    ;

    @Override
    public String getString(String key) {
        try {
            return ScriptPlugin.getBundle().getString(key);
        } catch (Exception e) {
            return super.getString(key);
        }
    }

    public void openModule() {
        Script script = (Script) jList.getSelectedValue();
        if (script != null) {
            framework.propertyChanged("OpenScript", script.scriptName);
        }
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.EAST;
    }
}
