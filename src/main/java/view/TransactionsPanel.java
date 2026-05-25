/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author k2
 */
import entity.ItemSold;
import entity.Product;
import entity.Transaction;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import service.TransactionService;
import utility.ThemeManager;
import utility.UIUtils;
import utility.UserSession;

public class TransactionsPanel extends JPanel {

    private final TransactionService transactionService = TransactionService.getInstance();

    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JComboBox<String> statusFilter;
    private JPanel gridPanel;
    private JScrollPane gridScroll;
    private JPanel headerPanel;
    private JPanel controlsPanel;
    private JPanel bodyPanel;
    private JLabel titleLabel;
    private JLabel fromLabel;
    private JLabel toLabel;

    public TransactionsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        setThisMonth();
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private JPanel buildHeader() {
        headerPanel = new JPanel(new BorderLayout(0, 12));
        headerPanel.setBackground(ThemeManager.getBg());
        headerPanel.setBorder(UIUtils.paddingBorder(0, 0, 16, 0));

        titleLabel = UIUtils.createLabel("Transactions", ThemeManager.FONT_HEADING, ThemeManager.getText());

        controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsPanel.setBackground(ThemeManager.getBg());

        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel   = new SpinnerDateModel();

        fromSpinner = new JSpinner(fromModel);
        toSpinner   = new JSpinner(toModel);

        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "MM/dd/yyyy"));
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "MM/dd/yyyy"));

        fromSpinner.setFont(ThemeManager.FONT_REGULAR);
        toSpinner.setFont(ThemeManager.FONT_REGULAR);
        fromSpinner.setPreferredSize(new Dimension(130, 34));
        toSpinner.setPreferredSize(new Dimension(130, 34));

        JButton todayBtn  = UIUtils.createNeutralButton("Today");
        JButton weekBtn   = UIUtils.createNeutralButton("This Week");
        JButton monthBtn  = UIUtils.createNeutralButton("This Month");
        JButton filterBtn = UIUtils.createAccentButton("Filter");

        todayBtn.addActionListener(e  -> setToday());
        weekBtn.addActionListener(e   -> setThisWeek());
        monthBtn.addActionListener(e  -> setThisMonth());
        filterBtn.addActionListener(e -> loadTransactions());

        fromLabel = UIUtils.createLabel("From:", ThemeManager.FONT_REGULAR, ThemeManager.getText());
        toLabel   = UIUtils.createLabel("To:", ThemeManager.FONT_REGULAR, ThemeManager.getText());

        statusFilter = new JComboBox<>(new String[]{ "All", "Active", "Void" });
        statusFilter.setFont(ThemeManager.FONT_REGULAR);
        statusFilter.setPreferredSize(new Dimension(100, 34));

        controlsPanel.add(fromLabel);
        controlsPanel.add(fromSpinner);
        controlsPanel.add(toLabel);
        controlsPanel.add(toSpinner);
        controlsPanel.add(todayBtn);
        controlsPanel.add(weekBtn);
        controlsPanel.add(monthBtn);
        controlsPanel.add(statusFilter);
        controlsPanel.add(filterBtn);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(controlsPanel, BorderLayout.CENTER);

        return headerPanel;
    }

    // ─────────────────────────────────────────
    // BODY
    // ─────────────────────────────────────────
    private JPanel buildBody() {
        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(ThemeManager.getBg());

        gridPanel = new JPanel(new GridLayout(0, 2, 16, 16));
        gridPanel.setBackground(ThemeManager.getBg());

        gridScroll = UIUtils.createScrollPane(gridPanel);
        gridScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gridScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.getViewport().setBackground(ThemeManager.getBg());

        bodyPanel.add(gridScroll, BorderLayout.CENTER);

        return bodyPanel;
    }

    // ─────────────────────────────────────────
    // DATE SHORTCUTS
    // ─────────────────────────────────────────
    private void setToday() {
        Date today = toDate(LocalDate.now());
        fromSpinner.setValue(today);
        toSpinner.setValue(today);
        loadTransactions();
    }

    private void setThisWeek() {
        LocalDate now = LocalDate.now();
        fromSpinner.setValue(toDate(now.with(DayOfWeek.MONDAY)));
        toSpinner.setValue(toDate(now));
        loadTransactions();
    }

    private void setThisMonth() {
        LocalDate now = LocalDate.now();
        fromSpinner.setValue(toDate(now.withDayOfMonth(1)));
        toSpinner.setValue(toDate(now));
        loadTransactions();
    }

    // ─────────────────────────────────────────
    // LOAD
    // ─────────────────────────────────────────
    private void loadTransactions() {
        LocalDate from = toLocalDate((Date) fromSpinner.getValue());
        LocalDate to   = toLocalDate((Date) toSpinner.getValue());

        if (from.isAfter(to)) {
            JOptionPane.showMessageDialog(this,
                "\"From\" date cannot be after \"To\" date.",
                "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String selectedStatus = (String) statusFilter.getSelectedItem();
            List<Transaction> transactions = transactionService.getTransactionsByDateRange(from, to);

            List<Transaction> filtered = transactions.stream()
                .filter(t -> selectedStatus.equals("All") ||
                             t.getStatus().equalsIgnoreCase(selectedStatus))
                .collect(java.util.stream.Collectors.toList());

            renderTransactions(filtered);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load transactions: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
          
    }

    // ─────────────────────────────────────────
    // RENDER
    // ─────────────────────────────────────────
    private void renderTransactions(List<Transaction> transactions) {
        gridPanel.removeAll();

        if (transactions.isEmpty()) {
            JLabel empty = UIUtils.createLabel(
                "No transactions found for the selected date range.",
                ThemeManager.FONT_REGULAR, ThemeManager.getSubtext()
            );
            empty.setBorder(UIUtils.paddingBorder(24, 24, 24, 24));
            gridPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            gridPanel.add(empty);
        } else {
            gridPanel.setLayout(new GridLayout(0, 2, 16, 16));
            for (Transaction transaction : transactions) {
                gridPanel.add(buildTransactionCard(transaction));
            }
            if (transactions.size() % 2 != 0) {
                JPanel filler = new JPanel();
                filler.setBackground(ThemeManager.getBg());
                gridPanel.add(filler);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildTransactionCard(Transaction transaction) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(ThemeManager.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.getBorder(), 1, true),
            UIUtils.paddingBorder(12, 12, 12, 12)
        ));
        card.setPreferredSize(new Dimension(0, 280));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        // top
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeManager.getSurface());

        JLabel idLabel = UIUtils.createLabel(
            "Transaction #" + transaction.getId(), ThemeManager.FONT_BOLD, ThemeManager.getText()
        );
        JLabel dateLabel = UIUtils.createLabel(
            transaction.getCreatedAt().toString(), ThemeManager.FONT_SMALL, ThemeManager.getSubtext()
        );
        // resolve username from account id; fall back gracefully if not found
        String processedByName;
        try {
            entity.Account acc = service.AccountService.getInstance()
                .getAllAccounts().stream()
                .filter(a -> a.getId() == transaction.getProcessedBy())
                .findFirst().orElse(null);
            processedByName = acc != null ? acc.getUsername() : "Unknown";
        } catch (java.sql.SQLException ex) {
            processedByName = "Unknown";
        }
        JLabel processedByLabel = UIUtils.createLabel(
            "By: " + processedByName, ThemeManager.FONT_SMALL, ThemeManager.getSubtext()
        );
        JLabel statusLabel = UIUtils.createLabel(
            transaction.getStatus().toUpperCase(), ThemeManager.FONT_SMALL,
            transaction.getStatus().equalsIgnoreCase("void") ? ThemeManager.DANGER : ThemeManager.SUCCESS
        );
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel idDatePanel = new JPanel(new GridLayout(0, 1));
        idDatePanel.setBackground(ThemeManager.getSurface());
        idDatePanel.add(idLabel);
        idDatePanel.add(dateLabel);
        idDatePanel.add(processedByLabel);

        if (transaction.getStatus().equalsIgnoreCase("void") && transaction.getVoidedBy() != null) {
            String voidedByName;
            try {
                entity.Account acc = service.AccountService.getInstance()
                    .getAllAccounts().stream()
                    .filter(a -> a.getId() == transaction.getVoidedBy())
                    .findFirst().orElse(null);
                voidedByName = acc != null ? acc.getUsername() : "Unknown";
            } catch (java.sql.SQLException ex) {
                voidedByName = "Unknown";
            }
            JLabel voidedByLabel = UIUtils.createLabel(
                "Voided by: " + voidedByName, ThemeManager.FONT_SMALL, ThemeManager.DANGER
            );
            idDatePanel.add(voidedByLabel);
        }

        topBar.add(idDatePanel, BorderLayout.WEST);
        topBar.add(statusLabel, BorderLayout.EAST);

        // receipt
        JPanel receiptPanel = new JPanel();
        receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));
        receiptPanel.setBackground(ThemeManager.getSurface());

        try {
            List<ItemSold> items = transactionService.getItemsByTransactionId(transaction.getId());
            double total = 0;

            receiptPanel.add(buildReceiptRowHeader());
            receiptPanel.add(UIUtils.createSeparator());

            for (ItemSold item : items) {
                Product product    = transactionService.getProductById(item.getProductId());
                String productName = product != null ? product.getName() : "Unknown Product";
                receiptPanel.add(buildReceiptRow(productName, item));
                total += item.getSubTotal().doubleValue();
            }

            receiptPanel.add(UIUtils.createSeparator());

            JPanel totalRow = new JPanel(new BorderLayout());
            totalRow.setBackground(ThemeManager.getSurface());
            totalRow.setBorder(UIUtils.paddingBorder(6, 4, 2, 4));
            JLabel totalLabel = UIUtils.createLabel(
                String.format("Total: ₱%,.2f", total), ThemeManager.FONT_BOLD, ThemeManager.getText()
            );
            totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            totalRow.add(totalLabel, BorderLayout.EAST);
            receiptPanel.add(totalRow);

        } catch (SQLException e) {
            receiptPanel.add(UIUtils.createLabel(
                "Failed to load items.", ThemeManager.FONT_SMALL, ThemeManager.DANGER
            ));
        }

        JScrollPane receiptScroll = new JScrollPane(receiptPanel);
        receiptScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ThemeManager.getBorder()));
        receiptScroll.getViewport().setBackground(ThemeManager.getSurface());
        receiptScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        receiptScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // bottom
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        bottomBar.setBackground(ThemeManager.getSurface());

        if (transaction.getStatus().equalsIgnoreCase("active")) {
            JButton voidBtn = UIUtils.createDangerButton("Cancel Transaction");
            voidBtn.addActionListener(e -> onVoidTransaction(transaction));
            bottomBar.add(voidBtn);
        }

        card.add(topBar, BorderLayout.NORTH);
        card.add(receiptScroll, BorderLayout.CENTER);
        card.add(bottomBar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildReceiptRowHeader() {
        JPanel row = new JPanel(new GridLayout(1, 3));
        row.setBackground(ThemeManager.getSurface());
        row.setBorder(UIUtils.paddingBorder(4, 4, 4, 4));
        row.add(UIUtils.createLabel("Product", ThemeManager.FONT_BOLD, ThemeManager.getSubtext()));
        row.add(UIUtils.createLabel("Qty", ThemeManager.FONT_BOLD, ThemeManager.getSubtext()));
        row.add(UIUtils.createLabel("Subtotal", ThemeManager.FONT_BOLD, ThemeManager.getSubtext()));
        return row;
    }

    private JPanel buildReceiptRow(String productName, ItemSold item) {
        JPanel row = new JPanel(new GridLayout(1, 3));
        row.setBackground(ThemeManager.getSurface());
        row.setBorder(UIUtils.paddingBorder(4, 4, 4, 4));
        row.add(UIUtils.createLabel(productName, ThemeManager.FONT_REGULAR, ThemeManager.getText()));
        row.add(UIUtils.createLabel(item.getQuantity().toPlainString(), ThemeManager.FONT_REGULAR, ThemeManager.getText()));
        row.add(UIUtils.createLabel(String.format("₱%,.2f", item.getSubTotal()), ThemeManager.FONT_REGULAR, ThemeManager.getText()));
        return row;
    }

    // ─────────────────────────────────────────
    // VOID
    // ─────────────────────────────────────────
    private void onVoidTransaction(Transaction transaction) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Transaction #" + transaction.getId() + "?\nThis cannot be undone.",
            "Cancel Transaction", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            try {
                int voidedBy = UserSession.getInstance().getAccountId();
                transactionService.voidTransaction(transaction.getId(), voidedBy);
                loadTransactions();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to void transaction: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─────────────────────────────────────────
    // THEME
    // ─────────────────────────────────────────
    public void applyTheme() {
        setBackground(ThemeManager.getBg());
        headerPanel.setBackground(ThemeManager.getBg());
        controlsPanel.setBackground(ThemeManager.getBg());
        bodyPanel.setBackground(ThemeManager.getBg());
        gridPanel.setBackground(ThemeManager.getBg());
        gridScroll.getViewport().setBackground(ThemeManager.getBg());
        gridScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder(), 1));

        titleLabel.setForeground(ThemeManager.getText());
        fromLabel.setForeground(ThemeManager.getText());
        toLabel.setForeground(ThemeManager.getText());

        loadTransactions();
        repaint();
        revalidate();
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────
    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}