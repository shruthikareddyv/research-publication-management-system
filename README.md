# Research Publication Management System — Desktop UI

This is a Java Swing + MySQL/JDBC application for all six project-member modules. The default application entry point (`org.example.Main`) now opens a graphical dashboard with tabs for Overview, Faculty, Publications, Co-authors, Indexing, Reports, and Analytics. The previous terminal menu is preserved as `org.example.ConsoleMain`.

## Project structure

```text
ResearchPublicationSystem/
├── pom.xml
├── README.md
├── run-macos.sh
├── database/
│   └── database_setup_full.sql
└── src/main/java/org/example/
    ├── Main.java                 # Starts the graphical UI
    ├── ConsoleMain.java          # Optional old terminal menu
    ├── database/DBConnection.java
    ├── dao/
    │   ├── FacultyDAO.java
    │   ├── PublicationDAO.java
    │   ├── FacultyPublicationDAO.java
    │   ├── IndexingDAO.java
    │   ├── ReportDAO.java
    │   └── AnalyticsDAO.java
    ├── model/                    # Entities and report/metric models
    └── ui/
        ├── MainWindow.java       # End-to-end desktop dashboard
        └── PublicationForm.java  # Type-specific publication entry form
```

## 1. One-time database setup from macOS Terminal

MySQL Workbench is optional. Start the MySQL service:

```bash
brew services start mysql
```

Open Terminal in the extracted project folder. If it is under Downloads:

```bash
cd ~/Downloads/ResearchPublicationSystem-GUI
```

Run the entire SQL script directly through the MySQL client:

```bash
mysql -u root -p < database/database_setup_full.sql
```

Enter the same MySQL root password that you use for `mysql -u root -p`. The script creates `ResearchPublicationDB` and any missing core/type-specific tables. It intentionally contains no `DROP TABLE` statements.

Verify the setup:

```bash
mysql -u root -p
```

At the `mysql>` prompt:

```sql
SHOW DATABASES;
USE ResearchPublicationDB;
SHOW TABLES;
SELECT * FROM Faculty;
EXIT;
```

You should see `ResearchPublicationDB`, the four core tables (`Faculty`, `Publication`, `Faculty_Publication`, `Indexing`), and the four type-specific tables (`Journal`, `Conference`, `Book`, `Book_Chapter`).

## 2. Requirements

- Java JDK 17 or newer
- Maven
- MySQL running on `localhost:3306`

On an Apple Silicon Mac with Homebrew, install missing tools with:

```bash
brew install openjdk@21
brew install maven
```

Check them:

```bash
java -version
mvn -version
mysql --version
```

If Java is installed but the shell cannot find it, follow Homebrew's `openjdk@21` PATH instructions for your Mac.

## 3. Launch the graphical application from Terminal

In the project folder, set your connection values in the same Terminal session:

```bash
export DB_USER="root"
export DB_URL="jdbc:mysql://localhost:3306/ResearchPublicationDB?serverTimezone=UTC"
read -s "DB_PASSWORD?Enter MySQL root password: "
echo
export DB_PASSWORD
```

Type your MySQL password when prompted. It won't be displayed while typing. This does not save the password into the Java source code.

Now launch the Swing dashboard:

```bash
mvn clean compile exec:java
```

The window should open with tabs for Overview, Faculty, Publications, Co-authors, Indexing, Reports, and Analytics. Keep this Terminal window open while using the application. The environment variables are temporary and must be set again in a new Terminal session.

### Optional one-command launcher

The project includes `run-macos.sh`. In Terminal, from the project folder:

```bash
chmod +x run-macos.sh
./run-macos.sh
```

The script asks for the database password if `DB_PASSWORD` is not already set, then compiles and opens the UI.

## 4. Launch from IntelliJ IDEA

1. Open the **project root folder** containing `pom.xml`.
2. Let IntelliJ import the Maven project and download the MySQL JDBC driver.
3. Open `src/main/java/org/example/Main.java` and run `Main.main()`.
4. Configure `DB_USER`, `DB_PASSWORD`, and optionally `DB_URL` in **Run → Edit Configurations → Environment variables** if IntelliJ is not launched from the configured Terminal session.

## 5. What each graphical tab does

- **Overview:** current faculty count, publication count, distinct indexing-agency count, and recent publications; quick navigation buttons.
- **Faculty:** add, search, edit, and delete faculty. Select a row to edit or delete it.
- **Publications:** view saved publication records; use **Add publication** to open the Swing form. Select Journal, Conference, Book, or Book Chapter and enter its type-specific fields. A successful save refreshes the dashboard/table.
- **Co-authors:** select a publication and enter faculty IDs separated by commas in the desired author order, e.g. `1,2,3`. Click **Save / replace authors**. The mapping DAO validates publication/faculty IDs, duplicate IDs, and the maximum of five authors.
- **Indexing:** select a publication, choose or type an agency and click **Add agency**. Select a saved agency row and click **Remove selected** to remove that assignment.
- **Reports:** run all-publication, faculty-wise, year-range, type-wise, and indexing-wise filters. Use **Export CSV…** to choose where to save the report.
- **Analytics:** select department totals, annual trend, publication counts by type, or counts by indexing agency. Click **Refresh analytics** to update both the table and bar chart.

The UI uses the same DAO and database methods tested by the console menu. Data is read/written in the MySQL database, not held only in the UI.

## 6. Optional terminal-menu mode

To use the old console menu, instead of the graphical UI, run:

```bash
mvn -q compile exec:java -Dexec.args=--console
```

## 7. If the UI does not open or does not connect

- **No window opens:** check that Java is running in a graphical macOS desktop session and run `mvn clean compile exec:java` from the project root.
- **Database not connected shown at the top:** verify `brew services list`, the database name, and the environment variables. Test credentials separately with `mysql -u root -p`.
- **Unknown database:** execute `database/database_setup_full.sql` as shown in section 1.
- **Table does not exist:** run the complete setup script. The publication form depends on `Journal`, `Conference`, `Book`, and `Book_Chapter` as well as the four core tables.
- **Duplicate entry:** choose a new Faculty ID or Publication ID, or remove an existing indexing assignment before adding the same agency again.
- **Faculty or publication not found:** add the parent faculty/publication record before creating co-author or indexing links.
- **`mvn` not found:** install Maven and reopen Terminal.
- **Console still opens:** ensure `pom.xml` points to `org.example.Main` and run the command above without `-Dexec.args=--console`.

## Data safety note

The SQL setup script uses `CREATE TABLE IF NOT EXISTS`; it is designed to add missing tables without dropping your existing tables or backend test records. The application performs changes only when you choose Add, Save, Update, or Delete actions in the UI.
