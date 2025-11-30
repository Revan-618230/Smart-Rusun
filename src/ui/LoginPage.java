package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;


import java.awt.*;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.Queue;

import database.DatabaseAccess;
import database.SecurityCheck;

public class LoginPage extends JFrame {
    private String generatedOtp;

    public LoginPage(String role) {
        setTitle("Login - sRusun (" + role + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(SmartRusunSimulator.bgColor);

        JLabel header = new JLabel("Login - " + role, SwingConstants.CENTER);
        header.setFont(SmartRusunSimulator.labelFont);
        header.setForeground(SmartRusunSimulator.accentColor.darker());
        header.setBorder(new EmptyBorder(30, 0, 30, 0));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(SmartRusunSimulator.bgColor);
        form.setBorder(new EmptyBorder(0, 60, 0, 60));

        JTextField phoneField = new JTextField();
        phoneField.setPreferredSize(new Dimension(300, 40));
        phoneField.setMaximumSize(new Dimension(300, 40));
        phoneField.setFont(SmartRusunSimulator.smallFont);

        JTextField otpField = new JTextField();
        otpField.setPreferredSize(new Dimension(300, 40));
        otpField.setMaximumSize(new Dimension(300, 40));
        otpField.setFont(SmartRusunSimulator.smallFont);
        otpField.setEditable(false);

        JButton requestOtp = new JButton("Request OTP");
        requestOtp.setBackground(SmartRusunSimulator.accentColor);
        requestOtp.setForeground(SmartRusunSimulator.buttonTextColor);
        requestOtp.setEnabled(false);

        JButton login = new JButton("Login");
        login.setBackground(SmartRusunSimulator.primaryColor);
        login.setForeground(SmartRusunSimulator.buttonTextColor);
        login.setEnabled(false);

        JPanel phonePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        phonePanel.setBackground(SmartRusunSimulator.bgColor);
        phonePanel.add(new JLabel("Phone Number:"));
        form.add(phonePanel);
        form.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel phoneFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        phoneFieldPanel.setBackground(SmartRusunSimulator.bgColor);
        phoneFieldPanel.add(phoneField);
        form.add(phoneFieldPanel);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel requestOtpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        requestOtpPanel.setBackground(SmartRusunSimulator.bgColor);
        requestOtpPanel.add(requestOtp);
        form.add(requestOtpPanel);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel otpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        otpPanel.setBackground(SmartRusunSimulator.bgColor);
        otpPanel.add(new JLabel("OTP:"));
        form.add(otpPanel);
        form.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel otpFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        otpFieldPanel.setBackground(SmartRusunSimulator.bgColor);
        otpFieldPanel.add(otpField);
        form.add(otpFieldPanel);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setBackground(SmartRusunSimulator.bgColor);
        loginPanel.add(login);
        form.add(loginPanel);

        JButton backButton = new JButton("Back");
        backButton.setBackground(SmartRusunSimulator.secondaryColor);
        backButton.setForeground(SmartRusunSimulator.buttonTextColor);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(SmartRusunSimulator.bgColor);
        buttonPanel.add(login);
        buttonPanel.add(backButton);
        form.add(buttonPanel);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(SmartRusunSimulator.bgColor);
        centerPanel.add(form);

        content.add(header, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);
        setContentPane(content);

        backButton.addActionListener(e -> {
            new RoleSelectionPage().setVisible(true);
            dispose();
        });

        phoneField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                requestOtp.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                requestOtp.setEnabled(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        requestOtp.addActionListener(e -> {
            generatedOtp = String.format("%06d", (int) (Math.random() * 1000000));
            otpField.setText(generatedOtp);
            JOptionPane.showMessageDialog(null, "OTP Sent Automatically!");
            login.setEnabled(true);
        });

        login.addActionListener(e -> {
            boolean checkCred = false;

            // Fetch the user's 'isActive' status and check if the NIK exists in the Pemilik
            // table
            String enteredPhoneNumber = phoneField.getText().trim(); // Remove leading/trailing spaces
            System.out.println("Entered phone number: " + enteredPhoneNumber);

            Queue<String> temp = DatabaseAccess
                    .getDataSet("SELECT NIK, isActive FROM Pemilik WHERE noPonsel = '" + enteredPhoneNumber + "'");
            String nik = (temp != null) ? temp.poll() : null; // Get NIK first
            String active = (temp != null) ? temp.poll() : "no"; // Then get isActive status

            System.out.println("NIK from database: " + nik);
            System.out.println("isActive status: " + active);

            if (nik == null) {
                JOptionPane.showMessageDialog(null, "NIK not found in the system.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return; // Stop if NIK doesn't exist
            }

            // Check if the user's role and active status are valid
            if (role.equals("Administrator")) {
                if (SecurityCheck.isAdministrator(enteredPhoneNumber) && active.equals("yes"))
                    checkCred = true;
            } else if (role.equals("Pengelola")) {
                if (SecurityCheck.isPengelola(enteredPhoneNumber) && active.equals("yes"))
                    checkCred = true;
            } else {
                if (SecurityCheck.isPemilik(enteredPhoneNumber) && active.equals("yes"))
                    checkCred = true;
            }

            // Check OTP
            System.out.println("Entered OTP: " + otpField.getText());
            System.out.println("Generated OTP: " + generatedOtp);

            if (otpField.getText().equals(generatedOtp) && checkCred) {
                JOptionPane.showMessageDialog(null, "Login Successful!");

                // Insert a new log entry for the login activity
                String insertLogQuery = "INSERT INTO LogPengguna (aktivitas, tanggal, waktu, NIK) VALUES ('Login', CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), '"
                        + nik + "')";
                System.out.println("SQL Query for log insert: " + insertLogQuery);

                boolean isLogInserted = DatabaseAccess.editData(insertLogQuery);

                if (isLogInserted) {
                    System.out.println("Login activity logged successfully.");
                    // Proceed to the main menu after logging the activity
                    new MainMenu(role).setVisible(true);
                    dispose();
                } else {
                    System.out.println("Failed to log login activity.");
                    JOptionPane.showMessageDialog(null, "Failed to insert login activity into logs.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                phoneField.setText("");
                otpField.setText("");
                JOptionPane.showMessageDialog(null, "Login Failed!");
            }
        });
    }
}