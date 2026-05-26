# RR Sales & Inventory
A point-of-sale and inventory management desktop app for Animal Supply Shop.

---

## Requirements

- **Java 17 or higher** — [Download here](https://www.oracle.com/java/technologies/downloads/)
- A running **PostgreSQL** database
- A **Google GenAI API key** (for the AI assistant on the dashboard)

To check your Java version:
```bash
java -version
```

---

## Database Setup

### 1. Create the database
```sql
CREATE DATABASE rr_sales_inventory;
```

### 2. Run the schema
Connect to the database and run the following:

```sql
CREATE TABLE accounts (
    id               SERIAL PRIMARY KEY,
    username         VARCHAR(100) NOT NULL UNIQUE,
    password_hash    TEXT NOT NULL,
    role             VARCHAR(20) NOT NULL,       -- 'admin' or 'staff'
    security_question TEXT,
    security_answer  TEXT,
    status           VARCHAR(20) DEFAULT 'active' -- 'active' or 'inactive'
);

CREATE TABLE products (
    id               SERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    unit             VARCHAR(50) NOT NULL,        -- e.g. 'piece', 'kg', 'liter'
    price_per_unit   NUMERIC(10, 2) NOT NULL,
    stock_quantity   NUMERIC(10, 2) DEFAULT 0,
    intended_for     VARCHAR(100),               -- e.g. 'dog', 'cat'
    category         VARCHAR(100),               -- e.g. 'food', 'medicine'
    sales            INT DEFAULT 0,
    status           VARCHAR(20) DEFAULT 'available', -- 'available' or 'unavailable'
    expiration_date  DATE                        -- nullable
);

CREATE TABLE product_options (
    id    SERIAL PRIMARY KEY,
    type  VARCHAR(50) NOT NULL,   -- 'category' or 'intended_for'
    value VARCHAR(100) NOT NULL
);

CREATE TABLE transactions (
    id           SERIAL PRIMARY KEY,
    created_at   DATE NOT NULL,
    status       VARCHAR(20) DEFAULT 'active',   -- 'active' or 'void'
    processed_by INT REFERENCES accounts(id),
    voided_by    INT REFERENCES accounts(id)     -- nullable
);

CREATE TABLE items_sold (
    id              SERIAL PRIMARY KEY,
    transaction_id  INT REFERENCES transactions(id),
    product_id      INT REFERENCES products(id),
    quantity        NUMERIC(10, 2) NOT NULL,
    price_per_unit  NUMERIC(10, 2) NOT NULL,
    subtotal        NUMERIC(10, 2) NOT NULL
);
```

### 3. Create the first admin account
Passwords are stored as bcrypt hashes. Use a bcrypt tool or a quick script to generate one, then insert directly:

```sql
INSERT INTO accounts (username, password_hash, role, security_question, security_answer, status)
VALUES (
    'admin',
    '$2a$10$...your_bcrypt_hash_here...',
    'admin',
    'What is your pet''s name?',
    '$2a$10$...bcrypt_hash_of_answer...',
    'active'
);
```

> You can generate a bcrypt hash at [bcrypt-generator.com](https://bcrypt-generator.com) or any equivalent tool.

---

## Configuration

Create a `.env` file in the **same folder as the JAR** with the following:

```env
DB_URL=jdbc:postgresql://localhost:5432/rr_sales_inventory
DB_USER=your_db_user
DB_PASSWORD=your_db_password
AI_API_KEY=your_google_genai_api_key
```

The app will not launch without this file.

---

## Running the App

```bash
java -jar rrSalesAndInventory-1.0-SNAPSHOT.jar
```

Or double-click the JAR if your system has Java associated with `.jar` files.

---

## Features

### Dashboard
- Daily, weekly, and monthly income cards
- Low stock and expiration alerts
- AI chat assistant — ask questions about your sales and inventory data

### Inventory
- Add new products, restock existing ones, and toggle availability
- Stock alert threshold: **≤ 7 units**
- Expiration alert window: **14 days** before expiry

### Transactions
- Cart-based sales flow — search products, set quantities, confirm
- Automatically deducts stock on checkout
- Failed transactions roll back completely — no partial updates

### Reports *(admin only)*
- Sales summaries by day, week, or month
- Top products and category breakdowns
- Export any report to PDF

### Accounts *(admin only)*
- Create and manage staff accounts
- Roles: `admin` or `staff`
- Admins see Reports and Accounts in the sidebar; staff do not

### Other
- Light / dark mode toggle (sidebar, bottom left)
- Password recovery via security question
- Passwords stored as bcrypt hashes

---

## Roles at a Glance

| Feature | Staff | Admin |
|---|:---:|:---:|
| Dashboard | ✓ | ✓ |
| Inventory | ✓ | ✓ |
| Transactions | ✓ | ✓ |
| Reports | — | ✓ |
| Accounts | — | ✓ |
