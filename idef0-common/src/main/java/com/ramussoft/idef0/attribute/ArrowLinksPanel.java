package com.ramussoft.idef0.attribute;

import java.awt.BorderLayout;

import java.awt.datatransfer.Transferable;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.data.negine.NSector;

public class ArrowLinksPanel extends JPanel {

    /**
     * Create the panel.
     */

    private HashMap<String, ItemHolder> holders = new HashMap<String, ArrowLinksPanel.ItemHolder>();

    private HashMap<String, ItemHolder> lowLevelHolders = new HashMap<String, ArrowLinksPanel.ItemHolder>();

    public ArrowLinksPanel(Function function) {
        setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane);

        DefaultListModel listModel = new DefaultListModel();

        int sectorIndex = 1;
        for (Sector sector : ((Function) function.getParentRow()).getSectors())
            if (function.equals(sector.getStart().getFunction())
                    || function.equals(sector.getEnd().getFunction())) {
                listModel.addElement(new ItemHolder(sector, null, sectorIndex,
                        -1));

                int rowIndex = 1;
                if (sector.getStream() != null)
                    for (Row row : sector.getStream().getAdded())
                        if (row != null) {
                            listModel.addElement(new ItemHolder(sector, row,
                                    sectorIndex, rowIndex));
                            rowIndex++;
                        }

                sectorIndex++;
            }

        for (int i = 0; i < listModel.getSize(); i++) {
            ItemHolder item = (ItemHolder) listModel.get(i);
            holders.put(item.getCode(), item);
            lowLevelHolders.put(item.getLowLevelCode(), item);
        }

        JList list = new JList(listModel);
        list.setDragEnabled(true);
        list.setTransferHandler(new TransferHandler(null) {
            public int getSourceActions(JComponent c) {
                return COPY;
            }

            protected Transferable createTransferable(JComponent c) {
                if (c instanceof JList) {
                    JList list = (JList) c;
                    Object[] values = list.getSelectedValues();

                    if (values == null || values.length == 0) {
                        return null;
                    }

                    StringBuffer plainBuf = new StringBuffer();
                    StringBuffer htmlBuf = new StringBuffer();

                    htmlBuf.append("<html>\n<body>\n<ul>\n");

                    for (int i = 0; i < values.length; i++) {
                        Object obj = values[i];
                        if (obj instanceof ItemHolder) {
                            ItemHolder holder = (ItemHolder) obj;
                            plainBuf.append(holder.getCode());
                            plainBuf.append("\n");
                            htmlBuf.append("<li>");
                            htmlBuf.append(holder.getCode());
                            htmlBuf.append("</li>\n");
                        } else
                            plainBuf.append("\n");
                        htmlBuf.append("<li></li>\n");
                    }

                    // remove the last newline
                    plainBuf.deleteCharAt(plainBuf.length() - 1);
                    htmlBuf.append("</ul>\n</body>\n</html>");

                    return new BasicTransferable(plainBuf.toString(), htmlBuf
                            .toString());
                }
                return null;
            }
        });
        scrollPane.setViewportView(list);
    }

    public ItemHolder getItemHolder(String code) {
        return holders.get(code);
    }

    public ItemHolder getItemHolderByLowLevelCode(String code) {
        return lowLevelHolders.get(code);
    }

    public static class ItemHolder {
        Sector sector;

        Row row;

        int sectorIndex;

        int rowIndex;

        private ItemHolder(Sector sector, Row row, int sectorIndex, int rowIndex) {
            super();
            this.sector = sector;
            this.row = row;
            this.sectorIndex = sectorIndex;
            this.rowIndex = rowIndex;
        }

        public String getCode() {
            if (row == null)
                return "\\" + sectorIndex;

            return "\\" + sectorIndex + "." + rowIndex;
        }

        @Override
        public String toString() {
            if (row == null)
                return sectorIndex + ". " + sector;

            return sectorIndex + "." + rowIndex + ". " + row;
        }

        public Sector getSector() {
            return sector;
        }

        public Row getRow() {
            return row;
        }

        public String getLowLevelCode() {
            if (row == null)
                return "\\" + ((NSector) sector).getElementId();

            return "\\" + ((NSector) sector).getElementId() + "."
                    + row.getElement().getId();
        }
    }

}
