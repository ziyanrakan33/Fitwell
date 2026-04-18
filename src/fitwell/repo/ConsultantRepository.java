package fitwell.repo;

import fitwell.db.Db;
import fitwell.entity.Consultant;
import fitwell.entity.ConsultantRole;

import java.sql.*;
import java.util.*;

public class ConsultantRepository {

    public List<Consultant> findAll() {
        String sql = "SELECT ID, firstName, lastName, phone, email, password, approved, role FROM TblConsultant ORDER BY ID";
        List<Consultant> out = new ArrayList<>();

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new Consultant(
                        rs.getInt("ID"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("approved"),
                        ConsultantRole.fromString(rs.getString("role"))
                ));
            }
            return out;

        } catch (Exception ex) {
            throw new RuntimeException("DB findAll consultants failed", ex);
        }
    }

    public Consultant findById(int id) {
        for (Consultant c : findAll()) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    public Consultant findByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        for (Consultant c : findAll()) {
            if (email.equalsIgnoreCase(c.getEmail())) {
                return c;
            }
        }
        return null;
    }

    public Consultant authenticate(String email, String password) {
        if (email == null || password == null) return null;
        Consultant c = findByEmail(email);
        if (c != null && password.equals(c.getPassword())) {
            return c;
        }
        return null;
    }

    public void update(Consultant c) {
        String sql = "UPDATE TblConsultant SET firstName=?, lastName=?, phone=?, email=?, password=?, role=? WHERE ID=?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getPassword());
            ps.setString(6, c.getRole().name());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB update consultant failed", ex);
        }
    }

    public int insert(Consultant c) {
        String sql = "INSERT INTO TblConsultant (firstName, lastName, phone, email, password, approved, role) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getPassword());
            ps.setBoolean(6, c.isApproved());
            ps.setString(7, c.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (Exception ex) {
            throw new RuntimeException("DB insert consultant failed", ex);
        }
    }

    public List<Consultant> findPendingApprovals() {
        List<Consultant> pending = new ArrayList<>();
        for (Consultant c : findAll()) {
            if (!c.isApproved()) pending.add(c);
        }
        return pending;
    }

    public void approve(int consultantId) {
        String sql = "UPDATE TblConsultant SET approved = TRUE WHERE ID = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, consultantId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB approve consultant failed", ex);
        }
    }

    public void reject(int consultantId) {
        String sql = "DELETE FROM TblConsultant WHERE ID = ? AND approved = FALSE";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, consultantId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("DB reject consultant failed", ex);
        }
    }

    public void ensurePasswordColumn() {
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE TblConsultant ADD COLUMN password VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE TblConsultant SET password = '1234' WHERE password IS NULL OR password = ''")) {
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public void ensureApprovedColumn() {
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE TblConsultant ADD COLUMN approved YESNO");
        } catch (Exception ignored) {
        }
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE TblConsultant SET approved = TRUE WHERE approved IS NULL")) {
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public void ensureRoleColumn() {
        try (Connection c = Db.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE TblConsultant ADD COLUMN role VARCHAR(50)");
        } catch (Exception ignored) {
        }
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE TblConsultant SET role = 'MANAGER' WHERE role IS NULL OR role = ''")) {
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public void ensureDefaultExists() {
        if (!findAll().isEmpty()) return;

        String ins = "INSERT INTO TblConsultant (firstName, lastName, phone, email, password, approved, role) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(ins)) {

            ps.setString(1, "Admin");
            ps.setString(2, "Consultant");
            ps.setString(3, "0500000000");
            ps.setString(4, "admin@fitwell.com");
            ps.setString(5, "1234");
            ps.setBoolean(6, true);
            ps.setString(7, ConsultantRole.MANAGER.name());
            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException("DB ensureDefault consultant failed", ex);
        }
    }
}
