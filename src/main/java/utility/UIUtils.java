/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author k2
 */
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIUtils {

    private UIUtils() {}

    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(ThemeManager.FONT_REGULAR);
        field.setForeground(ThemeManager.getText());
        field.setBackground(ThemeManager.getSurface());
        field.setCaretColor(ThemeManager.getText());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(ThemeManager.FONT_BOLD);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        Color hoverColor = bg.darker();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e)  { button.setBackground(bg); }
        });

        return button;
    }

    public static JButton createAccentButton(String text) {
        return createButton(text, ThemeManager.ACCENT, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, ThemeManager.DANGER, Color.WHITE);
    }

    public static JButton createNeutralButton(String text) {
        return createButton(text, ThemeManager.getBorder(), ThemeManager.getText());
    }

    public static JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(ThemeManager.getSurface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeManager.getBorder());
        return sep;
    }

    public static JScrollPane createScrollPane(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder(), 1));
        scroll.getViewport().setBackground(ThemeManager.getSurface());
        return scroll;
    }

    public static Border paddingBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    // ─────────────────────────────────────────
    // TABLE UTILITIES
    // ─────────────────────────────────────────

    public static JTable createStyledTable(String[] columns) {
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        });
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applyTheme(table);
        return table;
    }

    public static void applyTheme(JTable table) {
        // table body
        table.setBackground(ThemeManager.getSurface());
        table.setForeground(ThemeManager.getText());
        table.setGridColor(ThemeManager.getBorder());
        table.setFont(ThemeManager.FONT_REGULAR);
        table.setSelectionBackground(ThemeManager.ACCENT);
        table.setSelectionForeground(Color.WHITE);

        // header
        JTableHeader header = table.getTableHeader();
        header.setBackground(ThemeManager.getBg());
        header.setForeground(ThemeManager.getSubtext());
        header.setFont(ThemeManager.FONT_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorder()));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        // default cell renderer for text columns
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(isSelected ? ThemeManager.ACCENT : ThemeManager.getSurface());
                setForeground(isSelected ? Color.WHITE : ThemeManager.getText());
                setFont(ThemeManager.FONT_REGULAR);
                setBorder(UIUtils.paddingBorder(0, 8, 0, 8));
                return this;
            }
        };

        // apply default renderer to all columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    // colored status renderer — for the status column
    public static DefaultTableCellRenderer createStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                setBackground(isSelected ? ThemeManager.ACCENT : ThemeManager.getSurface());
                setForeground(isSelected ? Color.WHITE :
                    status.equalsIgnoreCase("available") ? ThemeManager.SUCCESS : ThemeManager.DANGER);
                setFont(ThemeManager.FONT_BOLD);
                setBorder(UIUtils.paddingBorder(0, 8, 0, 8));
                return this;
            }
        };
    }

    // button panel renderer — for the actions column
    public static class ButtonPanelRenderer implements javax.swing.table.TableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof JPanel) {
                ((JPanel) value).setBackground(isSelected ? ThemeManager.ACCENT : ThemeManager.getSurface());
                return (JPanel) value;
            }
            return new JLabel();
        }
    }

    // button panel editor — makes buttons in the actions column actually clickable
    public static class ButtonPanelEditor extends DefaultCellEditor {
        private JPanel panel;

        public ButtonPanelEditor() {
            super(new JCheckBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (value instanceof JPanel) {
                panel = (JPanel) value;
                panel.setBackground(ThemeManager.getSurface());
                return panel;
            }
            return new JLabel();
        }

        @Override
        public Object getCellEditorValue() { return panel; }
    }
}