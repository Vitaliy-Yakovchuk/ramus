package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.DeleteStatusList;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.table.StatusMessageFormat;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.RowFactory;
import com.ramussoft.pb.data.RowSetClass;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.frames.components.RowFindPanel;

/**
 * Клас - діалогове вікно для роботи з під’єднаними до сектора елементами
 * класифікатора.
 *
 * @author ZDD
 */

public class AlternativeArrowDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JPanel jPanel = null;

    private JScrollPane jScrollPane = null;

    private boolean isOk;

    private final StreamModel streamModel;

    private JList jList = null;

    private RowFindPanel rowFindPanel = null;

    private DataPlugin dataPlugin;

    private GUIFramework framework;

    private DeleteElementAction deleteElementAction;

    private class StreamModel extends AbstractListModel {

        Stream[] data = new Stream[0];

        private static final long serialVersionUID = 1L;

        public Stream[] getData() {
            return data;
        }

        public int getSize() {
            return data.length;
        }

        public Object getElementAt(final int index) {
            return data[index];
        }

        private Stream[] getStreams(final Vector v1) {
            final Vector<Stream> v = new Vector<Stream>();
            for (int i = 0; i < v1.size(); i++) {
                final Stream stream = (Stream) v1.get(i);
                if (!stream.isEmptyName())
                    v.add(stream);

            }
            final Stream[] res = new Stream[v.size()];
            for (int i = 0; i < res.length; i++)
                res[i] = v.get(i);
            return res;
        }

        public void refresh() {
            final Stream[] data = this.data;
            this.data = getStreams(dataPlugin.getRecChilds(
                    dataPlugin.getBaseStream(), true));
            if (data.length < this.data.length)
                fireIntervalAdded(this, data.length, this.data.length);
            else if (data.length > this.data.length)
                fireIntervalRemoved(this, this.data.length, data.length);
            RowFactory.sortByTitle(this.data);
            fireContentsChanged(this, 0, this.data.length);
        }

        public StreamModel() {
            super();
            refresh();
        }
    }

    ;

    public Stream showModal() {
        isOk = false;
        streamModel.refresh();
        Options.saveOptions("arrow_rows_dialog", this);
        setVisible(true);
        if (isOk) {
            return (Stream) jList.getSelectedValue();
        }
        return null;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.setPreferredSize(new java.awt.Dimension(200, 200));
            jPanel.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
            jPanel.add(getRowFindPanel(), BorderLayout.SOUTH);
        }
        return jPanel;
    }

    public AlternativeArrowDialog(DataPlugin dataPlugin, GUIFramework framework) {
        super();
        this.dataPlugin = dataPlugin;
        this.framework = framework;
        streamModel = new StreamModel();
        initialize();
        ResourceLoader.setJComponentsText(this);
        pack();
        setMinimumSize(getSize());
        Options.loadOptions("arrow_rows_dialog", this);
    }

    public AlternativeArrowDialog(final JDialog dialog, DataPlugin dataPlugin,
                                  GUIFramework framework) {
        super(dialog);
        this.dataPlugin = dataPlugin;
        this.framework = framework;
        streamModel = new StreamModel();
        initialize();
        ResourceLoader.setJComponentsText(this);
        final Dimension d = getSize();
        pack();
        setMinimumSize(getSize());
        setSize(d);
        setLocationRelativeTo(dialog);
        Options.loadOptions("arrow_rows_dialog", this);
    }

    private void initialize() {
        getMainPanel().add(getJPanel(), java.awt.BorderLayout.CENTER);
        setTitle("select_arrow");
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    @Override
    protected void onOk() {
        if (jList.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this,
                    ResourceLoader.getString("select_arrow_first"));
            return;
        }
        isOk = true;
        super.onOk();
    }

    @Override
    protected void onCancel() {
        super.onCancel();
    }

    /**
     * This method initializes jList
     *
     * @return javax.swing.JList
     */
    private JList getJList() {
        if (jList == null) {
            jList = new JList();
            jList.setModel(streamModel);
            jList.getInputMap()
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                            InputEvent.CTRL_MASK), "showFindPanel");
            jList.getActionMap().put("showFindPanel", new AbstractAction() {
                public void actionPerformed(final ActionEvent e) {
                    rowFindPanel.setVisible(true);
                    rowFindPanel.getJTextField().requestFocus();
                }
            });
            jList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent event) {
                    if (event.getClickCount() > 1
                            && event.getButton() == MouseEvent.BUTTON1)
                        onOk();
                }
            });

            jList.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {

                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            deleteElementAction.setEnabled(jList
                                    .getSelectedIndex() >= 0);

                        }
                    });

            JPopupMenu menu = new JPopupMenu();

            deleteElementAction = new DeleteElementAction();
            menu.add(deleteElementAction);

            jList.setComponentPopupMenu(menu);
        }
        return jList;
    }

    protected class DeleteElementAction extends AbstractAction {

        public DeleteElementAction() {
            //this.putValue(ACTION_COMMAND_KEY, GlobalResourcesManager.getString("DeleteElement"));
            this.putValue(Action.NAME,
                    GlobalResourcesManager.getString("DeleteElement"));

            setEnabled(false);
        }

        /**
         *
         */
        private static final long serialVersionUID = -5284012805486357491L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int i = jList.getSelectedIndex();
            if (i < 0)
                return;
            List<Stream> rows = new ArrayList<Stream>();

            for (int j : jList.getSelectedIndices()) {
                rows.add(streamModel.data[j]);
            }

            long[] ls = new long[rows.size()];
            for (int j = 0; j < ls.length; j++)
                ls[j] = rows.get(j).getElement().getId();

            DeleteStatusList list = framework.getAccessRules()
                    .getElementsDeleteStatusList(ls);
            if (list.size() > 0) {
                if (!StatusMessageFormat.deleteElements(list, null, framework))
                    return;
            } else {
                if (JOptionPane.showConfirmDialog(jList, GlobalResourcesManager
                                .getString("DeleteActiveElementsDialog.Warning"),
                        GlobalResourcesManager
                                .getString("ConfirmMessage.Title"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
            }

            ((Journaled) dataPlugin.getEngine()).startUserTransaction();

            for (long id : ls) {
                dataPlugin.getEngine().deleteElement(id);
            }

            ((Journaled) dataPlugin.getEngine()).commitUserTransaction();
            streamModel.refresh();
        }
    }

    /**
     * This method initializes rowFindPanel
     *
     * @return com.dsoft.pb.frames.elements.RowFindPanel
     */
    private RowFindPanel getRowFindPanel() {
        if (rowFindPanel == null) {
            rowFindPanel = new RowFindPanel() {
                @Override
                public boolean find(final String text, final boolean wordsOrder) {
                    return find(-1, text, wordsOrder);
                }

                @Override
                public boolean findNext(final String text,
                                        final boolean wordsOrder) {
                    return find(jList.getSelectedIndex(), text, wordsOrder);
                }

                private boolean find(final int selectedIndex,
                                     final String text, final boolean wordsOrder) {
                    final Stream[] streams = streamModel.getData();
                    for (int i = selectedIndex + 1; i < streams.length; i++) {
                        final Stream stream = streams[i];
                        if (select(text, wordsOrder, i, stream))
                            return true;
                    }
                    for (int i = 0; i < selectedIndex; i++) {
                        final Stream stream = streams[i];
                        if (select(text, wordsOrder, i, stream))
                            return true;
                    }
                    return false;
                }

                private boolean select(final String text,
                                       final boolean wordsOrder, final int i,
                                       final Stream stream) {
                    if (RowSetClass.isStartSame(stream, text, wordsOrder)) {
                        jList.setSelectedIndex(i);
                        jList.ensureIndexIsVisible(i);
                        return true;
                    }
                    return false;
                }

                @Override
                public void setVisible(final boolean aFlag) {
                    super.setVisible(aFlag);
                    Options.setBoolean("showFindStreamPanel", aFlag);
                }
            };
            boolean v = Options.getBoolean("showFindStreamPanel", true);
            if (!v)
                rowFindPanel.setVisible(false);
            else
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        rowFindPanel.getJTextField().requestFocus();
                    }

                });
        }
        return rowFindPanel;
    }
}
