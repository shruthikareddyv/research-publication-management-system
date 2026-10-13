package org.example;

import org.example.ui.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Graphical entry point for the Research Publication Management System. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            ConsoleMain.main(new String[0]);
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Use Java's default Swing look and feel if the system one is unavailable.
        }
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
