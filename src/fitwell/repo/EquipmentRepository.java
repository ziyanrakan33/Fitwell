package fitwell.repo;

import fitwell.db.Db;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentCategory;
import fitwell.domain.equipment.EquipmentLocation;
import fitwell.domain.equipment.EquipmentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentRepository {

    public synchronized void save(Equipment e) {
        if (e == null) return;

        Equipment existing = findBySerial(e.getSerialNumber());
        if (existing == null) insert(e);
        else update(e);
    }

    public synchronized Equipment findBySerial(String serial) {
        if (serial == null || serial.isBlank()) return null;

        String sql = "SELECT serialNumber, name, description, category, quantity, status, " +
                     "locationX, locationY, shelfNumber, isFlagged, flagReason " +
                     "FROM TblEquipment WHERE serialNumber=?";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, serial);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (Exception ex) {
            throw new RuntimeException("DB findBySerial failed: " + ex.getMessage(), ex);
        }
    }

    public synchronized List<Equipment> findAll() {
        String sql = "SELECT serialNumber, name, description, category, quantity, status, " +
                     "locationX, locationY, shelfNumber, isFlagged, flagReason " +
                     "FROM TblEquipment ORDER BY serialNumber";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Equipment> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;

        } catch (Exception ex) {
            throw new RuntimeException("DB findAll failed: " + ex.getMessage(), ex);
        }
    }

    public synchronized boolean delete(String serial) {
        if (serial == null || serial.isBlank()) return false;

        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);

            try {
                try (PreparedStatement ps1 =
                             c.prepareStatement("DELETE FROM TblEquipmentAssignment WHERE serialNumber=?")) {
                    ps1.setString(1, serial);
                    ps1.executeUpdate();
                }

                int rows;
                try (PreparedStatement ps2 =
                             c.prepareStatement("DELETE FROM TblEquipment WHERE serialNumber=?")) {
                    ps2.setString(1, serial);
                    rows = ps2.executeUpdate();
                }

                c.commit();
                return rows > 0;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }

        } catch (Exception ex) {
            throw new RuntimeException("DB delete failed: " + ex.getMessage(), ex);
        }
    }


    // ===== DB operations =====

    private void insert(Equipment e) {
        String sql = "INSERT INTO TblEquipment " +
                "(serialNumber, name, description, category, quantity, status, locationX, locationY, shelfNumber, isFlagged, flagReason) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            fillInsertParams(ps, e);
            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException("DB insert failed: " + ex.getMessage(), ex);
        }
    }

    private void update(Equipment e) {
        String sql = "UPDATE TblEquipment SET " +
                "name=?, description=?, category=?, quantity=?, status=?, " +
                "locationX=?, locationY=?, shelfNumber=?, isFlagged=?, flagReason=? " +
                "WHERE serialNumber=?";

        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            fillUpdateParams(ps, e);
            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException("DB update failed: " + ex.getMessage(), ex);
        }
    }

    // ===== Mapping =====

    private void fillInsertParams(PreparedStatement ps, Equipment e) throws SQLException {
        EquipmentLocation loc = e.getLocation();

        ps.setString(1, safe(e.getSerialNumber()));
        ps.setString(2, safe(e.getName()));
        ps.setString(3, safe(e.getDescription()));
        ps.setString(4, safe(e.getCategory().name()));
        ps.setInt(5, Math.max(0, e.getQuantity()));
        ps.setString(6, safe(e.getStatus().name()));
        ps.setInt(7, loc == null ? 0 : loc.getX());
        ps.setInt(8, loc == null ? 0 : loc.getY());
        ps.setInt(9, loc == null ? 0 : loc.getShelfNumber());
        ps.setBoolean(10, e.isFlagged());
        ps.setString(11, safe(e.getFlagReason()));
    }

    private void fillUpdateParams(PreparedStatement ps, Equipment e) throws SQLException {
        EquipmentLocation loc = e.getLocation();

        ps.setString(1, safe(e.getName()));
        ps.setString(2, safe(e.getDescription()));
        ps.setString(3, safe(e.getCategory().name()));
        ps.setInt(4, Math.max(0, e.getQuantity()));
        ps.setString(5, safe(e.getStatus().name()));
        ps.setInt(6, loc == null ? 0 : loc.getX());
        ps.setInt(7, loc == null ? 0 : loc.getY());
        ps.setInt(8, loc == null ? 0 : loc.getShelfNumber());
        ps.setBoolean(9, e.isFlagged());
        ps.setString(10, safe(e.getFlagReason()));
        ps.setString(11, safe(e.getSerialNumber())); // WHERE
    }

    private Equipment mapRow(ResultSet rs) throws SQLException {
        String serial = rs.getString("serialNumber");
        String name = rs.getString("name");
        String desc = rs.getString("description");

        EquipmentCategory cat = parseCategory(rs.getString("category"));
        EquipmentStatus st = parseStatus(rs.getString("status"));

        int qty = rs.getInt("quantity");
        int x = rs.getInt("locationX");
        int y = rs.getInt("locationY");
        int shelf = rs.getInt("shelfNumber");

        boolean flagged = rs.getBoolean("isFlagged");
        String reason = rs.getString("flagReason");

        Equipment e = new Equipment(serial, safe(name), safe(desc), cat, qty, st,
                new EquipmentLocation(x, y, shelf));

        if (flagged) e.flag(reason == null ? "" : reason);
        else e.unflag();

        return e;
    }

    private EquipmentCategory parseCategory(String s) {
        try { return EquipmentCategory.valueOf(s); }
        catch (Exception ex) { return EquipmentCategory.other; }
    }

    private EquipmentStatus parseStatus(String s) {
        try { return EquipmentStatus.valueOf(s); }
        catch (Exception ex) { return EquipmentStatus.IN_SERVICE; }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
