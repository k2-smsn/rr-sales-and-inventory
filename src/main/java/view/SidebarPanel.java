/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author k2
 */
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
    private JButton themeBtn;
    private JButton logoutBtn;

    public SidebarPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getSurface());
        setPreferredSize(new Dimension(200, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getBorder()));

        add(buildNav(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    private JPanel buildNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(ThemeManager.getSurface());
        nav.setBorder(UIUtils.paddingBorder(16, 0, 16, 0));

        JLabel appName = UIUtils.createLabel("  Animal Supply", ThemeManager.FONT_BOLD, ThemeManager.getText());
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);
        appName.setBorder(UIUtils.paddingBorder(8, 16, 24, 16));

        dashboardBtn    = buildNavButton("Dashboard");
        inventoryBtn    = buildNavButton("Inventory");
        transactionsBtn = buildNavButton("Transactions");
        reportsBtn      = buildNavButton("Reports");

        dashboardBtn.addActionListener(e    -> mainPanel.showPanel("dashboard"));
        inventoryBtn.addActionListener(e    -> mainPanel.showPanel("inventory"));
        transactionsBtn.addActionListener(e -> mainPanel.showPanel("transactions"));
        reportsBtn.addActionListener(e      -> mainPanel.showPanel("reports"));

        nav.add(appName);
        nav.add(dashboardBtn);
        nav.add(inventoryBtn);
        nav.add(transactionsBtn);
        nav.add(reportsBtn);

        return nav;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(ThemeManager.getSurface());
        bottom.setBorder(UIUtils.paddingBorder(8, 0, 16, 0));

        themeBtn  = buildNavButton(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        logoutBtn = buildNavButton("Logout");

        themeBtn.addActionListener(e -> onToggleTheme());
        logoutBtn.addActionListener(e -> onLogout());

        bottom.add(UIUtils.createSeparator());
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(themeBtn);
        bottom.add(logoutBtn);

        return bottom;
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

        Color hover = ThemeManager.isDarkMode()
            ? ThemeManager.DARK_BORDER
            : ThemeManager.LIGHT_BORDER;

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
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
}