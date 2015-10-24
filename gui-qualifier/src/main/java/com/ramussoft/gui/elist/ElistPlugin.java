package com.ramussoft.gui.elist;

import com.ramussoft.common.Attribute;
import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.table.TabbedTableView;

public class ElistPlugin extends AbstractViewPlugin implements Commands {

    public static final String OPEN_ELEMENT_LIST = "OpenElementList";

    public static final String OPEN_ELEMENT_LIST_IN_TABLE = "OpenElementListInTable";

    @Override
    public void setFramework(final GUIFramework framework) {
        super.setFramework(framework);

        framework.addActionListener(OPEN_ELEMENT_LIST, new ActionListener() {

            @Override
            public void onAction(final ActionEvent event) {

                if (framework.openView(event))
                    return;

                Attribute attribute = (Attribute) event.getValue();

                ElistTabView elistTabView = new ElistTabView(framework,
                        attribute) {
                    @Override
                    public void close() {
                        super.close();
                        TabbedEvent tEvent = new TabbedEvent("TabbedTableView",
                                this);
                        tabRemoved(tEvent);
                    }
                };

                TabbedEvent tEvent = new TabbedEvent(
                        TabbedTableView.MAIN_TABBED_VIEW, elistTabView);
                tabCreated(tEvent);
            }
        });

        framework.addActionListener(OPEN_ELEMENT_LIST_IN_TABLE,
                new ActionListener() {

                    @Override
                    public void onAction(final ActionEvent event) {

                        if (framework.openView(event))
                            return;

                        Attribute attribute = (Attribute) event.getValue();

                        ElistTableTabView elistTabView = new ElistTableTabView(
                                framework, attribute) {
                            @Override
                            public void close() {
                                super.close();
                                TabbedEvent tEvent = new TabbedEvent(
                                        "TabbedTableView", this);
                                tabRemoved(tEvent);
                            }
                        };

                        TabbedEvent tEvent = new TabbedEvent(
                                TabbedTableView.MAIN_TABBED_VIEW, elistTabView);
                        tabCreated(tEvent);
                    }
                });
    }

    @Override
    public String getName() {
        return "Element list";
    }

    @Override
    public UniqueView[] getUniqueViews() {
        return new UniqueView[]{new ElistView(framework)};
    }

}
