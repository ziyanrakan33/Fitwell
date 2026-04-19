package fitwell.persistence.jdbc;

import fitwell.domain.equipment.EquipmentAssignment;
import fitwell.persistence.api.EquipmentAssignmentRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcEquipmentAssignmentRepository implements EquipmentAssignmentRepository {

    public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
    }

    private final ConnectionProvider connectionProvider;

    public JdbcEquipmentAssignmentRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public int insert(EquipmentAssignment a) throws SQLException {
        String sql = "INSERT INTO EquipmentAssignments " +
                "(class_id, equipment_serial, qty_assigned, assigned_at, assigned_by_consultant_id, notes, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = connectionProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getClassId());
            ps.setString(2, a.getEquipmentSerial());
            ps.setInt(3, a.getQtyAssigned());
            ps.setTimestamp(4, a.getAssignedAt() != null
                    ? Timestamp.valueOf(a.getAssignedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            if (a.getAssignedByConsultantId() == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, a.getAssignedByConsultantId());
            ps.setString(6, a.getNotes());
            ps.setBoolean(7, a.isActive());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    a.setAssignmentId(id);
                    return id;
                }
            }
            return -1;
        }
    }

    public List<EquipmentAssignment> findActiveByClassId(int classId) throws SQLException {
        String sql = "SELECT * FROM EquipmentAssignments WHERE class_id=? AND is_active=true ORDER BY assignment_id";
        List<EquipmentAssignment> out = new ArrayList<>();
        try (Connection con = connectionProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public List<EquipmentAssignment> findActiveByEquipmentSerial(String serial) throws SQLException {
        String sql = "SELECT * FROM EquipmentAssignments WHERE equipment_serial=? AND is_active=true ORDER BY assignment_id";
        List<EquipmentAssignment> out = new ArrayList<>();
        try (Connection con = connectionProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, serial);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public void deactivateById(int assignmentId) throws SQLException {
        String sql = "UPDATE EquipmentAssignments SET is_active=false WHERE assignment_id=?";
        try (Connection con = connectionProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.executeUpdate();
        }
    }

    public int sumActiveQtyByClassIdAndSerial(int classId, String serial) throws SQLException {
        String sql = "SELECT SUM(qty_assigned) AS total_qty " +
                "FROM EquipmentAssignments WHERE class_id=? AND equipment_serial=? AND is_active=true";
        try (Connection con = connectionProvider.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setString(2, serial);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_qty");
                    return rs.wasNull() ? 0 : total;
                }
                return 0;
            }
        }
    }

    private EquipmentAssignment map(ResultSet rs) throws SQLException {
        EquipmentAssignment a = new EquipmentAssignment();
        a.setAssignmentId(rs.getInt("assignment_id"));
        a.setClassId(rs.getInt("class_id"));
        a.setEquipmentSerial(rs.getString("equipment_serial"));
        a.setQtyAssigned(rs.getInt("qty_assigned"));
        Timestamp ts = rs.getTimestamp("assigned_at");
        if (ts != null) a.setAssignedAt(ts.toLocalDateTime());
        int consultantId = rs.getInt("assigned_by_consultant_id");
        if (!rs.wasNull()) a.setAssignedByConsultantId(consultantId);
        a.setNotes(rs.getString("notes"));
        a.setActive(rs.getBoolean("is_active"));
        return a;
    }
}
