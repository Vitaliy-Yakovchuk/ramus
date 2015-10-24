package com.ramussoft.pb.frames.docking;

import javax.swing.Icon;

public class DefaultView implements View {

    private final int id;

    private final ViewPanel viewPanel;

    /**
     * Визначення подій, які можуть оброблятись диним вікном.
     *
     * @return Масив назв подій, які можуть оброблятись вікно.
     */

    public String[] getEnableActions() {
        return viewPanel.getEnableActions();
    }

    public DefaultView(final String title, final Icon icon, final ViewPanel component, final int id) {
        this.id = id;
        viewPanel = component;
    }

    /**
     * Повердає індекс даного вікна.
     *
     * @return Індекс вікна.
     */

    public int getId() {
        return id;
    }

    public ViewPanel getViewPanel() {
        return viewPanel;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public boolean isShowing() {
        return false;
    }
}
