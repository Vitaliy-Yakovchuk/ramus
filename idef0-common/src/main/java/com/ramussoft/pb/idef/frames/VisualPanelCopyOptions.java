package com.ramussoft.pb.idef.frames;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class VisualPanelCopyOptions extends JPanel {

    private JCheckBox copyFont;
    private JCheckBox copyBackground;
    private JCheckBox copyForeground;
    private JCheckBox copySize;

    /**
     * Create the panel.
     */
    public VisualPanelCopyOptions() {
        setLayout(new GridLayout(0, 1, 0, 0));

        copyFont = new JCheckBox("Visual.copyFont");
        copyFont.setSelected(true);
        add(copyFont);

        copyBackground = new JCheckBox("Visual.copyBackground");
        copyBackground.setSelected(true);
        add(copyBackground);

        copyForeground = new JCheckBox("Visual.copyForeground");
        copyForeground.setSelected(true);
        add(copyForeground);

        copySize = new JCheckBox("Visual.copySize");
        copySize.setSelected(true);
        add(copySize);

    }

    public boolean isCopyBackground() {
        return copyBackground.isSelected();
    }

    public boolean isCopyForeground() {
        return copyForeground.isSelected();
    }

    public boolean isCopyFont() {
        return copyFont.isSelected();
    }

    public boolean isCopySize() {
        return copySize.isSelected();
    }

    public void setCopyBackground(boolean copyBackground) {
        this.copyBackground.setSelected(copyBackground);
    }

    public void setCopyForeground(boolean copyForeground) {
        this.copyForeground.setSelected(copyForeground);
    }

    public void setCopyFont(boolean copyFont) {
        this.copyFont.setSelected(copyFont);
    }

    public void setCopySize(boolean copySize) {
        this.copySize.setSelected(copySize);
    }
}
