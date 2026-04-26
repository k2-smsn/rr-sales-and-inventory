/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author k2
 */
import com.mycompany.rrsalesandinventory.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.getDbUrl(),
            AppConfig.getDbUser(),
            AppConfig.getDbPassword()
        );
    }
}
