/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author k2
 */
import entity.CartItem;
import entity.Product;
import entity.Receipt;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import service.InventoryService;
import service.SalesService;
import utility.ThemeManager;
import utility.UIUtils;

public class NewTransactionPanel extends JPanel {

    private final InventoryService inventoryService = InventoryService.getInstance();
    private final SalesService salesService = SalesService.getInstance();
    private final MainPanel mainPanel;

    private final List<CartItem> cart = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();

    // — Search section
    private JTextField searchField;
    private JPanel searchResultsPanel;

    // — Cart section
    private JPanel cartPanel;
    private JLabel totalLabel;

    public NewTransactionPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        loadProducts();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        mainPanel.hideSidebar();
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeManager.getBg());
        header.setBorder(UIUtils.paddingBorder(0, 0, 16, 0));

        JLabel title = UIUtils.createLabel("New Transaction", ThemeManager.FONT_HEADING, ThemeManager.getText());
        header.add(title, BorderLayout.WEST);

        return header;
    }

    // ─────────────────────────────────────────
    // BODY — search (left) + cart (right)
    // ─────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setBackground(ThemeManager.getBg());

        body.add(buildSearchSection());
        body.add(buildCartSection());

        return body;
    }

    // ─────────────────────────────────────────
    // SEARCH SECTION
    // ─────────────────────────────────────────
    private JPanel buildSearchSection() {
        searchField = UIUtils.createTextField("Search products...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e)  { onSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e)  { onSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onSearch(); }
        });

        searchResultsPanel = new JPanel();
        searchResultsPanel.setLayout(new BoxLayout(searchResultsPanel, BoxLayout.Y_AXIS));
        searchResultsPanel.setBackground(ThemeManager.getSurface());

        JScrollPane scroll = UIUtils.createScrollPane(searchResultsPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JLabel searchTitle = UIUtils.createLabel("Products", ThemeManager.FONT_SUBHEADING, ThemeManager.getText());

        // title + search field stacked above the scrollable results
        JPanel topPart = new JPanel(new BorderLayout(0, 8));
        topPart.setBackground(ThemeManager.getBg());
        topPart.add(searchTitle, BorderLayout.NORTH);
        topPart.add(searchField, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(ThemeManager.getBg());
        wrapper.add(topPart, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    // ─────────────────────────────────────────
    // CART SECTION
    // ─────────────────────────────────────────
    private JPanel buildCartSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(ThemeManager.getBg());

        JLabel cartTitle = UIUtils.createLabel("Cart", ThemeManager.FONT_SUBHEADING, ThemeManager.getText());

        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(ThemeManager.getSurface());

        JScrollPane scroll = UIUtils.createScrollPane(cartPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        totalLabel = UIUtils.createLabel("Total: ₱0.00", ThemeManager.FONT_BOLD, ThemeManager.getText());
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        wrapper.add(cartTitle, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        wrapper.add(totalLabel, BorderLayout.SOUTH);

        return wrapper;
    }

    // ─────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(ThemeManager.getBg());
        footer.setBorder(UIUtils.paddingBorder(16, 0, 0, 0));

        JButton cancelBtn = UIUtils.createNeutralButton("Cancel");
        JButton confirmBtn = UIUtils.createAccentButton("Confirm Transaction");

        cancelBtn.addActionListener(e -> onCancel());
        confirmBtn.addActionListener(e -> onConfirm());

        footer.add(cancelBtn);
        footer.add(confirmBtn);

        return footer;
    }

    // ─────────────────────────────────────────
    // SEARCH LOGIC
    // ─────────────────────────────────────────
    private void onSearch() {
        String query = searchField.getText().trim().toLowerCase();
        List<Product> filtered = allProducts.stream()
            .filter(p -> p.getName().toLowerCase().startsWith(query))
            .collect(java.util.stream.Collectors.toList());
        renderSearchResults(filtered);
    }

    private void renderSearchResults(List<Product> products) {
        searchResultsPanel.removeAll();

        if (products.isEmpty()) {
            JLabel empty = UIUtils.createLabel("No products found.", ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
            empty.setBorder(UIUtils.paddingBorder(12, 12, 12, 12));
            searchResultsPanel.add(empty);
        } else {
            for (Product product : products) {
                searchResultsPanel.add(buildSearchResultRow(product));
                searchResultsPanel.add(UIUtils.createSeparator());
            }
        }

        searchResultsPanel.revalidate();
        searchResultsPanel.repaint();
    }
    
    private JPanel buildSearchResultRow(Product product) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(ThemeManager.getSurface());
        row.setBorder(UIUtils.paddingBorder(8, 12, 8, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // left — product info
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setBackground(ThemeManager.getSurface());

        JLabel name = UIUtils.createLabel(product.getName(), ThemeManager.FONT_BOLD, ThemeManager.getText());
        JLabel price = UIUtils.createLabel("₱" + product.getPricePerUnit() + " / " + product.getUnit(), ThemeManager.FONT_SMALL, ThemeManager.getSubtext());

        info.add(name);
        info.add(price);

        // right — add button or out of stock label
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setBackground(ThemeManager.getSurface());

        if (product.isOutOfStock()) {
            JLabel outOfStock = UIUtils.createLabel("Out of stock", ThemeManager.FONT_SMALL, ThemeManager.WARNING);
            right.add(outOfStock);
        } else {
            JButton addBtn = UIUtils.createAccentButton("Add");
            addBtn.addActionListener(e -> onAddToCart(product));
            right.add(addBtn);
        }

        row.add(info, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }
    
    private void loadProducts() {
        try {
            allProducts = inventoryService.getAllAvailable();
        } catch (SQLException e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }
    
    // ─────────────────────────────────────────
    // CART LOGIC
    // ─────────────────────────────────────────
    private void onAddToCart(Product product) {
        // if already in cart, increment by 1 with validation
        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                BigDecimal newQty = item.getQuantity().add(BigDecimal.ONE);
                String error = validateQuantity(newQty, product);
                if (error != null) {
                    showError(error);
                    return;
                }
                item.setQuantity(newQty);
                updateCart();
                return;
            }
        }
        // not in cart yet, add with qty 1
        cart.add(new CartItem(product, BigDecimal.ONE));
        updateCart();
    }

    private void onIncrement(CartItem item) {
        BigDecimal newQty = item.getQuantity().add(BigDecimal.ONE);
        String error = validateQuantity(newQty, item.getProduct());
        if (error != null) {
            showError(error);
            return;
        }
        item.setQuantity(newQty);
        updateCart();
    }

    private void onDecrement(CartItem item) {
        BigDecimal newQty = item.getQuantity().subtract(BigDecimal.ONE);
        String error = validateQuantity(newQty, item.getProduct());
        if (error != null) {
            showError(error);
            return;
        }
        item.setQuantity(newQty);
        updateCart();
    }

    private void onRemove(CartItem item) {
        cart.remove(item);
        updateCart();
    }

    private void onManualInput(CartItem item) {
        // build dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Quantity", true);
        dialog.setLayout(new BorderLayout(0, 0));
        dialog.setSize(320, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.getSurface());
        content.setBorder(UIUtils.paddingBorder(20, 20, 20, 20));

        JLabel title = UIUtils.createLabel(
            "Set quantity for " + item.getProduct().getName(),
            ThemeManager.FONT_BOLD,
            ThemeManager.getText()
        );
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = UIUtils.createLabel(
            "Stock available: " + item.getProduct().getStockQuantity().setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + item.getProduct().getUnit(), // 2 dp
            ThemeManager.FONT_SMALL,
            ThemeManager.getSubtext()
        );
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField inputField = UIUtils.createTextField("Enter quantity...");
        inputField.setText(item.getQuantity().setScale(2, RoundingMode.HALF_UP).toPlainString()); // 2 dp
        inputField.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel warningLabel = UIUtils.createLabel(" ", ThemeManager.FONT_SMALL, ThemeManager.DANGER);
        warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton confirmBtn = UIUtils.createAccentButton("Set Quantity");
        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmBtn.addActionListener(e -> {
            String raw = inputField.getText().trim();
            BigDecimal parsed = parseQuantityInput(raw, item.getProduct(), warningLabel);
            if (parsed == null) return;
            item.setQuantity(parsed);
            updateCart();
            dialog.dispose();
        });

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(hint);
        content.add(Box.createVerticalStrut(12));
        content.add(inputField);
        content.add(Box.createVerticalStrut(4));
        content.add(warningLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(confirmBtn);

        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────

    // returns error message string, or null if valid
    private String validateQuantity(BigDecimal qty, Product product) {
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            return "Quantity must be greater than zero.";
        }
        if (product.isPieceUnit()) {
            if (qty.stripTrailingZeros().scale() > 0) {
                return product.getName() + " is sold by piece and cannot have decimal quantities.";
            }
        }
        if (qty.compareTo(product.getStockQuantity()) > 0) {
            return "Insufficient stock. Only " + product.getStockQuantity().setScale(2, RoundingMode.HALF_UP).toPlainString() + " " + product.getUnit() + " available."; // 2 dp
        }
        return null;
    }

    // parses raw string input, sets warning label, returns null if invalid
    private BigDecimal parseQuantityInput(String raw, Product product, JLabel warningLabel) {
        if (raw.isEmpty()) {
            warningLabel.setText("Please enter a quantity.");
            return null;
        }
        BigDecimal parsed;
        try {
            parsed = new BigDecimal(raw);
        } catch (NumberFormatException e) {
            warningLabel.setText("Invalid number format.");
            return null;
        }
        String error = validateQuantity(parsed, product);
        if (error != null) {
            warningLabel.setText(error);
            return null;
        }
        warningLabel.setText(" ");
        return parsed;
    }

    // ─────────────────────────────────────────
    // RENDER CART
    // ─────────────────────────────────────────
    private void updateCart() {
        cartPanel.removeAll();

        if (cart.isEmpty()) {
            JLabel empty = UIUtils.createLabel("No items in cart.", ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
            empty.setBorder(UIUtils.paddingBorder(12, 12, 12, 12));
            cartPanel.add(empty);
        } else {
            for (CartItem item : cart) {
                cartPanel.add(buildCartRow(item));
                cartPanel.add(UIUtils.createSeparator());
            }
        }

        // update total
        BigDecimal total = cart.stream()
            .map(CartItem::getSubTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Total: ₱" + String.format("%,.2f", total));

        // re-filter using current search query and rerender
        String query = searchField.getText().trim().toLowerCase();
        List<Product> filtered = allProducts.stream()
            .filter(p -> p.getName().toLowerCase().startsWith(query))
            .collect(java.util.stream.Collectors.toList());
        renderSearchResults(filtered);

        cartPanel.revalidate();
        cartPanel.repaint();
    }

    private JPanel buildCartRow(CartItem item) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(ThemeManager.getSurface());
        row.setBorder(UIUtils.paddingBorder(8, 12, 8, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        // left — name and unit
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setBackground(ThemeManager.getSurface());
        JLabel name = UIUtils.createLabel(item.getProduct().getName(), ThemeManager.FONT_BOLD, ThemeManager.getText());
        JLabel unit = UIUtils.createLabel(item.getProduct().getUnit(), ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
        info.add(name);
        info.add(unit);

        // center — quantity controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        controls.setBackground(ThemeManager.getSurface());

        JButton minusBtn = UIUtils.createNeutralButton("−");
        JButton plusBtn  = UIUtils.createAccentButton("+");
        JButton setBtn   = UIUtils.createNeutralButton("✎");
        JLabel  qtyLabel = UIUtils.createLabel(item.getQuantity().setScale(2, RoundingMode.HALF_UP).toPlainString(), ThemeManager.FONT_BOLD, ThemeManager.getText()); // 2 dp
        qtyLabel.setPreferredSize(new Dimension(40, 20));
        qtyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        minusBtn.setPreferredSize(new Dimension(32, 28));
        plusBtn.setPreferredSize(new Dimension(32, 28));
        setBtn.setPreferredSize(new Dimension(32, 28));

        minusBtn.addActionListener(e -> onDecrement(item));
        plusBtn.addActionListener(e -> onIncrement(item));
        setBtn.addActionListener(e -> onManualInput(item));

        controls.add(minusBtn);
        controls.add(qtyLabel);
        controls.add(plusBtn);
        controls.add(setBtn);

        // right — subtotal and remove
        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setBackground(ThemeManager.getSurface());

        JLabel subTotal = UIUtils.createLabel(
            "₱" + String.format("%,.2f", item.getSubTotal()),
            ThemeManager.FONT_BOLD,
            ThemeManager.getText()
        );
        subTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton removeBtn = UIUtils.createDangerButton("Remove");
        removeBtn.addActionListener(e -> onRemove(item));

        right.add(subTotal);
        right.add(removeBtn);

        row.add(info, BorderLayout.WEST);
        row.add(controls, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    // ─────────────────────────────────────────
    // CANCEL & CONFIRM
    // ─────────────────────────────────────────
    private void onCancel() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel? Your cart will be cleared.",
            "Cancel Transaction",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            cart.clear();
            updateCart();
            searchField.setText("");
            navigateToDashboard();
        }
    }

    private void onConfirm() {
        if (cart.isEmpty()) {
            showError("Cart is empty. Please add products before confirming.");
            return;
        }

        // build order summary for confirmation
        StringBuilder summary = new StringBuilder("Order Summary:\n\n");
        for (CartItem item : cart) {
            summary.append(String.format("%-20s x%-6s ₱%,.2f%n",
                item.getProduct().getName(),
                item.getQuantity().setScale(2, RoundingMode.HALF_UP).toPlainString(), // 2 dp
                item.getSubTotal()
            ));
        }
        BigDecimal total = cart.stream()
            .map(CartItem::getSubTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.append(String.format("%nTotal: ₱%,.2f", total));

        int choice = JOptionPane.showConfirmDialog(
            this, summary.toString(), "Confirm Transaction",
            JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                Receipt receipt = salesService.processTransaction(cart);
                cart.clear();
                updateCart();
                searchField.setText("");
                showReceiptDialog(receipt);
                navigateToDashboard();
            } catch (SQLException e) {
                showError("Transaction failed: " + e.getMessage());
            }
        }
    }

    private void showReceiptDialog(Receipt receipt) {
        String username  = utility.UserSession.getInstance().getUsername();
        int WIDTH        = 40; // total receipt width in chars
        String SEP       = "-".repeat(WIDTH);

        StringBuilder sb = new StringBuilder();

        sb.append(center("R&R Animal Supply Shop", WIDTH)).append("\n");
        sb.append(center("Your Trusted Pet Supply Store", WIDTH)).append("\n");
        sb.append(SEP).append("\n");
        sb.append(String.format("Receipt #: %d%n",   receipt.getTransaction().getId()));
        sb.append(String.format("Date     : %s%n",   receipt.getTransaction().getCreatedAt()));
        sb.append(String.format("Cashier  : %s%n",   username));
        sb.append(SEP).append("\n");
        sb.append(String.format("%-22s %5s %10s%n",  "Item", "Qty", "Amount"));
        sb.append(SEP).append("\n");

        for (entity.ItemSold item : receipt.getItemsSold()) {
            String name = allProducts.stream()
                .filter(p -> p.getId() == item.getProductId())
                .map(entity.Product::getName)
                .findFirst().orElse("Product #" + item.getProductId());
            // format amount without peso sign to keep alignment, add it after
            String amount = String.format("P%,.2f", item.getSubTotal());
            sb.append(String.format("%-22s %5s %10s%n",
                truncate(name, 22),
                item.getQuantity().setScale(2, RoundingMode.HALF_UP).toPlainString(), // 2 dp
                amount
            ));
        }

        sb.append(SEP).append("\n");
        sb.append(String.format("%-22s %16s%n", "TOTAL",
            String.format("P%,.2f", receipt.getTotal())));
        sb.append(SEP).append("\n");
        sb.append("\n");
        sb.append(center("Thank you for your purchase!", WIDTH)).append("\n");
        sb.append(center("Please come again.  (^._.^)", WIDTH)).append("\n");

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Receipt", true);
        dialog.setSize(380, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        textArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton closeBtn = UIUtils.createAccentButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(ThemeManager.getBg());
        bottom.add(closeBtn);

        dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // centers text within a fixed width by padding with spaces
    private String center(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    private String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    // ─────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void navigateToDashboard() {
        cart.clear();
        updateCart();
        searchField.setText("");
        mainPanel.showSidebar();
        mainPanel.showPanel("dashboard");
    }
    
}