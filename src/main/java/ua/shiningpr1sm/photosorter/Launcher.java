package ua.shiningpr1sm.photosorter;

import javax.swing.*;
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
            Path currentJarPath = Paths.get(Launcher.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());

            String tempJarPath = tempJar.toAbsolutePath().toString();
            String targetJarPath = currentJarPath.toAbsolutePath().toString();

            String script = String.format(
                    "timeout /t 3 && del /f \"%s\" && move /y \"%s\" \"%s\" && start javaw -jar \"%s\"",
                    targetJarPath, tempJarPath, targetJarPath, targetJarPath
            );

            new ProcessBuilder("cmd", "/c", script).start();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}