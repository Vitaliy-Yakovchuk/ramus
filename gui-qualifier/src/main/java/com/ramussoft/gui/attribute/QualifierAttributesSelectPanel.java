package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;

public class QualifierAttributesSelectPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -2000745486013253409L;

    private List<Attribute> attributes;

    private JCheckBox[] boxs;

    public QualifierAttributesSelectPanel(Qualifier qualifier) {
        super(new BorderLayout());

        this.attributes = qualifier.getAttributes();

        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        boxs = new JCheckBox[attributes.size()];

        for (int i = 0; i < boxs.length; i++) {
            boxs[i] = new JCheckBox(attributes.get(i).getName());
            panel.add(boxs[i]);
        }

        JScrollPane pane = new JScrollPane(panel);
        this.add(pane, BorderLayout.CENTER);
    }

    public List<Attribute> getSelected() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < boxs.length; i++) {
            if (boxs[i].isSelected())
                attributes.add(this.attributes.get(i));
        }
        return attributes;
    }

    public void setSelected(List<Attribute> attributes) {
        for (int i = 0; i < boxs.length; i++) {
            Attribute attribute = this.attributes.get(i);
            boxs[i].setSelected(attributes.indexOf(attribute) >= 0);
        }
    }
}
