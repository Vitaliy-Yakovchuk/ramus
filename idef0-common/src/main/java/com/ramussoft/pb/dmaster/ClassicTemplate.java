package com.ramussoft.pb.dmaster;

import static com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM;
import static com.ramussoft.pb.idef.visual.MovingPanel.TOP;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

public class ClassicTemplate extends AbstractClassicTemplate {

    private static final int POS = 40;

    private PaintSector top = null;

    private PaintSector bottom = null;

    @Override
    public synchronized void createChilds(final Function function,
                                          final DataPlugin dataPlugin) {
        final MovingArea movingArea = new MovingArea(dataPlugin);
        movingArea.setDataPlugin(dataPlugin);
        movingArea.setActiveFunction(function);
        movingArea.setArrowAddingState();
        super.createChilds(function, dataPlugin, movingArea);

        int num = count / 2;
        if (2 * num == count)
            num--;
        createSimpleArrow(function, movingArea, num);

        PaintSector s;
        final Function f = (Function) function.getChildAt(num);

        createFrom(movingArea, f, top);
        createInPoint(movingArea, TOP, (Function) function
                .getChildAt(count - 1));

        s = movingArea.getRefactor().getSector();

        for (int i = count - 2; i > num; i--) {
            final Function fS = (Function) function.getChildAt(i);
            createFrom(movingArea, fS, s, getX(fS), POS);
            createInPoint(movingArea, TOP, fS);
        }

        createFrom(movingArea, f, bottom);
        createInPoint(movingArea, BOTTOM, (Function) function
                .getChildAt(count - 1));

        s = movingArea.getRefactor().getSector();

        for (int i = count - 2; i > num; i--) {
            final Function fS = (Function) function.getChildAt(i);
            createFrom(movingArea, fS, s, getX(fS), movingArea.CLIENT_HEIGHT
                    - POS);
            createInPoint(movingArea, BOTTOM, fS);
        }

        if (num > 0) {

            createFrom(movingArea, f, top, getX(f), POS - 1);
            createInPoint(movingArea, TOP, (Function) function.getChildAt(0));

            s = movingArea.getRefactor().getSector();

            for (int i = 1; i < num; i++) {
                final Function fS = (Function) function.getChildAt(i);
                createFrom(movingArea, fS, s, getX(fS), POS);
                createInPoint(movingArea, TOP, fS);
            }

            createFrom(movingArea, f, bottom);
            createInPoint(movingArea, BOTTOM, (Function) function.getChildAt(0));

            s = movingArea.getRefactor().getSector();

            for (int i = 1; i < num; i++) {
                final Function fS = (Function) function.getChildAt(i);
                createFrom(movingArea, fS, s, getX(fS),
                        movingArea.CLIENT_HEIGHT - POS);
                createInPoint(movingArea, BOTTOM, fS);
            }

        }
        movingArea.getRefactor().saveToFunction();
    }

    private void createFrom(final MovingArea movingArea, final Function f,
                            final PaintSector paintSector) {
        createFrom(movingArea, f, paintSector, getX(f),
                paintSector == top ? POS : movingArea.CLIENT_HEIGHT - POS);
    }

    private void createFrom(final MovingArea movingArea, final Function f,
                            final PaintSector paintSector, final double x, final double y) {
        final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
        pp.type = SectorRefactor.TYPE_START;
        pp.pin = paintSector.getPin(0);
        pp.x = x;
        pp.y = y;
        movingArea.getRefactor().setPoint(pp);
        movingArea.doSector();
    }

    private void createSimpleArrow(final Function function, final MovingArea movingArea,
                                   final int num) {
        createSimpleArrow(function, movingArea, num, TOP);
        top = movingArea.getRefactor().getSector();
        createSimpleArrow(function, movingArea, num, BOTTOM);
        bottom = movingArea.getRefactor().getSector();
    }

    @Override
    public String toString() {
        return ResourceLoader.getString("Template.Classic");
    }
}
