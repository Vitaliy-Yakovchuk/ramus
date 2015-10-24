package com.ramussoft.gui.eval;

import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.EvalPlugin;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.eval.Eval;
import com.ramussoft.eval.FunctionPersistent;
import com.ramussoft.eval.Replacementable;
import com.ramussoft.eval.Util;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.QualifierSetupEditor;

public class EvalQualifierSetupEditor extends AbstractTableModel implements
        QualifierSetupEditor {

    /**
     *
     */
    private static final long serialVersionUID = -4233427665634584877L;

    private Qualifier qualifier;

    private Engine engine;

    private FunctionPersistent[] functions;

    private String[] names;

    private Attribute[] attributes;

    private Attribute[] tableAttributes;

    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane pane = new JScrollPane();
        panel.add(pane, BorderLayout.CENTER);
        JTable table = new JTable(this);
        pane.setViewportView(table);

        return panel;
    }

    @Override
    public String getTitle() {
        return GlobalResourcesManager.getString("Qualifier.Formulas");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(Engine engine, Qualifier qualifier) {
        this.engine = engine;

        this.qualifier = qualifier;
        Element element = StandardAttributesPlugin.getElement(engine,
                qualifier.getId());
        List<FunctionPersistent> list = EvalPlugin.getFunctionAttribute(engine,
                element);
        if (list == null)
            list = new ArrayList<FunctionPersistent>(0);

        Util utils = Util.getUtils(engine);

        Hashtable<Attribute, ArrayList<Attribute>> attrs = new Hashtable<Attribute, ArrayList<Attribute>>();
        int lentgh = 0;
        for (Attribute attr : qualifier.getAttributes()) {
            if (attr.getAttributeType().toString().equals("Core.Table")) {
                ArrayList<Attribute> aList = new ArrayList<Attribute>();
                attrs.put(attr, aList);
                Qualifier q = StandardAttributesPlugin
                        .getTableQualifierForAttribute(engine, attr);
                aList.addAll(q.getAttributes());
                lentgh += q.getAttributes().size();
            } else {
                attrs.put(attr, new ArrayList<Attribute>(0));
                lentgh++;
            }
        }

        functions = new FunctionPersistent[lentgh];
        names = new String[lentgh];
        attributes = new Attribute[lentgh];
        tableAttributes = new Attribute[lentgh];
        int i = 0;
        for (Attribute attribute : attrs.keySet()) {
            if (attribute.getAttributeType().toString().equals("Core.Table")) {
                for (Attribute tableAttribute : attrs.get(attribute)) {
                    attributes[i] = attribute;
                    tableAttributes[i] = tableAttribute;
                    names[i] = Util.toCanonicalValue(attribute.getName() + "."
                            + tableAttribute.getName());
                    functions[i] = getFunction(list, attribute, tableAttribute,
                            utils);
                    i++;
                }
            } else {
                attributes[i] = attribute;
                names[i] = Util.toCanonicalValue(attribute.getName());
                functions[i] = getFunction(list, attribute, null, utils);
                i++;
            }
        }

        if (this.qualifier != null) {
            this.fireTableDataChanged();
        }
    }

    private FunctionPersistent getFunction(List<FunctionPersistent> list,
                                           Attribute attribute, Attribute tableAttribute, Util utils) {

        final String e = GlobalResourcesManager.getString("Eval.Element");

        for (FunctionPersistent fp : list) {
            if (fp.getQualifierAttributeId() == attribute.getId()) {
                if ((tableAttribute == null)
                        || (fp.getQualifierTableAttributeId() == tableAttribute
                        .getId()))
                    if (fp.getFunction() != null) {
                        String function = utils.decompile(qualifier,
                                fp.getFunction());
                        Eval eval = new Eval(function);
                        eval.replaceValueNames(new Replacementable() {

                            @Override
                            public String getNewName(String oldName) {
                                if ("ELEMENT".equals(oldName))
                                    return e;
                                return oldName;
                            }
                        });
                        fp.setFunction(eval.toString());
                        return fp;
                    }
            }
        }
        return null;
    }

    @Override
    public void save(Engine engine, Qualifier qualifier) {
        final String e = GlobalResourcesManager.getString("Eval.Element");

        Element element = StandardAttributesPlugin.getElement(engine,
                qualifier.getId());
        List<FunctionPersistent> list = new ArrayList<FunctionPersistent>();
        Util utils = Util.getUtils(engine);

        for (FunctionPersistent fp : functions)
            if (fp != null) {
                String function = fp.getFunction();
                Eval eval = new Eval(function);
                eval.replaceValueNames(new Replacementable() {

                    @Override
                    public String getNewName(String oldName) {
                        if (e.equals(oldName))
                            return "ELEMENT";
                        return oldName;
                    }
                });
                fp.setFunction(utils.compile(qualifier, eval.toString()));
                list.add(fp);
            }

        EvalPlugin.setFunctionAttribute(engine, element, list);
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return attributes.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return names[rowIndex];
        }
        FunctionPersistent fp = functions[rowIndex];
        if (fp == null)
            return null;
        if (columnIndex == 1)
            return fp.getFunction();
        return fp.getAutochange() != 0;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return;
        Attribute attribute = attributes[rowIndex];
        if ("".equals(value))
            functions[rowIndex] = null;
        else {

            FunctionPersistent fp = functions[rowIndex];
            if (fp == null) {
                fp = new FunctionPersistent();
                fp.setQualifierAttributeId(attribute.getId());
                if (tableAttributes[rowIndex] != null) {
                    fp.setQualifierTableAttributeId(tableAttributes[rowIndex]
                            .getId());
                }
                functions[rowIndex] = fp;
            }
            if (columnIndex == 1) {
                fp.setFunction(value.toString());
            } else
                fp.setAutochange(((Boolean) value) ? 1 : 0);

        }
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0)
            return GlobalResourcesManager.getString("OtherElement.Attribute");
        if (column == 1)
            return GlobalResourcesManager.getString("Qualifier.Formula");
        return GlobalResourcesManager.getString("Qualifier.Autochange");
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2)
            return Boolean.class;
        return super.getColumnClass(columnIndex);
    }

    @Override
    public String[] getErrors() {
        ArrayList<String> list = new ArrayList<String>();

        ArrayList<FunctionPersistent> auto = new ArrayList<FunctionPersistent>();

        for (FunctionPersistent fp : functions)
            if (fp != null) {
                if (fp.getAutochange() != 0)
                    auto.add(fp);
            }

        Hashtable<FunctionPersistent, Eval> hash = new Hashtable<FunctionPersistent, Eval>();
        for (FunctionPersistent fp : functions)
            if (fp != null) {
                try {
                    getEval(fp, hash);
                } catch (Exception e) {
                    list.add(MessageFormat.format(
                            GlobalResourcesManager.getString("Eval.Error"),
                            fp.getFunction()));
                }
            }

        if (list.size() == 0)
            for (FunctionPersistent fp : functions)
                if (fp != null) {
                    check(list, fp, auto, hash);
                }
        return list.toArray(new String[list.size()]);
    }

    private void check(ArrayList<String> list, FunctionPersistent fp,
                       ArrayList<FunctionPersistent> auto,
                       Hashtable<FunctionPersistent, Eval> hash) {

        String e = GlobalResourcesManager.getString("Eval.Element");

        Eval eval = getEval(fp, hash);
        for (String value : eval.getValues()) {
            boolean unknown = true;
            String userValue = Util.toUserValue(value);
            if (!userValue.equals(e)) {
                for (Attribute attr : qualifier.getAttributes()) {
                    if (attr.getName().equals(userValue)) {
                        unknown = false;
                        break;
                    }
                }
            } else
                unknown = false;

            if (unknown) {
                List<String> parts = new ArrayList<String>(3);
                StringTokenizer st = new StringTokenizer(userValue, ".");
                while (st.hasMoreTokens())
                    parts.add(st.nextToken());
                if (parts.size() == 2) {
                    for (Attribute attr : qualifier.getAttributes()) {
                        if (attr.getAttributeType().toString()
                                .equals("Core.Table")) {
                            if (attr.getName().equals(parts.get(0))) {
                                Qualifier table = StandardAttributesPlugin
                                        .getTableQualifierForAttribute(engine,
                                                attr);
                                for (Attribute a : table.getAttributes()) {
                                    if (a.getName().equals(parts.get(1))) {
                                        unknown = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else if (parts.size() == 3) {
                    Qualifier qualifier = engine.getQualifierByName(parts
                            .get(0));
                    if (qualifier != null) {
                        Element element = engine.getElement(parts.get(1),
                                qualifier.getId());
                        if (element != null) {
                            for (Attribute attr : qualifier.getAttributes()) {
                                if (attr.getName().equals(parts.get(2))) {
                                    unknown = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (unknown) {
                list.add(MessageFormat.format(
                        GlobalResourcesManager.getString("Eval.UnknowValue"),
                        value));
            }
        }

        if (fp.getAutochange() != 0) {
            recCheck(fp, hash, auto, new ArrayList<Attribute>(), list);
        }

        for (String function : eval.getFunctions())
            if (!Util.getUtils(engine).isFunctionExists(function)) {
                list.add(MessageFormat.format(
                        GlobalResourcesManager.getString("Eval.UnknowFunction"),
                        function));
            }
    }

    private void recCheck(FunctionPersistent fp,
                          Hashtable<FunctionPersistent, Eval> hash,
                          ArrayList<FunctionPersistent> auto, ArrayList<Attribute> recList,
                          ArrayList<String> list) {
        for (Attribute attr : recList) {
            if (fp.getQualifierAttributeId() == attr.getId()) {
                String message = GlobalResourcesManager
                        .getString("Recursive.Link");
                if (list.indexOf(message) < 0)
                    list.add(message);
                return;
            }
        }
        Attribute attribute = null;
        for (Attribute a : qualifier.getAttributes())
            if (fp.getQualifierAttributeId() == a.getId())
                attribute = a;

        recList.add(attribute);

        Eval eval = getEval(fp, hash);
        for (String value : eval.getValues())
            for (Attribute a : qualifier.getAttributes())
                if (a.getName().equals(value)) {
                    FunctionPersistent inAuto = null;
                    for (FunctionPersistent fp1 : auto)
                        if (fp1.getQualifierAttributeId() == a.getId()) {
                            inAuto = fp1;
                            break;
                        }
                    if (inAuto != null) {
                        recCheck(inAuto, hash, auto, recList, list);
                    }
                }

        recList.remove(attribute);
    }

    private Eval getEval(FunctionPersistent fp,
                         Hashtable<FunctionPersistent, Eval> hash) {
        Eval eval = hash.get(fp);
        if (eval == null) {
            eval = new Eval(fp.getFunction());
            hash.put(fp, eval);
        }
        return eval;
    }
}
