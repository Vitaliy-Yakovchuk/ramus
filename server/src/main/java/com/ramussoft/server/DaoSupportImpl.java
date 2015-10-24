package com.ramussoft.server;

import java.text.MessageFormat;
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.UserFactory;

public class DaoSupportImpl implements UserDetailsService {

    private UserFactory userFactory;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        com.ramussoft.net.common.User user = getUserFactory().getUser(username);

        if (user == null) {
            throw new UsernameNotFoundException(MessageFormat.format(
                    "User {0} not found", username));
        }

        List<Group> list = user.getGroups();
        GrantedAuthority[] arrayAuths = new GrantedAuthority[list.size() + 1];
        for (int i = 0; i < list.size(); i++) {
            arrayAuths[i] = new GrantedAuthorityImpl("ROLE_"
                    + list.get(i).getName().toUpperCase());
        }
        arrayAuths[list.size()] = new GrantedAuthorityImpl("ROLE_USER");

        return new User(user.getLogin(), user.getPassword(), true, true, true,
                true, arrayAuths);
    }

    /**
     * @param userFactory the userFactory to set
     */
    public void setUserFactory(UserFactory userFactory) {
        this.userFactory = userFactory;
    }

    /**
     * @return the userFactory
     */
    public UserFactory getUserFactory() {
        return userFactory;
    }
}
