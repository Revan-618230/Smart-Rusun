package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.Queue;

import database.DatabaseAccess;
import entities.PerangkatIoT;

import ui.dialogs.ManageIotDialog;
import ui.dialogs.ManageOwnersDialog;
import ui.dialogs.ViewActivityLogDialog;
import ui.dialogs.ViewUnitsDialog;
import static ui.utils.UIUtils.createButtonPanel;

public class MainMenu extends JFrame {
    public MainMenu(String role) {
        setTitle("Main Menu - sRusun (" + role + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(SmartRusunSimulator.bgColor);

        JLabel header = new JLabel("Welcome, " + role, SwingConstants.CENTER);
        header.setFont(SmartRusunSimulator.labelFont);
        header.setForeground(SmartRusunSimulator.accentColor.darker());
        header.setBorder(new EmptyBorder(30, 0, 30, 0));

        JPanel buttonPanel = new JPanel();

        if (role.equals("Pengelola")) {
            JButton monitor = new JButton("Monitor Water Usage");
            JButton control = new JButton("Control Water Flow");
            JButton logout = new JButton("Logout");
            monitor.setBackground(SmartRusunSimulator.accentColor);
            control.setBackground(SmartRusunSimulator.secondaryColor);
            logout.setBackground(SmartRusunSimulator.primaryColor);
            buttonPanel = createButtonPanel(monitor, control, logout);

            monitor.addActionListener(e -> {
                // Create buttons for different time periods
                JButton dailyButton = new JButton("Daily");
                JButton monthlyButton = new JButton("Monthly");
                JButton yearlyButton = new JButton("Yearly");
                JButton closeButton = new JButton("Close");
                // Create the DefaultTableModel to display the table
                String[] column = { "idPencatatan", "Jumlah Pemakaian", "Waktu", "Tanggal", "NIK", "SN" };
                DefaultTableModel model = new DefaultTableModel(column, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // Disable editing for all cells
                    }
                };

                // Create the JTable for displaying the results
                JTable table = new JTable(model);
                table.setFont(SmartRusunSimulator.tableFont); // Changed from whatever was there to tableFont
                table.getTableHeader().setFont(SmartRusunSimulator.tableFont);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                table.getColumnModel().getColumn(0).setPreferredWidth(100); // idPencatatan column
                table.getColumnModel().getColumn(1).setPreferredWidth(150); // Jumlah Pemakaian column
                table.getColumnModel().getColumn(2).setPreferredWidth(100); // Waktu column
                table.getColumnModel().getColumn(3).setPreferredWidth(100); // Tanggal column
                table.getColumnModel().getColumn(4).setPreferredWidth(150); // NIK column
                table.getColumnModel().getColumn(5).setPreferredWidth(100); // SN column

                // Create a JScrollPane to hold the table
                JScrollPane scrollPane = new JScrollPane(table);

                // Create a JPanel to hold the time period buttons
                JPanel timePeriodPanel = new JPanel();
                timePeriodPanel.setLayout(new GridLayout(1, 4)); // Layout for buttons
                timePeriodPanel.add(dailyButton);
                timePeriodPanel.add(monthlyButton);
                timePeriodPanel.add(yearlyButton);

                // Create a dialog for displaying the table and set its preferred size to ensure
                // it is larger
                JDialog dialog = new JDialog();
                dialog.setTitle("Water Usage Monitoring");
                dialog.setSize(800, 500); // Increase the size of the dialog
                dialog.setLocationRelativeTo(null); // Center the dialog on the screen

                // Add the button panel and table to the dialog
                dialog.setLayout(new BorderLayout());
                dialog.add(timePeriodPanel, BorderLayout.NORTH); // Add buttons above the table
                dialog.add(scrollPane, BorderLayout.CENTER); // Add the table below the buttons

                // Add action listeners for each button to update the table data
                dailyButton.addActionListener(evt -> {
                    resetTable(model);
                    updateTableData("SELECT * FROM PencatatanAir", model);
                });

                monthlyButton.addActionListener(evt -> {
                    resetTable(model);
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            String query = "SELECT " +
                                    "    p.NIK, " +
                                    "    p.nama, " +
                                    "    YEAR(pa.tanggal) AS Tahun, " +
                                    "    MONTH(pa.tanggal) AS Bulan, " +
                                    "    DATENAME(MONTH, pa.tanggal) AS NamaBulan, " +
                                    "    SUM(pa.jumlahPemakaian) AS TotalPemakaian " +
                                    "FROM " +
                                    "    PencatatanAir pa " +
                                    "JOIN " +
                                    "    Pemilik p ON pa.NIK = p.NIK " +
                                    "GROUP BY " +
                                    "    p.NIK, " +
                                    "    p.nama, " +
                                    "    YEAR(pa.tanggal), " +
                                    "    MONTH(pa.tanggal), " +
                                    "    DATENAME(MONTH, pa.tanggal) " +
                                    "ORDER BY " +
                                    "    Tahun DESC, " +
                                    "    Bulan DESC, " +
                                    "    p.NIK;";

                            try {
                                Queue<String> data = database.DatabaseAccess.getDataSet(query);
                                SwingUtilities.invokeLater(() -> updateTableDataMonth(data, model));
                            } catch (Exception e) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                                        "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE));
                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            monthlyButton.setEnabled(true); // Re-enable button after completion
                        }
                    }.execute();

                    monthlyButton.setEnabled(false); // Disable button during processing
                });

                yearlyButton.addActionListener(evt -> {
                    resetTable(model); // Reset the table before updating with new data
                    updateTableYearly("SELECT " +
                            "    p.NIK, " +
                            "    p.nama, " +
                            "    YEAR(pa.tanggal) AS Tahun, " +
                            "    SUM(pa.jumlahPemakaian) AS TotalPemakaian " +
                            "FROM " +
                            "    PencatatanAir pa " +
                            "JOIN " +
                            "    Pemilik p ON pa.NIK = p.NIK " +
                            "GROUP BY " +
                            "    p.NIK, " +
                            "    p.nama, " +
                            "    YEAR(pa.tanggal) " +
                            "ORDER BY " +
                            "    Tahun DESC, " +
                            "    p.NIK;", model);
                });

                closeButton.addActionListener(closeEvent -> {
                    dialog.dispose(); // Close the dialog
                });
                // Show the dialog
                dialog.setVisible(true);
            });

            control.addActionListener(e -> {
                // Step 1: Retrieve the list of towers dynamically from the UnitSarusun table
                String queryTowers = "SELECT DISTINCT idTower FROM UnitSarusun";
                Queue<String> towersData = database.DatabaseAccess.getDataSet(queryTowers);

                if (towersData == null || towersData.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No towers available.");
                    return;
                }

                // Let the user select a tower from the available towers
                String selectedTower = (String) JOptionPane.showInputDialog(
                        null, "Select Tower", "Select Tower",
                        JOptionPane.PLAIN_MESSAGE, null,
                        towersData.toArray(), towersData.peek());

                // If the user cancels or closes the dialog, stop further execution
                if (selectedTower == null) {
                    return;
                }

                // Step 2: Retrieve active units (UnitSarusun) for the selected tower
                String queryUnits = "SELECT DISTINCT ua.idUnit FROM UnitSarusun ua " +
                        "WHERE ua.idTower = '" + selectedTower + "' AND ua.isActive = 'yes'";
                Queue<String> unitsWithDevices = database.DatabaseAccess.getDataSet(queryUnits);

                if (unitsWithDevices == null || unitsWithDevices.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "No active units available in Tower " + selectedTower + ".");
                    return;
                }

                // Let the user select a unit from the available units
                String selectedUnit = (String) JOptionPane.showInputDialog(
                        null, "Select Unit in Tower " + selectedTower, "Select Unit",
                        JOptionPane.PLAIN_MESSAGE, null,
                        unitsWithDevices.toArray(), unitsWithDevices.peek());

                // If the user cancels or closes the dialog, stop further execution
                if (selectedUnit == null) {
                    return;
                }

                // Step 3: Retrieve active IoT devices associated with the selected unit
                String queryDevices = "SELECT pi.SN, pi.status FROM PerangkatIOT pi " +
                        "JOIN UnitSarusun ua ON pi.idUnit = ua.idUnit " +
                        "WHERE ua.idUnit = '" + selectedUnit + "' AND pi.isActive = 'yes'";
                Queue<String> devicesData = database.DatabaseAccess.getDataSet(queryDevices);

                // Print out the raw data for debugging purposes
                System.out.println("Devices data returned: " + devicesData);

                if (devicesData == null || devicesData.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No devices found for this unit.");
                    return;
                }

                // Step 4: Prepare device options to show in the dialog
                ArrayList<String> deviceOptions = new ArrayList<>();
                while (!devicesData.isEmpty()) {
                    String sn = devicesData.poll(); // SN
                    String status = devicesData.poll(); // Status
                    deviceOptions.add(sn + " (" + status + ")");
                }

                // Step 5: Let the user select a device from the list of available devices
                if (deviceOptions.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No valid devices found for this unit.");
                    return;
                }

                String selectedDevice = (String) JOptionPane.showInputDialog(
                        null, "Select Device in " + selectedUnit, "Device Selection",
                        JOptionPane.PLAIN_MESSAGE, null, deviceOptions.toArray(), deviceOptions.get(0));

                // If the user cancels or closes the dialog, stop further execution
                if (selectedDevice == null) {
                    return;
                }

                // Extract SN (Serial Number) and current status
                String[] parts = selectedDevice.split(" ");
                if (parts.length == 2) {
                    String sn = parts[0];
                    String currentStatus = parts[1].replace("(", "").replace(")", "");
                    String options[] = new String[1];
                    options[0] = (currentStatus.equals("Aktif")) ? "Turn OFF" : "Turn ON";

                    // Step 6: Ask the user if they want to turn the device ON or OFF
                    int result = JOptionPane.showOptionDialog(
                            null, "Control " + sn + " in " + selectedUnit,
                            "Device Control", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE, null,
                            options,
                            currentStatus.equals("Aktif") ? "Turn OFF" : "Turn ON");

                    // If the user cancels or closes the dialog, stop further execution
                    if (result == JOptionPane.CLOSED_OPTION) {
                        return;
                    }

                    String statusTemp = database.DatabaseAccess
                            .getDataSet("SELECT status FROM PerangkatIOT WHERE sn = '" + sn + "'").poll();

                    // Determine the new status based on the user's choice
                    String dispStatus = (statusTemp.equals("Aktif")) ? "OFF" : "ON";
                    String newStatus = (statusTemp.equals("Aktif")) ? "Nonaktif" : "Aktif";

                    // Step 7: Update the device status in the database
                    String updateStatusQuery = "UPDATE PerangkatIOT SET status = '" + newStatus + "' WHERE SN = '"
                            + sn + "'";
                    boolean isUpdated = database.DatabaseAccess.editData(updateStatusQuery);

                    // Log the user's activity in the LogPengguna table
                    String userNIK = database.DatabaseAccess.getLatestNIK(); // Assuming you have a method to get the
                                                                             // current
                    // user's NIK
                    String logQuery = "INSERT INTO LogPengguna (aktivitas, tanggal, waktu, NIK) VALUES (?, ?, ?, ?)";
                    LocalDateTime now = LocalDateTime.now();
                    String tanggal = now.toLocalDate().toString();
                    String waktu = now.toLocalTime().toString();

                    try {
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    } catch (ClassNotFoundException er) {
                        er.printStackTrace();
                    }
                    try (Connection conn = DriverManager.getConnection(
                            database.DatabaseAccess.url);
                            PreparedStatement pstmt = conn.prepareStatement(logQuery)) {
                        pstmt.setString(1, "Turned " + dispStatus + " device " + sn);
                        pstmt.setString(2, tanggal);
                        pstmt.setString(3, waktu);
                        pstmt.setString(4, userNIK);

                        // Execute the insert query
                        pstmt.executeUpdate();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to log activity.");
                    }

                    if (isUpdated) {
                        JOptionPane.showMessageDialog(null, "Device " + sn + " is now " + dispStatus);
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to update device status.");
                    }
                } else {
                    // Handle the case where the device format is not as expected
                    JOptionPane.showMessageDialog(null, "The selected device data is in an unexpected format.");
                }
            });

            logout.addActionListener(e -> {
                // Assuming you have the NIK of the user, you can fetch it from your session or
                // user data
                String NIK = database.DatabaseAccess.getLatestNIK(); // Replace with your method of fetching the user's
                                                                     // NIK

                // Get current date and time
                LocalDateTime now = LocalDateTime.now();
                String tanggal = now.toLocalDate().toString(); // Date in YYYY-MM-DD format
                String waktu = now.toLocalTime().toString(); // Time in HH:MM:SS format

                // SQL query to insert logout log
                String sql = "INSERT INTO LogPengguna (aktivitas, tanggal, waktu, NIK) VALUES (?, ?, ?, ?)";
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } catch (ClassNotFoundException er) {
                    er.printStackTrace();
                }
                try (Connection conn = DriverManager.getConnection(
                        database.DatabaseAccess.url);
                        PreparedStatement stmt = conn.prepareStatement(sql)) {

                    // Set the parameters for the prepared statement
                    stmt.setString(1, "Logout");
                    stmt.setString(2, tanggal);
                    stmt.setString(3, waktu);
                    stmt.setString(4, NIK);

                    // Execute the insert statement
                    stmt.executeUpdate();

                    // Close the current window and show the next page
                    new RoleSelectionPage().setVisible(true);
                    dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    // Handle exception (you may want to show an error message)
                }
            });
        } else if (role.equals("Administrator")) {
            JButton manageOwners = new JButton("Manage Sarusun Owners");
            JButton manageIot = new JButton("Manage IoT Devices");
            JButton viewUnits = new JButton("Manage Sarusun Units");
            JButton viewLog = new JButton("View activity log");
            JButton logout = new JButton("Logout");

            manageOwners.setBackground(SmartRusunSimulator.accentColor);
            manageIot.setBackground(SmartRusunSimulator.secondaryColor);
            viewUnits.setBackground(SmartRusunSimulator.primaryColor);
            viewLog.setBackground(new Color(255, 179, 179));
            logout.setBackground(new Color(255, 150, 150));

            buttonPanel = createButtonPanel(manageOwners, manageIot, viewUnits, viewLog, logout);

            manageOwners.addActionListener(e -> new ManageOwnersDialog().setVisible(true));
            manageIot.addActionListener(e -> new ManageIotDialog().setVisible(true));
            viewUnits.addActionListener(e -> new ViewUnitsDialog().setVisible(true));
            viewLog.addActionListener(e -> new ViewActivityLogDialog().setVisible(true));

            logout.addActionListener(e -> {
                // Assuming you have the NIK of the user, you can fetch it from your session or
                // user data
                String NIK = database.DatabaseAccess.getLatestNIK(); // Replace with your method of fetching the user's
                                                                     // NIK

                // Get current date and time
                LocalDateTime now = LocalDateTime.now();
                String tanggal = now.toLocalDate().toString(); // Date in YYYY-MM-DD format
                String waktu = now.toLocalTime().toString(); // Time in HH:MM:SS format

                // SQL query to insert logout log
                String sql = "INSERT INTO LogPengguna (aktivitas, tanggal, waktu, NIK) VALUES (?, ?, ?, ?)";
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } catch (ClassNotFoundException er) {
                    er.printStackTrace();
                }
                try (Connection conn = DriverManager.getConnection(
                        database.DatabaseAccess.url);
                        PreparedStatement stmt = conn.prepareStatement(sql)) {

                    // Set the parameters for the prepared statement
                    stmt.setString(1, "Logout");
                    stmt.setString(2, tanggal);
                    stmt.setString(3, waktu);
                    stmt.setString(4, NIK);

                    // Execute the insert statement
                    stmt.executeUpdate();

                    // Close the current window and show the next page
                    new RoleSelectionPage().setVisible(true);
                    dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    // Handle exception (you may want to show an error message)
                }
            });

        } else { // Pemilik Sarusun
                 // TODO Pemilik

            JButton controlDevices = new JButton("Control My Devices");
            JButton viewUsage = new JButton("View Water Usage");
            JButton logout = new JButton("Logout");
            controlDevices.setBackground(SmartRusunSimulator.accentColor);
            viewUsage.setBackground(SmartRusunSimulator.secondaryColor);
            logout.setBackground(SmartRusunSimulator.primaryColor);
            buttonPanel = createButtonPanel(controlDevices, viewUsage, logout);

            controlDevices.addActionListener(e -> {
                // Ambil NIK terbaru yang login
                String NIK = database.DatabaseAccess.getLatestNIK();
                System.out.println(NIK);
                if (NIK == null) {
                    JOptionPane.showMessageDialog(null, "No user is logged in", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Ambil unit berdasarkan NIK
                String ownedUnit = database.DatabaseAccess.getUnitByNIK(NIK);
                System.out.println(ownedUnit);
                if (ownedUnit == null) {
                    JOptionPane.showMessageDialog(null, "No unit found for your NIK", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Ambil perangkat yang hanya ada di unit pemilik
                ArrayList<PerangkatIoT> myDevices = new ArrayList<>();

                // Database query untuk mengambil perangkat berdasarkan NIK dan idUnit
                // (ownedUnit)
                String query = "SELECT * FROM PerangkatIOT WHERE NIK = ? AND idUnit = ?";
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } catch (ClassNotFoundException er) {
                    er.printStackTrace();
                }
                try (Connection conn = DriverManager.getConnection(
                        database.DatabaseAccess.url);
                        PreparedStatement stmt = conn.prepareStatement(query)) {

                    // Set parameter query
                    stmt.setString(1, NIK); // NIK pemilik yang sedang login
                    stmt.setString(2, ownedUnit); // Unit yang dimiliki oleh pemilik

                    // Execute query
                    ResultSet rs = stmt.executeQuery();

                    // Ambil data perangkat dari database
                    while (rs.next()) {
                        String sn = rs.getString("SN");
                        String status = rs.getString("status");
                        String unit = rs.getString("idUnit");
                        myDevices.add(new PerangkatIoT(sn, status, unit, "yes"));
                    }

                    // Cek jika tidak ada perangkat yang ditemukan
                    if (myDevices.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No devices found in your unit");
                        return;
                    }

                    // Membuat panel kontrol untuk perangkat yang ditemukan
                    JPanel controlPanel = new JPanel(new GridLayout(0, 1));
                    controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                    // Menambahkan tombol kontrol untuk setiap perangkat yang ditemukan
                    for (PerangkatIoT device : myDevices) {
                        JPanel devicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the content
                        JLabel snLabel = new JLabel(device.sn);

                        // Update button text to reflect whether the device is active or not
                        JToggleButton toggle = new JToggleButton(
                                device.status.equals("Aktif") ? "Active" : "Inactive");
                        toggle.setSelected(device.status.equals("Aktif"));
                        toggle.setBackground(device.status.equals("Aktif") ? Color.GREEN : Color.RED);
                        toggle.setForeground(Color.WHITE);

                        // Set alignment for the button's text to be centered
                        toggle.setHorizontalAlignment(SwingConstants.CENTER);
                        toggle.setPreferredSize(new Dimension(150, 50)); // Adjust the button size to make it more
                                                                         // clickable

                        // Action Listener untuk mengubah status perangkat
                        toggle.addActionListener(evt -> {
                            device.status = toggle.isSelected() ? "Aktif" : "Nonaktif";
                            toggle.setText(device.status.equals("Aktif") ? "Active" : "Inactive");
                            toggle.setBackground(device.status.equals("Aktif") ? Color.GREEN : Color.RED);

                            // Update status perangkat di database (optional)
                            updateDeviceStatusInDatabase(device);

                            // Log activity in LogPengguna table
                            String logQuery = "INSERT INTO LogPengguna (aktivitas, tanggal, waktu, NIK) VALUES (?, ?, ?, ?)";
                            LocalDateTime now = LocalDateTime.now();
                            String tanggal = now.toLocalDate().toString();
                            String waktu = now.toLocalTime().toString();
                            String activity = "Device " + device.sn + " turned "
                                    + (device.status.equals("Aktif") ? "ON" : "OFF");
                            try {
                                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                            } catch (ClassNotFoundException er) {
                                er.printStackTrace();
                            }
                            try (Connection logConn = DriverManager.getConnection(
                                    database.DatabaseAccess.url);
                                    PreparedStatement logStmt = logConn.prepareStatement(logQuery)) {

                                logStmt.setString(1, activity);
                                logStmt.setString(2, tanggal);
                                logStmt.setString(3, waktu);
                                logStmt.setString(4, NIK);

                                logStmt.executeUpdate();
                                System.out.println("Activity logged successfully: " + activity);

                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null,
                                        "Error logging the activity: " + ex.getMessage(), "Database Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });

                        devicePanel.add(snLabel);
                        devicePanel.add(toggle);
                        controlPanel.add(devicePanel);
                    }

                    // Tampilkan panel kontrol
                    JOptionPane.showMessageDialog(null, controlPanel,
                            "My Devices Control", JOptionPane.PLAIN_MESSAGE);

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            viewUsage.addActionListener(e -> {
                JButton dailyButton = new JButton("Daily");
                JButton monthlyButton = new JButton("Monthly");
                JButton yearlyButton = new JButton("Yearly");

                // Create the DefaultTableModel to display the table
                String[] column = { "Jumlah Pemakaian", "Tanggal", "SN" };
                DefaultTableModel model = new DefaultTableModel(column, 0) {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // Disable editing for all cells
                    }
                };

                // Create the JTable for displaying the results
                JTable table = new JTable(
                        model);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                table.getColumnModel().getColumn(0).setPreferredWidth(100); // idPencatatan
                                                                            // column
                table.getColumnModel().getColumn(1).setPreferredWidth(150); // Jumlah Pemakaian column
                table.getColumnModel().getColumn(2).setPreferredWidth(100); // Waktu column

                // Create a JScrollPane to hold the table
                JScrollPane scrollPane = new JScrollPane(table);

                // Create a JPanel to hold the time period buttons
                JPanel timePeriodPanel = new JPanel();
                timePeriodPanel.setLayout(new GridLayout(1, 4)); // Layout for buttons
                timePeriodPanel.add(dailyButton);
                timePeriodPanel.add(monthlyButton);
                timePeriodPanel.add(yearlyButton);

                // Create a dialog for displaying the table and set its preferred size to ensure
                // it is larger
                JDialog dialog = new JDialog();
                dialog.setTitle("Water Usage Monitoring");
                dialog.setSize(800, 500); // Increase the
                                          // size of the
                                          // dialog
                dialog.setLocationRelativeTo(null); // Center the dialog on the screen

                // Add the button panel and table to the dialog
                dialog.setLayout(new BorderLayout());
                dialog.add(timePeriodPanel, BorderLayout.NORTH); // Add buttons above the table
                dialog.add(scrollPane, BorderLayout.CENTER); // Add the table below the buttons

                // Add action listeners for each button to update the table data
                dailyButton.addActionListener(evt -> {
                    // Ambil NIK dari pemilik yang sedang login
                    String NIK = database.DatabaseAccess.getLatestNIK(); // Pastikan fungsi ini mengambil NIK yang benar

                    // Query untuk data harian berdasarkan NIK, hanya menampilkan jumlahPemakaian,
                    // tanggal, dan SN
                    String query = "SELECT jumlahPemakaian, tanggal, SN " +
                            "FROM PencatatanAir " +
                            "WHERE NIK = '" + NIK + "' " + // Filter berdasarkan NIK pemilik
                            "ORDER BY tanggal DESC";

                    // Call the updateTableDataForPemilik method to update the table data
                    updateTableDataForPemilik(query, model);
                });

                monthlyButton.addActionListener(evt -> {
                    resetTable(model);

                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            String NIK = database.DatabaseAccess.getLatestNIK(); // Ambil NIK pemilik yang sedang login

                            // Query untuk data bulanan berdasarkan NIK dan unit sarusun yang dimiliki
                            String query = "SELECT " +
                                    "    YEAR(pa.tanggal) AS Tahun, " +
                                    "    MONTH(pa.tanggal) AS Bulan, " +
                                    "    DATENAME(MONTH, pa.tanggal) AS NamaBulan, " +
                                    "    SUM(pa.jumlahPemakaian) AS TotalPemakaian " +
                                    "FROM " +
                                    "    PencatatanAir pa " +
                                    "JOIN " +
                                    "    Pemilik p ON pa.NIK = p.NIK " +
                                    "WHERE pa.NIK = '" + NIK + "' " + // Inject NIK into the query string
                                    "GROUP BY " +
                                    "    p.NIK, " +
                                    "    p.nama, " +
                                    "    YEAR(pa.tanggal), " +
                                    "    MONTH(pa.tanggal), " +
                                    "    DATENAME(MONTH, pa.tanggal) " +
                                    "ORDER BY " +
                                    "    Tahun DESC, " +
                                    "    Bulan DESC";

                            try {
                                Queue<String> data = database.DatabaseAccess.getDataSet(query); // Pass the query to
                                // getDataSet
                                SwingUtilities.invokeLater(() -> updateTableDataMonthForPemilik(data, model)); // Populate
                                                                                                               // the
                                                                                                               // table
                                                                                                               // with
                                                                                                               // the
                                                                                                               // data
                            } catch (Exception e) {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                                        "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE));
                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            monthlyButton.setEnabled(true);
                        }
                    }.execute();

                    monthlyButton.setEnabled(false); // Disable button during processing
                });

                yearlyButton.addActionListener(evt -> {
                    resetTable(model); // Reset the table before updating with new data
                    updateTableYearlyForPemilik("SELECT " +
                            "    YEAR(pa.tanggal) AS Tahun, " +
                            "    SUM(pa.jumlahPemakaian) AS TotalPemakaian " +
                            "FROM " +
                            "    PencatatanAir pa " +
                            "JOIN " +
                            "    Pemilik p ON pa.NIK = p.NIK " +
                            "GROUP BY " +
                            "    YEAR(pa.tanggal) " +
                            "ORDER BY " +
                            "    Tahun DESC", model);
                });

                // Show the dialog
                dialog.setVisible(true);
            });

            logout.addActionListener(e -> {
                // Assuming you have the NIK of the user, you can fetch it from your session or
                // user data
                String NIK = database.DatabaseAccess.getLatestNIK(); // Replace with your method of fetching the user's
                                                                     // NIK

                // Get current date and time
                LocalDateTime now = LocalDateTime.now();
                String tanggal = now.toLocalDate().toString(); // Date in YYYY-MM-DD format
                String waktu = now.toLocalTime().toString(); // Time in HH:MM:SS format

                // SQL query to insert logout log
                String sql = "INSERT INTO LogPengguna (aktivitas, tanggal, waktu, NIK) VALUES (?, ?, ?, ?)";
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } catch (ClassNotFoundException er) {
                    er.printStackTrace();
                }
                try (
                        Connection conn = DriverManager.getConnection(
                                database.DatabaseAccess.url);
                        PreparedStatement stmt = conn.prepareStatement(sql)) {

                    // Set the parameters for the prepared statement
                    stmt.setString(1, "Logout");
                    stmt.setString(2, tanggal);
                    stmt.setString(3, waktu);
                    stmt.setString(4, NIK);

                    // Execute the insert statement
                    stmt.executeUpdate();

                    // Close the current window and show the next page
                    new RoleSelectionPage().setVisible(true);
                    dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    // Handle exception (you may want to show an error message)
                }
            });
        }

        // Add components to content pane
        content.add(header, BorderLayout.NORTH);
        content.add(buttonPanel, BorderLayout.CENTER);
        setContentPane(content);
    }

    // All the static helper methods from the original class
    private static void resetTable(DefaultTableModel model) {
        model.setRowCount(0);
    }

    private static void updateDeviceStatusInDatabase(PerangkatIoT device) {
        // Query untuk memperbarui status perangkat
        String updateQuery = "UPDATE PerangkatIOT SET status = ?, isActive = ? WHERE SN = ?";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException er) {
            er.printStackTrace();
        }
        try (Connection conn = DriverManager.getConnection(
                database.DatabaseAccess.url);
                PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            // Tentukan nilai parameter untuk query
            stmt.setString(1, device.status); // Status perangkat (Aktif / Nonaktif)
            stmt.setString(2, device.status.equals("Aktif") ? "yes" : "no"); // Menentukan isActive, jika statusnya
                                                                             // "Aktif" maka isActive = "yes", jika
                                                                             // "Nonaktif" maka isActive = "no"
            stmt.setString(3, device.sn); // Serial Number perangkat (SN)

            // Eksekusi query untuk memperbarui status perangkat di database
            stmt.executeUpdate();

        } catch (SQLException ex) {
            // Menangani kesalahan koneksi atau eksekusi query
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Database Update Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateTableData(String query, DefaultTableModel model) {
        // Clear existing data
        model.setRowCount(0);

        // Define the column names as per the table structure
        String[] columns = { "idPencatatan", "Jumlah Pemakaian", "Tanggal", "NIK", "SN" };
        model.setColumnIdentifiers(columns);

        // Fetch data from the database
        Queue<String> temp = database.DatabaseAccess.getDataSet(query);

        // If temp is null or empty, show a message and return
        if (temp == null || temp.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data available for the selected time period.", "No Data",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Debugging: Print the size of the temp queue
        System.out.println("Queue size: " + temp.size());

        // Add data to the table model
        while (!temp.isEmpty()) {
            // Debugging: Print the current contents of the temp queue before processing
            System.out.println("Queue contents: " + temp);

            // Ensure that there are at least 5 elements in the queue before adding to the
            // table
            // As there are 5 columns defined in the model
            if (temp.size() >= 5) {
                model.addRow(new Object[] {
                        temp.poll(), // idPencatatan
                        temp.poll(), // Jumlah Pemakaian
                        temp.poll(), // Tanggal
                        temp.poll(), // NIK
                        temp.poll() // SN
                });
            } else {
                // Handle incomplete data (if necessary)
                System.out.println("Warning: Incomplete data. Skipping row.");
            }
        }
    }

    private static void updateTableDataForPemilik(String query, DefaultTableModel model) {
        // Clear existing data
        model.setRowCount(0);

        // Define the column names as per the table structure
        String[] columns = { "Jumlah Pemakaian", "Tanggal", "SN" };
        model.setColumnIdentifiers(columns);

        // Fetch data from the database
        Queue<String> temp = database.DatabaseAccess.getDataSet(query);

        // If temp is null or empty, show a message and return
        if (temp == null || temp.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data available for the selected time period.", "No Data",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Debugging: Print the size of the temp queue
        System.out.println("Queue size: " + temp.size());

        // Add data to the table model
        while (!temp.isEmpty()) {
            // Debugging: Print the current contents of the temp queue before processing
            System.out.println("Queue contents: " + temp);

            // Ensure that there are at least 5 elements in the queue before adding to the
            // table
            // As there are 5 columns defined in the model
            if (temp.size() >= 3) {
                model.addRow(new Object[] {
                        temp.poll(), // Jumlah Pemakaian
                        temp.poll(), // Tanggal
                        temp.poll() // SN
                });
            } else {
                // Handle incomplete data (if necessary)
                System.out.println("Warning: Incomplete data. Skipping row.");
            }
        }
    }

    private static void updateTableDataMonth(Queue<String> temp, DefaultTableModel model) {
        model.setRowCount(0);

        System.out.println("Data from queue: " + temp); // Debugging

        if (temp == null || temp.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Tidak ada data untuk periode yang dipilih",
                    "Data Kosong",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Pastikan kolom sesuai dengan query
        String[] columns = { "NIK", "Nama Pemilik", "Tahun", "Bulan", "Nama Bulan", "Total Pemakaian" };
        model.setColumnIdentifiers(columns);

        // Ambil data dalam blok 6 item per baris
        while (!temp.isEmpty()) {
            // Cek apakah ada cukup data untuk 1 baris lengkap
            if (temp.size() >= 6) {
                String nik = temp.poll(); // NIK (0)
                String namaPemilik = temp.poll(); // Nama Pemilik (1)
                String tahun = temp.poll(); // Tahun (2)
                String bulan = temp.poll(); // Bulan (angka) (3)
                String namaBulan = temp.poll(); // Nama Bulan (4)
                String totalPemakaian = temp.poll(); // Total Pemakaian (5)

                // Debugging untuk memastikan data yang diambil benar
                System.out.println("Adding row: " + nik + ", " + namaPemilik + ", " + tahun + ", " + bulan + ", "
                        + namaBulan + ", " + totalPemakaian);

                // Menambahkan baris ke model
                model.addRow(new Object[] {
                        nik,
                        namaPemilik,
                        tahun,
                        bulan,
                        namaBulan,
                        totalPemakaian
                });
            } else {
                System.err.println("Data tidak lengkap! Sisa: " + temp.size());
                break; // Keluar jika data tidak lengkap
            }
        }
    }

    private static void updateTableDataMonthForPemilik(Queue<String> temp, DefaultTableModel model) {
        model.setRowCount(0);

        System.out.println("Data from queue: " + temp); // Debugging

        if (temp == null || temp.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Tidak ada data untuk periode yang dipilih",
                    "Data Kosong",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Pastikan kolom sesuai dengan query
        String[] columns = { "Tahun", "Bulan", "Nama Bulan", "Total Pemakaian" };
        model.setColumnIdentifiers(columns);

        // Ambil data dalam blok 6 item per baris
        while (!temp.isEmpty()) {
            // Cek apakah ada cukup data untuk 1 baris lengkap
            if (temp.size() >= 4) {
                String tahun = temp.poll(); // Tahun (2)
                String bulan = temp.poll(); // Bulan (angka) (3)
                String namaBulan = temp.poll(); // Nama Bulan (4)
                String totalPemakaian = temp.poll(); // Total Pemakaian (5)

                // Debugging untuk memastikan data yang diambil benar

                // Menambahkan baris ke model
                model.addRow(new Object[] {

                        tahun,
                        bulan,
                        namaBulan,
                        totalPemakaian
                });
            } else {
                System.err.println("Data tidak lengkap! Sisa: " + temp.size());
                break; // Keluar jika data tidak lengkap
            }
        }
    }

    private static void updateTableYearly(String query, DefaultTableModel model) {
        model.setRowCount(0);
        Queue<String> temp = database.DatabaseAccess.getDataSet(query);

        if (temp == null || temp.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Tidak ada data untuk periode yang dipilih",
                    "Data Kosong",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] columns = { "NIK", "Nama Pemilik", "Tahun", "Total Pemakaian" };
        model.setColumnIdentifiers(columns);

        // Ambil data dalam blok 4 item per baris
        while (!temp.isEmpty()) {
            if (temp.size() >= 4) {
                // Ambil data per baris (NIK, Nama Pemilik, Tahun, Total Pemakaian)
                String nik = temp.poll();
                String nama = temp.poll();
                String tahun = temp.poll();
                String totalPemakaian = temp.poll();

                // Menambahkan baris baru ke model
                model.addRow(new Object[] {
                        nik,
                        nama,
                        tahun,
                        totalPemakaian
                });
            } else {
                // Jika data tidak lengkap
                System.err.println("Data tidak lengkap! Sisa: " + temp.size());
                break; // Keluar jika data tidak lengkap
            }
        }
    }

    private static void updateTableYearlyForPemilik(String query, DefaultTableModel model) {
        model.setRowCount(0);
        Queue<String> temp = database.DatabaseAccess.getDataSet(query);

        if (temp == null || temp.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Tidak ada data untuk periode yang dipilih",
                    "Data Kosong",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] columns = { "Tahun", "Total Pemakaian" };
        model.setColumnIdentifiers(columns);

        // Ambil data dalam blok 4 item per baris
        while (!temp.isEmpty()) {
            if (temp.size() >= 2) {
                String tahun = temp.poll();
                String totalPemakaian = temp.poll();

                // Menambahkan baris baru ke model
                model.addRow(new Object[] {
                        tahun,
                        totalPemakaian
                });
            } else {
                // Jika data tidak lengkap
                System.err.println("Data tidak lengkap! Sisa: " + temp.size());
                break; // Keluar jika data tidak lengkap
            }
        }
    }

    public static void resizeColumnWidth(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = 50; // Minimum width
            int maxWidth = 300; // Max width to avoid infinite growth

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                preferredWidth = Math.max(preferredWidth, c.getPreferredSize().width + 10);
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth(preferredWidth);
        }
    }
}