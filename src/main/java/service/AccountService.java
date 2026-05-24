package service;

import dao.AccountDAO;
import entity.Account;
import java.sql.SQLException;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class AccountService {
    private static AccountService instance;
    private final AccountDAO accountDAO = AccountDAO.getInstance();

    private AccountService() {}

    public static AccountService getInstance() {
        if (instance == null) instance = new AccountService();
        return instance;
    }

    // returns the account if credentials are valid and account is active, null otherwise
    public Account login(String username, String password) throws SQLException {
        Account account = accountDAO.getByUsername(username);
        if (account == null || !account.isActive()) return null;
        if (!BCrypt.checkpw(password, account.getPasswordHash())) return null;
        return account;
    }

    // returns the security question for a username, null if username not found
    public String getSecurityQuestion(String username) throws SQLException {
        Account account = accountDAO.getByUsername(username);
        return account != null ? account.getSecurityQuestion() : null;
    }

    // verifies the security answer (case-insensitive)
    public boolean verifySecurityAnswer(String username, String answer) throws SQLException {
        Account account = accountDAO.getByUsername(username);
        if (account == null) return false;
        return BCrypt.checkpw(answer.trim().toLowerCase(), account.getSecurityAnswer());
    }

    public void resetPassword(String username, String newPassword) throws SQLException {
        Account account = accountDAO.getByUsername(username);
        if (account == null) throw new IllegalArgumentException("Account not found.");
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        accountDAO.updatePassword(account.getId(), hash);
    }

    public void createAccount(String username, String password, String role,
                              String question, String answer) throws SQLException {
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        // answers stored as lowercase hash for case-insensitive verification
        String answerHash   = BCrypt.hashpw(answer.trim().toLowerCase(), BCrypt.gensalt());
        Account account = new Account(0, username, passwordHash, role, question, answerHash, "active");
        accountDAO.add(account);
    }

    public void toggleStatus(Account account) throws SQLException {
        String newStatus = account.isActive() ? "disabled" : "active";
        accountDAO.updateStatus(account.getId(), newStatus);
    }

    public List<Account> getAllAccounts() throws SQLException {
        return accountDAO.getAll();
    }
}