package utility;

import entity.Account;

public class UserSession {
    private static UserSession instance;
    private Account account;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void login(Account account)  { this.account = account; }
    public void logout()                { this.account = null; }

    public Account getAccount()         { return account; }
    public String getUsername()         { return account != null ? account.getUsername() : null; }
    public int getAccountId() { return account != null ? account.getId() : null; }
    public String getRole()             { return account != null ? account.getRole() : null; }
    public boolean isAdmin()            { return account != null && account.isAdmin(); }
    public boolean isLoggedIn()         { return account != null; }
}