package fitwell.persistence.db;

import fitwell.config.AppPaths;

import java.sql.Connection;
import java.sql.DriverManager;

public class Db {

    public static Connection getConnection() throws Exception {
        AppPaths.assertRequiredFilesExist();

        String dbPath = AppPaths.dbFile().getAbsolutePath();
        String url = "jdbc:ucanaccess://" + dbPath;

        return DriverManager.getConnection(url);
    }
}
