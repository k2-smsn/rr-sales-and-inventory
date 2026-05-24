/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author k2
 */
import java.awt.Color;
import java.awt.Font;

public class ThemeManager {
    private static boolean darkMode = false;

    // — Colors —
    public static final Color LIGHT_BG           = new Color(245, 245, 245);
    public static final Color LIGHT_SURFACE      = Color.WHITE;
    public static final Color LIGHT_TEXT         = new Color(30, 30, 30);
    public static final Color LIGHT_SUBTEXT      = new Color(120, 120, 120);
    public static final Color LIGHT_BORDER       = new Color(210, 210, 210);

    public static final Color DARK_BG            = new Color(18, 18, 18);
    public static final Color DARK_SURFACE       = new Color(30, 30, 30);
    public static final Color DARK_TEXT          = new Color(230, 230, 230);
    public static final Color DARK_SUBTEXT       = new Color(160, 160, 160);
    public static final Color DARK_BORDER        = new Color(60, 60, 60);

    public static final Color ACCENT             = new Color(72, 133, 237);
    public static final Color ACCENT_HOVER       = new Color(50, 110, 215);
    public static final Color DANGER             = new Color(220, 53, 69);
    public static final Color DANGER_HOVER       = new Color(190, 30, 45);
    public static final Color SUCCESS            = new Color(40, 167, 69);
    public static final Color WARNING            = new Color(255, 193, 7);

    // — Fonts —
    // — Fonts —
    public static final Font FONT_REGULAR        = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BOLD           = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SMALL          = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADING        = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEADING     = new Font("Segoe UI", Font.BOLD, 17);

    private ThemeManager() {}

    public static void setDarkMode(boolean enabled) { darkMode = enabled; }
    public static boolean isDarkMode() { return darkMode; }

    public static Color getBg()      { return darkMode ? DARK_BG      : LIGHT_BG; }
    public static Color getSurface() { return darkMode ? DARK_SURFACE  : LIGHT_SURFACE; }
    public static Color getText()    { return darkMode ? DARK_TEXT     : LIGHT_TEXT; }
    public static Color getSubtext() { return darkMode ? DARK_SUBTEXT  : LIGHT_SUBTEXT; }
    public static Color getBorder()  { return darkMode ? DARK_BORDER   : LIGHT_BORDER; }
}
