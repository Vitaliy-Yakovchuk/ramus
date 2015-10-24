package com.ramussoft.pb.idef.frames;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CreateParalelPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JCheckBox jCheckBox = null;

    private JCheckBox jCheckBox1 = null;

    /**
     * This is the default constructor
     */
    public CreateParalelPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.gridy = 1;
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridy = 0;
        this.setSize(300, 200);
        setLayout(new GridBagLayout());
        this.add(getJCheckBox(), gridBagConstraints);
        this.add(getJCheckBox1(), gridBagConstraints1);
    }

    /**
     * This method initializes jCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox() {
        if (jCheckBox == null) {
            jCheckBox = new JCheckBox();
            jCheckBox.setText("copyAllClasificators");
        }
        return jCheckBox;
    }

    /**
     * This method initializes jCheckBox1
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox1() {
        if (jCheckBox1 == null) {
            jCheckBox1 = new JCheckBox();
            jCheckBox1.setText("clearFunctionalBlock");
        }
        return jCheckBox1;
    }

    public boolean isCopyAllRows() {
        return getJCheckBox().isSelected();
    }

    public boolean isClearFunctionalBlock() {
        return getJCheckBox1().isSelected();
    }
}
