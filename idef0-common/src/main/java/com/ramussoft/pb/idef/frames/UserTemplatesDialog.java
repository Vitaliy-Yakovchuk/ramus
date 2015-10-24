package com.ramussoft.pb.idef.frames;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.dmaster.Template;
import com.ramussoft.pb.dmaster.TemplateFactory;
import com.ramussoft.pb.dmaster.UserTemplate;

public class UserTemplatesDialog extends JDialog {

    private Vector<Template> data = new Vector<Template>(0);

    private final TemplateSample sample = new TemplateSample();

    private UserTemplate active = null;

    private final AbstractTableModel model = new AbstractTableModel() {

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return data.size();
        }

        public Object getValueAt(int row, int column) {
            return data.get(row).toString();
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return true;
        }

        @Override
        public String getColumnName(int arg0) {
            return ResourceLoader.getString("Template");
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            UserTemplate ut = (UserTemplate) data.get(row);
            if (ut.getName().equals(value))
                return;
            if (TemplateFactory.isPresent(value.toString())) {
                JOptionPane.showMessageDialog(UserTemplatesDialog.this,
                        MessageFormat.format(ResourceLoader
                                        .getString("Template_Present"),
                                new Object[]{value}));
            } else {
                ut.setName(value.toString());
                TemplateFactory.updateUserTemplate(ut);
            }
        }

    };

    private final AbstractTableModel model2 = new AbstractTableModel() {

        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return ResourceLoader.getString("Function.Name");
        }

        public int getRowCount() {
            return active == null ? 0 : active.getFunctionCount();
        }

        public Object getValueAt(int row, int column) {
            return active.getFunctionName(row);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            active.setFunctionName(row, value.toString());
            sample.repaint();
            TemplateFactory.updateUserTemplate(active);
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return true;
        }
    };

    public UserTemplatesDialog(final JFrame frame) {
        super(frame);
        init();
    }

    private void init() {
        setTitle(ResourceLoader.getString("User.Templates"));
        final JSplitPane pane = new JSplitPane();
        pane.setDividerLocation(175);
        final JSplitPane r = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        r.setDividerLocation(300);
        final JScrollPane sPane = new JScrollPane();
        final JTable table = new JTable(model) {
            @Override
            public void changeSelection(int row, int column, boolean arg2,
                                        boolean arg3) {
                if (row < 0) {
                    active = null;
                    sample.setActive(null);
                } else {
                    int mRow = convertRowIndexToModel(row);
                    active = (UserTemplate) data.get(mRow);
                    sample.setActive(active);
                    sample.setDiagramType(active.getDecompositionType());
                }
                model2.fireTableDataChanged();
                sample.repaint();
                super.changeSelection(row, column, arg2, arg3);
            }
        };
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                "removeActives");
        table.getActionMap().put("removeActives", new AbstractAction() {

            public void actionPerformed(final ActionEvent arg0) {
                if (JOptionPane
                        .showConfirmDialog(
                                UserTemplatesDialog.this,
                                ResourceLoader
                                        .getString("are_you_shour_want_remove_active_element"),
                                ResourceLoader.getString("worninig"),
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    final int[] sels = table.getSelectedRows();
                    for (final int i : sels) {
                        final int row = table.convertRowIndexToModel(i);
                        TemplateFactory.removeUserTemplate(data.get(row)
                                .toString());
                    }
                    reloadTemplates();
                    table.changeSelection(-1, 0, false, false);
                }

            }

        });
        sPane.setViewportView(table);
        pane.setLeftComponent(sPane);
        r.setLeftComponent(sample);
        final JScrollPane pane2 = new JScrollPane();
        final JTable table2 = new JTable(model2);
        pane2.setViewportView(table2);
        r.setRightComponent(pane2);
        pane.setRightComponent(r);
        setContentPane(pane);
        pack();
        setMinimumSize(this.getSize());
        setLocationRelativeTo(null);
        Options.loadOptions("UserTemplatesEditor", this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent arg0) {
                Options.saveOptions("UserTemplatesEditor",
                        UserTemplatesDialog.this);
            }
        });
    }

    @Override
    public void setVisible(final boolean b) {
        if (b && !isVisible()) {
            reloadTemplates();
        }
        super.setVisible(b);
    }

    private void reloadTemplates() {
        data = TemplateFactory.getUserTemplates(new Vector<Template>(), -2);
        model.fireTableDataChanged();
    }
}
