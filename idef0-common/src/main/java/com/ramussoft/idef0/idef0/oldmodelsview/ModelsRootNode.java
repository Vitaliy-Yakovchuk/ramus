package com.ramussoft.idef0.idef0.oldmodelsview;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.TreeModel;

public abstract class ModelsRootNode extends Row implements TreeNode {

    private Vector<ModelsNode> children = new Vector<ModelsNode>();

    private Engine engine;

    private TreeModel model;

    private QualifierListener qualifierListener = new QualifierAdapter() {
        @Override
        public void qualifierDeleted(QualifierEvent event) {
            if (isDisabeUpdate())
                return;
            int index = 0;
            for (ModelsNode node : children) {
                if (node.getQualifier().equals(event.getOldQualifier())) {
                    children.remove(node);
                    node.close();
                    model.nodesWereRemoved(ModelsRootNode.this,
                            new int[]{index}, new Object[]{node});
                    return;
                }
                index++;
            }
        }

        @Override
        public void qualifierUpdated(QualifierEvent event) {
            if (isDisabeUpdate())
                return;
            if (IDEF0Plugin.isFunction(event.getNewQualifier())) {
                for (ModelsNode node : children) {
                    if (node.getQualifier().equals(event.getNewQualifier())) {
                        Collections.sort(children);
                        node.setQualifier(event.getNewQualifier());
                        model.nodesChanged(ModelsRootNode.this, createIndeces());
                        return;
                    }
                }

                TreeNode node = addChild(event.getNewQualifier());
                Collections.sort(children);
                model.nodesWereInserted(ModelsRootNode.this,
                        new int[]{getIndex(node)});
            }
        }
    };

    public ModelsRootNode(Engine engine, TreeModel model) {
        super();
        this.engine = engine;
        this.model = model;
        engine.addQualifierListener(qualifierListener);
        List<Qualifier> qList = engine.getQualifiers();
        for (Qualifier qualifier : qList) {
            if (IDEF0Plugin.isFunction(qualifier)) {
                addChild(qualifier);
            }
        }
        Collections.sort(children);
    }

    protected int[] createIndeces() {
        int[] res = new int[children.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = i;
        return res;
    }

    private TreeNode addChild(Qualifier qualifier) {
        ModelsNode modelsNode = new ModelsNode(engine, model, qualifier, this);
        children.add(modelsNode);
        return modelsNode;
    }

    @Override
    public Enumeration children() {
        return children.elements();
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public Row getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public Row getParent() {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public void close() {
        engine.removeQualifierListener(qualifierListener);
        for (ModelsNode node : children)
            node.close();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return GlobalResourcesManager.getString("ModelsView");
    }

    public abstract boolean isDisabeUpdate();
}
