package com.ramussoft.gui.elist;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public class TopTablePanel extends JTableHeader {

    /**
     *
     */
    private static final long serialVersionUID = -210798240622677697L;

    private ElistTablePanel panel;

    public TopTablePanel(final ElistTablePanel panel, JTable table) {
        super(table.getColumnModel());
        this.panel = panel;
        this.table = table;
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setToolTipText(panel.getToolTipText(getHeight() - e.getY(), e
                        .getX()));
            }
        });
        setResizingAllowed(false);
        panel.setSize(getSize().height, getSize().width);
    }

    @Override
    public void paint(Graphics gr) {
        panel.setSize(getSize().height, getSize().width);
        Graphics2D g = (Graphics2D) gr;
        g.translate(0, getHeight());
        g.rotate(Math.PI + Math.PI / 2);
        panel.paintMe(g);
    }

    @Override
    public void paintAll(Graphics g) {
        super.paintAll(g);
    }

    @Override
    protected void paintComponent(Graphics gr) {
        panel.setSize(getSize().height, getSize().width);
        Graphics2D g = (Graphics2D) gr;
        g.translate(0, getHeight());
        g.rotate(Math.PI + Math.PI / 2);
        panel.paintMe(g);
    }

}
