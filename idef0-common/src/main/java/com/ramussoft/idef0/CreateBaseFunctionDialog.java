package com.ramussoft.idef0;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.pb.idef.visual.MovingArea;

public class CreateBaseFunctionDialog extends BaseDialog {
    private TextField field = new TextField();

    private Engine engine;

    private AccessRules rules;

    protected Qualifier qualifier;

    private JRadioButton idef0 = new JRadioButton(
            ResourceLoader.getString("IDEF0"));

    private JRadioButton dfd = new JRadioButton(ResourceLoader.getString("DFD"));

    private JRadioButton dfds = new JRadioButton(
            ResourceLoader.getString("DFDS"));

    public CreateBaseFunctionDialog(JFrame frame, Engine engine,
                                    AccessRules rules) {
        super(frame, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.engine = engine;
        this.rules = rules;
        this.setTitle(GlobalResourcesManager.getString("CreateFunction"));
        double[][] size = {{5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5}};
        JPanel panel = new JPanel(new TableLayout(size));

        panel.add(new JLabel(ResourceLoader.getString("name")), "1,1");

        field.setPreferredSize(new Dimension(220,
                field.getPreferredSize().height));

        panel.add(field, "3,1");

        ButtonGroup bg = new ButtonGroup();
        bg.add(idef0);
        bg.add(dfd);
        bg.add(dfds);

        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        idef0.setSelected(true);
        jPanel.add(idef0);
        jPanel.add(dfd);
        jPanel.add(dfds);

        JPanel jPanel2 = new JPanel(new BorderLayout());

        jPanel2.add(panel, BorderLayout.CENTER);
        jPanel2.add(jPanel, BorderLayout.SOUTH);

        this.setMainPane(jPanel2);
        setMinSizePack();
        centerDialog();
        this.setResizable(false);
    }

    @Override
    protected void onOk() {
        Journaled rs = (Journaled) engine;

        rs.startUserTransaction();

        createModel();
        super.onOk();
        rs.commitUserTransaction();
    }

    protected void createModel() {
        qualifier = engine.createQualifier();

        qualifier.setName(field.getText());
        IDEF0Plugin.installFunctionAttributes(qualifier, engine);
        if (dfd.isSelected())
            NDataPluginFactory.getDataPlugin(qualifier, engine, rules)
                    .getBaseFunction()
                    .setDecompositionType(MovingArea.DIAGRAM_TYPE_DFD);
        else if (dfds.isSelected())
            NDataPluginFactory.getDataPlugin(qualifier, engine, rules)
                    .getBaseFunction()
                    .setDecompositionType(MovingArea.DIAGRAM_TYPE_DFDS);
    }

    public Qualifier getQualifier() {
        return qualifier;
    }
}
