package com.ramussoft.pb.frames.setup;

import java.awt.BorderLayout;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.pb.DataPlugin;

public class OwnerClasificators extends JPanel {

    private final Vector<Long> owners = new Vector<Long>(0);

    private Qualifier[] clasificators = new Qualifier[]{};

    private DataPlugin dataPlugin;

    private final AbstractTableModel model = new AbstractTableModel() {

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return clasificators.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return clasificators[rowIndex].getName();
            return owners.indexOf(clasificators[rowIndex].getId()) >= 0;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            assert value instanceof Boolean;
            Qualifier cl = clasificators[rowIndex];
            if ((Boolean) value) {
                if (owners.indexOf(cl.getId()) < 0)
                    owners.add(cl.getId());
            } else {
                owners.remove(cl.getId());
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return ResourceLoader.getString("clasificator");
            }
            return ResourceLoader.getString("owner");
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return super.getColumnClass(columnIndex);
            return Boolean.class;
        }
    };

    public OwnerClasificators(DataPlugin dataPlugin) {
        this.dataPlugin = dataPlugin;
        setLayout(new BorderLayout());
        final JScrollPane pane = new JScrollPane();
        pane.setViewportView(new JTable(model));
        this.add(pane, BorderLayout.CENTER);
    }

    public void updateOuners() {
        List<Qualifier> cls = dataPlugin.getEngine().getQualifiers();
        for (int i = cls.size() - 1; i >= 0; i--) {
            if (IDEF0Plugin.isFunction(cls.get(i)))
                cls.remove(i);
        }
        Collections.sort(cls, new Comparator<Qualifier>() {

            Collator collator = Collator.getInstance();

            @Override
            public int compare(Qualifier o1, Qualifier o2) {
                return collator.compare(o1.getName(), o2.getName());
            }
        });
        cls.remove(dataPlugin.getBaseFunction());
        clasificators = cls.toArray(new Qualifier[cls.size()]);
        String s = dataPlugin.getProperty(DataPlugin.PROPERTY_OUNERS);
        if (s == null)
            s = "";
        final StringTokenizer st = new StringTokenizer(s, " ");
        while (st.hasMoreElements()) {
            owners.add(Long.parseLong(st.nextToken()));
        }
        model.fireTableDataChanged();
    }

    public void apply() {
        final StringBuffer sb = new StringBuffer();
        for (final Long id : owners) {
            sb.append(id.toString());
            sb.append(' ');
        }
        dataPlugin.setProperty(DataPlugin.PROPERTY_OUNERS, sb.toString());
    }
}
