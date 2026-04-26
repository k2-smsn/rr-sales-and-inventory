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
    private JPanel gridPanel;
    private JScrollPane gridScroll;

    public TransactionsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        // default load — this month
        setThisMonth();
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setBackground(ThemeManager.getBg());
        header.setBorder(UIUtils.paddingBorder(0, 0, 16, 0));

        JLabel title = UIUtils.createLabel("Transactions", ThemeManager.FONT_HEADING, ThemeManager.getText());

        // date controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(ThemeManager.getBg());

        // spinners
        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel = new SpinnerDateModel();

        fromSpinner = new JSpinner(fromModel);
        toSpinner = new JSpinner(toModel);

        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(fromSpinner, "MM/dd/yyyy");
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(toSpinner, "MM/dd/yyyy");

        fromSpinner.setEditor(fromEditor);
        toSpinner.setEditor(toEditor);

        fromSpinner.setFont(ThemeManager.FONT_REGULAR);
        toSpinner.setFont(ThemeManager.FONT_REGULAR);
        fromSpinner.setPreferredSize(new Dimension(130, 34));
        toSpinner.setPreferredSize(new Dimension(130, 34));

        // shortcut buttons
        JButton todayBtn    = UIUtils.createNeutralButton("Today");
        JButton weekBtn     = UIUtils.createNeutralButton("This Week");
        JButton monthBtn    = UIUtils.createNeutralButton("This Month");
        JButton filterBtn   = UIUtils.createAccentButton("Filter");

        todayBtn.addActionListener(e -> setToday());
        weekBtn.addActionListener(e -> setThisWeek());
        monthBtn.addActionListener(e -> setThisMonth());
        filterBtn.addActionListener(e -> loadTransactions());

        controls.add(UIUtils.createLabel("From:", ThemeManager.FONT_REGULAR, ThemeManager.getText()));
        controls.add(fromSpinner);
        controls.add(UIUtils.createLabel("To:", ThemeManager.FONT_REGULAR, ThemeManager.getText()));
        controls.add(toSpinner);
        controls.add(todayBtn);
        controls.add(weekBtn);
        controls.add(monthBtn);
        controls.add(filterBtn);

        header.add(title, BorderLayout.NORTH);
        header.add(controls, BorderLayout.CENTER);

        return header;
    }

    // ─────────────────────────────────────────
    // BODY
    // ─────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(ThemeManager.getBg());

        gridPanel = new JPanel(new GridLayout(0, 2, 16, 16));
        gridPanel.setBackground(ThemeManager.getBg());

        gridScroll = UIUtils.createScrollPane(gridPanel);
        gridScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gridScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.getViewport().setBackground(ThemeManager.getBg());

        body.add(gridScroll, BorderLayout.CENTER);

        return body;
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
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
        fromSpinner.setValue(toDate(startOfWeek));
        toSpinner.setValue(toDate(now));
        loadTransactions();
    }

    private void setThisMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        fromSpinner.setValue(toDate(startOfMonth));
        toSpinner.setValue(toDate(now));
        loadTransactions();
    }

    // ─────────────────────────────────────────
    // LOAD TRANSACTIONS
    // ─────────────────────────────────────────
    private void loadTransactions() {
        LocalDate from = toLocalDate((Date) fromSpinner.getValue());
        LocalDate to = toLocalDate((Date) toSpinner.getValue());

        if (from.isAfter(to)) {
            JOptionPane.showMessageDialog(this,
                "\"From\" date cannot be after \"To\" date.",
                "Invalid Date Range",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Transaction> transactions = transactionService.getTransactionsByDateRange(from, to);
            renderTransactions(transactions);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load transactions: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ─────────────────────────────────────────
    // RENDER TRANSACTIONS
    // ─────────────────────────────────────────
    private void renderTransactions(List<Transaction> transactions) {
        gridPanel.removeAll();

        if (transactions.isEmpty()) {
            JLabel empty = UIUtils.createLabel(
                "No transactions found for the selected date range.",
                ThemeManager.FONT_REGULAR,
                ThemeManager.getSubtext()
            );
            empty.setBorder(UIUtils.paddingBorder(24, 24, 24, 24));
            gridPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            gridPanel.add(empty);
        } else {
            gridPanel.setLayout(new GridLayout(0, 2, 16, 16));
            for (Transaction transaction : transactions) {
                gridPanel.add(buildTransactionCard(transaction));
            }
            // if odd number, fill last slot with empty panel
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
        System.out.println("Building card for transaction #" + transaction.getId()); //test
        
        try {
            // outer card — fixed size, styled
            JPanel card = new JPanel(new BorderLayout(0, 0));
            card.setBackground(ThemeManager.getSurface());
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ThemeManager.getBorder(), 1, true),
                UIUtils.paddingBorder(12, 12, 12, 12)
            ));
            card.setPreferredSize(new Dimension(0, 280));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

            // — top: id, date, status
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBackground(ThemeManager.getSurface());

            JLabel idLabel = UIUtils.createLabel(
                "Transaction #" + transaction.getId(),
                ThemeManager.FONT_BOLD,
                ThemeManager.getText()
            );
            JLabel dateLabel = UIUtils.createLabel(
                transaction.getCreatedAt().toString(),
                ThemeManager.FONT_SMALL,
                ThemeManager.getSubtext()
            );
            JLabel statusLabel = UIUtils.createLabel(
                transaction.getStatus().toUpperCase(),
                ThemeManager.FONT_SMALL,
                transaction.getStatus().equalsIgnoreCase("void") ? ThemeManager.DANGER : ThemeManager.SUCCESS
            );
            statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel idDatePanel = new JPanel(new GridLayout(2, 1));
            idDatePanel.setBackground(ThemeManager.getSurface());
            idDatePanel.add(idLabel);
            idDatePanel.add(dateLabel);

            topBar.add(idDatePanel, BorderLayout.WEST);
            topBar.add(statusLabel, BorderLayout.EAST);

            // — middle: receipt items in a scroll pane
            JPanel receiptPanel = new JPanel();
            receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));
            receiptPanel.setBackground(ThemeManager.getSurface());

            // load items for this transaction
            System.out.println("About to fetch items..."); //test
            try {
                List<ItemSold> items = transactionService.getItemsByTransactionId(transaction.getId());
                System.out.println("Transaction #" + transaction.getId() + " — items count: " + items.size()); //test
                double total = 0;

                // column header
                JPanel colHeader = buildReceiptRowHeader();
                receiptPanel.add(colHeader);
                receiptPanel.add(UIUtils.createSeparator());

                for (ItemSold item : items) {
                    Product product = transactionService.getProductById(item.getProductId());
                    System.out.println("Item: " + item.getProductId() + " — product: " + (product != null ? product.getName() : "NULL")); //test
                    String productName = product != null ? product.getName() : "Unknown Product";
                    receiptPanel.add(buildReceiptRow(productName, item));
                    total += item.getSubTotal().doubleValue();
                }

                receiptPanel.add(UIUtils.createSeparator());

                // total row
                JPanel totalRow = new JPanel(new BorderLayout());
                totalRow.setBackground(ThemeManager.getSurface());
                totalRow.setBorder(UIUtils.paddingBorder(6, 4, 2, 4));
                JLabel totalLabel = UIUtils.createLabel(
                    String.format("Total: ₱%,.2f", total),
                    ThemeManager.FONT_BOLD,
                    ThemeManager.getText()
                );
                totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                totalRow.add(totalLabel, BorderLayout.EAST);
                receiptPanel.add(totalRow);

            } catch (SQLException e) {
                receiptPanel.add(UIUtils.createLabel("Failed to load items.", ThemeManager.FONT_SMALL, ThemeManager.DANGER));
            }

            JScrollPane receiptScroll = new JScrollPane(receiptPanel);
            receiptScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, ThemeManager.getBorder()));
            receiptScroll.getViewport().setBackground(ThemeManager.getSurface());
            receiptScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            receiptScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            // — bottom: cancel button (admin only)
            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
            bottomBar.setBackground(ThemeManager.getSurface());

            if (UserSession.getInstance().isAdmin()
                    && transaction.getStatus().equalsIgnoreCase("completed")) {
                JButton voidBtn = UIUtils.createDangerButton("Cancel Transaction");
                voidBtn.addActionListener(e -> onVoidTransaction(transaction));
                bottomBar.add(voidBtn);
            }

            card.add(topBar, BorderLayout.NORTH);
            card.add(receiptScroll, BorderLayout.CENTER);
            card.add(bottomBar, BorderLayout.SOUTH);

            return card;
        } catch (Exception e) {
            e.printStackTrace();
            return new JPanel(); // fallback
        }
        
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
        row.add(UIUtils.createLabel(
            item.getQuantity().toPlainString() ,
            ThemeManager.FONT_REGULAR, ThemeManager.getText()
        ));
        row.add(UIUtils.createLabel(
            String.format("₱%,.2f", item.getSubTotal()),
            ThemeManager.FONT_REGULAR, ThemeManager.getText()
        ));
        return row;
    }

    // ─────────────────────────────────────────
    // VOID TRANSACTION
    // ─────────────────────────────────────────
    private void onVoidTransaction(Transaction transaction) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Transaction #" + transaction.getId() + "?\nThis cannot be undone.",
            "Cancel Transaction",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            try {
                transactionService.voidTransaction(transaction.getId());
                loadTransactions();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to void transaction: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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