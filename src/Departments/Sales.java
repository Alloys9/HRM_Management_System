package Departments;

import Admin.Departments.Departments;
import Database.AppDefaults;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Sales extends JFrame {

    private static final String ROLE = "Sales";

    private DefaultTableModel userTableModel;
    private JTable userTable;
    private String username;
    private DefaultTableModel taskTableModel;
    private JTable taskTable;
    private JTextArea taskField;
    private JButton uploadTaskButton;

    public Sales() {
        setTitle("Sales");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel headerPanel = createHeaderPanel();
        JPanel bottomPanel = createBottomPanel();
        JPanel uploadPanel = createUploadPanel();

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);
        panel.add(uploadPanel, BorderLayout.SOUTH);

        getContentPane().add(panel);

        displayAccountantUsers();
        displayTasks();

        uploadTaskButton.addActionListener(e -> {
            String taskDescription = taskField.getText().trim();
            if (taskDescription.isEmpty() || "Enter task description here...".equalsIgnoreCase(taskDescription)) {
                JOptionPane.showMessageDialog(uploadPanel, "Error: Nothing entered", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                SwingUtilities.invokeLater(() -> {
                    if (uploadTaskToDatabase(taskDescription)) {
                        System.out.println("Uploaded Task: " + taskDescription);
                        taskField.setText("");
                        displayTasks();
                    } else {
                        JOptionPane.showMessageDialog(uploadPanel, "Error: Failed to upload to the database", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });


        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                int col = taskTable.columnAtPoint(e.getPoint());

                if (col == 2) {
                    int taskId = (int) taskTableModel.getValueAt(row, 0);
                    editTask(taskId);
                } else if (col == 3) {
                    int taskId = (int) taskTableModel.getValueAt(row, 0);
                    deleteTask(taskId);
                }
            }

        });

        configureUI();
        setVisible(true);
    }

    private void configureUI() {
        getContentPane().setBackground(new Color(240, 240, 240));

        userTable.setRowHeight(30);
        taskTable.setRowHeight(30);

        userTable.setGridColor(Color.LIGHT_GRAY);
        taskTable.setGridColor(Color.LIGHT_GRAY);

        userTable.setIntercellSpacing(new Dimension(10, 5));
        taskTable.setIntercellSpacing(new Dimension(10, 5));

        JTableHeader userTableHeader = userTable.getTableHeader();
        userTableHeader.setFont(new Font("Helvetica", Font.BOLD, 14));
        userTableHeader.setBackground(new Color(66, 134, 244));
        userTableHeader.setForeground(Color.WHITE);

        JTableHeader taskTableHeader = taskTable.getTableHeader();
        taskTableHeader.setFont(new Font("Helvetica", Font.BOLD, 14));
        taskTableHeader.setBackground(new Color(66, 134, 244));
        taskTableHeader.setForeground(Color.WHITE);


        uploadTaskButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        uploadTaskButton.setBackground(new Color(0, 123, 255));
        uploadTaskButton.setForeground(Color.WHITE);
        uploadTaskButton.setFont(new Font("Verdana", Font.BOLD, 16));
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        headerPanel.setPreferredSize(new Dimension(800, 80));

        ImageIcon backIcon = new ImageIcon("images/back.png");
        JLabel backIconLabel = new JLabel(backIcon);
        backIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backIconLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                Departments departments = new Departments();
                departments.setVisible(true);
                dispose();
            }
        });
        headerPanel.add(backIconLabel, BorderLayout.WEST);
        JPanel centerPanel = new JPanel();

        headerPanel.add(centerPanel, BorderLayout.CENTER);

        JLabel logoLabel = new JLabel(new ImageIcon("images/sales.png"));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(logoLabel);

        return headerPanel;
    }
    private void displayAccountantUsers() {
        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            String query = "SELECT username, email FROM users WHERE role = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, ROLE);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String username = resultSet.getString("username");
                        String email = resultSet.getString("email");

                        userTableModel.addRow(new Object[]{username, email});
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel createUploadPanel() {
        JPanel uploadPanel = new JPanel(new GridBagLayout());
        uploadPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        taskField = new JTextArea("Enter task description here...", 3, 30);
        taskField.setForeground(Color.GRAY);
        taskField.setCaretColor(Color.BLACK);
        taskField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        taskField.setFont(new Font("Helvetica", Font.PLAIN, 14));

        taskField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (taskField.getText().equals("Enter task description here...")) {
                    taskField.setText("");
                    taskField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent evt) {
                if (taskField.getText().isEmpty()) {
                    taskField.setForeground(Color.GRAY);
                    taskField.setText("Enter task description here...");
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        uploadPanel.add(taskField, gbc);

        uploadTaskButton = new JButton("Upload Task");
        uploadTaskButton.setBackground(new Color(0, 123, 255));
        uploadTaskButton.setForeground(Color.WHITE);
        uploadTaskButton.setFont(new Font("Helvetica", Font.BOLD, 16));
        uploadTaskButton.setFocusPainted(false);
        uploadTaskButton.setBorderPainted(false);

        uploadTaskButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                uploadTaskButton.setBackground(new Color(30, 144, 255));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                uploadTaskButton.setBackground(new Color(0, 123, 255));
            }
        });

        uploadTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String taskDescription = taskField.getText().trim();
                if (taskDescription.isEmpty() || "Enter task description here...".equalsIgnoreCase(taskDescription)) {
                    JOptionPane.showMessageDialog(uploadPanel, "Error: Nothing entered", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (!uploadTaskButton.isEnabled()) {
                        return;
                    }
                    uploadTaskButton.setEnabled(false);
                    SwingUtilities.invokeLater(() -> {
                        if (uploadTaskToDatabase(taskDescription)) {
                            System.out.println("Uploaded Task: " + taskDescription);
                            taskField.setText("");
                            displayTasks();
                        } else {

                        }

                        uploadTaskButton.setEnabled(true);
                    });
                }
            }
        });

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 5);
        uploadPanel.add(uploadTaskButton, gbc);

        return uploadPanel;
    }
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setPreferredSize(new Dimension(800, 200));

        String[] userColumnNames = {"Username", "Email"};
        userTableModel = new DefaultTableModel(userColumnNames, 0);
        userTable = new JTable(userTableModel);
        userTable.setDefaultEditor(Object.class, null);

        JScrollPane userTableScrollPane = new JScrollPane(userTable);
        userTableScrollPane.setPreferredSize(new Dimension(800, 100));

        String[] taskColumnNames = {"Task ID", "Description", "Edit", "Delete"};
        taskTableModel = new DefaultTableModel(taskColumnNames, 0);
        taskTable = new JTable(taskTableModel);
        taskTable.setDefaultEditor(Object.class, null);

        JScrollPane taskTableScrollPane = new JScrollPane(taskTable);
        taskTableScrollPane.setPreferredSize(new Dimension(800, 100));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userTableScrollPane, taskTableScrollPane);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(10);

        bottomPanel.add(splitPane, BorderLayout.CENTER);

        return bottomPanel;
    }

    private synchronized boolean uploadTaskToDatabase(String taskDescription) {
        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            // Check if the task already exists in the database
            if (taskExistsInDatabase(connection, taskDescription)) {
                System.out.println("Task already exists in the database.");
                return false; // Task already exists, no need to insert again
            }

            String query = "INSERT INTO tasks (description, user_role) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, taskDescription);
                preparedStatement.setString(2, ROLE);

                int rowsInserted = preparedStatement.executeUpdate();

                return rowsInserted > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    private boolean taskExistsInDatabase(Connection connection, String taskDescription) throws SQLException {
        String query = "SELECT COUNT(*) FROM tasks WHERE description = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, taskDescription);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            return count > 0;
        }
    }

    private void displayTasks() {
        try (Connection connection = DriverManager.getConnection(
                AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

            String query = "SELECT task_id, description FROM tasks WHERE user_role = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, ROLE);

                ResultSet resultSet = preparedStatement.executeQuery();

                taskTableModel.setRowCount(0);

                while (resultSet.next()) {
                    int taskId = resultSet.getInt("task_id");
                    String description = resultSet.getString("description");

                    JButton editButton = new JButton("Edit");
                    JButton deleteButton = new JButton("Delete");

                    editButton.setActionCommand("edit:" + taskId);
                    deleteButton.setActionCommand("delete:" + taskId);

                    editButton.addActionListener(e -> handleButtonClick(e.getActionCommand()));
                    deleteButton.addActionListener(e -> handleButtonClick(e.getActionCommand()));

                    editButton.setFocusPainted(false);
                    deleteButton.setFocusPainted(false);

                    taskTableModel.addRow(new Object[]{taskId, description, editButton, deleteButton});
                }

                setButtonColumnRenderer(2);
                setButtonColumnRenderer(3);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handleButtonClick(String actionCommand) {
        String[] parts = actionCommand.split(":");
        String action = parts[0];
        int taskId = Integer.parseInt(parts[1]);

        if ("edit".equals(action)) {
            editTask(taskId);
        } else if ("delete".equals(action)) {
            deleteTask(taskId);
        }
    }

    private void editTask(int taskId) {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        JLabel label = new JLabel("Enter new description for task ID: " + taskId);
        JTextField textField = new JTextField();

        panel.add(label);
        panel.add(textField);

        int result = JOptionPane.showConfirmDialog(
                Sales.this,
                panel,
                "Edit Task",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newDescription = textField.getText();
            if (!newDescription.isEmpty()) {
                try (Connection connection = DriverManager.getConnection(
                        AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

                    String query = "UPDATE tasks SET description = ? WHERE task_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, newDescription);
                        preparedStatement.setInt(2, taskId);
                        int rowsUpdated = preparedStatement.executeUpdate();

                        if (rowsUpdated > 0) {
                            JOptionPane.showMessageDialog(Sales.this, "Task ID: " + taskId + " updated successfully.");
                            displayTasks();
                        } else {
                            JOptionPane.showMessageDialog(Sales.this, "Failed to update Task ID: " + taskId);
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(Sales.this, "An error occurred while updating Task ID: " + taskId);
                }
            } else {
                JOptionPane.showMessageDialog(Sales.this, "Please enter a valid description.");
            }
        }
    }

    private void deleteTask(int taskId) {
        int confirmResult = JOptionPane.showConfirmDialog(
                Sales.this,
                "Are you sure you want to delete Task ID: " + taskId,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmResult == JOptionPane.YES_OPTION) {
            try (Connection connection = DriverManager.getConnection(
                    AppDefaults.DB_URL, AppDefaults.DB_USERNAME, AppDefaults.DB_PASSWORD)) {

                String query = "DELETE FROM tasks WHERE task_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, taskId);
                    int rowsDeleted = preparedStatement.executeUpdate();

                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(Sales.this, "Task ID: " + taskId + " deleted successfully.");
                        displayTasks();
                    } else {
                        JOptionPane.showMessageDialog(Sales.this, "Failed to delete Task ID: " + taskId);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Sales.this, "An error occurred while deleting Task ID: " + taskId);
            }
        }
    }

    private void setButtonColumnRenderer(int column) {
        taskTable.getColumnModel().getColumn(column).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Component) {
                    return (Component) value;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
    }

}
