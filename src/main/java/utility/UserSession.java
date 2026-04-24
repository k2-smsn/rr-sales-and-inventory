/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author k2
 */
public class UserSession {
    private static UserSession instance;
    private String username;
    private String role;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void login(String username) {
        this.username = username;
        this.role = username.equalsIgnoreCase("admin") ? "admin" : "staff";
    }

    public void logout() {
        this.username = null;
        this.role = null;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "admin".equals(role); }
    public boolean isLoggedIn() { return username != null; }
}