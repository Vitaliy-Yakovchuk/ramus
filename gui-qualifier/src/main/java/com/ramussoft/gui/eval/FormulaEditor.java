package com.ramussoft.gui.eval;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.eval.Util;
import com.ramussoft.gui.attribute.AttributeEditorView.ElementAttribute;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;

import static com.ramussoft.eval.Eval.isLetterOrDigitOr_;
import static com.ramussoft.eval.Eval.OPERATIONS;

public class FormulaEditor extends JTextField implements Commands {

    /**
     *
     */
    private static final long serialVersionUID = -5894495311745016236L;

    private GUIFramework framework;

    private CalculateInfo calculateInfo;

    private Engine engine;

    private Element element;

    private ActionListener listener = new ActionListener() {
        @Override
        public void onAction(ActionEvent event) {
            if (isValueSetting()) {
                String value = createValue((ElementAttribute) event.getValue());
                if (value == null)
                    return;
                int left = getCaretPosition();
                String text = getText();
                int right = left;
                left--;
                if (left < 0)
                    left++;
                if (text.length() > 0) {
                    if (OPERATIONS.indexOf(text.charAt(left)) < 0) {
                        while ((left >= 0) && (text.charAt(left) != '['))
                            left--;
                        while ((right < text.length())
                                && (text.charAt(right) != ']'))
                            right++;
                    } else
                        left++;
                }
                if (left >= text.length())
                    left = text.length();
                if (left < 0)
                    left = 0;
                if (right > text.length())
                    right = text.length();
                if (right < 0)
                    right = 0;
                select(left, right + 1);
                replaceSelection(value);
                setCaretPosition(left + value.length());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        requestFocus();
                                    }
                                });
                            }
                        });
                    }
                });

            }
        }
    };

    public FormulaEditor(GUIFramework framework, CalculateInfo calculateInfo) {
        this.framework = framework;
        this.calculateInfo = calculateInfo;
        this.engine = framework.getEngine();
        this.element = engine.getElement(calculateInfo.getElementId());
        if (calculateInfo.getFormula() != null) {
            Util utils = Util.getUtils(engine);
            this.setText(utils.decompile(calculateInfo.getFormula(), engine
                    .getElement(calculateInfo.getElementId())));
        }
        framework.addActionListener(ACTIVATE_ATTRIBUTE, listener);
        framework.addActionListener(ACTIVATE_TABLE_ATTRIBUTE, listener);
    }

    protected String createValue(ElementAttribute value) {
        String createUserValue = createUserValue(value);
        if (createUserValue == null)
            return null;
        return Util.toCanonicalValue(createUserValue);
    }

    private String createUserValue(ElementAttribute value) {
        if (value.element.getId() == element.getId()) {
            if (value.attribute.getId() == calculateInfo.getAttributeId())
                return null;
            return addSlashes(value.attribute.getName());
        }
        String name = addSlashes(value.element.getName());
        if (name.equals("")) {
            name = Util.ELEMENT_PREFIX + value.element.getId();
            return name + "." + addSlashes(value.attribute.getName());
        }
        if (value.element.getQualifierId() == element.getQualifierId()) {
            return name + "." + addSlashes(value.attribute.getName());
        }
        Qualifier qualifier = engine.getQualifier(value.element
                .getQualifierId());
        return addSlashes(qualifier.getName()) + "." + name + "."
                + addSlashes(value.attribute.getName());
    }

    public void close() {
        framework.removeActionListener(ACTIVATE_ATTRIBUTE, listener);
        framework.removeActionListener(ACTIVATE_TABLE_ATTRIBUTE, listener);
    }

    private String addSlashes(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '\\') || (c == '.')) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public boolean isValueSetting() {
        String text = getText();
        if (text.length() == 0)
            return true;
        int pos = getCaretPosition();
        if (pos == 0)
            return true;
        if (pos >= text.length())
            pos--;
        if (pos == text.length() - 1)
            return true;

        if ((text.charAt(pos) == '[') || (text.charAt(pos) == ']'))
            return true;

        while ((pos >= 0) && (isLetterOrDigitOr_(text.charAt(pos)))) {
            pos--;
            if ((pos >= 0) && (text.charAt(pos) == '['))
                return true;
        }
        return false;
    }

}
