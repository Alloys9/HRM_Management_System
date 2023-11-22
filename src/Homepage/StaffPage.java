package Homepage;

import Auth.SignInPage;
import Control.User;
import Database.AppDefaults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StaffPage extends JFrame {
    private User currentUser;

    public StaffPage(User user) {
        this.currentUser = user;

        setTitle("Authorization");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel infoLabel = new JLabel("<html><center>Congratulations " + currentUser.getUsername().toUpperCase() +
                "<br>You have successfully signed in!<br>Contact your administrator for further assistance.</center></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);


        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(infoLabel, constraints);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusable(false);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new SignInPage();
            }
        });
        constraints.gridy = 1;
        panel.add(logoutButton, constraints);

        add(panel);
        setVisible(true);
    }
}
