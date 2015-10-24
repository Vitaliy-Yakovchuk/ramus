package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.frames.components.JFontChooser;
import com.ramussoft.pb.idef.visual.VisualPanel;

public class VisualOptionsDialog extends BaseDialog {

    private JFontChooser fontChooser;
    private JColorChooser backgroundColorChooser;
    private JColorChooser foregroundColorChooser;
    private VisualPanel visualPanel;
    private GUIFramework framework;

    private boolean updateFont;
    private boolean updateBackground;
    private boolean updateForeground;

    /**
     * Create the dialog.
     */
    public VisualOptionsDialog(GUIFramework framework, VisualPanel visualPanel) {
        super(framework.getMainFrame(), true);
        setTitle(MainFrame.VISUAL_OPTIONS);
        this.visualPanel = visualPanel;
        this.framework = framework;
        setBounds(100, 100, 450, 300);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        fontChooser = new JFontChooser() {
            @Override
            protected void fireFontUpdated() {
                super.fireFontUpdated();
                updateFont = true;
            }
        };
        tabbedPane.addTab(ResourceLoader.getString("font"), null, fontChooser,
                null);

        backgroundColorChooser = new JColorChooser();
        backgroundColorChooser.getSelectionModel().addChangeListener(
                new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        updateBackground = true;
                    }
                });
        tabbedPane.addTab(ResourceLoader.getString("bk_color"), null,
                backgroundColorChooser, null);

        foregroundColorChooser = new JColorChooser();

        foregroundColorChooser.getSelectionModel().addChangeListener(
                new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        updateForeground = true;
                    }
                });
        tabbedPane.addTab(ResourceLoader.getString("fg_color"), null,
                foregroundColorChooser, null);

        Font font = visualPanel.getFontA();
        if (font != null)
            fontChooser.setSelFont(font);
        Color background = visualPanel.getBackgroundA();
        if (background != null)
            backgroundColorChooser.setColor(background);
        Color foreground = visualPanel.getForegroundA();
        if (foreground != null)
            foregroundColorChooser.setColor(foreground);

        updateFont = false;
        updateBackground = false;
        updateForeground = false;

        setMainPane(contentPanel);
        ResourceLoader.setJComponentsText(this);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        Options.loadOptions(this);
    }

    @Override
    protected void onOk() {
        if (updateBackground || updateForeground || updateFont) {
            ((Journaled) framework.getEngine()).startUserTransaction();
            if (updateBackground)
                visualPanel.setBackgroundA(backgroundColorChooser.getColor());
            if (updateForeground)
                visualPanel.setForegroundA(foregroundColorChooser.getColor());
            if (updateFont)
                visualPanel.setFontA(fontChooser.getSelFont());
            ((Journaled) framework.getEngine()).commitUserTransaction();
        }

        Options.saveOptions(this);
        super.onOk();
    }

}
