package com.ramussoft.gui.eval;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.eval.Eval;
import com.ramussoft.eval.MetaValue;
import com.ramussoft.eval.UnknownValuesException;
import com.ramussoft.eval.Util;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class SetFormulaDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 2449085465622565854L;

    private FormulaEditor editor;

    private Engine engine;

    private CalculateInfo info;

    public SetFormulaDialog(JDialog dialog, GUIFramework framework, CalculateInfo info) {
        super(dialog, true);
        this.info = info;
        this.engine = framework.getEngine();
        this.setTitle(GlobalResourcesManager.getString("Action.SetFormula"));
        editor = new FormulaEditor(framework, info);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(editor, BorderLayout.CENTER);
        this.setMainPane(panel);
        this.pack();
        this.setMinimumSize(getSize());
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2,
                getSize().height);
        centerDialog();
        Options.loadOptions(this);
    }

    public SetFormulaDialog(GUIFramework framework, CalculateInfo info) {
        super(framework.getMainFrame());
        setModal(false);
        this.info = info;
        this.engine = framework.getEngine();
        this.setTitle(GlobalResourcesManager.getString("Action.SetFormula"));
        editor = new FormulaEditor(framework, info);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(editor, BorderLayout.CENTER);
        this.setMainPane(panel);
        this.pack();
        this.setMinimumSize(getSize());
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2,
                getSize().height);
        centerDialog();
        Options.loadOptions(this);
    }

    @Override
    protected void onOk() {
        if (editor.getText().equals("")) {
            info.setFormula(null);
            ((Journaled) engine).startUserTransaction();
            engine.setCalculateInfo(info);
            ((Journaled) engine).commitUserTransaction();
            super.onOk();
            return;
        }

        Util utils = Util.getUtils(engine);
        try {
            try {
                info.setFormula(utils.compile(editor.getText(), engine
                        .getElement(info.getElementId()), engine
                        .getAttribute(info.getAttributeId())));
            } catch (UnknownValuesException e) {
                StringBuffer sb = new StringBuffer();
                sb.append("<html><body>");
                for (String value : e.getValues()) {
                    sb.append(MessageFormat.format(GlobalResourcesManager
                            .getString("Eval.UnknowValue"), value));
                    sb.append("<br>");
                }
                sb.append("</body></html>");
                JOptionPane.showMessageDialog(null, sb.toString());
            }
            Eval eval = new Eval(info.getFormula());
            List<String> rec = new ArrayList<String>();

            for (String function : eval.getFunctions())
                if (!utils.isFunctionExists(function)) {
                    JOptionPane.showMessageDialog(null,
                            MessageFormat
                                    .format(GlobalResourcesManager
                                                    .getString("Eval.UnknowFunction"),
                                            function));
                    return;
                }

            rec.add(utils.toValue(info.getElementId(), info.getAttributeId()));
            if (recCheck(rec, eval, utils)) {
                JOptionPane.showMessageDialog(null, GlobalResourcesManager
                        .getString("Recursive.Link"));
            } else {
                ((Journaled) engine).startUserTransaction();
                engine.setCalculateInfo(info);
                ((Journaled) engine).commitUserTransaction();
                super.onOk();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(
                    GlobalResourcesManager.getString("Eval.Error"), editor
                            .getText()));
            return;
        }

    }

    private boolean recCheck(List<String> rec, Eval eval, Util utils) {
        for (String value : eval.getValues()) {
            if (rec.indexOf(value) >= 0)
                return true;
            MetaValue metaValue = utils.toMetaValue(value);
            CalculateInfo info = engine.getCalculateInfo(metaValue
                    .getElementId(), metaValue.getAttributeId());
            if (info != null) {
                Eval eval2 = new Eval(info.getFormula());
                rec.add(utils.toValue(info.getElementId(), info
                        .getAttributeId()));
                if (recCheck(rec, eval2, utils))
                    return true;
                rec.remove(rec.size() - 1);
            }
        }
        return false;
    }

    @Override
    public void setVisible(boolean b) {
        if (!b) {
            Options.saveOptions(SetFormulaDialog.this);
            editor.close();
        }
        super.setVisible(b);
    }
}
