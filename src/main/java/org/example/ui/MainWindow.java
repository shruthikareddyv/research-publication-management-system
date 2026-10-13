package org.example.ui;

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
import org.example.model.PublicationReportRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Full Swing dashboard for all application modules. */
public class MainWindow extends JFrame {
    private static final Color NAVY = new Color(25, 43, 75);
    private static final Color BLUE = new Color(48, 101, 184);
    private static final Color LIGHT = new Color(245, 247, 251);
    private static final Color TEXT = new Color(36, 46, 66);
    private static final Color MUTED = new Color(104, 116, 137);
    private static final String[] TYPES = {"Journal", "Conference", "Book", "Book Chapter"};
    private static final String[] COMMON_AGENCIES = {"Scopus", "Web of Science", "IEEE Xplore", "UGC CARE", "PubMed"};

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final PublicationDAO publicationDAO = new PublicationDAO();
    private final FacultyPublicationDAO facultyPublicationDAO = new FacultyPublicationDAO();
    private final IndexingDAO indexingDAO = new IndexingDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel connectionStatus = new JLabel("Checking database connection…");
    private final JLabel statusBar = new JLabel("Ready");

    private final JLabel facultyCount = new JLabel("—");
    private final JLabel publicationCount = new JLabel("—");
    private final JLabel agencyCount = new JLabel("—");
    private final JTable recentTable = makeTable(new String[]{"Publication ID", "Title", "Type", "Year", "Indexing agencies"});

    private final JTextField facultySearch = new JTextField(22);
    private final JTable facultyTable = makeTable(new String[]{"Faculty ID", "Name", "Department", "Designation"});
    private List<Faculty> cachedFaculty = new ArrayList<>();

    private final JTable publicationTable = makeTable(new String[]{"Publication ID", "Title", "Type", "Year", "Publisher", "DOI", "URL"});

    private final JComboBox<PublicationOption> authorPublicationBox = new JComboBox<>();
    private final JTextField authorIdsField = new JTextField(24);
    private final JTable authorsTable = makeTable(new String[]{"Order", "Faculty ID", "Name", "Department"});
    private final JTable availableFacultyTable = makeTable(new String[]{"Faculty ID", "Name", "Department", "Designation"});

    private final JComboBox<PublicationOption> indexingPublicationBox = new JComboBox<>();
    private final JComboBox<String> agencyBox = new JComboBox<>(COMMON_AGENCIES);
    private final JTable indexingTable = makeTable(new String[]{"Indexing agency"});

    private final JComboBox<String> reportModeBox = new JComboBox<>(new String[]{
            "All publications", "Faculty-wise", "Year range", "Publication type", "Indexing agency"});
    private final JTextField reportFacultyId = new JTextField(7);
    private final JTextField reportStartYear = new JTextField(7);
    private final JTextField reportEndYear = new JTextField(7);
    private final JComboBox<String> reportTypeBox = new JComboBox<>(TYPES);
    private final JComboBox<String> reportAgencyBox = new JComboBox<>(COMMON_AGENCIES);
    private final JLabel reportCountLabel = new JLabel("Run a report to see matching publications.");
    private final JTable reportTable = makeTable(new String[]{"Publication ID", "Title", "Type", "Year", "Publisher", "Authors", "Indexing agencies", "DOI", "URL"});
    private List<PublicationReportRow> currentReportRows = new ArrayList<>();

    private final JComboBox<String> analyticsModeBox = new JComboBox<>(new String[]{
            "Department-wise publication totals", "Annual publication trend", "Publication counts by category", "Publication counts by indexing agency"});
    private final JTable analyticsTable = makeTable(new String[]{"Category", "Publication count"});
    private final BarChartPanel barChart = new BarChartPanel();
    private final JLabel analyticsCountLabel = new JLabel("Choose a metric and click Refresh.");

    public MainWindow() {
        setTitle("Research Publication Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Start maximized so the controls and tables have enough room on laptop and desktop displays.
        setMinimumSize(new Dimension(980, 640));
        setSize(1440, 900);
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(LIGHT);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addTab("Overview", scrollableTab(buildDashboardPanel()));
        tabs.addTab("Faculty", scrollableTab(buildFacultyPanel()));
        tabs.addTab("Publications", scrollableTab(buildPublicationsPanel()));
        tabs.addTab("Co-authors", scrollableTab(buildCoAuthorsPanel()));
        tabs.addTab("Indexing", scrollableTab(buildIndexingPanel()));
        tabs.addTab("Reports", scrollableTab(buildReportsPanel()));
        tabs.addTab("Analytics", scrollableTab(buildAnalyticsPanel()));
        tabs.setBorder(new EmptyBorder(4, 12, 4, 12));
        add(tabs, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        // Sensible minimum widths keep long IDs, titles, authors and URLs readable;
        // the tables show horizontal scrollbars rather than hiding those columns.
        setColumnWidths(recentTable, 120, 360, 130, 80, 240);
        setColumnWidths(publicationTable, 120, 360, 140, 80, 220, 190, 280);
        setColumnWidths(reportTable, 120, 320, 140, 80, 220, 320, 220, 180, 280);
        setColumnWidths(authorsTable, 80, 110, 240, 180);
        setColumnWidths(availableFacultyTable, 100, 240, 160, 220);

        tabs.addChangeListener(e -> refreshSelectedTab());
        reportModeBox.addActionListener(e -> updateReportFields());
        agencyBox.setEditable(true);
        reportAgencyBox.setEditable(true);
        refreshAll();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Research Publication Management System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel subtitle = new JLabel("Faculty  •  Publications  •  Co-authorship  •  Indexing  •  Reports  •  Analytics");
        subtitle.setForeground(new Color(214, 224, 241));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        right.setOpaque(false);
        connectionStatus.setForeground(new Color(235, 241, 250));
        connectionStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        JButton refresh = new JButton("Refresh data");
        styleButton(refresh, true);
        refresh.addActionListener(e -> refreshAll());
        right.add(connectionStatus);
        right.add(refresh);

        header.add(titles, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 228, 238)),
                new EmptyBorder(8, 18, 8, 18)));
        statusBar.setForeground(MUTED);
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bar.add(statusBar, BorderLayout.WEST);
        JLabel footer = new JLabel("Local MySQL database  •  Swing desktop UI");
        footer.setForeground(MUTED);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bar.add(footer, BorderLayout.EAST);
        return bar;
    }

    private JComponent buildDashboardPanel() {
        JPanel root = contentPanel();
        root.setLayout(new BorderLayout(14, 14));

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.setOpaque(false);
        JPanel headingPanel = heading("Overview", "Live summary from ResearchPublicationDB");
        top.add(headingPanel, BorderLayout.WEST);

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 14));
        cards.setOpaque(false);
        cards.add(metricCard("FACULTY MEMBERS", facultyCount, "Records in Faculty"));
        cards.add(metricCard("PUBLICATIONS", publicationCount, "All publication types"));
        cards.add(metricCard("INDEXING AGENCIES", agencyCount, "Distinct agency names"));

        JPanel recent = cardPanel();
        recent.setLayout(new BorderLayout(8, 8));
        recent.add(heading("Recent publications", "Most recent publication years first"), BorderLayout.NORTH);
        recent.add(new JScrollPane(recentTable), BorderLayout.CENTER);

        JPanel lower = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 6));
        lower.setOpaque(false);
        lower.add(actionButton("Manage faculty", () -> tabs.setSelectedIndex(1), false));
        lower.add(actionButton("Add publication", () -> openPublicationForm(), true));
        lower.add(actionButton("Manage co-authors", () -> tabs.setSelectedIndex(3), false));
        lower.add(actionButton("View reports", () -> tabs.setSelectedIndex(5), false));

        JPanel center = new JPanel(new BorderLayout(14, 14));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(recent, BorderLayout.CENTER);
        center.add(lower, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private JPanel metricCard(String label, JLabel value, String note) {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(label);
        name.setForeground(MUTED);
        name.setFont(new Font("SansSerif", Font.BOLD, 11));
        value.setForeground(NAVY);
        value.setFont(new Font("SansSerif", Font.BOLD, 32));
        JLabel detail = new JLabel(note);
        detail.setForeground(MUTED);
        detail.setFont(new Font("SansSerif", Font.PLAIN, 12));
        card.add(name);
        card.add(Box.createVerticalStrut(12));
        card.add(value);
        card.add(Box.createVerticalStrut(5));
        card.add(detail);
        return card;
    }

    private JComponent buildFacultyPanel() {
        JPanel root = contentPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.add(heading("Faculty management", "Add, search, update and delete faculty records"), BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        actionPanel.setOpaque(false);
        facultySearch.setToolTipText("Search names or departments");
        JButton search = actionButton("Search", this::filterFacultyRows, true);
        facultySearch.addActionListener(e -> filterFacultyRows());
        actionPanel.add(new JLabel("Search:"));
        actionPanel.add(facultySearch);
        actionPanel.add(search);
        actionPanel.add(actionButton("Clear", () -> { facultySearch.setText(""); filterFacultyRows(); }, false));
        actionPanel.add(actionButton("Add faculty", () -> showFacultyDialog(null), true));
        actionPanel.add(actionButton("Edit selected", this::editSelectedFaculty, false));
        actionPanel.add(actionButton("Delete selected", this::deleteSelectedFaculty, false));
        actionPanel.add(actionButton("Refresh", this::loadFaculty, false));

        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);
        body.add(actionPanel, BorderLayout.NORTH);
        body.add(new JScrollPane(facultyTable), BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildPublicationsPanel() {
        JPanel root = contentPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.add(heading("Publications", "Browse saved records and enter Journal, Conference, Book or Book Chapter details"), BorderLayout.NORTH);
        JPanel actions = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        actions.setOpaque(false);
        actions.add(actionButton("Add publication", this::openPublicationForm, true));
        actions.add(actionButton("Refresh list", this::loadPublications, false));
        actions.add(actionButton("Open co-authors", () -> tabs.setSelectedIndex(3), false));
        actions.add(actionButton("Open indexing", () -> tabs.setSelectedIndex(4), false));
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);
        body.add(actions, BorderLayout.NORTH);
        body.add(new JScrollPane(publicationTable), BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JComponent buildCoAuthorsPanel() {
        JPanel root = contentPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.add(heading("Co-author management", "Set author order for each publication (up to five faculty members)"), BorderLayout.NORTH);

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JPanel publicationRow = new JPanel(new BorderLayout(8, 0));
        publicationRow.setOpaque(false);
        publicationRow.add(new JLabel("Publication:"), BorderLayout.WEST);
        publicationRow.add(authorPublicationBox, BorderLayout.CENTER);
        publicationRow.add(actionButton("Load authors", this::loadAuthorsForSelectedPublication, false), BorderLayout.EAST);
        publicationRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(publicationRow);
        controls.add(Box.createVerticalStrut(8));

        JPanel idsRow = new JPanel(new BorderLayout(8, 4));
        idsRow.setOpaque(false);
        JLabel idsLabel = new JLabel("Faculty IDs in author order (comma-separated):");
        idsRow.add(idsLabel, BorderLayout.NORTH);
        authorIdsField.setToolTipText("Example: 101,102,103 — author order follows the order entered");
        authorIdsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        idsRow.add(authorIdsField, BorderLayout.CENTER);
        idsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(idsRow);
        controls.add(Box.createVerticalStrut(8));

        JPanel actions = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(actionButton("Save / replace authors", this::saveAuthors, true));
        actions.add(actionButton("Clear IDs", () -> authorIdsField.setText(""), false));
        controls.add(actions);

        JLabel hint = new JLabel("Enter 1–5 existing faculty IDs. The order entered becomes Author_Order 1, 2, 3…");
        hint.setForeground(MUTED);
        hint.setBorder(new EmptyBorder(4, 2, 2, 2));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(hint);

        JPanel authorPanel = cardPanel();
        authorPanel.setLayout(new BorderLayout(6, 6));
        authorPanel.add(heading("Current authors", "Author order is stored in Faculty_Publication"), BorderLayout.NORTH);
        authorPanel.add(new JScrollPane(authorsTable), BorderLayout.CENTER);
        JPanel facultyPanel = cardPanel();
        facultyPanel.setLayout(new BorderLayout(6, 6));
        facultyPanel.add(heading("Available faculty", "Use these IDs in the field above"), BorderLayout.NORTH);
        facultyPanel.add(new JScrollPane(availableFacultyTable), BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, authorPanel, facultyPanel);
        split.setResizeWeight(0.45);
        split.setOneTouchExpandable(true);
        split.setBorder(null);
        split.setMinimumSize(new Dimension(300, 280));

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        body.add(controls, BorderLayout.NORTH);
        body.add(split, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        authorPublicationBox.addActionListener(e -> loadAuthorsForSelectedPublication());
        return root;
    }

    private JComponent buildIndexingPanel() {
        JPanel root = contentPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.add(heading("Indexing management", "Link or remove indexing agencies for each publication"), BorderLayout.NORTH);

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JPanel publicationRow = new JPanel(new BorderLayout(8, 0));
        publicationRow.setOpaque(false);
        publicationRow.add(new JLabel("Publication:"), BorderLayout.WEST);
        publicationRow.add(indexingPublicationBox, BorderLayout.CENTER);
        publicationRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(publicationRow);
        controls.add(Box.createVerticalStrut(8));

        JPanel agencyRow = new JPanel(new BorderLayout(8, 0));
        agencyRow.setOpaque(false);
        agencyRow.add(new JLabel("Agency:"), BorderLayout.WEST);
        agencyBox.setEditable(true);
        agencyBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        agencyRow.add(agencyBox, BorderLayout.CENTER);
        agencyRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(agencyRow);
        controls.add(Box.createVerticalStrut(8));

        JPanel actions = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(actionButton("Add agency", this::addIndexing, true));
        actions.add(actionButton("Refresh list", this::loadIndexingForSelectedPublication, false));
        actions.add(actionButton("Remove selected", this::removeSelectedIndexing, false));
        controls.add(actions);

        JLabel hint = new JLabel("Common values: Scopus, Web of Science, IEEE Xplore, UGC CARE, PubMed. You can also type another agency.");
        hint.setForeground(MUTED);
        hint.setBorder(new EmptyBorder(4, 2, 4, 2));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(hint);

        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);
        body.add(controls, BorderLayout.NORTH);
        JPanel tableCard = cardPanel();
        tableCard.setLayout(new BorderLayout(6, 6));
        tableCard.add(heading("Agencies linked to selected publication", "Select a row before removing an assignment"), BorderLayout.NORTH);
        tableCard.add(new JScrollPane(indexingTable), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        indexingPublicationBox.addActionListener(e -> loadIndexingForSelectedPublication());
        return root;
    }

    private JComponent buildReportsPanel() {
        JPanel root = contentPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.add(heading("Filtered reports", "Filter publications and export matching rows to CSV"), BorderLayout.NORTH);

        JPanel filter = new JPanel(new GridLayout(0, 3, 10, 8));
        filter.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 226, 236)), new EmptyBorder(10, 10, 10, 10)));
        filter.setBackground(Color.WHITE);
        filter.add(filterField("Report type", reportModeBox));
        filter.add(filterField("Faculty ID", reportFacultyId));
        filter.add(filterField("Start year", reportStartYear));
        filter.add(filterField("End year", reportEndYear));
        filter.add(filterField("Publication type", reportTypeBox));
        filter.add(filterField("Indexing agency", reportAgencyBox));

        JPanel actions = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        actions.setOpaque(false);
        actions.add(actionButton("Run report", this::runReport, true));
        actions.add(actionButton("Export CSV…", this::exportReport, false));
        actions.add(actionButton("Show all", () -> { reportModeBox.setSelectedIndex(0); runReport(); }, false));
        JPanel tableCard = cardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));
        tableCard.add(reportCountLabel, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        JPanel filterArea = new JPanel(new BorderLayout(8, 8));
        filterArea.setOpaque(false);
        filterArea.add(filter, BorderLayout.CENTER);
        filterArea.add(actions, BorderLayout.SOUTH);
        // The report table consumes the remaining vertical space.
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, filterArea, tableCard);
        split.setResizeWeight(0.28);
        split.setOneTouchExpandable(true);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);
        updateReportFields();
        return root;
    }

    private JComponent buildAnalyticsPanel() {
        JPanel root = contentPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.add(heading("Analytics & summary counts", "Aggregated counts calculated from the current database contents"), BorderLayout.NORTH);
        JPanel controls = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 4));
        controls.setOpaque(false);
        controls.add(new JLabel("Metric:"));
        analyticsModeBox.setPreferredSize(new Dimension(360, 32));
        analyticsModeBox.setMaximumSize(new Dimension(480, 36));
        controls.add(analyticsModeBox);
        controls.add(actionButton("Refresh analytics", this::loadAnalytics, true));
        controls.add(analyticsCountLabel);

        JPanel tablePanel = cardPanel();
        tablePanel.setLayout(new BorderLayout(6, 6));
        tablePanel.add(heading("Values", "The table and bars use the same query results"), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(analyticsTable), BorderLayout.CENTER);
        JPanel chartPanel = cardPanel();
        chartPanel.setLayout(new BorderLayout(6, 6));
        chartPanel.add(heading("Visual comparison", "Bar length represents publication count"), BorderLayout.NORTH);
        chartPanel.add(barChart, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, chartPanel);
        split.setResizeWeight(0.42);
        split.setOneTouchExpandable(true);
        split.setBorder(null);
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);
        body.add(controls, BorderLayout.NORTH);
        body.add(split, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private void refreshSelectedTab() {
        switch (tabs.getSelectedIndex()) {
            case 0 -> refreshDashboard();
            case 1 -> loadFaculty();
            case 2 -> loadPublications();
            case 3 -> { loadFaculty(); refreshPublicationChoices(); loadAuthorsForSelectedPublication(); }
            case 4 -> { refreshPublicationChoices(); refreshAgencyChoices(); loadIndexingForSelectedPublication(); }
            case 5 -> { refreshAgencyChoices(); }
            case 6 -> loadAnalytics();
            default -> { }
        }
    }

    private void refreshAll() {
        try (Connection ignored = DBConnection.getConnection()) {
            connectionStatus.setText("● Database connected");
            connectionStatus.setForeground(new Color(190, 246, 208));
            statusBar.setText("Connected to ResearchPublicationDB");
        } catch (Exception ex) {
            connectionStatus.setText("● Database not connected");
            connectionStatus.setForeground(new Color(255, 207, 207));
            statusBar.setText("Connection failed: " + shortMessage(ex));
            return;
        }
        loadFaculty();
        loadPublications();
        refreshPublicationChoices();
        refreshAgencyChoices();
        refreshDashboard();
        loadAuthorsForSelectedPublication();
        loadIndexingForSelectedPublication();
        statusBar.setText("Data refreshed successfully");
    }

    private void refreshDashboard() {
        try {
            List<Faculty> faculties = facultyDAO.getAllFaculty();
            List<Publication> publications = publicationDAO.getAllPublications();
            List<String> agencies = indexingDAO.getAllAgencies();
            facultyCount.setText(String.valueOf(faculties.size()));
            publicationCount.setText(String.valueOf(publications.size()));
            agencyCount.setText(String.valueOf(agencies.size()));
            List<PublicationReportRow> rows = reportDAO.getAllPublications();
            DefaultTableModel model = modelOf(recentTable);
            clear(model);
            rows.stream().sorted(Comparator.comparingInt(PublicationReportRow::getYear).reversed())
                    .limit(8).forEach(r -> model.addRow(new Object[]{r.getPublicationId(), r.getTitle(), r.getPublicationType(), r.getYear(), r.getIndexingAgencies()}));
        } catch (Exception ex) {
            setErrorStatus(ex);
        }
    }

    private void loadFaculty() {
        try {
            cachedFaculty = facultyDAO.getAllFaculty();
            fillFacultyModel(cachedFaculty);
            fillAvailableFacultyModel(cachedFaculty);
        } catch (Exception ex) { setErrorStatus(ex); }
    }

    private void fillFacultyModel(List<Faculty> list) {
        DefaultTableModel model = modelOf(facultyTable);
        clear(model);
        for (Faculty f : list) model.addRow(new Object[]{f.getFacultyId(), f.getFacultyName(), f.getDepartment(), f.getDesignation()});
    }

    private void fillAvailableFacultyModel(List<Faculty> list) {
        DefaultTableModel model = modelOf(availableFacultyTable);
        clear(model);
        for (Faculty f : list) model.addRow(new Object[]{f.getFacultyId(), f.getFacultyName(), f.getDepartment(), f.getDesignation()});
    }

    private void filterFacultyRows() {
        String q = facultySearch.getText().trim().toLowerCase();
        if (q.isEmpty()) { fillFacultyModel(cachedFaculty); return; }
        fillFacultyModel(cachedFaculty.stream().filter(f ->
                String.valueOf(f.getFacultyId()).contains(q) || f.getFacultyName().toLowerCase().contains(q)
                        || f.getDepartment().toLowerCase().contains(q) || f.getDesignation().toLowerCase().contains(q))
                .collect(Collectors.toList()));
    }

    private void showFacultyDialog(Faculty existing) {
        JTextField id = new JTextField(existing == null ? "" : String.valueOf(existing.getFacultyId()), 18);
        JTextField name = new JTextField(existing == null ? "" : existing.getFacultyName(), 24);
        JTextField dept = new JTextField(existing == null ? "" : existing.getDepartment(), 24);
        JTextField designation = new JTextField(existing == null ? "" : existing.getDesignation(), 24);
        if (existing != null) id.setEditable(false);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 9));
        form.add(new JLabel("Faculty ID")); form.add(id);
        form.add(new JLabel("Faculty name")); form.add(name);
        form.add(new JLabel("Department")); form.add(dept);
        form.add(new JLabel("Designation")); form.add(designation);
        int option = JOptionPane.showConfirmDialog(this, form, existing == null ? "Add faculty" : "Edit faculty",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;
        try {
            int facultyId = Integer.parseInt(id.getText().trim());
            Faculty faculty = new Faculty(facultyId, required(name.getText(), "Faculty name"),
                    required(dept.getText(), "Department"), required(designation.getText(), "Designation"));
            if (existing == null) facultyDAO.addFaculty(faculty);
            else if (!facultyDAO.updateFaculty(faculty)) throw new IllegalStateException("No faculty record was updated.");
            loadFaculty(); refreshDashboard(); refreshPublicationChoices();
            showInfo(existing == null ? "Faculty added successfully." : "Faculty updated successfully.");
            statusBar.setText("Faculty record saved");
        } catch (Exception ex) { showError(ex); }
    }

    private void editSelectedFaculty() {
        int row = facultyTable.getSelectedRow();
        if (row < 0) { showWarning("Select a faculty row to edit."); return; }
        int id = Integer.parseInt(String.valueOf(facultyTable.getValueAt(row, 0)));
        Faculty faculty = cachedFaculty.stream().filter(f -> f.getFacultyId() == id).findFirst().orElse(null);
        if (faculty == null) { showWarning("Faculty record was not found. Refresh and try again."); return; }
        showFacultyDialog(faculty);
    }

    private void deleteSelectedFaculty() {
        int row = facultyTable.getSelectedRow();
        if (row < 0) { showWarning("Select a faculty row to delete."); return; }
        int id = Integer.parseInt(String.valueOf(facultyTable.getValueAt(row, 0)));
        String name = String.valueOf(facultyTable.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete faculty " + name + " (ID " + id + ")? Their co-author links will also be removed.",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (facultyDAO.deleteFaculty(id)) showInfo("Faculty deleted successfully.");
            else showWarning("Faculty record was not found.");
            loadFaculty(); refreshDashboard(); loadAuthorsForSelectedPublication();
        } catch (Exception ex) { showError(ex); }
    }

    private void loadPublications() {
        try {
            List<Publication> publications = publicationDAO.getAllPublications();
            DefaultTableModel model = modelOf(publicationTable);
            clear(model);
            for (Publication p : publications) model.addRow(new Object[]{p.getPublicationId(), p.getTitle(), p.getPublicationType(), p.getYear(), p.getPublisher(), nullToEmpty(p.getDoi()), nullToEmpty(p.getUrl())});
            refreshPublicationChoices();
        } catch (Exception ex) { setErrorStatus(ex); }
    }

    private void openPublicationForm() {
        PublicationForm form = new PublicationForm(() -> {
            loadPublications(); refreshPublicationChoices(); refreshDashboard();
            statusBar.setText("Publication added successfully");
        });
        form.setVisible(true);
    }

    private void refreshPublicationChoices() {
        try {
            List<Publication> publications = publicationDAO.getAllPublications();
            String authorSelected = selectedPublicationId(authorPublicationBox);
            String indexingSelected = selectedPublicationId(indexingPublicationBox);
            refillPublicationBox(authorPublicationBox, publications, authorSelected);
            refillPublicationBox(indexingPublicationBox, publications, indexingSelected);
        } catch (Exception ex) { setErrorStatus(ex); }
    }

    private void refillPublicationBox(JComboBox<PublicationOption> box, List<Publication> pubs, String selectedId) {
        box.removeAllItems();
        for (Publication p : pubs) box.addItem(new PublicationOption(p.getPublicationId(), p.getTitle()));
        if (selectedId != null) {
            for (int i = 0; i < box.getItemCount(); i++) {
                if (box.getItemAt(i).id.equals(selectedId)) { box.setSelectedIndex(i); return; }
            }
        }
        if (box.getItemCount() > 0) box.setSelectedIndex(0);
    }

    private void loadAuthorsForSelectedPublication() {
        String pubId = selectedPublicationId(authorPublicationBox);
        DefaultTableModel model = modelOf(authorsTable);
        clear(model);
        if (pubId == null) return;
        try {
            List<AuthorInfo> authors = facultyPublicationDAO.getAuthorsForPublication(pubId);
            for (AuthorInfo a : authors) model.addRow(new Object[]{a.getAuthorOrder(), a.getFacultyId(), a.getFacultyName(), a.getDepartment()});
        } catch (Exception ex) { setErrorStatus(ex); }
    }

    private void saveAuthors() {
        String pubId = selectedPublicationId(authorPublicationBox);
        if (pubId == null) { showWarning("Add a publication before setting authors."); return; }
        try {
            String input = required(authorIdsField.getText(), "Faculty IDs");
            String[] parts = input.split(",");
            List<Integer> ids = new ArrayList<>();
            for (String part : parts) ids.add(Integer.parseInt(part.trim()));
            facultyPublicationDAO.replaceAuthors(pubId, ids);
            loadAuthorsForSelectedPublication(); refreshDashboard();
            showInfo("Author list saved in the order entered.");
            statusBar.setText("Co-author assignments saved for " + pubId);
        } catch (Exception ex) { showError(ex); }
    }

    private void refreshAgencyChoices() {
        try {
            Object current = agencyBox.isEditable() ? agencyBox.getEditor().getItem() : agencyBox.getSelectedItem();
            Set<String> all = new LinkedHashSet<>();
            for (String agency : COMMON_AGENCIES) all.add(agency);
            all.addAll(indexingDAO.getAllAgencies());
            agencyBox.removeAllItems();
            for (String agency : all) agencyBox.addItem(agency);
            if (current != null) agencyBox.getEditor().setItem(current);

            Object reportCurrent = reportAgencyBox.isEditable() ? reportAgencyBox.getEditor().getItem() : reportAgencyBox.getSelectedItem();
            reportAgencyBox.removeAllItems();
            for (String agency : all) reportAgencyBox.addItem(agency);
            if (reportCurrent != null) reportAgencyBox.getEditor().setItem(reportCurrent);
        } catch (Exception ex) { setErrorStatus(ex); }
    }

    private void loadIndexingForSelectedPublication() {
        String pubId = selectedPublicationId(indexingPublicationBox);
        DefaultTableModel model = modelOf(indexingTable);
        clear(model);
        if (pubId == null) return;
        try {
            for (Indexing entry : indexingDAO.getIndexingForPublication(pubId)) model.addRow(new Object[]{entry.getIndexingName()});
        } catch (Exception ex) { setErrorStatus(ex); }
    }

    private void addIndexing() {
        String pubId = selectedPublicationId(indexingPublicationBox);
        if (pubId == null) { showWarning("Add a publication before adding indexing."); return; }
        try {
            Object selected = agencyBox.getEditor().getItem();
            String agency = required(selected == null ? "" : selected.toString(), "Indexing agency");
            indexingDAO.addIndexing(pubId, agency);
            loadIndexingForSelectedPublication(); refreshAgencyChoices(); refreshDashboard();
            showInfo("Indexing agency linked to " + pubId + ".");
            statusBar.setText("Indexing entry saved");
        } catch (Exception ex) { showError(ex); }
    }

    private void removeSelectedIndexing() {
        String pubId = selectedPublicationId(indexingPublicationBox);
        int row = indexingTable.getSelectedRow();
        if (pubId == null || row < 0) { showWarning("Select an indexing agency row to remove."); return; }
        String agency = String.valueOf(indexingTable.getValueAt(row, 0));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove " + agency + " from " + pubId + "?", "Confirm removal", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (indexingDAO.removeIndexing(pubId, agency)) showInfo("Indexing entry removed.");
            else showWarning("No matching indexing entry was found.");
            loadIndexingForSelectedPublication(); refreshAgencyChoices(); refreshDashboard();
        } catch (Exception ex) { showError(ex); }
    }

    private void updateReportFields() {
        String mode = String.valueOf(reportModeBox.getSelectedItem());
        reportFacultyId.setEnabled("Faculty-wise".equals(mode));
        reportStartYear.setEnabled("Year range".equals(mode));
        reportEndYear.setEnabled("Year range".equals(mode));
        reportTypeBox.setEnabled("Publication type".equals(mode));
        reportAgencyBox.setEnabled("Indexing agency".equals(mode));
    }

    private void runReport() {
        try {
            String mode = String.valueOf(reportModeBox.getSelectedItem());
            currentReportRows = switch (mode) {
                case "All publications" -> reportDAO.getAllPublications();
                case "Faculty-wise" -> reportDAO.byFaculty(Integer.parseInt(required(reportFacultyId.getText(), "Faculty ID")));
                case "Year range" -> reportDAO.byYearRange(Integer.parseInt(required(reportStartYear.getText(), "Start year")), Integer.parseInt(required(reportEndYear.getText(), "End year")));
                case "Publication type" -> reportDAO.byType(String.valueOf(reportTypeBox.getSelectedItem()));
                case "Indexing agency" -> reportDAO.byIndexing(required(String.valueOf(reportAgencyBox.getEditor().getItem()), "Indexing agency"));
                default -> new ArrayList<>();
            };
            DefaultTableModel model = modelOf(reportTable);
            clear(model);
            for (PublicationReportRow r : currentReportRows) {
                model.addRow(new Object[]{r.getPublicationId(), r.getTitle(), r.getPublicationType(), r.getYear(), r.getPublisher(),
                        r.getAuthors(), r.getIndexingAgencies(), nullToEmpty(r.getDoi()), nullToEmpty(r.getUrl())});
            }
            reportCountLabel.setText(currentReportRows.size() + " matching publication(s)");
            statusBar.setText("Report ready: " + currentReportRows.size() + " record(s)");
        } catch (Exception ex) { showError(ex); }
    }

    private void exportReport() {
        if (currentReportRows.isEmpty()) { showWarning("Run a report with matching rows before exporting."); return; }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export publication report to CSV");
        chooser.setSelectedFile(new java.io.File("publication-report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File selected = chooser.getSelectedFile();
        if (!selected.getName().toLowerCase().endsWith(".csv")) selected = new java.io.File(selected.getParentFile(), selected.getName() + ".csv");
        try {
            reportDAO.exportCsv(currentReportRows, Path.of(selected.getAbsolutePath()));
            showInfo("CSV report saved to:\n" + selected.getAbsolutePath());
            statusBar.setText("CSV exported: " + selected.getName());
        } catch (Exception ex) { showError(ex); }
    }

    private void loadAnalytics() {
        try {
            String mode = String.valueOf(analyticsModeBox.getSelectedItem());
            List<Metric> metrics = switch (mode) {
                case "Department-wise publication totals" -> analyticsDAO.departmentWisePublicationCounts();
                case "Annual publication trend" -> analyticsDAO.annualPublicationTrends();
                case "Publication counts by category" -> analyticsDAO.publicationCountsByType();
                case "Publication counts by indexing agency" -> analyticsDAO.publicationCountsByIndexing();
                default -> new ArrayList<>();
            };
            DefaultTableModel model = modelOf(analyticsTable);
            clear(model);
            for (Metric metric : metrics) model.addRow(new Object[]{metric.getLabel(), metric.getCount()});
            analyticsCountLabel.setText(metrics.size() + " category/period(s)");
            barChart.setMetrics(metrics);
            statusBar.setText("Analytics refreshed");
        } catch (Exception ex) { setErrorStatus(ex); showError(ex); }
    }

    private void setErrorStatus(Exception ex) {
        statusBar.setText("Could not load data: " + shortMessage(ex));
    }

    private void showError(Exception ex) {
        String message = shortMessage(ex);
        statusBar.setText("Operation failed: " + message);
        JOptionPane.showMessageDialog(this, message, "Operation failed", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Attention", JOptionPane.WARNING_MESSAGE);
    }

    private String shortMessage(Throwable ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) return ex.getClass().getSimpleName();
        if (message.contains("Duplicate entry")) return "A record with this ID or agency assignment already exists.";
        if (message.toLowerCase().contains("foreign key constraint")) return "The selected faculty/publication ID does not exist or is still linked to another record.";
        if (message.contains("Communications link failure") || message.contains("Connection refused")) return "Cannot connect to MySQL. Check that the service is running and DB_URL, DB_USER and DB_PASSWORD are set.";
        return message;
    }

    private String selectedPublicationId(JComboBox<PublicationOption> box) {
        PublicationOption selected = (PublicationOption) box.getSelectedItem();
        return selected == null ? null : selected.id;
    }

    private String required(String text, String name) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException(name + " cannot be empty.");
        return text.trim();
    }

    private String nullToEmpty(String value) { return value == null ? "" : value; }

    private JPanel contentPanel() {
        JPanel panel = new ResponsivePanel();
        panel.setBackground(LIGHT);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        return panel;
    }

    /** Wrap a whole tab in a scroll pane so smaller displays never hide controls. */
    private JComponent scrollableTab(JComponent component) {
        JScrollPane scroll = new JScrollPane(component,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JComponent filterField(String labelText, JComponent field) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        item.add(label);
        item.add(field);
        return item;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 230, 239)), new EmptyBorder(14, 14, 14, 14)));
        return panel;
    }

    private JPanel heading(String titleText, String subtitleText) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        return panel;
    }

    private JButton actionButton(String text, Runnable action, boolean primary) {
        JButton button = new JButton(text);
        styleButton(button, primary);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void styleButton(JButton button, boolean primary) {
        // Use a basic Swing button delegate so macOS native Aqua rendering cannot
        // ignore the foreground/background colours and make primary labels unreadable.
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        if (primary) {
            button.setBackground(BLUE);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BLUE), new EmptyBorder(7, 12, 7, 12)));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(TEXT);
            button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(213, 221, 233)), new EmptyBorder(7, 12, 7, 12)));
        }
        button.setPreferredSize(null);
        button.setMinimumSize(new Dimension(72, 32));
    }

    private JTable makeTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(27);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setGridColor(new Color(232, 236, 243));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(220, 233, 253));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(239, 243, 249));
        table.getTableHeader().setForeground(TEXT);
        table.setFillsViewportHeight(true);
        // Keep wide report/publication columns readable and expose a horizontal
        // scrollbar instead of compressing them until values become invisible.
        if (columns.length >= 7) table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }

    private void setColumnWidths(JTable table, int... widths) {
        int count = Math.min(table.getColumnModel().getColumnCount(), widths.length);
        for (int i = 0; i < count; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private DefaultTableModel modelOf(JTable table) { return (DefaultTableModel) table.getModel(); }
    private void clear(DefaultTableModel model) { model.setRowCount(0); }

    /** Content panel that fills the viewport when possible and becomes scrollable when not. */
    private static final class ResponsivePanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 22; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(40, (orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width) - 40);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() {
            Container parent = getParent();
            return parent instanceof JViewport viewport && getPreferredSize().height <= viewport.getHeight();
        }
    }

    private static final class PublicationOption {
        private final String id;
        private final String title;
        private PublicationOption(String id, String title) { this.id = id; this.title = title; }
        @Override public String toString() { return id + " — " + title; }
    }

    private static final class BarChartPanel extends JPanel {
        private List<Metric> metrics = new ArrayList<>();
        private BarChartPanel() { setBackground(Color.WHITE); setPreferredSize(new Dimension(400, 300)); }
        void setMetrics(List<Metric> metrics) { this.metrics = new ArrayList<>(metrics); revalidate(); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 18, top = 18, labelWidth = Math.min(155, getWidth() / 3), right = 20;
            int barArea = Math.max(90, getWidth() - left - labelWidth - right - 48);
            int rowHeight = Math.max(30, Math.min(48, (getHeight() - 35) / Math.max(1, metrics.size())));
            long max = metrics.stream().mapToLong(Metric::getCount).max().orElse(1);
            if (metrics.isEmpty()) {
                g2.setColor(MUTED); g2.drawString("No data available for this metric yet.", left, top + 18);
            } else {
                for (int i = 0; i < metrics.size(); i++) {
                    Metric metric = metrics.get(i);
                    int y = top + i * rowHeight;
                    String label = metric.getLabel() == null ? "" : metric.getLabel();
                    if (label.length() > 20) label = label.substring(0, 18) + "…";
                    g2.setColor(TEXT); g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    g2.drawString(label, left, y + 16);
                    int bx = left + labelWidth;
                    int bw = (int) Math.round(barArea * metric.getCount() / (double) max);
                    g2.setColor(new Color(232, 238, 248));
                    g2.fillRoundRect(bx, y + 3, barArea, 18, 8, 8);
                    g2.setColor(BLUE);
                    g2.fillRoundRect(bx, y + 3, Math.max(4, bw), 18, 8, 8);
                    g2.setColor(TEXT);
                    g2.drawString(String.valueOf(metric.getCount()), bx + barArea + 8, y + 17);
                }
            }
            g2.dispose();
        }
    }
}
