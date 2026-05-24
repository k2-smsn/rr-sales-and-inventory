package entity;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private LocalDate createdAt;
    private String status;
    private int processedBy; // account id of the user who processed this

    public Transaction() {}

    public Transaction(int id, LocalDate createdAt, String status, int processedBy) {
        this.id          = id;
        this.createdAt   = createdAt;
        this.status      = status;
        this.processedBy = processedBy;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }
    public LocalDate getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDate d)       { this.createdAt = d; }
    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }
    public int getProcessedBy()                 { return processedBy; }
    public void setProcessedBy(int processedBy) { this.processedBy = processedBy; }
}