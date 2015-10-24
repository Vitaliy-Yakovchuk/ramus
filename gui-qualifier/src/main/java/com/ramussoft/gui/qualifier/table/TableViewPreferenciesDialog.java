package com.ramussoft.gui.qualifier.table;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class TableViewPreferenciesDialog extends BaseDialog implements
        ItemListener {

    /**
     *
     */
    private static final long serialVersionUID = 1123356094252135168L;

    private TableViewProperties properties;

    private TableView tableView;

    private List<JCheckBox> boxes;

    private List<Attribute> list;

    private JComboBox hierarchies = new JComboBox();

    private Qualifier qualifier;

    /* hierarchy button */
    private JButton hButton = new JButton("...");

    private Hierarchy[] hs;

    private FocusListener listFocusListener = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            JComponent c = (JComponent) e.getComponent();
            c.scrollRectToVisible(new Rectangle(0, 0, c.getWidth(), c
                    .getHeight()));
        }
    };

    public TableViewPreferenciesDialog(JFrame frame, TableView tableView) {
        super(frame, true);
        this.tableView = tableView;
        this
                .setTitle(GlobalResourcesManager
                        .getString("TableViewPreferencies"));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        qualifier = tableView.getQualifier();
        list = qualifier.getAttributes();
        JPanel attrPanel = new JPanel(new BorderLayout());

        attrPanel.add(
                new JLabel(GlobalResourcesManager.getString("attributes")),
                BorderLayout.NORTH);

        JPanel attrList = new JPanel() {

            private static final long serialVersionUID = 6244779236441501596L;

            private final Insets insets = new Insets(0, 4, 0, 0);

            @Override
            public Insets getInsets() {
                return insets;
            }
        };
        attrList.setLayout(new BoxLayout(attrList, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(attrList);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        attrPanel.add(scrollPane, BorderLayout.CENTER);

        properties = tableView.getProperties();

        boxes = new ArrayList<JCheckBox>(list.size());

        for (Attribute attribute : list) {
            String name = tableView.getFramework().getSystemAttributeName(
                    attribute);
            if (name == null)
                name = attribute.getName();
            JCheckBox box = new JCheckBox(name);
            boxes.add(box);
            if (qualifier.getAttributeForName() == attribute.getId()) {
                box.setSelected(true);
                box.setEnabled(false);
            }
            box.addFocusListener(listFocusListener);
            attrList.add(box);
            box.setSelected(!properties.isPresent(attribute));
        }

        double[][] size = {
                {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5,
                        TableLayout.MINIMUM, 5}, {5, TableLayout.FILL, 5}};

        JPanel panel = new JPanel(new TableLayout(size));
        attrPanel.add(panel, BorderLayout.SOUTH);

        panel.add(
                new JLabel(GlobalResourcesManager.getString("HierarchyType")),
                "1,1");

        panel.add(hierarchies, "3,1");

        hButton.setToolTipText(GlobalResourcesManager.getString("Edit"));
        hButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                editHierarchy();
            }
        });
        panel.add(hButton, "5,1");

        hierarchies.addItem(GlobalResourcesManager.getString("BaseHierarchy"));

        hs = properties.getHierarchies();
        for (Hierarchy hierarchy : hs) {
            hierarchies.addItem(hierarchy.toString(qualifier.getAttributes(),
                    tableView.getFramework()));
        }
        hierarchies.addItem(GlobalResourcesManager
                .getString("NewHierarchyType"));

        hierarchies.setSelectedIndex(properties.getActiveHierarchy() + 1);

        hierarchies.addItemListener(this);

        setHButtonEnable();
        this.setMainPane(attrPanel);
        centerDialog();
        Options.loadOptions(this);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Options.saveOptions(TableViewPreferenciesDialog.this);
            }
        });
    }

    boolean rec = false;

    @Override
    public void itemStateChanged(ItemEvent e) {
        setHButtonEnable();
        if (rec)
            return;
        rec = true;
        try {
            if ((e.getStateChange() == ItemEvent.SELECTED)
                    && (hierarchies.getSelectedIndex() == hierarchies
                    .getItemCount() - 1)) {
                createNewHierarchy();
            }
        } finally {
            rec = false;
        }
    }

    private void setHButtonEnable() {
        hButton
                .setEnabled((hierarchies.getSelectedIndex() > 0)
                        && (hierarchies.getSelectedIndex() < hierarchies
                        .getItemCount() - 1));
    }

    private void createNewHierarchy() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                rec = true;
                try {
                    AttributeHierarchyEditorDialog dialog = new AttributeHierarchyEditorDialog(
                            TableViewPreferenciesDialog.this, tableView
                            .getFramework());
                    dialog.showModal(null, qualifier);
                    Hierarchy hierarchy = dialog.getHierarchy();
                    if (hierarchy != null) {
                        hs = Arrays.copyOf(hs, hs.length + 1);
                        hs[hs.length - 1] = hierarchy;
                        hierarchies.insertItemAt(hierarchy.toString(qualifier
                                        .getAttributes(), tableView.getFramework()),
                                hierarchies.getItemCount() - 1);
                        hierarchies
                                .setSelectedIndex(hierarchies.getItemCount() - 2);
                    } else {
                        hierarchies.setSelectedIndex(0);
                    }
                } finally {
                    rec = false;
                }
            }
        });
    }

    protected void editHierarchy() {
        rec = true;
        try {
            AttributeHierarchyEditorDialog dialog = new AttributeHierarchyEditorDialog(
                    this, tableView.getFramework());
            int index = hierarchies.getSelectedIndex() - 1;
            dialog.showModal(hs[index], qualifier);
            hs[index] = dialog.getHierarchy();
            hierarchies.removeItemAt(index + 1);
            if (hs[index] != null) {
                hierarchies.insertItemAt(hs[index].toString(qualifier
                        .getAttributes(), tableView.getFramework()), index + 1);
                hierarchies.setSelectedIndex(index + 1);
            } else {
                Hierarchy[] hrs = new Hierarchy[hs.length - 1];
                int j = 0;
                for (int i = 0; i < hs.length; i++) {
                    if (i != index) {
                        hrs[j] = hs[i];
                        j++;
                    }
                }
                hs = hrs;
                hierarchies.setSelectedIndex(0);
            }
        } finally {
            rec = false;
        }
    }

    @Override
    protected void onOk() {
        List<Long> l = new ArrayList<Long>();
        for (int i = 0; i < boxes.size(); i++) {
            JCheckBox box = boxes.get(i);
            if (!box.isSelected())
                l.add(list.get(i).getId());
        }

        long[] ms = new long[l.size()];
        for (int i = 0; i < ms.length; i++)
            ms[i] = l.get(i);

        properties.setHideAttributes(ms);

        properties.setHierarchies(hs);
        properties.setActiveHierarchy(hierarchies.getSelectedIndex() - 1);

        properties.store(tableView.engine, tableView);
        tableView.setProperties(properties);
        super.onOk();
    }

    @Override
    public void setVisible(boolean b) {
        if (!b)
            Options.saveOptions(this);
        super.setVisible(b);
    }

}
