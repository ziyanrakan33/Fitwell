package fitwell.ui.pro.trainee;

import fitwell.control.AuthenticationService;
import fitwell.control.FitWellServiceRegistry;
import fitwell.ui.pro.AppShellFrame;
import fitwell.ui.pro.components.SidebarButton;
import fitwell.ui.pro.components.StatCard;
import fitwell.ui.pro.components.TopBarPanel;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import java.awt.*;

public class TraineeDashboardPanel extends JPanel {
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);

    private final SidebarButton btnDashboard = new SidebarButton("Dashboard");
    private final SidebarButton btnRegister = new SidebarButton("Register Classes");
    private final SidebarButton btnMyPlan = new SidebarButton("My Plan");
    private final SidebarButton btnProfile = new SidebarButton("Profile");

    private final StatCard statUpcoming = new StatCard("Upcoming Classes", "—", "next 7 days");
    private final StatCard statAvailable = new StatCard("Available Classes", "—", "open for registration");
    private final StatCard statStatus = new StatCard("Portal Status", "Ready", "");

    private final JLabel welcomeTitle = new JLabel("Welcome, Trainee!");

    private final FitWellServiceRegistry services = FitWellServiceRegistry.getInstance();
    private final TraineeClassRegistrationPanel traineeClassRegistrationPanel =
            new TraineeClassRegistrationPanel(services.trainingClassQueryService(), services.traineeProfileService(), new fitwell.control.RegistrationController());
    private final TrainingPlanPanel trainingPlanPanel =
            new TrainingPlanPanel(services.trainingPlanService(), services.traineeProfileService());
    private final TraineeProfilePanel traineeProfilePanel =
            new TraineeProfilePanel(services.traineeProfileService());

    public TraineeDashboardPanel(AppShellFrame shell) {
        setLayout(new BorderLayout());
        setBackground(FWTheme.DASH_BG);

        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(true);
        main.setBackground(FWTheme.DASH_BG);

        main.add(new TopBarPanel(
                "Trainee Portal",
                "Browse and register classes based on availability",
                shell::logout
        ), BorderLayout.NORTH);

        main.add(buildMainContent(), BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        selectTab("dashboard");
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(220, 0));
        side.setBackground(FWTheme.SIDEBAR_BG);
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, FWTheme.BORDER));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(20, 16, 10, 16));

        JLabel logo = new JLabel("FitWell");
        logo.setForeground(FWTheme.TEXT_PRIMARY);
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel role = new JLabel("Trainee");
        role.setForeground(FWTheme.TEXT_SECONDARY);
        role.setFont(new Font("SansSerif", Font.PLAIN, 12));

        top.add(logo);
        top.add(Box.createVerticalStrut(4));
        top.add(role);

        JPanel nav = new JPanel(new GridLayout(8, 1, 0, 6));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));

        btnDashboard.addActionListener(e -> selectTab("dashboard"));
        btnRegister.addActionListener(e -> selectTab("register"));
        btnMyPlan.addActionListener(e -> selectTab("plan"));
        btnProfile.addActionListener(e -> selectTab("profile"));

        nav.add(btnDashboard);
        nav.add(btnRegister);
        nav.add(btnMyPlan);
        nav.add(btnProfile);

        side.add(top, BorderLayout.NORTH);
        side.add(nav, BorderLayout.CENTER);

        return side;
    }

    private JComponent buildMainContent() {
        contentPanel.setOpaque(true);
        contentPanel.setBackground(FWTheme.DASH_BG);

        contentPanel.add(buildDashboardHome(), "dashboard");
        contentPanel.add(buildRegisterPanel(), "register");
        contentPanel.add(buildPlanPanel(), "plan");
        contentPanel.add(buildProfilePanel(), "profile");

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(true);
        wrap.setBackground(FWTheme.DASH_BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        wrap.add(contentPanel, BorderLayout.CENTER);

        return wrap;
    }

    private JComponent buildDashboardHome() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setOpaque(true);
        page.setBackground(FWTheme.DASH_BG);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 12));
        stats.setOpaque(false);
        stats.add(statUpcoming);
        stats.add(statAvailable);
        stats.add(statStatus);

        JPanel welcome = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        welcome.setLayout(new BorderLayout(0, 12));

        welcomeTitle.setForeground(FWTheme.TEXT_PRIMARY);
        welcomeTitle.setFont(FWTheme.FONT_H2);

        JLabel desc = new JLabel("<html><div style='color:#A0AEC5;'>"
                + "Here you can browse classes, register (up to 24 hours in advance), "
                + "review your assigned personal and group plans, and manage your trainee profile."
                + "</div></html>");

        JButton goRegister = FWUi.primaryButton("Open Registration");
        goRegister.addActionListener(e -> selectTab("register"));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        bottom.setOpaque(false);
        bottom.add(goRegister);

        welcome.add(welcomeTitle, BorderLayout.NORTH);
        welcome.add(desc, BorderLayout.CENTER);
        welcome.add(bottom, BorderLayout.SOUTH);

        page.add(stats, BorderLayout.NORTH);
        page.add(welcome, BorderLayout.CENTER);

        return page;
    }

    private JComponent buildRegisterPanel() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setOpaque(true);
        page.setBackground(FWTheme.DASH_BG);

        JPanel header = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        header.setLayout(new BorderLayout());

        JLabel title = new JLabel("Class Registration");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JLabel sub = new JLabel("Classes assigned through the training plan workflow");
        sub.setForeground(FWTheme.TEXT_SECONDARY);

        JPanel txt = new JPanel(new GridLayout(2, 1));
        txt.setOpaque(false);
        txt.add(title);
        txt.add(sub);

        header.add(txt, BorderLayout.WEST);

        page.add(header, BorderLayout.NORTH);
        page.add(traineeClassRegistrationPanel, BorderLayout.CENTER);

        return page;
    }

    private JComponent buildPlanPanel() {
        return trainingPlanPanel;
    }

    private JComponent buildProfilePanel() {
        return traineeProfilePanel;
    }

    private void selectTab(String key) {
        btnDashboard.setSelectedState("dashboard".equals(key));
        btnRegister.setSelectedState("register".equals(key));
        btnMyPlan.setSelectedState("plan".equals(key));
        btnProfile.setSelectedState("profile".equals(key));
        contentLayout.show(contentPanel, key);
    }

    public void refreshStats() {
        String name = AuthenticationService.getInstance().getCurrentUserName();
        welcomeTitle.setText("Welcome, " + (name != null ? name : "Trainee") + "!");

        int traineeId = services.traineeProfileService().getCurrentTrainee().getId();
        statUpcoming.setValue(String.valueOf(services.trainingClassQueryService().countUpcomingClassesForTrainee(traineeId)));
        statUpcoming.setSubText("assigned classes");

        statAvailable.setValue(String.valueOf(services.trainingClassQueryService().countAvailableClassesForTrainee(traineeId)));
        statAvailable.setSubText("eligible and open");

        statStatus.setValue("Ready");
        statStatus.setSubText("");
        traineeClassRegistrationPanel.reload();
        trainingPlanPanel.reload();
        traineeProfilePanel.reload();
    }
}