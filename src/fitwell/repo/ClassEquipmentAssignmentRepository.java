package fitwell.repo;

import fitwell.db.Db;
import fitwell.entity.ClassEquipmentAssignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassEquipmentAssignmentRepository {

    public List<ClassEquipmentAssignment> findAll() {
        List<ClassEquipmentAssignment> out = new ArrayList<>();
        String sql = "SELECT AssignmentID, ClassID, SerialNumber, RequiredQuantity, Notes FROM ClassEquipmentAssignment";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load class-equipment assignments: " + ex.getMessage(), ex);
        }
        return out;
    }

    public List<ClassEquipmentAssignment> findByClassId(int classId) {
        List<ClassEquipmentAssignment> out = new ArrayList<>();
        String sql = "SELECT AssignmentID, ClassID, SerialNumber, RequiredQuantity, Notes " +
                     "FROM ClassEquipmentAssignment WHERE ClassID=?";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }

        } catch (Exception ex) {
            throw new RuntimeException("Failed to load assignments for class " + classId + ": " + ex.getMessage(), ex);
        }
        return out;
    }

    public void insert(ClassEquipmentAssignment a) {
        validate(a);

        String sql = "INSERT INTO ClassEquipmentAssignment (ClassID, SerialNumber, RequiredQuantity, Notes) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, a.getClassId());
            ps.setString(2, a.getSerialNumber());
            ps.setInt(3, a.getRequiredQuantity());
            ps.setString(4, a.getNotes());

            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to insert class-equipment assignment: " + ex.getMessage(), ex);
        }
    }

    public void deleteByClassId(int classId) {
        String sql = "DELETE FROM ClassEquipmentAssignment WHERE ClassID=?";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete assignments for class " + classId + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Replaces all equipment assignments for a class in one transaction.
     */
    public void replaceForClass(int classId, List<ClassEquipmentAssignment> assignments) {
        if (classId <= 0) throw new IllegalArgumentException("classId must be > 0");
        if (assignments == null) assignments = new ArrayList<>();

        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);

            try {
                try (PreparedStatement del = c.prepareStatement(
                        "DELETE FROM ClassEquipmentAssignment WHERE ClassID=?")) {
                    del.setInt(1, classId);
                    del.executeUpdate();
                }

                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO ClassEquipmentAssignment (ClassID, SerialNumber, RequiredQuantity, Notes) VALUES (?, ?, ?, ?)")) {

                    for (ClassEquipmentAssignment a : assignments) {
                        validate(a);
                        ins.setInt(1, classId);
                        ins.setString(2, a.getSerialNumber());
                        ins.setInt(3, a.getRequiredQuantity());
                        ins.setString(4, a.getNotes());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }

        } catch (Exception ex) {
            throw new RuntimeException("Failed to replace class-equipment assignments: " + ex.getMessage(), ex);
        }
    }

    private void validate(ClassEquipmentAssignment a) {
        if (a == null) throw new IllegalArgumentException("Assignment is null");
        if (a.getClassId() <= 0) throw new IllegalArgumentException("ClassID must be > 0");
        if (a.getSerialNumber() == null || a.getSerialNumber().trim().isEmpty())
            throw new IllegalArgumentException("SerialNumber is required");
        if (a.getRequiredQuantity() <= 0)
            throw new IllegalArgumentException("RequiredQuantity must be > 0");
    }

    private ClassEquipmentAssignment map(ResultSet rs) throws SQLException {
        return new ClassEquipmentAssignment(
                rs.getInt("AssignmentID"),
                rs.getInt("ClassID"),
                rs.getString("SerialNumber"),
                rs.getInt("RequiredQuantity"),
                rs.getString("Notes")
        );
    }
}