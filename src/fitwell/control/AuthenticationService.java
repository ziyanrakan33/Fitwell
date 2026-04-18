package fitwell.control;

import fitwell.domain.user.ConsultantRole;

public class AuthenticationService {
    private static final AuthenticationService INSTANCE = new AuthenticationService();

    private boolean loggedIn = false;
    private int currentUserId = -1;
    private String currentRole = null;
    private String currentUserName = null;
    private ConsultantRole currentConsultantRole = null;

    private AuthenticationService() {}

    public static AuthenticationService getInstance() {
        return INSTANCE;
    }

    public void login(int userId, String role, String displayName) {
        this.currentUserId = userId;
        this.currentRole = role;
        this.currentUserName = displayName;
        this.currentConsultantRole = null;
        this.loggedIn = true;
    }

    public void loginConsultant(int userId, ConsultantRole consultantRole, String displayName) {
        this.currentUserId = userId;
        this.currentRole = "consultant";
        this.currentUserName = displayName;
        this.currentConsultantRole = consultantRole;
        this.loggedIn = true;
    }

    public void logout() {
        this.loggedIn = false;
        this.currentUserId = -1;
        this.currentRole = null;
        this.currentUserName = null;
        this.currentConsultantRole = null;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public ConsultantRole getCurrentConsultantRole() {
        return currentConsultantRole;
    }

    public boolean isManager() {
        return ConsultantRole.MANAGER.equals(currentConsultantRole);
    }

    public boolean isTrainer() {
        return ConsultantRole.TRAINER.equals(currentConsultantRole);
    }

    public boolean isDietitian() {
        return ConsultantRole.DIETITIAN.equals(currentConsultantRole);
    }

    public boolean hasRole(ConsultantRole role) {
        return role != null && role.equals(currentConsultantRole);
    }
}
