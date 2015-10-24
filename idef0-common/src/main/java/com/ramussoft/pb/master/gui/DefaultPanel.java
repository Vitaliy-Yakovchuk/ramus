package com.ramussoft.pb.master.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.ramussoft.pb.master.model.Properties;
import com.ramussoft.pb.master.model.Property;

public class DefaultPanel extends JPanel implements IPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Properties properties;

    protected JPanel subPanel = new JPanel(new GridBagLayout());

    protected ArrayList<PropertyConnecter> connectors = new ArrayList<PropertyConnecter>();

    protected class PropertyConnecter {

        private Component cmp;

        private Component component;

        private final Property property;

        private double weightY = 0;

        public PropertyConnecter(final Property property) {
            this.property = property;
            switch (property.getType()) {
                case Property.TEXT_FIELD: {
                    cmp = new JTextField();
                }
                break;
                case Property.TEXT: {
                    cmp = new JTextArea();
                    final JScrollPane pane = new JScrollPane();
                    pane.setViewportView(cmp);
                    component = pane;
                    weightY = 0.5;
                }
                break;
                default:
                    break;
            }
        }

        public void get() {
            switch (property.getType()) {
                case Property.HTML_TEXT:
                case Property.TEXT:
                case Property.TEXT_FIELD: {
                    ((JTextComponent) cmp).setText((String) property.getValue());
                }
                break;

                default:
                    break;
            }
        }

        public void set() {
            switch (property.getType()) {
                case Property.HTML_TEXT:
                case Property.TEXT:
                case Property.TEXT_FIELD: {
                    property.setValue(((JTextComponent) cmp).getText());
                }
                break;

                default:
                    break;
            }
        }

        public Component getComponent() {
            if (component == null)
                return cmp;
            return component;
        }

        public double getWeightY() {
            return weightY;
        }
    }

    public DefaultPanel(final Properties properties) {
        super(new BorderLayout());
        this.properties = properties;
        build();
    }

    protected void build() {
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_END;
        c.weightx = 0.5;
        int y = 0;
        double mw = 0;
        for (int i = 0; i < properties.getPropertyCount(); i++) {
            final Property p = properties.getProperty(i);
            final JLabel label = new JLabel(p.getName());
            c.gridy = y;
            c.gridx = 0;
            c.weightx = 0;
            subPanel.add(label, c);
            y++;
            final PropertyConnecter connecter = new PropertyConnecter(p);
            connectors.add(connecter);
            c.gridy = y;
            c.gridx = 0;
            c.weighty = connecter.getWeightY();
            if (c.weighty > mw)
                mw = c.weighty;
            c.weightx = 0.5;
            subPanel.add(connecter.getComponent(), c);
            y++;
            c.gridy = y;
            c.gridx = 0;
            c.weightx = 0;
            c.weighty = 0;
            subPanel.add(new JPanel(new FlowLayout()), c);
            y++;
        }
        if (mw == 0) {
            c.weighty = 1;
            subPanel.add(new JPanel(new FlowLayout()), c);
        }
        this.add(subPanel, BorderLayout.CENTER);
    }

    public void get() {
        for (final PropertyConnecter c : connectors)
            c.get();
    }

    public void set() {
        for (final PropertyConnecter c : connectors)
            c.set();
    }

    public Component getComponent() {
        return this;
    }

    public String getDescribe() {
        return properties.getDescribe();
    }
}
