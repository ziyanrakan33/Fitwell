package fitwell.persistence.api;

import fitwell.domain.emergency.EmergencyAlert;

public interface EmergencyAlertRepository {
    EmergencyAlert activate(String message);
    EmergencyAlert getCurrentAlert();
    void clear();
}
