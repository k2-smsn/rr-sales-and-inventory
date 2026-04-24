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
        if (placeholder != null && !placeholder.isEmpty()) {
            field.putClientProperty("placeholder", placeholder);
        }
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
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        Color hoverColor = bg.darker();
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
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
}
