package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ChangeFunctionParentPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JScrollPane jScrollPane = null;

    //private ClasificatorTable clasificatorTable = null;

    /**
     * This is the default constructor
     */
    public ChangeFunctionParentPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(461, 315);
        setLayout(new BorderLayout());
        this.add(getJScrollPane(), BorderLayout.CENTER);
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            //jScrollPane.setViewportView(getClasificatorTable());
        }
        return jScrollPane;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
