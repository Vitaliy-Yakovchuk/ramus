package com.ramussoft.gui.qualifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.QualifierSetupEditor;

public class QualifierHistorySetupEditor implements QualifierSetupEditor {

    private JPanel panel = new JPanel();

    private List<JCheckBox> boxs = new ArrayList<JCheckBox>();

    private List<Attribute> attrs = new ArrayList<Attribute>();

    public QualifierHistorySetupEditor() {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }

    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public String[] getErrors() {
        return new String[]{};
    }

    @Override
    public String getTitle() {
        return GlobalResourcesManager.getString("Qualifier.History");
    }

    @Override
    public void load(Engine engine, Qualifier qualifier) {
        List<Long> attrIds = new ArrayList<Long>();
        for (int i = 0; i < this.attrs.size(); i++)
            if (boxs.size() > i) {
                JCheckBox box = boxs.get(i);
                if (box.isSelected())
                    attrIds.add(this.attrs.get(i).getId());
            }

        List<Long> l = StandardAttributesPlugin.getHistoryQualifiers(engine)
                .get(qualifier.getId());
        boolean loadHistory = false;
        if (panel.getComponentCount() == 0)
            loadHistory = true;

        panel.removeAll();

        boxs.clear();

        attrs.clear();
        for (Attribute attribute : qualifier.getAttributes()) {
            if (attribute.getAttributeType().isHistorySupport())
                attrs.add(attribute);
        }

        for (Attribute attribute : attrs) {
            JCheckBox box = new JCheckBox(attribute.getName());
            if (attrIds.indexOf(attribute.getId()) >= 0)
                box.setSelected(true);
            if ((loadHistory) && (l != null)
                    && (l.indexOf(attribute.getId()) >= 0))
                box.setSelected(true);
            panel.add(box);
            boxs.add(box);
        }

        panel.revalidate();
        panel.repaint();
    }

    @Override
    public void save(Engine engine, Qualifier qualifier) {
        Properties ps = new Properties();
        try {
            InputStream is = engine
                    .getInputStream(StandardAttributesPlugin.PROPERTIES);
            if (is != null) {
                ps.loadFromXML(is);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Long> attrs = new ArrayList<Long>();
        for (int i = 0; i < this.attrs.size(); i++) {
            JCheckBox box = boxs.get(i);
            if (box.isSelected())
                attrs.add(this.attrs.get(i).getId());
        }
        if (attrs.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Long l : attrs) {
                sb.append(l);
                sb.append(' ');
            }
            ps.setProperty(StandardAttributesPlugin.HISTORY_QUALIFIER_KEY
                    + qualifier.getId(), sb.toString());
        } else
            ps.remove(StandardAttributesPlugin.HISTORY_QUALIFIER_KEY
                    + qualifier.getId());
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ps.storeToXML(os, "Attributes with history, etc...");
            os.close();
            engine.setUndoableStream(StandardAttributesPlugin.PROPERTIES, os
                    .toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
