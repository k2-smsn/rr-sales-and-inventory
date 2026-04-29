# RR Animal Supply Shop — Sales & Inventory System

A desktop sales and inventory management system for an animal supply shop. Built with Java Swing, PostgreSQL, and Google Gemini AI.

---

## Features

- Process and record sales transactions with automatic stock decrement
- Real-time inventory monitoring with low stock and out of stock alerts
- Transaction history with date filtering and void capability
- Sales reports (summary, top products, category & segment performance) with PDF export
- AI assistant for inventory and sales inquiries
- Role-based access control (Admin and Staff)
- Light and dark mode UI

---

## Requirements

- Java JDK 17 or higher
- PostgreSQL 13 or higher
- Maven (for dependency management)
- A Google Gemini API key (for the AI assistant)

---

## Dependencies

Add these to your `pom.xml`:

```xml
<dependencies>
    <!-- PostgreSQL JDBC Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.3</version>
    </dependency>

    <!-- dotenv for environment variables -->
    <dependency>
        <groupId>io.github.cdimascio</groupId>
        <artifactId>dotenv-java</artifactId>
        <version>3.0.0</version>
    </dependency>

    <!-- OpenPDF for report export -->
    <dependency>
        <groupId>com.github.librepdf</groupId>
        <artifactId>openpdf</artifactId>
        <version>1.3.30</version>
    </dependency>

    <!-- Google Gemini AI SDK -->
    <dependency>
        <groupId>com.google.genai</groupId>
        <artifactId>google-genai</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

---

## Database Setup

### 1. Create the Database

Open your PostgreSQL client (pgAdmin or psql) and create a new database:

```sql
CREATE DATABASE rr_sales_inventory;
```

### 2. Create the Tables

Connect to the database and run the following SQL:

```sql
-- Products table
CREATE TABLE products (
    id              SERIAL PRIMARY KEY,
    name            TEXT NOT NULL,
    unit            TEXT NOT NULL,
    price_per_unit  DECIMAL(10, 2) NOT NULL,
    stock_quantity  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    intended_for    TEXT NOT NULL,
    category        TEXT NOT NULL,
    sales           INTEGER NOT NULL DEFAULT 0,
    status          TEXT NOT NULL DEFAULT 'available'
);

-- Transactions table
CREATE TABLE transactions (
    id          SERIAL PRIMARY KEY,
    created_at  DATE NOT NULL,
    status      TEXT NOT NULL DEFAULT 'active'
);

-- Items sold table
CREATE TABLE items_sold (
    id              SERIAL PRIMARY KEY,
    transaction_id  INTEGER NOT NULL REFERENCES transactions(id),
    product_id      INTEGER NOT NULL REFERENCES products(id),
    quantity        DECIMAL(10, 2) NOT NULL,
    price_per_unit  DECIMAL(10, 2) NOT NULL,
    subtotal        DECIMAL(10, 2) NOT NULL
);
```

### 3. Valid Column Values

| Column | Valid Values |
|--------|-------------|
| `products.unit` | `piece`, `kg`, `L` |
| `products.intended_for` | `chicken`, `pigeon`, `duck`, `dog`, `cat`, `cow`, `carabao` |
| `products.category` | `food`, `medicine`, `general`, `accessories` |
| `products.status` | `available`, `unavailable` |
| `transactions.status` | `active`, `void` |

---

## Environment Setup

### 1. Create the `.env` File

In the root directory of your project (same level as `pom.xml`), create a file named `.env`:

```
DB_URL=jdbc:postgresql://localhost:5432/rr_sales_inventory
DB_USER=your_postgres_username
DB_PASSWORD=your_postgres_password

ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_admin_password

STAFF_USERNAME=staff
STAFF_PASSWORD=your_staff_password

AI_API_KEY=your_gemini_api_key
```

### 2. Fill in Your Values

| Key | Description |
|-----|-------------|
| `DB_URL` | Your PostgreSQL connection URL. Replace `localhost` and `5432` if your server is elsewhere. Replace `rr_sales_inventory` with your database name if different. |
| `DB_USER` | Your PostgreSQL username (default is usually `postgres`) |
| `DB_PASSWORD` | Your PostgreSQL user password |
| `ADMIN_USERNAME` | The username for the admin account |
| `ADMIN_PASSWORD` | The password for the admin account |
| `STAFF_USERNAME` | The username for the staff account |
| `STAFF_PASSWORD` | The password for the staff account |
| `AI_API_KEY` | Your Google Gemini API key — get one at [aistudio.google.com](https://aistudio.google.com) |

### 3. Keep the `.env` File Private

Add `.env` to your `.gitignore` to prevent it from being committed to version control:

```
.env
```

---

## Running the Application

### Via IDE (NetBeans / IntelliJ / Eclipse)

1. Open the project in your IDE
2. Make sure all Maven dependencies are downloaded (right-click project → Maven → Reload)
3. Run `Main.java` as the main class

### Via Command Line

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.yourpackage.Main"
```

---

## Project Structure

```
src/
├── main/
│   └── java/
│       ├── ai/
│       │   └── AiIntegration.java          # Google Gemini AI wrapper
│       ├── config/
│       │   └── AppConfig.java              # Environment variable loader
│       ├── dao/
│       │   ├── DBConnection.java           # Database connection factory
│       │   ├── ProductDAO.java             # Product database operations
│       │   ├── TransactionDAO.java         # Transaction database operations
│       │   ├── ItemSoldDAO.java            # Items sold database operations
│       │   └── ReportsDAO.java             # Aggregation queries for reports
│       ├── entity/
│       │   ├── Product.java                # Product model
│       │   ├── Transaction.java            # Transaction model
│       │   ├── ItemSold.java               # Items sold model
│       │   ├── CartItem.java               # In-memory cart item
│       │   ├── Receipt.java                # Transaction result object
│       │   ├── SalesSummaryData.java       # Sales summary report data
│       │   ├── TopProductData.java         # Top products report data
│       │   └── SegmentData.java            # Segment performance report data
│       ├── service/
│       │   ├── InventoryService.java       # Product/inventory business logic
│       │   ├── SalesService.java           # Transaction processing logic
│       │   ├── TransactionService.java     # Transaction retrieval and voiding
│       │   └── ReportsService.java         # Report data aggregation
│       ├── utility/
│       │   ├── ThemeManager.java           # Colors, fonts, and theme state
│       │   ├── UIUtils.java                # Reusable Swing component factory
│       │   └── UserSession.java            # Current logged-in user state
│       └── view/
│           ├── Main.java                   # Application entry point
│           ├── LoginPanel.java             # Login screen
│           ├── MainPanel.java              # App shell with sidebar and CardLayout
│           ├── SidebarPanel.java           # Navigation sidebar
│           ├── DashboardPanel.java         # Dashboard with income, alerts, AI chat
│           ├── NewTransactionPanel.java    # New transaction / checkout screen
│           ├── InventoryPanel.java         # Inventory management
│           ├── TransactionsPanel.java      # Transaction history
│           └── ReportsPanel.java          # Reports and PDF export
```

---

## User Accounts

The system has exactly two accounts configured in the `.env` file.

| Role | Access |
|------|--------|
| **Admin** | Full access — can process transactions, manage inventory (add products, adjust stock, toggle availability), view transactions, void transactions, and generate reports |
| **Staff** | Can process transactions and view inventory and transactions — cannot adjust stock, add products, toggle availability, or void transactions |

---

## Notes

- The system does not delete any data. Products can be marked unavailable but not deleted. Transactions can be voided but not deleted.
- Voided transactions are excluded from all income calculations and reports.
- Stock is automatically decremented when a transaction is confirmed. Voiding a transaction does **not** automatically restore stock — this must be done manually via the Adjust Stock feature.
- The low stock alert threshold is set to 7 units. Products at or below this quantity (but above 0) show as low stock on the dashboard.
- The AI assistant only answers questions about the current inventory and sales data. It will decline questions outside this scope.
