/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author k2
 */
import javax.swing.*;
import java.awt.*;
import utility.ThemeManager;

public class MainPanel extends JPanel {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private DashboardPanel dashboardPanel;
    private InventoryPanel inventoryPanel;
    private TransactionsPanel transactionsPanel;
    private ReportsPanel reportsPanel;

    public MainPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());

        dashboardPanel    = new DashboardPanel(this);
        inventoryPanel    = new InventoryPanel();
        transactionsPanel = new TransactionsPanel();
        reportsPanel      = new ReportsPanel();

        contentPanel.setBackground(ThemeManager.getBg());
        contentPanel.add(dashboardPanel,    "dashboard");
        contentPanel.add(inventoryPanel,    "inventory");
        contentPanel.add(transactionsPanel, "transactions");
        contentPanel.add(reportsPanel,      "reports");

        SidebarPanel sidebar = new SidebarPanel(this);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPanel("dashboard");
    }

    public void showPanel(String name) {
        cardLayout.show(contentPanel, name);
    }

    public void applyTheme() {
        setBackground(ThemeManager.getBg());
        contentPanel.setBackground(ThemeManager.getBg());
        dashboardPanel.applyTheme();
        inventoryPanel.applyTheme();
        repaint();
        revalidate();
    }
}
