package TA_Job_Application_Module.bootstrap;

import TA_Job_Application_Module.portal.TAPortalApp;
import TA_Job_Application_Module.service.DataService;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        // Save the previous Look and Feel (possibly set by the authentication module).
        String oldLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        UIManager.put("OptionPane.yesButtonText", "Yes");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancel");

        SwingUtilities.invokeLater(() -> {
            TAPortalApp app = new TAPortalApp("jobs");
            app.setVisible(true);
            
            // When the window closes, clear cached AI analysis and restore Look and Feel.
            app.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Clear cached AI analysis results.
                    DataService.getInstance().clearCachedAIResults();
                    
                    try {
                        UIManager.setLookAndFeel(oldLookAndFeel);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
    }
}
