package com.ramussoft.gui.qualifier.table;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.View;

import org.jdesktop.swingx.JXTableHeader;

public class HTMLTableHeader extends JXTableHeader {

    /**
     *
     */
    private static final long serialVersionUID = 7969242935884258325L;

    public HTMLTableHeader(TableColumnModel columnModel) {
        super(columnModel);
    }

    @Override
    protected Dimension getPreferredSize(Dimension pref) {
        int height = pref.height;
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            TableCellRenderer renderer = getCellRenderer(i);
            Component comp = renderer.getTableCellRendererComponent(table,
                    getColumnModel().getColumn(i).getHeaderValue(), false,
                    false, -1, i);
            if (comp instanceof JLabel) {
                JLabel l = (JLabel) comp;
                try {
                    int width2 = getColumnModel().getColumn(i).getWidth();
                    int height2 = getPreferredSize(l.getText(), true, width2).height;
                    height = Math.max(height2, height);
                } catch (NullPointerException e) {
                    height = Math.max(height, comp.getPreferredSize().height);
                }
            } else
                height = Math.max(height, comp.getPreferredSize().height);
        }
        pref.height = height;
        return pref;
    }

    @Override
    protected TableCellRenderer createDefaultRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            /**
             *
             */
            private static final long serialVersionUID = 5363315650439425721L;

            @Override
            public Component getTableCellRendererComponent(JTable arg0,
                                                           Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
                Component component = super.getTableCellRendererComponent(arg0,
                        arg1, true, arg3, arg4, arg5);
                if (component instanceof JLabel) {
                    JLabel r = (JLabel) component;

                    if (table != null) {
                        JTableHeader header = table.getTableHeader();
                        if (header != null) {
                            r.setForeground(header.getForeground());
                            r.setBackground(header.getBackground());
                            r.setFont(header.getFont());
                        }
                    }
                    r.setVerticalAlignment(JLabel.TOP);
                    r.setHorizontalAlignment(JLabel.CENTER);
                    r.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                }
                return component;
            }
        };

        return r;
    }

    public static java.awt.Dimension getPreferredSize(String text,
                                                      boolean width, int prefSize) {

        JLabel resizer = new JLabel(text);

        View view = (View) resizer
                .getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);

        view.setSize(width ? prefSize : 0, width ? 0 : prefSize);

        float w = view.getPreferredSpan(View.X_AXIS);
        float h = view.getPreferredSpan(View.Y_AXIS) + 2;

        return new java.awt.Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }
}
