package com.ramussoft.idef0;

import java.text.Collator;
import java.util.Vector;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.Codeable;
import com.ramussoft.gui.qualifier.table.RowNode;
import com.ramussoft.gui.qualifier.table.RowRootCreater;
import com.ramussoft.gui.qualifier.table.RowTreeTableModel;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.pb.idef.visual.IDEF0Object;

public class ModelsNode extends RowNode implements Comparable<ModelsNode> {

    private static class FunctionNode extends RowNode implements Codeable {
        private FunctionNode(Vector<TreeTableNode> childs, Row row) {
            super(childs, row);
        }

        @Override
        public Object getValueAt(int index) {
            return row.getName();
        }

        @Override
        public String getCode() {
            /*
             * if (row.getParent() instanceof RootRow) return row.getCode();
			 */
            return IDEF0Object.getIDEF0Kod(row);
        }
    }

    private static Collator collator = Collator.getInstance();

    private Qualifier qualifier;

    private RowTreeTableModel treeTableModel;

    public ModelsNode(Engine engine, Row row, Qualifier aQualifier,
                      AccessRules accessor, GUIFramework framework) {
        super(new Vector<TreeTableNode>(), row);
        RowRootCreater rootCreater = new RowRootCreater() {
            @Override
            public TreeTableNode createRoot(RowSet rowSet) {
                root = ModelsNode.this;
                root.getChildren().addAll(createR2Children(rowSet.getRoot()));
                root.setParent();
                return root;
            }
        };

        this.qualifier = aQualifier;
        Attribute nameAttr = StandardAttributesPlugin
                .getAttributeNameAttribute(engine);
        Attribute name = engine.getAttribute(qualifier.getAttributeForName());
        if (name != null)
            nameAttr = name;
        treeTableModel = new RowTreeTableModel(engine, aQualifier,
                new Attribute[]{nameAttr}, accessor, rootCreater, framework) {
            @Override
            protected RowNode newRowNode(Row row) {
                return new FunctionNode(new Vector<TreeTableNode>(
                        row.getChildCount()), row);
            }
        };
        setParent();
    }

    public static Vector<TreeTableNode> createR2Children(Row root) {

        Vector<TreeTableNode> res = new Vector<TreeTableNode>();

        for (Row row : root.getChildren()) {
            RowNode node = new FunctionNode(createR2Children(row), row);
            node.setParent();
            res.add(node);
        }
        return res;
    }

    @Override
    public String toString() {
        return qualifier.getName();
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    @Override
    public int compareTo(ModelsNode o) {
        return collator.compare(this.toString(), o.toString());
    }

    public void close() {
        treeTableModel.getRowSet().close();
    }

    public void setQualifier(Qualifier qualifier) {
        this.qualifier = qualifier;
    }

    public RowTreeTableModel getTreeTableModel() {
        return treeTableModel;
    }
}
