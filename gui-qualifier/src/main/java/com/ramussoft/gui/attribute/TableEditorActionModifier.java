package com.ramussoft.gui.attribute;

import javax.swing.Action;

import com.ramussoft.gui.attribute.table.TableEditor;

public interface TableEditorActionModifier {

    Action[] modify(Action[] actions, TableEditor tableEditor);

}
