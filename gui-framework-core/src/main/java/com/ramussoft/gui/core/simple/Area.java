package com.ramussoft.gui.core.simple;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ramussoft.gui.common.GlobalResourcesManager;

public class Area extends CloseableTabbedPane {

    /**
     *
     */
    private static final long serialVersionUID = -214061889110466594L;

    private String id;

    private JPopupMenu menu;

    private Action closeTab = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 3835087856752030808L;

        {
            putValue(NAME,
                    GlobalResourcesManager.getString("close"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = getSelectedIndex();
            if (fireCloseTab(index))
                removeTabAt(getSelectedIndex());
        }
    };

    private Action closeAll = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 3835087856752030808L;

        {
            putValue(NAME,
                    GlobalResourcesManager.getString("CloseAllTabs"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int index = getTabCount() - 1; index >= 0; index--) {
                if (fireCloseTab(index))
                    removeTabAt(index);
            }
        }
    };

    private Action closeOthers = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 3835087856752030808L;

        {
            putValue(NAME,
                    GlobalResourcesManager.getString("CloseOthersTabs"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int s = getSelectedIndex();
            for (int index = getTabCount() - 1; index >= 0; index--) {
                if ((s != index) && (fireCloseTab(index)))
                    removeTabAt(index);
            }
        }
    };

    public Area(String id) {
        this.id = id;
        menu = new JPopupMenu();

        menu.add(closeTab);
        menu.add(closeOthers);
        menu.add(closeAll);

        addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = getSelectedIndex();
                if (selectedIndex >= 0)
                    getComponentAt(selectedIndex).requestFocus();
            }
        });
    }

    public String getUniqueId() {
        return id;
    }

    public int addTabFrame(TabFrame tabFrame) {
        addTab(tabFrame.getTitleText(), tabFrame);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                revalidate();
                repaint();
                int c = getTabCount();
                setSelectedIndex(c - 1);

            }
        });
        int i = getTabCount() - 1;
        setTabComponentAt(i, new ButtonTabComponent(this, menu));
        closeOthers.setEnabled(getTabCount() > 1);
        return i;
    }

    @Override
    public boolean fireCloseTab(int tabIndexToClose) {
        if (super.fireCloseTab(tabIndexToClose)) {
            closeOthers.setEnabled(getTabCount() > 1);
            return true;
        }
        return false;
    }

    public void setTabTitle(int index, String newTitle) {
        setTitleAt(index, newTitle);
    }

}
