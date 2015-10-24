package com.ramussoft.idef0;

import java.util.List;
import java.util.Vector;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

public class SectorCorrector {

    public void run(Engine engine, AccessRules accessRules) {
        ((Journaled) engine).startUserTransaction();
        log("Loading data");
        List<Qualifier> list = IDEF0Plugin.getBaseQualifiers(engine);
        for (Qualifier q : list) {
            DataPlugin dataPlugin = NDataPluginFactory.getDataPlugin(q, engine,
                    accessRules);
            Vector<Row> v = dataPlugin.getRecChilds(dataPlugin
                    .getBaseFunction(), true);
            for (Row r : v) {
                if (r.getChildCount() == 0) {
                    Function function = (Function) r;
                    MovingArea area = new MovingArea(dataPlugin, function);
                    area.setDataPlugin(dataPlugin);
                    SectorRefactor sr = area.getRefactor();
                    sr.loadFromFunction(function, false);
                    while (sr.getSectorsCount() > 0) {
                        sr.getSector(0).remove();
                    }
                    sr.saveToFunction();
                    log("Function " + r + " clean");
                }
            }
            for (Row r : v) {
                if (r.getChildCount() != 0) {
                    Function function = (Function) r;
                    MovingArea area = new MovingArea(dataPlugin, function);
                    area.setDataPlugin(dataPlugin);
                    SectorRefactor sr = area.getRefactor();
                    sr.loadFromFunction(function, false);
                    for (int i = 0; i < sr.getSectorsCount(); i++) {
                        PaintSector ps = sr.getSector(i);
                        if ((ps.getSector().getStart().getFunction() != null)
                                && (ps.getSector().getStart().getFunction()
                                .getChildCount() == 0))
                            sr.createSectorOnIn(ps, true);
                        if ((ps.getSector().getEnd().getFunction() != null)
                                && (ps.getSector().getEnd().getFunction()
                                .getChildCount() == 0))
                            sr.createSectorOnIn(ps, false);
                    }
                    log("Function " + r + " done");
                }
            }
        }
        ((Journaled) engine).commitUserTransaction();
    }

    public void log(String string) {
        System.out.println(string);
    }

}
