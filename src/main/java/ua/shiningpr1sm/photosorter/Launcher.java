package ua.shiningpr1sm.photosorter;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Launcher {
    public static void main(String[] args) {
        ConfigManager.initConfig();

        String currentVer = ConfigManager.getInternalVersion();
        String latestVer = ConfigManager.getLatestVersion();

        if (latestVer != null && !latestVer.equals(currentVer)) {
            try {
                Path tempJar = Paths.get("FileOrganizer_new.jar");
                ConfigManager.downloadNewVersion(tempJar);
                restartAndApply(tempJar);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(FileOrganizerSwing::new);
    }

    private static void restartAndApply(Path tempJar) {
        try {
            String currentJar = "FileOrganizer.jar";
            String tempJarName = tempJar.getFileName().toString();

            String script = String.format(
                    "timeout /t 5 && del /f \"%s\" && move /y \"%s\" \"%s\" && pause",
                    currentJar, tempJarName, currentJar
            );

            new ProcessBuilder("cmd", "/c", script).start();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}