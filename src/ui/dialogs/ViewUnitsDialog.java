package ui.dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import static ui.SmartRusunSimulator.*;

import java.awt.*;
import java.util.Queue;

import database.DatabaseAccess;
import entities.UnitSarusun;

public class ViewUnitsDialog extends JDialog {
    public ViewUnitsDialog() {
        setTitle("Manage Sarusun Units");
        setModal(true);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bgColor);

        String[] columns = { "Unit ID", "Status", "Owner NIK", "Lantai", "Tower" };
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Fetch data from the database
        Queue<String> temp = DatabaseAccess
                .getDataSet("select idUnit, status, nik, idLantai, idTower, isActive from unitSarusun");

        while (!temp.isEmpty()) {
            String idUnit = temp.poll();
            String status = temp.poll();
            String nik = temp.poll();
            nik = (nik == null) ? "N/A" : nik;
            String idLantai = temp.poll();
            String tower = temp.poll(); // New "Tower" field
            String active = temp.poll();

            if (active.equals("yes")) {
                model.addRow(new Object[] { idUnit, status, nik, idLantai, tower });
            }
        }

        // Create the table and add it to the scroll pane
        JTable table = new JTable(model);
        table.setFont(tableFont); // Changed from smallFont to tableFont
        table.getTableHeader().setFont(tableFont); // Also set for header
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER); // Add scrollPane to center

        // Create buttons and panel
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton closeBtn = new JButton("Close");

        addBtn.setBackground(accentColor);
        editBtn.setBackground(secondaryColor);
        deleteBtn.setBackground(primaryColor);
        closeBtn.setBackground(secondaryColor);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(bgColor);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(closeBtn);

        // Add the top label and the button panel
        add(new JLabel("Sarusun Units (View Only)", SwingConstants.CENTER), BorderLayout.NORTH);
        add(btnPanel, BorderLayout.SOUTH);

        // Add button logic for adding a new unit
        addBtn.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("Unit ID:"));
            JTextField idField = new JTextField();
            panel.add(idField);

            panel.add(new JLabel("Status:"));
            JComboBox<String> statusBox = new JComboBox<>(new String[] { "Occupied", "Available" });
            panel.add(statusBox);

            panel.add(new JLabel("Owner NIK:"));
            JTextField nikField = new JTextField();
            panel.add(nikField);

            panel.add(new JLabel("Lantai:"));
            JTextField floorField = new JTextField();
            panel.add(floorField);

            panel.add(new JLabel("Tower:"));
            JTextField towerField = new JTextField();
            panel.add(towerField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Add New Owner",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String id = idField.getText();
                String status = (String) statusBox.getSelectedItem();
                String nik = nikField.getText().equals("") ? null : nikField.getText(); // If empty, set to null
                String floor = floorField.getText();
                String tower = towerField.getText(); // Capture Tower field

                // Check for valid NIK only if the status is "Occupied"
                if (status.equals("Occupied") && (nik == null || nik.isEmpty())) {
                    JOptionPane.showMessageDialog(this, "Owner NIK is required for 'Occupied' status.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Exit the method if NIK is required but not provided
                }

                // If the NIK is not null (when status is "Occupied"), validate NIK exists in
                // the Pemilik table
                if (nik != null && !nik.isEmpty()) {
                    String checkNIKQuery = "SELECT COUNT(*) FROM Pemilik WHERE NIK = '" + nik + "'";
                    Queue<String> checkResult = DatabaseAccess.getDataSet(checkNIKQuery);

                    if (checkResult.peek().equals("0")) {
                        JOptionPane.showMessageDialog(this, "The NIK does not exist in the Pemilik table.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return; // Exit the method if NIK is invalid
                    }
                }

                // Prepare SQL insert statement
                String insert = "'" + id + "', '" + status + "', '" + nik + "', '" + floor + "', '" + tower
                        + "', 'yes')";

                // Execute insert into UnitSarusun
                DatabaseAccess.editData(
                        "insert into UnitSarusun (idUnit, status, nik, idLantai, idTower, isActive) values ("
                                + insert);

                // Refresh the table to show the new record
                refreshTableData(model, table);
            }
        });

        // Edit button logic for editing selected unit
        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            String unitId;

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an owner to edit", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                unitId = (String) table.getValueAt(selectedRow, 0);
            }

            String status = (String) table.getValueAt(selectedRow, 1);
            String nik = (String) table.getValueAt(selectedRow, 2);
            String floor = (String) table.getValueAt(selectedRow, 3);
            String tower = (String) table.getValueAt(selectedRow, 4); // Get Tower value

            UnitSarusun units = new UnitSarusun(unitId, status, nik, floor, tower);
            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("Unit ID:"));
            JTextField UID = new JTextField(units.id);
            panel.add(UID);
            panel.add(new JLabel("Status:"));
            JComboBox<String> statsBox = new JComboBox<>(new String[] { "Occupied", "Available" });
            panel.add(statsBox);
            panel.add(new JLabel("NIK:"));
            JTextField nikF = new JTextField(units.pemilikNik);
            panel.add(nikF);
            panel.add(new JLabel("Lantai:"));
            JTextField lantai = new JTextField(units.lantai);
            panel.add(lantai);
            panel.add(new JLabel("Tower:"));
            JTextField towerF = new JTextField(units.tower);
            panel.add(towerF);

            int result = JOptionPane.showConfirmDialog(null, panel, "Edit Owner",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String UnitID = UID.getText();
                String status_String = (String) statsBox.getSelectedItem();
                String nikS = nikF.getText();
                String lantaiS = lantai.getText();
                String towerS = towerF.getText(); // Capture Tower value

                // Check if the NIK exists in the Pemilik table
                boolean isNikValid = DatabaseAccess.checkIfNikExists(nikS); // A method to check if NIK exists in
                                                                            // Pemilik table

                if (!isNikValid && nikS != null && !nikS.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "The NIK entered does not exist in the database. Please enter a valid NIK.",
                            "Invalid NIK", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String insert = "idUnit = '" + UnitID + "', status = '" + status_String + "', NIK = '" + nikS
                        + "', idLantai = '" + lantaiS + "', idTower = '" + towerS + "', isActive = 'yes'";
                DatabaseAccess.editData("update UnitSarusun set " + insert + " where idUnit = '" + unitId + "'");
            }

            refreshTableData(model, table);
        });

        // Delete button logic for deleting selected unit
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an owner to delete", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String unitId = (String) table.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected Unit?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseAccess.editData("update UnitSarusun set isActive = 'no' where idUnit = '" + unitId + "'");
            }

            refreshTableData(model, table);
        });

        // Close button logic
        closeBtn.addActionListener(e -> dispose());
    }

    private void refreshTableData(DefaultTableModel model, JTable table) {
        // Clear previous table data
        model.setRowCount(0);

        // Fetch updated data from the database
        Queue<String> temp = DatabaseAccess
                .getDataSet("select idUnit, status, nik, idLantai, idTower, isActive from unitSarusun");

        // Add rows to the model from the fetched data
        while (!temp.isEmpty()) {
            String idUnit = temp.poll();
            String status = temp.poll();
            String nik = temp.poll();
            nik = (nik == null) ? "N/A" : nik;
            String idLantai = temp.poll();
            String tower = temp.poll(); // New "Tower" field
            String active = temp.poll();

            if (active.equals("yes")) {
                model.addRow(new Object[] { idUnit, status, nik, idLantai, tower });
            }
        }
        model.fireTableDataChanged(); // Ensure table updates properly
    }

}