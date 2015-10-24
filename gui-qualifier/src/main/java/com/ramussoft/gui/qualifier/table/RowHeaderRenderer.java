package com.ramussoft.gui.qualifier.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;

public class RowHeaderRenderer extends JLabel implements ListCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = -1557500724997100572L;

    private final JTable table;

    private final Border selectedBorder;

    private final Border normalBorder;

    private final Font selectedFont;

    private final Font normalFont;

    RowHeaderRenderer(final JTable table) {
        this.table = table;
        normalBorder = UIManager.getBorder("TableHeader.cellBorder");
        selectedBorder = BorderFactory.createRaisedBevelBorder();
        final JTableHeader header = table.getTableHeader();
        normalFont = header.getFont();
        selectedFont = normalFont.deriveFont(normalFont.getStyle() | Font.BOLD);
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setOpaque(true);
    }

    public Component getListCellRendererComponent(final JList list,
                                                  final Object value, final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {
        if (table.getSelectionModel().isSelectedIndex(index)) {
            setFont(selectedFont);
            setBorder(selectedBorder);
        } else {
            setFont(normalFont);
            setBorder(normalBorder);
        }
        if (value == null)
            setText("");
        else
            setText(value.toString());
        return this;
    }
}