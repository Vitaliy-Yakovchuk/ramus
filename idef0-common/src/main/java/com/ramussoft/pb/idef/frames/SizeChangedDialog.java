package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.dsoft.pb.idef.ResourceLoader;

public class SizeChangedDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private JCheckBox updateZoom;
    private JCheckBox updateFonts;

    /**
     * Create the dialog.
     */
    public SizeChangedDialog(JDialog jDialog) {
        super(jDialog, true);
        setTitle(ResourceLoader.getString("resize_dialog_title"));
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[]{0, 0};
        gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
        gbl_contentPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);
        {
            updateZoom = new JCheckBox(ResourceLoader.getString("Update_zoom"));
            updateZoom.setSelected(true);
            updateZoom.setEnabled(false);
            GridBagConstraints gbc_updateZoom = new GridBagConstraints();
            gbc_updateZoom.anchor = GridBagConstraints.WEST;
            gbc_updateZoom.insets = new Insets(0, 0, 5, 0);
            gbc_updateZoom.gridx = 0;
            gbc_updateZoom.gridy = 0;
            contentPanel.add(updateZoom, gbc_updateZoom);
        }
        {
            updateFonts = new JCheckBox(ResourceLoader.getString("Update_fonts"));
            updateFonts.setSelected(true);
            GridBagConstraints gbc_updateFonts = new GridBagConstraints();
            gbc_updateFonts.anchor = GridBagConstraints.WEST;
            gbc_updateFonts.gridx = 0;
            gbc_updateFonts.gridy = 1;
            contentPanel.add(updateFonts, gbc_updateFonts);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton(ResourceLoader.getString("ok"));
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
        }
    }

    public boolean isUpdateFonts() {
        return updateFonts.isSelected();
    }

    public boolean isUpdateZoom() {
        return updateZoom.isSelected();
    }

}
