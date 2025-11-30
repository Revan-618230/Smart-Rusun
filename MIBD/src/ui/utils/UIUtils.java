package ui.utils;

import javax.swing.*;

import static ui.SmartRusunSimulator.*;

import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class UIUtils {
    public static JPanel createButtonPanel(JButton... buttons) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        for (JButton btn : buttons) {
            btn.setPreferredSize(new Dimension(400, 80));
            btn.setFont(buttonFont);
            btn.setForeground(buttonTextColor);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor.darker(), 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)));
            panel.add(btn, gbc);
        }
        return panel;
    }

    public static void resizeColumnWidth(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = 50;
            int maxWidth = 300;

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