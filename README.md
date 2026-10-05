# Campus Placement and Recruitment Drive Management System

A production-grade desktop management application for university placement cells, built with **Java 17/21, Swing, JDBC, HikariCP, MySQL 8 and Maven**.

```
Swing UI  →  Service Layer (RBAC, transactions)  →  DAO  →  HikariCP / JDBC  →  MySQL Server
```

> **XAMPP is NOT required.** The app does not rely on Apache, phpMyAdmin, Tomcat, or web servers. The only runtime requirement is a standard **MySQL Server** installation.

---

## 1. System Requirements

| Software | Version | Purpose |
|---|---|---|
| Java JDK | 17 or newer (tested on 17, 21, and 26) | Application runtime and compilation |
| Apache Maven | 3.8 or newer | Dependency resolution and automated packaging |
| MySQL Server | 8.0 or newer | Relational database persistence |
| Internet connection | First build only | Maven dependency download (`mysql-connector-j`, HikariCP, etc.) |

---

## 2. Java & Maven Installation

- **Windows / macOS:** Install JDK 17+ (e.g. [Eclipse Temurin](https://adoptium.net)). On Windows, check "Set JAVA_HOME" and "Add to PATH".
- **Ubuntu / Debian:** `sudo apt install openjdk-17-jdk maven`

Verify your installation:

```bash
java -version
javac -version
mvn -v
```

All commands should report Java 17 or higher.

---

## 3. MySQL Server Setup

- **Windows:** Download MySQL Installer from [dev.mysql.com](https://dev.mysql.com/downloads/installer/) and install MySQL Server 8.x.
- **macOS:** `brew install mysql && brew services start mysql`
- **Ubuntu / Debian:** `sudo apt install mysql-server && sudo systemctl start mysql`

Verify MySQL is running:

```bash
mysql -u root -p -e "SELECT VERSION();"
```

---

## 4. Database Setup & Initialization

Everything (schema, tables, constraints, indexes, sample data, views, triggers, stored procedures, and least-privilege user creation) is packaged in `database/complete_database.sql`.

Run the script from the project root using one of the following methods:

**Command Line (All Platforms):**
```bash
mysql -u root -p < database/complete_database.sql
```

**Windows Command Prompt:**
```bat
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\complete_database.sql
```

**PowerShell:**
```powershell
Get-Content database\complete_database.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

**MySQL Workbench:**
1. Connect to your MySQL server.
2. Open `database/complete_database.sql`.
3. Click the lightning-bolt (**Execute**) icon.

### Verification
```bash
mysql -u root -p -e "USE campus_placement; SELECT COUNT(*) AS students FROM students; SHOW TRIGGERS;"
```
You should see 24 sample students and 9 integrity triggers.

---

## 5. Database Configuration (`db.properties`)

On a clean clone, copy the configuration template `db.properties.example` to `db.properties`:

**Windows:**
```bat
copy db.properties.example db.properties
```

**Linux / macOS:**
```bash
cp db.properties.example db.properties
```

Edit `db.properties` with your MySQL credentials:

```properties
db.url=jdbc:mysql://localhost:3306/campus_placement?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### Least-Privilege Application User (Recommended)
For secure multi-client deployment, run `database/08_least_privilege_user.sql` and connect using the limited application account:
```properties
db.username=placement_app
db.password=Placement@123
```
This user is restricted to `SELECT`, `INSERT`, `UPDATE`, `DELETE`, and `EXECUTE` on `campus_placement.*` and cannot drop tables or alter schema definitions.

*Note: Environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`) can also be used to override settings dynamically.*

---

## 6. Build & Automated Tests

### Run Automated Unit and Integration Tests
```bash
mvn test
```
The test suite includes 36+ automated tests verifying:
- PBKDF2-HMAC-SHA256 password hashing and dummy hash verification
- Field validators (email, phone, CGPA, dates)
- CSV formula injection protection (CWE-1236) while preserving valid negative numbers
- Role-based authorization & BOLA defenses (`StudentService`, `EligibilityService`, `SelectionService`, `ApplicationService`, `OfferService`)
- Search filtering across all data entities

### Package Runnable JAR
```bash
mvn clean package
```
This produces `target/campus-placement.jar`, a shaded executable containing all dependencies and JDBC drivers.

---

## 7. Running the Application

### Option A: Portable Launchers
- **Windows:** Double-click `run.bat` or run:
  ```bat
  run.bat
  ```
- **Linux / macOS:**
  ```bash
  chmod +x run.sh
  ./run.sh
  ```

### Option B: Direct Java Command
```bash
java -jar target/campus-placement.jar
```

On the sign-in screen, click **Test database connection** to confirm connectivity to your MySQL instance.

---

## 8. Sample Credentials

| Role | Username | Password | Notes |
|---|---|---|---|
| Placement Officer | `officer` | `Officer@123` | Full placement management access |
| Placement Officer | `tpo.assistant` | `Officer@123` | Secondary officer account |
| Student | `21CSE001` | `Student@123` | Placed (accepted Northwind offer) |
| Student | `21CSE003` | `Student@123` | Has pending offer from Meridian Analytics |
| Student | `21ECE003` | `Student@123` | Has pending offer from Helix Semiconductors |
| Student | `21IT001` | `Student@123` | Shortlisted in Kestrel Systems |
| Student | Any Student ID | `Student@123` | Default student password |

> **Security Note:** Default accounts are prompted to update passwords on initial login. New passwords can also be configured under **My Profile**. Account lockout activates after 5 consecutive failed login attempts.

---

## 9. Key Features & Architecture

- **Role-Based Access Control (RBAC):** Officer (12 management views) and Student (6 self-service views) interfaces backed by service-layer access checks.
- **Eligibility Engine:** Evaluates minimum CGPA, backlog thresholds, eligible departments, graduation year, and required student skills.
- **Transactional Application Flow:** Application submission, withdrawal, and offer responses run in atomic JDBC transactions (`REPEATABLE READ`).
- **Ordered Selection Rounds:** Selection rounds enforce strict sequence progression (both in service logic and via database triggers).
- **Offer Lifecycle:** Strict constraints prevent multiple offer acceptances and ensure offers can only be issued to shortlisted/selected candidates.
- **Reporting & Export:** 16 SQL analytics reports with sanitized CSV export (formula injection defended).

---

## 10. Troubleshooting

| Problem | Fix |
|---|---|
| "Cannot connect to the MySQL server…" | Ensure MySQL is running. Verify `db.url` in `db.properties`. |
| "MySQL rejected the username or password in db.properties." | Verify credentials in `db.properties` or environment variables. |
| "Database configuration file 'db.properties' was not found" | Copy `db.properties.example` to `db.properties`. |
| "The campus_placement database does not exist" | Run `database/complete_database.sql` to initialize the database. |
| `Public Key Retrieval is not allowed` | Ensure `allowPublicKeyRetrieval=true` is present in `db.url`. |
| Account temporarily locked | Wait 15 minutes or contact placement administrator to reset failed attempt counter. |

---

## License

This project is open source and available under the [MIT License](LICENSE).
