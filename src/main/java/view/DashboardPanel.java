/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author k2
 */
import ai.AiIntegration;
import entity.Product;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import service.InventoryService;
import service.ReportsService;
import utility.ThemeManager;
import utility.UIUtils;

public class DashboardPanel extends JPanel {

    private final ReportsService reportsService     = ReportsService.getInstance();
    private final InventoryService inventoryService = InventoryService.getInstance();
    private final AiIntegration aiIntegration       = new AiIntegration();
    private final MainPanel mainPanel;

    // income
    private JLabel dailyIncomeLabel;
    private JLabel weeklyIncomeLabel;
    private JLabel monthlyIncomeLabel;
    private JPanel incomeCardsPanel;
    private JPanel[] incomeCards = new JPanel[3];
    private JLabel[] incomePeriodLabels = new JLabel[3];

    // header
    private JPanel headerPanel;
    private JLabel titleLabel;
    private JPanel topSection;

    // alerts
    private JPanel alertsListPanel;
    private JLabel noAlertsLabel;
    private JPanel alertsSection;
    private JLabel alertsTitleLabel;
    private JScrollPane alertsScroll;

    // chat
    private JPanel chatHistoryPanel;
    private JScrollPane chatScroll;
    private JTextField chatInput;
    private JPanel chatSection;
    private JLabel chatTitleLabel;
    private JPanel inputArea;

    // split
    private JSplitPane splitPane;
    private JPanel bottomWrapper;

    public DashboardPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        topSection = new JPanel(new BorderLayout(0, 16));
        topSection.setBackground(ThemeManager.getBg());
        topSection.add(buildHeader(), BorderLayout.NORTH);
        topSection.add(buildIncomeCards(), BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(buildBottomSection(), BorderLayout.CENTER);

        loadIncomeData();
        loadStockAlerts();
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private JPanel buildHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.getBg());
        headerPanel.setBorder(UIUtils.paddingBorder(0, 0, 8, 0));

        titleLabel = UIUtils.createLabel("Dashboard", ThemeManager.FONT_HEADING, ThemeManager.getText());

        JButton newTransactionBtn = UIUtils.createAccentButton("+ New Transaction");
        newTransactionBtn.addActionListener(e -> mainPanel.showPanel("newTransaction"));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(newTransactionBtn, BorderLayout.EAST);

        return headerPanel;
    }

    // ─────────────────────────────────────────
    // INCOME CARDS
    // ─────────────────────────────────────────
    private JPanel buildIncomeCards() {
        incomeCardsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        incomeCardsPanel.setBackground(ThemeManager.getBg());

        dailyIncomeLabel   = UIUtils.createLabel("₱0.00", ThemeManager.FONT_HEADING, ThemeManager.SUCCESS);
        weeklyIncomeLabel  = UIUtils.createLabel("₱0.00", ThemeManager.FONT_HEADING, ThemeManager.SUCCESS);
        monthlyIncomeLabel = UIUtils.createLabel("₱0.00", ThemeManager.FONT_HEADING, ThemeManager.SUCCESS);

        String[] periods = { "Today", "This Week", "This Month" };
        JLabel[] valueLabels = { dailyIncomeLabel, weeklyIncomeLabel, monthlyIncomeLabel };

        for (int i = 0; i < 3; i++) {
            incomeCards[i] = new JPanel(new GridLayout(2, 1));
            incomeCards[i].setBackground(ThemeManager.getSurface());
            incomeCards[i].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ThemeManager.getBorder(), 1, true),
                UIUtils.paddingBorder(16, 16, 16, 16)
            ));
            incomePeriodLabels[i] = UIUtils.createLabel(periods[i], ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
            valueLabels[i].setHorizontalAlignment(SwingConstants.LEFT);
            incomeCards[i].add(incomePeriodLabels[i]);
            incomeCards[i].add(valueLabels[i]);
            incomeCardsPanel.add(incomeCards[i]);
        }

        return incomeCardsPanel;
    }

    private void loadIncomeData() {
        LocalDate today        = LocalDate.now();
        LocalDate startOfWeek  = today.with(DayOfWeek.MONDAY);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        try {
            BigDecimal daily   = reportsService.getGrossIncome(today, today);
            BigDecimal weekly  = reportsService.getGrossIncome(startOfWeek, today);
            BigDecimal monthly = reportsService.getGrossIncome(startOfMonth, today);

            dailyIncomeLabel.setText(String.format("₱%,.2f", daily));
            weeklyIncomeLabel.setText(String.format("₱%,.2f", weekly));
            monthlyIncomeLabel.setText(String.format("₱%,.2f", monthly));
        } catch (SQLException e) {
            dailyIncomeLabel.setText("Error");
            weeklyIncomeLabel.setText("Error");
            monthlyIncomeLabel.setText("Error");
        }
    }
    
    public void refresh() { //called outside on switch
        loadIncomeData();
        loadStockAlerts();
    }

    // ─────────────────────────────────────────
    // BOTTOM SECTION
    // ─────────────────────────────────────────
    private JPanel buildBottomSection() {
        alertsSection = buildAlertsSection();
        chatSection   = buildChatSection();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, alertsSection, chatSection);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setBackground(ThemeManager.getBg());

        bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setBackground(ThemeManager.getBg());
        bottomWrapper.add(splitPane, BorderLayout.CENTER);

        return bottomWrapper;
    }

    // ─────────────────────────────────────────
    // ALERTS SECTION
    // ─────────────────────────────────────────
    private JPanel buildAlertsSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeManager.getBg());

        alertsTitleLabel = UIUtils.createLabel("Stock Alerts", ThemeManager.FONT_SUBHEADING, ThemeManager.getText());

        alertsListPanel = new JPanel();
        alertsListPanel.setLayout(new BoxLayout(alertsListPanel, BoxLayout.Y_AXIS));
        alertsListPanel.setBackground(ThemeManager.getSurface());

        noAlertsLabel = UIUtils.createLabel("No stock alerts.", ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
        noAlertsLabel.setBorder(UIUtils.paddingBorder(12, 12, 12, 12));
        noAlertsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        alertsScroll = UIUtils.createScrollPane(alertsListPanel);
        alertsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        alertsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(alertsTitleLabel, BorderLayout.NORTH);
        panel.add(alertsScroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadStockAlerts() {
        alertsListPanel.removeAll();
        try {
            List<Product> alerts = inventoryService.getStockAlerts();
            if (alerts.isEmpty()) {
                alertsListPanel.add(noAlertsLabel);
            } else {
                for (Product p : alerts) {
                    alertsListPanel.add(buildAlertRow(p));
                    alertsListPanel.add(UIUtils.createSeparator());
                }
            }
        } catch (SQLException e) {
            alertsListPanel.add(UIUtils.createLabel(
                "Failed to load alerts.", ThemeManager.FONT_SMALL, ThemeManager.DANGER
            ));
        }
        alertsListPanel.revalidate();
        alertsListPanel.repaint();
    }

    private JPanel buildAlertRow(Product product) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(ThemeManager.getSurface());
        row.setBorder(UIUtils.paddingBorder(8, 12, 8, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean outOfStock = product.isOutOfStock();
        Color alertColor   = outOfStock ? ThemeManager.DANGER : ThemeManager.WARNING;
        String alertText   = outOfStock ? "Out of stock" : "Low stock";

        JLabel nameLabel   = UIUtils.createLabel(
            "#" + product.getId() + " — " + product.getName(),
            ThemeManager.FONT_REGULAR, ThemeManager.getText()
        );
        JLabel statusLabel = UIUtils.createLabel(alertText, ThemeManager.FONT_SMALL, alertColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLabel, BorderLayout.CENTER);
        row.add(statusLabel, BorderLayout.EAST);

        return row;
    }

    // ─────────────────────────────────────────
    // CHAT SECTION
    // ─────────────────────────────────────────
    private JPanel buildChatSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeManager.getBg());

        chatTitleLabel = UIUtils.createLabel("AI Assistant", ThemeManager.FONT_SUBHEADING, ThemeManager.getText());

        chatHistoryPanel = new JPanel();
        chatHistoryPanel.setLayout(new BoxLayout(chatHistoryPanel, BoxLayout.Y_AXIS));
        chatHistoryPanel.setBackground(ThemeManager.getSurface());
        chatHistoryPanel.setBorder(UIUtils.paddingBorder(8, 8, 8, 8));

        chatScroll = UIUtils.createScrollPane(chatHistoryPanel);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        inputArea = new JPanel(new BorderLayout(8, 0));
        inputArea.setBackground(ThemeManager.getBg());

        chatInput = UIUtils.createTextField("Ask about inventory or sales...");
        JButton sendBtn = UIUtils.createAccentButton("Send");

        chatInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onSend();
            }
        });
        sendBtn.addActionListener(e -> onSend());

        inputArea.add(chatInput, BorderLayout.CENTER);
        inputArea.add(sendBtn, BorderLayout.EAST);

        panel.add(chatTitleLabel, BorderLayout.NORTH);
        panel.add(chatScroll, BorderLayout.CENTER);
        panel.add(inputArea, BorderLayout.SOUTH);

        return panel;
    }

    private void onSend() {
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) return;

        chatInput.setText("");
        addChatBubble("You", userMessage, ThemeManager.ACCENT, Color.WHITE);

        String context    = buildAiContext();
        String fullPrompt = context + "\nUser question: " + userMessage;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() {
                return aiIntegration.getAIResponse(fullPrompt);
            }
            protected void done() {
                try {
                    String response = get();
                    addChatBubble("AI", response, ThemeManager.getSurface(), ThemeManager.getText());
                } catch (Exception e) {
                    addChatBubble("AI", "Error: " + e.getMessage(), ThemeManager.DANGER, Color.WHITE);
                }
            }
        };
        worker.execute();
    }

    private String buildAiContext() {
        StringBuilder context = new StringBuilder();
        context.append("[SYSTEM CONTEXT - not visible to user]\n");

        try {
            List<Product> products = inventoryService.getAllProducts("");
            context.append("Current Inventory:\n");
            for (Product p : products) {
                context.append(String.format("- %s (ID:%d) | Stock: %s %s | Status: %s | Category: %s | For: %s%n",
                    p.getName(), p.getId(), p.getStockQuantity().toPlainString(),
                    p.getUnit(), p.getStatus(), p.getCategory(), p.getIntendedFor()
                ));
            }
        } catch (SQLException e) {
            context.append("Inventory data unavailable.\n");
        }

        try {
            LocalDate today        = LocalDate.now();
            LocalDate startOfWeek  = today.with(DayOfWeek.MONDAY);
            LocalDate startOfMonth = today.withDayOfMonth(1);

            BigDecimal daily   = reportsService.getGrossIncome(today, today);
            BigDecimal weekly  = reportsService.getGrossIncome(startOfWeek, today);
            BigDecimal monthly = reportsService.getGrossIncome(startOfMonth, today);

            context.append("Sales Summary:\n");
            context.append(String.format("- Today's gross income: ₱%,.2f%n", daily));
            context.append(String.format("- This week's gross income: ₱%,.2f%n", weekly));
            context.append(String.format("- This month's gross income: ₱%,.2f%n", monthly));
        } catch (SQLException e) {
            context.append("Sales data unavailable.\n");
        }

        context.append("[END SYSTEM CONTEXT]\n");
        return context.toString();
    }

    private void addChatBubble(String sender, String message, Color bg, Color fg) {
        JPanel bubble = new JPanel(new BorderLayout(0, 4));
        bubble.setBackground(bg);
        bubble.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.getBorder(), 1, true),
            UIUtils.paddingBorder(8, 10, 8, 10)
        ));
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel senderLabel    = UIUtils.createLabel(sender, ThemeManager.FONT_BOLD, fg);
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(ThemeManager.FONT_REGULAR);
        messageArea.setForeground(fg);
        messageArea.setBackground(bg);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setBorder(null);

        bubble.add(senderLabel, BorderLayout.NORTH);
        bubble.add(messageArea, BorderLayout.CENTER);

        chatHistoryPanel.add(bubble);
        chatHistoryPanel.add(Box.createVerticalStrut(8));
        chatHistoryPanel.revalidate();
        chatHistoryPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ─────────────────────────────────────────
    // THEME
    // ─────────────────────────────────────────
    public void applyTheme() {
        // main panels
        setBackground(ThemeManager.getBg());
        topSection.setBackground(ThemeManager.getBg());
        headerPanel.setBackground(ThemeManager.getBg());
        bottomWrapper.setBackground(ThemeManager.getBg());
        splitPane.setBackground(ThemeManager.getBg());

        // title
        titleLabel.setForeground(ThemeManager.getText());

        // income cards
        incomeCardsPanel.setBackground(ThemeManager.getBg());
        for (int i = 0; i < 3; i++) {
            incomeCards[i].setBackground(ThemeManager.getSurface());
            incomeCards[i].setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ThemeManager.getBorder(), 1, true),
                UIUtils.paddingBorder(16, 16, 16, 16)
            ));
            incomePeriodLabels[i].setForeground(ThemeManager.getSubtext());
        }

        // alerts section
        alertsSection.setBackground(ThemeManager.getBg());
        alertsTitleLabel.setForeground(ThemeManager.getText());
        alertsListPanel.setBackground(ThemeManager.getSurface());
        alertsScroll.getViewport().setBackground(ThemeManager.getSurface());
        alertsScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder(), 1));
        noAlertsLabel.setForeground(ThemeManager.getSubtext());

        // chat section
        chatSection.setBackground(ThemeManager.getBg());
        chatTitleLabel.setForeground(ThemeManager.getText());
        chatHistoryPanel.setBackground(ThemeManager.getSurface());
        chatScroll.getViewport().setBackground(ThemeManager.getSurface());
        chatScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder(), 1));
        inputArea.setBackground(ThemeManager.getBg());
        chatInput.setBackground(ThemeManager.getSurface());
        chatInput.setForeground(ThemeManager.getText());
        chatInput.setCaretColor(ThemeManager.getText());
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // reload data
        loadIncomeData();
        loadStockAlerts();

        repaint();
        revalidate();
    }
}
