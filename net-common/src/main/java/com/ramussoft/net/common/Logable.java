package com.ramussoft.net.common;

public interface Logable {

    long login(String login, String password);

    boolean isAdmin();

    boolean canLogin();

    boolean canUndoRedo();

    void closeConnection();

}
