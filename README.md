# RR Sales and Inventory System

A desktop app for managing sales and inventory for an animal supply shop. Built with Java Swing and PostgreSQL. Has two user roles (admin and staff), light/dark mode, and a built-in AI assistant.

---

## Before You Start

Make sure you have these installed:

- Java JDK 17+
- PostgreSQL 13+
- Apache Maven
- A Google Gemini API key — get one free at [aistudio.google.com](https://aistudio.google.com)

---

## Getting the Database Ready

### Create the database

```sql
CREATE DATABASE rr_sales_inventory;
```

### Create the tables

Connect to that database and run this:

```sql
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

### Accepted values for certain columns

| Column | Accepted Values |
|--------|----------------|
| `products.unit` | `piece`, `kg`, `L` |
| `products.intended_for` | `chicken`, `pigeon`, `duck`, `dog`, `cat`, `cow`, `carabao` |
| `products.category` | `food`, `medicine`, `general`, `accessories` |
| `products.status` | `available`, `unavailable` |
| `transactions.status` | `active`, `void` |

---

## Setting Up the .env File

Create a file called `.env` in the root of the project (same level as `pom.xml`) and fill it in:

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

If your PostgreSQL is running on a different port or host, update `DB_URL` accordingly. Everything else is straightforward — just fill in your own values.

Make sure `.env` is in your `.gitignore` so you don't accidentally push credentials:

```
.env
```

---

## Dependencies

These go inside `<dependencies>` in your `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>

<dependency>
    <groupId>com.google.genai</groupId>
    <artifactId>google-genai</artifactId>
    <version>1.4.1</version>
</dependency>

<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.30</version>
</dependency>

<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.2.0</version>
</dependency>
```

To build a runnable JAR, add this inside `<build><plugins>`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.5.1</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>com.mycompany.rrsalesandinventory.Main</mainClass>
                    </transformer>
                </transformers>
                <createDependencyReducedPom>false</createDependencyReducedPom>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Running the App

### From NetBeans

Right-click the project → **Clean and Build**, then **Run**.

### From the terminal

```bash
mvn package
java -jar target/rrSalesAndInventory-1.0-SNAPSHOT.jar
```

Make sure the `.env` file is in the same directory you're running the command from.

### As a standalone JAR (distributing to others)

Put these two files in the same folder:

```
your_folder/
├── rrSalesAndInventory-1.0-SNAPSHOT.jar
└── .env
```

On Linux, create a `run.sh` script for easy launching:

```bash
#!/bin/bash
cd "$(dirname "$0")"
java -jar rrSalesAndInventory-1.0-SNAPSHOT.jar
```

Make it executable:

```bash
chmod +x run.sh
```

Then just double-click `run.sh` to launch.

---

## What It Does

- Record sales transactions — stock is decremented automatically on confirmation
- Search products and add them to a cart with quantity controls
- Monitor inventory with stock alerts on the dashboard (threshold: 7 units)
- Adjust stock levels and toggle product availability (admin only)
- View transaction history filtered by date, with the option to void transactions (admin only)
- Generate three types of reports: sales summary, top selling products, and category/segment performance
- Export any report to PDF
- Ask the built-in AI assistant questions about current inventory and sales data
- Switch between light and dark mode

---

## User Roles

| | Admin | Staff |
|---|---|---|
| Process transactions | ✓ | ✓ |
| View inventory | ✓ | ✓ |
| Add products | ✓ | ✗ |
| Adjust stock | ✓ | ✗ |
| Toggle availability | ✓ | ✗ |
| View transactions | ✓ | ✓ |
| Void transactions | ✓ | ✗ |
| Generate reports | ✓ | ✓ |
| Export to PDF | ✓ | ✓ |

---

## Project Structure

```
src/main/java/
├── ai/
│   └── AiIntegration.java          # Gemini AI wrapper and chat session
├── config/
│   └── AppConfig.java              # Loads all values from the .env file
├── dao/
│   ├── DBConnection.java           # Opens database connections
│   ├── ProductDAO.java             # All product SQL operations
│   ├── TransactionDAO.java         # All transaction SQL operations
│   ├── ItemSoldDAO.java            # All items_sold SQL operations
│   └── ReportsDAO.java             # Aggregation queries for reports
├── entity/
│   ├── Product.java                # Maps to the products table
│   ├── Transaction.java            # Maps to the transactions table
│   ├── ItemSold.java               # Maps to the items_sold table
│   ├── CartItem.java               # In-memory object for cart items
│   ├── Receipt.java                # Result returned after a sale is processed
│   ├── SalesSummaryData.java       # Data object for sales summary report
│   ├── TopProductData.java         # Data object for top products report
│   └── SegmentData.java            # Data object for segment performance report
├── service/
│   ├── InventoryService.java       # Product search, stock, availability logic
│   ├── SalesService.java           # Transaction processing logic
│   ├── TransactionService.java     # Transaction retrieval and voiding
│   └── ReportsService.java         # Pulls and aggregates report data
├── utility/
│   ├── ThemeManager.java           # Colors, fonts, and light/dark mode state
│   ├── UIUtils.java                # Factory for reusable styled Swing components
│   └── UserSession.java            # Stores the currently logged-in user
└── view/
    ├── Main.java                   # Entry point, app window setup
    ├── LoginPanel.java             # Login screen
    ├── MainPanel.java              # App shell — sidebar + page switcher
    ├── SidebarPanel.java           # Navigation, theme toggle, logout
    ├── DashboardPanel.java         # Income cards, stock alerts, AI chat
    ├── NewTransactionPanel.java    # Checkout / new sale screen
    ├── InventoryPanel.java         # Product list and management
    ├── TransactionsPanel.java      # Transaction history
    └── ReportsPanel.java           # Report generation and PDF export
```

---

## Things Worth Knowing

- Nothing gets deleted. Products can be marked unavailable and transactions can be voided, but no data is ever removed from the database.
- Voiding a transaction does not restore stock. If a transaction is cancelled, stock has to be manually adjusted through the inventory page.
- The AI assistant only answers questions about the shop's inventory and sales. It will decline anything outside that scope.
- The `.env` file must always be in the same directory as the JAR when running outside of the IDE.
