package com.ramussoft.net.common;

public interface UserProvider {

    void changePassword(String newPassword);

    User me();

}
