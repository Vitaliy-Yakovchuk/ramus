package com.ramussoft.pb.dmaster;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.types.FRectangle;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.frames.IDEFPanel;
import com.ramussoft.pb.idef.visual.MovingArea;

public class SimpleTemplate extends AbstractTemplate {

    protected int count;

    public SimpleTemplate(final int count) {
        this.count = count;
    }

    public SimpleTemplate() {
        this(4);
    }

    public void createChilds(final Function function,
                             final DataPlugin dataPlugin) {
        final MovingArea movingArea = new MovingArea(dataPlugin);
        movingArea.setDataPlugin(dataPlugin);
        movingArea.setActiveFunction(function);
        movingArea.setArrowAddingState();

        assert count > 0;
        final double x = 80;// Відступ зправа/зліва
        final double y = 80;// Відступ знизу/звурху
        final double width = movingArea.MOVING_AREA_WIDTH - x * 2
                - IDEFPanel.DEFAULT_WIDTH;
        final double height = movingArea.CLIENT_HEIGHT - y * 2
                - IDEFPanel.DEFAULT_HEIGHT;
        for (int i = 0; i < count; i++) {
            final Function f = (Function) dataPlugin.createRow(function, true);
            final FRectangle rect = new FRectangle(f.getBounds());
            rect.setX(x + width / (count - 1) * i);
            rect.setY(y + height / (count - 1) * i);
            f.setBounds(rect);
        }
    }

    public void setCount(final int count) {
        assert count > 0;
        this.count = count;
        dataPlugin = null;
    }

    @Override
    public String toString() {
        return ResourceLoader.getString("Template.Simple");
    }

}
