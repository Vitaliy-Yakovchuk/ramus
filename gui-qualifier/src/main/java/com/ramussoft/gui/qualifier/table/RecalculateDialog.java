package com.ramussoft.gui.qualifier.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.eval.FunctionPersistent;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class RecalculateDialog extends BaseDialog {

    private static final String UNCHECKED = "UNCHECKED";

    /**
     *
     */
    private static final long serialVersionUID = -4929824458513735191L;

    private Hashtable<Attribute, Boolean> chacked = new Hashtable<Attribute, Boolean>();

    private List<Attribute> result = new ArrayList<Attribute>();

    private Engine engine;

    private Qualifier qualifier;

    public RecalculateDialog(GUIFramework framework,
                             List<FunctionPersistent> list, Qualifier qualifier) {
        super(framework.getMainFrame(), true);
        this.qualifier = qualifier;
        setTitle(GlobalResourcesManager.getString("Action.Recalculate"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        engine = framework.getEngine();
        Properties properties = engine
                .getProperties(getPropertiesPath(qualifier));

        List<Attribute> unchecked = getUnchecked(properties, qualifier);

        for (FunctionPersistent fp : list) {
            Attribute attribute = null;
            for (Attribute a : qualifier.getAttributes()) {
                if (a.getId() == fp.getQualifierAttributeId()) {
                    attribute = a;
                    break;
                }
            }
            if (attribute != null) {
                final Attribute attr = attribute;
                final JCheckBox box = new JCheckBox(attribute.getName());
                box.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        chacked.put(attr, box.isSelected());
                    }
                });
                panel.add(box);
                if (unchecked.indexOf(attribute) < 0) {
                    box.setSelected(true);
                    chacked.put(attribute, Boolean.TRUE);
                } else
                    chacked.put(attribute, Boolean.FALSE);
            }
        }

        JScrollPane pane = new JScrollPane(panel);
        setMainPane(pane);
        pack();
        centerDialog();
        setMaximumSize(getSize());
        Options.loadOptions(this, properties);
    }

    private List<Attribute> getUnchecked(Properties properties,
                                         Qualifier qualifier) {
        String s = properties.getProperty(UNCHECKED);
        List<Attribute> result = new ArrayList<Attribute>();
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, ",");
            while (st.hasMoreTokens()) {
                long id = Long.parseLong(st.nextToken());
                for (Attribute attribute : qualifier.getAttributes()) {
                    if (attribute.getId() == id)
                        result.add(attribute);
                }
            }
        }
        return result;
    }

    private String getPropertiesPath(Qualifier qualifier) {
        return "/user/gui/table/view/recalculate-" + qualifier.getId() + ".xml";
    }

    @Override
    protected void onOk() {
        fillResult();
        StringBuffer sb = new StringBuffer();
        for (Attribute attr : qualifier.getAttributes()) {
            if (result.indexOf(attr) < 0)
                sb.append(attr.getId() + ",");
        }
        Properties properties = new Properties();
        properties.setProperty(UNCHECKED, sb.toString());
        Options.saveOptions(this, properties);
        engine.setProperties(getPropertiesPath(qualifier), properties);
        super.onOk();
    }

    private void fillResult() {
        for (Entry<Attribute, Boolean> entry : chacked.entrySet()) {
            if (entry.getValue()) {
                result.add(entry.getKey());
            }
        }
    }

    public List<Attribute> getResult() {
        return result;
    }
}
