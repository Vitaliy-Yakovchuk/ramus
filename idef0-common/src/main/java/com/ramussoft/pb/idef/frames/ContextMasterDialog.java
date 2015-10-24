package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.dmaster.SimpleTemplate;
import com.ramussoft.pb.dmaster.Template;
import com.ramussoft.pb.dmaster.TemplateFactory;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.idef.visual.MovingArea;

public class ContextMasterDialog extends BaseDialog {

    private Vector<Template> data = new Vector<Template>();

    private String cTemplate = "";

    private Template active = null;

    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(4, 2,
            20, 1));

    private final TemplateSample prev = new TemplateSample();

    private final AbstractListModel listModel = new AbstractListModel() {

        public Object getElementAt(int index) {
            return data.get(index);
        }

        public int getSize() {
            return data.size();
        }

    };

    private JList list = new JList(listModel);

    private final JScrollPane sPane = new JScrollPane();

    private Template result = null;

    private JRadioButton idef0 = new JRadioButton("IDEF0");

    private JRadioButton dfd = new JRadioButton("DFD");

    private JRadioButton dfds = new JRadioButton("DFDS");

    private final ListSelectionListener lsl = new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent event) {
            if (list.getSelectedValue() == null)
                return;
            cTemplate = list.getSelectedValue().toString();
            active.close();
            active = getTemplate();
            prev.setActive(active);
            if (active instanceof SimpleTemplate) {
                spinner.setEnabled(true);
                ((SimpleTemplate) active)
                        .setCount(((Number) spinner.getValue()).intValue());
            } else
                spinner.setEnabled(false);
            prev.repaint();
        }
    };

    public ContextMasterDialog(final JFrame frame) {
        super(frame, true);
        init();
    }

    private void init() {
        setTitle("Selecting_Template");
        final JSplitPane splitPane = new JSplitPane();
        final JPanel panel1 = new JPanel(new BorderLayout());
        final JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        panel3.add(new JLabel("Template"));
        panel1.add(panel3, BorderLayout.NORTH);
        panel1.add(sPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(panel1);
        splitPane.setDividerLocation(200);
        list.addListSelectionListener(lsl);
        sPane.setViewportView(list);

        final JPanel panel = new JPanel(new BorderLayout());

        panel.add(prev, BorderLayout.CENTER);
        final JPanel lp = new JPanel(new BorderLayout());

        lp.add(new JLabel("Function.Count"), BorderLayout.WEST);
        lp.add(new JPanel(new FlowLayout()), BorderLayout.CENTER);
        lp.add(spinner, BorderLayout.EAST);
        final JPanel panel2 = new JPanel(new BorderLayout());
        final JPanel panel4 = new JPanel(new FlowLayout());
        panel4.add(lp);
        panel2.add(panel4, BorderLayout.CENTER);
        panel.add(panel2, BorderLayout.SOUTH);

        splitPane.setRightComponent(panel);

        spinner.addChangeListener(new ChangeListener() {

            public void stateChanged(final ChangeEvent arg0) {
                if (active instanceof SimpleTemplate) {
                    ((SimpleTemplate) active).setCount(((Number) spinner
                            .getValue()).intValue());
                    prev.repaint();
                }
            }

        });
        JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel5.add(idef0);
        panel5.add(dfd);
        panel5.add(dfds);

        idef0.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadModels();
                prev.setDiagramType(0);
                prev.repaint();
            }
        });

        dfd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadModels();
                prev.setDiagramType(MovingArea.DIAGRAM_TYPE_DFD);
                prev.repaint();
            }
        });

        dfds.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadModels();
                prev.setDiagramType(MovingArea.DIAGRAM_TYPE_DFDS);
                prev.repaint();
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(idef0);
        group.add(dfd);
        group.add(dfds);
        idef0.setSelected(true);

        JPanel panel6 = new JPanel(new BorderLayout());
        panel6.add(panel5, BorderLayout.SOUTH);

        panel6.add(splitPane, BorderLayout.CENTER);

        setMainPane(panel6);
        ResourceLoader.setJComponentsText(this);
        pack();
        setMinimumSize(this.getSize());
        setLocationRelativeTo(null);
    }

    public Template showModal() {
        Options.loadOptions("ContextMasterDialog", this);
        result = null;
        prev.setDiagramType(getDecompositionType());
        loadModels();
        setVisible(true);
        for (Template template : data)
            template.close();
        Options.saveOptions("ContextMasterDialog", this);
        return result;
    }

    private void loadModels() {
        data = TemplateFactory.getTemplates(data, getDecompositionType());
        list.removeListSelectionListener(lsl);
        list = new JList(listModel);
        list.addListSelectionListener(lsl);
        sPane.setViewportView(list);
        active = getTemplate();
        if (active == null) {
            cTemplate = data.get(0).toString();
        }
        active = getTemplate();
        list.setSelectedValue(active, true);
    }

    private Template getTemplate() {
        for (final Template t : data) {
            if (cTemplate.equals(t.toString()))
                return t;
        }
        return null;
    }

    @Override
    protected void onOk() {
        result = active;
        super.onOk();
    }

    public int getDecompositionType() {
        if (dfd.isSelected())
            return MovingArea.DIAGRAM_TYPE_DFD;
        if (dfds.isSelected())
            return MovingArea.DIAGRAM_TYPE_DFDS;
        return 0;
    }

    public void setDecompositionType(int decompositionType) {
        if (decompositionType == MovingArea.DIAGRAM_TYPE_DFD)
            dfd.setSelected(true);
        else if (decompositionType == MovingArea.DIAGRAM_TYPE_DFDS)
            dfds.setSelected(true);
        else
            idef0.setSelected(true);
    }
}
