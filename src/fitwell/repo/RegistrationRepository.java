package fitwell.repo;

import fitwell.db.Db;
import fitwell.domain.registration.ClassRegistration;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class RegistrationRepository {

    public List<ClassRegistration> findByClassId(int classId) {
        List<ClassRegistration> result = new ArrayList<>();
        for (ClassRegistration registration : findAll()) {
            if (registration.getClassId() == classId) {
                result.add(registration);
            }
        }
        return result;
    }

    public List<ClassRegistration> findByTraineeId(int traineeId) {
        List<ClassRegistration> result = new ArrayList<>();
        for (ClassRegistration registration : findAll()) {
            if (registration.getTraineeId() == traineeId) {
                result.add(registration);
            }
        }
        return result;
    }

    public int countRegistrationsForClass(int classId) {
        String sql = "SELECT COUNT(*) AS cnt FROM TblClassRegistration WHERE classID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (Exception ex) {
            throw new RuntimeException("DB count registrations failed", ex);
        }
    }

    public boolean isRegistered(int classId, int traineeId) {
        String sql = "SELECT 1 FROM TblClassRegistration WHERE classID=? AND traineeID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, traineeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            throw new RuntimeException("DB isRegistered failed", ex);
        }
    }

    public void register(int classId, int traineeId, LocalDateTime when) {
        String sql = "INSERT INTO TblClassRegistration (classID, traineeID, registrationDate, registrationTime) VALUES (?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, classId);
            ps.setInt(2, traineeId);
            ps.setDate(3, java.sql.Date.valueOf(when.toLocalDate()));
            ps.setTimestamp(4, Timestamp.valueOf(when));
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB register failed", ex);
        }
    }

    public void unregister(int classId, int traineeId) {
        String sql = "DELETE FROM TblClassRegistration WHERE classID=? AND traineeID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, traineeId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB unregister failed", ex);
        }
    }

    public List<ClassRegistration> findAll() {
        String sql = "SELECT classID, traineeID, registrationTime FROM TblClassRegistration ORDER BY registrationTime DESC";
        List<ClassRegistration> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int cid = rs.getInt("classID");
                int tid = rs.getInt("traineeID");
                Timestamp ts = rs.getTimestamp("registrationTime");
                LocalDateTime dt = (ts == null ? LocalDateTime.now() : ts.toLocalDateTime());
                out.add(new ClassRegistration(cid, tid, dt));
            }
            return out;
        } catch (Exception ex) {
            throw new RuntimeException("DB findAll registrations failed", ex);
        }
    }

    // Returns class IDs with zero registrations in the given year
    public List<Integer> findUnregisteredClassIdsByYear(int year) {
        String sql = """
                SELECT c.classID
                FROM TblTrainingClass c
                LEFT JOIN TblClassRegistration r ON c.classID = r.classID
                WHERE YEAR(c.startTime)=?
                GROUP BY c.classID
                HAVING COUNT(r.traineeID)=0
                ORDER BY c.classID
                """;
        List<Integer> ids = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("classID"));
            }
            return ids;
        } catch (Exception ex) {
            throw new RuntimeException("DB unregistered report query failed", ex);
        }
    }
}
