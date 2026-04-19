package fitwell.ui.shell;

import fitwell.service.auth.AuthenticationService;
import fitwell.ui.theme.FWTheme;
import fitwell.ui.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings({"serial","this-escape"})
public class RoleSelectionPanel extends JPanel {

    private final AppShellFrame shell;
    private final AuthenticationService authenticationService;

    public RoleSelectionPanel(AppShellFrame shell, AuthenticationService authenticationService) {
        this.shell = shell;
        this.authenticationService = authenticationService;
        setLayout(new GridBagLayout());
        setBackground(FWTheme.ENTRY_BG);

        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(30, 30, 30, 30));
        center.setPreferredSize(new Dimension(980, 520));

        center.add(buildHeader(), BorderLayout.NORTH);
        center.add(buildCards(), BorderLayout.CENTER);

        add(center);
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel title = FWUi.title("FitWell", FWTheme.ENTRY_TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = FWUi.subtitle("Train Smart. Train Right.", FWTheme.ENTRY_SUBTEXT);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = FWUi.subtitle("Choose your portal to continue", FWTheme.ENTRY_SUBTEXT);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(8));
        p.add(subtitle);
        p.add(Box.createVerticalStrut(6));
        p.add(hint);

        return p;
    }

    private JComponent buildCards() {
        JPanel grid = new JPanel(new GridLayout(1, 2, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(18, 0, 0, 0));

        grid.add(roleCard(
                "Trainee",
                "Browse classes, register, manage your profile and track your activity.",
                "Open Trainee Portal",
                () -> loginAndNavigate("trainee", shell::showTraineeDashboard)
        ));

        grid.add(roleCard(
                "Consultant",
                "Manage classes, equipment, reports and system alerts in one place.",
                "Open Consultant Portal",
                () -> loginAndNavigate("consultant", shell::showConsultantDashboard)
        ));

        return grid;
    }

    private void loginAndNavigate(String role, Runnable onSuccess) {
        LoginDialog dialog = new LoginDialog(SwingUtilities.getWindowAncestor(this), role, authenticationService);
        dialog.setVisible(true);
        if (dialog.isAuthenticated()) {
            onSuccess.run();
        }
    }

    private JPanel roleCard(String title, String desc, String buttonText, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setOpaque(true);
        card.setBackground(FWTheme.ENTRY_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(FWTheme.ENTRY_TEXT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel descLabel = new JLabel("<html><div style='width:360px;color:#A0AEC5;'>" + desc + "</div></html>");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        top.add(titleLabel);
        top.add(descLabel);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        JButton enterBtn = FWUi.ghostDarkButton(buttonText);
        enterBtn.addActionListener(e -> action.run());
        bottom.add(enterBtn);

        card.add(top, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        // subtle hover
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FWTheme.ACCENT, 1, true),
                        new EmptyBorder(24, 24, 24, 24)
                ));
                card.setBackground(FWTheme.CARD_BG_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                        new EmptyBorder(24, 24, 24, 24)
                ));
                card.setBackground(FWTheme.ENTRY_CARD);
            }
            @Override public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        return card;
    }
}