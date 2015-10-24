package com.ramussoft.gui.eval;

import javax.swing.Action;

import com.ramussoft.gui.common.AbstractQualifierSetupPlugin;
import com.ramussoft.gui.common.QualifierSetupEditor;
import com.ramussoft.gui.qualifier.table.ElementActionPlugin;
import com.ramussoft.gui.qualifier.table.TableTabView;

public class EvalGUIPlugin extends AbstractQualifierSetupPlugin implements
        ElementActionPlugin {

    @Override
    public QualifierSetupEditor getSetupEditor() {
        return new EvalQualifierSetupEditor();
    }

    @Override
    public String getName() {
        return "EvalQualifierSetup";
    }

    @Override
    public Action[] getActions(TableTabView tableView) {
        return new Action[]{tableView.getRecalculateAction(),
                tableView.getSetFormulaAction()};
    }

}
