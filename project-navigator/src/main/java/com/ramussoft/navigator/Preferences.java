package com.ramussoft.navigator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import com.dsoft.pb.idef.ResourceLoader;

public class Preferences extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -6469377246772988167L;

    private ProjectNavigator navigator;

    private class Item implements Comparable<Item> {
        String modelName = "";

        String fileName = "";

        @Override
        public int compareTo(Item o) {
            Collator collator = Collator.getInstance();
            return collator.compare(this.modelName, o.modelName);
        }

    }

    ;

    private ArrayList<Item> items = new ArrayList<Item>();

    private JTextField port;

    private AbstractTableModel model = new AbstractTableModel() {

        /**
         *
         */
        private static final long serialVersionUID = 8170873790733965726L;

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Item item = items.get(rowIndex);
            if (columnIndex == 0)
                return item.modelName;
            return item.fileName;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Item item = items.get(rowIndex);
            if (columnIndex == 0)
                item.modelName = aValue.toString();
            else
                item.fileName = aValue.toString();
        }

        ;

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            if (column == 0)
                return ProjectNavigator.getString("Column.ModelName");
            return ProjectNavigator.getString("Column.FileName");
        }

        ;

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        ;
    };

    private JTable table;

    private FileFilter fileFilter = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isFile()) {
                if (f.getName().toLowerCase().endsWith(getRSF()))
                    return true;
                else
                    return false;
            }
            return true;
        }

        @Override
        public String getDescription() {
            return "*" + getRSF();
        }

    };

    private AbstractAction addAction;

    private AbstractAction removeAction;

    public Preferences(ProjectNavigator navigator) {
        super(ProjectNavigator.getString("Action.Preferences"));
        this.navigator = navigator;
        port = new JTextField("8080");
        port
                .setPreferredSize(new Dimension(120,
                        port.getPreferredSize().height));
        loadData();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());
        this.add(panel);
        JPanel oc = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton(ResourceLoader.getString("ok"));
        JButton cancel = new JButton(ResourceLoader.getString("cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences.this.setVisible(false);
            }
        });
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        oc.add(ok);
        oc.add(cancel);
        panel.add(oc, BorderLayout.SOUTH);
        JScrollPane pane = new JScrollPane();
        table = new JTable(model) {
            /**
             *
             */
            private static final long serialVersionUID = 4654413213213690885L;

            @Override
            public void changeSelection(int rowIndex, int columnIndex,
                                        boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                removeAction.setEnabled(rowIndex >= 0);
            }
        };
        pane.setViewportView(table);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel(ProjectNavigator.getString("HttpPort")));
        top.add(port);
        panel.add(top, BorderLayout.NORTH);
        panel.add(pane, BorderLayout.CENTER);
        panel.add(createControlPanel(), BorderLayout.EAST);
        pack();
        setSize(500, 300);
        setMaximumSize(getSize());
        setLocationRelativeTo(null);
    }

    protected String getRSF() {
        return ".rsf";
    }

    private Component createControlPanel() {
        JPanel control = new JPanel(new GridLayout(2, 1, 5, 5));
        addAction = new AbstractAction(ResourceLoader.getString("add")) {

            /**
             *
             */
            private static final long serialVersionUID = -9180912818685411263L;

            @Override
            public void actionPerformed(ActionEvent e) {
                addItem();
            }
        };
        control.add(new JButton(addAction));
        removeAction = new AbstractAction(ResourceLoader.getString("remove")) {

            /**
             *
             */
            private static final long serialVersionUID = -9180912818685411263L;

            @Override
            public void actionPerformed(ActionEvent e) {
                removeItem();
            }
        };
        removeAction.setEnabled(false);
        control.add(new JButton(removeAction));

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(control);
        return panel;
    }

    private void loadData() {
        File file = new File(navigator.getPreferencesFileName());
        if ((file.exists()) && (file.canRead())) {
            Properties ps = new Properties();
            try {
                FileInputStream inStream = new FileInputStream(file);
                ps.load(inStream);

                int modelCount = Integer.parseInt(ps.getProperty("ModelCount"));

                for (int i = 0; i < modelCount; i++) {
                    Item item = new Item();
                    items.add(item);
                    item.fileName = ps.getProperty("FileName_" + i);
                    item.modelName = ps.getProperty("ModelName_" + i);
                }

                Collections.sort(items);

                inStream.close();
                port.setText(ps.getProperty("WebPort"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addItem() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(fileFilter);
        int r = chooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            Item item = new Item();
            item.fileName = f.getAbsolutePath();
            items.add(item);
            model.fireTableRowsInserted(items.size() - 1, items.size() - 1);
        }
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        row = table.convertRowIndexToModel(row);
        items.remove(row);
        model.fireTableRowsDeleted(row, row);
    }

    protected void ok() {
        saveData();
        navigator.stop();
        navigator.start();
        navigator.openBrowser();
        setVisible(false);
    }

    private void saveData() {
        Properties ps = new Properties();
        ps.setProperty("WebPort", port.getText());
        ps.setProperty("ModelCount", Integer.toString(items.size()));
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            ps.setProperty("FileName_" + i, item.fileName);
            ps.setProperty("ModelName_" + i, item.modelName);
        }
        File file = new File(navigator.getPreferencesFileName());
        try {
            FileOutputStream out = new FileOutputStream(file);
            ps.store(out, "Ramus Web Navigator preferences");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
