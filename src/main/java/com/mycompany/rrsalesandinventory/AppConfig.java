package com.mycompany.rrsalesandinventory;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getDbUrl()      { return dotenv.get("DB_URL"); }
    public static String getDbUser()     { return dotenv.get("DB_USER"); }
    public static String getDbPassword() { return dotenv.get("DB_PASSWORD"); }
    public static String getAiApiKey()   { return dotenv.get("AI_API_KEY"); }

    private AppConfig() {}
}