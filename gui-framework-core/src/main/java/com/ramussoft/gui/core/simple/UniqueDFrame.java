package com.ramussoft.gui.core.simple;

import java.awt.BorderLayout;

import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.View;

public class UniqueDFrame extends DFrame {

    /**
     *
     */
    private static final long serialVersionUID = -3831772871665124869L;

    private String id;

    private UniqueView view;

    public UniqueDFrame(Control control, String id, String title,
                        UniqueView view) {
        super(control);
        this.id = id;
        this.view = view;
        this.title = title;
        this.add(view.createComponent(), BorderLayout.CENTER);
    }

    public String getUniqueId() {
        return getId();
    }

    public void setCloseable(boolean b) {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public View getView() {
        return view;
    }
}
