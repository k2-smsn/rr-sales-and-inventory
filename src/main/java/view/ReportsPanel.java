/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author k2
 */
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import entity.SalesSummaryData;
import entity.SegmentData;
import entity.TopProductData;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import service.ReportsService;
import utility.ThemeManager;
import utility.UIUtils;

public class ReportsPanel extends JPanel {

    private final ReportsService reportsService = ReportsService.getInstance();

    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JPanel reportPanel;
    private JButton exportBtn;
    private String currentReportType = null;

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.getBg());
        setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setBackground(ThemeManager.getBg());
        header.setBorder(UIUtils.paddingBorder(0, 0, 16, 0));

        JLabel title = UIUtils.createLabel("Reports", ThemeManager.FONT_HEADING, ThemeManager.getText());

        // date controls
        JPanel dateControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dateControls.setBackground(ThemeManager.getBg());

        SpinnerDateModel fromModel = new SpinnerDateModel();
        SpinnerDateModel toModel = new SpinnerDateModel();

        fromSpinner = new JSpinner(fromModel);
        toSpinner = new JSpinner(toModel);

        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(fromSpinner, "MM/dd/yyyy");
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(toSpinner, "MM/dd/yyyy");

        fromSpinner.setEditor(fromEditor);
        toSpinner.setEditor(toEditor);
        fromSpinner.setPreferredSize(new Dimension(130, 34));
        toSpinner.setPreferredSize(new Dimension(130, 34));

        JButton todayBtn = UIUtils.createNeutralButton("Today");
        JButton weekBtn  = UIUtils.createNeutralButton("This Week");
        JButton monthBtn = UIUtils.createNeutralButton("This Month");

        todayBtn.addActionListener(e -> setToday());
        weekBtn.addActionListener(e -> setThisWeek());
        monthBtn.addActionListener(e -> setThisMonth());

        dateControls.add(UIUtils.createLabel("From:", ThemeManager.FONT_REGULAR, ThemeManager.getText()));
        dateControls.add(fromSpinner);
        dateControls.add(UIUtils.createLabel("To:", ThemeManager.FONT_REGULAR, ThemeManager.getText()));
        dateControls.add(toSpinner);
        dateControls.add(todayBtn);
        dateControls.add(weekBtn);
        dateControls.add(monthBtn);

        // report type buttons
        JPanel reportBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        reportBtns.setBackground(ThemeManager.getBg());

        JButton summaryBtn   = UIUtils.createAccentButton("Sales Summary");
        JButton topBtn       = UIUtils.createAccentButton("Top Selling Products");
        JButton segmentBtn   = UIUtils.createAccentButton("Category & Segment");

        summaryBtn.addActionListener(e -> generateReport("summary"));
        topBtn.addActionListener(e -> generateReport("top_products"));
        segmentBtn.addActionListener(e -> generateReport("segment"));

        reportBtns.add(summaryBtn);
        reportBtns.add(topBtn);
        reportBtns.add(segmentBtn);

        header.add(title, BorderLayout.NORTH);
        header.add(dateControls, BorderLayout.CENTER);
        header.add(reportBtns, BorderLayout.SOUTH);

        return header;
    }

    // ─────────────────────────────────────────
    // BODY
    // ─────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(ThemeManager.getBg());

        reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBackground(ThemeManager.getSurface());
        reportPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorder(), 1));

        JLabel placeholder = UIUtils.createLabel(
            "Select a report type to generate.",
            ThemeManager.FONT_REGULAR,
            ThemeManager.getSubtext()
        );
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        reportPanel.add(placeholder, BorderLayout.CENTER);

        body.add(reportPanel, BorderLayout.CENTER);
        return body;
    }

    // ─────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(ThemeManager.getBg());
        footer.setBorder(UIUtils.paddingBorder(12, 0, 0, 0));

        exportBtn = UIUtils.createAccentButton("Export to PDF");
        exportBtn.setVisible(false);
        exportBtn.addActionListener(e -> exportToPdf());

        footer.add(exportBtn);
        return footer;
    }

    // ─────────────────────────────────────────
    // DATE SHORTCUTS
    // ─────────────────────────────────────────
    private void setToday() {
        Date today = toDate(LocalDate.now());
        fromSpinner.setValue(today);
        toSpinner.setValue(today);
    }

    private void setThisWeek() {
        LocalDate now = LocalDate.now();
        fromSpinner.setValue(toDate(now.with(DayOfWeek.MONDAY)));
        toSpinner.setValue(toDate(now));
    }

    private void setThisMonth() {
        LocalDate now = LocalDate.now();
        fromSpinner.setValue(toDate(now.withDayOfMonth(1)));
        toSpinner.setValue(toDate(now));
    }

    // ─────────────────────────────────────────
    // GENERATE REPORT
    // ─────────────────────────────────────────
    private void generateReport(String reportType) {
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
            currentReportType = reportType;
            reportPanel.removeAll();

            switch (reportType) {
                case "summary"      -> reportPanel.add(buildSummaryReport(from, to), BorderLayout.CENTER);
                case "top_products" -> reportPanel.add(buildTopProductsReport(from, to), BorderLayout.CENTER);
                case "segment"      -> reportPanel.add(buildSegmentReport(from, to), BorderLayout.CENTER);
            }

            exportBtn.setVisible(true);
            reportPanel.revalidate();
            reportPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to generate report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────
    // REPORT VIEWS
    // ─────────────────────────────────────────
    private JPanel buildSummaryReport(LocalDate from, LocalDate to) throws Exception {
        SalesSummaryData data = reportsService.getSalesSummary(from, to);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.getSurface());
        panel.setBorder(UIUtils.paddingBorder(24, 24, 24, 24));

        JLabel title = UIUtils.createLabel("Sales Summary", ThemeManager.FONT_HEADING, ThemeManager.getText());
        JLabel range = UIUtils.createLabel(from + " to " + to, ThemeManager.FONT_SMALL, ThemeManager.getSubtext());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        range.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(range);
        panel.add(Box.createVerticalStrut(24));
        panel.add(buildSummaryRow("Total Transactions", String.valueOf(data.getTotalTransactions())));
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildSummaryRow("Total Revenue", String.format("₱%,.2f", data.getTotalRevenue())));
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildSummaryRow("Average Transaction Value", String.format("₱%,.2f", data.getAverageTransactionValue())));

        return panel;
    }

    private JPanel buildSummaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ThemeManager.getSurface());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorder(), 1, true),
            UIUtils.paddingBorder(10, 16, 10, 16)
        ));

        JLabel labelComp = UIUtils.createLabel(label, ThemeManager.FONT_REGULAR, ThemeManager.getSubtext());
        JLabel valueComp = UIUtils.createLabel(value, ThemeManager.FONT_BOLD, ThemeManager.getText());
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.EAST);
        return row;
    }

    private JScrollPane buildTopProductsReport(LocalDate from, LocalDate to) throws Exception {
        List<TopProductData> data = reportsService.getTopProducts(from, to);

        String[] columns = { "Rank", "Product", "Category", "Qty Sold", "Revenue" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        int rank = 1;
        for (TopProductData row : data) {
            model.addRow(new Object[]{
                rank++,
                row.getProductName(),
                row.getCategory(),
                row.getTotalQuantitySold().toPlainString(),
                String.format("₱%,.2f", row.getTotalRevenue())
            });
        }

        JTable table = new JTable(model);
        UIUtils.applyTheme(table);
        table.setRowHeight(40);

        JScrollPane scroll = UIUtils.createScrollPane(table);
        scroll.setBorder(null);
        return scroll;
    }

    private JScrollPane buildSegmentReport(LocalDate from, LocalDate to) throws Exception {
        List<SegmentData> data = reportsService.getSegmentPerformance(from, to);

        String[] columns = { "Category", "Intended For", "Qty Sold", "Revenue" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        for (SegmentData row : data) {
            model.addRow(new Object[]{
                row.getCategory(),
                row.getIntendedFor(),
                row.getTotalQuantitySold().toPlainString(),
                String.format("₱%,.2f", row.getTotalRevenue())
            });
        }

        JTable table = new JTable(model);
        UIUtils.applyTheme(table);
        table.setRowHeight(40);

        JScrollPane scroll = UIUtils.createScrollPane(table);
        scroll.setBorder(null);
        return scroll;
    }

    // ─────────────────────────────────────────
    // PDF EXPORT
    // ─────────────────────────────────────────
    private void exportToPdf() {
        LocalDate from = toLocalDate((Date) fromSpinner.getValue());
        LocalDate to = toLocalDate((Date) toSpinner.getValue());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(getReportFileName(from, to)));
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fileChooser.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) {
            file = new java.io.File(file.getAbsolutePath() + ".pdf");
        }

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // title
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font subFont   = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.NORMAL);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font cellFont  = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

            document.add(new Paragraph(getReportTitle(), titleFont));
            document.add(new Paragraph("Date Range: " + from + " to " + to, subFont));
            document.add(new Paragraph("Generated: " + LocalDate.now(), subFont));
            document.add(new Paragraph(" "));

            switch (currentReportType) {
                case "summary" -> exportSummaryPdf(document, from, to, headerFont, cellFont);
                case "top_products" -> exportTopProductsPdf(document, from, to, headerFont, cellFont);
                case "segment" -> exportSegmentPdf(document, from, to, headerFont, cellFont);
            }

            document.close();

            JOptionPane.showMessageDialog(this,
                "Report saved to:\n" + file.getAbsolutePath(),
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to export PDF: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportSummaryPdf(Document doc, LocalDate from, LocalDate to,
            com.lowagie.text.Font headerFont, com.lowagie.text.Font cellFont) throws Exception {
        SalesSummaryData data = reportsService.getSalesSummary(from, to);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{ 3f, 2f });

        addPdfHeader(table, "Metric", headerFont);
        addPdfHeader(table, "Value", headerFont);

        addPdfCell(table, "Total Transactions", cellFont);
        addPdfCell(table, String.valueOf(data.getTotalTransactions()), cellFont);

        addPdfCell(table, "Total Revenue", cellFont);
        addPdfCell(table, String.format("₱%,.2f", data.getTotalRevenue()), cellFont);

        addPdfCell(table, "Average Transaction Value", cellFont);
        addPdfCell(table, String.format("₱%,.2f", data.getAverageTransactionValue()), cellFont);

        doc.add(table);
    }

    private void exportTopProductsPdf(Document doc, LocalDate from, LocalDate to,
            com.lowagie.text.Font headerFont, com.lowagie.text.Font cellFont) throws Exception {
        List<TopProductData> data = reportsService.getTopProducts(from, to);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{ 1f, 3f, 2f, 2f, 2f });

        addPdfHeader(table, "Rank", headerFont);
        addPdfHeader(table, "Product", headerFont);
        addPdfHeader(table, "Category", headerFont);
        addPdfHeader(table, "Qty Sold", headerFont);
        addPdfHeader(table, "Revenue", headerFont);

        int rank = 1;
        for (TopProductData row : data) {
            addPdfCell(table, String.valueOf(rank++), cellFont);
            addPdfCell(table, row.getProductName(), cellFont);
            addPdfCell(table, row.getCategory(), cellFont);
            addPdfCell(table, row.getTotalQuantitySold().toPlainString(), cellFont);
            addPdfCell(table, String.format("₱%,.2f", row.getTotalRevenue()), cellFont);
        }

        doc.add(table);
    }

    private void exportSegmentPdf(Document doc, LocalDate from, LocalDate to,
            com.lowagie.text.Font headerFont, com.lowagie.text.Font cellFont) throws Exception {
        List<SegmentData> data = reportsService.getSegmentPerformance(from, to);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{ 2f, 2f, 2f, 2f });

        addPdfHeader(table, "Category", headerFont);
        addPdfHeader(table, "Intended For", headerFont);
        addPdfHeader(table, "Qty Sold", headerFont);
        addPdfHeader(table, "Revenue", headerFont);

        for (SegmentData row : data) {
            addPdfCell(table, row.getCategory(), cellFont);
            addPdfCell(table, row.getIntendedFor(), cellFont);
            addPdfCell(table, row.getTotalQuantitySold().toPlainString(), cellFont);
            addPdfCell(table, String.format("₱%,.2f", row.getTotalRevenue()), cellFont);
        }

        doc.add(table);
    }

    // ─────────────────────────────────────────
    // PDF HELPERS
    // ─────────────────────────────────────────
    private void addPdfHeader(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private String getReportTitle() {
        return switch (currentReportType) {
            case "summary"      -> "Sales Summary Report";
            case "top_products" -> "Top Selling Products Report";
            case "segment"      -> "Category & Segment Performance Report";
            default             -> "Report";
        };
    }

    private String getReportFileName(LocalDate from, LocalDate to) {
        return getReportTitle().replace(" ", "_") + "_" + from + "_to_" + to + ".pdf";
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