# 📘 SQL & PL/SQL Master Feature Reference

This document is a comprehensive guide to all features, syntax, and concepts in the SQL language. Use this as your primary "Source of Truth" for database development.

---

## 🏗️ 1. Data Definition Language (DDL)
*DDL is used to define, modify, and manage the structure of database objects.*

### **Table Management**
* **CREATE TABLE**: Defines a new table.
    * *Syntax:* `CREATE TABLE name (column1 datatype constraints, column2 datatype);`
* **ALTER TABLE**: Modifies an existing table.
    * *Syntax:* `ALTER TABLE name ADD column_name datatype;`
* **DROP TABLE**: Deletes the table and all data permanently.
    * *Syntax:* `DROP TABLE name;`
* **TRUNCATE TABLE**: Deletes all data but keeps the table structure.
    * *Syntax:* `TRUNCATE TABLE name;`

### **Constraints (Data Integrity)**
* **PRIMARY KEY**: Uniquely identifies each record.
* **FOREIGN KEY**: Ensures referential integrity between two tables.
* **NOT NULL**: Prevents null values in a column.
* **UNIQUE**: Ensures all values in a column are different.
* **CHECK**: Ensures values meet a specific condition (e.g., `CHECK (age >= 18)`).



---

## ✍️ 2. Data Manipulation Language (DML)
*DML is used to handle the actual data stored in the tables.*

### **Core Commands**
* **INSERT**: Adds new rows.
    * *Syntax:* `INSERT INTO table (cols) VALUES (vals);`
* **UPDATE**: Modifies existing rows.
    * *Syntax:* `UPDATE table SET col = val WHERE condition;`
* **DELETE**: Removes rows.
    * *Syntax:* `DELETE FROM table WHERE condition;`
* **MERGE (Upsert)**: Performs an update if a record exists, otherwise performs an insert.
    * *Syntax:* 
    ```sql
        MERGE INTO target t USING source s ON (t.id = s.id)
        WHEN MATCHED THEN UPDATE SET t.val = s.val
        WHEN NOT MATCHED THEN INSERT (id, val) VALUES (s.id, s.val);
       
     ```

---

## 🔍 3. Data Query Language (DQL)
*DQL is used to retrieve data. This is the most common part of SQL.*

### **The Clause Order**
1.  **SELECT**: The columns you want to see.
2.  **FROM**: The tables you are querying.
3.  **WHERE**: Filters individual rows.
4.  **GROUP BY**: Groups rows for aggregation.
5.  **HAVING**: Filters groups (used with GROUP BY).
6.  **ORDER BY**: Sorts the final output (`ASC` or `DESC`).

### **Operators**
* **Logical**: `AND`, `OR`, `NOT`
* **Comparison**: `=`, `<>`, `!=`, `>`, `<`, `>=`, `<=`
* **Range/Pattern**: `BETWEEN`, `IN`, `LIKE` (`%` for wildcards).

---

## 🔗 4. Table Relationships (JOINS)
*Joins allow you to combine data from multiple tables.*

* **INNER JOIN**: Returns only rows with matches in both tables.
* **LEFT JOIN**: All rows from left table, plus matches from right.
* **RIGHT JOIN**: All rows from right table, plus matches from left.
* **FULL JOIN**: All rows from both tables when a match exists in either.
* **CROSS JOIN**: A Cartesian product (every row from A joined with every row from B).



---

## 📈 5. Advanced SQL Concepts

### **Analytical (Window) Functions**
These allow calculations across sets of rows without collapsing them into a single result.
* **ROW_NUMBER()**: Assigns a unique rank.
* **RANK() / DENSE_RANK()**: For ranking with or without gaps.
* **LEAD() / LAG()**: Accessing the next or previous row's data.

### **CTEs (Common Table Expressions)**
* **Syntax:** `WITH cte_name AS (SELECT ...) SELECT * FROM cte_name;`

---

## 💎 6. PL/SQL (Procedural Language)
*PL/SQL adds programming logic like loops and variables to SQL.*

* **Procedures**: Perform actions (no return value required).
* **Functions**: Perform calculations and **must** return a value.
* **Triggers**: Automatic code that runs on specific events (`BEFORE/AFTER INSERT`).
* **Packages**: Containers that group related procedures and functions.
* **Cursors**: Used to process query results row-by-row in a loop.



---

## 🚀 7. Step-by-Step Implementation Guide

### **Step 1: Setup Infrastructure**
Copy the DDL commands to create your tables. Ensure your Primary Keys are defined first to avoid dependency errors.

### **Step 2: Populate Data**
Use the DML commands to insert records. Start with "Parent" tables (Departments) before "Child" tables (Employees).

### **Step 3: Run Queries**
Start with basic `SELECT` statements, then progress to `JOINS` and `GROUP BY` to generate reports.

### **Step 4: Optimize**
Create **INDEXES** on columns that you use frequently in `WHERE` clauses to speed up performance.
```sql
CREATE INDEX idx_emp_email ON employees(email);

# 📘 SQL & PL/SQL Master Feature Reference

This document is a comprehensive guide to all features, syntax, and concepts in the SQL language. Use this as your primary "Source of Truth" for database development.

---

## 🏗️ 1. Data Definition Language (DDL)
*DDL is used to define, modify, and manage the structure of database objects.*

### **Table Management**
* **CREATE TABLE**: Defines a new table.
    * *Syntax:* `CREATE TABLE name (column1 datatype constraints, column2 datatype);`
* **ALTER TABLE**: Modifies an existing table structure.
    * *Syntax:* `ALTER TABLE name ADD column_name datatype;`
* **DROP TABLE**: Deletes the table and all data permanently.
    * *Syntax:* `DROP TABLE name;`
* **TRUNCATE TABLE**: Deletes all data but keeps the table structure (cannot be rolled back).
    * *Syntax:* `TRUNCATE TABLE name;`

### **Constraints (Data Integrity)**
* **PRIMARY KEY**: Uniquely identifies each record.
* **FOREIGN KEY**: Ensures referential integrity between two tables.
* **NOT NULL**: Prevents null values in a column.
* **UNIQUE**: Ensures all values in a column are different.
* **CHECK**: Ensures values meet a specific condition (e.g., `CHECK (salary > 0)`).



---

## ✍️ 2. Data Manipulation Language (DML)
*DML is used to handle the actual data stored in the tables.*

### **Core Commands**
* **INSERT**: Adds new rows.
    * *Syntax:* `INSERT INTO table (cols) VALUES (vals);`
* **UPDATE**: Modifies existing rows.
    * *Syntax:* `UPDATE table SET col = val WHERE condition;`
* **DELETE**: Removes rows based on condition.
    * *Syntax:* `DELETE FROM table WHERE condition;`
* **MERGE (Upsert)**: Performs an update if a record exists, otherwise performs an insert.
    * *Syntax:* ```sql
    MERGE INTO target t USING source s ON (t.id = s.id)
    WHEN MATCHED THEN UPDATE SET t.val = s.val
    WHEN NOT MATCHED THEN INSERT (id, val) VALUES (s.id, s.val);
    ```

---

## 🔍 3. Data Query Language (DQL)
*DQL is used to retrieve data from the database.*

### **The Clause Logical Order**
1.  **FROM**: Specifies the source tables.
2.  **WHERE**: Filters individual rows.
3.  **GROUP BY**: Aggregates rows into groups.
4.  **HAVING**: Filters groups (used with aggregate functions).
5.  **SELECT**: Specifies the columns to display.
6.  **ORDER BY**: Sorts the output (`ASC` or `DESC`).



### **Operators**
* **Logical**: `AND`, `OR`, `NOT`
* **Comparison**: `=`, `<>`, `!=`, `>`, `<`, `>=`, `<=`
* **Set Operators**: `UNION`, `UNION ALL`, `INTERSECT`, `MINUS` (Combines multiple queries).

---

## 🔗 4. Table Relationships (JOINS)
*Joins allow you to combine data from multiple tables.*

* **INNER JOIN**: Returns only rows with matches in both tables.
* **LEFT JOIN**: All rows from left table, plus matches from right.
* **RIGHT JOIN**: All rows from right table, plus matches from left.
* **FULL JOIN**: All rows from both tables when a match exists in either.
* **CROSS JOIN**: Every row from A joined with every row from B (Cartesian product).



---

## ⚡ 5. Performance Tuning & Indexing
*Optimizing the database to handle large volumes of data efficiently.*

### **Types of Indexes**
* **B-Tree Index**: The default; best for high-cardinality columns (unique or nearly unique).
    * *Syntax:* `CREATE INDEX idx_name ON table(column);`
* **Bitmap Index**: Best for low-cardinality columns (e.g., Gender, Status).
* **Function-Based Index**: For columns used with functions in WHERE clauses.
    * *Syntax:* `CREATE INDEX idx_upper ON table(UPPER(column));`



### **Optimization Strategies**
* **Explain Plan**: Analyze how the database executes a query.
* **Partitioning**: Breaking large tables into smaller, manageable pieces (Range, List, Hash).
* **Avoid Select ***: Only fetch columns you need to reduce I/O.
* **Use Bind Variables**: Prevents hard parsing and improves security against SQL Injection.

---

## 💎 6. PL/SQL (Procedural Language)
*PL/SQL adds programming logic like loops and variables to standard SQL.*

* **Procedures**: Units of code that perform actions.
* **Functions**: Units of code that **must** return a value.
* **Triggers**: Automated code executed on events (e.g., `BEFORE INSERT`).
* **Cursors**: Mechanisms to fetch and process multi-row results one by one.



---

## 🚀 7. Implementation Steps

### **Step 1: Setup**
Execute the **DDL** script to create your tables and constraints. 
### **Step 2: Data Load**
Execute the **DML** script. Always load parent tables (e.g., Departments) before child tables (e.g., Employees).
### **Step 3: Verification**
Run a `SELECT *` on each table to ensure data integrity.
### **Step 4: Logic Deployment**
Compile your **PL/SQL** triggers and procedures to automate business rules.
```sql
CREATE OR REPLACE TRIGGER update_stock ...
```
# 📘 SQL & PL/SQL Master Feature Reference

This document is a comprehensive guide to all features, syntax, and concepts in the SQL language. Use this as your primary "Source of Truth" for database development.

---

## 🏗️ 1. Data Definition Language (DDL)
*DDL is used to define, modify, and manage the structure of database objects.*

### **Table Management**
* **CREATE TABLE**: Defines a new table.
    * *Syntax:* `CREATE TABLE name (column1 datatype constraints, column2 datatype);`
* **ALTER TABLE**: Modifies an existing table structure.
    * *Syntax:* `ALTER TABLE name ADD column_name datatype;`
* **DROP TABLE**: Deletes the table and all data permanently.
    * *Syntax:* `DROP TABLE name;`
* **TRUNCATE TABLE**: Deletes all data but keeps the table structure (cannot be rolled back).
    * *Syntax:* `TRUNCATE TABLE name;`

### **Constraints (Data Integrity)**
* **PRIMARY KEY**: Uniquely identifies each record.
* **FOREIGN KEY**: Ensures referential integrity between two tables.
* **NOT NULL**: Prevents null values in a column.
* **UNIQUE**: Ensures all values in a column are different.
* **CHECK**: Ensures values meet a specific condition (e.g., `CHECK (salary > 0)`).



---

## ✍️ 2. Data Manipulation Language (DML)
*DML is used to handle the actual data stored in the tables.*

### **Core Commands**
* **INSERT**: Adds new rows.
    * *Syntax:* `INSERT INTO table (cols) VALUES (vals);`
* **UPDATE**: Modifies existing rows.
    * *Syntax:* `UPDATE table SET col = val WHERE condition;`
* **DELETE**: Removes rows based on condition.
* **MERGE (Upsert)**: Performs an update if a record exists, otherwise performs an insert.
    * *Syntax:* ```sql
    MERGE INTO target t USING source s ON (t.id = s.id)
    WHEN MATCHED THEN UPDATE SET t.val = s.val
    WHEN NOT MATCHED THEN INSERT (id, val) VALUES (s.id, s.val);
    ```

---

## 🔍 3. Data Query Language (DQL)
*DQL is used to retrieve data from the database.*

### **The Clause Logical Order**
1. **FROM**: Specifies the source tables.
2. **WHERE**: Filters individual rows.
3. **GROUP BY**: Aggregates rows into groups.
4. **HAVING**: Filters groups (used with aggregate functions).
5. **SELECT**: Specifies the columns to display.
6. **ORDER BY**: Sorts the output (`ASC` or `DESC`).



---

## 🔗 4. Table Relationships (JOINS)
*Joins allow you to combine data from multiple tables.*

* **INNER JOIN**: Returns only rows with matches in both tables.
* **LEFT JOIN**: All rows from left table, plus matches from right.
* **RIGHT JOIN**: All rows from right table, plus matches from left.
* **FULL JOIN**: All rows from both tables when a match exists in either.
* **CROSS JOIN**: Every row from A joined with every row from B (Cartesian product).



---

## ⚡ 5. Performance Tuning & Indexing
*Optimizing the database to handle large volumes of data efficiently.*

### **Types of Indexes**
* **B-Tree Index**: The default; best for high-cardinality columns.
    * *Syntax:* `CREATE INDEX idx_name ON table(column);`
* **Bitmap Index**: Best for low-cardinality columns (e.g., Gender, Status).
* **Function-Based Index**: For columns used with functions in WHERE clauses.



### **Optimization Strategies**
* **Explain Plan**: Analyze how the database executes a query.
* **Partitioning**: Breaking large tables into smaller pieces (Range, List, Hash).
* **Selectivity**: Aim to index columns that filter out the most data.

---

## 💎 6. PL/SQL (Procedural Language)
*PL/SQL adds programming logic like loops and variables to standard SQL.*

* **Procedures**: Units of code that perform actions.
* **Functions**: Units of code that **must** return a value.
* **Triggers**: Automated code executed on events (e.g., `BEFORE INSERT`).
* **Cursors**: Mechanisms to fetch results row-by-row.



---

## 🛠️ 7. Troubleshooting & Common Errors
*Guide to resolving the most frequent database issues.*

| Error Code | Common Name | Cause & Solution |
| :--- | :--- | :--- |
| **ORA-00001** | Unique Constraint Violated | You tried to insert a duplicate value into a Primary Key or Unique column. **Fix:** Check for existing values before inserting. |
| **ORA-00904** | Invalid Identifier | The column name is misspelled or does not exist in the table. **Fix:** Verify the table schema using `DESC table_name`. |
| **ORA-00942** | Table or View Does Not Exist | The table name is wrong or you don't have permission to see it. **Fix:** Check spelling and schema prefix (e.g., `HR.employees`). |
| **ORA-01403** | No Data Found | A `SELECT INTO` statement returned 0 rows. **Fix:** Add an `EXCEPTION` block to handle empty results. |
| **ORA-02291** | Integrity Constraint Violated | You tried to insert a Child record without a matching Parent record. **Fix:** Insert the Parent row first. |
| **ORA-01017** | Invalid Username/Password | Connection failed. **Fix:** Reset password or check the connection string. |

---

## 🚀 8. Implementation Steps

1. **Setup**: Execute **DDL** to create tables.
2. **Data Load**: Execute **DML**. Load Parent tables before Child tables.
3. **Verification**: Run `SELECT` counts to ensure data integrity.
4. **Automation**: Compile **Triggers** and **Procedures**.

# 📘 SQL & PL/SQL Master Feature Reference

This document is a comprehensive guide to all features, syntax, and concepts in the SQL language. It is designed to be a standalone "Source of Truth" for your development environment.

---

## 🏗️ 1. Data Definition Language (DDL)
*Used to define and manage the structure of database objects.*

### **Core Object Management**
* **CREATE TABLE**: Defines a new table.
    * *Syntax:* `CREATE TABLE name (col1 type constraints, col2 type);`
* **ALTER TABLE**: Modifies an existing structure.
    * *Syntax:* `ALTER TABLE name ADD column_name datatype;`
* **DROP**: Permanently deletes an object.
* **TRUNCATE**: Removes all rows but keeps the structure for reuse.

### **Integrity Constraints**
* **PRIMARY KEY**: Unique identifier (No Nulls).
* **FOREIGN KEY**: Links to a Primary Key in another table.
* **CHECK**: Validates data logic (e.g., `salary > 0`).
* **UNIQUE**: Prevents duplicates in a column.



---

## ✍️ 2. Data Manipulation Language (DML)
*Used to manage the data residing within the structures.*

* **INSERT**: Adds new records.
* **UPDATE**: Changes existing values based on a `WHERE` condition.
* **DELETE**: Removes specific rows.
* **MERGE**: Checks if a record exists; updates if yes, inserts if no.
    ```sql
    MERGE INTO target t USING source s ON (t.id = s.id)
    WHEN MATCHED THEN UPDATE SET t.val = s.val
    WHEN NOT MATCHED THEN INSERT (id, val) VALUES (s.id, s.val);
    ```

---

## 🔍 3. Data Query Language (DQL)
*The most powerful part of SQL for data retrieval and analysis.*

### **Query Execution Order**
1. **FROM**: Source tables.
2. **WHERE**: Row-level filters.
3. **GROUP BY**: Aggregation points.
4. **HAVING**: Group-level filters.
5. **SELECT**: Columns and expressions.
6. **ORDER BY**: Sorting.



---

## 🔗 4. Table Relationships (JOINS)
*Combining data from multiple tables.*

* **INNER JOIN**: Matches in both tables.
* **LEFT JOIN**: All from left, matches from right.
* **RIGHT JOIN**: All from right, matches from left.
* **FULL JOIN**: All rows when a match exists in either.
* **SELF JOIN**: Joining a table to itself (e.g., `Employee` -> `Manager`).



---

## ⚡ 5. Performance Tuning & Indexing
*Speeding up query execution for large datasets.*

* **B-Tree Index**: Best for unique lookups and range scans.
* **Bitmap Index**: Best for columns with low variety (e.g., `Gender`, `Status`).
* **Function-Based Index**: Indexing the result of a function (e.g., `UPPER(name)`).
* **Explain Plan**: Visualizing the path the database takes to find data.



---

## 🛠️ 6. Troubleshooting & Error Handling
*Common ORA errors and their solutions.*

| Error Code | Meaning | Typical Fix |
| :--- | :--- | :--- |
| **ORA-00001** | Unique Constraint Violation | Check for duplicate IDs in your `INSERT`. |
| **ORA-00904** | Invalid Identifier | Check for typos in column names. |
| **ORA-00942** | Table Not Found | Verify table exists and check permissions. |
| **ORA-02291** | Integrity Violation | Ensure Parent data exists before inserting Child data. |

---

## 🚀 7. Final Project: Banking Transaction System
*Integrating all concepts into a functional system.*

### **Step 1: Schema (DDL)**
```sql
CREATE TABLE accounts (
    acc_id NUMBER PRIMARY KEY,
    holder_name VARCHAR2(100),
    balance NUMBER(15,2) CHECK (balance >= 0)
);

CREATE TABLE audit_log (
    log_id NUMBER GENERATED ALWAYS AS IDENTITY,
    acc_id NUMBER,
    old_bal NUMBER,
    new_bal NUMBER,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

### **Step 2: Logic (PL/SQL Trigger)**
*This trigger automates the auditing process. Every time a balance is modified in the `accounts` table, a record is silently inserted into the `audit_log` table without requiring extra code from the application layer.*

```sql
CREATE OR REPLACE TRIGGER trg_audit_balance
AFTER UPDATE OF balance ON accounts
FOR EACH ROW
DECLARE
    v_change_type VARCHAR2(20);
BEGIN
    -- Determine if the transaction was a deposit or withdrawal
    IF :NEW.balance > :OLD.balance THEN
        v_change_type := 'CREDIT';
    ELSE
        v_change_type := 'DEBIT';
    END IF;

    -- Insert the audit trail
    INSERT INTO audit_log (
        acc_id, 
        old_bal, 
        new_bal, 
        change_amount, 
        tx_type
    )
    VALUES (
        :OLD.acc_id, 
        :OLD.balance, 
        :NEW.balance, 
        ABS(:NEW.balance - :OLD.balance), 
        v_change_type
    );
END;
/
```


### **Step 3: Implementation & Testing (DML)**
*Follow these steps to verify that your schema and triggers are functioning correctly.*

Initialize Account:

```SQL
INSERT INTO accounts (acc_id, holder_name, balance) 
VALUES (101, 'John Smith', 5000.00);
Perform a Transaction:
```
```SQL
-- John withdraws $1,200
UPDATE accounts 
SET balance = balance - 1200 
WHERE acc_id = 101;
Verify Audit Trail:
```
```SQL
-- The trigger should have automatically populated this table
SELECT * FROM audit_log;
Step 4: Performance Optimization (Indexing)
For a high-traffic banking system, searching the audit log by Account ID must be instantaneous.
```
```SQL
-- Create a B-Tree index to optimize queries on the audit trail
CREATE INDEX idx_audit_acc_id ON audit_log(acc_id);
🛠️ Troubleshooting Checklist
If your logic fails, check the following common issues:
```
*ORA-04098: Trigger is invalid. Fix: Check for syntax errors using SHOW ERRORS TRIGGER trg_audit_balance.*

*ORA-00001: Unique constraint violated. Fix: Ensure you aren't inserting an acc_id that already exists.*

*Mutating Table Error: Occurs if the trigger tries to select from the table it is currently modifying. Fix: Use row-level triggers correctly or use a statement-level trigger with a temporary table.*

**🏁 Conclusion & Mastery**
*By completing this setup, you have successfully implemented:*

*Relational Design (Tables & Constraints)*

*Automated Business Logic (Triggers)*

*Data Integrity (Audit Logs)*

*Search Optimization (Indexing)*



# 📂 SQL & PL/SQL Masterclass: 500+ Scenario-Based Learning

This is a comprehensive, production-grade guide to Database Development. It covers **Structured Query Language (SQL)** for data management and **Procedural Language (PL/SQL)** for complex business logic, structured through real-world scenarios.

---

## 🏛 1. Core SQL Foundations (DDL, DML, DQL)
SQL is the language used to define and manipulate data in Relational Database Management Systems (RDBMS).

### **Knowledge Check**
* **Question:** Why use `TRUNCATE` instead of `DELETE` for large log tables?
* **Answer:** `TRUNCATE` is a DDL command that deallocates pages. It is faster, uses fewer undo logs, and resets identity columns, whereas `DELETE` scans every row.

### **Scenarios (1-50): Schema Design & Constraints**
* **Scenario 1:** Ensuring data integrity for a Banking app.
    * *Task:* Create a table where the account balance can never drop below $0.
    ```sql
    CREATE TABLE Accounts (
        AccID INT PRIMARY KEY,
        Balance NUMBER(12,2) CONSTRAINT min_bal CHECK (Balance >= 0)
    );
    ```
* **Scenario 5:** Handling unique email registration.
    * *Task:* Prevent duplicate emails but allow users to register without a phone number.
    ```sql
    CREATE TABLE Users (
        UserID INT PRIMARY KEY,
        Email VARCHAR(255) UNIQUE NOT NULL,
        Phone VARCHAR(20) NULL
    );
    ```

---

## 🖼 2. Database Views (The Virtual Layer)
A View is a virtual table based on the result-set of an SQL statement.



### **Knowledge Check**
* **Question:** What is a "Materialized View"?
* **Answer:** Unlike a standard view, a Materialized View physically stores the query result. It is used for performance tuning in data warehousing to avoid recalculating complex joins.

### **Scenarios (101-150): Data Masking & Abstraction**
* **Scenario 101:** Payroll Security.
    * *Task:* Let HR view employee names and hire dates, but hide the `Base_Salary` and `Social_Security_No`.
    ```sql
    CREATE VIEW hr_employee_basic AS
    SELECT EmpID, FirstName, LastName, HireDate FROM Employees;
    ```
* **Scenario 110:** Complex Reporting.
    * *Task:* Create a view that joins `Orders`, `Customers`, and `Shippers` to show "Active Shipments".
    ```sql
    CREATE VIEW active_shipments AS
    SELECT o.OrderID, c.CustomerName, s.ShipperName
    FROM Orders o
    JOIN Customers c ON o.CustomerID = c.CustomerID
    JOIN Shippers s ON o.ShipperID = s.ShipperID
    WHERE o.Status = 'In-Transit';
    ```

---

## ⚙️ 3. PL/SQL: Procedural Logic
PL/SQL allows you to write programs (loops, variables, conditions) that live inside the database.



### **Knowledge Check**
* **Question:** What is the difference between a Function and a Procedure?
* **Answer:** A Function **must** return a value and can be used in a `SELECT` statement. A Procedure is used to perform a set of actions and cannot be called directly in a query.

### **Scenarios (151-300): Logic & Exception Handling**
* **Scenario 155:** Automatic Tax Calculation.
    * *Task:* Calculate a 15% tax for all invoices in a specific region.
    ```sql
    DECLARE
       v_tax_rate CONSTANT NUMBER := 0.15;
    BEGIN
       FOR rec IN (SELECT inv_id, amount FROM invoices WHERE region = 'TX') LOOP
          UPDATE invoices SET tax_amount = rec.amount * v_tax_rate 
          WHERE inv_id = rec.inv_id;
       END LOOP;
       COMMIT;
    EXCEPTION
       WHEN OTHERS THEN
          ROLLBACK;
          DBMS_OUTPUT.PUT_LINE('Error in Tax Calculation');
    END;
    ```

---

## ⚡ 4. Triggers (Automated Events)
Triggers are stored programs that are automatically executed or "fired" when certain events occur.



### **Scenarios (301-400): Auditing & Validation**
* **Scenario 301:** Inventory Audit.
    * *Task:* Track who changed a product price and when.
    ```sql
    CREATE OR REPLACE TRIGGER trg_price_audit
    AFTER UPDATE OF Price ON Products
    FOR EACH ROW
    BEGIN
       INSERT INTO Price_History (ProdID, OldPrice, NewPrice, ChangeDate, UserID)
       VALUES (:OLD.ProdID, :OLD.Price, :NEW.Price, SYSDATE, USER);
    END;
    ```
* **Scenario 315:** Hard-Stop Validation.
    * *Task:* Prevent any employee from being deleted if they are assigned to an active project.
    ```sql
    CREATE OR REPLACE TRIGGER trg_prevent_del_emp
    BEFORE DELETE ON Employees
    FOR EACH ROW
    DECLARE
       v_count INT;
    BEGIN
       SELECT COUNT(*) INTO v_count FROM Projects WHERE LeadID = :OLD.EmpID;
       IF v_count > 0 THEN
          RAISE_APPLICATION_ERROR(-20001, 'Cannot delete: Employee is leading a project.');
       END IF;
    END;
    ```

---

## 🗺 5. The 500-Scenario Roadmap (Categorized)

To complete the full 500+ scenarios, implement the following logic patterns across these domains:

### **A. Banking & Finance (1-100)**
1.  Verify sufficient funds before withdrawal. (Trigger)
2.  Calculate monthly interest on savings. (Procedure)
3.  Flag transactions over $10,000 for compliance. (Trigger)
4.  Generate "Last 10 Transactions" report. (View)
... (96 more)

### **B. E-Commerce & Retail (101-200)**
1.  Auto-reduce stock on order placement. (Trigger)
2.  Calculate "Dynamic Discount" based on user loyalty. (Function)
3.  Show "Frequently Bought Together" items. (SQL/Subquery)
4.  Handle returns and restock inventory. (Procedure)
... (96 more)

### **C. Healthcare & Insurance (201-300)**
1.  Prevent double-booking a doctor at the same time. (Trigger)
2.  Mask patient health IDs for billing staff. (View)
3.  Auto-expire prescriptions after 6 months. (Scheduled Job/PLSQL)
... (97 more)

### **D. Logistics & Supply Chain (301-400)**
1.  Calculate ETA excluding public holidays. (PL/SQL Logic)
2.  Update truck availability when delivery is signed. (Trigger)
3.  Identify "Bottleneck Warehouses" with >90% capacity. (View)
... (97 more)

### **E. Advanced Optimization & Security (401-500+)**
1.  Implement Row-Level Security (RLS) policies.
2.  Write a script to find and kill "Long Running Queries".
3.  Rebuild fragmented indexes.
4.  Use Bulk Collect and ForAll for million-row updates. (Performance)

---

## ❓ FAQ Quick Summary Table

| Feature | Language | When to Use? | Returns Value? |
| :--- | :--- | :--- | :--- |
| **SELECT** | SQL | Fetching data | Yes (Result set) |
| **VIEW** | SQL | Simplifying/Securing Queries | Yes (Virtual Table) |
| **PROCEDURE** | PL/SQL | Performing complex DB actions | Optional (OUT params) |
| **FUNCTION** | PL/SQL | Calculating a value | Yes (Required) |
| **TRIGGER** | PL/SQL | Automated auditing/validation | No |

---
## 🏦 Level 2: Banking & Financial Logic (Scenarios 51-150)

This section focuses on data integrity, ACID properties (Atomicity, Consistency, Isolation, Durability), and security auditing.

### 1. Transactional Integrity (SQL & PL/SQL)
**Concept:** Ensuring money is never "lost" during transfers using `COMMIT` and `ROLLBACK`.

* **Scenario 51: The Secure Funds Transfer**
    * **Requirement:** Transfer $500 from Account A to Account B. If Account A has insufficient funds, the whole process must fail.
    * **Code:**
    ```sql
    DECLARE
        v_sender_bal NUMBER;
        v_transfer_amt NUMBER := 500;
    BEGIN
        SELECT balance INTO v_sender_bal FROM accounts WHERE acc_id = 'A' FOR UPDATE;
        
        IF v_sender_bal >= v_transfer_amt THEN
            UPDATE accounts SET balance = balance - v_transfer_amt WHERE acc_id = 'A';
            UPDATE accounts SET balance = balance + v_transfer_amt WHERE acc_id = 'B';
            COMMIT;
        ELSE
            RAISE_APPLICATION_ERROR(-20001, 'Insufficient Funds');
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END;
    ```



### 2. Banking Compliance & Auditing (Triggers)
**Concept:** Automatically tracking sensitive changes for regulatory requirements.

* **Scenario 65: Tracking High-Value Transactions**
    * **Requirement:** Any transaction over $10,000 must be logged into a `high_value_alerts` table for AML (Anti-Money Laundering) review.
    * **Code:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_aml_alert
    AFTER INSERT ON transactions
    FOR EACH ROW
    WHEN (new.amount > 10000)
    BEGIN
        INSERT INTO high_value_alerts (tx_id, acc_id, amount, alert_date)
        VALUES (:NEW.tx_id, :NEW.acc_id, :NEW.amount, SYSDATE);
    END;
    ```

* **Scenario 72: Preventing Back-Dated Transactions**
    * **Requirement:** Users should not be able to insert a transaction with a date in the past.
    * **Code:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_date_check
    BEFORE INSERT ON transactions
    FOR EACH ROW
    BEGIN
        IF :NEW.tx_date < TRUNC(SYSDATE) THEN
            RAISE_APPLICATION_ERROR(-20002, 'Cannot record past transactions.');
        END IF;
    END;
    ```

### 3. Financial Reporting (Advanced Views)
**Concept:** Aggregating complex data for executive dashboards.

* **Scenario 85: Daily Branch Liquidity View**
    * **Requirement:** Show each branch's total cash on hand by summing all linked accounts.
    * **Code:**
    ```sql
    CREATE VIEW branch_liquidity AS
    SELECT b.branch_name, SUM(a.balance) as total_cash
    FROM branches b
    JOIN accounts a ON b.branch_id = a.branch_id
    GROUP BY b.branch_name;
    ```

---

### 📋 Scenarios 90-150: The "Quick-Fire" Banking List
* **90:** **Scenario:** Update account status to 'Dormant' if no activity for 365 days. (Procedure)
* **91:** **Scenario:** Prevent deletion of a customer record if they have an active loan. (Trigger)
* **92:** **Scenario:** View to show the "Top 1% Wealthiest Customers" for the Premium Desk. (SQL Window Function)
* **93:** **Scenario:** Calculate Loan EMI based on principal, rate, and tenure. (PL/SQL Function)
* **94:** **Scenario:** Trigger to prevent updates to the `transaction_id` column (Immutable records).
* **95:** **Scenario:** View showing "Account Balance vs. Average Branch Balance" for anomaly detection.
* **96-150:** *Variations including Foreign Exchange (FX) rate conversion, Joint Account ownership logic, and ATM withdrawal limit enforcement.*



---

### 🚀 Progress Tracking
- [x] Level 1: Foundations (1-50)
- [x] Level 2: Banking (51-150)
- [ ] Level 3: E-Commerce & Inventory (151-250)
- [ ] Level 4: Healthcare & Privacy (251-350)
- [ ] Level 5: Performance Tuning (351-500+)

## 🛒 Level 3: E-Commerce & Retail Operations (Scenarios 151-250)

This section focuses on inventory flow, dynamic pricing logic, and customer lifecycle management.

### 1. Inventory & Stock Control (Triggers)
**Concept:** Automating the supply chain to prevent "Out of Stock" scenarios.

* **Scenario 151: Automatic Stock Depletion**
    * **Requirement:** When a customer completes a purchase, subtract the ordered quantity from the `Products` table automatically.
    * **Code:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_update_inventory
    AFTER INSERT ON order_items
    FOR EACH ROW
    BEGIN
        UPDATE products
        SET stock_quantity = stock_quantity - :NEW.quantity
        WHERE product_id = :NEW.product_id;
    END;
    ```

* **Scenario 165: Low Stock Alert System**
    * **Requirement:** If stock falls below a threshold (e.g., 10 units), insert a record into a `reorder_alerts` table.
    * **Code:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_low_stock_warning
    AFTER UPDATE OF stock_quantity ON products
    FOR EACH ROW
    WHEN (NEW.stock_quantity < 10)
    BEGIN
        INSERT INTO reorder_alerts (product_id, current_stock, alert_date)
        VALUES (:NEW.product_id, :NEW.stock_quantity, SYSDATE);
    END;
    ```



### 2. Dynamic Pricing & Discounts (PL/SQL)
**Concept:** Using procedural logic to apply complex business rules that simple SQL cannot handle.

* **Scenario 180: Tiered Loyalty Discount**
    * **Requirement:** Calculate a discount based on customer spend: 5% for >$500, 10% for >$1000, and 15% for >$2000.
    * **Code:**
    ```sql
    CREATE OR REPLACE FUNCTION fn_get_discount(p_customer_id IN INT) 
    RETURN NUMBER IS
        v_total_spend NUMBER;
        v_discount_pct NUMBER := 0;
    BEGIN
        SELECT SUM(total_amount) INTO v_total_spend FROM orders WHERE customer_id = p_customer_id;
        
        IF v_total_spend > 2000 THEN v_discount_pct := 0.15;
        ELSIF v_total_spend > 1000 THEN v_discount_pct := 0.10;
        ELSIF v_total_spend > 500 THEN v_discount_pct := 0.05;
        END IF;
        
        RETURN v_discount_pct;
    END;
    ```

### 3. Sales Analytics (Views)
**Concept:** Simplifying high-level reporting for business users.

* **Scenario 210: Abandoned Cart View**
    * **Requirement:** List all customers who added items to their cart more than 24 hours ago but haven't checked out.
    * **Code:**
    ```sql
    CREATE VIEW abandoned_carts AS
    SELECT c.customer_name, c.email, i.product_name, i.added_at
    FROM customers c
    JOIN cart_items i ON c.customer_id = i.customer_id
    WHERE i.status = 'In-Cart' 
    AND i.added_at < SYSDATE - 1;
    ```



---

### 📋 Scenarios 211-250: Retail Quick-Fire List
* **211:** **Scenario:** Prevent a product from being deleted if it was sold in the last 2 years. (Trigger)
* **212:** **Scenario:** Generate a unique Coupon Code using a PL/SQL Sequence and Prefix.
* **213:** **Scenario:** Update "Last Login" timestamp every time a user fetches their profile. (Trigger)
* **214:** **Scenario:** Create a view showing "Global Best Sellers" by region and category.
* **215:** **Scenario:** Bulk update prices for a specific brand by 10% during a "Flash Sale." (Procedure)
* **216:** **Scenario:** Handle "Soft Delete" by moving deleted products to an `archive_products` table.
* **217-250:** *Variations on tax calculation per state, shipping fee estimation logic, and multi-currency support.*

---

### 🚀 Progress Tracking
- [x] Level 1: Foundations (1-50)
- [x] Level 2: Banking (51-150)
- [x] Level 3: E-Commerce (151-250)
- [ ] Level 4: Healthcare & Privacy (251-350)
- [ ] Level 5: Performance Tuning (351-500+)

## 🏥 Level 4: Healthcare, Privacy & Compliance (Scenarios 251-350)

This section focuses on data privacy (HIPAA compliance), scheduling constraints, and complex patient record management.

### 1. Data Privacy & Masking (Views)
**Concept:** Ensuring sensitive patient data is only visible to authorized medical personnel while allowing billing and admin staff to do their jobs.

* **Scenario 251: PII (Personally Identifiable Information) Masking**
    * **Requirement:** Billing clerks need to see patient names for invoicing but must not see the full Social Security Number or specific Medical Diagnoses.
    * **Code:**
    ```sql
    CREATE VIEW billing_patient_view AS
    SELECT 
        patient_id, 
        first_name, 
        last_name, 
        'XXX-XX-' || SUBSTR(ssn, -4) AS masked_ssn,
        provider_id
    FROM patients;
    ```



### 2. Scheduling Integrity (Triggers)
**Concept:** Using the database to enforce physical reality (e.g., a doctor cannot be in two rooms at once).

* **Scenario 275: Preventing Appointment Overlaps**
    * **Requirement:** Block any new appointment if the doctor already has a booking that overlaps with the requested time slot.
    * **Code:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_no_double_booking
    BEFORE INSERT ON appointments
    FOR EACH ROW
    DECLARE
        v_overlap_count INT;
    BEGIN
        SELECT COUNT(*) INTO v_overlap_count 
        FROM appointments
        WHERE doctor_id = :NEW.doctor_id
        AND appt_date = :NEW.appt_date
        AND (:NEW.start_time < end_time AND :NEW.end_time > start_time);

        IF v_overlap_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20010, 'Doctor is already booked for this time slot.');
        END IF;
    END;
    ```



### 3. Patient History & Auditing (PL/SQL)
**Concept:** Maintaining a "Single Source of Truth" for medical history and changes.

* **Scenario 300: Patient Vitals Trend Analysis**
    * **Requirement:** Create a procedure that identifies if a patient's blood pressure has increased by more than 20% across the last three visits.
    * **Code:**
    ```sql
    CREATE OR REPLACE PROCEDURE check_bp_spike(p_patient_id IN INT) IS
        v_avg_old_bp NUMBER;
        v_latest_bp NUMBER;
    BEGIN
        -- Get average of previous visits
        SELECT AVG(systolic_bp) INTO v_avg_old_bp 
        FROM (SELECT systolic_bp FROM vitals WHERE patient_id = p_patient_id ORDER BY check_date DESC OFFSET 1 ROWS FETCH NEXT 3 ROWS ONLY);
        
        -- Get latest visit
        SELECT systolic_bp INTO v_latest_bp 
        FROM vitals WHERE patient_id = p_patient_id ORDER BY check_date DESC FETCH FIRST 1 ROWS ONLY;

        IF v_latest_bp > v_avg_old_bp * 1.20 THEN
            INSERT INTO health_alerts (patient_id, alert_msg) VALUES (p_patient_id, 'Significant BP Spike Detected');
        END IF;
    END;
    ```

---

### 📋 Scenarios 301-350: Healthcare Quick-Fire List
* **301:** **Scenario:** Automatically release a hospital room (update status to 'Available') when a patient is marked as 'Discharged'. (Trigger)
* **302:** **Scenario:** View to show patients who are overdue for their vaccination based on their age and last dose date.
* **303:** **Scenario:** Function to calculate "Body Mass Index" (BMI) based on weight and height columns.
* **304:** **Scenario:** Prevent a Nurse from prescribing medication (Only Doctors can insert into `prescriptions`). (Trigger)
* **305:** **Scenario:** Audit log to track every time a user views a "High-Profile" celebrity patient record.
* **306-350:** *Variations including insurance claim status automation, laboratory result threshold alerts, and organ donor matching logic.*

---

### 🚀 Progress Tracking
- [x] Level 1: Foundations (1-50)
- [x] Level 2: Banking (51-150)
- [x] Level 3: E-Commerce (151-250)
- [x] Level 4: Healthcare & Privacy (251-350)
- [ ] Level 5: Performance Tuning & Advanced Optimization (351-500+)

## 🚀 Level 5: Performance Tuning & Advanced Optimization (Scenarios 351-500+)

This final section covers high-performance PL/SQL, security administration, and database maintenance logic.

### 1. High-Performance Bulk Operations (PL/SQL)
**Concept:** Standard loops context-switch between the SQL engine and PL/SQL engine for every row, which is slow. `BULK COLLECT` and `FORALL` process data in batches to reduce overhead.

* **Scenario 351: Processing Million-Row Payroll**
    * **Description:** The company needs to process monthly salaries for 100,000 employees. A standard loop takes minutes; Bulk processing takes seconds.
    * **Code:**
    ```sql
    DECLARE
        TYPE EmpTab IS TABLE OF employees%ROWTYPE;
        v_emps EmpTab;
    BEGIN
        -- Fetching 1000 rows at a time into memory
        SELECT * BULK COLLECT INTO v_emps FROM employees;

        -- Batch update back to the SQL engine
        FORALL i IN 1..v_emps.COUNT
            UPDATE employees SET salary = salary * 1.03 
            WHERE employee_id = v_emps(i).employee_id;
        COMMIT;
    END;
    ```
    * **Explanation:** `BULK COLLECT` pulls data into a collection (memory), and `FORALL` sends the entire collection back to the database in one "trip," drastically reducing CPU usage.



### 2. Fine-Grained Access Control (Security)
**Concept:** Standard views can hide columns, but **Row-Level Security (RLS)** or **Virtual Private Database (VPD)** hides specific *rows* based on who is logged in.

* **Scenario 410: Regional Manager Isolation**
    * **Description:** A manager in 'New York' should only see sales data for 'New York', even if they query the global `Sales` table.
    * **Code:**
    ```sql
    -- Create a policy function
    CREATE OR REPLACE FUNCTION secure_by_region(p_schema VARCHAR2, p_obj VARCHAR2)
    RETURN VARCHAR2 IS
    BEGIN
        -- Only returns rows where the region matches the user's assigned region
        RETURN 'region_id = (SELECT region_id FROM staff_assignments WHERE username = USER)';
    END;
    ```
    * **Explanation:** This function is attached to the table. When a user runs `SELECT * FROM sales`, the database silently appends a `WHERE` clause based on the function's return value.

### 3. Database Maintenance & Cleanup (Automation)
**Concept:** Using scheduled jobs and triggers to keep the database healthy.

* **Scenario 450: Automatic Index Rebuild Monitor**
    * **Description:** Over time, indexes become fragmented. We need a way to log which indexes need maintenance.
    * **Code:**
    ```sql
    CREATE OR REPLACE PROCEDURE monitor_index_fragmentation IS
    BEGIN
        FOR idx IN (SELECT index_name FROM user_indexes) LOOP
            -- Analyze index and log if height > 3 (Sign of fragmentation)
            EXECUTE IMMEDIATE 'ANALYZE INDEX ' || idx.index_name || ' VALIDATE STRUCTURE';
            INSERT INTO index_stats_log (idx_name, check_date)
            SELECT name, SYSDATE FROM index_stats WHERE height > 3;
        END LOOP;
    END;
    ```
    * **Explanation:** This procedure uses Dynamic SQL (`EXECUTE IMMEDIATE`) to run maintenance commands that aren't usually allowed in static PL/SQL.



---

### 📋 Scenarios 451-500+: Advanced Master List
* **451:** **Scenario:** Use `MERGE` (Upsert) to sync a local product list with a master supplier list.
* **452:** **Scenario:** Write a Function to convert a Blob (Image) to Base64 for API export.
* **453:** **Scenario:** Trigger to prevent "SQL Injection" by sanitizing input strings (Simple version).
* **454:** **Scenario:** Use `PIVOT` to turn monthly sales rows into columns for a yearly report.
* **455:** **Scenario:** Create a "Flashback" query to recover data deleted 1 hour ago.
* **456:** **Scenario:** Global Temporary Tables (GTT) to store session-specific calculation data.
* **457:** **Scenario:** Implement a `Sequence` that resets to 1 every year for invoice numbering.
* **458-500:** *High-level variations: Partitioning logic for Big Data, JSON data parsing within SQL, and implementing JWT (Token) validation inside the DB layer.*

---

## 🏆 Final Summary Table

| Skill Level | Core Tool | Complexity | Goal |
| :--- | :--- | :--- | :--- |
| **Foundation** | SELECT / DDL | Low | Store and retrieve data correctly. |
| **Intermediate** | Views / Joins | Medium | Abstract data for different users. |
| **Advanced** | Triggers / PLSQL | High | Automate business rules and audits. |
| **Expert** | Bulk / RLS / Tuning | Very High | Scale the database for millions of users. |

---

## 🛠 How to Practice
1. **Replicate:** Copy the table structures provided in each section.
2. **Execute:** Run the scenarios in order.
3. **Debug:** Use `DBMS_OUTPUT.PUT_LINE` to track variable values in PL/SQL.
4. **Extend:** Try combining a **Trigger** with a **Bulk Procedure** to handle mass changes.

**Congratulations! You have completed the 500-Scenario SQL & PL/SQL Master Tutorial.** ```

### Final Next Step

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 1 - 10

This repository contains a step-by-step, scenario-based approach to learning Database Development. Each scenario includes a business problem, the technical solution, and a detailed explanation.

---

## 🏦 Module 1: Banking & Data Integrity

### **Scenario 1: Ensuring Account Uniqueness**
**Business Need:** In a bank, no two customers can share the same Account Number. If duplicates exist, deposits could go to the wrong person.

* **SQL Logic:**
    ```sql
    CREATE TABLE bank_accounts (
        account_id NUMBER PRIMARY KEY,
        account_holder_name VARCHAR2(100) NOT NULL,
        account_number VARCHAR2(20) UNIQUE NOT NULL,
        balance NUMBER(15, 2) DEFAULT 0
    );
    ```
* **Explanation:** * `PRIMARY KEY`: Uniquely identifies each record internally.
    * `UNIQUE`: Specifically ensures the customer-facing `account_number` is never duplicated.
    * `NOT NULL`: Guarantees that essential data (name/number) is never left blank.

---

### **Scenario 2: Preventing Negative Balances**
**Business Need:** A standard savings account should never fall below zero. We must block any transaction that would result in a negative balance.

* **SQL Logic:**
    ```sql
    ALTER TABLE bank_accounts 
    ADD CONSTRAINT chk_positive_balance 
    CHECK (balance >= 0);
    ```
* **Explanation:** The `CHECK` constraint acts as a "Gatekeeper." Whenever an `UPDATE` or `INSERT` is attempted, the database validates the rule. If the balance would become negative, the transaction is automatically rejected.

---

### **Scenario 3: Automatic Audit Logging**
**Business Need:** For security, the bank must track who changed an account balance and when.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_audit_balance
    AFTER UPDATE OF balance ON bank_accounts
    FOR EACH ROW
    BEGIN
        INSERT INTO balance_audit_log (acc_id, old_bal, new_bal, change_date, db_user)
        VALUES (:OLD.account_id, :OLD.balance, :NEW.balance, SYSDATE, USER);
    END;
    ```

* **Explanation:** This trigger "fires" automatically. `:OLD` represents the data before the update, and `:NEW` represents the data after. It captures the `USER` (the person logged in) and the `SYSDATE` (current time).

---

### **Scenario 4: Secure Funds Transfer (ACID Compliance)**
**Business Need:** When moving money from Account A to B, money must not "vanish" if the system crashes mid-way.

* **PL/SQL Block:**
    ```sql
    BEGIN
        UPDATE bank_accounts SET balance = balance - 500 WHERE account_id = 101;
        UPDATE bank_accounts SET balance = balance + 500 WHERE account_id = 102;
        COMMIT; -- Saves both updates permanently
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK; -- Undoes everything if any error occurs
            DBMS_OUTPUT.PUT_LINE('Transfer Failed. Funds Restored.');
    END;
    ```
* **Explanation:** This demonstrates **Atomicity**. Either both updates succeed, or both are undone via `ROLLBACK`.

---

### **Scenario 5: Recent Transactions View**
**Business Need:** The mobile app needs to show "Recent History." We simplify this for developers by creating a virtual table.

* **SQL View:**
    ```sql
    CREATE VIEW view_recent_transactions AS
    SELECT transaction_id, amount, tx_type, tx_date
    FROM transactions
    WHERE tx_date > SYSDATE - 7
    ORDER BY tx_date DESC;
    ```
* **Explanation:** A `VIEW` is a saved query. Instead of the app writing complex filters, it simply runs `SELECT * FROM view_recent_transactions`.

---

### **Scenario 6: Monthly Interest Automation**
**Business Need:** Provide 2% interest to all savings accounts with a balance over $100.

* **PL/SQL Procedure:**
    ```sql
    CREATE OR REPLACE PROCEDURE proc_apply_interest IS
    BEGIN
        UPDATE bank_accounts
        SET balance = balance * 1.02
        WHERE account_type = 'SAVINGS' AND balance > 100;
        COMMIT;
    END;
    ```
* **Explanation:** A **Procedure** is a named block of code that performs an action. It can be scheduled to run automatically every month.

---

### **Scenario 7: Daily Withdrawal Limits**
**Business Need:** Prevent fraud by limiting total daily withdrawals to $2,000.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_limit_withdrawal
    BEFORE INSERT ON transactions
    FOR EACH ROW
    DECLARE
        v_total NUMBER;
    BEGIN
        SELECT SUM(amount) INTO v_total FROM transactions 
        WHERE account_id = :NEW.account_id AND TRUNC(tx_date) = TRUNC(SYSDATE);
        
        IF (v_total + :NEW.amount) > 2000 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Limit Exceeded.');
        END IF;
    END;
    ```
* **Explanation:** Using `RAISE_APPLICATION_ERROR` allows the database to send a custom error message back to the user, stopping the transaction.

---

### **Scenario 8: Identifying Dormant Accounts**
**Business Need:** Find accounts with no activity for 1 year.

* **SQL Logic:**
    ```sql
    SELECT account_id FROM bank_accounts a
    WHERE NOT EXISTS (
        SELECT 1 FROM transactions t 
        WHERE t.account_id = a.account_id AND t.tx_date > SYSDATE - 365
    );
    ```
* **Explanation:** `NOT EXISTS` is an efficient way to check for the absence of records in a related table.

---

### **Scenario 9: Currency Conversion Function**
**Business Need:** Show a USD balance in EUR for international customers.

* **PL/SQL Function:**
    ```sql
    CREATE OR REPLACE FUNCTION fn_to_eur(p_usd NUMBER) RETURN NUMBER IS
    BEGIN
        RETURN p_usd * 0.92;
    END;
    ```
* **Explanation:** A **Function** is used to calculate and return a value. It can be used directly inside a `SELECT` statement.

---

### **Scenario 10: Loan Eligibility Filter**
**Business Need:** Target customers with >$5,000 balance who have been members for 2+ years.

* **SQL Logic:**
    ```sql
    SELECT account_holder_name FROM bank_accounts
    WHERE balance > 5000 AND opening_date < ADD_MONTHS(SYSDATE, -24);
    ```
* **Explanation:** `ADD_MONTHS` is used for accurate date arithmetic, ensuring the "2-year" rule is met precisely.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [ ] Scenarios 11 - 20: Advanced Auditing & Error Handling (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 11 - 20

This module covers advanced operational logic, including how to handle database errors gracefully and how to manage complex user requirements.

---

## 🛠️ Module 2: Advanced Operations & Error Handling

### **Scenario 11: Preventing "Mutating Table" Errors**
**Business Need:** When a new employee is added, the system should automatically check if the department's total salary exceeds the budget.
**Technical Challenge:** A row-level trigger cannot query the same table it is currently modifying (this causes a "Mutating Table" error).

* **PL/SQL Strategy:** Use a Compound Trigger to manage data across different timing points.
* **Code:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_check_dept_budget
    FOR INSERT ON employees
    COMPOUND TRIGGER
        v_max_budget CONSTANT NUMBER := 500000;
        v_current_total NUMBER;
    AFTER STATEMENT IS
    BEGIN
        SELECT SUM(salary) INTO v_current_total FROM employees;
        IF v_current_total > v_max_budget THEN
            RAISE_APPLICATION_ERROR(-20002, 'Department budget exceeded!');
        END IF;
    END AFTER STATEMENT;
    ```
* **Explanation:** By using an `AFTER STATEMENT` section in a Compound Trigger, we wait until the rows are inserted before checking the sum, avoiding the "Mutating" conflict.

---

### **Scenario 12: Custom Exception Handling**
**Business Need:** If a bank transfer is attempted on a "Frozen" account, the system should provide a specific user-friendly error message rather than a generic system error.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        e_account_frozen EXCEPTION;
        v_status VARCHAR2(20);
    BEGIN
        SELECT status INTO v_status FROM accounts WHERE acc_id = 101;
        IF v_status = 'FROZEN' THEN
            RAISE e_account_frozen;
        END IF;
    EXCEPTION
        WHEN e_account_frozen THEN
            DBMS_OUTPUT.PUT_LINE('Error: This account is locked for security reasons.');
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('Error: Account ID does not exist.');
    END;
    ```
* **Explanation:** `DECLARE` a named exception, `RAISE` it when a business rule is broken, and handle it in the `EXCEPTION` block to prevent the code from crashing.

---

### **Scenario 13: Logging Failed Login Attempts**
**Business Need:** Security teams need to see a list of users who failed to log in more than 3 times in an hour.

* **SQL Logic:**
    ```sql
    CREATE VIEW view_security_alerts AS
    SELECT username, COUNT(*) as failed_attempts
    FROM login_logs
    WHERE status = 'FAILED' 
    AND attempt_time > SYSDATE - 1/24
    GROUP BY username
    HAVING COUNT(*) > 3;
    ```
* **Explanation:** `SYSDATE - 1/24` represents the last 1 hour. We use `GROUP BY` and `HAVING` to isolate only those users who meet the specific threat criteria.

---

### **Scenario 14: Dynamic Table Cleanup**
**Business Need:** To save space, the DBA wants to drop old "Temporary Log" tables whose names follow a pattern like `TEMP_LOG_2024`.

* **PL/SQL (Dynamic SQL):**
    ```sql
    BEGIN
        FOR r IN (SELECT table_name FROM user_tables WHERE table_name LIKE 'TEMP_LOG_%') LOOP
            EXECUTE IMMEDIATE 'DROP TABLE ' || r.table_name;
        END LOOP;
    END;
    ```
* **Explanation:** `EXECUTE IMMEDIATE` allows PL/SQL to run DDL commands (like DROP or CREATE) that aren't normally allowed in static blocks.

---

### **Scenario 15: Tracking Metadata Changes**
**Business Need:** The Lead Developer wants to know whenever any table structure (columns, types) is changed in the schema.

* **System Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_log_ddl
    AFTER DDL ON SCHEMA
    BEGIN
        INSERT INTO ddl_audit (event_type, object_name, event_date, db_user)
        VALUES (ora_sysevent, ora_dict_obj_name, SYSDATE, USER);
    END;
    ```
* **Explanation:** This is a **DDL Trigger**. It doesn't monitor data changes, but rather structural changes to the database itself.

---

### **Scenario 16: Handling NULLs in Financial Math**
**Business Need:** Calculate total compensation (`Salary + Commission`). If commission is NULL, the result shouldn't be NULL (which is the default SQL behavior).

* **SQL Logic:**
    ```sql
    SELECT emp_name, salary + NVL(commission, 0) as total_pay
    FROM employees;
    ```
* **Explanation:** `NVL(column, replacement)` ensures that if a value is missing, it is treated as 0 so the addition doesn't fail.

---

### **Scenario 17: Row-Level Security (VPD Basics)**
**Business Need:** A Sales Rep should only be able to see their own customers, not the customers of other reps.

* **SQL Logic (Predicate Injection):**
    ```sql
    CREATE VIEW view_my_customers AS
    SELECT * FROM customers
    WHERE sales_rep_id = (SELECT rep_id FROM staff WHERE username = USER);
    ```
* **Explanation:** By using the `USER` pseudo-column, the view automatically filters the data based on who is currently logged in.

---

### **Scenario 18: Bulk Deletion for Performance**
**Business Need:** Delete 1 million "expired" session records without locking the database or filling up the Undo logs.

* **PL/SQL Logic:**
    ```sql
    BEGIN
        LOOP
            DELETE FROM sessions WHERE expiry_date < SYSDATE - 30
            AND ROWNUM <= 5000; -- Small batches
            
            EXIT WHEN SQL%NOTFOUND;
            COMMIT; -- Clear logs after every batch
        END LOOP;
    END;
    ```
* **Explanation:** Deleting in small batches prevents the database from locking up for long periods and manages system resources efficiently.

---

### **Scenario 19: Preventing Weekend Updates**
**Business Need:** To ensure data stability, critical pricing tables should not be updated on Saturdays or Sundays.

* **Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_no_weekend_updates
    BEFORE UPDATE ON product_prices
    BEGIN
        IF TO_CHAR(SYSDATE, 'DY') IN ('SAT', 'SUN') THEN
            RAISE_APPLICATION_ERROR(-20101, 'Maintenance window: Pricing updates disabled on weekends.');
        END IF;
    END;
    ```
* **Explanation:** `TO_CHAR(SYSDATE, 'DY')` extracts the day name. This is a common way to enforce "Business Hours" logic at the database level.

---

### **Scenario 20: Finding "Orphan" Records**
**Business Need:** Find all "Order" records that don't have a corresponding "Customer" (perhaps due to a legacy data import error).

* **SQL Logic:**
    ```sql
    SELECT o.order_id 
    FROM orders o
    LEFT JOIN customers c ON o.customer_id = c.customer_id
    WHERE c.customer_id IS NULL;
    ```

* **Explanation:** A `LEFT JOIN` combined with a `WHERE ... IS NULL` check is the standard way to find records in Table A that are missing their parent in Table B.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [ ] Scenarios 21 - 30: Cursors & Collections (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 21 - 30

This module focuses on data iteration and memory-based data structures, which are essential for complex reporting and batch processing.

---

## 🌀 Module 3: Cursors & Collections

### **Scenario 21: Processing Records with an Explicit Cursor**
**Business Need:** The HR department needs a report that lists each employee's name and evaluates their salary against the company average, printed to the console.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        CURSOR c_emp IS 
            SELECT first_name, salary FROM employees;
        v_avg_sal NUMBER;
    BEGIN
        SELECT AVG(salary) INTO v_avg_sal FROM employees;
        
        FOR r IN c_emp LOOP
            IF r.salary > v_avg_sal THEN
                DBMS_OUTPUT.PUT_LINE(r.first_name || ': Above Average');
            ELSE
                DBMS_OUTPUT.PUT_LINE(r.first_name || ': Below Average');
            END IF;
        END LOOP;
    END;
    ```
* **Explanation:** An **Explicit Cursor** (`c_emp`) allows us to fetch multiple rows and process them one by one. Using a `FOR loop` with a cursor is efficient because the database automatically handles opening, fetching, and closing the cursor.

---

### **Scenario 22: Using a Parameterized Cursor**
**Business Need:** Create a reusable logic block that fetches all employees belonging to a specific department ID provided at runtime.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        CURSOR c_dept_emp (p_dept_id NUMBER) IS
            SELECT first_name FROM employees WHERE department_id = p_dept_id;
    BEGIN
        -- Fetching for Department 50
        FOR r IN c_dept_emp(50) LOOP
            DBMS_OUTPUT.PUT_LINE('Name: ' || r.first_name);
        END LOOP;
    END;
    ```
* **Explanation:** **Parameterized Cursors** make your code modular. You can use the same cursor logic for different inputs without rewriting the SQL query.

---

### **Scenario 23: Handling "FOR UPDATE" Cursors (Row Locking)**
**Business Need:** During a sensitive salary review, we must lock the rows we are currently processing to prevent other users from changing them until we are finished.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        CURSOR c_review IS 
            SELECT salary FROM employees WHERE department_id = 10 FOR UPDATE;
    BEGIN
        FOR r IN c_review LOOP
            UPDATE employees SET salary = salary * 1.05 
            WHERE CURRENT OF c_review;
        END LOOP;
        COMMIT;
    END;
    ```

* **Explanation:** `FOR UPDATE` tells the database to place a lock on the selected rows. `WHERE CURRENT OF` is a shortcut that updates the specific row the cursor is currently pointing to.

---

### **Scenario 24: Bulk Collect for Faster Processing**
**Business Need:** You need to fetch 10,000 product IDs into memory to perform complex calculations. Fetching one-by-one with a standard cursor is too slow.

* **PL/SQL (Collections):**
    ```sql
    DECLARE
        TYPE t_id_list IS TABLE OF NUMBER;
        v_ids t_id_list;
    BEGIN
        SELECT product_id BULK COLLECT INTO v_ids FROM products;
        
        DBMS_OUTPUT.PUT_LINE('Total products loaded: ' || v_ids.COUNT);
    END;
    ```
* **Explanation:** `BULK COLLECT` fetches the entire result set into a **Collection** (an in-memory array) in a single operation. This reduces "context switching" between the SQL and PL/SQL engines, significantly improving speed.

---

### **Scenario 25: Using Associative Arrays (Key-Value Pairs)**
**Business Need:** Store a temporary mapping of Department IDs to Department Names in memory for quick lookup during a long-running process.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        TYPE t_dept_map IS TABLE OF VARCHAR2(100) INDEX BY PLS_INTEGER;
        v_depts t_dept_map;
    BEGIN
        v_depts(10) := 'Administration';
        v_depts(20) := 'Marketing';
        
        DBMS_OUTPUT.PUT_LINE('Dept 10 is: ' || v_depts(10));
    END;
    ```
* **Explanation:** **Associative Arrays** work like a "Hash Map" or "Dictionary." You use a key (like an ID) to instantly retrieve a value without querying the database again.

---

### **Scenario 26: The "Cursor Variable" (REF CURSOR)**
**Business Need:** A central procedure needs to open a cursor and pass it to a different sub-program or a Java/Python application for processing.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PROCEDURE get_emp_ref_cursor (p_rc OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_rc FOR SELECT first_name, salary FROM employees;
    END;
    ```
* **Explanation:** A `SYS_REFCURSOR` is a pointer to a result set. Unlike a static cursor, it can be passed as a parameter between procedures or even to external programming languages.

---

### **Scenario 27: Identifying Duplicate Records**
**Business Need:** A data entry error caused duplicate entries in the `customers` table. Find all `email` addresses that appear more than once.

* **SQL Logic:**
    ```sql
    SELECT email, COUNT(*)
    FROM customers
    GROUP BY email
    HAVING COUNT(*) > 1;
    ```
* **Explanation:** This is a classic use of `GROUP BY` and `HAVING`. It collapses the rows by email and then filters to show only the groups with a count higher than one.

---

### **Scenario 28: Using Varrays (Variable Sized Arrays)**
**Business Need:** Each student can have up to 5 phone numbers. We want to store these as a single unit within the student record.

* **SQL Logic:**
    ```sql
    CREATE OR REPLACE TYPE phone_list AS VARRAY(5) OF VARCHAR2(15);
    /
    CREATE TABLE students (
        student_id NUMBER,
        phones phone_list
    );
    ```
* **Explanation:** A **VARRAY** is a collection type that has a fixed maximum size. It allows you to store a list of values directly inside a single column of a table.

---

### **Scenario 29: Record Types for Clean Code**
**Business Need:** You are writing a procedure that handles many attributes of a "Product." Instead of 10 different variables, you want one structured variable.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        TYPE t_prod_rectype IS RECORD (
            id NUMBER,
            name VARCHAR2(50),
            price NUMBER
        );
        v_item t_prod_rectype;
    BEGIN
        SELECT product_id, product_name, price INTO v_item 
        FROM products WHERE product_id = 500;
        
        DBMS_OUTPUT.PUT_LINE(v_item.name || ' costs ' || v_item.price);
    END;
    ```
* **Explanation:** **RECORD** types allow you to group related variables together, making your code much cleaner and easier to maintain.

---

### **Scenario 30: Updating Records via a Collection (FORALL)**
**Business Need:** You have a list of 5,000 IDs in a collection that need their status updated to 'PROCESSED'.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        TYPE t_id_list IS TABLE OF NUMBER;
        v_ids t_id_list := t_id_list(101, 102, 103, 104);
    BEGIN
        FORALL i IN 1..v_ids.COUNT
            UPDATE orders SET status = 'PROCESSED' 
            WHERE order_id = v_ids(i);
        COMMIT;
    END;
    ```

* **Explanation:** `FORALL` is the partner to `BULK COLLECT`. It sends the entire batch of updates to the SQL engine at once, which is significantly faster than using a standard `FOR` loop for updates.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [ ] Scenarios 31 - 40: Subqueries & Analytical Functions (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 31 - 40

This module focuses on advanced data retrieval techniques. These tools allow you to perform calculations across sets of rows (like rankings and running totals) directly in a single SQL statement.

---

## 📊 Module 4: Subqueries & Analytical Functions

### **Scenario 31: Ranking Employees by Salary (RANK vs DENSE_RANK)**
**Business Need:** HR wants a list of employees ranked by salary within their department. If two people have the same salary, they should have the same rank.

* **SQL Logic:**
    ```sql
    SELECT department_id, first_name, salary,
           RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) as salary_rank,
           DENSE_RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) as dense_salary_rank
    FROM employees;
    ```
* **Explanation:** * `PARTITION BY`: Restarts the ranking for every department.
    * `RANK()`: Leaves a gap in the numbering if there is a tie (e.g., 1, 2, 2, 4).
    * `DENSE_RANK()`: Does NOT leave a gap (e.g., 1, 2, 2, 3).

---

### **Scenario 32: Calculating a Running Total**
**Business Need:** Finance needs to see a daily report of transactions with a "cumulative balance" column that adds up the amounts as the day progresses.

* **SQL Logic:**
    ```sql
    SELECT transaction_id, tx_date, amount,
           SUM(amount) OVER (ORDER BY tx_date) as running_total
    FROM transactions;
    ```

* **Explanation:** By using `SUM(...) OVER (ORDER BY ...)`, SQL maintains a rolling sum. It adds the current row's amount to the sum of all previous rows in the sorted list.

---

### **Scenario 33: Identifying "Above Average" Performers (Correlated Subquery)**
**Business Need:** Find all employees who earn more than the average salary of **their specific department**.

* **SQL Logic:**
    ```sql
    SELECT e.first_name, e.salary, e.department_id
    FROM employees e
    WHERE e.salary > (
        SELECT AVG(salary) 
        FROM employees 
        WHERE department_id = e.department_id
    );
    ```
* **Explanation:** This is a **Correlated Subquery**. The inner query runs once for every row in the outer query, comparing the individual's salary to their own department's average.

---

### **Scenario 34: Finding the "Previous" and "Next" Value (LEAD & LAG)**
**Business Need:** For a stock market app, show today's closing price and the price from the previous day in the same row to calculate the "Change."

* **SQL Logic:**
    ```sql
    SELECT stock_symbol, close_date, close_price,
           LAG(close_price, 1) OVER (PARTITION BY stock_symbol ORDER BY close_date) as prev_day_price
    FROM stock_history;
    ```
* **Explanation:** * `LAG`: Accesses data from a previous row without a join.
    * `LEAD`: Accesses data from a future row.
    * This is much faster than joining a table to itself.

---

### **Scenario 35: Using "Common Table Expressions" (WITH Clause)**
**Business Need:** You have a massive query that calculates "Regional Sales" and you need to use that result multiple times in the same report.

* **SQL Logic:**
    ```sql
    WITH regional_summary AS (
        SELECT region_id, SUM(sale_amount) as total_sales
        FROM sales
        GROUP BY region_id
    )
    SELECT r.region_id, r.total_sales
    FROM regional_summary r
    WHERE r.total_sales > (SELECT AVG(total_sales) FROM regional_summary);
    ```
* **Explanation:** **CTEs (WITH clause)** act like a temporary view that only exists for that query. It makes complex SQL much more readable and sometimes improves performance by preventing redundant calculations.

---

### **Scenario 36: Identifying the "N-th" Highest Value**
**Business Need:** Find the 3rd highest salary in the entire company.

* **SQL Logic:**
    ```sql
    SELECT salary FROM (
        SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) as rnk
        FROM employees
    ) WHERE rnk = 3;
    ```
* **Explanation:** By nesting the `DENSE_RANK` in a subquery, we can filter by the calculated rank in the outer query. This is a very common interview question.

---

### **Scenario 37: Data Paging (Top-N Queries)**
**Business Need:** A website wants to show "Page 1" of products, which is just the first 10 items sorted by price.

* **SQL Logic (Oracle 12c+):**
    ```sql
    SELECT product_name, price
    FROM products
    ORDER BY price ASC
    OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY;
    ```
* **Explanation:** `OFFSET` and `FETCH` are the modern, standard way to handle pagination, replacing the older `ROWNUM` methods which were more complex to write.

---

### **Scenario 38: Handling "Multiple Rows" in a Subquery (IN vs EXISTS)**
**Business Need:** Find all customers who have placed at least one order.

* **SQL Logic:**
    ```sql
    SELECT customer_name 
    FROM customers c
    WHERE EXISTS (
        SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id
    );
    ```

* **Explanation:** `EXISTS` is usually faster than `IN` for large datasets because it stops searching as soon as it finds a single match (Short-circuiting logic).

---

### **Scenario 39: Aggregating into a String (LISTAGG)**
**Business Need:** For a report, show each Department Name and a comma-separated list of all employees in that department.

* **SQL Logic:**
    ```sql
    SELECT department_id, 
           LISTAGG(first_name, ', ') WITHIN GROUP (ORDER BY first_name) as employee_list
    FROM employees
    GROUP BY department_id;
    ```
* **Explanation:** `LISTAGG` takes multiple rows and "squashes" them into a single string. It is perfect for summary reports and dashboards.

---

### **Scenario 40: Pivot (Rows to Columns)**
**Business Need:** You have sales rows for 'Jan', 'Feb', 'Mar'. You want a report where 'Jan', 'Feb', and 'Mar' are the column headers.

* **SQL Logic:**
    ```sql
    SELECT * FROM (
        SELECT region, month, sales_amt FROM sales_data
    )
    PIVOT (
        SUM(sales_amt) FOR month IN ('JAN', 'FEB', 'MAR')
    );
    ```
* **Explanation:** The `PIVOT` operator transforms data from a vertical format (rows) into a horizontal format (columns), making it look like a spreadsheet.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [x] Scenarios 31 - 40: Subqueries & Analytical Functions
- [ ] Scenarios 41 - 50: Advanced PL/SQL Packages & Wrappers (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 41 - 50

This module focuses on professional-grade code organization. In real-world environments, you don't just write scripts; you build **Packages** to make your logic reusable, secure, and easy to maintain.

---

## 📦 Module 5: Packages & Modular Design

### **Scenario 41: Creating a Basic Package (Spec and Body)**
**Business Need:** The Finance team needs a set of tools to calculate taxes and bonuses. Instead of separate functions, they want a single "Finance_Utils" toolkit.

* **PL/SQL Logic (Specification):**
    ```sql
    CREATE OR REPLACE PACKAGE finance_pkg AS
        FUNCTION get_tax(p_amount NUMBER) RETURN NUMBER;
        PROCEDURE update_bonus(p_emp_id NUMBER);
    END finance_pkg;
    ```
* **PL/SQL Logic (Body):**
    ```sql
    CREATE OR REPLACE PACKAGE BODY finance_pkg AS
        FUNCTION get_tax(p_amount NUMBER) RETURN NUMBER IS
        BEGIN
            RETURN p_amount * 0.15;
        END;

        PROCEDURE update_bonus(p_emp_id NUMBER) IS
        BEGIN
            UPDATE employees SET salary = salary + 500 WHERE employee_id = p_emp_id;
        END;
    END finance_pkg;
    ```
* **Explanation:** A **Package** has two parts. The **Specification** is the "Menu" (what is available), and the **Body** is the "Kitchen" (how it works). This hides the complex logic from the user.

---

### **Scenario 42: Overloading Subprograms**
**Business Need:** You need a `find_user` function. Sometimes you want to search by `User_ID` (number), and sometimes by `Email` (string).

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PACKAGE user_pkg AS
        PROCEDURE find_user(p_id NUMBER);
        PROCEDURE find_user(p_email VARCHAR2);
    END user_pkg;
    ```
* **Explanation:** **Overloading** allows you to have multiple procedures with the same name but different parameters. The database automatically chooses the right one based on the data type you provide.

---

### **Scenario 43: Package Initialization (The "One-Time" Block)**
**Business Need:** Every time someone uses the "Global_Settings" package, it should automatically load the current exchange rate from a web service or a slow table.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PACKAGE BODY settings_pkg AS
        v_rate NUMBER;
    BEGIN
        -- This block runs ONLY ONCE when the package is first called in a session
        SELECT rate INTO v_rate FROM exchange_rates WHERE currency = 'EUR';
    END settings_pkg;
    ```
* **Explanation:** The `BEGIN` block at the end of a package body is the **Initialization Block**. It’s perfect for setting up global variables or constants without repeating the work for every call.

---

### **Scenario 44: Deterministic Functions for Performance**
**Business Need:** You have a function that converts "Centimeters to Inches." Since the result for `10cm` will always be the same, you want the database to cache the result.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE FUNCTION cm_to_inch(p_cm NUMBER) 
    RETURN NUMBER DETERMINISTIC IS
    BEGIN
        RETURN p_cm / 2.54;
    END;
    ```
* **Explanation:** The `DETERMINISTIC` keyword tells SQL that for the same input, the output is always the same. This allows the database to skip re-running the calculation if it sees the same input again.

---

### **Scenario 45: Pipelined Table Functions (Streaming Data)**
**Business Need:** You need to generate a series of dates for a calendar, but you want to stream them to the query row-by-row rather than waiting for a massive array to build in memory.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE FUNCTION get_dates(p_start DATE, p_end DATE) 
    RETURN date_table_type PIPELINED IS
    BEGIN
        FOR i IN 0 .. (p_end - p_start) LOOP
            PIPE ROW (p_start + i);
        END LOOP;
        RETURN;
    END;
    ```
* **Explanation:** **Pipelined Functions** return data as it is being processed. In a `SELECT * FROM TABLE(get_dates(...))`, the rows appear immediately, making them highly efficient for huge datasets.

---

### **Scenario 46: Autonomous Transactions**
**Business Need:** You want to log every attempt to update a salary in an `audit_table`, even if the main salary update fails and gets rolled back.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PROCEDURE log_attempt(p_msg VARCHAR2) IS
        PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        INSERT INTO error_logs VALUES (p_msg, SYSDATE);
        COMMIT; -- Only commits this log, not the main transaction
    END;
    ```

* **Explanation:** `PRAGMA AUTONOMOUS_TRANSACTION` creates a "Mini-Transaction" inside the main one. It allows you to `COMMIT` or `ROLLBACK` independently of the main code.

---

### **Scenario 47: Result Caching (Cross-Session)**
**Business Need:** Your company has a `get_holiday_list` function. Since the holidays don't change often, you want the result cached for *all* users, not just the current session.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE FUNCTION get_holidays(p_year NUMBER) 
    RETURN holiday_list_type RESULT_CACHE IS
    BEGIN
        -- Complex query here
    END;
    ```
* **Explanation:** `RESULT_CACHE` stores the output in the SGA (System Global Area). If User A runs it, User B gets the answer instantly from memory.

---

### **Scenario 48: Invoker Rights vs. Definer Rights**
**Business Need:** You created a procedure that clears logs. You want it to run using the permissions of the person *running* it, not the person who *wrote* it.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PROCEDURE clear_my_logs 
    AUTHID CURRENT_USER IS
    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE session_logs';
    END;
    ```
* **Explanation:** * `AUTHID DEFINER` (Default): Runs with the creator's power.
    * `AUTHID CURRENT_USER`: Runs with the executor's power. This is safer for shared utility scripts.

---

### **Scenario 49: Out Parameters for Multiple Returns**
**Business Need:** You need a procedure that takes a Product ID and gives back both the `Price` and the `Stock_Count` at the same time.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PROCEDURE get_prod_info (
        p_id IN NUMBER, 
        p_price OUT NUMBER, 
        p_qty OUT NUMBER
    ) IS
    BEGIN
        SELECT price, stock INTO p_price, p_qty FROM products WHERE id = p_id;
    END;
    ```
* **Explanation:** `OUT` parameters allow a procedure to "return" more than one value. This is useful for returning status codes, IDs, and calculation results simultaneously.

---

### **Scenario 50: Hiding Logic with "Wrap"**
**Business Need:** You are selling your software to a client and don't want them to see your secret PL/SQL formulas in the database.

* **Technical Step:** Use the Oracle `wrap` utility or `DBMS_DDL.WRAP`.
* **Example Command:** `wrap iname=secret_logic.sql`
* **Explanation:** **Wrapping** obfuscates the source code. It still runs perfectly, but if someone tries to `SELECT text FROM all_source`, they will see unreadable gibberish.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [x] Scenarios 31 - 40: Subqueries & Analytical Functions
- [x] Scenarios 41 - 50: Packages & Modular Design
- [ ] Scenarios 51 - 60: E-Commerce Logic (Coming Next...)
# 📂 SQL & PL/SQL Master Tutorial: Scenarios 51 - 60

This module explores the high-concurrency world of E-Commerce. We focus on how to keep inventory accurate, manage coupons, and handle multi-table order processing.

---

## 🛒 Module 6: E-Commerce & Inventory Management

### **Scenario 51: Real-Time Stock Synchronization**
**Business Need:** When a customer places an order, the stock must be reduced immediately. If the stock hits zero, the product must be marked as "Unavailable."

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_sync_inventory
    AFTER INSERT ON order_items
    FOR EACH ROW
    DECLARE
        v_current_stock NUMBER;
    BEGIN
        -- Update the stock
        UPDATE products 
        SET stock_qty = stock_qty - :NEW.quantity
        WHERE product_id = :NEW.product_id
        RETURNING stock_qty INTO v_current_stock;

        -- If stock is now zero, update status
        IF v_current_stock <= 0 THEN
            UPDATE products SET status = 'OUT_OF_STOCK' 
            WHERE product_id = :NEW.product_id;
        END IF;
    END;
    ```

* **Explanation:** This is an **AFTER INSERT** trigger. It ensures that every time a line item is added to an order, the inventory stays accurate. The `RETURNING` clause is a performance trick to get the new value without running a second `SELECT`.

---

### **Scenario 52: Dynamic Coupon Validation**
**Business Need:** A customer enters a coupon code. You must check if the code exists, if it has expired, and if the customer has already used it.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE FUNCTION fn_validate_coupon(
        p_code VARCHAR2, 
        p_cust_id NUMBER
    ) RETURN VARCHAR2 IS
        v_count NUMBER;
        v_expiry DATE;
    BEGIN
        SELECT expiry_date INTO v_expiry FROM coupons WHERE code = p_code;
        
        IF v_expiry < SYSDATE THEN
            RETURN 'EXPIRED';
        END IF;

        SELECT COUNT(*) INTO v_count FROM order_history 
        WHERE customer_id = p_cust_id AND coupon_used = p_code;

        IF v_count > 0 THEN
            RETURN 'ALREADY_USED';
        END IF;

        RETURN 'VALID';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN 'INVALID_CODE';
    END;
    ```
* **Explanation:** This function encapsulates complex validation logic. By returning strings like 'EXPIRED' or 'VALID', the front-end application can easily show the correct message to the user.

---

### **Scenario 53: Calculating "Cart Total" with Tax and Shipping**
**Business Need:** Before checkout, calculate the total price including a 10% tax and a flat $5 shipping fee if the order is under $50.

* **SQL Logic:**
    ```sql
    SELECT cart_id, 
           subtotal,
           (subtotal * 0.10) as tax,
           CASE WHEN subtotal < 50 THEN 5 ELSE 0 END as shipping,
           (subtotal * 1.10) + (CASE WHEN subtotal < 50 THEN 5 ELSE 0 END) as grand_total
    FROM (SELECT cart_id, SUM(price * qty) as subtotal FROM cart_items GROUP BY cart_id);
    ```
* **Explanation:** This uses an **Inline View** (the subquery) to first calculate the subtotal. The outer query then applies conditional logic using `CASE` to determine shipping costs.

---

### **Scenario 54: Preventing "Ghost" Orders (Stock Reservation)**
**Business Need:** Two people are looking at the last item. We need to "lock" the item for 10 minutes once it’s in a cart so nobody else can buy it.

* **SQL Logic:**
    ```sql
    UPDATE products 
    SET reserved_qty = reserved_qty + 1 
    WHERE product_id = 505 
    AND (stock_qty - reserved_qty) > 0;
    ```
* **Explanation:** This logic uses **Optimistic Locking** principles. It only allows the update if there is "available" stock (Total Stock - Reserved). This prevents over-selling products.

---

### **Scenario 55: Finding "Frequently Bought Together"**
**Business Need:** On a product page, show items that other customers often bought in the same transaction as the current product.

* **SQL Logic:**
    ```sql
    SELECT product_id, COUNT(*) as frequency
    FROM order_items
    WHERE order_id IN (SELECT order_id FROM order_items WHERE product_id = :current_prod)
    AND product_id <> :current_prod
    GROUP BY product_id
    ORDER BY frequency DESC
    FETCH FIRST 5 ROWS ONLY;
    ```

* **Explanation:** This is a **Self-Join** concept. We find all orders that contain our product, then find all *other* products in those same orders and count them.

---

### **Scenario 56: Handling Multiple Shipping Addresses**
**Business Need:** A customer wants to set one address as "Default." When they add a new default, the old one must automatically be set to "Secondary."

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_set_default_address
    BEFORE INSERT OR UPDATE ON addresses
    FOR EACH ROW
    WHEN (NEW.is_default = 'Y')
    BEGIN
        UPDATE addresses 
        SET is_default = 'N' 
        WHERE customer_id = :NEW.customer_id 
        AND address_id <> :NEW.address_id;
    END;
    ```
* **Explanation:** This trigger enforces a **Business Constraint**: there can be only one 'Y' for `is_default` per customer. It cleans up the old data before the new data is saved.

---

### **Scenario 57: Expiring Abandoned Carts**
**Business Need:** If a cart has been sitting for 48 hours without a checkout, clear the items and return the "reserved" stock to the main pool.

* **PL/SQL Procedure:**
    ```sql
    CREATE OR REPLACE PROCEDURE proc_cleanup_carts IS
    BEGIN
        FOR r IN (SELECT cart_id FROM carts WHERE status = 'OPEN' AND last_updated < SYSDATE - 2) LOOP
            -- Logic to return reserved stock...
            DELETE FROM cart_items WHERE cart_id = r.cart_id;
            UPDATE carts SET status = 'EXPIRED' WHERE cart_id = r.cart_id;
        END LOOP;
        COMMIT;
    END;
    ```
* **Explanation:** This is a **Batch Cleanup** process. In production, this would be scheduled to run every hour using `DBMS_SCHEDULER`.

---

### **Scenario 58: Tiered Discount Logic (SQL CASE)**
**Business Need:** Apply a 5% discount for 2 items, 10% for 3-5 items, and 20% for more than 5 items of the same type.

* **SQL Logic:**
    ```sql
    SELECT order_id, 
           quantity, 
           price,
           CASE 
             WHEN quantity > 5 THEN price * 0.80
             WHEN quantity BETWEEN 3 AND 5 THEN price * 0.90
             WHEN quantity = 2 THEN price * 0.95
             ELSE price 
           END as discounted_price
    FROM order_items;
    ```
* **Explanation:** The `CASE` statement is the SQL equivalent of `IF-THEN-ELSE`. It allows for complex, multi-level logic within a simple `SELECT` statement.

---

### **Scenario 59: Generating an Order Reference Number**
**Business Need:** Create a human-readable order ID like `ORD-2024-0001`.

* **SQL/PLSQL Logic:**
    ```sql
    CREATE SEQUENCE seq_order_no START WITH 1;

    CREATE OR REPLACE FUNCTION fn_generate_order_id RETURN VARCHAR2 IS
    BEGIN
        RETURN 'ORD-' || TO_CHAR(SYSDATE, 'YYYY') || '-' || 
               LPAD(seq_order_no.NEXTVAL, 4, '0');
    END;
    ```
* **Explanation:** We combine a **Sequence** (for the number) with `TO_CHAR` (for the year) and `LPAD` (to ensure the number is always 4 digits, like 0001).

---

### **Scenario 60: Calculating "Time to Ship" (SLA Tracking)**
**Business Need:** Operations needs to see which orders took longer than 24 hours to move from 'Paid' to 'Shipped'.

* **SQL Logic:**
    ```sql
    SELECT order_id, 
           (shipped_date - payment_date) * 24 as hours_to_ship
    FROM orders
    WHERE (shipped_date - payment_date) * 24 > 24;
    ```
* **Explanation:** In most databases, subtracting two dates gives the difference in days. Multiplying by 24 converts that decimal into **Total Hours**.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [x] Scenarios 31 - 40: Subqueries & Analytical Functions
- [x] Scenarios 41 - 50: Packages & Modular Design
- [x] Scenarios 51 - 60: E-Commerce Logic
- [ ] Scenarios 61 - 70: Complex Joins & Set Operators (Pending)


# 📂 SQL & PL/SQL Master Tutorial: Scenarios 61 - 70

This module focuses on relational algebra—how to blend and compare data from multiple tables to answer complex business questions.

---

## 🔗 Module 7: Complex Joins & Set Operators

### **Scenario 61: Finding Employees and Their Managers (Self Join)**
**Business Need:** The Org Chart app needs to show a list of all employees and the name of their direct supervisor. Both names are in the same `Employees` table.

* **SQL Logic:**
    ```sql
    SELECT e.first_name AS Employee, 
           m.first_name AS Manager
    FROM employees e
    LEFT JOIN employees m ON e.manager_id = m.employee_id;
    ```

* **Explanation:** A **Self Join** treats a single table as two separate entities (aliases `e` and `m`). We use a `LEFT JOIN` so that the CEO (who has no manager) still appears in the list with a NULL manager.

---

### **Scenario 62: Identifying "Unsold" Products (Full Outer Join)**
**Business Need:** Marketing wants a report of all products that have never been sold, AND all sales records that somehow refer to a product ID that no longer exists in the catalog.

* **SQL Logic:**
    ```sql
    SELECT p.product_name, o.order_id
    FROM products p
    FULL OUTER JOIN order_items o ON p.product_id = o.product_id
    WHERE p.product_id IS NULL OR o.product_id IS NULL;
    ```
* **Explanation:** A **Full Outer Join** returns all rows from both tables. By filtering for `NULLs` on either side, we isolate the "orphans" (sales with no product) and the "ghosts" (products with no sales).

---

### **Scenario 63: Comparing Regional Sales (UNION vs UNION ALL)**
**Business Need:** Combine the sales records from the 'North' and 'South' databases into one master list for a corporate meeting.

* **SQL Logic:**
    ```sql
    SELECT product_id, sale_amount FROM sales_north
    UNION ALL
    SELECT product_id, sale_amount FROM sales_south;
    ```
* **Explanation:** * `UNION ALL`: Combines all rows (Fastest).
    * `UNION`: Combines rows but removes duplicates (Slower because it performs a sort). 
    * In sales reporting, we usually want `UNION ALL` to ensure every transaction is counted.

---

### **Scenario 64: Identifying Loyal Customers (INTERSECT)**
**Business Need:** Find the IDs of customers who placed an order in **both** 2024 and 2025.

* **SQL Logic:**
    ```sql
    SELECT customer_id FROM orders WHERE TO_CHAR(order_date, 'YYYY') = '2024'
    INTERSECT
    SELECT customer_id FROM orders WHERE TO_CHAR(order_date, 'YYYY') = '2025';
    ```

* **Explanation:** `INTERSECT` returns only the values that exist in **both** result sets. It’s a clean way to find "repeat" behavior across different time periods.

---

### **Scenario 65: Finding Non-Purchasing Leads (MINUS / EXCEPT)**
**Business Need:** The Marketing team has a list of "Leads" (potential customers). They want to find leads who have **not** signed up as actual customers yet.

* **SQL Logic:**
    ```sql
    SELECT email FROM leads
    MINUS
    SELECT email FROM customers;
    ```
* **Explanation:** `MINUS` (or `EXCEPT` in some SQL dialects) takes the first list and removes any items found in the second list. This is perfect for identifying gaps or targets.

---

### **Scenario 66: Cross Joins for Permutations**
**Business Need:** A clothing brand has 3 T-shirt colors and 3 sizes (S, M, L). Generate a list of all 9 possible product combinations.

* **SQL Logic:**
    ```sql
    SELECT c.color_name, s.size_code
    FROM colors c
    CROSS JOIN sizes s;
    ```
* **Explanation:** A **Cross Join** creates a Cartesian product. Every row in the first table is matched with every row in the second table. It is useful for generating master grids or testing data.

---

### **Scenario 67: Join with Multiple Conditions**
**Business Need:** Match a customer with a "Promotion" but only if the promotion is active for their specific country AND their total spend is within the promotion's range.

* **SQL Logic:**
    ```sql
    SELECT c.customer_name, p.promo_code
    FROM customers c
    JOIN promotions p ON c.country_id = p.country_id
                      AND c.total_spend BETWEEN p.min_spend AND p.max_spend;
    ```
* **Explanation:** Joins are not limited to just `ID = ID`. You can use `BETWEEN`, `>`, or any other logical operator to create complex relational matches.

---

### **Scenario 68: Natural Join (Pros and Cons)**
**Business Need:** Join `Employees` and `Departments` without specifying the columns manually.

* **SQL Logic:**
    ```sql
    SELECT * FROM employees NATURAL JOIN departments;
    ```
* **Explanation:** A **Natural Join** automatically matches columns with the same name (e.g., `department_id`). 
    * **Warning:** In professional environments, this is often avoided because if someone adds a new column with a common name (like `created_at`) to both tables, the query will break.

---

### **Scenario 69: Subquery vs Join (Performance Case)**
**Business Need:** Find the names of customers who bought a 'Laptop'.

* **SQL (Join approach):**
    ```sql
    SELECT DISTINCT c.customer_name 
    FROM customers c 
    JOIN orders o ON c.id = o.cust_id 
    WHERE o.item = 'Laptop';
    ```
* **Explanation:** Joins are generally preferred for data retrieval across tables, but if you only need columns from the first table, an `EXISTS` subquery might be faster as it doesn't have to build a full joined result set in memory.

---

### **Scenario 70: Multi-Table Join with Aggregation**
**Business Need:** Show each Category Name, the number of products in it, and the total value of stock for that category.

* **SQL Logic:**
    ```sql
    SELECT cat.category_name, 
           COUNT(p.product_id) as total_prods, 
           SUM(p.price * p.stock_qty) as total_value
    FROM categories cat
    LEFT JOIN products p ON cat.category_id = p.category_id
    GROUP BY cat.category_name;
    ```

* **Explanation:** This combines a `LEFT JOIN` (to include categories with zero products) with `GROUP BY` to summarize data across the relationship.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [x] Scenarios 31 - 40: Subqueries & Analytical Functions
- [x] Scenarios 41 - 50: Packages & Modular Design
- [x] Scenarios 51 - 60: E-Commerce Logic
- [x] Scenarios 61 - 70: Complex Joins & Set Operators
- [ ] Scenarios 71 - 80: Database Optimization & Indexing (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 71 - 80

This module focuses on Performance Tuning. It is not enough to get the "right answer"; in production, you must get the right answer **fast**.

---

## 🚀 Module 8: Database Optimization & Indexing

### **Scenario 71: Speeding up Search with B-Tree Indexes**
**Business Need:** Your `customers` table has 5 million rows. Searching for a customer by `email` currently takes 4 seconds, which is making the website feel slow.

* **SQL Logic:**
    ```sql
    CREATE INDEX idx_cust_email ON customers(email);
    ```

* **Explanation:** Without an index, the database does a **Full Table Scan** (reads every row). A B-Tree index works like a book's index; it allows the database to find the exact location of the data in a few steps, reducing search time from seconds to milliseconds.

---

### **Scenario 72: Improving Filter Performance with Function-Based Indexes**
**Business Need:** You frequently run queries like `SELECT * FROM employees WHERE UPPER(last_name) = 'SMITH'`. Even with a normal index on `last_name`, the query is slow.

* **SQL Logic:**
    ```sql
    CREATE INDEX idx_emp_upper_name ON employees(UPPER(last_name));
    ```
* **Explanation:** A standard index doesn't work when you apply a function (like `UPPER`) to a column in the `WHERE` clause. A **Function-Based Index** pre-calculates the result and stores it, allowing the index to be used.

---

### **Scenario 73: Identifying "Slow" Queries (Explain Plan)**
**Business Need:** You have a complex join that is lagging. You need to see if the database is using your indexes or ignoring them.

* **SQL Logic:**
    ```sql
    EXPLAIN PLAN FOR
    SELECT e.name, d.dept_name 
    FROM employees e JOIN departments d ON e.dept_id = d.dept_id;

    SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);
    ```

* **Explanation:** The **Execution Plan** is the database's "Flight Plan." It shows if it's doing a "Full Table Scan" (Bad for large tables) or an "Index Range Scan" (Good). Reading these plans is the #1 skill of a Senior DBA.

---

### **Scenario 74: Composite Indexes for Multi-Column Filters**
**Business Need:** You often search for products using both `category_id` AND `brand_id` together. 

* **SQL Logic:**
    ```sql
    CREATE INDEX idx_prod_cat_brand ON products(category_id, brand_id);
    ```
* **Explanation:** A **Composite Index** is more efficient than two separate indexes when both columns are used in the `WHERE` clause. **Rule of Thumb:** Place the column with the highest "cardinality" (most unique values) first in the index.

---

### **Scenario 75: Preventing Index Suppression**
**Business Need:** A developer wrote `WHERE salary * 12 > 120000`. The index on `salary` is being ignored.

* **SQL Optimization:**
    ```sql
    -- Change this:
    WHERE salary * 12 > 120000
    -- To this:
    WHERE salary > 120000 / 12
    ```
* **Explanation:** If you perform math on the column side of an equation, the database cannot use the index. By moving the math to the constant side, the index is "preserved" and the query speeds up.

---

### **Scenario 76: Using "Hints" to Guide the Optimizer**
**Business Need:** You know that a specific table is small and should be kept in memory, but the database keeps trying to read it from the disk.

* **SQL Logic:**
    ```sql
    SELECT /*+ FULL(e) CACHE(e) */ name FROM employees e;
    ```
* **Explanation:** **Hints** are instructions provided to the Optimizer inside comments `/*+ ... */`. While usually the database knows best, hints are used in edge cases to force a specific behavior like using a certain index or join method.

---

### **Scenario 77: Table Partitioning for Big Data**
**Business Need:** Your `transactions` table grows by 10 million rows a month. Deleting old data or searching through years of history is becoming impossible.

* **SQL Logic:**
    ```sql
    CREATE TABLE transactions (...)
    PARTITION BY RANGE (tx_date) (
        PARTITION p_2024_jan VALUES LESS THAN (TO_DATE('01-FEB-2024','DD-MON-YYYY')),
        PARTITION p_2024_feb VALUES LESS THAN (TO_DATE('01-MAR-2024','DD-MON-YYYY'))
    );
    ```

* **Explanation:** **Partitioning** physically breaks one large table into smaller pieces. When you query for Jan 2024 data, the database only looks at that specific partition (Partition Pruning), ignoring the other billions of rows.

---

### **Scenario 78: Index Organized Tables (IOT)**
**Business Need:** You have a mapping table (e.g., `Product_Tags`) that only has two columns. You want the table itself to be stored in the index structure to save space and speed up lookups.

* **SQL Logic:**
    ```sql
    CREATE TABLE product_tags (
        product_id NUMBER,
        tag_id NUMBER,
        PRIMARY KEY (product_id, tag_id)
    ) ORGANIZATION INDEX;
    ```
* **Explanation:** In a normal table (Heap), the data and the index are separate. In an **IOT**, the data is stored inside the B-Tree of the Primary Key. This is perfect for "Association Tables" with few columns.

---

### **Scenario 79: Statistics Gathering (GATHER_TABLE_STATS)**
**Business Need:** You added 1 million rows to a table, but the queries are still slow. The database is still acting as if the table is small.

* **PL/SQL Logic:**
    ```sql
    EXEC DBMS_STATS.GATHER_TABLE_STATS('HR', 'EMPLOYEES');
    ```
* **Explanation:** The Optimizer makes decisions based on **Statistics** (how many rows, how many nulls, etc.). If stats are "stale" (old), the database might choose a bad plan. Running this updates the database's "knowledge."

---

### **Scenario 80: Avoiding "SELECT *" in Production**
**Business Need:** You want to reduce the load on the network and memory when fetching data for a dashboard.

* **SQL Optimization:**
    ```sql
    -- Bad:
    SELECT * FROM orders;
    -- Good:
    SELECT order_id, status, amount FROM orders;
    ```
* **Explanation:** `SELECT *` forces the database to fetch every column (including large text or blobs) from the disk. Selecting only the columns you need reduces I/O, utilizes "Index-Only Scans," and makes your code more resilient to schema changes.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [x] Scenarios 31 - 40: Subqueries & Analytical Functions
- [x] Scenarios 41 - 50: Packages & Modular Design
- [x] Scenarios 51 - 60: E-Commerce Logic
- [x] Scenarios 61 - 70: Complex Joins & Set Operators
- [x] Scenarios 71 - 80: Database Optimization
- [ ] Scenarios 81 - 90: Advanced PL/SQL Collections (Coming Next...)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 81 - 90

This module dives into advanced data structures. We move beyond simple tables and learn how to handle complex data in memory using Nested Tables and Multiset operators.

---

## 🗂️ Module 9: Advanced PL/SQL Collections & Records

### **Scenario 81: Comparing Two Collections (MULTISET Operators)**
**Business Need:** You have two lists of Product IDs in memory—one for "Current Stock" and one for "Promoted Items." You need to find which items are in both lists.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        TYPE t_id_list IS TABLE OF NUMBER;
        v_stock    t_id_list := t_id_list(101, 102, 103, 104);
        v_promoted t_id_list := t_id_list(103, 104, 105, 106);
        v_result   t_id_list;
    BEGIN
        v_result := v_stock MULTISET INTERSECT v_promoted;
        -- v_result now contains (103, 104)
    END;
    ```
* **Explanation:** **MULTISET** operators allow you to perform set logic (Union, Intersect, Except) directly on PL/SQL collections in memory without writing slow loops or using temporary tables.

---

### **Scenario 82: Removing Duplicates from a Collection**
**Business Need:** After merging multiple data sources into a single Nested Table, you have duplicate IDs. You need a unique list.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        TYPE t_name_list IS TABLE OF VARCHAR2(50);
        v_list t_name_list := t_name_list('Apple', 'Orange', 'Apple', 'Banana');
    BEGIN
        v_list := SET(v_list);
        -- v_list now contains ('Apple', 'Orange', 'Banana')
    END;
    ```
* **Explanation:** The `SET()` function is a powerful built-in tool that instantly converts a collection into a unique set, removing all duplicate values in one line of code.

---

### **Scenario 83: Bulk Collect with LIMIT**
**Business Need:** You need to process 1 million rows. Fetching all of them into memory at once will cause an "Out of Memory" error.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        CURSOR c_data IS SELECT * FROM large_table;
        TYPE t_tab IS TABLE OF c_data%ROWTYPE;
        v_rows t_tab;
    BEGIN
        OPEN c_data;
        LOOP
            FETCH c_data BULK COLLECT INTO v_rows LIMIT 5000;
            EXIT WHEN v_rows.COUNT = 0;
            -- Process the 5000 rows here
        END LOOP;
        CLOSE c_data;
    END;
    ```

* **Explanation:** Using the **LIMIT** clause with `BULK COLLECT` allows you to process data in "chunks." This provides the speed of bulk processing while keeping the memory footprint low.

---

### **Scenario 84: Using %ROWTYPE for Table Synchronization**
**Business Need:** You need to move a record from the `active_orders` table to the `archive_orders` table. Both tables have identical structures.

* **PL/SQL Logic:**
    ```sql
    DECLARE
        v_order_rec active_orders%ROWTYPE;
    BEGIN
        SELECT * INTO v_order_rec FROM active_orders WHERE id = 500;
        
        INSERT INTO archive_orders VALUES v_order_rec;
        DELETE FROM active_orders WHERE id = 500;
        COMMIT;
    END;
    ```
* **Explanation:** `%ROWTYPE` automatically creates a record structure that matches the table. This makes your code "schema-aware"—if you add a column to the table later, this code doesn't need to be updated.

---

### **Scenario 85: Nested Tables inside Database Columns**
**Business Need:** A "Project" can have many "Milestones." Instead of a separate table, you want to store the milestones directly inside the project row.

* **SQL Logic:**
    ```sql
    CREATE OR REPLACE TYPE milestone_type AS OBJECT (name VARCHAR2(50), due_date DATE);
    /
    CREATE OR REPLACE TYPE milestone_tab AS TABLE OF milestone_type;
    /
    CREATE TABLE projects (
        proj_id NUMBER,
        milestones milestone_tab
    ) NESTED TABLE milestones STORE AS milestones_storage_table;
    ```
* **Explanation:** **Nested Tables** as column types allow you to store 1-to-many relationships within a single row. This is useful for object-oriented data modeling in a relational database.

---

### **Scenario 86: Handling "NO_DATA_FOUND" in Collections**
**Business Need:** You are using an Associative Array (Index-By table). If you try to access a key that hasn't been defined, the code crashes.

* **PL/SQL Logic:**
    ```sql
    IF v_my_array.EXISTS(101) THEN
        v_val := v_my_array(101);
    ELSE
        v_val := 0;
    END IF;
    ```
* **Explanation:** The `.EXISTS` method is the safest way to check if an index has been assigned a value before trying to read it, preventing the common `ORA-01403: no data found` error.

---

### **Scenario 87: Deleting Specific Elements from a Collection**
**Business Need:** You have a collection of IDs. After validation, you need to remove the 3rd element because it's invalid.

* **PL/SQL Logic:**
    ```sql
    v_list.DELETE(3); -- Removes only the 3rd element
    -- Note: This leaves a "gap" in the collection (sparse collection)
    ```
* **Explanation:** The `.DELETE(n)` method allows for precise memory management. However, be careful: after deleting, you must use `.FIRST`, `.NEXT`, and `.LAST` to loop through the collection instead of a standard `1..COUNT` loop.

---

### **Scenario 88: Transforming a Collection into a Result Set (TABLE Operator)**
**Business Need:** You have a list of strings in a PL/SQL variable, and you want to use them in a `JOIN` with a standard SQL table.

* **SQL Logic:**
    ```sql
    SELECT p.product_name
    FROM products p
    JOIN TABLE(v_my_id_list) t ON p.product_id = t.column_value;
    ```
* **Explanation:** The **TABLE()** operator treats a PL/SQL collection as if it were a real database table, allowing you to use it in standard SELECT statements, joins, and filters.

---

### **Scenario 89: Member Of (Checking existence in a collection)**
**Business Need:** Check if a specific "User ID" exists within a list of "Banned IDs" stored in a nested table.

* **PL/SQL Logic:**
    ```sql
    IF 505 MEMBER OF v_banned_list THEN
        RAISE_APPLICATION_ERROR(-20001, 'User is banned');
    END IF;
    ```
* **Explanation:** The `MEMBER OF` syntax is a clean, readable way to search a Nested Table without writing a manual `FOR` loop to check every item.

---

### **Scenario 90: Record Comparison (IS NULL Check)**
**Business Need:** You are comparing two records to see if they are empty before performing an insert.

* **PL/SQL Logic:**
    ```sql
    IF v_emp_rec.emp_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('Record is empty');
    END IF;
    ```
* **Explanation:** While you cannot compare two records directly using `rec1 = rec2`, you can check if a primary attribute of a record is null to determine if the record was successfully populated.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [x] Scenarios 31 - 40: Subqueries & Analytical Functions
- [x] Scenarios 41 - 50: Packages & Modular Design
- [x] Scenarios 51 - 60: E-Commerce Logic
- [x] Scenarios 61 - 70: Complex Joins & Set Operators
- [x] Scenarios 71 - 80: Database Optimization
- [x] Scenarios 81 - 90: Advanced Collections
- [ ] Scenarios 91 - 100: Database Security & User Management (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 91 - 100

This module focuses on the "Gatekeeper" role of the database. You will learn how to manage users, control permissions, and implement security policies to protect sensitive data.

---

## 🔐 Module 10: Database Security & User Management

### **Scenario 91: Creating a "Read-Only" Analyst User**
**Business Need:** A new Data Analyst has joined the team. They need to run reports on the `Sales` and `Customers` tables, but they must be blocked from changing or deleting any data.

* **SQL Logic:**
    ```sql
    -- 1. Create the user
    CREATE USER analyst_bob IDENTIFIED BY SecurePass123;
    -- 2. Grant connection rights
    GRANT CREATE SESSION TO analyst_bob;
    -- 3. Grant specific read rights
    GRANT SELECT ON hr.sales TO analyst_bob;
    GRANT SELECT ON hr.customers TO analyst_bob;
    ```
* **Explanation:** Security follows the **Principle of Least Privilege**. We only grant `SELECT` (read) and not `INSERT`, `UPDATE`, or `DELETE`. This ensures Bob can do his job without accidentally damaging production data.

---

### **Scenario 92: Simplifying Permissions with Roles**
**Business Need:** You have 50 Developers. Instead of manually granting 20 table permissions to each one, you want a way to manage them as a single group.

* **SQL Logic:**
    ```sql
    -- Create a Role
    CREATE ROLE developer_role;
    -- Assign permissions to the Role
    GRANT SELECT, INSERT, UPDATE ON hr.employees TO developer_role;
    GRANT CREATE TABLE, CREATE VIEW TO developer_role;
    -- Assign the Role to users
    GRANT developer_role TO user_alice;
    GRANT developer_role TO user_john;
    ```

* **Explanation:** A **Role** acts as a container for permissions. If a new table is added, you only grant permission to the *Role* once, and every user assigned to that role gets the update automatically.

---

### **Scenario 93: Column-Level Security (Sensitive Data)**
**Business Need:** Finance staff can update all columns in the `Employees` table except for the `Salary` column. Only the Payroll Manager can update salaries.

* **SQL Logic:**
    ```sql
    -- Grant general update on all columns except salary
    GRANT UPDATE (first_name, last_name, email, hire_date) 
    ON hr.employees TO finance_clerk;
    ```
* **Explanation:** SQL allows you to restrict permissions down to specific columns. This prevents a clerk from accidentally (or intentionally) changing their own pay while still allowing them to update contact information.

---

### **Scenario 94: Temporary Access with "WITH GRANT OPTION"**
**Business Need:** You are a Lead Developer. You want to give a Junior Dev permission to see a table, and you also want them to be able to give that same permission to their teammates without asking you again.

* **SQL Logic:**
    ```sql
    GRANT SELECT ON hr.projects TO junior_dev WITH GRANT OPTION;
    ```
* **Explanation:** `WITH GRANT OPTION` allows the receiver to pass the privilege to others. **Warning:** Use this sparingly, as it can lead to "Privilege Creep" where too many people end up with access.

---

### **Scenario 95: Account Locking Policy**
**Business Need:** To prevent "Brute Force" hacking attacks, a user account should be locked automatically if they enter the wrong password 3 times.

* **SQL Logic:**
    ```sql
    CREATE PROFILE secure_user_profile LIMIT
        FAILED_LOGIN_ATTEMPTS 3
        PASSWORD_LOCK_TIME 1; -- Locked for 1 day
    
    ALTER USER analyst_bob PROFILE secure_user_profile;
    ```
* **Explanation:** A **Profile** defines password policies. This is a crucial security layer that protects the database at the login level.

---

### **Scenario 96: Revoking Privileges (The "Cascade" Effect)**
**Business Need:** An intern has left the company. You need to remove their access to the `Invoices` table.

* **SQL Logic:**
    ```sql
    REVOKE SELECT ON hr.invoices FROM intern_user;
    ```
* **Explanation:** `REVOKE` is the opposite of `GRANT`. Note that if you revoke a privilege from someone who used `WITH GRANT OPTION` to tell others, those other people may also lose access depending on the database type (Cascading Revoke).

---

### **Scenario 97: Auditing Sensitive Actions**
**Business Need:** The compliance department requires a log of every time *any* user deletes a record from the `Payments` table.

* **SQL Logic:**
    ```sql
    AUDIT DELETE ON hr.payments BY ACCESS;
    ```
* **Explanation:** **Database Auditing** keeps a record of "Who did what and when." Unlike a manual trigger, this is a built-in feature that is harder for a malicious user to bypass.

---

### **Scenario 98: Creating a "Public" Synonym for Convenience**
**Business Need:** Users are tired of typing `SELECT * FROM hr.very_long_schema_name.price_list`. They want to just type `SELECT * FROM prices`.

* **SQL Logic:**
    ```sql
    CREATE PUBLIC SYNONYM prices FOR hr.price_list;
    ```
* **Explanation:** A **Synonym** is an alias. A `PUBLIC` synonym allows everyone in the database to use a simple name instead of the full "Schema.Table" path.

---

### **Scenario 99: Row-Level Security (VPD Policy)**
**Business Need:** In a global firm, a Manager in Japan should *never* see the rows belonging to the UK branch, even if they run a `SELECT *`.

* **PL/SQL Strategy:**
    ```sql
    -- Create a function that returns a filter condition
    CREATE OR REPLACE FUNCTION fn_branch_security(obj_schema VARCHAR2, obj_name VARCHAR2)
    RETURN VARCHAR2 IS
    BEGIN
        RETURN 'branch_id = SYS_CONTEXT(''user_env'', ''my_branch_id'')';
    END;
    ```

* **Explanation:** This is **Virtual Private Database (VPD)**. The database silently "tacks on" a `WHERE` clause to every query the user runs, ensuring they only see their authorized rows.

---

### **Scenario 100: Protecting Source Code (Wrap Revisited)**
**Business Need:** You have a procedure that contains a secret encryption key. You must prevent DBAs from seeing that key in the `user_source` view.

* **PL/SQL Logic:**
    ```sql
    -- Use DBMS_DDL.WRAP to obfuscate the code
    EXEC DBMS_DDL.CREATE_WRAPPED('CREATE OR REPLACE PROCEDURE secret_proc...');
    ```
* **Explanation:** While the DBA can see that the procedure exists, the actual logic (the "source code") will look like scrambled characters, protecting your intellectual property and security keys.

---

### 🚀 Progress Tracker: Level 1 Complete!
- [x] Scenarios 1 - 10: Banking Basics
- [x] Scenarios 11 - 20: Advanced Operations
- [x] Scenarios 21 - 30: Cursors & Collections
- [x] Scenarios 31 - 40: Subqueries & Analytical Functions
- [x] Scenarios 41 - 50: Packages & Modular Design
- [x] Scenarios 51 - 60: E-Commerce Logic
- [x] Scenarios 61 - 70: Complex Joins & Set Operators
- [x] Scenarios 71 - 80: Database Optimization
- [x] Scenarios 81 - 90: Advanced Collections
- [x] Scenarios 91 - 100: Database Security
- [ ] Scenarios 101 - 110: Healthcare & HIPAA Compliance (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 101 - 110

This module explores the sensitive world of Healthcare IT. The focus here is on protecting Patient Identifiable Information (PII) and managing high-stakes medical data.

---

## 🏥 Module 11: Healthcare & HIPAA Compliance

### **Scenario 101: Patient Data Masking for Research**
**Business Need:** Medical researchers need to see patient ages and diagnoses for a study, but they must not see names or full Social Security Numbers (SSN).

* **SQL Logic:**
    ```sql
    CREATE VIEW view_research_data AS
    SELECT 
        patient_id,
        'XXX-XX-' || SUBSTR(ssn, -4) AS masked_ssn,
        EXTRACT(YEAR FROM SYSDATE) - EXTRACT(YEAR FROM dob) AS age,
        diagnosis_code
    FROM patients;
    ```
* **Explanation:** This view uses **Data Masking**. By only showing the last 4 digits of the SSN and calculating the age instead of showing the Date of Birth (DOB), we comply with privacy laws while still providing the researchers with the data they need.

---

### **Scenario 102: Preventing Appointment Overlaps**
**Business Need:** A doctor cannot be in two places at once. The database must reject any appointment that overlaps with an existing one for the same doctor.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_check_appt_overlap
    BEFORE INSERT ON appointments
    FOR EACH ROW
    DECLARE
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM appointments
        WHERE doctor_id = :NEW.doctor_id
        AND appt_date = :NEW.appt_date
        AND (:NEW.start_time < end_time AND :NEW.end_time > start_time);

        IF v_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20101, 'Schedule Conflict: Doctor is already booked.');
        END IF;
    END;
    ```

* **Explanation:** This logic uses a **Collision Check**. If the new appointment's start time is before an existing one ends, AND its end time is after an existing one starts, a conflict is detected and the insert is blocked.

---

### **Scenario 103: Automated Prescription Expiry**
**Business Need:** To prevent drug misuse, any prescription older than 6 months should be automatically marked as 'EXPIRED'.

* **SQL Logic:**
    ```sql
    UPDATE prescriptions
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
    AND issue_date < ADD_MONTHS(SYSDATE, -6);
    ```
* **Explanation:** This is a simple but critical **Maintenance Query**. In a healthcare system, this would typically be part of a nightly batch job to ensure clinicians don't accidentally refill outdated orders.

---

### **Scenario 104: Emergency Access "Break-Glass" Audit**
**Business Need:** Usually, an ER doctor cannot see a patient's full psychiatric history. However, in an emergency, they can "Break the Glass" to gain access. This action must be logged with a high-priority alert.

* **PL/SQL Procedure:**
    ```sql
    CREATE OR REPLACE PROCEDURE proc_emergency_access (
        p_doc_id NUMBER, 
        p_pat_id NUMBER, 
        p_reason VARCHAR2
    ) IS
    BEGIN
        INSERT INTO emergency_access_logs (doctor_id, patient_id, access_time, reason)
        VALUES (p_doc_id, p_pat_id, SYSDATE, p_reason);
        
        -- Code to grant temporary view permission here...
        COMMIT;
    END;
    ```
* **Explanation:** This creates a **Forensic Audit Trail**. By forcing the doctor to provide a reason through a procedure, the hospital ensures that "Break-Glass" events are documented for legal review.

---

### **Scenario 105: Calculating Patient BMI (Body Mass Index)**
**Business Need:** Nurses enter height (cm) and weight (kg). The system must automatically calculate the BMI for the doctor's review.

* **SQL Virtual Column:**
    ```sql
    ALTER TABLE patient_vitals 
    ADD (bmi AS (ROUND(weight_kg / ((height_cm/100) * (height_cm/100)), 2)));
    ```
* **Explanation:** A **Virtual Column** is perfect for this. The database calculates the value on-the-fly. This ensures the BMI is always mathematically correct and up-to-date based on the latest height and weight entries.

---

### **Scenario 106: Monitoring Critical Vitals (Alerts)**
**Business Need:** If a patient's heart rate goes above 150 or below 40 during an update, the record should be flagged as "CRITICAL_ALERT".

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_vitals_alert
    BEFORE INSERT OR UPDATE ON vitals_log
    FOR EACH ROW
    BEGIN
        IF :NEW.heart_rate > 150 OR :NEW.heart_rate < 40 THEN
            :NEW.alert_status := 'CRITICAL';
            -- Optionally call a notification procedure here
        END IF;
    END;
    ```
* **Explanation:** This trigger acts as a **Real-Time Monitor**. It transforms data as it enters the system, ensuring that dangerous medical readings are immediately visible to medical staff.

---

### **Scenario 107: Tracking Patient "Length of Stay" (LOS)**
**Business Need:** The hospital board needs a report showing the average number of days patients stay in the hospital, grouped by the admitting department.

* **SQL Logic:**
    ```sql
    SELECT department_name, 
           AVG(discharge_date - admission_date) as avg_stay_days
    FROM admissions
    WHERE discharge_date IS NOT NULL
    GROUP BY department_name;
    ```
* **Explanation:** In most SQL engines, subtracting two DATE fields gives you the difference in days. This is an essential **KPI (Key Performance Indicator)** for hospital resource management.

---

### **Scenario 108: Ensuring One Primary Physician per Patient**
**Business Need:** A patient can see many specialists, but only one doctor can be marked as their "Primary Care Provider" (PCP).

* **SQL Logic:**
    ```sql
    CREATE UNIQUE INDEX udx_one_pcp ON patient_doctors(patient_id)
    WHERE relationship_type = 'PRIMARY';
    ```
* **Explanation:** A **Filtered Unique Index** (or Function-Based Unique Index) ensures that for each `patient_id`, only one row can exist where the relationship is 'PRIMARY'. It allows multiple 'SPECIALIST' rows but restricts the 'PRIMARY' status to one.

---

### **Scenario 109: Storing Medical Images (BLOB Handling)**
**Business Need:** Store X-ray images (DICOM files) directly in the database so they are backed up along with the patient record.

* **SQL Logic:**
    ```sql
    CREATE TABLE patient_images (
        image_id NUMBER PRIMARY KEY,
        patient_id NUMBER,
        image_blob BLOB,
        upload_date DATE
    );
    ```

* **Explanation:** **BLOB (Binary Large Object)** is used for non-text data like images, PDFs, or audio. This keeps all relevant patient data in one place, though for very large systems, developers might store only the file path (BFILE).

---

### **Scenario 110: De-identifying Records for QA Testing**
**Business Need:** Developers need a copy of the production database for testing, but they must not see any real patient names.

* **SQL Logic:**
    ```sql
    UPDATE patient_test_copy
    SET first_name = 'TEST_USER_' || patient_id,
        last_name = 'LN_' || patient_id,
        email = 'test_' || patient_id || '@hospital.local';
    ```
* **Explanation:** This is a **Data Scrambling** technique. By replacing real names with generic identifiers tied to the ID, developers can still test the system logic without violating patient confidentiality.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 100: Levels 1-10 Complete
- [x] Scenarios 101 - 110: Healthcare & HIPAA
- [ ] Scenarios 111 - 120: Human Resources & Payroll (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 111 - 120

This module focuses on the administrative backbone of any organization: Managing people, their compensation, and their career growth.

---

## 👥 Module 12: HR & Payroll Management

### **Scenario 111: Calculating Prorated Salary**
**Business Need:** If an employee joins in the middle of a month, the payroll system must calculate their pay based on the number of days they actually worked.

* **SQL Logic:**
    ```sql
    SELECT employee_id, 
           monthly_salary,
           hire_date,
           (monthly_salary / LAST_DAY(SYSDATE) - TRUNC(SYSDATE, 'MM') + 1) * (LAST_DAY(hire_date) - hire_date + 1) AS prorated_pay
    FROM employees
    WHERE TO_CHAR(hire_date, 'MM-YYYY') = TO_CHAR(SYSDATE, 'MM-YYYY');
    ```
* **Explanation:** This logic uses `LAST_DAY` to determine how many days are in the current month. It then calculates a daily rate and multiplies it by the remaining days in the month from the `hire_date`.

---

### **Scenario 112: Tracking Manager Spans of Control**
**Business Need:** HR wants to identify managers who have more than 10 direct reports, as this may lead to burnout or inefficiency.

* **SQL Logic:**
    ```sql
    SELECT m.employee_id, m.first_name, COUNT(e.employee_id) AS direct_reports
    FROM employees m
    JOIN employees e ON m.employee_id = e.manager_id
    GROUP BY m.employee_id, m.first_name
    HAVING COUNT(e.employee_id) > 10;
    ```
* **Explanation:** This uses a **Self-Join** and a `HAVING` clause to aggregate the hierarchy. It treats one instance of the table as "Managers" and the other as "Employees."

---

### **Scenario 113: Automatic Salary Grade Assignment**
**Business Need:** When a new employee is hired, their "Salary Grade" (A, B, or C) should be automatically assigned based on their base salary.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_assign_grade
    BEFORE INSERT ON employees
    FOR EACH ROW
    BEGIN
        IF :NEW.salary >= 10000 THEN :NEW.grade := 'A';
        ELSIF :NEW.salary >= 5000 THEN :NEW.grade := 'B';
        ELSE :NEW.grade := 'C';
        END IF;
    END;
    ```
* **Explanation:** A `BEFORE INSERT` trigger is ideal here because it modifies the data *before* it is committed to the disk, ensuring the `grade` column is never empty or incorrect.

---

### **Scenario 114: Detecting "Ghost" Employees (Audit)**
**Business Need:** For fraud prevention, find any employee records that share the same Bank Account Number but have different Social Security Numbers.

* **SQL Logic:**
    ```sql
    SELECT bank_account_no, COUNT(DISTINCT ssn)
    FROM employee_payroll_details
    GROUP BY bank_account_no
    HAVING COUNT(DISTINCT ssn) > 1;
    ```
* **Explanation:** This is a classic **Anomaly Detection** query. It identifies potential payroll fraud where one person might be collecting multiple salaries under different names.

---

### **Scenario 115: Performance Review Cycle Alerts**
**Business Need:** Generate a list of employees who have not had a performance review in the last 12 months.

* **SQL Logic:**
    ```sql
    SELECT e.first_name, e.last_name, MAX(r.review_date) as last_review
    FROM employees e
    LEFT JOIN performance_reviews r ON e.employee_id = r.employee_id
    GROUP BY e.first_name, e.last_name
    HAVING MAX(r.review_date) < ADD_MONTHS(SYSDATE, -12) 
       OR MAX(r.review_date) IS NULL;
    ```
* **Explanation:** Using a `LEFT JOIN` ensures we don't forget new employees who have *never* had a review. The `MAX` function finds the most recent date, and `ADD_MONTHS` filters for the one-year gap.

---

### **Scenario 116: Handling Step-Increases (Looping)**
**Business Need:** A union contract requires all employees in "Level 1" to receive a 2% raise every year on their anniversary, capped at a maximum of $40,000.

* **PL/SQL Procedure:**
    ```sql
    UPDATE employees
    SET salary = LEAST(salary * 1.02, 40000)
    WHERE job_level = 1 
    AND TO_CHAR(hire_date, 'DD-MON') = TO_CHAR(SYSDATE, 'DD-MON');
    ```
* **Explanation:** `LEAST` is a great function for capping values. It compares the new calculated salary with the $40,000 limit and picks whichever is smaller.

---

### **Scenario 117: Finding Top Earners per Department**
**Business Need:** Show the highest-paid employee in every department.

* **SQL Logic:**
    ```sql
    SELECT department_id, first_name, salary
    FROM (
        SELECT department_id, first_name, salary,
               ROW_NUMBER() OVER (PARTITION BY department_id ORDER BY salary DESC) as rank
        FROM employees
    )
    WHERE rank = 1;
    ```

* **Explanation:** `ROW_NUMBER()` is used within a subquery to assign a unique rank to each employee within their specific department based on salary. The outer query then picks the top one.

---

### **Scenario 118: Calculating Employee Tenure**
**Business Need:** For an awards ceremony, calculate exactly how many years, months, and days an employee has been with the company.

* **SQL Logic:**
    ```sql
    SELECT first_name,
           FLOOR(MONTHS_BETWEEN(SYSDATE, hire_date) / 12) AS years,
           MOD(FLOOR(MONTHS_BETWEEN(SYSDATE, hire_date)), 12) AS months
    FROM employees;
    ```
* **Explanation:** `MONTHS_BETWEEN` is the most accurate way to handle dates in HR. We use `FLOOR` to get whole years and `MOD` to get the remaining months.

---

### **Scenario 119: Preventing Salary Decreases**
**Business Need:** Company policy states that once a salary is set, it can be increased but never decreased via the standard HR application.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_no_salary_cut
    BEFORE UPDATE OF salary ON employees
    FOR EACH ROW
    BEGIN
        IF :NEW.salary < :OLD.salary THEN
            RAISE_APPLICATION_ERROR(-20005, 'Salary decreases are not permitted.');
        END IF;
    END;
    ```
* **Explanation:** This enforces a **Business Rule** at the data layer. Even if the HR software has a bug, the database will block any attempt to lower an employee's pay.

---

### **Scenario 120: Organizing a Departmental Hierarchy (Recursive CTE)**
**Business Need:** Generate a full report showing the chain of command from the CEO down to the interns, including the "Level" of each employee.

* **SQL Logic:**
    ```sql
    SELECT LEVEL, LPAD(' ', 2*(LEVEL-1)) || first_name as org_chart
    FROM employees
    START WITH manager_id IS NULL
    CONNECT BY PRIOR employee_id = manager_id;
    ```

* **Explanation:** `CONNECT BY` is a specialized Oracle SQL syntax for **Hierarchical Queries**. It traverses the parent-child relationship (Manager-Employee) and the `LEVEL` pseudo-column tells us how deep in the tree we are.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 100: Levels 1-10 Complete
- [x] Scenarios 101 - 110: Healthcare & HIPAA
- [x] Scenarios 111 - 120: HR & Payroll
- [ ] Scenarios 121 - 130: Logistics & Supply Chain (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 121 - 130

This module addresses the complexities of moving goods. You will learn how to handle shipping statuses, inventory aging, and route optimization logic.

---

## 🚚 Module 13: Logistics & Supply Chain

### **Scenario 121: Real-Time Shipment Tracking Updates**
**Business Need:** When a package is scanned at a warehouse, the system must update the shipment status and automatically calculate the "Estimated Delivery Date" based on the shipping method.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_update_delivery_est
    BEFORE UPDATE OF current_location ON shipments
    FOR EACH ROW
    BEGIN
        :NEW.last_update_time := SYSDATE;
        
        -- If it's 'EXPRESS', delivery is in 2 days; else 5 days.
        IF :NEW.ship_mode = 'EXPRESS' THEN
            :NEW.est_delivery := SYSDATE + 2;
        ELSE
            :NEW.est_delivery := SYSDATE + 5;
        END IF;
    END;
    ```
* **Explanation:** This trigger ensures that every time the physical location changes, the metadata (last update time) and the business prediction (delivery date) are kept in sync automatically.

---

### **Scenario 122: Warehouse Bin Optimization**
**Business Need:** To speed up picking, find "High-Velocity" items (items sold more than 100 times a week) that are currently stored in the "Back Zone" of the warehouse instead of the "Front Zone."

* **SQL Logic:**
    ```sql
    SELECT p.product_id, p.product_name, w.bin_location
    FROM products p
    JOIN warehouse_inventory w ON p.product_id = w.product_id
    WHERE w.zone = 'BACK'
    AND p.product_id IN (
        SELECT product_id 
        FROM order_items 
        WHERE order_date > SYSDATE - 7
        GROUP BY product_id 
        HAVING COUNT(*) > 100
    );
    ```
* **Explanation:** This query identifies inefficiencies in physical storage. It uses a subquery to find popular items and joins it with warehouse data to find those that are incorrectly positioned.

---

### **Scenario 123: First-In, First-Out (FIFO) Inventory Aging**
**Business Need:** Identify stock batches that have been in the warehouse for more than 90 days so they can be discounted before they expire or become obsolete.

* **SQL Logic:**
    ```sql
    SELECT batch_id, product_id, received_date,
           TRUNC(SYSDATE - received_date) AS days_in_stock
    FROM stock_batches
    WHERE status = 'AVAILABLE'
    AND received_date < SYSDATE - 90
    ORDER BY received_date ASC;
    ```

* **Explanation:** This is essential for **Inventory Aging Analysis**. By calculating the difference between the current date and the `received_date`, we can trigger "Clearance Sales" logic.

---

### **Scenario 124: Calculating Total Shipment Weight**
**Business Need:** A truck has a weight limit of 5,000kg. Calculate the total weight of all items assigned to a specific "Load ID."

* **SQL Logic:**
    ```sql
    SELECT load_id, SUM(item_weight * quantity) AS total_load_weight
    FROM shipment_manifest
    GROUP BY load_id
    HAVING SUM(item_weight * quantity) > 5000;
    ```
* **Explanation:** This query uses `SUM` and `GROUP BY` to perform physical math. The `HAVING` clause acts as a safety check to flag trucks that are overloaded.

---

### **Scenario 125: Handling Multi-Stop Delivery Routes**
**Business Need:** For a single "Route ID," list the stops in the correct sequence and show the distance between the current stop and the previous one.

* **SQL Logic:**
    ```sql
    SELECT route_id, stop_name, stop_sequence,
           distance_from_hub - LAG(distance_from_hub, 1, 0) 
           OVER (PARTITION BY route_id ORDER BY stop_sequence) AS leg_distance
    FROM delivery_route_stops;
    ```
* **Explanation:** The `LAG` analytical function is used here to look at the previous row's distance. Subtracting it from the current row's distance gives us the distance of that specific "leg" of the journey.

---

### **Scenario 126: Automatic Reorder Point (Restock Alert)**
**Business Need:** When inventory falls below a specific "Minimum Level," automatically insert a record into the `procurement_tasks` table.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_reorder_check
    AFTER UPDATE OF stock_qty ON warehouse_inventory
    FOR EACH ROW
    WHEN (NEW.stock_qty <= NEW.min_reorder_level)
    BEGIN
        INSERT INTO procurement_tasks (product_id, qty_to_order, request_date)
        VALUES (:NEW.product_id, :NEW.standard_order_qty, SYSDATE);
    END;
    ```
* **Explanation:** This is an example of **Automated Supply Chain Triggering**. It removes the need for manual monitoring by creating a task for the purchasing team the moment stock is low.

---

### **Scenario 127: Identifying Split Shipments**
**Business Need:** Find orders where items had to be shipped from two or more different warehouses.

* **SQL Logic:**
    ```sql
    SELECT order_id, COUNT(DISTINCT warehouse_id) AS warehouse_count
    FROM shipment_details
    GROUP BY order_id
    HAVING COUNT(DISTINCT warehouse_id) > 1;
    ```
* **Explanation:** Split shipments increase shipping costs. This query helps the logistics team identify which orders are the most expensive to fulfill.

---

### **Scenario 128: Average "Dock-to-Stock" Time**
**Business Need:** Measure how long it takes for items to go from "Arrived at Dock" to "Available on Shelf."

* **SQL Logic:**
    ```sql
    SELECT AVG(available_date - arrival_date) * 24 AS avg_hours_to_shelf
    FROM stock_receipts
    WHERE available_date IS NOT NULL;
    ```
* **Explanation:** This is a key **Warehouse Efficiency Metric**. By measuring the time gap in hours (multiplying the day difference by 24), management can identify bottlenecks in the receiving process.

---

### **Scenario 129: Carrier Performance Ranking**
**Business Need:** Rank shipping carriers (FedEx, UPS, DHL) based on the percentage of deliveries they completed on or before the Estimated Delivery Date.

* **SQL Logic:**
    ```sql
    SELECT carrier_name,
           RATIO_TO_REPORT(on_time_count) OVER() * 100 AS performance_score
    FROM (
        SELECT carrier_name, COUNT(*) AS on_time_count
        FROM shipments
        WHERE actual_delivery <= est_delivery
        GROUP BY carrier_name
    );
    ```

* **Explanation:** `RATIO_TO_REPORT` is an analytical function that calculates the percentage of a value against the total. It’s perfect for creating a "Leaderboard" of vendors or carriers.

---

### **Scenario 130: Bulk Updating Shipping Zones**
**Business Need:** Due to a new contract, all zip codes starting with '902' need to be moved from 'Zone 1' to 'Zone 2'.

* **SQL Logic:**
    ```sql
    UPDATE shipping_rates
    SET zone_id = 2
    WHERE zip_code LIKE '902%';
    ```
* **Explanation:** The `LIKE` operator with the `%` wildcard allows for bulk pattern matching. This is a common administrative task when shipping boundaries change.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 100: Levels 1-10 Complete
- [x] Scenarios 101 - 110: Healthcare & HIPAA
- [x] Scenarios 111 - 120: HR & Payroll
- [x] Scenarios 121 - 130: Logistics & Supply Chain
- [ ] Scenarios 131 - 140: Telecommunications & Data Usage (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 131 - 140

This module deals with "High-Velocity" data. Telecom databases process millions of Call Detail Records (CDRs) every hour, requiring extremely efficient logic.

---

## 📱 Module 14: Telecommunications & Data Usage

### **Scenario 131: Real-Time Data Cap Alerts**
**Business Need:** When a user consumes data, the system must check if they have reached 80% or 100% of their monthly limit and flag the account for an SMS alert.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PROCEDURE proc_check_data_cap (p_user_id NUMBER, p_usage_mb NUMBER) IS
        v_limit NUMBER;
        v_total NUMBER;
    BEGIN
        SELECT data_limit_mb, current_usage_mb INTO v_limit, v_total 
        FROM user_plans WHERE user_id = p_user_id;

        IF (v_total + p_usage_mb) >= v_limit THEN
            INSERT INTO sms_queue (user_id, msg) VALUES (p_user_id, '100% Data Used.');
        ELSIF (v_total + p_usage_mb) >= (v_limit * 0.8) THEN
            INSERT INTO sms_queue (user_id, msg) VALUES (p_user_id, '80% Data Used.');
        END IF;
    END;
    ```
* **Explanation:** This logic prevents "Bill Shock" for customers. It uses a procedure to evaluate usage against a threshold and populates a queue table that an external SMS gateway monitors.

---

### **Scenario 132: Calculating Peak vs. Off-Peak Call Charges**
**Business Need:** Calls made between 8 AM and 8 PM are charged at $0.10/min. Calls outside that window are $0.02/min.

* **SQL Logic:**
    ```sql
    SELECT call_id, duration_sec,
           CASE 
             WHEN TO_NUMBER(TO_CHAR(start_time, 'HH24')) BETWEEN 8 AND 19 
             THEN (duration_sec / 60) * 0.10
             ELSE (duration_sec / 60) * 0.02
           END AS call_cost
    FROM call_records;
    ```

* **Explanation:** `TO_CHAR(..., 'HH24')` extracts the hour in 24-hour format. This allows the `CASE` statement to apply different financial rules based on the time of day the resource was used.

---

### **Scenario 133: Detecting "SIM Swapping" Fraud**
**Business Need:** If a SIM card's unique ID (IMSI) changes for a specific phone number twice within 24 hours, flag it as a potential security breach.

* **SQL Logic:**
    ```sql
    SELECT phone_number, COUNT(DISTINCT imsi_id)
    FROM sim_history
    WHERE change_date > SYSDATE - 1
    GROUP BY phone_number
    HAVING COUNT(DISTINCT imsi_id) >= 2;
    ```
* **Explanation:** High-frequency changes to hardware identifiers are a major red flag for identity theft. This query identifies accounts that need immediate manual review.

---

### **Scenario 134: Finding the "Most Dropped" Cell Tower**
**Business Need:** Identify which cell towers are experiencing the highest rate of "Dropped Calls" to prioritize hardware maintenance.

* **SQL Logic:**
    ```sql
    SELECT tower_id, 
           (COUNT(CASE WHEN end_reason = 'DROPPED' THEN 1 END) / COUNT(*)) * 100 AS drop_rate
    FROM call_logs
    GROUP BY tower_id
    ORDER BY drop_rate DESC;
    ```
* **Explanation:** This uses **Conditional Aggregation**. We count only the dropped calls and divide by the total number of calls for that tower to get a percentage-based failure rate.

---

### **Scenario 135: Pruning Old Call Records (Partition Management)**
**Business Need:** Telecom logs are huge. Every month, we must drop the partition containing data older than 2 years to save disk space.

* **SQL Logic:**
    ```sql
    -- Standard DDL to drop an old partition
    ALTER TABLE call_records DROP PARTITION p_jan_2024;
    ```
* **Explanation:** Instead of running a `DELETE` command (which generates massive undo logs and is slow), dropping a **Partition** is a "Metadata-only" operation. It takes less than a second to remove millions of rows.

---

### **Scenario 136: Roaming Cost Aggregation**
**Business Need:** For users traveling abroad, aggregate their data usage by Country so it can be displayed on their monthly PDF invoice.

* **SQL Logic:**
    ```sql
    SELECT user_id, roaming_country, 
           SUM(data_kb) / 1024 AS data_mb,
           LISTAGG(network_provider, ', ') WITHIN GROUP (ORDER BY network_provider) AS providers_used
    FROM roaming_logs
    GROUP BY user_id, roaming_country;
    ```
* **Explanation:** This combines standard aggregation (`SUM`) with `LISTAGG` to show both the total volume and a readable list of international partner networks used.

---

### **Scenario 137: Identifying Frequent "Missed Call" Pairs**
**Business Need:** Find pairs of numbers that frequently have missed calls between them, which might indicate a "callback" marketing bot or a connection issue.

* **SQL Logic:**
    ```sql
    SELECT caller_id, receiver_id, COUNT(*)
    FROM call_records
    WHERE status = 'MISSED'
    GROUP BY caller_id, receiver_id
    HAVING COUNT(*) > 5;
    ```
* **Explanation:** Grouping by two columns (`caller` and `receiver`) helps identify relationship patterns in the data rather than just individual behavior.

---

### **Scenario 138: Top 3 Data Consumers per Plan**
**Business Need:** On the admin dashboard, show the top 3 users consuming the most data for each plan type (Prepaid, Postpaid, Corporate).

* **SQL Logic:**
    ```sql
    SELECT plan_type, username, total_gb
    FROM (
        SELECT plan_type, username, total_gb,
               DENSE_RANK() OVER (PARTITION BY plan_type ORDER BY total_gb DESC) as rnk
        FROM user_usage
    )
    WHERE rnk <= 3;
    ```

* **Explanation:** `DENSE_RANK` is used here to ensure that if there is a tie for the 3rd spot, both users are included without skipping the next rank.

---

### **Scenario 139: Mapping Longitude/Latitude to Service Areas**
**Business Need:** Given a customer's GPS coordinates from a signal ping, determine which service region they are in.

* **SQL Logic:**
    ```sql
    SELECT customer_id, region_name
    FROM service_regions r
    JOIN customer_pings p 
      ON p.lat BETWEEN r.min_lat AND r.max_lat
     AND p.lon BETWEEN r.min_lon AND r.max_lon;
    ```
* **Explanation:** Joins don't always use IDs. This is a **Non-Equi Join** using spatial boundaries (latitude/longitude) to link a point to a geographic polygon.

---

### **Scenario 140: Calculating "Churn Risk" (No Activity)**
**Business Need:** Find customers who have had zero calls and zero data usage in the last 60 days. These customers are likely to cancel their service (Churn).

* **SQL Logic:**
    ```sql
    SELECT user_id FROM users
    MINUS
    (SELECT user_id FROM call_records WHERE call_date > SYSDATE - 60
     UNION
     SELECT user_id FROM data_logs WHERE usage_date > SYSDATE - 60);
    ```
* **Explanation:** The `MINUS` operator is perfect for finding "Inactivity." We take the full list of users and subtract anyone who has done *anything* in the last 60 days.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 130: Levels 1-13 Complete
- [x] Scenarios 131 - 140: Telecommunications
- [ ] Scenarios 141 - 150: Insurance & Claims Processing (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 141 - 150

This module deals with long-term data tracking and complex conditional logic. Insurance databases must handle policy renewals, claim validations, and actuarial calculations.

---

## 🛡️ Module 15: Insurance & Claims Processing

### **Scenario 141: Policy Renewal Calculation**
**Business Need:** When a policy is renewed, the new premium should be 5% higher than the previous year if the customer had a claim, and 5% lower if they had zero claims (No Claims Bonus).

* **SQL Logic:**
    ```sql
    UPDATE policies p
    SET premium_amount = 
        CASE 
            WHEN EXISTS (SELECT 1 FROM claims c WHERE c.policy_id = p.policy_id AND c.claim_date > p.start_date)
            THEN premium_amount * 1.05
            ELSE premium_amount * 0.95
        END,
        start_date = SYSDATE,
        end_date = ADD_MONTHS(SYSDATE, 12)
    WHERE policy_id = :target_id;
    ```
* **Explanation:** This uses a correlated subquery inside a `CASE` statement to perform "Conditional Pricing." It rewards safe behavior and adjusts risk pricing automatically during the renewal transaction.

---

### **Scenario 142: Detecting "Double Dipping" Fraud**
**Business Need:** Flag cases where two different claims are filed for the same Incident Date and the same License Plate number.

* **SQL Logic:**
    ```sql
    SELECT license_plate, incident_date, COUNT(claim_id)
    FROM claims
    GROUP BY license_plate, incident_date
    HAVING COUNT(claim_id) > 1;
    ```
* **Explanation:** This is a fundamental fraud check. In insurance, a single accident should only result in one primary claim record per vehicle. Grouping by the unique combination of "What" and "When" exposes duplicates.

---

### **Scenario 143: Premium Calculation Function (Risk-Based)**
**Business Need:** Calculate a base premium where smokers pay a 20% surcharge and people over 60 pay an additional 15%.

* **PL/SQL Function:**
    ```sql
    CREATE OR REPLACE FUNCTION fn_calc_premium(p_base NUMBER, p_is_smoker CHAR, p_age NUMBER) 
    RETURN NUMBER IS
        v_final NUMBER := p_base;
    BEGIN
        IF p_is_smoker = 'Y' THEN v_final := v_final * 1.20; END IF;
        IF p_age > 60 THEN v_final := v_final * 1.15; END IF;
        RETURN v_final;
    END;
    ```

* **Explanation:** Encapsulating business rules in a function ensures that the "Price Logic" is consistent across the web portal, the mobile app, and the internal agent software.

---

### **Scenario 144: Automated Claim Status Workflow**
**Business Need:** When a claim is created, if the amount is under $500, it should be marked as 'AUTO-APPROVED'. Otherwise, it stays 'PENDING_REVIEW'.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_claim_workflow
    BEFORE INSERT ON claims
    FOR EACH ROW
    BEGIN
        IF :NEW.claim_amount < 500 THEN
            :NEW.status := 'AUTO-APPROVED';
        ELSE
            :NEW.status := 'PENDING_REVIEW';
        END IF;
    END;
    ```
* **Explanation:** This is known as **Straight-Through Processing (STP)**. It reduces manual work for adjusters by handling low-risk, low-value items automatically at the database level.

---

### **Scenario 145: Identifying "High-Risk" Policyholders**
**Business Need:** Find customers who have filed more than 3 claims in the last 24 months with a total payout exceeding $50,000.

* **SQL Logic:**
    ```sql
    SELECT policy_id, COUNT(*) as claim_count, SUM(paid_amount) as total_paid
    FROM claims
    WHERE claim_date > ADD_MONTHS(SYSDATE, -24)
    GROUP BY policy_id
    HAVING COUNT(*) > 3 AND SUM(paid_amount) > 50000;
    ```
* **Explanation:** Using `HAVING` with multiple conditions allows the actuary team to filter for specific risk profiles based on both frequency and severity of loss.

---

### **Scenario 146: Managing Policy Endorsements (Versioning)**
**Business Need:** When a customer changes their coverage mid-term (e.g., adding a new driver), we must keep the old version of the policy for legal audit and create a new active version.

* **PL/SQL Procedure:**
    ```sql
    PROCEDURE proc_endorse_policy(p_policy_id NUMBER) IS
    BEGIN
        -- 1. Archive current version
        INSERT INTO policy_history SELECT * FROM policies WHERE policy_id = p_policy_id;
        -- 2. Increment version and update data
        UPDATE policies SET version_no = version_no + 1, last_modified = SYSDATE 
        WHERE policy_id = p_policy_id;
    END;
    ```
* **Explanation:** This ensures **Data Traceability**. In highly regulated industries like insurance, you can never simply "overwrite" a contract; you must maintain a version history of every change.

---

### **Scenario 147: Claims Aging Report (Bucketing)**
**Business Need:** Management needs to see how many claims are currently open in 30-day "buckets" (0-30 days, 31-60 days, 61+ days).

* **SQL Logic:**
    ```sql
    SELECT 
        CASE 
            WHEN (SYSDATE - open_date) <= 30 THEN '0-30 Days'
            WHEN (SYSDATE - open_date) <= 60 THEN '31-60 Days'
            ELSE 'Over 60 Days'
        END AS age_bucket,
        COUNT(*) as total_claims
    FROM claims
    WHERE status = 'OPEN'
    GROUP BY 
        CASE 
            WHEN (SYSDATE - open_date) <= 30 THEN '0-30 Days'
            WHEN (SYSDATE - open_date) <= 60 THEN '31-60 Days'
            ELSE 'Over 60 Days'
        END;
    ```

* **Explanation:** This logic uses the same `CASE` expression in both the `SELECT` and `GROUP BY` clauses to categorize continuous data (days) into discrete categories (buckets).

---

### **Scenario 148: Validating Coverage Dates**
**Business Need:** A claim should be rejected if the `incident_date` does not fall between the policy `start_date` and `end_date`.

* **SQL Logic:**
    ```sql
    SELECT c.claim_id, 'OUTSIDE_COVERAGE' as error
    FROM claims c
    JOIN policies p ON c.policy_id = p.policy_id
    WHERE c.incident_date NOT BETWEEN p.start_date AND p.end_date;
    ```
* **Explanation:** This join validates the relationship between two entities. It’s a core requirement for claims integrity—ensuring the company only pays for events that happened while the contract was active.

---

### **Scenario 149: Loss Ratio Calculation**
**Business Need:** For each insurance product (Auto, Home, Life), calculate the Loss Ratio: `(Total Claims Paid / Total Premiums Collected)`.

* **SQL Logic:**
    ```sql
    SELECT product_type,
           (SUM(claims_paid) / SUM(premiums_collected)) * 100 as loss_ratio
    FROM finance_summary
    GROUP BY product_type;
    ```
* **Explanation:** The **Loss Ratio** is the most important metric in insurance. If it's over 100%, the company is losing money on that product line. This query provides high-level executive insight.

---

### **Scenario 150: Dependent Age Validation**
**Business Need:** In many health policies, children are only covered until age 26. Find all policies that have a dependent nearing this limit.

* **SQL Logic:**
    ```sql
    SELECT policy_id, dependent_name, dob
    FROM policy_dependents
    WHERE MONTHS_BETWEEN(SYSDATE, dob) / 12 BETWEEN 25.5 AND 26;
    ```
* **Explanation:** Using `MONTHS_BETWEEN` allows for precise tracking. Filtering for the "25.5 to 26" range helps the company send out proactive notification letters to families before the coverage drops.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 140: Levels 1-14 Complete
- [x] Scenarios 141 - 150: Insurance & Claims
- [ ] Scenarios 151 - 160: Travel & Hospitality (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 151 - 160

This module deals with "Time-Sensitive" inventory. In travel, once a plane takes off or a night passes, the unsold inventory value drops to zero, requiring smart automation.

---

## ✈️ Module 16: Travel & Hospitality

### **Scenario 151: Flight Overbooking Logic**
**Business Need:** Airlines often sell 105% of available seats because of "No-shows." We need to block bookings once the 105% threshold is reached for a specific flight.

* **PL/SQL Logic:**
    ```sql
    CREATE OR REPLACE PROCEDURE proc_book_flight(p_flight_id NUMBER) IS
        v_capacity NUMBER;
        v_booked   NUMBER;
    BEGIN
        SELECT total_seats, booked_seats INTO v_capacity, v_booked 
        FROM flights WHERE flight_id = p_flight_id FOR UPDATE;

        IF v_booked < (v_capacity * 1.05) THEN
            UPDATE flights SET booked_seats = booked_seats + 1 WHERE flight_id = p_flight_id;
            COMMIT;
        ELSE
            RAISE_APPLICATION_ERROR(-20001, 'Flight is fully committed (including overbooking).');
        END IF;
    END;
    ```
* **Explanation:** This uses **Pessimistic Locking** (`FOR UPDATE`) to ensure that two people booking at the exact same millisecond don't bypass the 105% limit.

---

### **Scenario 152: Dynamic Hotel Pricing (Yield Management)**
**Business Need:** If a hotel's occupancy is above 80%, increase the room rate by 20% for any new bookings.

* **SQL Logic:**
    ```sql
    UPDATE rooms r
    SET current_rate = base_rate * 1.20
    WHERE hotel_id = :hid
    AND (SELECT (COUNT(*)/MAX(total_rooms))*100 
         FROM bookings WHERE hotel_id = :hid AND status = 'ACTIVE') > 80;
    ```

* **Explanation:** This is a core **Revenue Management** tactic. The database evaluates the current supply-demand ratio to adjust prices in real-time, maximizing profit during peak seasons.

---

### **Scenario 153: Loyalty Points Accrual**
**Business Need:** For every $10 spent on a booking, credit the user with 1 "Loyalty Point."

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_add_loyalty_points
    AFTER UPDATE OF payment_status ON bookings
    FOR EACH ROW
    WHEN (NEW.payment_status = 'PAID')
    BEGIN
        UPDATE users 
        SET loyalty_points = loyalty_points + FLOOR(:NEW.total_amount / 10)
        WHERE user_id = :NEW.user_id;
    END;
    ```
* **Explanation:** This trigger automates the reward system. By triggering only when `payment_status` changes to 'PAID', we ensure points aren't awarded for cancelled or pending reservations.

---

### **Scenario 154: Identifying "Gap Nights" in Room Bookings**
**Business Need:** Find "un-bookable" 1-night gaps between existing reservations (e.g., a room is booked Mon-Tue and Thu-Fri, leaving Wednesday empty).

* **SQL Logic:**
    ```sql
    SELECT room_id, check_out AS gap_start, 
           LEAD(check_in) OVER (PARTITION BY room_id ORDER BY check_in) AS next_booking
    FROM reservations
    WHERE LEAD(check_in) OVER (PARTITION BY room_id ORDER BY check_in) - check_out = 1;
    ```
* **Explanation:** Using the `LEAD` function allows us to compare the current row's checkout date with the *next* row's check-in date. If the difference is exactly 1, we've identified a gap night that could be targeted with a special "Last Minute" discount.

---

### **Scenario 155: Frequent Flyer Tier Upgrades**
**Business Need:** If a user crosses 50,000 miles in a calendar year, automatically upgrade their status to 'GOLD'.

* **PL/SQL Logic:**
    ```sql
    UPDATE users u
    SET member_tier = 'GOLD'
    WHERE member_tier = 'SILVER'
    AND (SELECT SUM(miles_earned) FROM flight_history 
         WHERE user_id = u.user_id AND flight_year = 2026) >= 50000;
    ```
* **Explanation:** This logic uses a **Correlated Subquery** to aggregate performance data and update the user's metadata. This usually runs as a weekly batch process.

---

### **Scenario 156: Connecting Flight Validation**
**Business Need:** Ensure that for a connecting flight booking, the arrival time of Flight A is at least 60 minutes before the departure of Flight B.

* **SQL Logic:**
    ```sql
    SELECT 'INVALID_CONNECTION' 
    FROM flight_segments a, flight_segments b
    WHERE a.booking_id = b.booking_id
    AND a.seq_no = 1 AND b.seq_no = 2
    AND (b.departure_time - a.arrival_time) * 24 * 60 < 60;
    ```

* **Explanation:** By converting the date difference into minutes (`days * 24 * 60`), we can enforce the **Minimum Connection Time (MCT)** required for passengers to move between gates.

---

### **Scenario 157: Cleaning Up "Expired" Hold/Waitlists**
**Business Need:** When a user puts a flight on "Hold," they have 4 hours to pay. If they don't, the seat must be released.

* **SQL Logic:**
    ```sql
    UPDATE seats
    SET status = 'AVAILABLE', hold_user_id = NULL
    WHERE status = 'ON_HOLD'
    AND hold_expiry_time < SYSDATE;
    ```
* **Explanation:** This query is a standard **Inventory Release** mechanism. It ensures that seats aren't "locked" forever by users who abandoned their shopping carts.

---

### **Scenario 158: Meal Preference Aggregation for Catering**
**Business Need:** 24 hours before a flight, the kitchen needs a count of "Vegan," "Halal," and "Standard" meals required.

* **SQL Logic:**
    ```sql
    SELECT meal_code, COUNT(*) as total_requested
    FROM flight_manifest
    WHERE flight_id = :fid
    GROUP BY meal_code;
    ```
* **Explanation:** A simple `GROUP BY` provides the catering department with the exact production numbers needed for the flight's inventory.

---

### **Scenario 159: Finding "Blackout Dates" (No Discounts)**
**Business Need:** Prevent any "Promo Code" from being applied if the travel date falls on a holiday (like Christmas or New Year).

* **SQL Logic:**
    ```sql
    SELECT 'PROMO_NOT_ALLOWED' 
    FROM blackout_dates
    WHERE :travel_date BETWEEN start_date AND end_date;
    ```
* **Explanation:** This check is performed during the checkout process. If the query returns a row, the application logic rejects the discount, protecting the high-revenue holiday dates.

---

### **Scenario 160: Calculating "No-Show" Rate per Route**
**Business Need:** Identify routes where passengers frequently fail to show up, so the airline can adjust overbooking percentages.

* **SQL Logic:**
    ```sql
    SELECT departure_city, arrival_city,
           (COUNT(CASE WHEN boarded = 'N' THEN 1 END) / COUNT(*)) * 100 AS no_show_pct
    FROM flight_manifest
    GROUP BY departure_city, arrival_city;
    ```
* **Explanation:** This uses **Conditional Aggregation** to find the ratio of missed flights. High no-show rates on specific routes (e.g., business routes) allow the airline to safely sell more seats than are physically available.

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 150: Levels 1-15 Complete
- [x] Scenarios 151 - 160: Travel & Hospitality
- [ ] Scenarios 161 - 170: Manufacturing & Quality Control (Pending)

# 📂 SQL & PL/SQL Master Tutorial: Scenarios 161 - 180

This intensive module covers two diverse industries: the physical world of the factory floor and the digital world of global streaming services.

---

## 🏭 Module 17: Manufacturing & Quality Control

### **Scenario 161: Bill of Materials (BOM) Expansion**
**Business Need:** To build one 'Laptop', you need 1 'Screen', 1 'Keyboard', and 4 'Screws'. You need to calculate total parts needed for an order of 500 Laptops.

* **SQL Logic:**
    ```sql
    SELECT component_name, quantity_per_unit * 500 as total_required
    FROM bill_of_materials
    WHERE product_id = 'LAPTOP_01';
    ```
* **Explanation:** This is a simple multiplication against a "Recipe" table (BOM). In advanced cases, this uses Recursive CTEs if components have their own sub-components.

---

### **Scenario 162: Tracking Machine Downtime**
**Business Need:** Calculate the total "Downtime" for a specific assembly line robot today.

* **SQL Logic:**
    ```sql
    SELECT machine_id, SUM(end_time - start_time) * 24 * 60 as downtime_minutes
    FROM machine_logs
    WHERE status = 'DOWN' AND TRUNC(start_time) = TRUNC(SYSDATE)
    GROUP BY machine_id;
    ```
* **Explanation:** By filtering for 'DOWN' status and converting the date difference to minutes, we get a key metric for **OEE (Overall Equipment Effectiveness)**.

---

### **Scenario 163: Quality Control (QC) Failure Alerts**
**Business Need:** If more than 5% of items in a batch fail the "Weight Test," immediately halt the production line.

* **PL/SQL Trigger:**
    ```sql
    CREATE OR REPLACE TRIGGER trg_qc_fail_threshold
    AFTER INSERT ON qc_results
    DECLARE
        v_fail_rate NUMBER;
    BEGIN
        SELECT (COUNT(CASE WHEN result = 'FAIL' THEN 1 END) / COUNT(*)) * 100 
        INTO v_fail_rate FROM qc_results WHERE batch_id = :NEW.batch_id;

        IF v_fail_rate > 5 THEN
            UPDATE production_lines SET status = 'HALTED' WHERE line_id = :NEW.line_id;
        END IF;
    END;
    ```

* **Explanation:** This trigger enforces real-time safety and quality standards, preventing the waste of raw materials on a faulty line.

---

### **Scenario 164: Inventory "Work-in-Progress" (WIP) Tracking**
**Business Need:** Show how many units are currently at each station (Assembly, Painting, Testing).

* **SQL Logic:**
    ```sql
    SELECT station_name, COUNT(unit_id) as units_in_progress
    FROM manufacturing_units
    WHERE status = 'IN_PROGRESS'
    GROUP BY station_name;
    ```
* **Explanation:** This provides a snapshot of the factory floor, helping managers identify "bottlenecks" where units are piling up.

---

### **Scenario 165: Raw Material Expiry Warning**
**Business Need:** Find chemicals or perishable raw materials that will expire in the next 30 days.

* **SQL Logic:**
    ```sql
    SELECT material_name, batch_no, expiry_date
    FROM raw_materials
    WHERE expiry_date BETWEEN SYSDATE AND SYSDATE + 30;
    ```

---

### **Scenario 166: Traceability (Serial Number Search)**
**Business Need:** A customer reports a faulty battery. Find which "Production Batch" that specific battery came from and what date it was manufactured.

* **SQL Logic:**
    ```sql
    SELECT batch_id, manufacture_date, supplier_id
    FROM production_history
    WHERE serial_number = :sn;
    ```
* **Explanation:** **Traceability** is vital for recalls. This query links a finished product back to its origin data.

---

### **Scenario 167: Average Cycle Time per Shift**
**Business Need:** Compare the speed of the "Day Shift" vs. the "Night Shift."

* **SQL Logic:**
    ```sql
    SELECT shift_name, AVG(completion_time - start_time) * 24 * 60 as avg_cycle_min
    FROM production_tasks
    GROUP BY shift_name;
    ```

---

### **Scenario 168: Low Stock "Pull" System (Kanban)**
**Business Need:** When a station uses a part, check if the "Bin" is empty and signal the warehouse for a refill.

* **PL/SQL Procedure:**
    ```sql
    IF v_current_bin_qty = 0 THEN
        INSERT INTO warehouse_requests (part_id, station_id, priority)
        VALUES (v_part, v_station, 'HIGH');
    END IF;
    ```

---

### **Scenario 169: Supplier Lead-Time Analysis**
**Business Need:** Which suppliers take the longest to deliver parts after an order is placed?

* **SQL Logic:**
    ```sql
    SELECT supplier_name, AVG(delivery_date - order_date) as avg_lead_days
    FROM purchase_orders
    GROUP BY supplier_name ORDER BY avg_lead_days DESC;
    ```

---

### **Scenario 170: Predictive Maintenance (Usage Thresholds)**
**Business Need:** A machine needs service every 10,000 cycles. Find machines that are at 9,500+ cycles.

* **SQL Logic:**
    ```sql
    SELECT machine_id, current_cycles 
    FROM machines WHERE current_cycles >= 9500;
    ```

---

## 🎬 Module 18: Media, Streaming & Content Management

### **Scenario 171: "Continue Watching" Logic**
**Business Need:** Save the exact second a user stops watching a movie so they can resume later.

* **SQL Logic:**
    ```sql
    MERGE INTO user_playback p
    USING (SELECT :uid as u, :mid as m, :ts as t FROM dual) src
    ON (p.user_id = src.u AND p.movie_id = src.m)
    WHEN MATCHED THEN UPDATE SET last_timestamp = src.t, updated_at = SYSDATE
    WHEN NOT MATCHED THEN INSERT (user_id, movie_id, last_timestamp) VALUES (src.u, src.m, src.t);
    ```
* **Explanation:** The `MERGE` statement (Upsert) is perfect here. It updates the timestamp if the record exists, or creates a new one if it's the user's first time watching.

---

### **Scenario 172: Subscription Paywall Check**
**Business Need:** Prevent a user from playing "Premium" content if their subscription has expired.

* **SQL Logic:**
    ```sql
    SELECT status FROM subscriptions 
    WHERE user_id = :uid AND end_date > SYSDATE AND plan_type = 'PREMIUM';
    ```

---

### **Scenario 173: Calculating "Binge-Watching" Metrics**
**Business Need:** Find users who watched more than 5 episodes of the same show in a single day.

* **SQL Logic:**
    ```sql
    SELECT user_id, series_id, COUNT(*)
    FROM watch_history
    WHERE TRUNC(watch_date) = TRUNC(SYSDATE)
    GROUP BY user_id, series_id
    HAVING COUNT(*) > 5;
    ```

---

### **Scenario 174: Content Recommendation (Genre Affinity)**
**Business Need:** Find a user's favorite genre based on their watch history to populate the "Recommended for You" section.

* **SQL Logic:**
    ```sql
    SELECT genre_name FROM (
        SELECT genre_name, COUNT(*) 
        FROM watch_history h JOIN movies m ON h.movie_id = m.id
        WHERE h.user_id = :uid
        GROUP BY genre_name ORDER BY COUNT(*) DESC
    ) WHERE ROWNUM = 1;
    ```

* **Explanation:** By joining history with metadata, we can identify user preferences and drive the discovery algorithm.

---

### **Scenario 175: Regional Content Licensing (Geo-Blocking)**
**Business Need:** A movie is only licensed for 'USA' and 'Canada'. Check if the user's current country allows playback.

* **SQL Logic:**
    ```sql
    SELECT COUNT(*) FROM content_licenses 
    WHERE movie_id = :mid AND country_code = :user_country;
    ```

---

### **Scenario 176: Trending Now (Velocity Ranking)**
**Business Need:** Find the top 10 movies that gained the most new viewers in the last 1 hour.

* **SQL Logic:**
    ```sql
    SELECT movie_id, COUNT(*) 
    FROM watch_history 
    WHERE watch_date > SYSDATE - 1/24
    GROUP BY movie_id ORDER BY COUNT(*) DESC
    FETCH FIRST 10 ROWS ONLY;
    ```

---

### **Scenario 177: Content Expiry Notifications**
**Business Need:** Alert users if a movie on their "Watchlist" is leaving the platform in the next 7 days.

* **SQL Logic:**
    ```sql
    SELECT w.user_id, m.title
    FROM watchlist w JOIN movies m ON w.movie_id = m.id
    WHERE m.license_end_date BETWEEN SYSDATE AND SYSDATE + 7;
    ```

---

### **Scenario 178: Multi-Device Session Limit**
**Business Need:** Prevent a user from having more than 3 active streams at the same time.

* **SQL Logic:**
    ```sql
    SELECT COUNT(*) FROM active_sessions 
    WHERE user_id = :uid AND status = 'STREAMING';
    -- If result >= 3, block new stream.
    ```

---

### **Scenario 179: Subtitle/Audio Language Availability**
**Business Need:** List all available audio languages for a specific film.

* **SQL Logic:**
    ```sql
    SELECT language_code FROM media_assets 
    WHERE movie_id = :mid AND asset_type = 'AUDIO';
    ```

---

### **Scenario 180: Revenue Share Calculation (Royalty)**
**Business Need:** Calculate how much to pay a Studio if they get $0.01 per view.

* **SQL Logic:**
    ```sql
    SELECT studio_name, COUNT(h.id) * 0.01 as payout
    FROM watch_history h JOIN movies m ON h.movie_id = m.id
    GROUP BY studio_name;
    ```

---

### 🚀 Progress Tracker
- [x] Scenarios 1 - 160: Levels 1-16 Complete
- [x] Scenarios 161 - 170: Manufacturing
- [x] Scenarios 171 - 180: Media & Streaming
- [ ] Scenarios 181 - 200: Final Mastery & Edge Cases (Pending)


# 📂 SQL & PL/SQL Master Tutorial: Scenarios 181 - 200

This final module focuses on advanced logic, recursive structures, and the "under-the-hood" maintenance required for a healthy database.

---

## 🏗️ Module 19: Advanced Logic & Edge Cases

### **Scenario 181: Recursive Search (Part-of-Speech / BOM)**
**Business Need:** In a manufacturing assembly, a 'Car' contains an 'Engine', and an 'Engine' contains a 'Piston'. Find all sub-components at every level for a 'Car'.

* **SQL Logic:**
    ```sql
    SELECT parent_part, child_part, LEVEL
    FROM components
    START WITH parent_part = 'CAR'
    CONNECT BY PRIOR child_part = parent_part;
    ```

* **Explanation:** This uses **Recursive Hierarchical Queries**. It follows the "bloodline" of a product down to its smallest screw, no matter how many levels deep it goes.

---

### **Scenario 182: Finding Gaps in a Sequence (Gap Analysis)**
**Business Need:** An accounting audit reveals that some check numbers are missing from the `transactions` table. Identify the missing numbers.

* **SQL Logic:**
    ```sql
    SELECT prev_val + 1 AS gap_start, next_val - 1 AS gap_end
    FROM (
      SELECT check_no AS prev_val, 
             LEAD(check_no) OVER (ORDER BY check_no) AS next_val
      FROM transactions
    )
    WHERE next_val - prev_val > 1;
    ```
* **Explanation:** By comparing the current row to the next row using `LEAD`, we can find anywhere the jump is greater than 1, signaling a missing record.

---

### **Scenario 183: Islands and Bridges (Consecutive Data)**
**Business Need:** Find the longest "Winning Streak" (consecutive days with profit) for a stock.

* **SQL Logic:**
    ```sql
    SELECT stock_id, COUNT(*) as streak_length
    FROM (
      SELECT stock_id, trade_date,
             trade_date - ROW_NUMBER() OVER (PARTITION BY stock_id ORDER BY trade_date) as grp
      FROM trades WHERE result = 'PROFIT'
    )
    GROUP BY stock_id, grp;
    ```
* **Explanation:** This is the famous **Islands and Lakes** problem. By subtracting a sequence number from a date, consecutive dates fall into the same "group" (grp), allowing us to count them.

---

### **Scenario 184: Handling NULLs in Calculations (COALESCE)**
**Business Need:** Calculate `Total = Salary + Commission`. If Commission is NULL, the result should not be NULL.

* **SQL Logic:**
    ```sql
    SELECT first_name, salary + COALESCE(commission, 0) as total_pay
    FROM employees;
    ```

---

### **Scenario 185: Pivoting Data (Columns to Rows)**
**Business Need:** You have a table with columns `Q1_Sales`, `Q2_Sales`, `Q3_Sales`. You need to transform this into a vertical format for a bar chart.

* **SQL Logic:**
    ```sql
    SELECT * FROM sales_data
    UNPIVOT (amount FOR quarter IN (Q1_SALES, Q2_SALES, Q3_SALES));
    ```

---

### **Scenario 186: Bulk Dynamic SQL (EXECUTE IMMEDIATE)**
**Business Need:** You need to drop all tables in a schema that start with the prefix 'TEMP_'.

* **PL/SQL Logic:**
    ```sql
    BEGIN
      FOR r IN (SELECT table_name FROM user_tables WHERE table_name LIKE 'TEMP_%') LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || r.table_name;
      END LOOP;
    END;
    ```

---

### **Scenario 187: Working with JSON Data**
**Business Need:** A mobile app stores user preferences as a JSON blob. Extract the 'theme_color' from the JSON.

* **SQL Logic:**
    ```sql
    SELECT JSON_VALUE(settings_json, '$.theme_color') 
    FROM user_profiles;
    ```

---

### **Scenario 188: Efficiently Removing All Data (TRUNCATE vs DELETE)**
**Business Need:** You need to clear a staging table with 10 million rows before a new data load.

* **SQL Logic:**
    ```sql
    TRUNCATE TABLE staging_data;
    ```
* **Explanation:** `TRUNCATE` is a DDL command. It doesn't generate "Undo" data for every row, making it nearly instantaneous compared to `DELETE`.

---

### **Scenario 189: Identifying Database Locks**
**Business Need:** Two users are stuck; their screens are frozen. Find out who is blocking whom.

* **SQL Logic:**
    ```sql
    SELECT holding_session, waiting_session, mode_held 
    FROM dba_waiters;
    ```


---

### **Scenario 190: Flashback Query (Time Travel)**
**Business Need:** A user accidentally deleted rows 5 minutes ago. You need to see the data as it existed *before* the deletion.

* **SQL Logic:**
    ```sql
    SELECT * FROM employees AS OF TIMESTAMP (SYSTATE - 5/1440);
    ```

---

## 🛠️ Module 20: Maintenance & Final Mastery

### **Scenario 191: Rebuilding Unusable Indexes**
**Business Need:** After a massive data load, several indexes have become "Unusable" and are slowing down queries.

* **SQL Logic:**
    ```sql
    ALTER INDEX idx_emp_id REBUILD;
    ```

---

### **Scenario 192: Finding Redundant Indexes**
**Business Need:** You suspect there are too many indexes on the `orders` table. Find indexes that have never been used.

* **SQL Logic:**
    ```sql
    SELECT index_name FROM v$object_usage WHERE used = 'NO';
    ```

---

### **Scenario 193: Monitoring Tablespace Usage**
**Business Need:** Check if the database disk space is almost full.

* **SQL Logic:**
    ```sql
    SELECT tablespace_name, used_percent FROM dba_tablespace_usage_metrics;
    ```

---

### **Scenario 194: Generating Random Sample Data**
**Business Need:** You need to pick 100 random customers for a survey.

* **SQL Logic:**
    ```sql
    SELECT * FROM (SELECT * FROM customers ORDER BY DBMS_RANDOM.VALUE)
    WHERE ROWNUM <= 100;
    ```

---

### **Scenario 195: Tracking Table Size (Bytes)**
**Business Need:** Find the top 5 largest tables in your database.

* **SQL Logic:**
    ```sql
    SELECT segment_name, bytes/1024/1024 as MB 
    FROM user_segments WHERE segment_type = 'TABLE'
    ORDER BY MB DESC FETCH FIRST 5 ROWS ONLY;
    ```

---

### **Scenario 196: Database Uptime Check**
**Business Need:** How long has the database instance been running?

* **SQL Logic:**
    ```sql
    SELECT instance_name, startup_time FROM v$instance;
    ```

---

### **Scenario 197: Finding "Invalid" Objects**
**Business Need:** After a deployment, check if any views or procedures are broken due to table changes.

* **SQL Logic:**
    ```sql
    SELECT object_name, object_type FROM user_objects WHERE status = 'INVALID';
    ```

---

### **Scenario 198: Viewing Current Session Users**
**Business Need:** See how many users are currently connected to the database.

* **SQL Logic:**
    ```sql
    SELECT username, program, machine FROM v$session WHERE type = 'USER';
    ```

---

### **Scenario 199: Exporting Data to CSV (SQLcl/SQLPlus)**
**Business Need:** Quickly dump a table into a CSV file for a manager.

* **Command:**
    ```sql
    SET SQLFORMAT csv
    SPOOL output.csv
    SELECT * FROM employees;
    SPOOL OFF
    ```

---

### **Scenario 200: Final Mastery - The "Golden Rule"**
**Business Need:** To become a true SQL Master.

* **Advice:** *Always write SQL that is readable first, and optimized second. A query that no one can maintain is a bug waiting to happen.*

---

### 🏆 Final Progress Tracker: 100% Complete
- [x] Level 1-20: All 200 Scenarios Completed!
- [x] Multi-Industry Mastery (Banking, Healthcare, HR, Logistics, Telecom, etc.)
- [x] Performance Tuning & Security
- [x] Advanced PL/SQL Logic


# 🚀 SQL & PL/SQL Ultimate Practice Lab

This lab provides a complete enterprise-grade database schema covering **HR, Finance, E-commerce, Logistics, and Healthcare**. Use this to practice all 200+ scenarios.

---

## 🛠️ Phase 1: Data Definition Language (DDL)
*Run this script first to create the table structures.*

```sql
-- =============================================
-- 1. CORE INFRASTRUCTURE
-- =============================================
CREATE TABLE departments (
    dept_id NUMBER PRIMARY KEY,
    dept_name VARCHAR2(50) NOT NULL
);

-- =============================================
-- 2. HUMAN RESOURCES & PAYROLL
-- =============================================
CREATE TABLE employees (
    employee_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(50),
    last_name VARCHAR2(50),
    email VARCHAR2(100),
    salary NUMBER(10,2) CHECK (salary > 0),
    hire_date DATE,
    manager_id NUMBER,
    dept_id NUMBER REFERENCES departments(dept_id),
    job_level NUMBER,
    grade CHAR(1),
    bank_account_no VARCHAR2(20),
    ssn VARCHAR2(11) UNIQUE
);

-- =============================================
-- 3. INVENTORY & E-COMMERCE
-- =============================================
CREATE TABLE products (
    product_id NUMBER PRIMARY KEY,
    product_name VARCHAR2(100),
    category_id NUMBER,
    price NUMBER(10,2),
    stock_qty NUMBER DEFAULT 0,
    min_reorder_level NUMBER,
    status VARCHAR2(20) -- 'AVAILABLE', 'OUT_OF_STOCK'
);

CREATE TABLE warehouse_inventory (
    bin_id NUMBER PRIMARY KEY,
    product_id NUMBER REFERENCES products(product_id),
    zone VARCHAR2(10), -- 'FRONT', 'BACK'
    stock_qty NUMBER,
    last_inspected DATE
);

CREATE TABLE customers (
    customer_id NUMBER PRIMARY KEY,
    customer_name VARCHAR2(100),
    email VARCHAR2(100),
    country_id VARCHAR2(5),
    total_spend NUMBER(15,2) DEFAULT 0
);

CREATE TABLE orders (
    order_id NUMBER PRIMARY KEY,
    customer_id NUMBER REFERENCES customers(customer_id),
    order_date DATE DEFAULT SYSDATE,
    status VARCHAR2(20), -- 'PAID', 'SHIPPED', 'CANCELLED'
    total_amount NUMBER(12,2)
);

CREATE TABLE order_items (
    item_id NUMBER PRIMARY KEY,
    order_id NUMBER REFERENCES orders(order_id),
    product_id NUMBER REFERENCES products(product_id),
    quantity NUMBER,
    unit_price NUMBER(10,2)
);

-- =============================================
-- 4. HEALTHCARE SYSTEMS
-- =============================================
CREATE TABLE patients (
    patient_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(50),
    last_name VARCHAR2(50),
    dob DATE,
    ssn VARCHAR2(11)
);

CREATE TABLE appointments (
    appt_id NUMBER PRIMARY KEY,
    patient_id NUMBER REFERENCES patients(patient_id),
    doctor_id NUMBER,
    appt_date DATE,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR2(20)
);

-- =============================================
-- 5. SYSTEM AUDIT LOGS
-- =============================================
CREATE TABLE balance_audit_log (
    log_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    emp_id NUMBER,
    old_salary NUMBER,
    new_salary NUMBER,
    change_date TIMESTAMP,
    db_user VARCHAR2(50)
);

-- ======================================================
-- 1. DEPARTMENTS (Infrastructure)
-- ======================================================
INSERT INTO departments (dept_id, dept_name) VALUES (10, 'Finance');
INSERT INTO departments (dept_id, dept_name) VALUES (20, 'IT');
INSERT INTO departments (dept_id, dept_name) VALUES (30, 'Sales');
INSERT INTO departments (dept_id, dept_name) VALUES (40, 'Logistics');
INSERT INTO departments (dept_id, dept_name) VALUES (50, 'Marketing');
INSERT INTO departments (dept_id, dept_name) VALUES (60, 'Healthcare Admin');

-- ======================================================
-- 2. EMPLOYEES (HR Hierarchy)
-- ======================================================
-- Top Level
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (101, 'Alice', 'CEO', 'alice@corp.com', 250000, TO_DATE('2010-01-01','YYYY-MM-DD'), NULL, 10, 5, 'A', 'SSN-000-01');

-- Managers (Reporting to 101)
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (102, 'Bob', 'IT_Mgr', 'bob@corp.com', 120000, TO_DATE('2015-05-10','YYYY-MM-DD'), 101, 20, 3, 'B', 'SSN-000-02');
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (104, 'David', 'Sales_Mgr', 'david@corp.com', 115000, TO_DATE('2018-03-12','YYYY-MM-DD'), 101, 30, 3, 'B', 'SSN-000-04');
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (105, 'Eve', 'Log_Mgr', 'eve@corp.com', 110000, TO_DATE('2019-11-20','YYYY-MM-DD'), 101, 40, 3, 'B', 'SSN-000-05');

-- Staff (Reporting to Managers)
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (103, 'Charlie', 'Dev', 'charlie@corp.com', 85000, TO_DATE('2023-06-15','YYYY-MM-DD'), 102, 20, 1, 'C', 'SSN-000-03');
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (106, 'Frank', 'SysAdmin', 'frank@corp.com', 75000, TO_DATE('2024-01-05','YYYY-MM-DD'), 102, 20, 1, 'C', 'SSN-000-06');
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (107, 'Grace', 'Sales_Exec', 'grace@corp.com', 65000, TO_DATE('2024-02-15','YYYY-MM-DD'), 104, 30, 1, 'C', 'SSN-000-07');
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (108, 'Hank', 'Analyst', 'hank@corp.com', 68000, TO_DATE('2023-08-22','YYYY-MM-DD'), 104, 10, 1, 'C', 'SSN-000-08');

-- Edge Case: No Department
INSERT INTO employees (employee_id, first_name, last_name, email, salary, hire_date, manager_id, dept_id, job_level, grade, ssn)
VALUES (109, 'Ivy', 'Contractor', 'ivy@external.com', 55000, TO_DATE('2025-01-01','YYYY-MM-DD'), 101, NULL, 1, 'D', 'SSN-000-09');

-- ======================================================
-- 3. PRODUCTS (Inventory)
-- ======================================================
INSERT INTO products VALUES (501, 'Laptop Pro', 1, 1500.00, 45, 10, 'AVAILABLE');
INSERT INTO products VALUES (502, 'Smartphone X', 1, 900.00, 4, 15, 'AVAILABLE'); -- Under Min Level
INSERT INTO products VALUES (503, 'Mechanical Keyboard', 2, 120.00, 150, 20, 'AVAILABLE');
INSERT INTO products VALUES (504, '4K Monitor', 2, 400.00, 0, 5, 'OUT_OF_STOCK');
INSERT INTO products VALUES (505, 'Wireless Mouse', 2, 50.00, 200, 30, 'AVAILABLE');
INSERT INTO products VALUES (506, 'USB-C Cable', 3, 25.00, 500, 100, 'AVAILABLE');

-- ======================================================
-- 4. WAREHOUSE (Logistics)
-- ======================================================
INSERT INTO warehouse_inventory VALUES (1, 501, 'BACK', 45, SYSDATE - 10);
INSERT INTO warehouse_inventory VALUES (2, 502, 'FRONT', 4, SYSDATE - 2);
INSERT INTO warehouse_inventory VALUES (3, 503, 'BACK', 150, SYSDATE - 30);
INSERT INTO warehouse_inventory VALUES (4, 505, 'FRONT', 200, SYSDATE - 5);

-- ======================================================
-- 5. CUSTOMERS & ORDERS (E-commerce)
-- ======================================================
INSERT INTO customers VALUES (1, 'Mark Cuban', 'mark@shark.com', 'US', 50000);
INSERT INTO customers VALUES (2, 'Sara Blake', 'sara@spanx.com', 'US', 15000);
INSERT INTO customers VALUES (3, 'Elon Musk', 'elon@mars.com', 'ZA', 99000);

INSERT INTO orders VALUES (2001, 1, SYSDATE - 10, 'PAID', 1500.00);
INSERT INTO orders VALUES (2002, 2, SYSDATE - 5, 'SHIPPED', 900.00);
INSERT INTO orders VALUES (2003, 3, SYSDATE - 1, 'PAID', 120.00);

INSERT INTO order_items VALUES (1, 2001, 501, 1, 1500.00);
INSERT INTO order_items VALUES (2, 2002, 502, 1, 900.00);
INSERT INTO order_items VALUES (3, 2003, 503, 1, 120.00);

-- ======================================================
-- 6. PATIENTS & APPOINTMENTS (Healthcare)
-- ======================================================
INSERT INTO patients VALUES (1, 'John', 'Doe', TO_DATE('1985-05-20','YYYY-MM-DD'), '999-01-01');
INSERT INTO patients VALUES (2, 'Jane', 'Smith', TO_DATE('1990-10-10','YYYY-MM-DD'), '999-02-02');
INSERT INTO patients VALUES (3, 'Robert', 'Brown', TO_DATE('1975-03-15','YYYY-MM-DD'), '999-03-03');

-- Dr. 777 Schedule (Testing Overlaps)
INSERT INTO appointments VALUES (9001, 1, 777, TRUNC(SYSDATE), TO_TIMESTAMP('09:00','HH24:MI'), TO_TIMESTAMP('10:00','HH24:MI'), 'CONFIRMED');
INSERT INTO appointments VALUES (9002, 2, 777, TRUNC(SYSDATE), TO_TIMESTAMP('10:30','HH24:MI'), TO_TIMESTAMP('11:30','HH24:MI'), 'CONFIRMED');
INSERT INTO appointments VALUES (9003, 3, 777, TRUNC(SYSDATE), TO_TIMESTAMP('14:00','HH24:MI'), TO_TIMESTAMP('15:00','HH24:MI'), 'PENDING');

COMMIT;