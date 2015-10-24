package com.ramussoft.net.common.internet;

import com.ramussoft.common.Element;

public interface ClientService {

    Long login(String login, String password);

    Boolean canLogin();

    void redo(byte[] bs);

    void undo(byte[] bs);

    byte[] loadAllData();

    boolean deleteStream(String path);

    public byte[] getStream(String path);

    void setStream(String path, byte[] bytes);

    byte[] replaceElements(Element[] oldElements,
                           Element newElement);

    boolean isAdmin();

    long nextValue(String s);

}
