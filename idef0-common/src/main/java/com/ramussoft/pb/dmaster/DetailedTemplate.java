package com.ramussoft.pb.dmaster;

import static com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM;
import static com.ramussoft.pb.idef.visual.MovingPanel.TOP;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.visual.MovingArea;

public class DetailedTemplate extends AbstractClassicTemplate {

    @Override
    public synchronized void createChilds(final Function function,
                                          final DataPlugin dataPlugin) {
        final MovingArea movingArea = new MovingArea(dataPlugin);
        movingArea.setDataPlugin(dataPlugin);
        movingArea.setActiveFunction(function);
        movingArea.setArrowAddingState();
        super.createChilds(function, dataPlugin, movingArea);

        for (int num = 0; num < count; num++) {
            createSimpleArrow(function, movingArea, num, TOP);
            createSimpleArrow(function, movingArea, num, BOTTOM);
        }
        movingArea.getRefactor().saveToFunction();
    }

    @Override
    public String toString() {
        return ResourceLoader.getString("Template.Detailed");
    }

}
