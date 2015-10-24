package com.ramussoft.script;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import javax.swing.JComponent;

import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.event.ActionListener;

public class ScriptEditor extends AbstractView implements TabView, Constants {

    private String scriptPath;

    private SaveScriptAction saveScriptAction = new SaveScriptAction();

    private JEditorPane editorPane;

    private ActionListener actionListener;

    public ScriptEditor(GUIFramework framework, String scriptPath) {
        super(framework);
        this.scriptPath = scriptPath;
        saveScriptAction.setEnabled(framework.getAccessRules().canUpdateStream(
                scriptPath));
        actionListener = new ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                saveScript();
            }
        };
        framework.addActionListener("BeforeFileSave", actionListener);
    }

    @Override
    public JComponent createComponent() {
        JScrollPane pane = new JScrollPane();
        editorPane = new JEditorPane();
        pane.setViewportView(editorPane);
        if (scriptPath.endsWith(".js"))
            editorPane.setContentType("text/javascript");
        byte[] bs = framework.getEngine().getStream(scriptPath);
        if (bs == null)
            bs = new byte[]{};
        try {
            editorPane.setText(new String(bs, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!saveScriptAction.isEnabled())
            editorPane.setEditable(false);
        return pane;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{saveScriptAction};
    }

    @Override
    public String getTitle() {
        return scriptPath.substring(PREFIX.length());
    }

    public String getScriptPath() {
        return scriptPath;
    }

    private class SaveScriptAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -7212398857603498246L;

        public SaveScriptAction() {
            putValue(ACTION_COMMAND_KEY, "Action.SaveScript");
            putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/file-save.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveScript();
        }

    }

    ;

    private void saveScript() {
        byte[] array = framework.getEngine().getStream(scriptPath);
        byte[] toSave = null;
        try {
            toSave = editorPane.getText().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!Arrays.equals(array, toSave)) {
            try {
                ((Journaled) framework.getEngine()).startUserTransaction();
                framework.getEngine().setStream(scriptPath, toSave);
                ((Journaled) framework.getEngine()).commitUserTransaction();
            } catch (Exception e2) {
                ((Journaled) framework.getEngine()).rollbackUserTransaction();
            }
        }
    }

    @Override
    public String getString(String key) {
        try {
            return ScriptPlugin.getBundle().getString(key);
        } catch (Exception e) {
            return super.getString(key);
        }
    }

    @Override
    public void close() {
        if (saveScriptAction.isEnabled())
            saveScript();
        framework.removeActionListener("BeforeFileSave", actionListener);
        super.close();
    }

    @Override
    public com.ramussoft.gui.common.event.ActionEvent getOpenAction() {
        return new com.ramussoft.gui.common.event.ActionEvent("OpenScript",
                scriptPath);
    }
}
