package com.ramussoft.demo.sample1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.event.JournalAdatper;
import com.ramussoft.common.journal.event.JournalEvent;
import com.ramussoft.common.journal.event.JournalListener;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.Database;

public class MainFrame extends JFrame {

    private static final List<MainFrame> frames = new ArrayList<MainFrame>();

    private final Attribute[] ATTRIBUTES;

    private Database database;

    private Engine engine;

    private AccessRules rules;

    private List<Row> data = new ArrayList<Row>();

    private Model tableModel;

    private JTable table;

    public MainFrame(Database database) {
        frames.add(this);
        this.database = database;
        this.engine = database.getEngine(null);
        this.rules = database.getAccessRules(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frames.remove(MainFrame.this);
                MainFrame.this.removeListeners();
                if (frames.size() == 0)
                    try {
                        ((FileIEngineImpl) engine.getDeligate()).close();
                        System.exit(0);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
            }
        });

        ATTRIBUTES = new Attribute[]{
                engine.getSystemAttribute(Application.TEXT_ATTRIBUTE1),
                engine.getSystemAttribute(Application.TEXT_ATTRIBUTE2),
                engine.getSystemAttribute(Application.DOUBLE_ATTRIBUTE1)};

        loadData();

        engine.addElementListener(engine
                .getSystemQualifier(Application.QUALIFIER1), elementListener);
        engine.addElementAttributeListener(engine
                        .getSystemQualifier(Application.QUALIFIER1),
                elementAttributeListener);

        setUndoRedoEnable();

        ((Journaled) engine).addJournalListener(journalListener);

        this.setContentPane(createContentPane());
        this.setTitle("My simple application with undo/redo support.");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
    }

    private void loadData() {
        data.clear();
        ArrayList<Attribute> attrs = new ArrayList<Attribute>();
        for (Attribute attribute : ATTRIBUTES)
            attrs.add(attribute);
        // We can use engine.getAttribute(Element, Attribute) method to cat all
        // attributes, but this method works match faster for a set of elements.
        Hashtable<Element, Object[]> hash = engine.getElements(engine
                .getSystemQualifier(Application.QUALIFIER1), attrs);
        for (Entry<Element, Object[]> entry : hash.entrySet()) {
            data.add(new Row(entry.getKey(), entry.getValue()));
        }
        Collections.sort(data, new Comparator<Row>() {

            @Override
            public int compare(Row o1, Row o2) {
                if (o1.element.getId() < o2.element.getId())
                    return -1;
                if (o2.element.getId() < o1.element.getId())
                    return 1;
                return 0;
            }
        });
    }

    private Container createContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createToolBar(), BorderLayout.NORTH);
        panel.add(createTable(), BorderLayout.CENTER);
        return panel;
    }

    private Component createToolBar() {
        JToolBar bar = new JToolBar();
        bar.add(undo);
        bar.add(redo);
        bar.addSeparator();
        bar.add(addRow);
        bar.add(removeRows);
        bar.addSeparator();
        bar.add(openNewWindow);
        bar.addSeparator();
        bar.add(saveAs);
        return bar;
    }

    private Component createTable() {
        JScrollPane pane = new JScrollPane();
        tableModel = new Model();
        table = new JTable(tableModel);
        pane.setViewportView(table);
        return pane;
    }

    private int indexOfElement(Element element) {
        int index = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).element.equals(element)) {
                index = i;
                break;
            }
        }
        return index;
    }

    ;

    protected void removeListeners() {
        engine.removeElementListener(engine
                .getSystemQualifier(Application.QUALIFIER1), elementListener);
        engine.removeElementAttributeListener(engine
                        .getSystemQualifier(Application.QUALIFIER1),
                elementAttributeListener);
        ((Journaled) engine).removeJournalListener(journalListener);
    }

    private void setUndoRedoEnable() {
        Journaled journaled = (Journaled) engine;
        undo.setEnabled(journaled.canUndo());
        redo.setEnabled(journaled.canRedo());
    }

    /**
     *
     */
    private static final long serialVersionUID = 2181463043651401001L;

    private class Row {// We can use java.util.Map.Entry instead this class.
        Element element;

        Object[] objects;

        public Row(Element element, Object[] data) {
            this.element = element;
            this.objects = data;
        }
    }

    private class Model extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = -1842715135339752741L;

        @Override
        public int getColumnCount() {
            return ATTRIBUTES.length;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row row = data.get(rowIndex);
            return row.objects[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == ATTRIBUTES.length - 1)
                return Double.class;
            return String.class;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ((Journaled) engine).startUserTransaction();
            engine.setAttribute(data.get(rowIndex).element,
                    ATTRIBUTES[columnIndex], aValue);
            ((Journaled) engine).commitUserTransaction();
            // these is no need to update data.get(rowIndex).objects, as they we
            // be updated with the listeners.
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Always check ability to edit with Access Rules class. In this
            // example use can always edit everything. But if there are any
            // "hard" connections or no rights to edit object for network
            // version of application, AccessRules will show ability to edit any
            // object.
            Element element = data.get(rowIndex).element;
            Attribute attribute = ATTRIBUTES[columnIndex];
            return rules.canUpdateElement(element.getId(), attribute.getId());
        }

        @Override
        public String getColumnName(int column) {
            return ATTRIBUTES[column].getName();
        }
    }

    ;

    private AbstractAction addRow = new AbstractAction("Add Row") {

        /**
         *
         */
        private static final long serialVersionUID = 2034525709297733624L;

        @Override
        public void actionPerformed(ActionEvent e) {
            ((Journaled) engine).startUserTransaction();
            engine.createElement(engine.getSystemQualifier(
                    Application.QUALIFIER1).getId());
            ((Journaled) engine).commitUserTransaction();
        }
    };

    private AbstractAction removeRows = new AbstractAction("Remove Rows") {

        /**
         *
         */
        private static final long serialVersionUID = -7964886712878674663L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0)
                return;
            List<Element> elements = new ArrayList<Element>();
            for (int row : selectedRows) {
                int modelRow = table.convertRowIndexToModel(row);
                elements.add(data.get(modelRow).element);
            }
            ((Journaled) engine).startUserTransaction();
            for (Element element : elements) {
                engine.deleteElement(element.getId());
            }
            ((Journaled) engine).commitUserTransaction();
        }
    };

    private AbstractAction openNewWindow = new AbstractAction("Open new window") {

        /**
         *
         */
        private static final long serialVersionUID = -961213701948966079L;

        @Override
        public void actionPerformed(ActionEvent e) {
            new MainFrame(database).setVisible(true);
        }
    };

    private AbstractAction undo = new AbstractAction("Undo") {

        /**
         *
         */
        private static final long serialVersionUID = 2034525709297733624L;

        @Override
        public void actionPerformed(ActionEvent e) {
            ((Journaled) engine).undoUserTransaction();
        }
    };

    private AbstractAction redo = new AbstractAction("Redo") {

        /**
         *
         */
        private static final long serialVersionUID = -7964886712878674663L;

        @Override
        public void actionPerformed(ActionEvent e) {
            ((Journaled) engine).redoUserTransaction();
        }
    };

    private AbstractAction saveAs = new AbstractAction("Save As") {

        /**
         *
         */
        private static final long serialVersionUID = -1201166502299552772L;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser() {
                /**
                 *
                 */
                private static final long serialVersionUID = -3245345656456454645L;

                public void approveSelection() {
                    if (getSelectedFile().exists()) {
                        if (JOptionPane
                                .showConfirmDialog(
                                        MainFrame.this,
                                        MessageFormat.format(
                                                "File {0} exists, replace?",
                                                getSelectedFile().getName()),
                                        UIManager
                                                .getString("OptionPane.messageDialogTitle"),
                                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;
                    }
                    super.approveSelection();
                }
            };
            chooser.setFileFilter(new MyFileFilter());
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".app"))
                        file = new File(file.getParentFile(), file.getName()
                                + ".app");
                    ((FileIEngineImpl) engine.getDeligate()).saveToFile(file);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, e1
                            .getLocalizedMessage());
                }
            }

        }
    };

    private ElementListener elementListener = new ElementAdapter() {
        public void elementCreated(com.ramussoft.common.event.ElementEvent event) {
            Object[] objects = new Object[ATTRIBUTES.length];

            // If user undone element deletion we should load all attributes of
            // an element.
            if (event.isJournaled())
                for (int i = 0; i < ATTRIBUTES.length; i++)
                    objects[i] = engine.getAttribute(event.getNewElement(),
                            ATTRIBUTES[i]);

            data.add(new Row(event.getNewElement(), objects));
            tableModel.fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        ;

        public void elementDeleted(com.ramussoft.common.event.ElementEvent event) {
            int index = indexOfElement(event.getOldElement());
            // In this example this check is unneeded, but you should always
            // make such checks in real application.
            if (index >= 0) {
                data.remove(index);
                tableModel.fireTableRowsDeleted(index, index);
            }
        }

    };

    private ElementAttributeListener elementAttributeListener = new ElementAttributeListener() {

        @Override
        public void attributeChanged(AttributeEvent event) {
            int index = indexOfElement(event.getElement());
            int attributeIndex = -1;
            for (int i = 0; i < ATTRIBUTES.length; i++) {
                if (ATTRIBUTES[i].equals(event.getAttribute())) {
                    attributeIndex = i;
                    break;
                }
            }
            // In this example this check is unneeded, but you should always
            // make such checks in real application.
            if ((index >= 0) && (attributeIndex >= 0)) {
                Row row = data.get(index);
                row.objects[attributeIndex] = event.getNewValue();
                tableModel.fireTableCellUpdated(index, attributeIndex);
            }
        }
    };

    private JournalListener journalListener = new JournalAdatper() {

        @Override
        public void afterUndo(JournalEvent event) {
            setUndoRedoEnable();
        }

        @Override
        public void afterStore(JournalEvent event) {
            setUndoRedoEnable();
        }

        @Override
        public void afterRedo(JournalEvent event) {
            setUndoRedoEnable();
        }
    };

}
