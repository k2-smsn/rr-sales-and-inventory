/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author k2
 */
import dao.ReportsDAO;
import entity.SalesSummaryData;
import entity.SegmentData;
import entity.TopProductData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportsService {
    private static ReportsService instance;
    private final ReportsDAO reportsDAO = ReportsDAO.getInstance();

    private ReportsService() {}

    public static ReportsService getInstance() {
        if (instance == null) instance = new ReportsService();
        return instance;
    }

    public SalesSummaryData getSalesSummary(LocalDate from, LocalDate to) throws SQLException {
        return reportsDAO.getSalesSummary(from, to);
    }

    public List<TopProductData> getTopProducts(LocalDate from, LocalDate to) throws SQLException {
        return reportsDAO.getTopProducts(from, to);
    }

    public List<SegmentData> getSegmentPerformance(LocalDate from, LocalDate to) throws SQLException {
        return reportsDAO.getSegmentPerformance(from, to);
    }
}
