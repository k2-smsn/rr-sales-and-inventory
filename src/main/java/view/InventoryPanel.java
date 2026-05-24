package view;

import entity.Product;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
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
    private JTable table;
    private DefaultTableModel tableModel;

    private String currentFilter = "all";

    private JButton allFilterBtn;
    private JButton unavailableFilterBtn;
    private JButton lowStockFilterBtn;
    private JButton outOfStockFilterBtn;
    private JPanel headerPanel;
    private JPanel topRow;
    private JPanel filterRow;
    private JPanel rightPanel;
    private JLabel titleLabel;

    private static final String[] COLUMNS = {
        "ID", "Name", "Unit", "Price/Unit", "Stock", "Intended For", "Category", "Sales", "Status", "Actions"
    };

    private static final int COL_ID           = 0;
    private static final int COL_NAME         = 1;
    private static final int COL_UNIT         = 2;
    private static final int COL_PRICE        = 3;
    private static final int COL_STOCK        = 4;
    private static final int COL_INTENDED_FOR = 5;
    private static final int COL_CATEGORY     = 6;
    private static final int COL_SALES        = 7;
    private static final int COL_STATUS       = 8;
    private static final int COL_ACTIONS      = 9;

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

        searchField = UIUtils.createTextField("Search products...");
        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { renderCurrentSearch(); }
            public void removeUpdate(DocumentEvent e)  { renderCurrentSearch(); }
            public void changedUpdate(DocumentEvent e) { renderCurrentSearch(); }
        });

        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(ThemeManager.getBg());
        rightPanel.add(searchField);

        if (UserSession.getInstance().isAdmin()) {
            // manage options opens the options dialog; add product opens the add dialog
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

        allFilterBtn         = buildFilterButton("All", "all");
        unavailableFilterBtn = buildFilterButton("Unavailable", "unavailable");
        lowStockFilterBtn    = buildFilterButton("Low Stock", "low_stock");
        outOfStockFilterBtn  = buildFilterButton("Out of Stock", "out_of_stock");

        filterRow.add(allFilterBtn);
        filterRow.add(unavailableFilterBtn);
        filterRow.add(lowStockFilterBtn);
        filterRow.add(outOfStockFilterBtn);

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

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COL_ACTIONS;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // applyTheme sets the default renderer which handles stock alert row coloring
        UIUtils.applyTheme(table);

        // these column-specific renderers must come after applyTheme
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(UIUtils.createStatusRenderer());
        table.getColumnModel().getColumn(COL_ACTIONS).setCellRenderer(new UIUtils.ButtonPanelRenderer());
        table.getColumnModel().getColumn(COL_ACTIONS).setCellEditor(new UIUtils.ButtonPanelEditor());

        table.getColumnModel().getColumn(COL_ID).setPreferredWidth(40);
        table.getColumnModel().getColumn(COL_NAME).setPreferredWidth(160);
        table.getColumnModel().getColumn(COL_UNIT).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_PRICE).setPreferredWidth(90);
        table.getColumnModel().getColumn(COL_STOCK).setPreferredWidth(70);
        table.getColumnModel().getColumn(COL_INTENDED_FOR).setPreferredWidth(90);
        table.getColumnModel().getColumn(COL_CATEGORY).setPreferredWidth(90);
        table.getColumnModel().getColumn(COL_SALES).setPreferredWidth(60);
        table.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(90);
        table.getColumnModel().getColumn(COL_ACTIONS).setPreferredWidth(220);

        JScrollPane scroll = UIUtils.createScrollPane(table);
        body.add(scroll, BorderLayout.CENTER);

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
                case "unavailable"  -> p.getStatus().equalsIgnoreCase("unavailable");
                case "low_stock"    -> !p.isOutOfStock()
                                    && p.getStockQuantity().compareTo(BigDecimal.valueOf(7)) <= 0
                                    && !p.getStatus().equalsIgnoreCase("unavailable");
                case "out_of_stock" -> p.isOutOfStock()
                                    && !p.getStatus().equalsIgnoreCase("unavailable");
                default             -> true;
            })
            .collect(java.util.stream.Collectors.toList());

        renderProducts(filtered);
    }

    private void renderProducts(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product product : products) {
            BigDecimal stock = product.getStockQuantity();

            // append !! for out of stock, ! for low stock — used by row renderer for coloring
            String stockDisplay;
            if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                stockDisplay = stock.toPlainString() + "  !!";
            } else if (stock.compareTo(BigDecimal.valueOf(7)) <= 0) {
                stockDisplay = stock.toPlainString() + "  !";
            } else {
                stockDisplay = stock.toPlainString();
            }

            tableModel.addRow(new Object[]{
                product.getId(),
                product.getName(),
                product.getUnit(),
                String.format("₱%,.2f", product.getPricePerUnit()),
                stockDisplay,
                product.getIntendedFor(),
                product.getCategory(),
                product.getSales(),
                product.getStatus(),
                buildActionsPanel(product)
            });
        }
    }

    private JPanel buildActionsPanel(Product product) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        panel.setBackground(ThemeManager.getSurface());

        if (UserSession.getInstance().isAdmin()) {
            JButton adjustBtn = UIUtils.createAccentButton("Adjust Stock");
            JButton toggleBtn = product.getStatus().equalsIgnoreCase("available")
                ? UIUtils.createDangerButton("Mark Unavailable")
                : UIUtils.createButton("Mark Available", ThemeManager.SUCCESS, Color.WHITE);

            adjustBtn.addActionListener(e -> showAdjustStockDialog(product));
            toggleBtn.addActionListener(e -> onToggleAvailability(product));

            panel.add(adjustBtn);
            panel.add(toggleBtn);
        } else {
            panel.add(UIUtils.createLabel("View only", ThemeManager.FONT_SMALL, ThemeManager.getSubtext()));
        }

        return panel;
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

        // scrollable list of current options
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

        // reloads the list panel from DB — used after add or delete
        Runnable reloadList = () -> reloadOptionList(listPanel, type, dialog);

        addBtn.addActionListener(e -> {
            String val = inputField.getText().trim();
            if (val.isEmpty()) { warningLabel.setText("Cannot be empty."); return; }

            // confirm before adding
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
    // ADJUST STOCK DIALOG
    // ─────────────────────────────────────────

    private void showAdjustStockDialog(Product product) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Adjust Stock", true);
        dialog.setSize(340, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(20, 20, 20, 20));

        JLabel titleLabel = UIUtils.createLabel(
            "Adjust Stock — " + product.getName(), ThemeManager.FONT_BOLD, ThemeManager.getText()
        );
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel idLabel = UIUtils.createLabel(
            "Product ID: " + product.getId(), ThemeManager.FONT_SMALL, ThemeManager.getSubtext()
        );
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel currentStock = UIUtils.createLabel(
            "Current Stock: " + product.getStockQuantity().toPlainString() + " " + product.getUnit(),
            ThemeManager.FONT_SMALL, ThemeManager.getSubtext()
        );
        currentStock.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField amountField = UIUtils.createTextField("Enter amount...");
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel warningLabel = UIUtils.createLabel(" ", ThemeManager.FONT_SMALL, ThemeManager.DANGER);
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setBackground(ThemeManager.getSurface());
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addBtn      = UIUtils.createAccentButton("Add");
        JButton subtractBtn = UIUtils.createDangerButton("Subtract");

        addBtn.addActionListener(e -> {
            BigDecimal amount = parseAmount(amountField.getText().trim(), product, warningLabel);
            if (amount == null) return;
            try {
                inventoryService.adjustStock(product, amount, "add");
                refreshAfterAction(dialog);
            } catch (SQLException ex) {
                showError("Failed to adjust stock: " + ex.getMessage());
            }
        });

        subtractBtn.addActionListener(e -> {
            BigDecimal amount = parseAmount(amountField.getText().trim(), product, warningLabel);
            if (amount == null) return;
            try {
                inventoryService.adjustStock(product, amount, "subtract");
                refreshAfterAction(dialog);
            } catch (IllegalArgumentException ex) {
                warningLabel.setText(ex.getMessage());
            } catch (SQLException ex) {
                showError("Failed to adjust stock: " + ex.getMessage());
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(subtractBtn);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(idLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(currentStock);
        content.add(Box.createVerticalStrut(12));
        content.add(amountField);
        content.add(Box.createVerticalStrut(4));
        content.add(warningLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(btnPanel);

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
        dialog.setSize(400, 500);
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

        JLabel warningLabel = UIUtils.createLabel(" ", ThemeManager.FONT_SMALL, ThemeManager.DANGER);
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
                // piece is the only whole-unit type; kg, L, g, mg, mL all allow decimals
                if (unit.equals("piece") && stock.stripTrailingZeros().scale() > 0) {
                    warningLabel.setText("Stock for piece unit cannot have decimals."); return;
                }
            } catch (NumberFormatException ex) {
                warningLabel.setText("Stock must be a valid non-negative number."); return;
            }

            Product product = new Product(0, name, unit, price, stock, intendedFor, category, 0, "available");
            try {
                inventoryService.addProduct(product);
                refreshAfterAction(dialog);
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
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
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

    private BigDecimal parseAmount(String raw, Product product, JLabel warningLabel) {
        if (raw.isEmpty()) { warningLabel.setText("Please enter an amount."); return null; }
        BigDecimal amount;
        try {
            amount = new BigDecimal(raw);
        } catch (NumberFormatException e) {
            warningLabel.setText("Invalid number format."); return null;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            warningLabel.setText("Amount must be greater than zero."); return null;
        }
        if (product.isPieceUnit() && amount.stripTrailingZeros().scale() > 0) {
            warningLabel.setText("Piece unit cannot have decimal amounts."); return null;
        }
        warningLabel.setText(" ");
        return amount;
    }

    private void refreshAfterAction(JDialog dialog) {
        dialog.dispose();
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        loadProducts();
        renderCurrentSearch();
    }

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

        // re-apply theme and column-specific renderers after theme change
        UIUtils.applyTheme(table);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(UIUtils.createStatusRenderer());
        table.getColumnModel().getColumn(COL_ACTIONS).setCellRenderer(new UIUtils.ButtonPanelRenderer());
        table.getColumnModel().getColumn(COL_ACTIONS).setCellEditor(new UIUtils.ButtonPanelEditor());

        updateFilterButtons();
        renderCurrentSearch();

        repaint();
        revalidate();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}