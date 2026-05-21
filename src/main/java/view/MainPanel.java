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
    private SidebarPanel sidebar;

    private DashboardPanel dashboardPanel;
    private InventoryPanel inventoryPanel;
    private TransactionsPanel transactionsPanel;
    private ReportsPanel reportsPanel;

    public MainPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());

        // create sidebar FIRST before any panels
        sidebar = new SidebarPanel(this);

        dashboardPanel    = new DashboardPanel(this);
        inventoryPanel    = new InventoryPanel();
        transactionsPanel = new TransactionsPanel();
        reportsPanel      = new ReportsPanel();

        contentPanel.setBackground(ThemeManager.getBg());
        contentPanel.add(dashboardPanel,    "dashboard");
        contentPanel.add(inventoryPanel,    "inventory");
        contentPanel.add(transactionsPanel, "transactions");
        contentPanel.add(reportsPanel,      "reports");

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPanel("dashboard");
    }

    public void showPanel(String name) {
        if (name.equals("newTransaction")) {
            contentPanel.add(new NewTransactionPanel(this), "newTransaction");
        }
        if (name.equals("inventory")) {
            inventoryPanel.refresh();
        }
        if (name.equals("dashboard")) {
            dashboardPanel.refresh();
        }
        cardLayout.show(contentPanel, name);
    }

    public void hideSidebar() {
        sidebar.setVisible(false);
        revalidate();
        repaint();
    }

    public void showSidebar() {
        sidebar.setVisible(true);
        revalidate();
        repaint();
    }
    
    public void applyTheme() {
        setBackground(ThemeManager.getBg());
        contentPanel.setBackground(ThemeManager.getBg());
        sidebar.applyTheme();
        dashboardPanel.applyTheme();
        inventoryPanel.applyTheme();
        transactionsPanel.applyTheme();
        reportsPanel.applyTheme();
        repaint();
        revalidate();
    }
}
