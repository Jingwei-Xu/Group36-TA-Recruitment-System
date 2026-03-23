package com.ta.recruitment;

import com.ta.recruitment.dao.JobDao;
import com.ta.recruitment.dao.JobDaoFileImpl;
import com.ta.recruitment.service.JobService;
import com.ta.recruitment.ui.JobManagementFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep default LookAndFeel if system style fails.
            }

            JobDao jobDao = new JobDaoFileImpl(Path.of("data"));
            JobService jobService = new JobService(jobDao);
            JobManagementFrame frame = new JobManagementFrame(jobService);
            frame.setVisible(true);
        });
    }
}
