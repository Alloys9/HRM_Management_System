package Admin.Members;

import Admin.AdminPage;
import Control.User;
import Database.AppDefaults;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;


public class Members extends JFrame {

    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JButton editButton;
    private JButton deleteButton;

    public Members(User user) {
        setTitle("Members Page");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);



        JPanel headerPanel = new JPanel(new BorderLayout());

        ImageIcon backIcon = new ImageIcon("images/back.png");
        JLabel backButtonLabel = new JLabel(backIcon);

        backButtonLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                goBackToAdminPage();
            }
        });

        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.add(backButtonLabel);

        headerPanel.add(backButtonPanel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new BorderLayout());


        tableModel = new DefaultTableModel();
        membersTable = new JTable(tableModel);
        membersTable.setDefaultEditor(Object.class, null);
        scrollPane = new JScrollPane(membersTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = membersTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int userId = (int) tableModel.getValueAt(selectedRow, 0);
                    editMember(userId);
                } else {
                    JOptionPane.showMessageDialog(Members.this, "Select a member to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = membersTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int userId = (int) tableModel.getValueAt(selectedRow, 0);
                    int confirm = JOptionPane.showConfirmDialog(Members.this, "Are you sure you want to delete this member?",
                            "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteMember(userId);
                    }
                } else {
                    JOptionPane.showMessageDialog(Members.this, "Select a member to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        populateMembersTable();

        setVisible(true);
    }
    private void goBackToAdminPage() {
        AdminPage adminPage = new AdminPage();
        adminPage.setVisible(true);
        this.dispose();
    }

    private void populateMembersTable() {
        try {
            Connection connection = DriverManager.getConnection(
                    AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD);

            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users");
            ResultSet resultSet = preparedStatement.executeQuery();

            tableModel.setColumnCount(0);
            tableModel.setRowCount(0);

            tableModel.addColumn("User ID");
            tableModel.addColumn("Email");
            tableModel.addColumn("Username");
            tableModel.addColumn("Role");

            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("user_id"),
                        resultSet.getString("email"),
                        resultSet.getString("username"),
                        resultSet.getString("role")
                });
            }



            connection.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void editMember(int userId) {
        try {
            Connection connection = DriverManager.getConnection(
                    AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD);

            PreparedStatement fetchStatement = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");
            fetchStatement.setInt(1, userId);
            ResultSet resultSet = fetchStatement.executeQuery();

            if (resultSet.next()) {
                String email = resultSet.getString("email");
                String username = resultSet.getString("username");
                String role = resultSet.getString("role");

                JDialog editDialog = new JDialog(this, "Edit Member");
                editDialog.setLayout(new GridLayout(4, 2, 15, 15));

                JLabel emailLabel = new JLabel("Email:");
                JTextField emailField = new JTextField(email);
                emailField.setBorder(new LineBorder(Color.GRAY));

                JLabel usernameLabel = new JLabel("Username:");
                JTextField usernameField = new JTextField(username);
                usernameField.setBorder(new LineBorder(Color.GRAY));

                JLabel roleLabel = new JLabel("Role:");
                JTextField roleField = new JTextField(role);
                roleField.setBorder(new LineBorder(Color.GRAY));

                JButton saveButton = new JButton("Save");
                JButton cancelButton = new JButton("Cancel");

                editDialog.add(emailLabel);
                editDialog.add(emailField);
                editDialog.add(usernameLabel);
                editDialog.add(usernameField);
                editDialog.add(roleLabel);
                editDialog.add(roleField);
                editDialog.add(saveButton);
                editDialog.add(cancelButton);

                saveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String newEmail = emailField.getText();
                        String newUsername = usernameField.getText();
                        String newRole = roleField.getText();

                        try {
                            Connection updateConnection = DriverManager.getConnection(
                                    AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD);

                            PreparedStatement updateStatement = updateConnection.prepareStatement(
                                    "UPDATE users SET email = ?, username = ?, role = ? WHERE user_id = ?"
                            );

                            updateStatement.setString(1, newEmail);
                            updateStatement.setString(2, newUsername);
                            updateStatement.setString(3, newRole);
                            updateStatement.setInt(4, userId);

                            int result = updateStatement.executeUpdate();

                            if (result > 0) {
                                JOptionPane.showMessageDialog(editDialog, "Member updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                editDialog.setVisible(false);
                                tableModel.setRowCount(0);
                                populateMembersTable();
                            } else {
                                JOptionPane.showMessageDialog(editDialog, "Error updating member.", "Error", JOptionPane.ERROR_MESSAGE);
                            }

                            updateConnection.close();

                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(editDialog, "Error updating member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editDialog.setVisible(false);
                    }
                });

                editDialog.setSize(400, 250);
                editDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                editDialog.pack();
                editDialog.setLocationRelativeTo(this);
                editDialog.setVisible(true);
            }

            connection.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error editing member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteMember(int userId) {
        try {
            Connection connection = DriverManager.getConnection(
                    AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD);

            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?");
            preparedStatement.setInt(1, userId);

            int result = preparedStatement.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Member deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0);
                populateMembersTable();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting member.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            connection.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}