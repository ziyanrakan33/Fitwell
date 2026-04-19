package fitwell.persistence.api;

import fitwell.domain.user.Consultant;
import java.util.List;

public interface ConsultantRepository {
    List<Consultant> findAll();
    Consultant findById(int id);
    Consultant findByEmail(String email);
    Consultant authenticate(String email, String password);
    void update(Consultant c);
    int insert(Consultant c);
    List<Consultant> findPendingApprovals();
    void approve(int consultantId);
    void reject(int consultantId);
    void ensurePasswordColumn();
    void ensureApprovedColumn();
    void ensureRoleColumn();
    void ensureDefaultExists();
}
