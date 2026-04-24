/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.rrsalesandinventory;

/**
 *
 * @author k2
 */
import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getDbUrl() { return dotenv.get("DB_URL"); }
    public static String getDbUser() { return dotenv.get("DB_USER"); }
    public static String getDbPassword() { return dotenv.get("DB_PASSWORD"); }
    public static String getAiApiKey() { return dotenv.get("AI_API_KEY"); }
    public static String getAdminUsername() { return dotenv.get("ADMIN_USERNAME"); }
    public static String getAdminPassword() { return dotenv.get("ADMIN_PASSWORD"); }
    public static String getStaffUsername() { return dotenv.get("STAFF_USERNAME"); }
    public static String getStaffPassword() { return dotenv.get("STAFF_PASSWORD"); }

    private AppConfig() {}
}
