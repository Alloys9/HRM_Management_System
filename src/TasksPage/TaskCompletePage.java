package TasksPage;

import Control.User;
import Database.AppDefaults;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskCompletePage extends JFrame {
    private User currentUser;

    public TaskCompletePage(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Completed Tasks");
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

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);


        JButton backButton = new JButton(new AbstractAction("Back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor((Component) e.getSource());
                currentFrame.dispose();

                new TaskPage(currentUser).setVisible(true);
            }
        });
        backButton.setFocusPainted(false);


        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(5, 5, 5, 5);
        panel.add(backButton, constraints);

        JTable completedTaskTable = createCompletedTaskTable();
        completedTaskTable.setDefaultEditor(Object.class, null);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(completedTaskTable), constraints);

        return panel;
    }

    private JTable createCompletedTaskTable() {
        JTable completedTaskTable = new JTable(createCompletedTaskTableModel());
        completedTaskTable.setRowHeight(40);
        return completedTaskTable;
    }

    private DefaultTableModel createCompletedTaskTableModel() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Task ID");
        model.addColumn("Task Description");

        List<Task> completedTasks = getCompletedTasksFromDatabase();

        for (Task task : completedTasks) {
            model.addRow(new Object[]{task.getId(), task.getDescription()});
        }

        return model;
    }

    private List<Task> getCompletedTasksFromDatabase() {
        List<Task> completedTasks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            String sql = "SELECT user_id, task_id FROM tasks_completed";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int userId = resultSet.getInt("user_id");
                        int taskId = resultSet.getInt("task_id");

                        String description = getTaskDescription(taskId, connection);
                        completedTasks.add(new Task(taskId, description));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching completed task details.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return completedTasks;
    }

    private String getTaskDescription(int taskId, Connection connection) throws SQLException {
        String description = "Task not found.";

        String getDescriptionSql = "SELECT description FROM tasks WHERE task_id = ?";
        try (PreparedStatement getDescriptionStatement = connection.prepareStatement(getDescriptionSql)) {
            getDescriptionStatement.setInt(1, taskId);

            try (ResultSet descriptionResultSet = getDescriptionStatement.executeQuery()) {
                if (descriptionResultSet.next()) {
                    description = descriptionResultSet.getString("description");
                }
            }
        }
        return description;
    }

    private static class Task {
        private final int id;
        private final String description;

        public Task(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }
}
