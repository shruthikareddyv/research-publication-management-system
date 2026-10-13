package org.example.ui;

import org.example.dao.PublicationDAO;
import org.example.model.Publication;
import org.example.model.PublicationDetails;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/** Cleaned-up replacement for Member 3's misplaced PublicationForm source. */
public class PublicationForm extends JFrame {
    private final JTextField idField = new JTextField(22);
    private final JTextField titleField = new JTextField(22);
    private final JTextField yearField = new JTextField(22);
    private final JTextField publisherField = new JTextField(22);
    private final JTextField doiField = new JTextField(22);
    private final JTextField urlField = new JTextField(22);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Journal", "Conference", "Book", "Book Chapter"});
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel detailsCards = new JPanel(cardLayout);

    private final JTextField volumeField = new JTextField(22);
    private final JTextField issueField = new JTextField(22);
    private final JTextField journalPagesField = new JTextField(22);
    private final JTextField conferenceDateField = new JTextField(22);
    private final JTextField isbnField = new JTextField(22);
    private final JTextField editionField = new JTextField(22);
    private final JTextField bookTitleField = new JTextField(22);
    private final JTextField chapterNumberField = new JTextField(22);
    private final JTextField chapterPagesField = new JTextField(22);
    private final Runnable onSaved;

    public PublicationForm() {
        this(() -> { });
    }

    public PublicationForm(Runnable onSaved) {
        this.onSaved = onSaved == null ? () -> { } : onSaved;
        setTitle("Research Publication Management System - Publication Entry");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(650, 690);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("Publication Entry", SwingConstants.CENTER);
        heading.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(heading, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        JPanel common = new JPanel(new GridLayout(0, 2, 10, 9));
        common.add(new JLabel("Publication ID (max 10):")); common.add(idField);
        common.add(new JLabel("Title:")); common.add(titleField);
        common.add(new JLabel("Year:")); common.add(yearField);
        common.add(new JLabel("Publisher:")); common.add(publisherField);
        common.add(new JLabel("DOI (optional):")); common.add(doiField);
        common.add(new JLabel("URL (optional):")); common.add(urlField);
        common.add(new JLabel("Publication Type:")); common.add(typeBox);
        main.add(common, BorderLayout.NORTH);

        detailsCards.add(makeDetailsPanel(new String[]{"Volume:", "Issue:", "Pages:"},
                new JTextField[]{volumeField, issueField, journalPagesField}), "Journal");
        detailsCards.add(makeDetailsPanel(new String[]{"Conference Date (YYYY-MM-DD):"},
                new JTextField[]{conferenceDateField}), "Conference");
        detailsCards.add(makeDetailsPanel(new String[]{"ISBN:", "Edition:"},
                new JTextField[]{isbnField, editionField}), "Book");
        detailsCards.add(makeDetailsPanel(new String[]{"Book Title:", "Chapter Number:", "Pages:"},
                new JTextField[]{bookTitleField, chapterNumberField, chapterPagesField}), "Book Chapter");
        detailsCards.setBorder(BorderFactory.createTitledBorder("Type-specific details"));
        main.add(detailsCards, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Publication");
        JButton clearButton = new JButton("Clear Form");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        actions.add(saveButton); actions.add(clearButton);
        add(actions, BorderLayout.SOUTH);

        typeBox.addActionListener(e -> cardLayout.show(detailsCards, (String) typeBox.getSelectedItem()));
        saveButton.addActionListener(e -> savePublication());
        clearButton.addActionListener(e -> clearForm());
        cardLayout.show(detailsCards, "Journal");
        setVisible(true);
    }

    private JPanel makeDetailsPanel(String[] labels, JTextField[] fields) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 9));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            panel.add(fields[i]);
        }
        return panel;
    }

    private void savePublication() {
        try {
            String id = required(idField.getText(), "Publication ID");
            String title = required(titleField.getText(), "Title");
            int year = Integer.parseInt(required(yearField.getText(), "Year"));
            String publisher = required(publisherField.getText(), "Publisher");
            String type = (String) typeBox.getSelectedItem();

            PublicationDetails details = new PublicationDetails();
            switch (type) {
                case "Journal" -> {
                    details.setVolume(required(volumeField.getText(), "Volume"));
                    details.setIssue(required(issueField.getText(), "Issue"));
                    details.setPages(required(journalPagesField.getText(), "Pages"));
                }
                case "Conference" -> details.setConferenceDate(required(conferenceDateField.getText(), "Conference date"));
                case "Book" -> {
                    details.setIsbn(required(isbnField.getText(), "ISBN"));
                    details.setEdition(required(editionField.getText(), "Edition"));
                }
                case "Book Chapter" -> {
                    details.setBookTitle(required(bookTitleField.getText(), "Book title"));
                    details.setChapterNumber(Integer.parseInt(required(chapterNumberField.getText(), "Chapter number")));
                    details.setPages(required(chapterPagesField.getText(), "Pages"));
                }
                default -> throw new IllegalArgumentException("Choose a valid publication type.");
            }
            Publication publication = new Publication(id, title, year, publisher,
                    doiField.getText().trim(), urlField.getText().trim(), type);
            new PublicationDAO().addPublication(publication, details);
            JOptionPane.showMessageDialog(this, "Publication saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            onSaved.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year and chapter number must be whole numbers.", "Input error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save publication: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " cannot be empty.");
        return value.trim();
    }

    private void clearForm() {
        idField.setText(""); titleField.setText(""); yearField.setText(""); publisherField.setText("");
        doiField.setText(""); urlField.setText(""); volumeField.setText(""); issueField.setText("");
        journalPagesField.setText(""); conferenceDateField.setText(""); isbnField.setText("");
        editionField.setText(""); bookTitleField.setText(""); chapterNumberField.setText(""); chapterPagesField.setText("");
        typeBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(PublicationForm::new);
    }
}
