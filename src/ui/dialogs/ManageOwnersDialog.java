package ui.dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.util.Queue;
import database.DatabaseAccess;
import entities.Pemilik;
import ui.SmartRusunSimulator;

public class ManageOwnersDialog extends JDialog {
    private JTable ownerTable;
    private DefaultTableModel tableModel;

    public ManageOwnersDialog() {
        setTitle("Manage Sarusun Owners");
        setModal(true);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(SmartRusunSimulator.bgColor);

        String[] columns = { "NIK", "Name", "Address", "Phone", "Role" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ownerTable = new JTable(tableModel);
        ownerTable.setFont(SmartRusunSimulator.tableFont); // Changed from smallFont to tableFont
        ownerTable.getTableHeader().setFont(SmartRusunSimulator.tableFont);
        ownerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ownerTable.getTableHeader().setFont(SmartRusunSimulator.smallFont);
        refreshTableData();

        JScrollPane scrollPane = new JScrollPane(ownerTable);

        JButton addBtn = new JButton("Add Owner");
        JButton editBtn = new JButton("Edit Owner");
        JButton deleteBtn = new JButton("Delete");
        JButton closeBtn = new JButton("Close");

        addBtn.setBackground(SmartRusunSimulator.accentColor);
        editBtn.setBackground(SmartRusunSimulator.secondaryColor);
        deleteBtn.setBackground(SmartRusunSimulator.primaryColor);
        closeBtn.setBackground(SmartRusunSimulator.secondaryColor);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(SmartRusunSimulator.bgColor);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(closeBtn);

        add(new JLabel("Sarusun Owners", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("NIK:"));
            JTextField nikField = new JTextField();
            panel.add(nikField);
            panel.add(new JLabel("Name:"));
            JTextField nameField = new JTextField();
            panel.add(nameField);
            panel.add(new JLabel("Address:"));
            JTextField addressField = new JTextField();
            panel.add(addressField);
            panel.add(new JLabel("Phone:"));
            JTextField phoneField = new JTextField();
            panel.add(phoneField);
            panel.add(new JLabel("Role:"));
            JTextField roleField = new JTextField();
            panel.add(roleField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Add New Owner",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String nik = nikField.getText();
                String name = nameField.getText();
                String address = addressField.getText();
                String phoneNo = phoneField.getText();
                String role = roleField.getText();

                String insert = "'" + nik + "', '" + name + "', '" + address + "', '" + phoneNo + "', '" + role
                        + "', 'yes');";
                DatabaseAccess.editData(
                        "insert into Pemilik (NIK, nama, alamat, noPonsel, peran, isActive) values (" + insert);
                refreshTableData();
            }
        });

        editBtn.addActionListener(e -> {
            int selectedRow = ownerTable.getSelectedRow();
            String currNIK;

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an owner to edit", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                currNIK = (String) ownerTable.getValueAt(selectedRow, 0);
            }

            String name = (String) ownerTable.getValueAt(selectedRow, 1);
            String alamat = (String) ownerTable.getValueAt(selectedRow, 2);
            String noTlp = (String) ownerTable.getValueAt(selectedRow, 3);
            String peran = (String) ownerTable.getValueAt(selectedRow, 4);

            Pemilik owner = new Pemilik(currNIK, name, alamat, noTlp, peran, "yes");
            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("NIK:"));
            JTextField nikField = new JTextField(owner.nik);
            panel.add(nikField);
            panel.add(new JLabel("Name:"));
            JTextField nameField = new JTextField(owner.nama);
            panel.add(nameField);
            panel.add(new JLabel("Address:"));
            JTextField addressField = new JTextField(owner.alamat);
            panel.add(addressField);
            panel.add(new JLabel("Phone:"));
            JTextField phoneField = new JTextField(owner.noPonsel);
            panel.add(phoneField);
            panel.add(new JLabel("Role:"));
            JTextField roleField = new JTextField(owner.peran);
            panel.add(roleField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Edit Owner",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String nik = nikField.getText();
                String nama = nameField.getText();
                String address = addressField.getText();
                String phoneNo = phoneField.getText();

                String insert = "nik = '" + nik + "', nama = '" + nama + "', alamat = '" + address
                        + "', noPonsel = '" + phoneNo + "', peran = 'yes'";
                DatabaseAccess.editData("update Pemilik set " + insert + " where nik = '" + currNIK + "'");
                refreshTableData();
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = ownerTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an owner to delete", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nik = (String) ownerTable.getValueAt(selectedRow, 0);
            // Instead of using the list, check the database directly:
            boolean isAssigned = DatabaseAccess.getDataSet(
                    "SELECT COUNT(*) FROM UnitSarusun WHERE NIK = '" + nik + "' AND isActive = 'yes'").peek()
                    .equals("0") ? false : true;

            if (isAssigned) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete an owner assigned to a unit", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete selected owner?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String active = DatabaseAccess.getDataSet("select isActive from Pemilik where NIK = '" + nik + "'")
                        .poll();
                if (active.equals("yes"))
                    DatabaseAccess.editData("update Pemilik set isActive = 'no' where NIK = '" + nik + "'");
                else
                    DatabaseAccess.editData("update Pemilik set isActive = 'yes' where NIK = '" + nik + "'");
                refreshTableData();
            }
        });

        closeBtn.addActionListener(e -> dispose());
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        Queue<String> temp = DatabaseAccess
                .getDataSet("select NIK, nama, alamat, noPonsel, peran from Pemilik where isActive = 'yes'");
        while (!temp.isEmpty()) {
            tableModel.addRow(new Object[] { temp.poll(), temp.poll(), temp.poll(), temp.poll(), temp.poll() });
        }

        resizeColumnWidth(ownerTable);
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