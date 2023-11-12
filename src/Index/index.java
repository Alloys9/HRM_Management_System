package Index;

import Auth.SignInPage;
import Auth.SignUpPage;
import Database.AppDefaults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class index extends JFrame {
    public index() {

        setSize(AppDefaults.DEFAULT_FRAME_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        ImageIcon imageIcon = new ImageIcon("images/welcome.png");
        JLabel imageLabel = new JLabel(imageIcon);

        JLabel headingLabel = new JLabel("<html><h1 style='font-size: 36px; font-weight: bold;'>Welcome To UTOPIA</h1></html>");

        JButton signUpButton = new JButton("Sign Up");
        JButton signInButton = new JButton("Sign In");

        getRootPane().setDefaultButton(null);
        signUpButton.setFocusPainted(false);
        signInButton.setFocusPainted(false);

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new SignUpPage();
            }
        });

        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new SignInPage();
            }
        });

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(imageLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(headingLabel, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(signUpButton);
        buttonPanel.add(signInButton);

        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(buttonPanel, constraints);

        add(panel);

        setVisible(true);
    }


        public static void main(String[] args) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new index();
                }
            });
        }
    }

