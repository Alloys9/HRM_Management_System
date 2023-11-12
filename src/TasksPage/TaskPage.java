package TasksPage;

import Control.User;
import Database.AppDefaults;
import Homepage.HomePage;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskPage extends JFrame {
    private User currentUser;
    private JTable taskTable;
    private JButton deleteButton;
    private JButton completeButton;

    public TaskPage(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("TaskChosen");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = createMainPanel();
        add(panel);
        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));


        JButton backButton = new JButton(new AbstractAction("Back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor((Component) e.getSource());
                currentFrame.dispose();

                new HomePage(currentUser).setVisible(true);
            }
        });
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(70, 30));

        JButton tasksCompletedButton = new JButton(new AbstractAction("Tasks Done") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new TaskCompletePage(currentUser);
            }
        });
        tasksCompletedButton.setFocusPainted(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(tasksCompletedButton, BorderLayout.EAST);

        JPanel spacePanel = new JPanel();
        spacePanel.setPreferredSize(new Dimension(10, 20));
        taskTable = createTaskTable(currentUser);

        deleteButton = new JButton("Delete");
        completeButton = new JButton("Complete Task");
        deleteButton.setEnabled(false);
        completeButton.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = taskTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String taskDescription = (String) taskTable.getValueAt(selectedRow, 0);
                    int confirmResult = showConfirmationDialog("Delete", "Are you sure you want to delete the task?");
                    if (confirmResult == JOptionPane.YES_OPTION) {
                        handleDeleteButtonClick(currentUser.getUsername(), taskDescription);
                        updateTableModel(selectedRow);
                        resetButtons();
                    }
                }
            }
        });

        completeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = taskTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String taskDescription = (String) taskTable.getValueAt(selectedRow, 0);
                    int confirmResult = showConfirmationDialog("Complete Task", "Are you sure you want to complete the task?");
                    if (confirmResult == JOptionPane.YES_OPTION) {
                        handleCompleteTaskButtonClick(currentUser.getUsername(), taskDescription);
                        updateTableModel(selectedRow);
                        resetButtons();
                    }
                }
            }
        });

        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = taskTable.getSelectedRow();
                if (selectedRow >= 0) {
                    deleteButton.setEnabled(true);
                    completeButton.setEnabled(true);
                } else {
                    resetButtons();
                }
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTable createTaskTable(User user) {
        JTable taskTable = new JTable(createTableModel(user)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        taskTable.setDefaultRenderer(Object.class, new TaskTableRenderer());
        taskTable.setRowHeight(40);
        return taskTable;
    }

    private DefaultTableModel createTableModel(User user) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("<html><b><font size='5'>Tasks Assigned</font></b></html>");

        List<String> chosenTasks = getChosenTasksFromDatabase(user.getUsername());

        for (String task : chosenTasks) {
            model.addRow(new Object[]{task});
        }

        return model;
    }


    private class TaskTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Component) {
                return (Component) value;
            } else {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }

    private void resetButtons() {
        deleteButton.setEnabled(false);
        completeButton.setEnabled(false);
    }

    private void updateTableModel(int row) {
        DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
        model.removeRow(row);
    }

    private List<String> getChosenTasksFromDatabase(String username) {
        List<String> chosenTasks = new ArrayList<>();
        Set<Integer> uniqueTaskIds = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            int userId = getUserId(username, connection);

            String sql = "SELECT task_id FROM tasks_chosen WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int chosenTaskId = resultSet.getInt("task_id");

                        if (uniqueTaskIds.add(chosenTaskId)) {
                            String description = getTaskDescription(chosenTaskId, connection);
                            chosenTasks.add(description);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            chosenTasks.add("Error fetching task details.");
        }
        return chosenTasks;
    }

    private int getUserId(String username, Connection connection) throws SQLException {
        int userId = -1;
        String getUserIdSql = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement getUserIdStatement = connection.prepareStatement(getUserIdSql)) {
            getUserIdStatement.setString(1, username);

            try (ResultSet userIdResultSet = getUserIdStatement.executeQuery()) {
                if (userIdResultSet.next()) {
                    userId = userIdResultSet.getInt("user_id");
                } else {
                    throw new SQLException("User not found.");
                }
            }
        }
        return userId;
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

    private void handleDeleteButtonClick(String username, String taskDescription) {
        deleteTaskFromDatabase(username, taskDescription);


    }

    private void handleCompleteTaskButtonClick(String username, String taskDescription) {
        completeTask(username, taskDescription);
    }

    private void completeTask(String username, String taskDescription) {
        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            int userId = getUserId(username, connection);


            int taskId = getTaskId(taskDescription, connection);

            String insertCompletedTaskSql = "INSERT INTO tasks_completed (user_id, task_id) VALUES (?, ?)";
            try (PreparedStatement insertCompletedTaskStatement = connection.prepareStatement(insertCompletedTaskSql)) {
                insertCompletedTaskStatement.setInt(1, userId);
                insertCompletedTaskStatement.setInt(2, taskId);
                insertCompletedTaskStatement.executeUpdate();
            }

            String deleteChosenTaskSql = "DELETE FROM tasks_chosen WHERE user_id = ? AND task_id = ?";
            try (PreparedStatement deleteChosenTaskStatement = connection.prepareStatement(deleteChosenTaskSql)) {
                deleteChosenTaskStatement.setInt(1, userId);
                deleteChosenTaskStatement.setInt(2, taskId);
                deleteChosenTaskStatement.executeUpdate();
            }

        } catch (SQLException e) {

        }
    }

    private int getTaskId(String taskDescription, Connection connection) throws SQLException {
        int taskId = -1;
        String getTaskIdSql = "SELECT task_id FROM tasks WHERE description = ?";
        try (PreparedStatement getTaskIdStatement = connection.prepareStatement(getTaskIdSql)) {
            getTaskIdStatement.setString(1, taskDescription);

            try (ResultSet taskIdResultSet = getTaskIdStatement.executeQuery()) {
                if (taskIdResultSet.next()) {
                    taskId = taskIdResultSet.getInt("task_id");
                } else {
                    throw new SQLException("Task not found.");
                }
            }
        }
        return taskId;
    }
    private int showConfirmationDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(TaskPage.this, message, title, JOptionPane.YES_NO_OPTION);
    }

    private void deleteTaskFromDatabase(String username, String taskDescription) {
        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            int userId = getUserId(username, connection);

            int taskId = getTaskId(taskDescription, connection);

            String deleteChosenTaskSql = "DELETE FROM tasks_chosen WHERE user_id = ? AND task_id = ?";
            try (PreparedStatement deleteChosenTaskStatement = connection.prepareStatement(deleteChosenTaskSql)) {
                deleteChosenTaskStatement.setInt(1, userId);
                deleteChosenTaskStatement.setInt(2, taskId);
                deleteChosenTaskStatement.executeUpdate();

            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(TaskPage.this, "Error deleting task from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

}
