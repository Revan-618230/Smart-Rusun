package ui.dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.util.Queue;
import database.DatabaseAccess;
import ui.SmartRusunSimulator;

public class ViewActivityLogDialog extends JDialog {
    public ViewActivityLogDialog() {
        setTitle("View Activity Log");
        setModal(true);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(SmartRusunSimulator.bgColor);

        // Define columns for the JTable (Activity Log columns)
        String[] columns = { "Log ID", "Activity", "Date", "Time", "NIK" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int column, int row) {
                return false;
            }
        };

        // SQL query to fetch activity logs (e.g., Login/Logout)
        Queue<String> temp = DatabaseAccess.getDataSet("SELECT * FROM LogPengguna");

        // Populate the table with fetched data
        while (!temp.isEmpty()) {
            String idLog = temp.poll();
            String activity = temp.poll();
            String date = temp.poll();
            String time = temp.poll();
            String nik = temp.poll();

            // Add row to the model if the activity exists
            model.addRow(new Object[] { idLog, activity, date, time, nik });
        }

        // Create JTable with the model
        JTable table = new JTable(model);
        table.setFont(SmartRusunSimulator.tableFont); // Changed from smallFont to tableFont
        table.getTableHeader().setFont(SmartRusunSimulator.tableFont);
        JScrollPane scrollPane = new JScrollPane(table);
        refreshTableData(model, table);

        // Refresh button to reload the data
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(SmartRusunSimulator.accentColor);

        // Close button to close the dialog
        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(SmartRusunSimulator.secondaryColor);

        // Panel for buttons at the bottom
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(SmartRusunSimulator.bgColor);
        btnPanel.add(refreshBtn);
        btnPanel.add(closeBtn);

        // Add the components to the dialog
        add(new JLabel("Activity Logs", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Refresh button action: reload data into the table
        refreshBtn.addActionListener(e -> refreshTableData(model, table));

        // Close button action: dispose of the dialog
        closeBtn.addActionListener(e -> dispose());
    }

    private void refreshTableData(DefaultTableModel model, JTable table) {
        // Clear existing rows
        model.setRowCount(0);

        // Fetch data from the database again
        Queue<String> temp = DatabaseAccess.getDataSet("SELECT * FROM LogPengguna");

        // Add the fetched data into the table model
        while (!temp.isEmpty()) {
            String idLog = temp.poll();
            String activity = temp.poll();
            String date = temp.poll();
            String time = temp.poll();
            String nik = temp.poll();

            model.addRow(new Object[] { idLog, activity, date, time, nik });
        }

        // Resize the column widths
        resizeColumnWidth(table);
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