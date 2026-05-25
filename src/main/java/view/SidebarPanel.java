package view;

import com.mycompany.rrsalesandinventory.Main;
import javax.swing.*;
import java.awt.*;
import utility.ThemeManager;
import utility.UIUtils;
import utility.UserSession;

public class SidebarPanel extends JPanel {

    private final MainPanel mainPanel;

    private JButton dashboardBtn;
    private JButton inventoryBtn;
    private JButton transactionsBtn;
    private JButton reportsBtn;
    private JButton accountsBtn; // admin only
    private JButton themeBtn;
    private JButton logoutBtn;
    private JPanel navPanel;
    private JPanel bottomPanel;
    private JLabel appNameLabel;

    public SidebarPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getSurface());
        setPreferredSize(new Dimension(200, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getBorder()));

        dashboardBtn    = buildNavButton("Dashboard");
        inventoryBtn    = buildNavButton("Inventory");
        transactionsBtn = buildNavButton("Transactions");
        reportsBtn      = buildNavButton("Reports");
        accountsBtn     = buildNavButton("Accounts");
        themeBtn        = buildNavButton(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        logoutBtn       = buildNavButton("Logout");

        dashboardBtn.addActionListener(e    -> mainPanel.showPanel("dashboard"));
        inventoryBtn.addActionListener(e    -> mainPanel.showPanel("inventory"));
        transactionsBtn.addActionListener(e -> mainPanel.showPanel("transactions"));
        reportsBtn.addActionListener(e      -> mainPanel.showPanel("reports"));
        accountsBtn.addActionListener(e     -> mainPanel.showPanel("accounts"));
        themeBtn.addActionListener(e        -> onToggleTheme());
        logoutBtn.addActionListener(e       -> onLogout());

        add(buildNav(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    private JPanel buildNav() {
        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(ThemeManager.getSurface());
        navPanel.setBorder(UIUtils.paddingBorder(16, 0, 16, 0));

        appNameLabel = UIUtils.createLabel("  Animal Supply", ThemeManager.FONT_BOLD, ThemeManager.getText());
        appNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        appNameLabel.setBorder(UIUtils.paddingBorder(8, 16, 24, 16));

        navPanel.add(appNameLabel);
        navPanel.add(dashboardBtn);
        navPanel.add(inventoryBtn);
        navPanel.add(transactionsBtn);
        
        // reports and accounts nav only visible to admins
        if (UserSession.getInstance().isAdmin()) {
            navPanel.add(reportsBtn);
            navPanel.add(accountsBtn);
        }

        return navPanel;
    }

    private JPanel buildBottom() {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(ThemeManager.getSurface());
        bottomPanel.setBorder(UIUtils.paddingBorder(8, 0, 16, 0));

        bottomPanel.add(UIUtils.createSeparator());
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(themeBtn);
        bottomPanel.add(logoutBtn);

        return bottomPanel;
    }

    private JButton buildNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeManager.FONT_REGULAR);
        btn.setForeground(ThemeManager.getText());
        btn.setBackground(ThemeManager.getSurface());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(UIUtils.paddingBorder(10, 20, 10, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeManager.getBorder());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeManager.getSurface());
            }
        });

        return btn;
    }

    private void onToggleTheme() {
        ThemeManager.setDarkMode(!ThemeManager.isDarkMode());
        themeBtn.setText(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        mainPanel.applyTheme();
    }

    private void onLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            UserSession.getInstance().logout();
            Main.navigateTo(new LoginPanel());
        }
    }

    public void applyTheme() {
        setBackground(ThemeManager.getSurface());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getBorder()));

        navPanel.setBackground(ThemeManager.getSurface());
        bottomPanel.setBackground(ThemeManager.getSurface());
        appNameLabel.setForeground(ThemeManager.getText());

        for (JButton btn : new JButton[]{
            dashboardBtn, inventoryBtn, transactionsBtn, reportsBtn,
            accountsBtn, themeBtn, logoutBtn
        }) {
            btn.setBackground(ThemeManager.getSurface());
            btn.setForeground(ThemeManager.getText());
        }

        repaint();
        revalidate();
    }
}