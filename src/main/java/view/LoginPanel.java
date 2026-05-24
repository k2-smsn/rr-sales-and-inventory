package view;

import com.mycompany.rrsalesandinventory.Main;
import entity.Account;
import service.AccountService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import utility.ThemeManager;
import utility.UIUtils;
import utility.UserSession;

public class LoginPanel extends JPanel {

    private final AccountService accountService = AccountService.getInstance();

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
        card.setPreferredSize(new Dimension(400, 370));

        JLabel title    = UIUtils.createLabel("Animal Supply Shop", ThemeManager.FONT_HEADING, ThemeManager.getText());
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
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onLogin();
            }
        });

        // show password toggle
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setBackground(ThemeManager.getSurface());
        showPassword.setForeground(ThemeManager.getSubtext());
        showPassword.setFont(ThemeManager.FONT_SMALL);
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPassword.setFocusPainted(false);
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        warningLabel = UIUtils.createLabel(" ", ThemeManager.FONT_SMALL, ThemeManager.DANGER);
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UIUtils.createAccentButton("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginBtn.addActionListener(e -> onLogin());

        // forgot password as a plain link-style label
        JLabel forgotLabel = new JLabel("Forgot password?");
        forgotLabel.setFont(ThemeManager.FONT_SMALL);
        forgotLabel.setForeground(ThemeManager.ACCENT);
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        forgotLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        forgotLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) { onForgotPassword(); }
        });

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
        card.add(Box.createVerticalStrut(6));
        card.add(showPassword);
        card.add(Box.createVerticalStrut(4));
        card.add(warningLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(forgotLabel);

        return card;
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            warningLabel.setText("Please enter both username and password.");
            return;
        }

        warningLabel.setForeground(ThemeManager.getSubtext());
        warningLabel.setText("Logging in...");

        SwingWorker<Account, Void> worker = new SwingWorker<>() {
            protected Account doInBackground() throws Exception {
                return accountService.login(username, password);
            }
            protected void done() {
                try {
                    Account account = get();
                    if (account != null) {
                        UserSession.getInstance().login(account);
                        Main.navigateTo(new MainPanel());
                    } else {
                        warningLabel.setForeground(ThemeManager.DANGER);
                        warningLabel.setText("Invalid username or password.");
                        passwordField.setText("");
                    }
                } catch (Exception e) {
                    warningLabel.setForeground(ThemeManager.DANGER);
                    warningLabel.setText("Database error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void onForgotPassword() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            warningLabel.setText("Enter your username first.");
            return;
        }

        try {
            String question = accountService.getSecurityQuestion(username);
            if (question == null) {
                JOptionPane.showMessageDialog(this,
                    "No account found with that username.",
                    "Not Found", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showSecurityDialog(username, question);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // step 1 — verify security answer
    private void showSecurityDialog(String username, String question) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Forgot Password", true);
        dialog.setSize(380, 240);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        JLabel qLabel = UIUtils.createLabel("Security Question", ThemeManager.FONT_BOLD, ThemeManager.getText());
        JLabel questionLabel = UIUtils.createLabel(question, ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
        qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField answerField = UIUtils.createTextField("Your answer...");
        answerField.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel warningLabel = UIUtils.createLabel(" ", ThemeManager.FONT_SMALL, ThemeManager.DANGER);
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton confirmBtn = UIUtils.createAccentButton("Confirm");
        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmBtn.addActionListener(e -> {
            String answer = answerField.getText().trim();
            if (answer.isEmpty()) { warningLabel.setText("Please enter your answer."); return; }
            try {
                if (accountService.verifySecurityAnswer(username, answer)) {
                    dialog.dispose();
                    showResetPasswordDialog(username);
                } else {
                    warningLabel.setText("Incorrect answer.");
                }
            } catch (SQLException ex) {
                warningLabel.setText("Database error.");
            }
        });

        content.add(qLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(questionLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(UIUtils.createLabel("Answer", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(answerField);
        content.add(Box.createVerticalStrut(4));
        content.add(warningLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(confirmBtn);

        dialog.add(content);
        dialog.setVisible(true);
    }

    // step 2 — set new password
    private void showResetPasswordDialog(String username) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reset Password", true);
        dialog.setSize(380, 240);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        JLabel title = UIUtils.createLabel("Reset Password", ThemeManager.FONT_BOLD, ThemeManager.getText());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField newPassField = new JPasswordField();
        newPassField.setFont(ThemeManager.FONT_REGULAR);
        newPassField.setForeground(ThemeManager.getText());
        newPassField.setBackground(ThemeManager.getSurface());
        newPassField.setCaretColor(ThemeManager.getText());
        newPassField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        newPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JPasswordField confirmPassField = new JPasswordField();
        confirmPassField.setFont(ThemeManager.FONT_REGULAR);
        confirmPassField.setForeground(ThemeManager.getText());
        confirmPassField.setBackground(ThemeManager.getSurface());
        confirmPassField.setCaretColor(ThemeManager.getText());
        confirmPassField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        confirmPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel warningLabel = UIUtils.createLabel(" ", ThemeManager.FONT_SMALL, ThemeManager.DANGER);
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = UIUtils.createAccentButton("Save New Password");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            String newPass     = new String(newPassField.getPassword()).trim();
            String confirmPass = new String(confirmPassField.getPassword()).trim();
            if (newPass.isEmpty()) { warningLabel.setText("Password cannot be empty."); return; }
            if (!newPass.equals(confirmPass)) { warningLabel.setText("Passwords do not match."); return; }
            try {
                accountService.resetPassword(username, newPass);
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    "Password reset successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                warningLabel.setText("Database error.");
            }
        });

        content.add(title);
        content.add(Box.createVerticalStrut(16));
        content.add(UIUtils.createLabel("New Password", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(newPassField);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Confirm Password", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(confirmPassField);
        content.add(Box.createVerticalStrut(4));
        content.add(warningLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(saveBtn);

        dialog.add(content);
        dialog.setVisible(true);
    }
}