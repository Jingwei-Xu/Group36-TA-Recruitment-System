package edu.ebu6304.app;

import edu.ebu6304.app.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Files;
import java.nio.file.Path;

public class TAManagementApp {
    public static void main(String[] args) {
        String defaultDataPath = "D:\\Code\\Program\\EBU6304\\3.29\\data";
        String dataPath = args != null && args.length > 0 && args[0] != null && !args[0].isBlank()
                ? args[0]
                : defaultDataPath;

        if (!Files.exists(Path.of(dataPath))) {
            JOptionPane.showMessageDialog(
                    null,
                    "Data path does not exist: " + Path.of(dataPath).toAbsolutePath(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 javax.swing.UnsupportedLookAndFeelException ignored) {
        }

        final String finalDataPath = dataPath;
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(finalDataPath);
            frame.setVisible(true);
        });
    }
}
