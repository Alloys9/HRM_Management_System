package Auth;


import Admin.AdminPage;
import Control.User;
import Database.AppDefaults;
import Homepage.HomePage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class SignInPage extends JFrame {

    private JTextField signInUsernameOrEmailField;
    private JPasswordField signInPasswordField;
    private JButton signInButton;

    public SignInPage() {
        setTitle("Sign In");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Logo
        ImageIcon imageIcon = new ImageIcon("images/login.png");
        JLabel imageLabel = new JLabel(imageIcon);

        // Labels and fields
        JLabel usernameOrEmailLabel = new JLabel("Username or Email:");
        JLabel passwordLabel = new JLabel("Password:");

        signInUsernameOrEmailField = new JTextField(20);
        signInPasswordField = new JPasswordField(20);
        signInButton = new JButton("Sign In");

        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usernameOrEmail = signInUsernameOrEmailField.getText();
                String password = new String(signInPasswordField.getPassword());

                try {
                    Connection connection = DriverManager.getConnection(
                            AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD);

                    PreparedStatement preparedStatement = connection.prepareStatement(
                            "SELECT * FROM users WHERE username = ? OR email = ?");
                    preparedStatement.setString(1, usernameOrEmail);
                    preparedStatement.setString(2, usernameOrEmail);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String storedPassword = resultSet.getString("password");
                        String role = resultSet.getString("role");

                        if (password.equals(storedPassword)) {
                            if ("admin".equalsIgnoreCase(role)) {

                                dispose();
                                new AdminPage();
                            } else {

                                User user = new User(usernameOrEmail);
                                dispose();
                                new HomePage(user);
                            }
                        } else {
                            JOptionPane.showMessageDialog(SignInPage.this, "Incorrect password.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(SignInPage.this, "User not found.");
                    }
                    connection.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(SignInPage.this, "Error signing in: " + ex.getMessage());
                }
            }
        });

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(imageLabel, constraints);

        constraints.gridy = 1;
        panel.add(usernameOrEmailLabel, constraints);
        constraints.gridy = 2;
        panel.add(signInUsernameOrEmailField, constraints);

        constraints.gridy = 3;
        panel.add(passwordLabel, constraints);
        constraints.gridy = 4;
        panel.add(signInPasswordField, constraints);

        constraints.gridy = 5;
        constraints.gridwidth = 2;
        panel.add(signInButton, constraints);

        //A navigation link to the signup page
        JLabel signUpLink = new JLabel("Don't have an account? Sign up here.");
        signUpLink.setForeground(Color.BLUE);
        signUpLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                dispose();
                new SignUpPage();
            }
        });
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        panel.add(signUpLink, constraints);

        add(panel);

        setVisible(true);
    }

}
