package Admin.Departments;

import Admin.AdminPage;
import Database.AppDefaults;
import Departments.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Departments extends JFrame {

    private Map<String, Class<?>> pageClasses;
    private List<String> tileImages;
    private JButton departmentsButton;

    public Departments() {
        setTitle("Departments Page");
        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        pageClasses = new HashMap<>();
        pageClasses.put("accounts", Accounts.class);
        pageClasses.put("ceo", CEO.class);
        pageClasses.put("hr", HR.class);
        pageClasses.put("it", IT.class);
        pageClasses.put("legal", Legal.class);
        pageClasses.put("marketing", Marketing.class);
        pageClasses.put("operations", Operations.class);
        pageClasses.put("sales", Sales.class);
        pageClasses.put("secretary", Secretary.class);

        tileImages = new ArrayList<>();
        tileImages.add("images/accounts.png");
        tileImages.add("images/ceo.png");
        tileImages.add("images/hr.png");
        tileImages.add("images/it.png");
        tileImages.add("images/legal.png");
        tileImages.add("images/marketing.png");
        tileImages.add("images/operations.png");
        tileImages.add("images/sales.png");
        tileImages.add("images/secretary.png");


        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.PAGE_AXIS));

        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        ImageIcon imageIcon = new ImageIcon("images/stock.png");
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(imageLabel);


        add(headerPanel, BorderLayout.NORTH);

        JPanel tilesPanel = new JPanel(new GridLayout(3, 3, 20, 20));

        tilesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] tileLabels = {"Accounts", "CEO", "HR", "IT", "Legal", "Marketing", "Operations", "Sales", "Secretary"};
        String[] pageNames = {"accounts", "ceo", "hr", "it", "legal", "marketing", "operations", "sales", "secretary"};

        for (int i = 0; i < tileLabels.length; i++) {
            JPanel tilePanel = new JPanel();
            tilePanel.setLayout(new BorderLayout()); // Use BorderLayout for the tile panel
            tilePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add a border

            // Create a panel for the image and label
            JPanel imageAndLabelPanel = new JPanel();
            imageAndLabelPanel.setLayout(new BoxLayout(imageAndLabelPanel, BoxLayout.PAGE_AXIS));
            imageAndLabelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add an image for the tile
            ImageIcon tileImageIcon = new ImageIcon(tileImages.get(i));
            imageLabel = new JLabel(tileImageIcon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Create a label for the text
            JLabel textLabel = new JLabel(tileLabels[i]);
            textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add the imageLabel and textLabel to the imageAndLabelPanel
            imageAndLabelPanel.add(imageLabel);
            imageAndLabelPanel.add(textLabel);

            // Add a MouseListener to the imageAndLabelPanel to handle the click event
            int finalI = i;
            imageAndLabelPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Open the corresponding page when the tile is clicked
                    openPage(pageNames[finalI]);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    tilePanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    tilePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                }
            });

            tilePanel.add(imageAndLabelPanel, BorderLayout.CENTER);

            tilesPanel.add(tilePanel);
        }

        ImageIcon backIcon = new ImageIcon("images/back.png");
        JLabel backButtonLabel = new JLabel(backIcon);

        backButtonLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                goBackToAdminPage();
            }
        });

        JPanel backButtonPanel = new JPanel();
        backButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.add(backButtonLabel);

        headerPanel.add(backButtonPanel);

        add(tilesPanel, BorderLayout.CENTER);


        setVisible(true);
    }
    private void goBackToAdminPage() {
        AdminPage adminPage = new AdminPage();
        adminPage.setVisible(true);
        this.dispose();
    }

    private void openPage(String pageName) {
        Class<?> pageClass = pageClasses.get(pageName);
        if (pageClass != null) {
            try {

                JFrame page = (JFrame) pageClass.getDeclaredConstructor().newInstance();
                page.setVisible(true);
                this.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}