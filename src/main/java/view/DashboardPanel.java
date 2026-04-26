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

    // income labels
    private JLabel dailyIncomeLabel;
    private JLabel weeklyIncomeLabel;
    private JLabel monthlyIncomeLabel;

    // stock alerts
    private JPanel alertsListPanel;
    private JLabel noAlertsLabel;

    // ai chat
    private JPanel chatHistoryPanel;
    private JScrollPane chatScroll;
    private JTextField chatInput;

    public DashboardPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        // top section — header + income cards stacked
        JPanel topSection = new JPanel(new BorderLayout(0, 16));
        topSection.setBackground(ThemeManager.getBg());
        topSection.add(buildHeader(), BorderLayout.NORTH);
        topSection.add(buildIncomeCards(), BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(buildBottomSection(), BorderLayout.CENTER); // CENTER takes all remaining space

        loadIncomeData();
        loadStockAlerts();
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.getBg());
        header.setBorder(UIUtils.paddingBorder(0, 0, 8, 0));

        JLabel title = UIUtils.createLabel("Dashboard", ThemeManager.FONT_HEADING, ThemeManager.getText());

        JButton newTransactionBtn = UIUtils.createAccentButton("+ New Transaction");
        newTransactionBtn.addActionListener(e -> mainPanel.showPanel("newTransaction"));

        header.add(title, BorderLayout.WEST);
        header.add(newTransactionBtn, BorderLayout.EAST);

        return header;
    }

    // ─────────────────────────────────────────
    // INCOME CARDS
    // ─────────────────────────────────────────
    private JPanel buildIncomeCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setBackground(ThemeManager.getBg());
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        dailyIncomeLabel   = UIUtils.createLabel("₱0.00", ThemeManager.FONT_HEADING, ThemeManager.SUCCESS);
        weeklyIncomeLabel  = UIUtils.createLabel("₱0.00", ThemeManager.FONT_HEADING, ThemeManager.SUCCESS);
        monthlyIncomeLabel = UIUtils.createLabel("₱0.00", ThemeManager.FONT_HEADING, ThemeManager.SUCCESS);

        cards.add(buildIncomeCard("Today", dailyIncomeLabel));
        cards.add(buildIncomeCard("This Week", weeklyIncomeLabel));
        cards.add(buildIncomeCard("This Month", monthlyIncomeLabel));

        return cards;
    }

    private JPanel buildIncomeCard(String period, JLabel valueLabel) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(ThemeManager.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.getBorder(), 1, true),
            UIUtils.paddingBorder(16, 16, 16, 16)
        ));

        JLabel periodLabel = UIUtils.createLabel(period, ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(periodLabel);
        card.add(valueLabel);

        return card;
    }

    private void loadIncomeData() {
        LocalDate today = LocalDate.now();
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

    // ─────────────────────────────────────────
    // BOTTOM SECTION — alerts + ai chat
    // ─────────────────────────────────────────
    private JPanel buildBottomSection() {
        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 0));
        bottom.setBackground(ThemeManager.getBg());

        // 40% alerts, 60% chat — approximate via preferred sizes
        JPanel alertsWrapper = buildAlertsSection();
        JPanel chatWrapper   = buildChatSection();

        bottom.add(alertsWrapper);
        bottom.add(chatWrapper);

        // enforce 40/60 split
        alertsWrapper.setPreferredSize(new Dimension(0, 0));
        chatWrapper.setPreferredSize(new Dimension(0, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, alertsWrapper, chatWrapper);
        split.setResizeWeight(0.4);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(ThemeManager.getBg());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ThemeManager.getBg());
        wrapper.add(split, BorderLayout.CENTER);

        return wrapper;
    }

    // ─────────────────────────────────────────
    // ALERTS SECTION
    // ─────────────────────────────────────────
    private JPanel buildAlertsSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeManager.getBg());

        JLabel title = UIUtils.createLabel("Stock Alerts", ThemeManager.FONT_SUBHEADING, ThemeManager.getText());

        alertsListPanel = new JPanel();
        alertsListPanel.setLayout(new BoxLayout(alertsListPanel, BoxLayout.Y_AXIS));
        alertsListPanel.setBackground(ThemeManager.getSurface());

        noAlertsLabel = UIUtils.createLabel(
            "No stock alerts.",
            ThemeManager.FONT_REGULAR,
            ThemeManager.getSubtext()
        );
        noAlertsLabel.setBorder(UIUtils.paddingBorder(12, 12, 12, 12));
        noAlertsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scroll = UIUtils.createScrollPane(alertsListPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

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
                "Failed to load alerts.",
                ThemeManager.FONT_SMALL,
                ThemeManager.DANGER
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

        JLabel nameLabel = UIUtils.createLabel(
            "#" + product.getId() + " — " + product.getName(),
            ThemeManager.FONT_REGULAR,
            ThemeManager.getText()
        );
        JLabel statusLabel = UIUtils.createLabel(alertText, ThemeManager.FONT_SMALL, alertColor);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(nameLabel, BorderLayout.CENTER);
        row.add(statusLabel, BorderLayout.EAST);

        return row;
    }

    // ─────────────────────────────────────────
    // AI CHAT SECTION
    // ─────────────────────────────────────────
    private JPanel buildChatSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ThemeManager.getBg());

        JLabel title = UIUtils.createLabel("AI Assistant", ThemeManager.FONT_SUBHEADING, ThemeManager.getText());

        // chat history
        chatHistoryPanel = new JPanel();
        chatHistoryPanel.setLayout(new BoxLayout(chatHistoryPanel, BoxLayout.Y_AXIS));
        chatHistoryPanel.setBackground(ThemeManager.getSurface());
        chatHistoryPanel.setBorder(UIUtils.paddingBorder(8, 8, 8, 8));

        chatScroll = UIUtils.createScrollPane(chatHistoryPanel);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // input area
        JPanel inputArea = new JPanel(new BorderLayout(8, 0));
        inputArea.setBackground(ThemeManager.getBg());

        chatInput = UIUtils.createTextField("Ask about inventory or sales...");
        JButton sendBtn = UIUtils.createAccentButton("Send");

        chatInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onSend();
            }
        });
        sendBtn.addActionListener(e -> onSend());

        inputArea.add(chatInput, BorderLayout.CENTER);
        inputArea.add(sendBtn, BorderLayout.EAST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(chatScroll, BorderLayout.CENTER);
        panel.add(inputArea, BorderLayout.SOUTH);

        return panel;
    }

    private void onSend() {
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) return;

        chatInput.setText("");
        addChatBubble("You", userMessage, ThemeManager.ACCENT, Color.WHITE);

        // build context string secretly prepended to the prompt
        String context = buildAiContext();
        String fullPrompt = context + "\nUser question: " + userMessage;

        // run in background so UI doesn't freeze
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return aiIntegration.getAIResponse(fullPrompt);
            }
            @Override
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

        // stock data
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

        // sales data — today, this week, this month
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

        JLabel senderLabel = UIUtils.createLabel(sender, ThemeManager.FONT_BOLD, fg);
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

        // scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ─────────────────────────────────────────
    // THEME
    // ─────────────────────────────────────────
    public void applyTheme() {
        setBackground(ThemeManager.getBg());
        loadIncomeData();
        loadStockAlerts();
        repaint();
        revalidate();
    }
}
