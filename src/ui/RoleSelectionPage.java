package ui;

import javax.swing.*;

import static ui.SmartRusunSimulator.*;

import java.awt.*;
import javax.swing.border.EmptyBorder;
import ui.utils.UIUtils;

public class RoleSelectionPage extends JFrame {
    public RoleSelectionPage() {
        setTitle("Select Role - sRusun");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(bgColor);

        JLabel header = new JLabel("Select Your Role", SwingConstants.CENTER);
        header.setFont(labelFont);
        header.setForeground(accentColor.darker());
        header.setBorder(new EmptyBorder(30, 0, 30, 0));

        JButton adminButton = new JButton("Administrator");
        adminButton.setBackground(primaryColor);
        JButton managerButton = new JButton("Pengelola");
        managerButton.setBackground(accentColor);
        JButton ownerButton = new JButton("Pemilik Sarusun");
        ownerButton.setBackground(secondaryColor);

        JPanel buttonPanel = UIUtils.createButtonPanel(adminButton, managerButton, ownerButton);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(bgColor);
        centerPanel.add(buttonPanel);

        JLabel footer = new JLabel("© 2025 Ricaldiddy Industries", SwingConstants.CENTER);
        footer.setFont(smallFont);
        footer.setForeground(Color.GRAY);
        footer.setBorder(new EmptyBorder(10, 0, 10, 0));

        content.add(header, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);

        adminButton.addActionListener(e -> {
            new LoginPage("Administrator").setVisible(true);
            dispose();
        });
        managerButton.addActionListener(e -> {
            new LoginPage("Pengelola").setVisible(true);
            dispose();
        });
        ownerButton.addActionListener(e -> {
            new LoginPage("Pemilik Sarusun").setVisible(true);
            dispose();
        });
    }
}