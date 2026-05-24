package view;

import javax.swing.*;
import java.awt.*;
import utility.ThemeManager;
import utility.UserSession;

public class MainPanel extends JPanel {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private SidebarPanel sidebar;

    private DashboardPanel dashboardPanel;
    private InventoryPanel inventoryPanel;
    private TransactionsPanel transactionsPanel;
    private ReportsPanel reportsPanel;
    private AccountsPanel accountsPanel;

    public MainPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getBg());

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

        // accounts panel only instantiated and added for admins
        if (UserSession.getInstance().isAdmin()) {
            accountsPanel = new AccountsPanel();
            contentPanel.add(accountsPanel, "accounts");
        }

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
        if (name.equals("accounts") && accountsPanel != null) {
            accountsPanel.refresh();
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
        if (accountsPanel != null) accountsPanel.applyTheme();
        repaint();
        revalidate();
    }
}