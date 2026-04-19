package fitwell.persistence.jdbc;

import fitwell.domain.training.GroupPlan;
import fitwell.domain.training.PersonalPlan;
import fitwell.domain.training.Plan;
import fitwell.domain.training.PlanStatus;
import fitwell.persistence.api.TrainingPlanRepository;
import fitwell.persistence.db.Db;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcTrainingPlanRepository implements TrainingPlanRepository {

    public List<Plan> findAll() {
        List<Plan> out = new ArrayList<>();
        out.addAll(findAllPersonal());
        out.addAll(findAllGroup());
        out.sort((p1, p2) -> Integer.compare(p1.getPlanId(), p2.getPlanId()));
        return out;
    }

    private List<Plan> findAllPersonal() {
        List<Plan> out = new ArrayList<>();
        String sql = "SELECT p.planID, p.startDate, p.duration, p.status, p.dietaryRestrictions, t.traineeID " +
                     "FROM TblPersonalPlan p LEFT JOIN TblTraineePlan t ON p.planID = t.planID";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int traineeId = rs.getInt("traineeID");
                if (rs.wasNull()) traineeId = 0;
                Timestamp ts = rs.getTimestamp("startDate");
                LocalDate start = ts == null ? LocalDate.now() : ts.toLocalDateTime().toLocalDate();
                String durStr = rs.getString("duration");
                int dur = 1;
                try { dur = Integer.parseInt(durStr); } catch (Exception ignored) {}
                String statusStr = rs.getString("status");
                if (statusStr == null) statusStr = "ACTIVE";
                out.add(new PersonalPlan(rs.getInt("planID"), traineeId, start, dur, statusStr, rs.getString("dietaryRestrictions")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    private List<Plan> findAllGroup() {
        List<Plan> out = new ArrayList<>();
        String sql = "SELECT p.planID, p.startDate, p.duration, p.status, p.ageRange, p.preferredClassTypes, p.generalGuidelines, t.traineeID " +
                     "FROM TblGroupPlan p LEFT JOIN TblTraineePlan t ON p.planID = t.planID";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int traineeId = rs.getInt("traineeID");
                if (rs.wasNull()) traineeId = 0;
                Timestamp ts = rs.getTimestamp("startDate");
                LocalDate start = ts == null ? LocalDate.now() : ts.toLocalDateTime().toLocalDate();
                String durStr = rs.getString("duration");
                int dur = 1;
                try { dur = Integer.parseInt(durStr); } catch (Exception ignored) {}
                String statusStr = rs.getString("status");
                if (statusStr == null) statusStr = "ACTIVE";
                out.add(new GroupPlan(
                        rs.getInt("planID"), traineeId, start, dur, PlanStatus.fromValue(statusStr),
                        rs.getString("ageRange"), rs.getString("preferredClassTypes"), rs.getString("generalGuidelines")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public Plan findById(int planId) {
        for (Plan plan : findAll()) {
            if (plan.getPlanId() == planId) return plan;
        }
        return null;
    }

    public List<Plan> findByTraineeId(int traineeId) {
        List<Plan> out = new ArrayList<>();
        for (Plan plan : findAll()) {
            if (plan.getOwnerTraineeId() == traineeId) out.add(plan);
        }
        return out;
    }

    public synchronized Plan save(Plan plan) {
        if (plan == null) throw new IllegalArgumentException("Plan is required.");
        Plan existing = findById(plan.getPlanId());
        if (existing != null) {
            updatePlan(plan);
            return plan;
        } else {
            return insertPlan(plan);
        }
    }

    private Plan insertPlan(Plan plan) {
        int newId = -1;
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                int consultantId = 42;
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery("SELECT TOP 1 ID FROM TblConsultant")) {
                    if (rs.next()) consultantId = rs.getInt(1);
                }

                String sqlParent = "INSERT INTO TblFitnessPlan (startDate, duration, status, consultantID) VALUES (?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sqlParent, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, plan.getStartDate() != null ? Timestamp.valueOf(plan.getStartDate().atStartOfDay()) : null);
                    ps.setString(2, String.valueOf(plan.getDurationMonths()));
                    ps.setString(3, plan.getStatus());
                    ps.setInt(4, consultantId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) newId = keys.getInt(1);
                    }
                    if (newId <= 0) {
                        try (Statement stmt = c.createStatement();
                             ResultSet rs = stmt.executeQuery("SELECT @@IDENTITY")) {
                            if (rs.next()) newId = rs.getInt(1);
                        }
                    }
                }

                if (plan instanceof PersonalPlan) {
                    PersonalPlan p = (PersonalPlan) plan;
                    String sql = "INSERT INTO TblPersonalPlan (planID, startDate, duration, status, dietaryRestrictions) VALUES (?,?,?,?,?)";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setInt(1, newId);
                        ps.setTimestamp(2, plan.getStartDate() != null ? Timestamp.valueOf(plan.getStartDate().atStartOfDay()) : null);
                        ps.setString(3, String.valueOf(plan.getDurationMonths()));
                        ps.setString(4, plan.getStatus());
                        ps.setString(5, p.getDietaryRestrictions());
                        ps.executeUpdate();
                    }
                } else if (plan instanceof GroupPlan) {
                    GroupPlan p = (GroupPlan) plan;
                    String sql = "INSERT INTO TblGroupPlan (planID, startDate, duration, status, ageRange, preferredClassTypes, generalGuidelines) VALUES (?,?,?,?,?,?,?)";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setInt(1, newId);
                        ps.setTimestamp(2, plan.getStartDate() != null ? Timestamp.valueOf(plan.getStartDate().atStartOfDay()) : null);
                        ps.setString(3, String.valueOf(plan.getDurationMonths()));
                        ps.setString(4, plan.getStatus());
                        ps.setString(5, p.getAgeRange());
                        ps.setString(6, p.getPreferredClassTypes());
                        ps.setString(7, p.getGeneralGuidelines());
                        ps.executeUpdate();
                    }
                }

                if (newId != -1 && plan.getOwnerTraineeId() > 0) {
                    saveOwnerMapping(c, newId, plan.getOwnerTraineeId());
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw new RuntimeException("DB insert plan failed: " + e.getMessage(), e);
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return findById(newId != -1 ? newId : plan.getPlanId());
    }

    private void updatePlan(Plan plan) {
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                String sqlParent = "UPDATE TblFitnessPlan SET startDate=?, duration=?, status=? WHERE planID=?";
                try (PreparedStatement ps = c.prepareStatement(sqlParent)) {
                    ps.setTimestamp(1, plan.getStartDate() != null ? Timestamp.valueOf(plan.getStartDate().atStartOfDay()) : null);
                    ps.setString(2, String.valueOf(plan.getDurationMonths()));
                    ps.setString(3, plan.getStatus());
                    ps.setInt(4, plan.getPlanId());
                    ps.executeUpdate();
                }
                if (plan instanceof PersonalPlan) {
                    PersonalPlan p = (PersonalPlan) plan;
                    String sql = "UPDATE TblPersonalPlan SET startDate=?, duration=?, status=?, dietaryRestrictions=? WHERE planID=?";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setTimestamp(1, plan.getStartDate() != null ? Timestamp.valueOf(plan.getStartDate().atStartOfDay()) : null);
                        ps.setString(2, String.valueOf(plan.getDurationMonths()));
                        ps.setString(3, plan.getStatus());
                        ps.setString(4, p.getDietaryRestrictions());
                        ps.setInt(5, p.getPlanId());
                        ps.executeUpdate();
                    }
                } else if (plan instanceof GroupPlan) {
                    GroupPlan p = (GroupPlan) plan;
                    String sql = "UPDATE TblGroupPlan SET startDate=?, duration=?, status=?, ageRange=?, preferredClassTypes=?, generalGuidelines=? WHERE planID=?";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setTimestamp(1, plan.getStartDate() != null ? Timestamp.valueOf(plan.getStartDate().atStartOfDay()) : null);
                        ps.setString(2, String.valueOf(plan.getDurationMonths()));
                        ps.setString(3, plan.getStatus());
                        ps.setString(4, p.getAgeRange());
                        ps.setString(5, p.getPreferredClassTypes());
                        ps.setString(6, p.getGeneralGuidelines());
                        ps.setInt(7, p.getPlanId());
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw new RuntimeException("DB update plan failed: " + e.getMessage(), e);
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveOwnerMapping(Connection c, int planId, int traineeId) throws Exception {
        String sql = "INSERT INTO TblTraineePlan (traineeID, planID, dateAssigned) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, traineeId);
            ps.setInt(2, planId);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        }
    }
}
