package com.ramussoft.pb;

public interface IDEF0UndoMeneger {
    public boolean canUndo();

    public boolean canRedo();

    public void undo();

    public void redo();
}
