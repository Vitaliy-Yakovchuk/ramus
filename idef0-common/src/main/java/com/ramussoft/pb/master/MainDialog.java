package com.ramussoft.pb.master;

import java.text.MessageFormat;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.idef.elements.ProjectOptions;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.master.gui.DefaultPanelCreator;
import com.ramussoft.pb.master.gui.MainPanel;
import com.ramussoft.pb.master.model.DefaultMasterModel;
import com.ramussoft.pb.master.model.DefaultProperties;
import com.ramussoft.pb.master.model.DefaultProperty;
import com.ramussoft.pb.master.model.Properties;
import com.ramussoft.pb.master.model.Property;

public class MainDialog extends JDialog {

    private static final String DEFINITION = "definition:";
    private static final String USED_AT = "used_at:";
    private static final String AUTOR = "autor:";
    private static final String PROJECT_NAME = "project_name:";
    private MainPanel mainPanel = null;
    private DefaultMasterModel masterModel = null;
    private final JFrame frame;

    public MainDialog(final JFrame frame) {
        super(frame, true);
        this.frame = frame;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(MessageFormat.format(Factory.getString("Master"),
                new Object[]{ResourceLoader.getString("project options")}));

    }

    public void showModal(final DataPlugin dataPlugin,
                          final ProjectOptions projectOptions) {
        final Properties name_author = new DefaultProperties(new Property[]{
                new DefaultProperty(AUTOR, Property.TEXT_FIELD),
                new DefaultProperty(PROJECT_NAME, Property.TEXT_FIELD)},
                Factory.getString("name_author.Describe"));

        final Properties used_at = new DefaultProperties(
                new Property[]{new DefaultProperty(USED_AT,
                        Property.TEXT_FIELD)}, Factory
                .getString("used_at.Describe"));

        final Properties def = new DefaultProperties(
                new Property[]{new DefaultProperty(DEFINITION, Property.TEXT)},
                Factory.getString("describe.Describe"));
        masterModel = new DefaultMasterModel(new Properties[]{name_author,
                used_at, def});
        mainPanel = new MainPanel(new DefaultPanelCreator(masterModel)) {
            @Override
            public boolean cancel() {
                if (super.cancel()) {
                    MainDialog.this.setVisible(false);
                    return true;
                }
                return false;
            }

            @Override
            protected void finish() {
                super.finish();
                projectOptions.setProjectAutor(getValue(AUTOR));
                projectOptions.setProjectName(getValue(PROJECT_NAME));
                projectOptions.setDefinition(getValue(DEFINITION));
                projectOptions.setUsedAt(getValue(USED_AT));
                MainDialog.this.setVisible(false);
            }
        };
        setContentPane(mainPanel);
        pack();
        setMinimumSize(this.getSize());
        setLocationRelativeTo(null);
        setVisible(true);
        frame.repaint();
    }

    private String getValue(final String key) {
        final Object value = masterModel.getProperty(key).getValue();
        return value == null ? "" : value.toString();
    }
}
