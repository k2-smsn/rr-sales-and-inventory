/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.rrsalesandinventory;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import utility.ThemeManager;
import utility.UserSession;
import view.LoginPanel;
import view.MainPanel;
/**
 *
 * @author k2
 */
public class Main {

    private static JFrame frame;
    private static JPanel contentPane;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // simulate admin login
            UserSession.getInstance().login("admin");
            
            frame = new JFrame("Animal Supply Shop");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);

            contentPane = new JPanel(new BorderLayout());
            contentPane.setBackground(ThemeManager.getBg());
            frame.setContentPane(contentPane);

            // ── swap this line to test different panels ──
            navigateTo(new LoginPanel());
            // navigateTo(new DashboardPanel());
            // navigateTo(new LoginPanel());

            frame.setVisible(true);
        });

    }
    
    public static void navigateTo(JPanel panel) {
        contentPane.removeAll();
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
    }

}
