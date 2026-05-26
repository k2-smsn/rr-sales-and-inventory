# Animal Supply Shop — Sales & Inventory System

> A desktop management tool for an animal supply shop. Handles sales transactions, tracks inventory, generates reports, and includes a built-in AI assistant. Built with Java Swing, backed by PostgreSQL, and powered by Google Gemini.

---

## What's Inside

| Area | What you can do |
|---|---|
| **Dashboard** | View daily, weekly, and monthly income at a glance; see low-stock and expiry alerts; chat with the AI assistant |
| **Transactions** | Process new sales with a searchable cart; browse and filter transaction history by date and status |
| **Inventory** | Browse products by category, status, or expiry; add, edit, and adjust stock (admin); toggle product availability (admin) |
| **Reports** | Generate sales summaries, top-selling products, and category/segment breakdowns by date range; export any report to PDF |
| **Accounts** | Create and manage staff and admin accounts; enable or disable users (admin only) |
| **Themes** | Flip between light and dark mode from the sidebar at any time |

### A few things worth knowing up front

- Voiding a transaction does **not** auto-restore stock — adjust it manually in Inventory if needed.
- Products and transactions are never hard-deleted. Products can be marked unavailable; transactions can be voided.
- Expiry tracking is per-product. Items expiring within 14 days show an alert; expired items are flagged separately.
- The AI assistant only answers questions about your shop's inventory and sales data — it will say so if you go off-topic.
- Staff accounts can process sales and view inventory but cannot modify products, void transactions, or access reports and accounts pages.

---

## Prerequisites

Get these installed before anything else:

- **Java JDK 17** or newer
- **Apache Maven**
- **PostgreSQL 13** or newer
- **A Google Gemini API key** — free to obtain at [aistudio.google.com](https://aistudio.google.com)

---

## Step 1 — Set Up the Database

Open your PostgreSQL client (psql, pgAdmin, or similar) and run the following:

**Create the database:**

```sql
CREATE DATABASE rr_sales_inventory;
```

**Switch to it, then create the tables:**

```sql
\c rr_sales_inventory
```

```sql
CREATE TABLE accounts (
    id                SERIAL PRIMARY KEY,
    username          TEXT NOT NULL UNIQUE,
    password_hash     TEXT NOT NULL,
    role              TEXT NOT NULL,
    security_question TEXT NOT NULL,
    security_answer   TEXT NOT NULL,
    status            TEXT NOT NULL DEFAULT 'active'
);

CREATE TABLE products (
    id              SERIAL PRIMARY KEY,
    name            TEXT NOT NULL,
    unit            TEXT NOT NULL,
    price_per_unit  DECIMAL(10, 2) NOT NULL,
    stock_quantity  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    intended_for    TEXT NOT NULL,
    category        TEXT NOT NULL,
    sales           INTEGER NOT NULL DEFAULT 0,
    status          TEXT NOT NULL DEFAULT 'available',
    expiration_date DATE
);

CREATE TABLE transactions (
    id          SERIAL PRIMARY KEY,
    created_at  DATE NOT NULL,
    status      TEXT NOT NULL DEFAULT 'active'
);

CREATE TABLE items_sold (
    id              SERIAL PRIMARY KEY,
    transaction_id  INTEGER NOT NULL REFERENCES transactions(id),
    product_id      INTEGER NOT NULL REFERENCES products(id),
    quantity        DECIMAL(10, 2) NOT NULL,
    price_per_unit  DECIMAL(10, 2) NOT NULL,
    subtotal        DECIMAL(10, 2) NOT NULL
);
```

**Accepted column values reference:**

| Column | Valid Values |
|---|---|
| `accounts.role` | `admin`, `staff` |
| `accounts.status` | `active`, `disabled` |
| `products.unit` | `piece`, `kg`, `L` |
| `products.intended_for` | `chicken`, `pigeon`, `duck`, `dog`, `cat`, `cow`, `carabao` |
| `products.category` | `food`, `medicine`, `general`, `accessories` |
| `products.status` | `available`, `unavailable` |
| `transactions.status` | `active`, `void` |

**Seed the first admin account:**

Passwords are stored as BCrypt hashes. Use the app's Accounts panel to create accounts once running, or insert a pre-hashed password directly. The test credentials below use BCrypt hash for `admin123` and `staff123` respectively.

```sql
-- Admin account (password: admin123)
INSERT INTO accounts (username, password_hash, role, security_question, security_answer, status)
VALUES (
    'admin',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3r.7U5wAnJpIEoOuHQUG9Gc.lbG8HO',
    'admin',
    'What is your pet''s name?',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p396tR6zQT9yVBJlJZ9D.O',
    'active'
);

-- Staff account (password: staff123)
INSERT INTO accounts (username, password_hash, role, security_question, security_answer, status)
VALUES (
    'staff',
    '$2a$10$Nf8N1GFGzQGwK7PMaHxNheCWJOgJWv.O5a5z6Vu5I5HcpkF9sX0Oi',
    'staff',
    'What is your mother''s maiden name?',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p396tR6zQT9yVBJlJZ9D.O',
    'active'
);
```

> ! These are pre-computed hashes for testing only. Change passwords after first login using the Accounts panel, or generate fresh BCrypt hashes with any online BCrypt tool.

---

## Step 2 — Create the `.env` File

In the **project root** (the same folder as `pom.xml`), create a file named exactly `.env` with the following contents:

```
DB_URL=jdbc:postgresql://localhost:5432/rr_sales_inventory
DB_USER=your_postgres_username
DB_PASSWORD=your_postgres_password
AI_API_KEY=your_gemini_api_key
```

Replace each value with your own. If PostgreSQL is running on a non-default port or remote host, update `DB_URL` accordingly.

Make sure `.env` is listed in `.gitignore` so it never gets pushed to version control:

```
.env
```

---

## Step 3 — Build and Run

### Option A — NetBeans (recommended for development)

1. Open the project in NetBeans.
2. Right-click the project → **Clean and Build**.
3. Click **Run** (or press F6).

### Option B — Terminal / Command Line

```bash
# From the project root (where pom.xml lives)
mvn package
java -jar target/rrSalesAndInventory-1.0-SNAPSHOT.jar
```

The `.env` file must be in the **same directory** you run the command from.

### Option C — Distributing as a standalone JAR

Put the JAR and `.env` together in one folder:

```
launch/
├── rrSalesAndInventory-1.0-SNAPSHOT.jar
└── .env
```

On Linux/macOS, you can create a launcher script:

```bash
#!/bin/bash
cd "$(dirname "$0")"
java -jar rrSalesAndInventory-1.0-SNAPSHOT.jar
```

Save it as `run.sh`, then make it executable:

```bash
chmod +x run.sh
```

---

## Test Login Credentials

Use these to explore the system right away.

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Staff | `staff` | `staff123` |

**Admin** has full access to all panels including Reports, Accounts, and all inventory management actions.

**Staff** can process transactions and view inventory but cannot modify products, void transactions, or see the Reports and Accounts panels.

---

## Project Layout

```
src/main/java/
├── ai/
│   └── AiIntegration.java          # Google Gemini chat wrapper
├── com/mycompany/rrsalesandinventory/
│   ├── AppConfig.java              # Reads all values from .env
│   └── Main.java                   # App entry point, window setup
├── dao/                            # Direct database access layer
│   ├── AccountDAO.java
│   ├── ProductDAO.java
│   ├── ProductOptionDAO.java
│   ├── TransactionDAO.java
│   ├── ItemSoldDAO.java
│   └── ReportsDAO.java
├── entity/                         # Plain data objects
│   ├── Account.java
│   ├── Product.java
│   ├── Transaction.java
│   ├── ItemSold.java
│   ├── CartItem.java
│   ├── Receipt.java
│   ├── SalesSummaryData.java
│   ├── TopProductData.java
│   └── SegmentData.java
├── service/                        # Business logic layer
│   ├── AccountService.java
│   ├── InventoryService.java
│   ├── ProductOptionService.java
│   ├── SalesService.java
│   ├── TransactionService.java
│   └── ReportsService.java
├── utility/
│   ├── DBConnection.java           # Opens JDBC connections
│   ├── ThemeManager.java           # Light/dark mode colors and fonts
│   ├── UIUtils.java                # Reusable styled Swing components
│   └── UserSession.java            # Holds the logged-in user's state
└── view/
    ├── LoginPanel.java
    ├── MainPanel.java
    ├── SidebarPanel.java
    ├── DashboardPanel.java
    ├── NewTransactionPanel.java
    ├── InventoryPanel.java
    ├── TransactionsPanel.java
    ├── ReportsPanel.java
    └── AccountsPanel.java
```

---

## Dependencies (from `pom.xml`)

These are resolved automatically by Maven — no manual downloads needed.

| Library | Purpose |
|---|---|
| `org.postgresql:postgresql:42.7.3` | PostgreSQL JDBC driver |
| `com.google.genai:google-genai:1.4.1` | Google Gemini AI SDK |
| `com.github.librepdf:openpdf:1.3.30` | PDF export |
| `io.github.cdimascio:dotenv-java:3.2.0` | Loads `.env` into app config |
| `org.mindrot:jbcrypt:0.4` | Password hashing |
