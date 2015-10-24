package com.ramussoft.server;

import java.util.List;

import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;

public class CheckableUserFactory implements UserFactory {

    private boolean admin;
    private UserFactory userFactory;

    public CheckableUserFactory(UserFactory userFactory) {
        this.userFactory = userFactory;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public void updateUser(User user) {
        if (admin)
            userFactory.updateUser(user);
    }

    @Override
    public void updateGroup(Group group) {
        if (admin)
            userFactory.updateGroup(group);
    }

    @Override
    public List<User> getUsers() {
        if (admin)
            return userFactory.getUsers();
        return null;
    }

    @Override
    public User getUser(String login) {
        if (admin)
            return userFactory.getUser(login);
        return null;
    }

    @Override
    public List<Group> getGroups() {
        if (admin)
            return userFactory.getGroups();
        return null;
    }

    @Override
    public Group getGroup(String groupName) {
        if (admin)
            return userFactory.getGroup(groupName);
        return null;
    }

    @Override
    public void deleteUser(String login) {
        if (admin)
            userFactory.deleteUser(login);
    }

    @Override
    public void deleteGroup(String groupName) {
        if (admin)
            userFactory.deleteGroup(groupName);
    }

    @Override
    public void createUser(User user) {
        if (admin)
            userFactory.createUser(user);
    }

    @Override
    public void createGroup(Group group) {
        if (admin)
            userFactory.createGroup(group);
    }

    public boolean isAdmin() {
        return admin;
    }
}
