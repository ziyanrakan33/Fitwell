package fitwell.repo;

import fitwell.db.Db;
import fitwell.entity.Trainee;
import fitwell.entity.PreferredUpdateMethod;

import java.sql.*;
import java.util.*;

public class TraineeRepository {

    public Trainee findById(int traineeId) {
        for (Trainee trainee : findAll()) {
            if (trainee.getId() != null && trainee.getId() == traineeId) {
                return trainee;
            }
        }
        return null;
    }

    public Trainee findByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        for (Trainee t : findAll()) {
            if (email.equalsIgnoreCase(t.getEmail())) {
                return t;
            }
        }
        return null;
    }

    public Trainee authenticate(String email, String password) {
        if (email == null || password == null) return null;
        Trainee t = findByEmail(email);
        if (t != null && password.equals(t.getPassword())) {
            return t;
        }
        return null;
    }

    public List<Trainee> findAll() {
        String sql = "SELECT ID, firstName, lastName, phone, email, password, preferredUpdateMethod FROM TblTrainee ORDER BY ID";
        List<Trainee> out = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new Trainee(
                        rs.getInt("ID"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("password"),
                        PreferredUpdateMethod.fromValue(rs.getString("preferredUpdateMethod"))
                ));
            }
            return out;
        } catch (Exception ex) {
            throw new RuntimeException("DB findAll trainees failed", ex);
        }
    }

    public void ensurePasswordColumn() {
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE TblTrainee ADD COLUMN password VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE TblTrainee SET password = '1234' WHERE password IS NULL OR password = ''")) {
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public void ensureDefaultExists() {
        if (!findAll().isEmpty()) return;

        String ins = "INSERT INTO TblTrainee (firstName, lastName, phone, email, password, preferredUpdateMethod) VALUES (?,?,?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, "Demo");
            ps.setString(2, "Trainee");
            ps.setString(3, "0501234567");
            ps.setString(4, "trainee@fitwell.com");
            ps.setString(5, "1234");
            ps.setString(6, PreferredUpdateMethod.EMAIL.name());
            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException("DB ensureDefault trainee failed", ex);
        }
    }

    public int insert(Trainee t) {
        String sql = "INSERT INTO TblTrainee (firstName, lastName, phone, email, password, preferredUpdateMethod) VALUES (?,?,?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getFirstName());
            ps.setString(2, t.getLastName());
            ps.setString(3, t.getPhone());
            ps.setString(4, t.getEmail());
            ps.setString(5, t.getPassword());
            ps.setString(6, t.getPreferredUpdateMethod());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (Exception ex) {
            throw new RuntimeException("DB insert trainee failed", ex);
        }
    }

    public void update(Trainee t) {
        String sql = "UPDATE TblTrainee SET firstName=?, lastName=?, phone=?, email=?, preferredUpdateMethod=?, password=? WHERE ID=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getFirstName());
            ps.setString(2, t.getLastName());
            ps.setString(3, t.getPhone());
            ps.setString(4, t.getEmail());
            ps.setString(5, t.getPreferredUpdateMethod());
            ps.setString(6, t.getPassword());
            ps.setInt(7, t.getId());
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB update trainee failed", ex);
        }
    }

    public void delete(int traineeId) {
        String delRegs = "DELETE FROM TblClassRegistration WHERE traineeID=?";
        String delT = "DELETE FROM TblTrainee WHERE ID=?";
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(delRegs)) {
                    ps.setInt(1, traineeId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps2 = c.prepareStatement(delT)) {
                    ps2.setInt(1, traineeId);
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
            throw new RuntimeException("DB delete trainee failed", ex);
        }
    }
}
