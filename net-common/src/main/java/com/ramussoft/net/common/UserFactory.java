package com.ramussoft.net.common;

import java.util.List;

public interface UserFactory {

    List<User> getUsers();

    List<Group> getGroups();

    void createGroup(Group group);

    void updateGroup(Group group);

    void deleteGroup(String groupName);

    void createUser(User user);

    void updateUser(User user);

    void deleteUser(String login);

    User getUser(String login);

    Group getGroup(String groupName);
}
