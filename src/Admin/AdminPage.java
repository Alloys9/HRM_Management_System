package Admin;

import Admin.Departments.AllCompletedTasksPage;
import Admin.Members.Members;
import Control.User;
import Database.AppDefaults;
import Admin.Departments.Departments;
import Index.index;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPage extends JFrame {

    private Map<String, Class<?>> pageClasses;
    private List<String> tileImages;

    public AdminPage() {
        setTitle("ADMINISTRATOR");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        pageClasses = new HashMap<>();
        pageClasses.put("Departments", Departments.class);
        pageClasses.put("Members", Members.class);
        pageClasses.put("CompletedTasks", AllCompletedTasksPage.class);

        tileImages = new ArrayList<>();
        tileImages.add("images/department.png");
        tileImages.add("images/members.png");
        tileImages.add("images/completed.png");

        // Create a panel for the header section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.PAGE_AXIS));

        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        ImageIcon imageIcon = new ImageIcon("images/admin.png");
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(imageLabel);

        JLabel titleLabel = new JLabel("Welcome, ADMINISTRATOR");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        JPanel tilesPanel = new JPanel(new GridLayout(1, 3));  // Updated to 1x3 for three tiles
        tilesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] tileLabels = {"Departments", "Members", "Completed Tasks"};  // Removed "Logout"
        String[] pageNames = {"Departments", "Members", "CompletedTasks"};

        for (int i = 0; i < tileLabels.length; i++) {
            JPanel tilePanel = new JPanel();
            tilePanel.setLayout(new BoxLayout(tilePanel, BoxLayout.PAGE_AXIS));
            tilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel imageAndLabelPanel = new JPanel();
            imageAndLabelPanel.setLayout(new BoxLayout(imageAndLabelPanel, BoxLayout.PAGE_AXIS));
            imageAndLabelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            ImageIcon tileImageIcon = new ImageIcon(tileImages.get(i));
            imageLabel = new JLabel(tileImageIcon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel textLabel = new JLabel(tileLabels[i]);
            textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            Font labelFont = new Font("SansSerif", Font.BOLD, 14);
            textLabel.setFont(labelFont);
            textLabel.setForeground(Color.DARK_GRAY);

            imageAndLabelPanel.add(imageLabel);
            imageAndLabelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            imageAndLabelPanel.add(textLabel);

            int finalI = i;
            imageAndLabelPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openPage(pageNames[finalI]);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    tilePanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    tilePanel.setBorder(BorderFactory.createEmptyBorder());
                }
            });

            tilePanel.add(imageAndLabelPanel);
            tilesPanel.add(tilePanel);
        }

        add(tilesPanel, BorderLayout.CENTER);

        // Create a panel for the logout button
        JPanel logoutPanel = new JPanel();
        logoutPanel.setLayout(new BoxLayout(logoutPanel, BoxLayout.PAGE_AXIS));
        logoutPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon logoutIcon = new ImageIcon("images/logout.png");  // Replace with the actual path
        JLabel logoutLabel = new JLabel(logoutIcon);
        logoutLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoutTextLabel = new JLabel("Logout");
        logoutTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        logoutTextLabel.setFont(labelFont);
        logoutTextLabel.setForeground(Color.DARK_GRAY);

        logoutPanel.add(logoutLabel);
        logoutPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        logoutPanel.add(logoutTextLabel);

        logoutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                index();  // Call your logout method (replace with the actual method name)
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutPanel.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        add(logoutPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void openPage(String pageName) {
        Class<?> pageClass = pageClasses.get(pageName);

        if (pageClass != null) {
            try {
                JFrame pageInstance;

                // Create an instance of the selected page class
                if (pageClass == Members.class || pageClass == AllCompletedTasksPage.class) {
                    // If it's the Members or AllCompletedTasksPage class, pass a User instance
                    pageInstance = (JFrame) pageClass.getDeclaredConstructor(User.class).newInstance(new User(""));
                } else {
                    pageInstance = (JFrame) pageClass.getDeclaredConstructor().newInstance();
                }

                // Dispose the current frame and make the new frame visible
                this.dispose();
                pageInstance.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void index() {
        dispose();
        new index();
        System.out.println("Logging out...");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminPage adminPage = new AdminPage();
            adminPage.setVisible(true);
        });
    }
}
