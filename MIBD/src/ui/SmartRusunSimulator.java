package ui;
import javax.swing.*;

import entities.PerangkatIoT;
import entities.UnitSarusun;

import java.awt.*;
import java.util.ArrayList;

public class SmartRusunSimulator extends JFrame {
    // === Color and Font Configs ===
    // Add 'static' to all constants
    public static final Color bgColor = new Color(255, 248, 240);
    public static final Color primaryColor = new Color(255, 179, 179);
    public static final Color secondaryColor = new Color(179, 223, 255);
    public static final Color accentColor = new Color(179, 255, 197);
    public static final Color buttonTextColor = new Color(51, 51, 51);
    public static final Font buttonFont = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font labelFont = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font smallFont = new Font("Segoe UI", Font.PLAIN, 22);
    public static final Font tableFont = new Font("Segoe UI", Font.PLAIN, 16);

    public static ArrayList<UnitSarusun> unitSarusunList = new ArrayList<>();
    public static ArrayList<PerangkatIoT> perangkatIoTList = new ArrayList<>();
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ui.RoleSelectionPage().setVisible(true));
    }
}