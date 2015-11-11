package com.ramussoft.pb.master.gui;

import java.awt.BorderLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.swingx.JXHeader;

import com.ramussoft.pb.master.Factory;
import com.ramussoft.pb.master.model.DefaultMasterModel;
import com.ramussoft.pb.master.model.DefaultProperties;
import com.ramussoft.pb.master.model.DefaultProperty;
import com.ramussoft.pb.master.model.Properties;
import com.ramussoft.pb.master.model.Property;

public class MainPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final PanelCreator creator;

    private int currentPanel = 0;

    private final JLabel etap = new JLabel();

    private final JPanel panel = new JPanel(new BorderLayout());

    // private final JTextPane textPane = new JTextPane();

    private JXHeader header = new JXHeader();

    private final Action back = new AbstractAction(Factory.getString("Back"),
            new ImageIcon(getClass().getResource(
                    "/com/dsoft/pb/master/icons/back.png"))) {

        public void actionPerformed(ActionEvent e) {
            MainPanel.this.back();
        }

    };

    private final Action next = new AbstractAction(Factory.getString("Next"),
            new ImageIcon(getClass().getResource(
                    "/com/dsoft/pb/master/icons/next.png"))) {

        public void actionPerformed(ActionEvent e) {
            MainPanel.this.next();
        }

    };

    private final Action finish = new AbstractAction(Factory
            .getString("Finish"), new ImageIcon(getClass().getResource(
            "/com/dsoft/pb/master/icons/finish.png"))) {

        public void actionPerformed(ActionEvent e) {
            MainPanel.this.finish();
        }

    };

    private final Action cancel = new AbstractAction(Factory
            .getString("Cancel"), new ImageIcon(getClass().getResource(
            "/com/dsoft/pb/master/icons/cancel.png"))) {

        public void actionPerformed(ActionEvent e) {
            MainPanel.this.cancel();
        }

    };

    private final JButton bBack = new JButton(back);

    private final JButton bNext = new JButton(next);

    private final JButton bCancel = new JButton(cancel);

    private final JButton bFinish = new JButton(finish);

    public MainPanel(final PanelCreator creator) {
        super(new BorderLayout());
        this.creator = creator;
        init();
        back.setEnabled(false);
        setCurrentPanel(0);
    }

    protected void finish() {
        creator.getPanel(currentPanel).set();
    }

    protected void back() {
        creator.getPanel(currentPanel).set();
        setCurrentPanel(currentPanel - 1);
        if (currentPanel <= 0)
            back.setEnabled(false);
    }

    protected void next() {
        creator.getPanel(currentPanel).set();
        setCurrentPanel(currentPanel + 1);
        if (currentPanel > 0)
            back.setEnabled(true);
    }

    private void checkFinish() {
        if (currentPanel == creator.getPanelCount() - 1) {
            next.setEnabled(false);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    bFinish.requestFocus();
                }
            });
        } else if (!next.isEnabled())
            next.setEnabled(true);
    }

    public boolean cancel() {
        return JOptionPane.showConfirmDialog(this, Factory
                .getString("ReallyCancel"), UIManager
                .getString("OptionPane.titleText"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

    }

    private void setCurrentPanel(final int i) {
        currentPanel = i;
        {
            final IPanel panel = creator.getPanel(i);
            panel.get();
            header.setDescription(panel.getDescribe());
            this.panel.removeAll();
            this.panel.add(panel.getComponent(), BorderLayout.CENTER);
        }
        panel.validate();
        panel.repaint();
        etap.setText(MessageFormat.format(Factory.getString("EtapOf"),
                new Object[]{Integer.toString(i + 1),
                        Integer.toString(creator.getPanelCount())}));
        checkFinish();
    }

    private void init() {
        JPanel comp = new JPanel(new BorderLayout());
        comp.add(header, BorderLayout.CENTER);
        comp.add(new JPanel(new FlowLayout()), BorderLayout.SOUTH);
        this.add(comp, BorderLayout.NORTH);
        this.add(new JPanel(new FlowLayout()), BorderLayout.WEST);
        // final JPanel p = new JPanel(new BorderLayout());
        // p.setBorder(BorderFactory.createTitledBorder(Factory
        // .getString("Describe")));
        // textPane.setContentType("text/html");
        // textPane.setEditable(false);

        // GUIPatchFactory.patchHTMLTextPane(textPane);

        // final JScrollPane pane = new JScrollPane();
        // pane.setPreferredSize(new Dimension(180, 20));
        // pane.setViewportView(textPane);
        // p.add(header, BorderLayout.CENTER);

        header.setIcon(new ImageIcon(getClass().getResource(
                "/com/dsoft/pb/master/icons/configure.png")));

        this.add(panel, BorderLayout.CENTER);
        this.add(createBottom(), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(600, 310));
    }

    private Component createBottom() {
        final JPanel panel = new JPanel(new GridLayout(1, 4, 5, 0));
        panel.add(bBack);
        panel.add(bNext);
        panel.add(bCancel);
        panel.add(bFinish);

        final JPanel p = new JPanel(new FlowLayout());

        p.add(panel);

        final JPanel res = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        final JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(etap);

        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;

        res.add(p1, c);

        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 0;

        res.add(p, c);

        return res;
    }

    public void actionPerformed(final ActionEvent e) {

    }

    public static void main(final String[] args) {

        try {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        UIManager.put("TextArea.font", new Font("Tymes New Roman", 0, 14));

        final JFrame frame = new JFrame("Test");

        final Properties p1 = new DefaultProperties(new Property[]{
                new DefaultProperty("v1", Property.TEXT_FIELD),
                new DefaultProperty("v2", Property.TEXT),
                new DefaultProperty("v3", Property.TEXT_FIELD),
                new DefaultProperty("v3", Property.TEXT)},
                "<html><body><h1>Header</h1> just text</body></html>");

        final Properties p2 = new DefaultProperties(new Property[]{
                new DefaultProperty("x1", Property.TEXT_FIELD),
                new DefaultProperty("x2", Property.TEXT_FIELD),
                new DefaultProperty("x3", Property.TEXT_FIELD),
                new DefaultProperty("x4", Property.TEXT_FIELD)},
                "Just simple text describe");

        frame.setContentPane(new MainPanel(new DefaultPanelCreator(
                new DefaultMasterModel(new Properties[]{p1, p1, p1, p1, p2,
                        p1, p1, p1}))) {
            @Override
            public boolean cancel() {
                if (super.cancel()) {
                    System.exit(0);
                    return true;
                }
                return false;
            }

            @Override
            protected void finish() {
                System.exit(0);
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
