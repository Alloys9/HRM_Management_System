package Auth;

import Control.User;
import Database.AppDefaults;
import Homepage.StaffPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignUpPage extends JFrame {

    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmSignUpPasswordField;
    private JButton signUpButton;

    public SignUpPage() {
        setTitle("Sign Up");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Logo
        ImageIcon imageIcon = new ImageIcon("images/register.png");
        JLabel imageLabel = new JLabel(imageIcon);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(imageLabel, constraints);

        // Labels and fields
        JLabel emailLabel = new JLabel("Email:");
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");

        emailField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmSignUpPasswordField = new JPasswordField(20);
        signUpButton = new JButton("Sign Up");

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmSignUpPasswordField.getPassword());

                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(SignUpPage.this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(SignUpPage.this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try (Connection connection = DriverManager.getConnection(
                            AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

                        // Use a prepared statement to prevent SQL injection
                        String insertQuery = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                            // Set parameters using prepared statement
                            preparedStatement.setString(1, email);
                            preparedStatement.setString(2, username);

                            // Hash the password before storing it in the database
                            String hashedPassword = hashPassword(password);
                            preparedStatement.setString(3, hashedPassword);

                            preparedStatement.executeUpdate();
                        }

                        String userRole = getUserRole(username);

                        JOptionPane.showMessageDialog(SignUpPage.this, "User signed up: " + username);

                        dispose();

                        if ("staff".equals(userRole)) {
                            User user = new User(username);
                            new StaffPage(user);
                        }

                    } catch (SQLException | NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(SignUpPage.this, "Error signing up: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Navigation link to the sign-in page
        JLabel signInLink = new JLabel("Already have an account? Sign in here.");
        signInLink.setForeground(Color.BLUE); // Make the link blue
        signInLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signInLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new SignInPage();
            }
        });

        // Add components to the panel using GridBagConstraints
        constraints.gridy = 1;
        panel.add(emailLabel, constraints);
        constraints.gridy = 2;
        panel.add(emailField, constraints);

        constraints.gridy = 3;
        panel.add(usernameLabel, constraints);
        constraints.gridy = 4;
        panel.add(usernameField, constraints);

        constraints.gridy = 5;
        panel.add(passwordLabel, constraints);
        constraints.gridy = 6;
        panel.add(passwordField, constraints);

        constraints.gridy = 7;
        panel.add(confirmPasswordLabel, constraints);
        constraints.gridy = 8;
        panel.add(confirmSignUpPasswordField, constraints);

        constraints.gridy = 9;
        constraints.gridwidth = 2;
        panel.add(signUpButton, constraints);

        constraints.gridy = 10;
        constraints.gridwidth = 2;
        panel.add(signInLink, constraints);

        // Add the panel to the frame
        add(panel);

        emailField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        confirmSignUpPasswordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        setVisible(true);
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

    private String getUserRole(String username) {
        String role = null;
        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            String sql = "SELECT role FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        role = resultSet.getString("role");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return role;
    }

}
