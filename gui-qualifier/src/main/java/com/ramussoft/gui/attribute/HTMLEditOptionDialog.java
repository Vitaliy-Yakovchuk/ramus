package com.ramussoft.gui.attribute;

import javax.swing.JFrame;

import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

/**
 * Діалогове вікно для налаштування зовнішнього редактора.
 *
 * @author Яковчук В. В.
 */

public class HTMLEditOptionDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -4757863676382443227L;
    private final HTMLEditOptionPanel panel = new HTMLEditOptionPanel();

    public HTMLEditOptionDialog(final JFrame frame) {
        super(frame);
        setMainPane(panel);
        setTitle(GlobalResourcesManager.getString("HTMLEditor.Options"));
        pack();
        setLocationRelativeTo(frame);
    }

    public void showModal() {
        panel.modal();
        Options.loadOptions("htmlEditorOption", this);
        setVisible(true);
        Options.saveOptions("htmlEditorOption", this);
    }

    @Override
    protected void onOk() {
        panel.ok();
        super.onOk();
    }

}
