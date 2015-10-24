package com.ramussoft.pb.dfd.visual;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.frames.SelectRowDialog;
import com.ramussoft.pb.frames.components.JFontChooser;
import com.ramussoft.pb.idef.elements.SectorRefactor;

public class DFDObjectDialog extends BaseDialog {

    protected DFDObject object;

    protected JFontChooser fontChooser = new JFontChooser();

    protected JColorChooser foregroundColorChooser = new JColorChooser();

    protected JColorChooser backgroundColorChooser = new JColorChooser();

    private JLabel qualifier = new JLabel();

    private JLabel element = new JLabel();

    protected DataPlugin dataPlugin;

    private Row link;

    protected GUIFramework framework;

    public DFDObjectDialog(GUIFramework framework, DataPlugin dataPlugin) {
        super(framework.getMainFrame(), true);

        setTitle("dfd_object_options");

        this.dataPlugin = dataPlugin;
        this.framework = framework;

        JTabbedPane pane = new JTabbedPane();

        pane.addTab(ResourceLoader.getString("dfd_object"), createFirstTab(pane));
        pane.addTab(ResourceLoader.getString("font"), fontChooser);
        pane.addTab(ResourceLoader.getString("bk_color"),
                backgroundColorChooser);
        pane.addTab(ResourceLoader.getString("fg_color"),
                foregroundColorChooser);

        setMainPane(pane);

        ResourceLoader.setJComponentsText(this);

        this.pack();
        this.setMinimumSize(getSize());
        centerDialog();
        Options.loadOptions(this);
    }

    protected Component createFirstTab(JTabbedPane pane) {
        double[][] size = {{5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5},
                {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM}};

        JPanel panel = new JPanel(new BorderLayout());
        JPanel c = new JPanel(new TableLayout(size));
        c.add(new JLabel("row:"), "1,1");
        c.add(qualifier, "3,1");

        c.add(new JLabel("element:"), "1,3");
        c.add(element, "3,3");
        panel.add(c, BorderLayout.NORTH);

        JPanel jPanel = new JPanel(new BorderLayout());

        JPanel jPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton button = new JButton("set_dfd_object");

        jPanel2.add(button);

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SelectRowDialog dialog = new SelectRowDialog(
                        DFDObjectDialog.this) {

                };
                dialog.setSelectType(SelectType.RADIO);
                dialog.init(framework, dataPlugin, framework.getAccessRules());
                List<Row> list = dialog.showModal();
                if ((list != null) && (list.size() > 0)) {
                    setLink(list.get(0));
                }
            }
        });

        jPanel.add(jPanel2, BorderLayout.NORTH);

        panel.add(jPanel, BorderLayout.CENTER);
        return panel;
    }

    public void showModal(DFDObject object) {
        this.object = object;
        Function function = object.getFunction();

        setLink(dataPlugin.findRowByGlobalId(function.getLink()));

        fontChooser.setSelFont(function.getFont());
        backgroundColorChooser.setColor(function.getBackground());
        foregroundColorChooser.setColor(function.getForeground());

        setVisible(true);
        Options.saveOptions(this);
    }

    private void setLink(Row link) {
        this.link = link;
        if (link != null) {
            element.setText(link.getKod() + " " + link.getName());
            qualifier.setText(link.getQualifier().getName());
        } else {
            element.setText("");
            qualifier.setText("");
        }
    }

    @Override
    protected void onOk() {
        Journaled journaled = (Journaled) dataPlugin.getEngine();
        journaled.startUserTransaction();
        Function function = object.getFunction();
        function.setFont(fontChooser.getSelFont());
        function.setBackground(backgroundColorChooser.getColor());
        function.setForeground(foregroundColorChooser.getColor());
        if (link == null) {
            function.setLink(-1);
        } else
            function.setLink(link.getElement().getId());
        Function p = (Function) function.getParent();
        List<Sector> list = new ArrayList<Sector>();
        if (p != null) {
            for (Sector sector : p.getSectors()) {
                if (function.equals(sector.getStart().getFunction()))
                    list.add(sector);
                else if (function.equals(sector.getEnd().getFunction()))
                    list.add(sector);
            }
        }
        for (Sector sector : list)
            SectorRefactor.fixOwners(sector, dataPlugin);
        journaled.commitUserTransaction();
        super.onOk();
    }

}
