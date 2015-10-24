package com.ramussoft.gui.core.simple;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.View;

public class TabFrame extends DFrame {

    /**
     *
     */
    private static final long serialVersionUID = 5775356454361768463L;

    private String areaId;

    private TabView view;

    private Area area;

    private int index;

    public TabFrame(Control control, Area area, TabView view) {
        super(control);
        this.area = area;
        JComponent component = view.createComponent();
        this.title = view.getTitle();
        add(component, BorderLayout.CENTER);
        index = area.addTabFrame(this);
        this.view = view;
    }

    @Override
    public String getId() {
        return areaId;
    }

    @Override
    public void setTitleText(String newTitle) {
        super.setTitleText(newTitle);
        for (int i = 0; i < area.getTabCount(); i++)
            if (area.getComponentAt(i) == this)
                index = i;
        area.setTabTitle(index, newTitle);
    }

    public void setRemoveOnClose(boolean b) {
    }

    public void setCloseable(boolean b) {
    }

    @Override
    public View getView() {
        return view;
    }

}
