package view;

import entity.Product;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import service.InventoryService;
import service.ProductOptionService;
import utility.ThemeManager;
import utility.UIUtils;
import utility.UserSession;

public class InventoryPanel extends JPanel {

    private final InventoryService inventoryService = InventoryService.getInstance();
    private final ProductOptionService optionService = ProductOptionService.getInstance();
    private List<Product> allProducts = new ArrayList<>();

    private JTextField searchField;
    private JPanel gridPanel;       // holds the cards
    private JPanel gridWrapper;     // centers the grid
    private JScrollPane gridScroll;

    private String currentFilter = "all";

    private JButton allFilterBtn;
    private JButton unavailableFilterBtn;
    private JButton lowStockFilterBtn;
    private JButton outOfStockFilterBtn;
    private JButton expiringSoonFilterBtn;
    private JButton expiredFilterBtn;

    private JPanel headerPanel;
    private JPanel topRow;
    private JPanel filterRow;
    private JPanel rightPanel;
    private JLabel titleLabel;

    // fixed card dimensions
    private static final int CARD_WIDTH  = 240;
    private static final int CARD_HEIGHT = 310;
    private static final int CARDS_PER_ROW = 4;
    private static final int CARD_GAP = 12;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public InventoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        loadProducts();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        updateFilterButtons();
    }

    // ─────────────────────────────────────────
    // LOAD
    // ─────────────────────────────────────────

    private void loadProducts() {
        try {
            allProducts = inventoryService.getAllProducts("");
        } catch (SQLException e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────

    private JPanel buildHeader() {
        headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setBackground(ThemeManager.getBg());
        headerPanel.setBorder(UIUtils.paddingBorder(0, 0, 16, 0));

        topRow = new JPanel(new BorderLayout());
        topRow.setBackground(ThemeManager.getBg());

        titleLabel = UIUtils.createLabel("Inventory", ThemeManager.FONT_HEADING, ThemeManager.getText());

        // search label + field wrapped together
        JLabel searchLabel = UIUtils.createLabel("Search: ", ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
        searchField = UIUtils.createTextField("Search products...");
        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { renderCurrentSearch(); }
            public void removeUpdate(DocumentEvent e)  { renderCurrentSearch(); }
            public void changedUpdate(DocumentEvent e) { renderCurrentSearch(); }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        searchPanel.setBackground(ThemeManager.getBg());
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(ThemeManager.getBg());
        rightPanel.add(searchPanel);

        if (UserSession.getInstance().isAdmin()) {
            JButton manageOptionsBtn = UIUtils.createButton("Manage Options", ThemeManager.getBorder(), ThemeManager.getText());
            manageOptionsBtn.addActionListener(e -> showManageOptionsDialog());
            rightPanel.add(manageOptionsBtn);

            JButton addProductBtn = UIUtils.createAccentButton("+ Add Product");
            addProductBtn.addActionListener(e -> showAddProductDialog());
            rightPanel.add(addProductBtn);
        }

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(rightPanel, BorderLayout.EAST);

        filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setBackground(ThemeManager.getBg());

        allFilterBtn          = buildFilterButton("All", "all");
        unavailableFilterBtn  = buildFilterButton("Unavailable", "unavailable");
        lowStockFilterBtn     = buildFilterButton("Low Stock", "low_stock");
        outOfStockFilterBtn   = buildFilterButton("Out of Stock", "out_of_stock");
        expiringSoonFilterBtn = buildFilterButton("Expiring Soon", "expiring_soon");
        expiredFilterBtn      = buildFilterButton("Expired", "expired");

        filterRow.add(allFilterBtn);
        filterRow.add(unavailableFilterBtn);
        filterRow.add(lowStockFilterBtn);
        filterRow.add(outOfStockFilterBtn);
        filterRow.add(expiringSoonFilterBtn);
        filterRow.add(expiredFilterBtn);

        headerPanel.add(topRow, BorderLayout.NORTH);
        headerPanel.add(filterRow, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JButton buildFilterButton(String label, String filterKey) {
        JButton btn = UIUtils.createFilterButton(label);
        btn.addActionListener(e -> {
            currentFilter = filterKey;
            updateFilterButtons();
            renderCurrentSearch();
        });
        return btn;
    }

    private void updateFilterButtons() {
        updateFilterButtonStyle(allFilterBtn, "all");
        updateFilterButtonStyle(unavailableFilterBtn, "unavailable");
        updateFilterButtonStyle(lowStockFilterBtn, "low_stock");
        updateFilterButtonStyle(outOfStockFilterBtn, "out_of_stock");
        updateFilterButtonStyle(expiringSoonFilterBtn, "expiring_soon");
        updateFilterButtonStyle(expiredFilterBtn, "expired");
    }

    private void updateFilterButtonStyle(JButton btn, String filterKey) {
        boolean isActive = currentFilter.equals(filterKey);
        btn.setBackground(isActive ? ThemeManager.ACCENT : ThemeManager.getBorder());
        btn.setForeground(isActive ? Color.WHITE : ThemeManager.getText());
    }

    // ─────────────────────────────────────────
    // BODY
    // ─────────────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(ThemeManager.getBg());

        // fixed 4-column grid — wraps naturally as products are added
        gridPanel = new JPanel(new GridLayout(0, CARDS_PER_ROW, CARD_GAP, CARD_GAP));
        gridPanel.setBackground(ThemeManager.getBg());

        // wrapper gives the grid padding inside the scroll pane
        gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(ThemeManager.getBg());
        gridWrapper.setBorder(BorderFactory.createEmptyBorder(CARD_GAP, CARD_GAP, CARD_GAP, CARD_GAP));
        gridWrapper.add(gridPanel, BorderLayout.NORTH);

        gridScroll = UIUtils.createScrollPane(gridWrapper);
        gridScroll.setBorder(null);
        gridScroll.getViewport().setBackground(ThemeManager.getBg());
        gridScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        body.add(gridScroll, BorderLayout.CENTER);
        renderCurrentSearch();
        return body;
    }

    // ─────────────────────────────────────────
    // RENDER
    // ─────────────────────────────────────────

    private void renderCurrentSearch() {
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        List<Product> filtered = allProducts.stream()
            .filter(p -> p.getName().toLowerCase().startsWith(query))
            .filter(p -> switch (currentFilter) {
                case "unavailable"   -> p.getStatus().equalsIgnoreCase("unavailable");
                case "low_stock"     -> !p.isOutOfStock()
                                     && p.getStockQuantity().compareTo(BigDecimal.valueOf(7)) <= 0
                                     && !p.getStatus().equalsIgnoreCase("unavailable");
                case "out_of_stock"  -> p.isOutOfStock()
                                     && !p.getStatus().equalsIgnoreCase("unavailable");
                case "expiring_soon" -> p.isExpiringSoon()
                                     && !p.getStatus().equalsIgnoreCase("unavailable");
                case "expired"       -> p.isExpired()
                                     && !p.getStatus().equalsIgnoreCase("unavailable");
                default              -> true;
            })
            .collect(java.util.stream.Collectors.toList());

        renderProducts(filtered);
    }

    private void renderProducts(List<Product> products) {
        gridPanel.removeAll();

        if (products.isEmpty()) {
            JLabel empty = UIUtils.createLabel("No products found.", ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
            gridPanel.add(empty);
        } else {
            for (Product product : products) {
                gridPanel.add(buildProductCard(product));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            UIUtils.paddingBorder(14, 14, 14, 14)
        ));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        // name + status badge row
        JPanel nameRow = new JPanel(new BorderLayout(4, 0));
        nameRow.setBackground(ThemeManager.getSurface());
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel nameLabel   = UIUtils.createLabel(product.getName(), ThemeManager.FONT_BOLD, ThemeManager.getText());
        JLabel statusBadge = UIUtils.createLabel(
            product.getStatus(),
            ThemeManager.FONT_SMALL,
            product.getStatus().equalsIgnoreCase("available") ? ThemeManager.SUCCESS : ThemeManager.DANGER
        );
        statusBadge.setHorizontalAlignment(SwingConstants.RIGHT);
        nameRow.add(nameLabel, BorderLayout.CENTER);
        nameRow.add(statusBadge, BorderLayout.EAST);

        // id + category + intended for subtext
        JLabel subLabel = UIUtils.createLabel(
            "ID: " + product.getId() + "  •  " + product.getCategory() + "  •  " + product.getIntendedFor(),
            ThemeManager.FONT_SMALL, ThemeManager.getSubtext()
        );
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // price per unit
        JLabel priceLabel = UIUtils.createLabel(
            String.format("₱%,.2f / %s", product.getPricePerUnit(), product.getUnit()),
            ThemeManager.FONT_REGULAR, ThemeManager.getText()
        );
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // stock with alert indicator
        JLabel stockLabel = UIUtils.createLabel(buildStockText(product), ThemeManager.FONT_REGULAR, getStockColor(product));
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // expiration with alert indicator
        JLabel expLabel = buildExpirationLabel(product);
        expLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // sales count
        JLabel salesLabel = UIUtils.createLabel(
            "Sales: " + product.getSales(), ThemeManager.FONT_REGULAR, ThemeManager.getSubtext()
        );
        salesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // action buttons — admin only
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setBackground(ThemeManager.getSurface());
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        if (UserSession.getInstance().isAdmin()) {
            JButton restockBtn = UIUtils.createAccentButton("Restock");
            JButton toggleBtn  = product.getStatus().equalsIgnoreCase("available")
                ? UIUtils.createDangerButton("Mark Unavailable")
                : UIUtils.createButton("Mark Available", ThemeManager.SUCCESS, Color.WHITE);

            restockBtn.addActionListener(e -> showRestockDialog(product));
            toggleBtn.addActionListener(e -> onToggleAvailability(product));

            btnRow.add(restockBtn);
            btnRow.add(toggleBtn);
        } else {
            btnRow.add(UIUtils.createLabel("View only", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        }

        card.add(nameRow);
        card.add(Box.createVerticalStrut(3));
        card.add(subLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(priceLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(stockLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(expLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(salesLabel);
        card.add(Box.createVerticalGlue());
        card.add(btnRow);

        return card;
    }

    // builds stock text — !! out of stock, ! low stock
    private String buildStockText(Product product) {
        BigDecimal stock = product.getStockQuantity();
        String base = "Stock: " + stock.toPlainString() + " " + product.getUnit();
        if (stock.compareTo(BigDecimal.ZERO) <= 0) return base + "  !!";
        if (stock.compareTo(BigDecimal.valueOf(7)) <= 0) return base + "  !";
        return base;
    }

    private Color getStockColor(Product product) {
        if (product.isOutOfStock()) return new Color(180, 0, 0);
        if (product.getStockQuantity().compareTo(BigDecimal.valueOf(7)) <= 0) return new Color(160, 100, 0);
        return ThemeManager.getText();
    }

    // builds expiration label — !! expired, ! expiring soon, plain otherwise
    private JLabel buildExpirationLabel(Product product) {
        if (product.getExpirationDate() == null) {
            return UIUtils.createLabel("Expires: None", ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
        }
        String dateStr = product.getExpirationDate().format(DATE_FORMATTER);
        String text;
        Color color;
        if (product.isExpired()) {
            text  = "Expires: " + dateStr + "  !!";
            color = new Color(180, 0, 0);
        } else if (product.isExpiringSoon()) {
            text  = "Expires: " + dateStr + "  !";
            color = new Color(160, 100, 0);
        } else {
            text  = "Expires: " + dateStr;
            color = ThemeManager.getText();
        }
        return UIUtils.createLabel(text, ThemeManager.FONT_REGULAR, color);
    }

    // ─────────────────────────────────────────
    // MANAGE OPTIONS DIALOG
    // ─────────────────────────────────────────

    private void showManageOptionsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Manage Product Options", true);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new GridLayout(1, 2, 16, 0));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(20, 20, 20, 20));

        content.add(buildOptionSection(dialog, "Intended For", "intended_for"));
        content.add(buildOptionSection(dialog, "Category", "category"));

        dialog.add(content);
        dialog.setVisible(true);
    }

    // builds one column (Intended For or Category) inside the manage options dialog
    private JPanel buildOptionSection(JDialog dialog, String title, String type) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setBackground(ThemeManager.getSurface());

        JLabel titleLabel = UIUtils.createLabel(title, ThemeManager.FONT_BOLD, ThemeManager.getText());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(ThemeManager.getSurface());

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder()));
        scrollPane.getViewport().setBackground(ThemeManager.getSurface());

        JTextField inputField = UIUtils.createTextField("Add new...");
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        // fixed-height warning label so layout never shifts when text appears
        JLabel warningLabel = UIUtils.createWarningLabel();

        JButton addBtn = UIUtils.createAccentButton("Add");

        Runnable reloadList = () -> reloadOptionList(listPanel, type, dialog);

        addBtn.addActionListener(e -> {
            String val = inputField.getText().trim();
            if (val.isEmpty()) { warningLabel.setText("Cannot be empty."); return; }

            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Add \"" + val + "\" to " + title + "?",
                "Confirm Add",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                optionService.addOption(type, val);
                inputField.setText("");
                warningLabel.setText(" ");
                reloadList.run();
            } catch (IllegalArgumentException ex) {
                warningLabel.setText(ex.getMessage());
            } catch (SQLException ex) {
                // 23505 = PostgreSQL unique violation
                if (ex.getMessage().contains("duplicate key") || ex.getSQLState().equals("23505")) {
                    warningLabel.setText("\"" + val + "\" already exists.");
                } else {
                    showError("Failed to add option: " + ex.getMessage());
                }
            }
        });

        reloadList.run();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(ThemeManager.getSurface());
        bottomPanel.add(inputField);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(warningLabel);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(addBtn);

        section.add(titleLabel, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);
        section.add(bottomPanel, BorderLayout.SOUTH);

        return section;
    }

    // reloads and repaints the list panel for a given option type
    private void reloadOptionList(JPanel listPanel, String type, JDialog dialog) {
        listPanel.removeAll();
        try {
            List<String> options = optionService.getByType(type);
            for (String val : options) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(ThemeManager.getSurface());
                row.setBorder(UIUtils.paddingBorder(4, 8, 4, 8));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

                JLabel valLabel = UIUtils.createLabel(val, ThemeManager.FONT_REGULAR, ThemeManager.getText());

                // createDangerIconButton uses fixed sizing so ✕ is never clipped
                JButton deleteBtn = UIUtils.createDangerIconButton("✕");

                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(
                        dialog,
                        "Delete \"" + val + "\" from " + type + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (confirm != JOptionPane.YES_OPTION) return;
                    try {
                        optionService.deleteOption(type, val);
                        reloadOptionList(listPanel, type, dialog);
                    } catch (SQLException ex) {
                        showError("Failed to delete option: " + ex.getMessage());
                    }
                });

                row.add(valLabel, BorderLayout.CENTER);
                row.add(deleteBtn, BorderLayout.EAST);
                listPanel.add(row);
            }
        } catch (SQLException ex) {
            showError("Failed to load options: " + ex.getMessage());
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    // ─────────────────────────────────────────
    // RESTOCK DIALOG
    // ─────────────────────────────────────────

    private void showRestockDialog(Product product) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Restock — " + product.getName(), true);
        dialog.setSize(360, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(20, 20, 20, 20));

        JLabel titleLabel = UIUtils.createLabel(
            "Restock — " + product.getName(), ThemeManager.FONT_BOLD, ThemeManager.getText()
        );
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel currentLabel = UIUtils.createLabel(
            "Current stock: " + product.getStockQuantity().toPlainString() + " " + product.getUnit(),
            ThemeManager.FONT_SMALL, ThemeManager.getSubtext()
        );
        currentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // amount label makes the field purpose clear
        JLabel amountLabel = UIUtils.createLabel("Amount to Add", ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField amountField = UIUtils.createTextField("e.g. 10, 2.5");
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel warningLabel = UIUtils.createWarningLabel();
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton confirmBtn = UIUtils.createAccentButton("Restock");
        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        confirmBtn.addActionListener(e -> {
            String raw = amountField.getText().trim();
            if (raw.isEmpty()) { warningLabel.setText("Please enter an amount."); return; }

            BigDecimal amount;
            try {
                amount = new BigDecimal(raw);
            } catch (NumberFormatException ex) {
                warningLabel.setText("Invalid number format."); return;
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                warningLabel.setText("Amount must be greater than zero."); return;
            }
            if (product.isPieceUnit() && amount.stripTrailingZeros().scale() > 0) {
                warningLabel.setText("Piece unit cannot have decimal amounts."); return;
            }

            try {
                // null expiry — restock does not touch the expiration date
                inventoryService.restockProduct(product, amount, null);
                dialog.dispose();
                loadProducts();
                renderCurrentSearch();
            } catch (IllegalArgumentException ex) {
                warningLabel.setText(ex.getMessage());
            } catch (SQLException ex) {
                showError("Failed to restock: " + ex.getMessage());
            }
        });

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(currentLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(amountLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(amountField);
        content.add(Box.createVerticalStrut(4));
        content.add(warningLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(confirmBtn);

        dialog.add(content);
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────
    // ADD PRODUCT DIALOG
    // ─────────────────────────────────────────

    private void showAddProductDialog() {
        // load combo box options from DB before opening dialog
        String[] intendedForOptions;
        String[] categoryOptions;
        try {
            intendedForOptions = optionService.getIntendedForOptions().toArray(new String[0]);
            categoryOptions    = optionService.getCategoryOptions().toArray(new String[0]);
        } catch (SQLException e) {
            showError("Failed to load product options: " + e.getMessage());
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Product", true);
        dialog.setSize(400, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(20, 20, 20, 20));

        JLabel titleLabel = UIUtils.createLabel("Add New Product", ThemeManager.FONT_BOLD, ThemeManager.getText());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = UIUtils.createTextField("Product name");
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JComboBox<String> unitBox = new JComboBox<>(new String[]{ "piece", "kg", "L", "g", "mg", "mL" });
        unitBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        unitBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        unitBox.setFont(ThemeManager.FONT_REGULAR);

        JTextField priceField = UIUtils.createTextField("Price per unit");
        priceField.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JTextField stockField = UIUtils.createTextField("Initial stock");
        stockField.setAlignmentX(Component.LEFT_ALIGNMENT);
        stockField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // populated from DB via ProductOptionService
        JComboBox<String> intendedForBox = new JComboBox<>(intendedForOptions);
        intendedForBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        intendedForBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        intendedForBox.setFont(ThemeManager.FONT_REGULAR);

        JComboBox<String> categoryBox = new JComboBox<>(categoryOptions);
        categoryBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        categoryBox.setFont(ThemeManager.FONT_REGULAR);

        // checkbox toggles the expiry date field
        JCheckBox hasExpiry = new JCheckBox("Set expiration date");
        hasExpiry.setBackground(ThemeManager.getSurface());
        hasExpiry.setForeground(ThemeManager.getText());
        hasExpiry.setFont(ThemeManager.FONT_REGULAR);
        hasExpiry.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField expiryField = UIUtils.createTextField("yyyy-MM-dd");
        expiryField.setAlignmentX(Component.LEFT_ALIGNMENT);
        expiryField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        expiryField.setEnabled(false);

        hasExpiry.addActionListener(e -> expiryField.setEnabled(hasExpiry.isSelected()));

        JLabel warningLabel = UIUtils.createWarningLabel();
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = UIUtils.createAccentButton("Add Product");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveBtn.addActionListener(e -> {
            String name        = nameField.getText().trim();
            String unit        = (String) unitBox.getSelectedItem();
            String priceRaw    = priceField.getText().trim();
            String stockRaw    = stockField.getText().trim();
            String intendedFor = (String) intendedForBox.getSelectedItem();
            String category    = (String) categoryBox.getSelectedItem();

            if (name.isEmpty()) { warningLabel.setText("Product name is required."); return; }

            BigDecimal price;
            try {
                price = new BigDecimal(priceRaw);
                if (price.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                warningLabel.setText("Price must be a valid number greater than zero."); return;
            }

            BigDecimal stock;
            try {
                stock = new BigDecimal(stockRaw);
                if (stock.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                // piece is the only whole-unit type
                if (unit.equals("piece") && stock.stripTrailingZeros().scale() > 0) {
                    warningLabel.setText("Stock for piece unit cannot have decimals."); return;
                }
            } catch (NumberFormatException ex) {
                warningLabel.setText("Stock must be a valid non-negative number."); return;
            }

            // parse expiry only if checkbox is checked
            LocalDate expiryDate = null;
            if (hasExpiry.isSelected()) {
                try {
                    expiryDate = LocalDate.parse(expiryField.getText().trim(), DATE_FORMATTER);
                } catch (DateTimeParseException ex) {
                    warningLabel.setText("Invalid date. Use yyyy-MM-dd format."); return;
                }
            }

            Product product = new Product(0, name, unit, price, stock, intendedFor, category, 0, "available", expiryDate);
            try {
                inventoryService.addProduct(product);
                dialog.dispose();
                loadProducts();
                renderCurrentSearch();
            } catch (SQLException ex) {
                showError("Failed to add product: " + ex.getMessage());
            }
        });

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(16));
        content.add(UIUtils.createLabel("Name", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(nameField);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Unit", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(unitBox);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Price per Unit", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(priceField);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Initial Stock", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(stockField);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Intended For", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(intendedForBox);
        content.add(Box.createVerticalStrut(10));
        content.add(UIUtils.createLabel("Category", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        content.add(Box.createVerticalStrut(4));
        content.add(categoryBox);
        content.add(Box.createVerticalStrut(10));
        content.add(hasExpiry);
        content.add(Box.createVerticalStrut(6));
        content.add(expiryField);
        content.add(Box.createVerticalStrut(4));
        content.add(warningLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(saveBtn);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.getSurface());
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────
    // TOGGLE AVAILABILITY
    // ─────────────────────────────────────────

    private void onToggleAvailability(Product product) {
        String action = product.getStatus().equalsIgnoreCase("available")
            ? "mark as unavailable" : "mark as available";
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to " + action + " " + product.getName() + "?",
            "Confirm",
            JOptionPane.YES_NO_OPTION
        );
        if (choice == JOptionPane.YES_OPTION) {
            try {
                inventoryService.toggleAvailability(product);
                loadProducts();
                renderCurrentSearch();
            } catch (SQLException e) {
                showError("Failed to update status: " + e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────

    // called from MainPanel when navigating to this panel
    public void refresh() {
        loadProducts();
        renderCurrentSearch();
    }

    // ─────────────────────────────────────────
    // THEME
    // ─────────────────────────────────────────

    public void applyTheme() {
        setBackground(ThemeManager.getBg());

        headerPanel.setBackground(ThemeManager.getBg());
        topRow.setBackground(ThemeManager.getBg());
        rightPanel.setBackground(ThemeManager.getBg());
        filterRow.setBackground(ThemeManager.getBg());
        titleLabel.setForeground(ThemeManager.getText());

        searchField.setBackground(ThemeManager.getSurface());
        searchField.setForeground(ThemeManager.getText());
        searchField.setCaretColor(ThemeManager.getText());
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        gridPanel.setBackground(ThemeManager.getBg());
        gridWrapper.setBackground(ThemeManager.getBg());
        gridScroll.getViewport().setBackground(ThemeManager.getBg());

        updateFilterButtons();
        renderCurrentSearch();

        repaint();
        revalidate();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}