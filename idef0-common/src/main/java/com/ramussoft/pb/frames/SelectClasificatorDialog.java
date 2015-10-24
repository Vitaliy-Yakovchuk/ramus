package com.ramussoft.pb.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.Row;

public class SelectClasificatorDialog extends BaseDialog {

    private final JPanel base = new JPanel();

    private final JScrollPane scrollPane = new JScrollPane();

    private final JList list = new JList();

    private Row result;

    private Vector<Row> rows;

    private Row active = null;

    public SelectClasificatorDialog() {
        super();
    }

    public SelectClasificatorDialog(final JDialog dialog) {
        super(dialog);
        initialize();
    }

    public SelectClasificatorDialog(final JFrame frame) {
        super(frame);
        initialize();
    }

    public void initialize() {
        createPanel();
        setMinSizePack();
        centerDialog();
        Options.loadOptions("selectCLDialog", this);
    }

    private void createPanel() {
        scrollPane.setViewportView(list);
        setTitle("select_clasificator");
        base.setLayout(new BorderLayout());
        base.add(scrollPane, BorderLayout.CENTER);
        base.setPreferredSize(new Dimension(300, 150));
        list.setDragEnabled(false);
        list.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(final ListSelectionEvent e) {
                changeActive();
            }

        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseEvent.BUTTON1) {
                    onOk();
                }
            }
        });
        setMainPane(base);
        ResourceLoader.setJComponentsText(this);
    }

    protected void changeActive() {
        getJButton().setEnabled(list.getSelectedIndex() >= 0);
    }

    public Row showModal() {
        result = null;
        rows = Main.dataPlugin.getRecChilds(null, false);
        list.setModel(new AbstractListModel() {

            public Object getElementAt(final int index) {
                final Row row = rows.get(index);
                return row.getKod() + " " + row.getName();
            }

            public int getSize() {
                if (rows == null)
                    return 0;
                return rows.size();
            }

        });
        if (active != null) {
            final int index = rows.indexOf(active);
            list.setSelectedIndex(index);
        }
        setVisible(true);
        rows = null;
        Options.saveOptions("selectCLDialog", this);
        return result;
    }

    @Override
    protected void onOk() {
        result = rows.get(list.getSelectedIndex());
        super.onOk();
    }

    public void setActive(final Row row) {
        active = row;
    }
}
