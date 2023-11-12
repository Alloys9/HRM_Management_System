package Admin.Departments;

import Admin.AdminPage;
import Control.User;
import Database.AppDefaults;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllCompletedTasksPage extends JFrame {

    public AllCompletedTasksPage(User user) {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("All Completed Tasks");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = createMainPanel();
        add(panel);
        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));

        ImageIcon logoIcon = new ImageIcon("images/t_completed.png");
        JLabel logoLabel = new JLabel(logoIcon);

        GridBagConstraints logoConstraints = new GridBagConstraints();
        logoConstraints.gridx = 0;
        logoConstraints.gridy = 0;
        logoConstraints.gridwidth = 2;
        logoConstraints.anchor = GridBagConstraints.PAGE_START;
        panel.add(logoLabel, logoConstraints);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        JButton backButton = new JButton(new AbstractAction("Back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new AdminPage();
            }
        });
        backButton.setFocusPainted(false);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(5, 5, 5, 5);
        panel.add(backButton, constraints);

        JTable allCompletedTasksTable = createAllCompletedTasksTable();
        allCompletedTasksTable.setDefaultEditor(Object.class, null);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0; // Allow horizontal expansion
        constraints.weighty = 1.0; // Allow vertical expansion
        panel.add(new JScrollPane(allCompletedTasksTable), constraints);

        return panel;
    }

    private JTable createAllCompletedTasksTable() {
        JTable allCompletedTasksTable = new JTable(createAllCompletedTasksTableModel());
        allCompletedTasksTable.setRowHeight(40);
        return allCompletedTasksTable;
    }

    private DefaultTableModel createAllCompletedTasksTableModel() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Task ID");
        model.addColumn("Task Description");
        model.addColumn("Username");
        model.addColumn("Role");

        List<Task> allCompletedTasks = getAllCompletedTasksFromDatabase();

        for (Task task : allCompletedTasks) {
            model.addRow(new Object[]{task.getId(), task.getDescription(), task.getUsername(), task.getRole()});
        }

        return model;
    }

    private List<Task> getAllCompletedTasksFromDatabase() {
        List<Task> allCompletedTasks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            String sql = "SELECT tc.user_id, tc.task_id, t.description, u.username, u.role " +
                    "FROM tasks_completed tc " +
                    "JOIN tasks t ON tc.task_id = t.task_id " +
                    "JOIN users u ON tc.user_id = u.user_id";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int taskId = resultSet.getInt("task_id");
                        String description = resultSet.getString("description");
                        String username = resultSet.getString("username");
                        String role = resultSet.getString("role");

                        allCompletedTasks.add(new Task(taskId, description, username, role));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching completed task details.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return allCompletedTasks;
    }

    private static class Task {
        private final int id;
        private final String description;
        private final String username;
        private final String role;

        public Task(int id, String description, String username, String role) {
            this.id = id;
            this.description = description;
            this.username = username;
            this.role = role;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
