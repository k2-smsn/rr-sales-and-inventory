package view;

import entity.Account;
import service.AccountService;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import utility.ThemeManager;
import utility.UIUtils;
import utility.UserSession;

public class AccountsPanel extends JPanel {

    private final AccountService accountService = AccountService.getInstance();

    private JPanel listPanel;
    private JPanel headerPanel;
    private JLabel titleLabel;

    public AccountsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadAccounts();
    }

    private JPanel buildHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.getBg());
        headerPanel.setBorder(UIUtils.paddingBorder(0, 0, 16, 0));

        titleLabel = UIUtils.createLabel("Accounts", ThemeManager.FONT_HEADING, ThemeManager.getText());

        JButton addBtn = UIUtils.createAccentButton("+ New Account");
        addBtn.addActionListener(e -> showAddAccountDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addBtn, BorderLayout.EAST);

        return headerPanel;
    }

    private JScrollPane buildBody() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(ThemeManager.getSurface());

        JScrollPane scroll = UIUtils.createScrollPane(listPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scroll;
    }

    private void loadAccounts() {
        listPanel.removeAll();
        try {
            List<Account> accounts = accountService.getAllAccounts();
            if (accounts.isEmpty()) {
                JLabel empty = UIUtils.createLabel("No accounts found.", ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
                empty.setBorder(UIUtils.paddingBorder(12, 12, 12, 12));
                listPanel.add(empty);
            } else {
                for (Account account : accounts) {
                    listPanel.add(buildAccountRow(account));
                    listPanel.add(UIUtils.createSeparator());
                }
            }
        } catch (SQLException e) {
            JLabel error = UIUtils.createLabel("Failed to load accounts: " + e.getMessage(), ThemeManager.FONT_SMALL, ThemeManager.DANGER);
            error.setBorder(UIUtils.paddingBorder(12, 12, 12, 12));
            listPanel.add(error);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildAccountRow(Account account) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(ThemeManager.getSurface());
        row.setBorder(UIUtils.paddingBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        // left — username + role badge
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setBackground(ThemeManager.getSurface());

        JLabel nameLabel = UIUtils.createLabel(account.getUsername(), ThemeManager.FONT_BOLD, ThemeManager.getText());
        JLabel roleLabel = UIUtils.createLabel(account.getRole(), ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
        info.add(nameLabel);
        info.add(roleLabel);

        // right — status badge + toggle button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(ThemeManager.getSurface());

        JLabel statusLabel = UIUtils.createLabel(
            account.getStatus().toUpperCase(),
            ThemeManager.FONT_SMALL,
            account.isActive() ? ThemeManager.SUCCESS : ThemeManager.DANGER
        );

        // prevent admin from disabling their own account
        boolean isSelf = account.getUsername().equals(UserSession.getInstance().getUsername());
        if (!isSelf) {
            JButton toggleBtn = account.isActive()
                ? UIUtils.createDangerButton("Disable")
                : UIUtils.createButton("Enable", ThemeManager.SUCCESS, Color.WHITE);
            toggleBtn.addActionListener(e -> onToggleStatus(account));
            right.add(statusLabel);
            right.add(toggleBtn);
        } else {
            JLabel selfLabel = UIUtils.createLabel("(you)", ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
            right.add(statusLabel);
            right.add(selfLabel);
        }

        row.add(info, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private void onToggleStatus(Account account) {
        String action = account.isActive() ? "disable" : "enable";
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to " + action + " \"" + account.getUsername() + "\"?",
            "Confirm",
            JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            try {
                accountService.toggleStatus(account);
                loadAccounts();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to update account: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddAccountDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Account", true);
        dialog.setSize(330, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        JLabel title = UIUtils.createLabel("New Account", ThemeManager.FONT_BOLD, ThemeManager.getText());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField usernameField = UIUtils.createTextField("Username");
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(280, 36));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(ThemeManager.FONT_REGULAR);
        passwordField.setForeground(ThemeManager.getText());
        passwordField.setBackground(ThemeManager.getSurface());
        passwordField.setCaretColor(ThemeManager.getText());
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(280, 36));

        JComboBox<String> roleBox = new JComboBox<>(new String[]{ "staff", "admin" });
        roleBox.setFont(ThemeManager.FONT_REGULAR);
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleBox.setMaximumSize(new Dimension(280, 36));

        JTextField questionField = UIUtils.createTextField("e.g. What is your pet's name?");
        questionField.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionField.setMaximumSize(new Dimension(280, 36));

        JTextField answerField = UIUtils.createTextField("Answer");
        answerField.setAlignmentX(Component.LEFT_ALIGNMENT);
        answerField.setMaximumSize(new Dimension(280, 36));

        JLabel warningLabel = UIUtils.createWarningLabel();
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = UIUtils.createAccentButton("Create Account");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role     = (String) roleBox.getSelectedItem();
            String question = questionField.getText().trim();
            String answer   = answerField.getText().trim();

            if (username.isEmpty()) { warningLabel.setText("Username is required."); return; }
            if (password.isEmpty()) { warningLabel.setText("Password is required."); return; }
            if (question.isEmpty()) { warningLabel.setText("Security question is required."); return; }
            if (answer.isEmpty())   { warningLabel.setText("Security answer is required."); return; }

            try {
                accountService.createAccount(username, password, role, question, answer);
                dialog.dispose();
                loadAccounts();
            } catch (SQLException ex) {
                if (ex.getMessage().contains("duplicate key") || "23505".equals(ex.getSQLState())) {
                    warningLabel.setText("Username \"" + username + "\" is already taken.");
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Failed to create account: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        content.add(title);
        content.add(Box.createVerticalStrut(16));
        content.add(UIUtils.createLabel("Username", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(usernameField);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Password", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(passwordField);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Role", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(roleBox);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Security Question", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(questionField);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Security Answer", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(answerField);
        content.add(Box.createVerticalStrut(4));
        content.add(warningLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(saveBtn);

        dialog.add(content);
        dialog.setVisible(true);
    }

    public void applyTheme() {
        setBackground(ThemeManager.getBg());
        headerPanel.setBackground(ThemeManager.getBg());
        titleLabel.setForeground(ThemeManager.getText());
        listPanel.setBackground(ThemeManager.getSurface());
        loadAccounts();
        repaint();
        revalidate();
    }

    // called from MainPanel when navigating here
    public void refresh() {
        loadAccounts();
    }
}