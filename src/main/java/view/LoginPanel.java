/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import com.mycompany.rrsalesandinventory.AppConfig;
import com.mycompany.rrsalesandinventory.Main;
import javax.swing.JPanel;

/**
 *
 * @author k2
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import utility.ThemeManager;
import utility.UIUtils;
import utility.UserSession;

public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel warningLabel;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(ThemeManager.getBg());
        add(buildCard());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            UIUtils.paddingBorder(32, 40, 32, 40)
        ));
        card.setPreferredSize(new Dimension(380, 320));

        JLabel title = UIUtils.createLabel("Animal Supply Shop", ThemeManager.FONT_HEADING, ThemeManager.getText());
        JLabel subtitle = UIUtils.createLabel("Sign in to continue", ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = UIUtils.createTextField("Username");
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        passwordField = new JPasswordField();
        passwordField.setFont(ThemeManager.FONT_REGULAR);
        passwordField.setForeground(ThemeManager.getText());
        passwordField.setBackground(ThemeManager.getSurface());
        passwordField.setCaretColor(ThemeManager.getText());
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // login on enter from password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onLogin();
            }
        });

        warningLabel = UIUtils.createLabel(" ", ThemeManager.FONT_SMALL, ThemeManager.DANGER);
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UIUtils.createAccentButton("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginBtn.addActionListener(e -> onLogin());

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(UIUtils.createLabel("Username", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(UIUtils.createLabel("Password", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(warningLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(loginBtn);

        return card;
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            warningLabel.setText("Please enter both username and password.");
            return;
        }

        boolean isAdmin = username.equals(AppConfig.getAdminUsername())
            && password.equals(AppConfig.getAdminPassword());

        boolean isStaff = username.equals(AppConfig.getStaffUsername())
            && password.equals(AppConfig.getStaffPassword());

        if (isAdmin) {
            UserSession.getInstance().login(AppConfig.getAdminUsername());
            Main.navigateTo(new MainPanel());
        } else if (isStaff) {
            UserSession.getInstance().login(AppConfig.getStaffUsername());
            Main.navigateTo(new MainPanel());
        } else {
            warningLabel.setText("Invalid username or password.");
            passwordField.setText("");
        }
    }
}
