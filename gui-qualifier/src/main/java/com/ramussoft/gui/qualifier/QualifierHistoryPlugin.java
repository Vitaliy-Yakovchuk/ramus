package com.ramussoft.gui.qualifier;

import java.awt.event.ActionEvent;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractQualifierSetupPlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.QualifierSetupEditor;
import com.ramussoft.gui.qualifier.table.ElementActionPlugin;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.TableTabView;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

public class QualifierHistoryPlugin extends AbstractQualifierSetupPlugin
        implements ElementActionPlugin {

    @Override
    public QualifierSetupEditor getSetupEditor() {
        return new QualifierHistorySetupEditor();
    }

    @Override
    public String getName() {
        return "HistoryQualifierSetup";
    }

    @Override
    public Action[] getActions(TableTabView tableView) {
        return new Action[]{getHistoryAction(tableView)};
    }

    private Action getHistoryAction(final TableTabView tableView) {
        Action res = (Action) tableView.getTag().get("HistoryAction");

        if (res == null) {
            final Action action = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = 6587512715104944731L;

                {
                    putValue(ACTION_COMMAND_KEY, "Action.ShowHistory");
                    putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/history.png")));
                    setEnabled(false);
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    RowTreeTable table = tableView.getComponent().getTable();
                    Row row;
                    if ((table.getSelectedColumn() >= 0)
                            && (table.getSelectedNode() != null)
                            && ((row = table.getSelectedNode().getRow()) != null)) {
                        int c = table.convertColumnIndexToModel(table
                                .getSelectedColumn());
                        Attribute attribute = table.getRowSet().getAttributes()[c];
                        showHistory(tableView, row.getElement(), attribute);
                    }

                }
            };
            res = action;
            tableView.getTag().put("HistoryAction", res);
            tableView.getComponent().getTable().addSelectionListener(
                    new SelectionListener() {

                        @Override
                        public void changeSelection(SelectionEvent event) {
                            RowTreeTable table = tableView.getComponent()
                                    .getTable();
                            Row row;
                            if ((table.getSelectedColumn() >= 0)
                                    && (table.getSelectedNode() != null)
                                    && ((row = table.getSelectedNode().getRow()) != null)) {
                                int c = table.convertColumnIndexToModel(table
                                        .getSelectedColumn());
                                Attribute attribute = table.getRowSet()
                                        .getAttributes()[c];
                                action.setEnabled(StandardAttributesPlugin
                                        .hasHistory(tableView.getFramework()
                                                        .getEngine(), row.getElement(),
                                                attribute));

                            } else {
                                action.setEnabled(false);
                            }
                        }
                    });
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    protected void showHistory(TableTabView tableView, Element element,
                               Attribute attribute) {
        GUIFramework framework = tableView.getFramework();
        Hashtable<Element, Hashtable<Attribute, HistoryDialog>> h = (Hashtable<Element, Hashtable<Attribute, HistoryDialog>>) framework
                .get("HistoryDialogs");
        if (h == null) {
            h = new Hashtable<Element, Hashtable<Attribute, HistoryDialog>>();
            framework.put("HistoryDialogs", h);
        }
        Hashtable<Attribute, HistoryDialog> h1 = h.get(element);
        if (h1 == null) {
            h1 = new Hashtable<Attribute, HistoryDialog>();
            h.put(element, h1);
        }

        HistoryDialog hd = h1.get(attribute);
        if (hd == null) {
            hd = new HistoryDialog(framework, this, element, attribute);
            h1.put(attribute, hd);
        }
        hd.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    public void windowClosed(GUIFramework framework, Element element,
                             Attribute attribute) {
        Hashtable<Element, Hashtable<Attribute, HistoryDialog>> h = (Hashtable<Element, Hashtable<Attribute, HistoryDialog>>) framework
                .get("HistoryDialogs");
        Hashtable<Attribute, HistoryDialog> h1 = h.get(element);
        h1.remove(attribute);
        if (h1.size() == 0)
            h.remove(element);
    }

}
