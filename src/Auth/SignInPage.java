package Auth;

import Admin.AdminPage;
import Control.User;
import Database.AppDefaults;
import Homepage.HomePage;
import Homepage.StaffPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

                try (Connection connection = DriverManager.getConnection(
                        AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

                    // Use a prepared statement to prevent SQL injection
                    String query = "SELECT * FROM users WHERE username = ? OR email = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, usernameOrEmail);
                        preparedStatement.setString(2, usernameOrEmail);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                String storedPassword = resultSet.getString("password");
                                String role = resultSet.getString("role");

                                // Hash the provided password for comparison
                                String hashedPassword = hashPassword(password);

                                if (hashedPassword.equals(storedPassword)) {
                                    handleSignInSuccess(role, usernameOrEmail);
                                } else {
                                    JOptionPane.showMessageDialog(SignInPage.this, "Incorrect password.");
                                }
                            } else {
                                JOptionPane.showMessageDialog(SignInPage.this, "User not found.");
                            }
                        }
                    }
                } catch (SQLException | NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
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

        // A navigation link to the signup page
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

    private void handleSignInSuccess(String role, String username) {
        dispose();
        if ("admin".equalsIgnoreCase(role)) {
            new AdminPage();
        } else if ("staff".equalsIgnoreCase(role)) {
            User user = new User(username);
            new StaffPage(user);
        } else {
            User user = new User(username);
            new HomePage(user);
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());

        // Convert byte array to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
