package com.ramussoft.pb.idef.frames;

import javax.swing.JFrame;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.frames.BaseDialog;

public class CreateParalelDialog extends BaseDialog {

    private CreateParalelPanel createParalelModel = null; // @jve:decl-index=0:visual-constraint="233,144"

    private boolean res;

    /**
     * This method initializes createParalelModel
     *
     * @return com.dsoft.clasificators.idef.frames.CreateParalelModel
     */
    public CreateParalelPanel getCreateParalelModel() {
        if (createParalelModel == null) {
            createParalelModel = new CreateParalelPanel();
        }
        return createParalelModel;
    }

    public CreateParalelDialog(final JFrame frame) {
        super(frame);
        setTitle("createParalel");
        setMainPane(getCreateParalelModel());
        ResourceLoader.setJComponentsText(this);
        pack();
        centerDialog();
        Options.loadOptions("createParalel", this);
    }

    public boolean showModal() {
        res = false;
        setVisible(true);
        Options.saveOptions("createParalel", this);
        return res;
    }

    @Override
    protected void onOk() {
        res = true;
        super.onOk();
    }
}
