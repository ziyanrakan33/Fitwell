package fitwell.persistence.db;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Ensures required database tables exist. Creates them if missing.
 */
public final class DbMigration {

    private DbMigration() {}

    public static void ensureTablesExist() {
        ensureClassEquipmentAssignmentTable();
        ensureEquipmentAssignmentsTable();
        ensureTblEquipmentAssignmentTable();
        ensureTrainingClassStatusColumn();
    }

    private static void ensureTrainingClassStatusColumn() {
        String sql = "ALTER TABLE TblTrainingClass ADD COLUMN status VARCHAR(20) DEFAULT 'SCHEDULED'";
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception ignored) {
        }
    }

    private static void ensureTblEquipmentAssignmentTable() {
        String sql = "CREATE TABLE TblEquipmentAssignment (" +
                "ID AUTOINCREMENT PRIMARY KEY, " +
                "serialNumber VARCHAR(255), " +
                "classID INTEGER)";
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception ignored) {
        }
    }

    private static void ensureClassEquipmentAssignmentTable() {
        String sql = "CREATE TABLE ClassEquipmentAssignment (" +
                "AssignmentID AUTOINCREMENT PRIMARY KEY, " +
                "ClassID INTEGER, " +
                "SerialNumber VARCHAR(255), " +
                "RequiredQuantity INTEGER, " +
                "Notes VARCHAR(255))";
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception ignored) {
        }
    }

    private static void ensureEquipmentAssignmentsTable() {
        String sql = "CREATE TABLE EquipmentAssignments (" +
                "assignment_id AUTOINCREMENT PRIMARY KEY, " +
                "class_id INTEGER, " +
                "equipment_serial VARCHAR(255), " +
                "qty_assigned INTEGER, " +
                "assigned_at DATETIME, " +
                "assigned_by_consultant_id INTEGER, " +
                "notes VARCHAR(255), " +
                "is_active YESNO DEFAULT TRUE)";
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception ignored) {
        }
    }
}
