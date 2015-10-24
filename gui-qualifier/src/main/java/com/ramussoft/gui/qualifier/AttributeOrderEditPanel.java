package com.ramussoft.gui.qualifier;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.Attribute;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class AttributeOrderEditPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 6093956847036812876L;

    private List<Attribute> attributes;

    private Hashtable<Attribute, String> groups = new Hashtable<Attribute, String>();

    private boolean showGroups;

    private class AttributesTableModel extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = -3183733467056921857L;

        @Override
        public int getColumnCount() {
            if (showGroups)
                return 2;
            return 1;
        }

        @Override
        public int getRowCount() {
            return attributes.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Attribute attribute = attributes.get(rowIndex);
            if (columnIndex > 0)
                return groups.get(attribute);
            return attribute;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0)
                return GlobalResourcesManager
                        .getString("OtherElement.Attribute");
            return GlobalResourcesManager.getString("Group");
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Attribute attribute = attributes.get(rowIndex);
            if (aValue == null)
                groups.remove(attribute);
            else
                groups.put(attribute, aValue.toString());
        }
    }

    ;

    private AttributesTableModel model = new AttributesTableModel();

    private JTable list;

    private Action up = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = 6580755484618181297L;

        {
            putValue(ACTION_COMMAND_KEY, "MoveAttributeUp");
            putValue(SHORT_DESCRIPTION, GlobalResourcesManager
                    .getString("MoveAttributeUp"));
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-up.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            up();
        }
    };

    private Action down = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = -6176525277715126232L;

        {
            putValue(ACTION_COMMAND_KEY, "MoveAttributeDown");
            putValue(SHORT_DESCRIPTION, GlobalResourcesManager
                    .getString("MoveAttributeDown"));
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-down.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            down();
        }
    };

    public AttributeOrderEditPanel(List<Attribute> attributes) {
        this(attributes, false);
    }

    public AttributeOrderEditPanel(List<Attribute> attributes,
                                   boolean showGroups) {
        super(new BorderLayout());
        this.attributes = attributes;
        this.showGroups = showGroups;
        list = new JTable(model) {
            /**
             *
             */
            private static final long serialVersionUID = 8519192346906407254L;

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                JComboBox box = new JComboBox();
                box.setEditable(true);
                List<String> list = new ArrayList<String>();
                for (String s : groups.values()) {
                    if ((s == null) || (s.length() == 0))
                        continue;
                    if (list.indexOf(s) >= 0)
                        continue;
                    list.add(s);
                    box.addItem(s);
                }

                return new DefaultCellEditor(box);
            }
        };
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(list);
        this.add(pane, BorderLayout.CENTER);
        this.add(createUpDown(), BorderLayout.EAST);
    }

    protected void up() {
        int[] is = list.getSelectedRows();
        final ArrayList<Integer> nSel = new ArrayList<Integer>();
        for (int i = 0; i < is.length; i++) {
            final int index = is[i];
            if (index > 0) {
                final Attribute obj = attributes.get(index);
                attributes.remove(index);
                final int j = index - 1;
                attributes.add(j, obj);
                nSel.add(j);
            } else
                is[i] = -1;
        }
        is = new int[nSel.size()];
        for (int i = 0; i < is.length; i++)
            is[i] = nSel.get(i);
        list.getSelectionModel().clearSelection();
        for (int i : is)
            list.getSelectionModel().addSelectionInterval(i, i);
    }

    protected void down() {
        int[] is = list.getSelectedRows();
        final ArrayList<Integer> nSel = new ArrayList<Integer>();
        for (int i = 0; i < is.length; i++) {
            final int index = is[i];
            if (index + 1 < attributes.size()) {
                final Attribute obj = attributes.get(index);
                attributes.remove(index);
                final int j = index + 1;
                attributes.add(j, obj);
                nSel.add(j);
            } else
                is[i] = -1;
        }
        is = new int[nSel.size()];
        for (int i = 0; i < is.length; i++)
            is[i] = nSel.get(i);
        list.getSelectionModel().clearSelection();
        for (int i : is)
            list.getSelectionModel().addSelectionInterval(i, i);
    }

    private JComponent createUpDown() {

        TableLayout layout = new TableLayout(new double[]{TableLayout.FILL},
                new double[]{TableLayout.FILL, 5, TableLayout.FILL});

        JPanel panel = new JPanel(layout);

        JButton upButton = new JButton(up);
        upButton.setFocusable(false);
        JButton downButton = new JButton(down);
        downButton.setFocusable(false);

        panel.add(upButton, "0,0");
        panel.add(downButton, "0,2");

        JPanel p = new JPanel(new FlowLayout());
        p.add(panel);
        return p;
    }

    public void refresh() {
        model = new AttributesTableModel();
        list.setModel(model);
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
        refresh();
    }

    public String[] getAttributeGroups() {
        String[] result = new String[attributes.size()];
        for (int i = 0; i < result.length; i++) {
            String s = groups.get(attributes.get(i));
            if ((s != null) && (s.length() == 0))
                s = null;
            result[i] = s;
        }
        return result;
    }

    public void setGroups(Hashtable<Attribute, String> groups) {
        this.groups = groups;
    }
}
