package org.example;

import org.example.dao.AnalyticsDAO;
import org.example.dao.FacultyDAO;
import org.example.dao.FacultyPublicationDAO;
import org.example.dao.IndexingDAO;
import org.example.dao.PublicationDAO;
import org.example.dao.ReportDAO;
import org.example.database.DBConnection;
import org.example.model.AuthorInfo;
import org.example.model.Faculty;
import org.example.model.FacultyPublication;
import org.example.model.Indexing;
import org.example.model.Metric;
import org.example.model.Publication;
import org.example.model.PublicationDetails;
import org.example.model.PublicationReportRow;
import org.example.ui.PublicationForm;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Integrated console menu for Members 2-6. */
public class ConsoleMain {
    private static final Scanner SC = new Scanner(System.in);
    private static final FacultyDAO facultyDAO = new FacultyDAO();
    private static final PublicationDAO publicationDAO = new PublicationDAO();
    private static final FacultyPublicationDAO facultyPublicationDAO = new FacultyPublicationDAO();
    private static final IndexingDAO indexingDAO = new IndexingDAO();
    private static final ReportDAO reportDAO = new ReportDAO();
    private static final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println(" RESEARCH PUBLICATION MANAGEMENT SYSTEM");
        System.out.println("===============================================");
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1 -> facultyMenu();
                    case 2 -> addPublication();
                    case 3 -> coAuthorMenu();
                    case 4 -> indexingMenu();
                    case 5 -> reportMenu();
                    case 6 -> analyticsMenu();
                    case 7 -> SwingUtilities.invokeLater(() -> new PublicationForm());
                    case 8 -> testConnection();
                    case 0 -> running = false;
                    default -> System.out.println("Choose a number from the menu.");
                }
            } catch (Exception ex) {
                System.out.println("Operation failed: " + readableMessage(ex));
            }
        }
        SC.close();
        System.out.println("Application closed.");
    }

    private static void printMainMenu() {
        System.out.println("\nMAIN MENU");
        System.out.println("1. Faculty management (Member 2)");
        System.out.println("2. Add publication (Member 3)");
        System.out.println("3. Co-author management (Member 4)");
        System.out.println("4. Indexing management (Member 4)");
        System.out.println("5. Filtered reports and CSV export (Member 5)");
        System.out.println("6. Analytics and summary counts (Member 6)");
        System.out.println("7. Open publication Swing form (Member 3)");
        System.out.println("8. Test database connection");
        System.out.println("0. Exit");
    }

    private static void testConnection() throws SQLException {
        try (Connection ignored = DBConnection.getConnection()) {
            System.out.println("Database connection successful.");
        }
    }

    // -------------------- Member 2: Faculty --------------------
    private static void facultyMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nFACULTY MANAGEMENT");
            System.out.println("1. Add faculty");
            System.out.println("2. View all faculty");
            System.out.println("3. Search faculty");
            System.out.println("4. Update faculty");
            System.out.println("5. Delete faculty");
            System.out.println("0. Back");
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> {
                    int id = readInt("Faculty ID: ");
                    if (facultyDAO.findById(id) != null) {
                        System.out.println("That faculty ID already exists.");
                        break;
                    }
                    Faculty f = new Faculty(id, readNonBlank("Faculty name: "),
                            readNonBlank("Department: "), readNonBlank("Designation: "));
                    facultyDAO.addFaculty(f);
                    System.out.println("Faculty added successfully.");
                }
                case 2 -> printFacultyList(facultyDAO.getAllFaculty());
                case 3 -> {
                    System.out.println("1. By ID  2. By name  3. By department");
                    int search = readInt("Search option: ");
                    if (search == 1) {
                        Faculty f = facultyDAO.findById(readInt("Faculty ID: "));
                        if (f == null) System.out.println("Faculty not found."); else printFaculty(f);
                    } else if (search == 2) {
                        printFacultyList(facultyDAO.findByName(readNonBlank("Name contains: ")));
                    } else if (search == 3) {
                        printFacultyList(facultyDAO.findByDepartment(readNonBlank("Department contains: ")));
                    } else System.out.println("Invalid search option.");
                }
                case 4 -> {
                    int id = readInt("Faculty ID to update: ");
                    Faculty f = facultyDAO.findById(id);
                    if (f == null) {
                        System.out.println("Faculty not found.");
                        break;
                    }
                    String name = readWithDefault("Name [" + f.getFacultyName() + "]: ", f.getFacultyName());
                    String dept = readWithDefault("Department [" + f.getDepartment() + "]: ", f.getDepartment());
                    String des = readWithDefault("Designation [" + f.getDesignation() + "]: ", f.getDesignation());
                    f.setFacultyName(name); f.setDepartment(dept); f.setDesignation(des);
                    if (facultyDAO.updateFaculty(f)) System.out.println("Faculty updated."); else System.out.println("No changes made.");
                }
                case 5 -> {
                    int id = readInt("Faculty ID to delete: ");
                    if (facultyDAO.deleteFaculty(id)) System.out.println("Faculty deleted. Related co-author links are removed by the database.");
                    else System.out.println("Faculty ID not found.");
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void printFacultyList(List<Faculty> faculties) {
        if (faculties.isEmpty()) {
            System.out.println("No faculty records found.");
            return;
        }
        System.out.printf("%-10s %-28s %-22s %-22s%n", "ID", "NAME", "DEPARTMENT", "DESIGNATION");
        for (Faculty f : faculties) printFaculty(f);
    }

    private static void printFaculty(Faculty f) {
        System.out.printf("%-10d %-28s %-22s %-22s%n", f.getFacultyId(), f.getFacultyName(), f.getDepartment(), f.getDesignation());
    }

    // -------------------- Member 3: Publication entry --------------------
    private static void addPublication() throws SQLException {
        System.out.println("\nADD PUBLICATION");
        String id = readNonBlank("Publication ID (max 10 characters, e.g. PUB0001): ");
        if (id.length() > 10) throw new IllegalArgumentException("Publication ID must be at most 10 characters.");
        if (publicationDAO.exists(id)) throw new IllegalArgumentException("That Publication ID already exists.");
        String title = readNonBlank("Title: ");
        int year = readInt("Publication year: ");
        String publisher = readNonBlank("Publisher: ");
        String doi = readLine("DOI (press Enter if none): ");
        String url = readLine("URL (press Enter if none): ");
        System.out.println("1. Journal  2. Conference  3. Book  4. Book Chapter");
        int typeChoice = readInt("Publication type: ");
        String type = switch (typeChoice) {
            case 1 -> "Journal";
            case 2 -> "Conference";
            case 3 -> "Book";
            case 4 -> "Book Chapter";
            default -> throw new IllegalArgumentException("Choose a valid publication type.");
        };
        PublicationDetails details = new PublicationDetails();
        switch (type) {
            case "Journal" -> {
                details.setVolume(readNonBlank("Volume: "));
                details.setIssue(readNonBlank("Issue: "));
                details.setPages(readNonBlank("Pages (e.g. 10-18): "));
            }
            case "Conference" -> details.setConferenceDate(readNonBlank("Conference date (YYYY-MM-DD): "));
            case "Book" -> {
                details.setIsbn(readNonBlank("ISBN: "));
                details.setEdition(readNonBlank("Edition: "));
            }
            case "Book Chapter" -> {
                details.setBookTitle(readNonBlank("Book title: "));
                details.setChapterNumber(readInt("Chapter number: "));
                details.setPages(readNonBlank("Pages (e.g. 20-29): "));
            }
            default -> throw new IllegalStateException("Unexpected type.");
        }
        publicationDAO.addPublication(new Publication(id.trim(), title, year, publisher, doi, url, type), details);
        System.out.println("Publication and type-specific details saved successfully.");
        System.out.println("Next, use Co-author management to link one to five faculty authors and Indexing management to add agencies.");
    }

    // -------------------- Member 4: Co-authorship --------------------
    private static void coAuthorMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nCO-AUTHOR MANAGEMENT");
            System.out.println("1. Set/replace authors for a publication (1-5 faculty)");
            System.out.println("2. View authors of a publication");
            System.out.println("3. View publication IDs linked to a faculty member");
            System.out.println("0. Back");
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> {
                    String pubId = readNonBlank("Publication ID: ");
                    List<Faculty> all = facultyDAO.getAllFaculty();
                    printFacultyList(all);
                    if (all.isEmpty()) {
                        System.out.println("Add faculty members before assigning authors.");
                        break;
                    }
                    int count = readInt("Number of faculty authors (1-5): ");
                    if (count < 1 || count > 5) throw new IllegalArgumentException("Author count must be from 1 to 5.");
                    List<Integer> ids = new ArrayList<>();
                    for (int i = 1; i <= count; i++) ids.add(readInt("Faculty ID for author order " + i + ": "));
                    facultyPublicationDAO.replaceAuthors(pubId, ids);
                    System.out.println("Author list saved in the order entered.");
                }
                case 2 -> {
                    String id = readNonBlank("Publication ID: ");
                    List<AuthorInfo> authors = facultyPublicationDAO.getAuthorsForPublication(id);
                    if (authors.isEmpty()) System.out.println("No authors linked to this publication yet.");
                    else {
                        System.out.println("Author order | Faculty ID | Name | Department");
                        for (AuthorInfo a : authors) System.out.printf("%12d | %10d | %s | %s%n",
                                a.getAuthorOrder(), a.getFacultyId(), a.getFacultyName(), a.getDepartment());
                    }
                }
                case 3 -> {
                    int facultyId = readInt("Faculty ID: ");
                    List<FacultyPublication> links = facultyPublicationDAO.getMappingsForFaculty(facultyId);
                    if (links.isEmpty()) System.out.println("No publication links found for that faculty ID.");
                    else for (FacultyPublication link : links)
                        System.out.println("Publication ID: " + link.getPublicationId() + " | Author order: " + link.getAuthorOrder());
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // -------------------- Member 4: Indexing --------------------
    private static void indexingMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nINDEXING MANAGEMENT");
            System.out.println("1. Add indexing agency to a publication");
            System.out.println("2. View agencies for a publication");
            System.out.println("3. Remove an agency from a publication");
            System.out.println("4. List all agencies currently in use");
            System.out.println("0. Back");
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> {
                    String pubId = readNonBlank("Publication ID: ");
                    System.out.println("Examples: Scopus, Web of Science, IEEE Xplore, UGC CARE, PubMed");
                    String agency = readNonBlank("Indexing agency: ");
                    indexingDAO.addIndexing(pubId, agency);
                    System.out.println("Indexing agency linked to publication.");
                }
                case 2 -> {
                    String pubId = readNonBlank("Publication ID: ");
                    List<Indexing> entries = indexingDAO.getIndexingForPublication(pubId);
                    if (entries.isEmpty()) System.out.println("No indexing agencies recorded for this publication.");
                    else entries.forEach(i -> System.out.println(i.getIndexingName()));
                }
                case 3 -> {
                    String pubId = readNonBlank("Publication ID: ");
                    String agency = readNonBlank("Agency name to remove (must match spelling): ");
                    System.out.println(indexingDAO.removeIndexing(pubId, agency) ? "Indexing entry removed." : "No matching indexing entry found.");
                }
                case 4 -> {
                    List<String> agencies = indexingDAO.getAllAgencies();
                    if (agencies.isEmpty()) System.out.println("No agencies in use yet.");
                    else agencies.forEach(System.out::println);
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // -------------------- Member 5: Filtered reports --------------------
    private static void reportMenu() throws SQLException, IOException {
        System.out.println("\nREPORTS");
        System.out.println("1. All publications");
        System.out.println("2. Faculty-wise publications");
        System.out.println("3. Year range");
        System.out.println("4. Publication type");
        System.out.println("5. Indexing agency");
        int choice = readInt("Report choice: ");
        List<PublicationReportRow> rows;
        switch (choice) {
            case 1 -> rows = reportDAO.getAllPublications();
            case 2 -> rows = reportDAO.byFaculty(readInt("Faculty ID: "));
            case 3 -> rows = reportDAO.byYearRange(readInt("Start year: "), readInt("End year: "));
            case 4 -> {
                System.out.println("Journal / Conference / Book / Book Chapter");
                rows = reportDAO.byType(readNonBlank("Type: "));
            }
            case 5 -> rows = reportDAO.byIndexing(readNonBlank("Indexing agency: "));
            default -> { System.out.println("Invalid report choice."); return; }
        }
        printReportRows(rows);
        if (!rows.isEmpty() && readLine("Export these rows to CSV? (y/n): ").trim().equalsIgnoreCase("y")) {
            String filename = readWithDefault("CSV filename [reports/publication-report.csv]: ", "reports/publication-report.csv");
            reportDAO.exportCsv(rows, Path.of(filename));
            System.out.println("CSV report saved to: " + Path.of(filename).toAbsolutePath());
        }
    }

    private static void printReportRows(List<PublicationReportRow> rows) {
        if (rows.isEmpty()) {
            System.out.println("No matching publications found.");
            return;
        }
        System.out.println("Matching publications: " + rows.size());
        for (PublicationReportRow r : rows) {
            System.out.println("------------------------------------------------------------");
            System.out.println("ID: " + r.getPublicationId() + " | Year: " + r.getYear() + " | Type: " + r.getPublicationType());
            System.out.println("Title: " + r.getTitle());
            System.out.println("Publisher: " + r.getPublisher());
            System.out.println("Authors: " + (r.getAuthors().isBlank() ? "Not assigned" : r.getAuthors()));
            System.out.println("Indexing: " + (r.getIndexingAgencies().isBlank() ? "Not assigned" : r.getIndexingAgencies()));
            if (r.getDoi() != null && !r.getDoi().isBlank()) System.out.println("DOI: " + r.getDoi());
            if (r.getUrl() != null && !r.getUrl().isBlank()) System.out.println("URL: " + r.getUrl());
        }
        System.out.println("------------------------------------------------------------");
    }

    // -------------------- Member 6: Aggregation and analytics --------------------
    private static void analyticsMenu() throws SQLException {
        boolean back = false;
        while (!back) {
            System.out.println("\nANALYTICS");
            System.out.println("1. Department-wise publication totals");
            System.out.println("2. Annual publication trend");
            System.out.println("3. Publication counts by category");
            System.out.println("4. Publication counts by indexing agency");
            System.out.println("0. Back");
            int choice = readInt("Enter choice: ");
            List<Metric> metrics = switch (choice) {
                case 1 -> analyticsDAO.departmentWisePublicationCounts();
                case 2 -> analyticsDAO.annualPublicationTrends();
                case 3 -> analyticsDAO.publicationCountsByType();
                case 4 -> analyticsDAO.publicationCountsByIndexing();
                case 0 -> null;
                default -> {
                    System.out.println("Invalid choice.");
                    yield null;
                }
            };
            if (choice == 0) back = true;
            else printMetrics(metrics);
        }
    }

    private static void printMetrics(List<Metric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            System.out.println("No statistics to show yet.");
            return;
        }
        System.out.printf("%-35s %s%n", "CATEGORY", "PUBLICATIONS");
        System.out.println("-----------------------------------------------");
        for (Metric metric : metrics) System.out.printf("%-35s %d%n", metric.getLabel(), metric.getCount());
    }

    // -------------------- Input helpers --------------------
    private static int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            try { return Integer.parseInt(value); }
            catch (NumberFormatException ex) { System.out.println("Enter a valid whole number."); }
        }
    }

    private static String readNonBlank(String prompt) {
        while (true) {
            String value = readLine(prompt).trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine();
    }

    private static String readWithDefault(String prompt, String current) {
        String value = readLine(prompt);
        return value.isBlank() ? current : value.trim();
    }

    private static String readableMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) return ex.getClass().getSimpleName();
        if (ex instanceof SQLException && message.contains("Duplicate entry"))
            return "A record with this ID/value already exists.";
        if (ex instanceof SQLException && message.contains("foreign key constraint"))
            return "Related faculty/publication record was not found, or the record is linked elsewhere.";
        return message;
    }
}
