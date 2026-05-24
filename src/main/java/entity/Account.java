package entity;

public class Account {
    private int id;
    private String username;
    private String passwordHash;
    private String role;
    private String securityQuestion;
    private String securityAnswer; // stored as bcrypt hash
    private String status;

    public Account() {}

    public Account(int id, String username, String passwordHash, String role,
                   String securityQuestion, String securityAnswer, String status) {
        this.id               = id;
        this.username         = username;
        this.passwordHash     = passwordHash;
        this.role             = role;
        this.securityQuestion = securityQuestion;
        this.securityAnswer   = securityAnswer;
        this.status           = status;
    }

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getUsername()                      { return username; }
    public void setUsername(String username)         { this.username = username; }

    public String getPasswordHash()                  { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    public String getSecurityQuestion()                       { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion)  { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer()                         { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer)      { this.securityAnswer = securityAnswer; }

    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    public boolean isActive() { return "active".equalsIgnoreCase(status); }
    public boolean isAdmin()  { return "admin".equalsIgnoreCase(role); }
}