package com.ramussoft.pb.idef.frames;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXDatePicker;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.idef0.IDEF0TabView;
import com.ramussoft.pb.data.negine.NFunction;

public class DiagramOptionsDialog extends BaseDialog {

    private StatusPanel statusPanel;

    private JXDatePicker createDate = new JXDatePicker();

    private JXDatePicker revDate = new JXDatePicker();

    private JTextField author = new TextField();

    private GUIFramework framework;

    private NFunction function;

    private JComboBox sizesBox = new JComboBox();

    private JComboBox horizontalPageCountBox = new JComboBox();

    public DiagramOptionsDialog(GUIFramework framework) {
        super(framework.getMainFrame(), true);
        this.framework = framework;
        JPanel panel = new JPanel(new BorderLayout());
        statusPanel = new StatusPanel();

        JTabbedPane pane = new JTabbedPane();
        panel.add(pane, BorderLayout.CENTER);
        pane.addTab(ResourceLoader.getString("general"), createGeneralPanel());
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(statusPanel, BorderLayout.NORTH);
        ResourceLoader.setJComponentsText(statusPanel);
        pane.addTab(ResourceLoader.getString("status"), panel2);
        this.setMainPane(panel);
        this.pack();
        this.setMinimumSize(this.getSize());
        centerDialog();
        Options.loadOptions(this);
    }

    private Component createGeneralPanel() {
        double[][] size = {
                {5, TableLayout.FILL, 5},
                {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                        TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                        TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                        TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                        TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5}};
        JPanel panel = new JPanel(new TableLayout(size));

        panel.add(new JLabel(ResourceLoader.getString("autor:")), "1,1");
        panel.add(author, "1,3");

        panel.add(new JLabel(ResourceLoader.getString("creation_date:")), "1,5");
        panel.add(createDate, "1,7");

        panel.add(new JLabel(ResourceLoader.getString("rev_date:")), "1,9");
        panel.add(revDate, "1,11");

        panel.add(new JLabel(ResourceLoader.getString("page_size:")), "1,13");
        panel.add(sizesBox, "1,15");

        panel.add(
                new JLabel(ResourceLoader.getString("horizontal_page_count:")),
                "1,17");
        panel.add(horizontalPageCountBox, "1,19");

        sizesBox.addItem("A4");
        sizesBox.addItem("A3");

        horizontalPageCountBox.addItem("1");
        horizontalPageCountBox.addItem("2");
        horizontalPageCountBox.addItem("3");
        horizontalPageCountBox.addItem("4");
        horizontalPageCountBox.addItem("5");
        horizontalPageCountBox.addItem("6");
        horizontalPageCountBox.addItem("7");
        horizontalPageCountBox.addItem("8");
        horizontalPageCountBox.addItem("9");
        horizontalPageCountBox.addItem("10");

        return panel;
    }

    public void showModal(NFunction function) {
        setTitle(ResourceLoader.getString("DiagramProperties") + " - "
                + function.getName());
        this.function = function;
        statusPanel.setStatus(function.getStatus());
        createDate.setDate(function.getCreateDate());
        revDate.setDate(function.getRevDate());
        author.setText(function.getAuthor());

        String size = function.getPageSize();
        if (size != null) {
            int i = size.indexOf('x');
            if (i < 0)
                sizesBox.setSelectedItem(size);
            else {
                horizontalPageCountBox.setSelectedItem(size.substring(i + 1));
                sizesBox.setSelectedItem(size.substring(0, i));
            }
        }

        setVisible(true);
        Options.saveOptions(this);
    }

    @Override
    protected void onOk() {
        Engine engine = framework.getEngine();
        ((Journaled) engine).startUserTransaction();
        function.setStatus(statusPanel.getStatus());
        if (!function.getCreateDate().equals(createDate.getDate()))
            function.setCreateDate(createDate.getDate());
        if (!function.getRevDate().equals(revDate.getDate()))
            function.setRevDate(revDate.getDate());
        if (!function.getAuthor().equals(author.getText()))
            function.setAuthor(author.getText());
        String newSize = String.valueOf(sizesBox.getSelectedItem());
        if (horizontalPageCountBox.getSelectedIndex() != 0)
            newSize += "x" + horizontalPageCountBox.getSelectedItem();
        if (!newSize.equals(function.getPageSize())) {
            function.setPageSize(newSize);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    framework.propertyChanged(IDEF0TabView.UPDATE_SIZES);
                }
            });
        }

        ((Journaled) engine).commitUserTransaction();
        super.onOk();
    }
}
