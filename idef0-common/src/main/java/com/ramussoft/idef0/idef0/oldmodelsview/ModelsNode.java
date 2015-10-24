package com.ramussoft.idef0.idef0.oldmodelsview;

import java.text.Collator;

import javax.swing.tree.TreeNode;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.idef0.TreeModel;

public class ModelsNode extends Row implements TreeNode, Comparable<ModelsNode> {

    private static Collator collator = Collator.getInstance();

    private Qualifier qualifier;

    private Row parent;

    private RowSet rowSet;

    public ModelsNode(Engine engine, final TreeModel model,
                      Qualifier aQualifier, Row aParent) {
        this.qualifier = aQualifier;
        this.engine = engine;
        this.parent = aParent;
        this.rowSet = new RowSet(engine, aQualifier, new Attribute[]{},
                new RowSet.RowCreater() {
                    @Override
                    public Row createRow(Element element, RowSet data,
                                         Attribute[] attributes, Object[] objects) {
                        if (element == null) {
                            return ModelsNode.this;
                        }
                        return new Row(element, data, attributes, objects);
                    }
                }) {
            @Override
            protected void removedFromChildren(Row parent, Row row, int index) {
                super.removedFromChildren(parent, row, index);
                if (parent instanceof RootRow) {
                    children.remove(row);
                }

                if (index >= 0)

                    model.nodesWereRemoved(getParent(parent),
                            new int[]{index}, new Object[]{row});
            }

            private TreeNode getParent(Row parent) {
                if (parent instanceof RootRow) {
                    return ModelsNode.this;
                }
                return parent;
            }

            @Override
            protected void added(Row parent, Row row, int index) {
                super.added(parent, row, index);
                if (parent instanceof RootRow) {
                    children.add(index, row);
                }

                model.nodesWereInserted(getParent(parent), new int[]{index});
            }

            @Override
            protected void attributeChanged(AttributeEvent event) {
                super.attributeChanged(event);
                if (qualifier.getAttributeForName() == event.getAttribute()
                        .getId()) {
                    Row row = findRow(event.getElement().getId());
                    model.nodeChanged(row);
                }
            }
        };
    }

    @Override
    public Row getParent() {
        return parent;
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
        rowSet.close();
    }

    public void setQualifier(Qualifier qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public int hashCode() {
        return qualifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void setName(String value) {
        qualifier.setName(value);
        engine.updateQualifier(qualifier);
    }
}
