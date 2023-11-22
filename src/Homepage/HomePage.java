package Homepage;

import Control.User;
import Database.AppDefaults;
import Index.index;
import TasksPage.TaskPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomePage extends JFrame {
    private User currentUser;
    private List<String> userTask;

    private HashMap<String, String> tasksChosen;

    public HomePage(User user) {
        this.currentUser = user;
        this.userTask = new ArrayList<>();
        this.tasksChosen = new HashMap<>();

        setTitle("Home");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);


        ImageIcon imageIcon = new ImageIcon("images/home.png");
        JLabel imageLabel = new JLabel(imageIcon);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(imageLabel, constraints);


        JLabel userNameLabel = new JLabel("Welcome, " + currentUser.getUsername().toUpperCase());
        JLabel roleLabel = new JLabel("" + getUserRole(currentUser.getUsername()));
        userNameLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        roleLabel.setForeground(Color.GRAY);

        constraints.gridy = 2;
        panel.add(userNameLabel, constraints);
        constraints.gridy = 3;
        panel.add(roleLabel, constraints);


        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        loadItemsFromDatabase(itemsPanel, constraints);
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Set the constraints for scrollPane
        GridBagConstraints scrollPaneConstraints = new GridBagConstraints();
        scrollPaneConstraints.gridx = 0;
        scrollPaneConstraints.gridy = 4;
        scrollPaneConstraints.gridwidth = 2;
        scrollPaneConstraints.fill = GridBagConstraints.BOTH; // Allow vertical and horizontal expansion
        scrollPaneConstraints.weightx = 1.0;
        scrollPaneConstraints.weighty = 1.0;

        // Add the JScrollPane to the main panel
        panel.add(scrollPane, scrollPaneConstraints);

        JButton viewTaskButton = new JButton("Your Tasks");
        viewTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (userTask.isEmpty()) {
                    JOptionPane.showMessageDialog(HomePage.this, "No Pending Tasks.");
                } else {
                    String selectedTask = userTask.get(0);
                    int taskId = extractTaskId(selectedTask);

                    if (taskId != -1) {
                        TaskPage taskPage = new TaskPage(currentUser);
                        taskPage.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(HomePage.this, "Error extracting Task ID.");
                    }
                }
            }
        });

        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        GridBagConstraints bottomRightConstraints = new GridBagConstraints();
        bottomRightConstraints.insets = new Insets(80, 0, 0, 80);
        bottomRightConstraints.gridx = 1;

        ImageIcon logoutIcon = new ImageIcon("images/logout.png");
        JLabel logoutImageLabel = new JLabel(logoutIcon);

        JButton logoutButton = new JButton();
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new index();
            }
        });

        logoutButton.setIcon(logoutIcon);

        bottomRightPanel.add(logoutButton);

        constraints.gridy = 2;
        constraints.gridwidth = 0;
        panel.add(bottomRightPanel, bottomRightConstraints);

        viewTaskButton.setFocusable(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(viewTaskButton);

        constraints.gridy = 5;
        constraints.gridwidth = 2;
        panel.add(buttonPanel, constraints);


        add(panel);
        setVisible(true);
    }
    private int extractTaskId(String taskDescription) {
        String[] parts = taskDescription.split("Task ID: ");
        if (parts.length > 1) {
            String taskIdStr = parts[1].split(",")[0].trim();
            return Integer.parseInt(taskIdStr);
        } else {
            return -1;
        }
    }

    private void loadItemsFromDatabase(JPanel itemsPanel, GridBagConstraints constraints) {
        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            String userRole = getUserRole(currentUser.getUsername());

            String sql = "SELECT task_id, description FROM tasks WHERE user_role = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userRole);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int row = 0;

                    // Use GridLayout for grid-like organization
                    itemsPanel.setLayout(new GridBagLayout());

                    while (resultSet.next()) {
                        int taskId = resultSet.getInt("task_id");
                        String taskDescription = resultSet.getString("description");

                        JPanel taskEntryPanel = new JPanel(new BorderLayout(10, 5)); // Adjust spacing
                        taskEntryPanel.setPreferredSize(new Dimension(500, 80)); // Adjust preferred size
                        taskEntryPanel.setBackground(Color.WHITE); // Add a background color for better visibility

                        JTextArea taskTextArea = new JTextArea(taskDescription);
                        taskTextArea.setLineWrap(true);
                        taskTextArea.setWrapStyleWord(true);
                        taskTextArea.setEditable(false);
                        taskTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

                        JScrollPane textScrollPane = new JScrollPane(taskTextArea);
                        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        taskEntryPanel.add(textScrollPane, BorderLayout.CENTER);

                        JButton chooseTaskButton = new JButton("Choose Task");
                        chooseTaskButton.setFocusPainted(false);

                        chooseTaskButton.setBackground(new Color(128, 128, 128));
                        chooseTaskButton.setForeground(Color.WHITE);


                        if (isTaskAlreadyChosen(connection, currentUser.getUserId(), taskId)) {
                            chooseTaskButton.setEnabled(false);
                            chooseTaskButton.setText("Task Chosen");
                        } else {
                            chooseTaskButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    tasksChosen.put(currentUser.getUsername() + "_" + taskId, taskDescription);

                                    addChosenTaskToDatabase(currentUser.getUsername(), taskId, chooseTaskButton);

                                    chooseTaskButton.setEnabled(false);
                                    chooseTaskButton.setText("Task Chosen");
                                }
                            });
                        }

                        taskEntryPanel.add(chooseTaskButton, BorderLayout.SOUTH);

                        taskEntryPanel.setBackground(new Color(240, 240, 240));

                        GridBagConstraints gridBagConstraints = new GridBagConstraints();
                        gridBagConstraints.gridx = 0;
                        gridBagConstraints.gridy = row;
                        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                        gridBagConstraints.weightx = 1.0;
                        gridBagConstraints.insets = new Insets(10, 10, 10, 10);

                        itemsPanel.add(taskEntryPanel, gridBagConstraints);

                        userTask.add("Task ID: " + taskId + ", Description: " + taskDescription);
                        row++;
                    }

                    if (row == 0) {
                        JLabel noTasksLabel = new JLabel("No tasks available.");
                        itemsPanel.add(noTasksLabel);
                    }
                }
            }

            // Wrap itemsPanel with a JScrollPane
            JScrollPane scrollPane = new JScrollPane(itemsPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            // Set the layout of the container (assuming it is a JPanel)
            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);

        } catch (SQLException ex) {
            showErrorDialog("Error loading items from database", ex.getMessage());
        }
    }


    public static boolean isTaskAlreadyChosen(Connection connection, int userId, int taskId) throws SQLException {
            String sql = "SELECT COUNT(*) FROM tasks_chosen WHERE user_id = ? AND task_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setInt(2, taskId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
            return false;
        }


        private void addChosenTaskToDatabase(String username, int taskId, JButton chooseTaskButton) {
            try (Connection connection = DriverManager.getConnection(
                    AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {
                String getUserIdSql = "SELECT user_id FROM users WHERE username = ?";
                try (PreparedStatement getUserIdStatement = connection.prepareStatement(getUserIdSql)) {
                    getUserIdStatement.setString(1, username);

                    try (ResultSet userIdResultSet = getUserIdStatement.executeQuery()) {
                        if (userIdResultSet.next()) {
                            int user_id = userIdResultSet.getInt("user_id");
                            if (!isTaskAlreadyChosen(connection, user_id, taskId)) {
                                String insertTaskSql = "INSERT INTO tasks_chosen (user_id, task_id) VALUES (?, ?)";
                                try (PreparedStatement preparedStatement = connection.prepareStatement(insertTaskSql)) {
                                    preparedStatement.setInt(1, user_id);
                                    preparedStatement.setInt(2, taskId);
                                    preparedStatement.executeUpdate();

                                    showSuccessDialog();

                                    chooseTaskButton.setEnabled(false);
                                    chooseTaskButton.setText("Task Chosen");
                                }
                            } else {

                                JOptionPane.showMessageDialog(this, "Task already chosen.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            System.err.println("User not found: " + username);
                        }
                    }
                }
            } catch (SQLException ex) {
                if (ex.getSQLState().equals("23000") && ex.getErrorCode() == 1062) {
                    showErrorDialog("Task Already Chosen, Try Another!", "The task has already been chosen.");
                } else {
                    showErrorDialog("Error adding chosen task to database", ex.getMessage());
                }
            }
        }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }



    private void showSuccessDialog() {
        JOptionPane.showMessageDialog(this, "Task chosen successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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
