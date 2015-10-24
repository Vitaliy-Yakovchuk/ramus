package com.ramussoft.gui.branches;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.tree.TreeModelSupport;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.ramussoft.common.Branch;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.BranchAdapter;
import com.ramussoft.common.event.BranchEvent;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionChangeEvent;
import com.ramussoft.gui.common.event.ActionChangeListener;
import com.ramussoft.gui.qualifier.Commands;

public class BranchView extends AbstractUniqueView implements UniqueView {

    private Engine engine;
    private BranchModel branchModel;
    private AddChildBranch addChildBranch = new AddChildBranch();
    private ActivateBranch activateBranch = new ActivateBranch();
    private BranchEditCommment editCommment = new BranchEditCommment();
    private JXTreeTable table;
    private DateFormat timeFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM);
    private BranchAdapter branchListener;

    private long actualBranch;

/*	private ImageIcon branch = new ImageIcon(getClass().getResource(
            "/com/ramussoft/gui/branch-down.png"));
	private ImageIcon branchTree = new ImageIcon(getClass().getResource(
			"/com/ramussoft/gui/branch-down-right.png"));*/

    public BranchView(GUIFramework framework) {
        super(framework);
        this.engine = framework.getEngine();
    }

    public class Node implements TreeTableNode, Comparable<Node> {

        private List<Node> children = new ArrayList<BranchView.Node>();

        Branch branch;

        private Node parent;

        public Node(Branch branch) {
            this.branch = branch;
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
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return children.size() == 0;
        }

        @Override
        public Enumeration<? extends TreeTableNode> children() {
            return Collections.enumeration(children);
        }

        @Override
        public Object getValueAt(int column) {
            switch (column) {
                case 0:
                    String text = branch.getReason();
                    if (parent == null) {
                        text = GlobalResourcesManager.getString("BranchesRoot");
                        return text;
                    }
                    if (branch.getChildren().size() == 0) {
                        text += " ("
                                + GlobalResourcesManager
                                .getString("BranchEditable") + ")";
                    }
                    return text;
                case 1:
                    return timeFormat.format(branch.getCreationTime());
                case 2:
                    return branch.getModule();
                case 3: {
                    if (branch.getType() == 0)
                        return GlobalResourcesManager.getString("Branch.Main");
                    return GlobalResourcesManager.getString("Branch.Child");
                }

            }
            return null;
        }

        @Override
        public TreeTableNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Node getParent() {
            return parent;
        }

        @Override
        public boolean isEditable(int column) {
            return false;
        }

        @Override
        public void setValueAt(Object aValue, int column) {
        }

        @Override
        public Object getUserObject() {
            return branch;
        }

        @Override
        public void setUserObject(Object userObject) {
            this.branch = (Branch) userObject;
        }

        public void sort() {
            Collections.sort(children);
            for (Node node : children)
                node.sort();
        }

        @Override
        public int compareTo(Node o) {
            if (branch.getBranchId() < o.branch.getBranchId())
                return -1;
            if (branch.getBranchId() > o.branch.getBranchId())
                return 1;
            return 0;
        }

    }

    ;

    private class AddChildBranch extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 7924659449991534686L;

        public AddChildBranch() {
            putValue(ACTION_COMMAND_KEY, "Action.AddBranchChild");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/branch-down.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addChildBranch();
        }
    }

    ;

    private class ActivateBranch extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 7924659449991534686L;

        public ActivateBranch() {
            putValue(ACTION_COMMAND_KEY, "Action.ActivateBranch");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/branch-actual.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            Node node = (Node) table.getPathForRow(row).getLastPathComponent();
            engine.setActiveBranch(node.branch.getBranchId());
        }
    }

    ;

    private class BranchEditCommment extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 7924659449991534686L;

        public BranchEditCommment() {
            putValue(ACTION_COMMAND_KEY, "Action.EditBranchComment");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/edit-comment-branch.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            Node node = (Node) table.getPathForRow(row).getLastPathComponent();
            String s = (String) JOptionPane.showInputDialog(framework
                            .getMainFrame(), GlobalResourcesManager
                            .getString("BranchCreationReason"), GlobalResourcesManager
                            .getString("Action.EditBranchComment"),
                    JOptionPane.QUESTION_MESSAGE, null, null, node.branch
                            .getReason());
            if (s != null) {
                node.branch.setReason(s);
                engine.updateBranch(node.branch);
                branchModel.getBranchModelSupport().firePathChanged(
                        nodeToTreePath(node));
            }
        }

        private TreePath nodeToTreePath(Node node) {
            List<Node> list = new ArrayList<Node>();
            while (node != null) {
                list.add(0, node);
                node = node.getParent();
            }

            TreePath parentPath = new TreePath(list.toArray());
            return parentPath;
        }
    }

    ;

    @Override
    public Action[] getActions() {
        return new Action[]{activateBranch, addChildBranch, editCommment};
    }

    public void addChildBranch() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        Node node = (Node) table.getPathForRow(row).getLastPathComponent();
        String message = GlobalResourcesManager
                .getString("BranchCreationWarning");
        if (node.branch.getChildren().size() > 0)
            message = GlobalResourcesManager
                    .getString("BranchNodeCreationWarniong");
        if (JOptionPane.showConfirmDialog(framework.getMainFrame(), message,
                UIManager.getString("OptionPane.titleText"),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        String s = JOptionPane.showInputDialog(framework.getMainFrame(),
                GlobalResourcesManager.getString("BranchCreationReason"),
                GlobalResourcesManager.getString("BranchCreationReason"),
                JOptionPane.QUESTION_MESSAGE);
        if (s == null)
            return;
        int type = node.branch.getType() - 1;
        if (node.branch.getChildren().size() > 0) {
            type = getMaxType(type, (Node) branchModel.getRoot());
        }
        engine.createBranch(node.branch.getBranchId(), s, type + 1, "core");
    }

    private int getMaxType(int type, Node node) {
        if (node.branch.getType() > type)
            type = node.branch.getType();
        for (Node n : node.children) {
            int t = getMaxType(type, n);
            if (type < t)
                type = t;
        }
        return type;
    }

    @Override
    public JComponent createComponent() {
        this.branchModel = new BranchModel(createRoot(engine.getRootBranch()));

        this.table = new BranchTreeTable(branchModel, this);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        boolean b = table.getSelectedRow() >= 0;
                        Node node = null;
                        if (b) {
                            node = (Node) table.getPathForRow(
                                    table.getSelectedRow())
                                    .getLastPathComponent();
                            if (node.branch.getChildren().size() > 0)
                                addChildBranch.setEnabled(false);
                                //addChildBranch.putValue(Action.SMALL_ICON,
                                //		branchTree);
                            else
                                addChildBranch.setEnabled(true);
                            //addChildBranch.putValue(Action.SMALL_ICON,
                            //		branch);
                            ActionChangeEvent event = new ActionChangeEvent(
                                    new Action[]{addChildBranch, editCommment});
                            for (ActionChangeListener l : getActionChangeListeners())
                                l.actionsRemoved(event);
                            for (ActionChangeListener l : getActionChangeListeners())
                                l.actionsAdded(event);

                        } else
                            addChildBranch.setEnabled(false);

                        activateBranch.setEnabled(b
                                && node.branch.getBranchId() != actualBranch);
                        editCommment.setEnabled(b && node.parent != null);

                    }
                });

        table.setRootVisible(true);
        branchListener = new BranchAdapter() {

            @Override
            public void branchDeleted(BranchEvent event) {
                branchModel.setRoot(createRoot(engine.getRootBranch()));
            }

            @Override
            public void branchCreated(final BranchEvent event) {
                branchModel.setRoot(createRoot(engine.getRootBranch()));
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        table.expandAll();
                        for (int i = 0; i < table.getRowCount(); i++) {
                            Node n = (Node) table.getPathForRow(i)
                                    .getLastPathComponent();
                            if (n.branch.getBranchId() == event.getBranchId()) {
                                table.getSelectionModel().addSelectionInterval(
                                        i, i);
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public void branchActivated(BranchEvent event) {
                framework.propertyChanged(Commands.FULL_REFRESH);
                actualBranch = event.getBranchId();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        table.repaint();
                    }
                });
            }
        };
        engine.addBranchListener(branchListener);

        table.expandAll();
        actualBranch = engine.getActiveBranch();
        return new JScrollPane(table);
    }

    protected Node createRoot(Branch rootBranch) {
        Node root = new Node(rootBranch);
        for (Branch b : rootBranch.getChildren()) {
            if (b.getType() == root.branch.getType()) {
                Node e = new Node(b);
                e.parent = root;
                root.children.add(e);
                addNode(root, b);
            } else {
                Node e = createRoot(b);
                Node p = null;
                Node x = root;
                while (x != null) {
                    for (Node n : x.children)
                        if (n.branch.getBranchId() == b.getParentBranchId())
                            p = n;
                    if (p != null)
                        break;
                    x = x.parent;
                }
                if (p == null
                        && root.branch.getBranchId() == b.getParentBranchId())
                    p = root;
                e.parent = p;
                p.children.add(e);
                for (int i = e.children.size() - 1; i >= 0; i--) {
                    Node n = e.children.get(i);
                    if (n.branch.getType() == b.getType()) {
                        n.parent.children.remove(n);
                        n.parent = p;
                        p.children.add(n);
                    }
                }
            }
        }
        return root;
    }

    private void addNode(Node root, Branch br) {
        for (Branch b : br.getChildren()) {
            if (b.getType() == root.branch.getType()) {
                Node e = new Node(b);
                e.parent = root;
                root.children.add(e);
                addNode(root, b);
            } else {
                Node e = createRoot(b);
                Node p = null;
                for (Node n : root.children)
                    if (n.branch.getBranchId() == b.getParentBranchId())
                        p = n;
                e.parent = p;
                p.children.add(e);
                for (int i = e.children.size() - 1; i >= 0; i--) {
                    Node n = e.children.get(i);
                    if (n.branch.getType() == b.getType()) {
                        n.parent.children.remove(n);
                        n.parent = p;
                        p.children.add(n);
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        super.close();
        engine.removeBranchListener(branchListener);
    }

    private class BranchModel extends DefaultTreeTableModel {
        public BranchModel(TreeTableNode root) {
            super(root);
        }

        @Override
        public void setRoot(TreeTableNode root) {
            Node n = (Node) root;
            n.sort();
            super.setRoot(root);
        }

        @SuppressWarnings("unused")
        public Node findNode(long branchId) {
            return findNode(branchId, (Node) root);
        }

        private Node findNode(long branchId, Node node) {
            if (node.branch.getBranchId() == branchId)
                return node;
            for (Node n : node.children) {
                Node t = findNode(branchId, n);
                if (t != null)
                    return t;
            }
            return null;
        }

        public TreeModelSupport getBranchModelSupport() {
            return modelSupport;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return GlobalResourcesManager.getString("BranchCreationReason");
                case 1:
                    return GlobalResourcesManager.getString("BranchCreationTime");

                default:
                    break;
            }
            return super.getColumnName(column);
        }
    }

    ;

    @Override
    public String getId() {
        return "BranchesList";
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.Qualifiers";
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.WEST;
    }

    public long getActualBranch() {
        return actualBranch;
    }
}
