package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.print.PageSelector;

public class IDEF0ChackedPanel extends JPanel implements PageSelector {

    private static final long serialVersionUID = 1L;

    private JScrollPane jScrollPane = null;

    private JPanel jPanel2 = null;

    private JPanel jPanel3 = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    private JTable jTable = null;

    private JPanel jPanel4 = null;

    private Function[] functions = new Function[0];

    private boolean[] checkeds = new boolean[0];

    private final FunctionTableModel tableModel = new FunctionTableModel();

    private class FunctionTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return functions.length;
        }

        public Object getValueAt(final int rowIndex, final int columnIndex) {
            if (columnIndex == 0) {
                final Function function = functions[rowIndex];
                return MovingFunction
                        .getIDEF0Kod((com.ramussoft.database.common.Row) function)
                        + " " + function.getName();
            }
            return new Boolean(checkeds[rowIndex]);
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex,
                               final int columnIndex) {
            checkeds[rowIndex] = ((Boolean) aValue).booleanValue();
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            if (columnIndex == 0)
                return String.class;
            return Boolean.class;
        }

        @Override
        public String getColumnName(final int column) {
            if (column == 0)
                return ResourceLoader.getString("works");
            return ResourceLoader.getString("print");
        }
    }

    ;

    private Function[] convert(final Vector<Function> res) {
        final Function[] functions = new Function[res.size()];
        for (int i = 0; i < functions.length; i++)
            functions[i] = res.get(i);
        return functions;
    }

    /**
     * Задає набір функціональних блоків, що можна відмітити.
     *
     * @param functions Набір функціональних блоків, які необхідно відмітити.
     */

    public void setFunctions(final Function[] functions) {
        this.functions = functions;
        checkeds = new boolean[functions.length];
        for (int i = 0; i < checkeds.length; i++)
            checkeds[i] = true;
        tableModel.fireTableDataChanged();
    }

    /**
     * Метод, який виводить в таблицю набір усіх функцій (включно з базовою
     * фунцією "Роботи").
     */

    public void setFunctionAll(DataPlugin dataPlugin) {
        final Function base = dataPlugin.getBaseFunction();
        final Vector<Row> v = dataPlugin.getRecChilds(base, true);
        final Function[] functions = new Function[v.size() + 1];
        functions[0] = base;
        for (int i = 0; i < v.size(); i++)
            functions[i + 1] = (Function) v.get(i);
        setFunctions(functions);
    }

    /**
     * Метод, який виводить в талицю набір тільки тих функцій, що мають дитячі
     * елементи (включно з базовою фунцією "Роботи", якщо вона має дочірні
     * елементи).
     */

    public void setFunctionParents(DataPlugin dataPlugin) {
        final Function base = dataPlugin.getBaseFunction();
        final Vector<Function> res = new Vector<Function>();
        if (base.isHaveChilds())
            res.add(base);
        final Vector<Row> v = dataPlugin.getRecChilds(base, true);
        for (int i = 0; i < v.size(); i++)
            if (((Function) v.get(i)).isHaveRealChilds())
                res.add((Function) v.get(i));

        setFunctions(convert(res));
    }

    /**
     * Метод повертає набір функціональних блоків, що були відмічені.
     *
     * @return Набір функціональних блоків, що вибрав користувач.
     */

    public Function[] getChecked() {
        final Vector<Function> res = new Vector<Function>();
        for (int i = 0; i < checkeds.length; i++)
            if (checkeds[i])
                res.add(functions[i]);
        return convert(res);
    }

    /**
     * This is the default constructor
     */
    public IDEF0ChackedPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(582, 367);
        setLayout(new BorderLayout());
        this.add(getJPanel2(), BorderLayout.CENTER);
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setLayout(new BorderLayout());
            jPanel2.add(getJScrollPane(), BorderLayout.CENTER);
            jPanel2.add(getJPanel3(), BorderLayout.NORTH);
        }
        return jPanel2;
    }

    /**
     * This method initializes jPanel3
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            final FlowLayout flowLayout = new FlowLayout();
            flowLayout.setVgap(0);
            flowLayout.setHgap(0);
            flowLayout.setAlignment(FlowLayout.LEFT);
            jPanel3 = new JPanel();
            jPanel3.setLayout(flowLayout);
            jPanel3.add(getJPanel4(), null);
        }
        return jPanel3;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setMnemonic(KeyEvent.VK_UNDEFINED);
            jButton.setToolTipText("check_all");
            jButton.setIcon(new ImageIcon(getClass().getResource(
                    "/images/check_all.gif")));
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    checkAll();
                }
            });
            jButton.setFocusable(false);
        }
        return jButton;
    }

    private void checkAll() {
        for (int i = 0; i < checkeds.length; i++)
            checkeds[i] = true;
        if (checkeds.length > 0)
            tableModel.fireTableRowsUpdated(0, checkeds.length - 1);
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setIcon(new ImageIcon(getClass().getResource(
                    "/images/uncheck_all.gif")));
            jButton1.setToolTipText("uncheck_all");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    uncheckAll();
                }
            });
            jButton1.setFocusable(false);
        }
        return jButton1;
    }

    private void uncheckAll() {
        for (int i = 0; i < checkeds.length; i++)
            checkeds[i] = false;
        if (checkeds.length > 0)
            tableModel.fireTableRowsUpdated(0, checkeds.length - 1);
    }

    /**
     * This method initializes jTable
     *
     * @return javax.swing.JTable
     */
    private JTable getJTable() {
        if (jTable == null) {
            jTable = new JTable();
            jTable.setRowSelectionAllowed(true);
            jTable.setModel(tableModel);
        }
        return jTable;
    }

    /**
     * This method initializes jPanel4
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            final GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            gridLayout.setHgap(5);
            gridLayout.setColumns(2);
            jPanel4 = new JPanel();
            jPanel4.setLayout(gridLayout);
            jPanel4.add(getJButton(), null);
            jPanel4.add(getJButton1(), null);
        }
        return jPanel4;
    }

    public int[] getSelected() {
        int len = 0;
        for (final boolean element : checkeds)
            if (element)
                len++;
        final int[] res = new int[len];
        len = 0;
        for (int i = 0; i < checkeds.length; i++)
            if (checkeds[i]) {
                res[len] = i;
                len++;
            }
        return res;
    }

    public Function[] getSelectedFunctions() {
        int len = 0;
        for (final boolean element : checkeds)
            if (element)
                len++;
        final Function[] res = new Function[len];
        len = 0;
        for (int i = 0; i < checkeds.length; i++)
            if (checkeds[i]) {
                res[len] = functions[i];
                len++;
            }
        return res;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
