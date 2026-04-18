package fitwell.repo;

import fitwell.db.Db;
import fitwell.entity.TrainingClass;
import fitwell.entity.TrainingClassStatus;

import java.sql.*;
import java.util.*;

public class TrainingClassRepository {

    public TrainingClass findById(int classId) {
        String sql = "SELECT classID, name, startTime, endTime, type, maxParticipants, consultantID FROM TblTrainingClass WHERE classID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Timestamp st = rs.getTimestamp("startTime");
                Timestamp et = rs.getTimestamp("endTime");
                return new TrainingClass(
                        rs.getInt("classID"),
                        rs.getString("name"),
                        st == null ? null : st.toLocalDateTime(),
                        et == null ? null : et.toLocalDateTime(),
                        rs.getString("type"),
                        rs.getInt("maxParticipants"),
                        rs.getInt("consultantID")
                );
            }
        } catch (Exception ex) {
            throw new RuntimeException("DB findById class failed", ex);
        }
    }

    public List<TrainingClass> findAll() {
        String sql = "SELECT classID, name, startTime, endTime, type, maxParticipants, consultantID FROM TblTrainingClass ORDER BY startTime";
        List<TrainingClass> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Integer id = rs.getInt("classID");
                String name = rs.getString("name");
                Timestamp st = rs.getTimestamp("startTime");
                Timestamp et = rs.getTimestamp("endTime");
                String type = rs.getString("type");
                int max = rs.getInt("maxParticipants");
                int cons = rs.getInt("consultantID");

                out.add(new TrainingClass(
                        id,
                        name,
                        st == null ? null : st.toLocalDateTime(),
                        et == null ? null : et.toLocalDateTime(),
                        type,
                        max,
                        cons
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new RuntimeException("DB findAll classes failed", ex);
        }
    }

    public int insert(TrainingClass tc) {
        String sql = "INSERT INTO TblTrainingClass (name, startTime, endTime, type, maxParticipants, consultantID) VALUES (?,?,?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tc.getName());
            ps.setTimestamp(2, Timestamp.valueOf(tc.getStartTime()));
            ps.setTimestamp(3, Timestamp.valueOf(tc.getEndTime()));
            ps.setString(4, tc.getType());
            ps.setInt(5, tc.getMaxParticipants());
            ps.setInt(6, tc.getConsultantId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            // UCanAccess sometimes doesn’t return keys reliably:
            return findLastInsertedId();
        } catch (Exception ex) {
            throw new RuntimeException("DB insert class failed", ex);
        }
    }

    private int findLastInsertedId() throws Exception {
        String sql = "SELECT MAX(classID) AS maxId FROM TblTrainingClass";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("maxId") : -1;
        }
    }

    public void update(TrainingClass tc) {
        String sql = "UPDATE TblTrainingClass SET name=?, startTime=?, endTime=?, type=?, maxParticipants=?, consultantID=? WHERE classID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tc.getName());
            ps.setTimestamp(2, Timestamp.valueOf(tc.getStartTime()));
            ps.setTimestamp(3, Timestamp.valueOf(tc.getEndTime()));
            ps.setString(4, tc.getType());
            ps.setInt(5, tc.getMaxParticipants());
            ps.setInt(6, tc.getConsultantId());
            ps.setInt(7, tc.getClassId());

            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB update class failed", ex);
        }
    }

    public void updateStatus(int classId, TrainingClassStatus status) {
        String sql = "UPDATE TblTrainingClass SET status=? WHERE classID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status == null ? TrainingClassStatus.SCHEDULED.name() : status.name());
            ps.setInt(2, classId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB updateStatus class failed", ex);
        }
    }

    public TrainingClassStatus findStatus(int classId) {
        String sql = "SELECT status FROM TblTrainingClass WHERE classID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String s = rs.getString("status");
                    if (s != null && !s.isBlank()) {
                        try { return TrainingClassStatus.valueOf(s); } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return TrainingClassStatus.SCHEDULED;
    }

    public void delete(int classId) {
        // Delete registrations first to avoid FK constraint violation
        String delRegs = "DELETE FROM TblClassRegistration WHERE classID=?";
        String delClass = "DELETE FROM TblTrainingClass WHERE classID=?";

        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = c.prepareStatement(delRegs)) {
                    ps1.setInt(1, classId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = c.prepareStatement(delClass)) {
                    ps2.setInt(1, classId);
                    ps2.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("DB delete class failed", ex);
        }
    }
}
