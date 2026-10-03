# Campus Placement and Recruitment Drive Management System

A desktop application for a university placement office, built with **Java 17, Swing, JDBC, MySQL 8 and Maven**.

```
Swing UI  →  Service (business rules, transactions)  →  DAO  →  JDBC  →  MySQL Server
```

> **XAMPP is NOT required.** The app does not use XAMPP, Apache, phpMyAdmin, Tomcat or any web server.
> The only thing it needs at runtime is a normal **MySQL Server** installation.

---

## 1. Requirements

| Software | Version | Purpose |
|---|---|---|
| Java JDK | 17 or newer (compiled for 17, tested on 21) | Build and run the app |
| Apache Maven | 3.8 or newer | Build the project (downloads the MySQL JDBC driver) |
| MySQL Server | 8.0 or newer | Database |
| Internet connection | first build only | Maven downloads its plugins and `mysql-connector-j` |

---

## 2. Install Java

- **Windows / macOS:** install a JDK 17+ (for example Eclipse Temurin from https://adoptium.net). On Windows, tick
  "Set JAVA_HOME" and "Add to PATH" during installation.
- **Ubuntu / Debian:** `sudo apt install openjdk-17-jdk`

Check it:

```bash
java -version
javac -version
```

Both must report version 17 or higher.

Install Maven from https://maven.apache.org/download.cgi (unzip it and add its `bin` folder to `PATH`), or with
`sudo apt install maven` / `brew install maven`. Check with `mvn -v`.

---

## 3. Install MySQL Server

- **Windows:** download *MySQL Installer* from https://dev.mysql.com/downloads/installer/ and install
  **MySQL Server 8.x** (MySQL Workbench is optional but handy). Choose a root password during setup and remember it.
  The installer registers MySQL as a Windows service that starts automatically.
- **macOS:** `brew install mysql` then `brew services start mysql`, or use the DMG from https://dev.mysql.com/downloads/mysql/.
- **Ubuntu / Debian:** `sudo apt install mysql-server` then `sudo systemctl start mysql`.

Check the server is running:

```bash
mysql -u root -p -e "SELECT VERSION();"
```

---

## 4. Create the database

Everything (database, tables, constraints, indexes, sample data, views, triggers and procedures) is created by one script:
`database/complete_database.sql`. It drops and recreates the `campus_placement` database, so it can be re-run any time
to reset the data.

## 5. Run the SQL script

From the project folder, choose **one** of these:

**Command line (all platforms)**

```bash
mysql -u root -p < database/complete_database.sql
```

On Windows Command Prompt, if `mysql` is not on the PATH, use the full path, for example:

```bat
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\complete_database.sql
```

(In PowerShell use: `Get-Content database\complete_database.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p`)

**MySQL Workbench**

1. Connect to your local server.
2. *File → Open SQL Script…* → choose `database/complete_database.sql`.
3. Click the lightning-bolt (Execute) button.

**Verify**

```bash
mysql -u root -p -e "USE campus_placement; SELECT COUNT(*) AS students FROM students; SHOW TRIGGERS;"
```

You should see 24 students and 9 triggers.

The numbered scripts `01_…` to `06_…` contain the same content split by topic (database, tables, constraints and indexes,
sample data, views, procedures and triggers). `07_reports.sql` contains the 16 report queries; it only reads data.

> Sample-data dates are relative to the day you run the script (`CURDATE()`), so the "open" drives are always open.

---

## 6. Configure `db.properties`

Edit `src/main/resources/db.properties` **before building**:

```properties
db.url=jdbc:mysql://localhost:3306/campus_placement?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

Usually only `db.password` (and maybe `db.username`) need changing. If MySQL listens on another port, change `3306`.

You can also change the settings **without rebuilding**: put a `db.properties` file next to where you run the JAR
(the current working directory). If that file exists it overrides the bundled one.

Credentials appear nowhere else in the source code.

---

## 7. Build with Maven

```bash
mvn clean package
```

This produces `target/campus-placement.jar`, a single runnable JAR that already contains the MySQL JDBC driver.

## 8. Run the application

```bash
java -jar target/campus-placement.jar
```

or, during development, `mvn exec:java`.

On the sign-in screen, **Test database connection** confirms that the app can reach MySQL and shows which
`db.properties` it used.

---

## 9. Sample login credentials

| Role | Username | Password |
|---|---|---|
| Placement officer | `officer` | `Officer@123` |
| Placement officer | `tpo.assistant` | `Officer@123` |
| Student | any student ID, e.g. `21CSE001`, `21CSE003`, `21IT001`, `21ECE003` | `Student@123` |

Students added by the officer get a login automatically: username = student ID, password = `Student@123`.
Students can change their password from **My Profile**. Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes.

Useful sample students:

| Student | Why it is interesting |
|---|---|
| `21CSE001` Ananya Rao | Placed (accepted Northwind offer) |
| `21CSE003` Priya Sharma | Has a **pending** offer to accept or reject; CGPA 7.64 shows ineligibility reasons |
| `21ECE003` Siddharth Rao | Pending offer from Helix Semiconductors |
| `21IT001` Meera Joshi | Shortlisted in Kestrel (round 2 passed), applied to Aurelia |
| `21IT004` Rohan Gupta | Shortlisted, waiting for round 2 |
| `21CSE002` Rahul Mehta | Eligible for the open Aurelia drive but has not applied yet |

---

## 10. Complete test workflow

1. **Officer** (`officer`) → **Dashboard**: counts come from live SQL queries.
2. **Students → Add student** (e.g. ID `24CSE099`, CSE, batch 2027, CGPA 8.2, 0 backlogs).
   Select the row → **Details** → *Academic records → Add record*; *Skills → Assign skill* (JavaScript).
3. **Companies → Add company**, then **Add job profile** for it.
4. **Drives → New drive**: pick the job profile, status `OPEN`, deadline in the future, criteria
   (min CGPA 7, max backlogs 1, batch 2027, department CSE, skill JavaScript).
5. Select the drive → **Check eligibility** → the new student appears as **ELIGIBLE**; others show reasons.
6. **Logout** → sign in as `24CSE099` / `Student@123` → **My Profile** shows records and skills.
7. **Placement Drives** → select the drive → **Check eligibility** → **Apply**. **My Applications** shows `APPLIED`.
8. **Logout** → officer → **Applications** (filter by drive) shows the application.
9. **Selection** → choose the drive → **Add round** (e.g. 1 Aptitude Test, 2 HR Interview).
   Select round 1 → select the candidate → **Mark PASS** (status becomes `SHORTLISTED`); round 2 → **Mark PASS** (`SELECTED`).
   Trying round 2 before round 1 is refused.
10. **Offers → Issue offer** → choose the candidate → set package and dates.
11. **Logout** → student → **My Offers** → **Accept offer**.
12. **Logout** → officer → **Dashboard** and **Reports** (Accepted offers, Department placement,
    Overall placement statistics) include the new placement. Use **Export CSV** to save any report.

Detailed test cases with expected messages are in `docs/Test_Cases.md`.

---

## 11. Troubleshooting

| Problem | Fix |
|---|---|
| "Cannot connect to the MySQL server…" | Start MySQL (Windows: *Services → MySQL80 → Start*; Linux: `sudo systemctl start mysql`; macOS: `brew services start mysql`). Check host/port in `db.url`. |
| "MySQL rejected the username or password in db.properties." | Fix `db.username` / `db.password`, rebuild, or place an edited `db.properties` next to where you run the JAR. |
| "The campus_placement database does not exist…" | Run `database/complete_database.sql` (step 5). |
| "A required table is missing." | Re-run `complete_database.sql`. |
| `Public Key Retrieval is not allowed` | Keep `allowPublicKeyRetrieval=true` in `db.url` (already in the default). |
| `mvn` not found / wrong Java version | Install Maven and JDK 17+, and make sure `mvn -v` shows Java 17 or newer. |
| Maven cannot download dependencies | The first build needs internet access to Maven Central. Behind a proxy, configure `~/.m2/settings.xml`. |
| Error 1419 (SUPER privilege / binary logging) while running the script as a non-root user | Run the script as `root`, or run `SET GLOBAL log_bin_trust_function_creators = 1;` as root first. |
| Script fails in a GUI tool at `DELIMITER` | Use the `mysql` command line or MySQL Workbench; both support `DELIMITER`. |
| Open drives show as closed | The sample dates are relative to the day the script ran; re-run `complete_database.sql` to refresh them. |
| "No suitable driver" | Run the shaded JAR from `target/campus-placement.jar` (it bundles the driver), not the compiled classes alone. |

---

## Project structure

```
CampusPlacement/
├── pom.xml
├── README.md
├── database/            01–07 SQL scripts and complete_database.sql
├── docs/                ER diagram, relational schema, 3NF, FDs, data dictionary, business rules, test cases
└── src/main/
    ├── java/com/campusplacement/
    │   ├── Main.java
    │   ├── config/      DatabaseConfig (reads db.properties)
    │   ├── db/          ConnectionManager, Db (query + transaction helper)
    │   ├── model/       Java records for all entities
    │   ├── dao/         JDBC data access with prepared statements
    │   ├── service/     Business rules, eligibility engine, transactions, session
    │   ├── ui/          LoginFrame, MainFrame, components/, officer/, student/
    │   └── util/        Password hashing, validation, formatting, error translation, CSV
    └── resources/       db.properties, reports.sql
```

## Features

- Two roles with separate navigation: placement officer (12 screens) and student (6 screens).
- CRUD for students, academic records, skills (and assignment), departments, companies, job profiles and drives.
- One eligibility engine used for officer checks, student checks and application submission, with reasons for every failed criterion.
- Application submission in a single JDBC transaction (drive lock, status, deadline, eligibility, duplicate check, insert).
- Ordered selection rounds with PASS/FAIL results; round order is enforced in Java and by triggers; application status follows results.
- Offers only for selected candidates, one per application; students accept or reject inside a transaction, and can accept only one offer.
- Dashboards, 16 SQL reports with CSV export, sortable tables, search and filters, and friendly error messages.
