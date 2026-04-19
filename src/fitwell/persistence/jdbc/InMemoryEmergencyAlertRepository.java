package fitwell.persistence.jdbc;

import fitwell.domain.emergency.EmergencyAlert;
import fitwell.persistence.api.EmergencyAlertRepository;

public class InMemoryEmergencyAlertRepository implements EmergencyAlertRepository {

    private static EmergencyAlert currentAlert;
    private static int nextId = 1;

    public EmergencyAlert activate(String message) {
        currentAlert = new EmergencyAlert(nextId++, null, message);
        return currentAlert;
    }

    public EmergencyAlert getCurrentAlert() {
        return currentAlert;
    }

    public void clear() {
        currentAlert = null;
    }
}
