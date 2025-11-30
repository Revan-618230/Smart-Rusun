package ui.dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.util.Queue;

import database.DatabaseAccess;
import entities.PerangkatIoT;
import ui.SmartRusunSimulator;
import static ui.SmartRusunSimulator.perangkatIoTList;

public class ManageIotDialog extends JDialog {
    private JTable iotTable;
    private DefaultTableModel tableModel;

    public ManageIotDialog() {
        setTitle("Manage IoT Devices");
        setModal(true);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(SmartRusunSimulator.bgColor);

        String[] columns = { "SN", "Status", "Unit", "NIK" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int column, int row) {
                return false;
            }
        };

        iotTable = new JTable(tableModel);
        iotTable.setFont(SmartRusunSimulator.tableFont); // Changed from smallFont to tableFont
        iotTable.getTableHeader().setFont(SmartRusunSimulator.tableFont);
        iotTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        iotTable.getTableHeader().setFont(SmartRusunSimulator.smallFont);
        refreshTableData();

        iotTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Object statusValue = table.getValueAt(row, 1); // Value from column 1

                if (isSelected) {
                    // Keep selection colors
                    c.setForeground(table.getSelectionForeground());
                    c.setBackground(table.getSelectionBackground());
                } else {
                    // Determine row color based on column 1 value
                    if ("Aktif".equals(statusValue)) {
                        c.setForeground(Color.GREEN.darker());
                    } else {
                        c.setForeground(Color.RED.darker());
                    }

                    // Optional: reset background to default
                    c.setBackground(table.getBackground());
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(iotTable);

        JButton addBtn = new JButton("Add Device");
        JButton deleteBtn = new JButton("Delete Device");
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");

        addBtn.setBackground(SmartRusunSimulator.accentColor);
        deleteBtn.setBackground(SmartRusunSimulator.primaryColor);
        refreshBtn.setBackground(SmartRusunSimulator.secondaryColor);
        closeBtn.setBackground(SmartRusunSimulator.secondaryColor);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(SmartRusunSimulator.bgColor);
        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(closeBtn);

        add(new JLabel("IoT Devices", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("Serial Number:"));
            JTextField snField = new JTextField();
            panel.add(snField);

            panel.add(new JLabel("Unit:"));
            JComboBox<String> unitCombo = new JComboBox<>();
            Queue<String> QUnit = DatabaseAccess.getDataSet("select idUnit from UnitSarusun");
            while (!QUnit.isEmpty()) {
                unitCombo.addItem(QUnit.poll());
            }
            panel.add(unitCombo);

            // Add a field for NIK
            panel.add(new JLabel("NIK:"));
            JTextField nikField = new JTextField();
            panel.add(nikField);

            int result = JOptionPane.showConfirmDialog(
                    null, panel, "Add New IoT Device",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION && !snField.getText().isEmpty() && !nikField.getText().isEmpty()) {
                String sn = snField.getText().trim();
                String nik = nikField.getText().trim();

                // Validate that the serial number doesn't already exist
                boolean exists = DatabaseAccess.getDataSet(
                        "SELECT COUNT(*) FROM PerangkatIOT WHERE SN = '" + sn + "'").peek().equals("0") ? false : true;

                if (exists) {
                    JOptionPane.showMessageDialog(this,
                            "Serial number already exists", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Validate if NIK exists in the database
                    Queue<String> temp = DatabaseAccess
                            .getDataSet("SELECT NIK FROM Pemilik WHERE NIK = '" + nik + "'");
                    if (temp == null || temp.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "NIK not found in the system", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Add the new device with the provided NIK
                        perangkatIoTList
                                .add(new PerangkatIoT(sn, (String) unitCombo.getSelectedItem(), "OFF", nik));

                        // Insert into database
                        String insertQuery = "INSERT INTO PerangkatIOT (SN, status, idUnit, isActive, NIK) VALUES ('"
                                + sn + "', 'Aktif', '" + unitCombo.getSelectedItem() + "', 'yes', '" + nik + "')";
                        boolean isInserted = DatabaseAccess.editData(insertQuery);

                        if (isInserted) {
                            JOptionPane.showMessageDialog(this, "Device added successfully.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to add device to the database.");
                        }
                        refreshTableData();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = iotTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an device to delete", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sn = (String) iotTable.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected device?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String active = DatabaseAccess
                        .getDataSet("select isActive from PerangkatIOT where SN = '" + sn + "'").poll();
                if (active.equals("yes"))
                    DatabaseAccess.editData("update PerangkatIOT set isActive = 'no' where SN = '" + sn + "'");
                else
                    DatabaseAccess.editData("update PerangkatIOT set isActive = 'yes' where sn = '" + sn + "'");
                refreshTableData();
            }
        });

        refreshBtn.addActionListener(e -> refreshTableData());
        closeBtn.addActionListener(e -> dispose());
    }

    private void refreshTableData() {
        tableModel.setRowCount(0); // Clear the existing rows
        Queue<String> temp = DatabaseAccess.getDataSet("SELECT * FROM PerangkatIOT");

        // Populate the table with the fetched data from the database
        while (!temp.isEmpty()) {
            String sn = temp.poll();
            String status = temp.poll();
            String idUnit = temp.poll();
            String active = temp.poll(); // Fetch 'active' status
            String nik = temp.poll(); // Fetch 'NIK'

            // Safeguard against null 'active' value
            if (active == null) {
                active = "no"; // Set a default value if 'active' is null
            }

            // Only add active devices to the table
            if ("yes".equals(active)) {
                tableModel.addRow(new Object[] { sn, status, idUnit, nik }); // Add NIK to the table
            }
        }

        resizeColumnWidth(iotTable); // Resize columns to fit the data
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