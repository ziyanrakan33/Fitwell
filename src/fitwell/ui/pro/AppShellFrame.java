package fitwell.ui.pro;

import fitwell.service.auth.AuthenticationService;
import fitwell.ui.pro.consultant.ConsultantDashboardPanel;
import fitwell.ui.pro.trainee.TraineeDashboardPanel;

import javax.swing.*;
import java.awt.*;

public class AppShellFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private final RoleSelectionPanel roleSelectionPanel;
    private final ConsultantDashboardPanel consultantDashboardPanel;
    private final TraineeDashboardPanel traineeDashboardPanel;

    public static final String CARD_ROLE_SELECTION = "roleSelection";
    public static final String CARD_CONSULTANT = "consultantDashboard";
    public static final String CARD_TRAINEE = "traineeDashboard";

    public AppShellFrame() {
        super("FitWell");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 820));
        setSize(1440, 900);
        setLocationRelativeTo(null);

        roleSelectionPanel = new RoleSelectionPanel(this);
        consultantDashboardPanel = new ConsultantDashboardPanel(this);
        traineeDashboardPanel = new TraineeDashboardPanel(this);

        root.add(roleSelectionPanel, CARD_ROLE_SELECTION);
        root.add(consultantDashboardPanel, CARD_CONSULTANT);
        root.add(traineeDashboardPanel, CARD_TRAINEE);

        setContentPane(root);
        showRoleSelection();
    }

    public void showRoleSelection() {
        cardLayout.show(root, CARD_ROLE_SELECTION);
    }

    public void showConsultantDashboard() {
        consultantDashboardPanel.refreshStats();
        cardLayout.show(root, CARD_CONSULTANT);
    }

    public void showTraineeDashboard() {
        traineeDashboardPanel.refreshStats();
        cardLayout.show(root, CARD_TRAINEE);
    }

    public void logout() {
        AuthenticationService.getInstance().logout();
        showRoleSelection();
    }
}