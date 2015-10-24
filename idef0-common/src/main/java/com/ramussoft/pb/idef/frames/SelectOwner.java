package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;

public class SelectOwner extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JScrollPane jScrollPane = null;

    private JList jList = null;

    DefaultListModel listModel;

    public void setFunction(final Function function) {
        final Row[] rows = function.getOwners();
        final Row ouner = function.getOwner();
        listModel = new DefaultListModel();
        jList.setModel(listModel);
        boolean b = true;
        if (ouner != null) {
            for (final Row row : rows)
                if (ouner.equals(row)) {
                    b = false;
                    break;
                }
        }
        if (b && ouner != null) {
            listModel.addElement("<html><body><i><b>" + ouner.getName()
                    + "</b></i></body></html>");
        } else
            listModel.addElement("<html><body><i><b>"
                    + ResourceLoader.getString("owner_not_selected")
                    + "</b></i></body></html>");
        jList.setSelectedIndex(0);
        for (final Row element : rows)
            listModel.addElement(element);
        if (!b)
            jList.setSelectedValue(ouner, true);

    }

    /**
     * This is the default constructor
     */
    public SelectOwner() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.setSize(300, 200);
        this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jList
     *
     * @return javax.swing.JList
     */
    public JList getJList() {
        if (jList == null) {
            jList = new JList();
        }
        return jList;
    }

    public Row getOwner() {
        Object value = jList.getSelectedValue();
        if (value instanceof Row)
            return (Row) value;
        return null;
    }

}
