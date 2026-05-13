package ua.shiningpr1sm.photosorter;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Launcher {
    public static void main(String[] args) {
        ConfigManager.initConfig();

        String currentVer = ConfigManager.getProperty("version");
        String latestVer = ConfigManager.getLatestVersion();

        if (latestVer != null && !latestVer.equals(currentVer)) {
            try {
                Path tempJar = Paths.get("File Organizer_new.jar");
                ConfigManager.downloadNewVersion(tempJar);

                ConfigManager.setProperty("version", latestVer);
                restartAndApply(tempJar);
                return;
            } catch (Exception e) {
                System.err.println("Update failed, starting current version...");
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            FileOrganizerSwing mainApp = new FileOrganizerSwing();
            mainApp.setVisible(true);
        });
    }
    private static void restartAndApply(Path tempJar) {
        try {
            String currentJar = "File Organizer.jar";
            String tempJarName = tempJar.getFileName().toString();

            // Windows command:
            // 1. Wait 2 seconds until process die
            // 2. Del old JAR
            // 3. Rename new JAR
            // 4. Starting new JAR
            String script = String.format(
                    "timeout /t 2 && del /f %s && move /y %s %s && start javaw -jar %s",
                    currentJar, tempJarName, currentJar, currentJar
            );

            new ProcessBuilder("cmd", "/c", script).start();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}