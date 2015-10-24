package com.ramussoft.pb.idef.frames;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.Function;

/**
 * Клас - вкладка на IDEF0 редакторі.
 *
 * @author Яковчук В.В.
 */

public class Tab extends JPanel {

    private JButton jButton = null;

    private Function function;

    private final JLabel label;

    private final JTabbedPane pane;

    private final JPanel panel;

    /**
     * This is the default constructor
     */
    public Tab(final Function function, final JTabbedPane pane) {
        super();
        this.pane = pane;
        panel = new JPanel(new BorderLayout());
        // this.setLayout(new BorderLayout());
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        label = new JLabel();
        setFunction(function);
        this.add(label);
        this.add(getJButton());
        // this.add(getJLabel(), BorderLayout.CENTER);
        // this.add(getJButton(), BorderLayout.EAST);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        // setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton() {
                @Override
                protected void paintComponent(final Graphics g) {
                    super.paintComponent(g);
                    final Graphics2D g2 = (Graphics2D) g.create();
                    // shift the image for pressed buttons
                    if (getModel().isPressed()) {
                        g2.translate(1, 1);
                    }
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(Color.BLACK);
                    if (getModel().isRollover()) {
                        g2.setColor(Color.MAGENTA);
                    }
                    final int delta = 3;
                    g2.drawLine(delta, delta, getWidth() - delta - 1,
                            getHeight() - delta - 1);
                    g2.drawLine(getWidth() - delta - 1, delta, delta,
                            getHeight() - delta - 1);
                    g2.dispose();

                }
            };
            jButton.setToolTipText(ResourceLoader.getString("close_tab"));
            jButton.setPreferredSize(new Dimension(17, 17));
            jButton.setContentAreaFilled(false);
            jButton.setFocusable(false);
            jButton.setBorder(BorderFactory.createEtchedBorder());
            jButton.setBorderPainted(false);
            jButton.addMouseListener(buttonMouseListener);
            jButton.setRolloverEnabled(true);
        }
        return jButton;
    }

    public JButton getCloseButton() {
        return getJButton();
    }

    public Function getFunction() {
        return function;
    }

    public void select() {
        pane.setSelectedIndex(pane.indexOfTabComponent(this));
    }

    public String getText() {
        return label.getText();
    }

    public void setFunction(final Function function) {
        this.function = function;
        final String text = function.getKod() + " " + function.getName();
        if (text.length() < 43)
            label.setText(text);
        else {
            // label.setToolTipText("<html><body
            // width=70%>"+text+"</body></html>");
            label.setText(text.substring(0, 40) + "...");
        }
    }

    public JPanel getPanel() {
        return panel;
    }
}
