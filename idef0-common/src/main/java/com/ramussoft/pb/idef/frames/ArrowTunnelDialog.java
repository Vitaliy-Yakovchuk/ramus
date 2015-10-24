/*
 * Created on 24/9/2005
 */
package com.ramussoft.pb.idef.frames;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

/**
 * @author ZDD
 */
public class ArrowTunnelDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    private JRadioButton jRadioButton = null;

    private JRadioButton jRadioButton1 = null;

    private JRadioButton jRadioButton2 = null;

    private PaintSector sector;

    private final MovingArea movingArea;

    private final GridLayout gridLayout3 = new GridLayout();

    public void showModal(final PaintSector sector) {
        this.sector = sector;
        NSectorBorder sb;
        boolean show;
        if (sector.isSelEnd()) {
            sb = sector.getSector().getEnd();
            show = sector.getSector().getEnd().getFunction() != null;
        } else {
            sb = sector.getSector().getStart();
            show = sector.getSector().getStart().getFunction() != null;
        }

        jRadioButton.setVisible(show);

        if (!show) {
            gridLayout3.setRows(2);
            jPanel1.removeAll();
            jPanel1.add(jRadioButton1, null);
            jPanel1.add(jRadioButton2, null);
        } else {
            gridLayout3.setRows(3);
            jPanel1.removeAll();
            jPanel1.add(jRadioButton1, null);
            jPanel1.add(jRadioButton, null);
            jPanel1.add(jRadioButton2, null);
        }

        if (sb.getTunnelSoft() == Crosspoint.TUNNEL_SOFT) {
            if (show)
                jRadioButton.setSelected(true);
            else
                jRadioButton2.setSelected(true);
        } else if (sb.getTunnelSoft() == Crosspoint.TUNNEL_SIMPLE_SOFT)
            jRadioButton2.setSelected(true);
        else
            jRadioButton1.setSelected(true);
        pack();
        setVisible(true);
        Options.saveOptions("arrow_border_dialog", this);
    }

    /**
     * This is the default constructor
     */
    public ArrowTunnelDialog(final MovingArea movingArea) {
        super(movingArea.getPanel().getFramework().getMainFrame(), true);
        this.movingArea = movingArea;
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setResizable(false);
        setTitle("arrow_border_dialog");
        this.setSize(365, 223);
        setContentPane(getJContentPane());
        setModal(true);
        ResourceLoader.setJComponentsText(this);
        setLocationRelativeTo(Main.getMainFrame());
        Options.loadOptions("arrow_border_dialog", this);
        pack();
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getJPanel1(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            final FlowLayout flowLayout2 = new FlowLayout();
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout2);
            flowLayout2.setAlignment(java.awt.FlowLayout.RIGHT);
            jPanel.add(getJPanel2(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.setLayout(gridLayout3);
            jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    null, ResourceLoader
                            .getString("how_do_you_want_resolve_thid_tunnel"),
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
                    null));
            gridLayout3.setRows(3);
            gridLayout3.setColumns(1);
            jPanel1.add(getJRadioButton1(), null);
            jPanel1.add(getJRadioButton(), null);
            jPanel1.add(getJRadioButton2(), null);
            final ButtonGroup bg = new ButtonGroup();
            bg.add(jRadioButton);
            bg.add(jRadioButton1);
            bg.add(jRadioButton2);
        }
        return jPanel1;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            final GridLayout gridLayout1 = new GridLayout();
            jPanel2 = new JPanel();
            jPanel2.setLayout(gridLayout1);
            gridLayout1.setRows(1);
            gridLayout1.setHgap(5);
            jPanel2.add(getJButton(), null);
            jPanel2.add(getJButton1(), null);
        }
        return jPanel2;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("ok");
            jButton.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    movingArea.startUserTransaction();
                    tunnelIt();
                    movingArea.commitUserTransaction();
                    setVisible(false);
                }

            });
        }
        return jButton;
    }

    protected void tunnelIt() {
        NSectorBorder crosspoint;
        boolean start;
        if (sector.isSelStart()) {
            crosspoint = sector.getSector().getStart();
            start = true;
        } else if (sector.isSelEnd()) {
            crosspoint = sector.getSector().getEnd();
            start = false;
        } else
            return;
        if (jRadioButton.isSelected())
            crosspoint.setTunnelSoft(Crosspoint.TUNNEL_SOFT);
        else if (jRadioButton2.isSelected())
            crosspoint.setTunnelSoft(Crosspoint.TUNNEL_SIMPLE_SOFT);
        else {
            SectorRefactor.fixOwners(
                    movingArea.getRefactor().createSectorOnIn(sector, start),
                    movingArea.getDataPlugin());
        }
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setText("cancel");
            jButton1.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    setVisible(false);
                }

            });

        }
        return jButton1;
    }

    /**
     * This method initializes jRadioButton
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton() {
        if (jRadioButton == null) {
            jRadioButton = new JRadioButton();
            jRadioButton.setText("change_it_to_resolve_rounded_tunnel");
        }
        return jRadioButton;
    }

    /**
     * This method initializes jRadioButton1
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton1() {
        if (jRadioButton1 == null) {
            jRadioButton1 = new JRadioButton();
            jRadioButton1.setText("resolve_it_to_border_arrow");
        }
        return jRadioButton1;
    }

    /**
     * This method initializes jRadioButton1
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton2() {
        if (jRadioButton2 == null) {
            jRadioButton2 = new JRadioButton();
            jRadioButton2.setText("simple_change_it_to_resolve_rounded_tunnel");
        }
        return jRadioButton2;
    }
} // @jve:decl-index=0:visual-constraint="43,31"
