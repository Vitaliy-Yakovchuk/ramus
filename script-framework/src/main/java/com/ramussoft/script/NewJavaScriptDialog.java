package com.ramussoft.script;

import java.text.MessageFormat;

import info.clearthought.layout.TableLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.gui.common.prefrence.Options;

public class NewJavaScriptDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -3991964158500694523L;

    private TextField moduleName = new TextField();

    private Engine engine;

    private String jsModuleName = null;

    public NewJavaScriptDialog(GUIFramework framework) {
        super(framework.getMainFrame(), true);
        this.engine = framework.getEngine();
        setTitle(ScriptPlugin.getBundle()
                .getString("NewJavaScriptDialog.title"));
        double[][] size = {{5, TableLayout.FILL, TableLayout.MINIMUM, 5},
                {5, TableLayout.MINIMUM, 5}};
        JPanel panel = new JPanel(new TableLayout(size));
        panel.add(moduleName, "1, 1");
        panel.add(new JLabel(".js"), "2, 1");
        setMainPane(panel);
        this.pack();
        this.setMinimumSize(getSize());
        centerDialog();
        Options.loadOptions(this);
    }

    @Override
    protected void onOk() {
        String name = moduleName.getText();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!((c == '_') || ((c <= 'Z') && (c >= 'A')) || ((c <= 'z') && (c >= 'a')))) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                        ScriptPlugin.getBundle().getString("ModuleWrongName"),
                        getModuleInnerName()));
                return;
            }
        }
        if (engine.getStream(JSModulesEditor.PREFIX + getModuleInnerName()) == null) {
            super.onOk();
            this.jsModuleName = JSModulesEditor.PREFIX + getModuleInnerName();
        } else
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    ScriptPlugin.getBundle().getString("ModuleAlreadyExists"),
                    getModuleInnerName()));
        Options.saveOptions(this);
    }

    private String getModuleInnerName() {
        return moduleName.getText() + ".js";
    }

    public String getModuleName() {
        return jsModuleName;
    }
}
