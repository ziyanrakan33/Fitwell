package fitwell.integration.notification;

import javax.swing.JOptionPane;

public class NotificationGateway {
    public void notifyConsultants(String message) {
        JOptionPane.showMessageDialog(null, message, "Consultant Notification", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("[ConsultantNotification] " + message);
    }
}
