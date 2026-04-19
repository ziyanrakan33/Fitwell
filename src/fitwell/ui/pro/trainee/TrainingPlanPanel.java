package fitwell.ui.pro.trainee;

import fitwell.service.training.TraineeProfileService;
import fitwell.service.training.TrainingClassService;
import fitwell.service.training.TrainingPlanService;
import fitwell.domain.training.GroupPlan;
import fitwell.domain.training.PersonalPlan;
import fitwell.domain.training.Plan;
import fitwell.domain.user.Trainee;
import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingPlanMember;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class TrainingPlanPanel extends JPanel {
    private final TrainingPlanService trainingPlanService;
    private final TraineeProfileService traineeProfileService;
    private final TrainingClassService trainingClassService;
    private final JPanel content = new JPanel();

    public TrainingPlanPanel(TrainingPlanService trainingPlanService, TraineeProfileService traineeProfileService, TrainingClassService trainingClassService) {
        this.trainingPlanService = trainingPlanService;
        this.traineeProfileService = traineeProfileService;
        this.trainingClassService = trainingClassService;
        setLayout(new BorderLayout(12, 12));
        setBackground(FWTheme.DASH_BG);
        setOpaque(true);

        JPanel header = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        header.setLayout(new BorderLayout());
        JLabel title = new JLabel("Training Plans");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        JLabel subtitle = new JLabel("Personal and group plans assigned to you.");
        subtitle.setForeground(FWTheme.TEXT_SECONDARY);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        reload();
    }

    public void reload() {
        content.removeAll();
        int traineeId = traineeProfileService.getCurrentTrainee().getId();
        List<Plan> plans = trainingPlanService.getPlansForTrainee(traineeId);
        if (plans.isEmpty()) {
            JLabel label = new JLabel("No plans are assigned yet. Contact your consultant to get started.");
            label.setForeground(FWTheme.TEXT_SECONDARY);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setBorder(new EmptyBorder(30, 20, 30, 20));
            content.add(label);
        } else {
            for (Plan plan : plans) {
                content.add(buildPlanCard(plan, traineeId));
                content.add(Box.createVerticalStrut(12));
            }
        }
        revalidate();
        repaint();
    }

    private JPanel buildPlanCard(Plan plan, int currentTraineeId) {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // -- Header row: type + status badge --
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        String typeLabel = plan instanceof PersonalPlan ? "Personal Plan" : "Group Plan";
        JLabel title = new JLabel(typeLabel + "  #" + plan.getPlanId());
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));

        JLabel statusBadge = buildStatusBadge(plan.getStatus());
        headerRow.add(title, BorderLayout.WEST);
        headerRow.add(statusBadge, BorderLayout.EAST);

        // -- Info grid --
        JPanel infoGrid = new JPanel(new GridLayout(0, 2, 16, 4));
        infoGrid.setOpaque(false);

        addInfoPair(infoGrid, "Start Date", plan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        addInfoPair(infoGrid, "Duration", plan.getDurationMonths() + " months");

        boolean isOwner = plan.getOwnerTraineeId() == currentTraineeId;
        addInfoPair(infoGrid, "Your Role", isOwner ? "Owner" : "Member");

        if (plan instanceof PersonalPlan pp) {
            if (!pp.getDietaryRestrictions().isEmpty()) {
                addInfoPair(infoGrid, "Dietary Restrictions", pp.getDietaryRestrictions());
            }
            if (!pp.getDietitianNotes().isEmpty()) {
                addInfoPair(infoGrid, "Dietitian Notes", pp.getDietitianNotes());
            }
        } else if (plan instanceof GroupPlan gp) {
            if (!gp.getAgeRange().isEmpty()) {
                addInfoPair(infoGrid, "Age Range", gp.getAgeRange());
            }
            if (!gp.getPreferredClassTypes().isEmpty()) {
                addInfoPair(infoGrid, "Preferred Classes", gp.getPreferredClassTypes());
            }
            if (!gp.getGeneralGuidelines().isEmpty()) {
                addInfoPair(infoGrid, "Guidelines", gp.getGeneralGuidelines());
            }
        }

        // -- Members section (group plans) --
        JPanel sectionsPanel = new JPanel();
        sectionsPanel.setOpaque(false);
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));

        if (plan instanceof GroupPlan) {
            List<TrainingPlanMember> members = trainingPlanService.getMembersForPlan(plan.getPlanId());
            if (!members.isEmpty()) {
                sectionsPanel.add(Box.createVerticalStrut(8));
                JLabel membersTitle = new JLabel("Group Members (" + members.size() + ")");
                membersTitle.setForeground(FWTheme.ACCENT);
                membersTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
                membersTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                sectionsPanel.add(membersTitle);
                sectionsPanel.add(Box.createVerticalStrut(4));

                JPanel memberChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
                memberChips.setOpaque(false);
                memberChips.setAlignmentX(Component.LEFT_ALIGNMENT);

                Trainee owner = traineeProfileService.findById(plan.getOwnerTraineeId());
                memberChips.add(buildChip((owner != null ? owner.fullName() : "#" + plan.getOwnerTraineeId()) + " (owner)",
                        FWTheme.ACCENT));

                for (TrainingPlanMember m : members) {
                    Trainee t = traineeProfileService.findById(m.getTraineeId());
                    String name = t != null ? t.fullName() : "#" + m.getTraineeId();
                    boolean isMe = m.getTraineeId() == currentTraineeId;
                    memberChips.add(buildChip(name + (isMe ? " (you)" : ""),
                            isMe ? FWTheme.ACCENT : FWTheme.TEXT_SECONDARY));
                }
                sectionsPanel.add(memberChips);
            }
        }

        // -- Assigned classes section --
        Set<Integer> classIds = trainingPlanService.getClassIdsForPlan(plan.getPlanId());
        if (!classIds.isEmpty()) {
            sectionsPanel.add(Box.createVerticalStrut(8));
            JLabel classesTitle = new JLabel("Assigned Classes (" + classIds.size() + ")");
            classesTitle.setForeground(FWTheme.ACCENT);
            classesTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
            classesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            sectionsPanel.add(classesTitle);
            sectionsPanel.add(Box.createVerticalStrut(4));

            JPanel classPanel = new JPanel(new GridLayout(0, 1, 0, 2));
            classPanel.setOpaque(false);
            classPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (int cid : classIds) {
                TrainingClass tc = trainingClassService.findById(cid);
                String label = tc != null
                        ? tc.getName() + "  |  " + tc.getType() + "  |  " + tc.getStartTime().format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
                        : "Class #" + cid;
                JLabel classLabel = new JLabel("  " + label);
                classLabel.setForeground(FWTheme.TEXT_SECONDARY);
                classLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                classPanel.add(classLabel);
            }
            sectionsPanel.add(classPanel);
        }

        // -- Assemble --
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(infoGrid, BorderLayout.NORTH);
        body.add(sectionsPanel, BorderLayout.CENTER);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JLabel buildStatusBadge(String status) {
        JLabel badge = new JLabel(" " + status + " ");
        badge.setOpaque(true);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        switch (status) {
            case "ACTIVE" -> {
                badge.setBackground(new Color(0, 200, 151, 40));
                badge.setForeground(FWTheme.SUCCESS);
            }
            case "PAUSED" -> {
                badge.setBackground(new Color(251, 191, 36, 40));
                badge.setForeground(new Color(251, 191, 36));
            }
            case "COMPLETED" -> {
                badge.setBackground(new Color(100, 116, 139, 40));
                badge.setForeground(FWTheme.TEXT_SECONDARY);
            }
            case "CANCELLED" -> {
                badge.setBackground(new Color(239, 68, 68, 40));
                badge.setForeground(FWTheme.DANGER);
            }
            default -> {
                badge.setBackground(FWTheme.CARD_BG);
                badge.setForeground(FWTheme.TEXT_PRIMARY);
            }
        }
        return badge;
    }

    private void addInfoPair(JPanel grid, String label, String value) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(FWTheme.TEXT_SECONDARY);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel val = new JLabel(value);
        val.setForeground(FWTheme.TEXT_PRIMARY);
        val.setFont(new Font("SansSerif", Font.PLAIN, 12));
        grid.add(lbl);
        grid.add(val);
    }

    private JLabel buildChip(String text, Color color) {
        JLabel chip = new JLabel(text);
        chip.setOpaque(true);
        chip.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        chip.setForeground(color);
        chip.setFont(new Font("SansSerif", Font.PLAIN, 11));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60), 1, true),
                new EmptyBorder(3, 8, 3, 8)));
        return chip;
    }
}
