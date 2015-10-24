package com.ramussoft.pb.frames.docking;

public interface View {
    String[] getEnableActions();

    /**
     * Повердає індекс даного вікна.
     *
     * @return Індекс вікна.
     */

    public int getId();

    public ViewPanel getViewPanel();

    String getTitle();

    boolean isShowing();

}
