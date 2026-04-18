package fitwell.config;

import java.io.File;
import java.nio.file.Path;

public class AppPaths {

    public static File dbFile() {
        File inDist = new File(System.getProperty("user.dir"), "dist/db/fitwell_2.accdb");
        if (inDist.exists()) return inDist;

        File inRoot = new File(System.getProperty("user.dir"), "db/fitwell_2.accdb");
        if (inRoot.exists()) return inRoot;

        return inDist;
    }

    public static Path jrxmlPath() {
        File inDist = new File(System.getProperty("user.dir"), "dist/reports/UnregisteredClassesReport.jrxml");
        if (inDist.exists()) return inDist.toPath();

        File inRoot = new File(System.getProperty("user.dir"), "reports/UnregisteredClassesReport.jrxml");
        if (inRoot.exists()) return inRoot.toPath();

        File inResources = new File(System.getProperty("user.dir"), "src/main/resources/reports/UnregisteredClassesReport.jrxml");
        if (inResources.exists()) return inResources.toPath();

        return inDist.toPath(); // best guess for error message
    }

    public static void assertRequiredFilesExist() {
        File db = dbFile();
        if (!db.exists()) {
            throw new IllegalStateException(
                    "DB file not found: " + db.getAbsolutePath() + "\n" +
                    "Place it next to the JAR in: db/fitwell_2.accdb"
            );
        }
    }
    
    public static Path reportsOutputDir() {
        File base = new File(System.getProperty("user.dir"));

        // if we are already inside "dist", output should be "reports_out" (not dist/reports_out)
        File out = base.getName().equalsIgnoreCase("dist")
                ? new File(base, "reports_out")
                : new File(base, "dist/reports_out");

        if (!out.exists()) out.mkdirs();
        return out.toPath();
    }

}
