package fitwell.ui.pro.consultant;

import fitwell.ui.pro.components.ProTextBlocks;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmergencyControlCenterCard extends JPanel {

    private final JLabel statusValue = new JLabel("NORMAL");
    private final JLabel activatedValue = new JLabel("-");
    private final JLabel autoResumeValue = new JLabel("-");

    private Runnable onActivate;
    private Runnable onDeactivate;

    public EmergencyControlCenterCard() {
        buildUi();
        setNormal();
    }

    public void setOnActivate(Runnable onActivate) {
        this.onActivate = onActivate;
    }

    public void setOnDeactivate(Runnable onDeactivate) {
        this.onDeactivate = onDeactivate;
    }

    public void setNormal() {
        statusValue.setText("NORMAL");
        statusValue.setForeground(FWTheme.SUCCESS);
    }

    public void setEmergencyActive(LocalDateTime activatedAt, LocalDateTime autoResumeAt) {
        statusValue.setText("EMERGENCY ACTIVE");
        statusValue.setForeground(FWTheme.DANGER);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        activatedValue.setText(activatedAt == null ? "-" : activatedAt.format(f));
        autoResumeValue.setText(autoResumeAt == null ? "-" : autoResumeAt.format(f));
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 12));
        setOpaque(false);

        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Emergency Control Center");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JComponent info = ProTextBlocks.monoBlock(
                "Bonus story behavior:\n" +
                "• Activate alert => suspend ongoing & upcoming classes immediately\n" +
                "• Deactivate => manual resume OR auto resume after 30 minutes\n" +
                "• Classes continue from suspension point, but original end time stays fixed"
        );

        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        statusValue.setFont(new Font("SansSerif", Font.BOLD, 28));
        statusValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        activatedValue.setForeground(FWTheme.TEXT_PRIMARY);
        activatedValue.setFont(new Font("SansSerif", Font.PLAIN, 14));
        activatedValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        autoResumeValue.setForeground(FWTheme.TEXT_PRIMARY);
        autoResumeValue.setFont(new Font("SansSerif", Font.PLAIN, 14));
        autoResumeValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusPanel.add(statusValue);
        statusPanel.add(Box.createVerticalStrut(18));
        statusPanel.add(line("Activated:", activatedValue));
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(line("Auto Resume:", autoResumeValue));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        JButton activateBtn = FWUi.primaryButton("Activate Emergency");
        JButton deactivateBtn = FWUi.ghostDarkButton("Deactivate Emergency");

        activateBtn.addActionListener(e -> {
            if (onActivate != null) onActivate.run();
        });

        deactivateBtn.addActionListener(e -> {
            if (onDeactivate != null) onDeactivate.run();
        });

        buttons.add(activateBtn);
        buttons.add(deactivateBtn);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(info, BorderLayout.NORTH);
        center.add(statusPanel, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private JComponent line(String label, JLabel value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(FWTheme.TEXT_SECONDARY);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));

        p.add(l);
        p.add(value);
        return p;
    }
}