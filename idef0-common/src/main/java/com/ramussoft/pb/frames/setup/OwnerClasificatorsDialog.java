package com.ramussoft.pb.frames.setup;

import javax.swing.JFrame;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.frames.BaseDialog;

public class OwnerClasificatorsDialog extends BaseDialog {
    private OwnerClasificators clasificators;

    private DataPlugin dataPlugin;

    public OwnerClasificatorsDialog(DataPlugin dataPlugin) {
        super();
        this.dataPlugin = dataPlugin;
        setModal(true);
        init();
    }

    public OwnerClasificatorsDialog(final JFrame frame, DataPlugin dataPlugin) {
        super(frame, true);
        this.dataPlugin = dataPlugin;
        init();
    }

    private void init() {
        clasificators = new OwnerClasificators(dataPlugin);
        setTitle("Owners.Clasificators");
        setMainPane(clasificators);
        ResourceLoader.setJComponentsText(this);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        Options.loadOptions("OwnerClasificatorDialog", this);
    }

    public void showModal() {
        clasificators.updateOuners();
        setVisible(true);
        Options.saveOptions("OwnerClasificatorDialog", this);
    }

    @Override
    protected void onOk() {
        clasificators.apply();
        super.onOk();
    }
}
