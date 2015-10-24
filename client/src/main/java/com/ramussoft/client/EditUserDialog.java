package com.ramussoft.client;

import info.clearthought.layout.TableLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.net.common.User;

public class EditUserDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -828552740754434425L;

    private User user;

    private User resUser;

    private JTextField name = new JTextField();

    private JTextField login = new JTextField();

    private JPasswordField password = new JPasswordField();

    public EditUserDialog(JDialog dialog, User user, AdminPanelPlugin plugin) {
        super(dialog, true);
        this.user = user;
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if (user == null) {
            setTitle(plugin.getString("UserDialog.Create"));
            this.user = new User();
        } else {
            setTitle(plugin.getString("UserDialog.Edit"));
            login.setText(user.getLogin());
            name.setText(user.getName());
            password.setText(user.getPassword());
            login.setEnabled(false);
        }

        double[][] size = {
                {5, TableLayout.FILL, 5, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5, TableLayout.FILL, 5,
                        TableLayout.FILL, 5}};

        TableLayout layout = new TableLayout(size);
        JPanel contentPane = new JPanel(layout);

        contentPane.add(new JLabel(plugin.getString("UserDialog.Login")),
                "1, 1");
        contentPane.add(login, "3, 1");
        contentPane
                .add(new JLabel(plugin.getString("UserDialog.Name")), "1, 3");
        contentPane.add(name, "3, 3");
        contentPane.add(new JLabel(plugin.getString("UserDialog.Password")),
                "1, 5");
        contentPane.add(password, "3, 5");

        this.setMainPane(contentPane);
        this.pack();
        this.setMinimumSize(getSize());
        this.setLocationRelativeTo(null);
        Options.loadOptions(this);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            Options.saveOptions(this);
        }
    }

    public User getUser() {
        return resUser;
    }

    @Override
    protected void onOk() {
        user.setPassword(new String(password.getPassword()));
        user.setName(name.getText());
        user.setLogin(login.getText());
        super.onOk();
        resUser = user;
    }

}
