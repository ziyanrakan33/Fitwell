package fitwell.ui.pro.trainee;

import fitwell.entity.GroupPlan;
import fitwell.entity.PersonalPlan;
import fitwell.entity.Plan;

import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlanPanel extends JPanel {

    private final fitwell.control.TrainingPlanService planService = fitwell.control.FitWellServiceRegistry.getInstance().trainingPlanService();
    private final int currentTraineeId = 1; // Assuming Trainee ID 1 for now

    public PlanPanel() {
        setLayout(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(FWTheme.DASH_BG);

        JPanel header = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        header.setLayout(new BorderLayout());

        JLabel title = new JLabel("My Active Plans");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JLabel sub = new JLabel("Your upcoming and active training plans");
        sub.setForeground(FWTheme.TEXT_SECONDARY);

        JPanel txt = new JPanel(new GridLayout(2, 1));
        txt.setOpaque(false);
        txt.add(title);
        txt.add(sub);

        header.add(txt, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
        add(buildPlansGrid(), BorderLayout.CENTER);
    }

    private JComponent buildPlansGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);

        List<Plan> plans = planService.getPlansForTrainee(currentTraineeId);

        if (plans.isEmpty()) {
            JLabel noPlans = new JLabel("No active plans found.");
            noPlans.setForeground(FWTheme.TEXT_SECONDARY);
            noPlans.setFont(FWTheme.FONT_BODY);
            noPlans.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.add(noPlans, BorderLayout.CENTER);
            return wrap;
        }

        for (Plan plan : plans) {
            grid.add(buildPlanCard(plan));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(grid, BorderLayout.NORTH);
        return new JScrollPane(wrapper, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {{
            getViewport().setOpaque(false);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }};
    }

    private JPanel buildPlanCard(Plan plan) {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 10));

        String planType = (plan instanceof PersonalPlan) ? "Personal Plan" : "Group Plan";
        JLabel title = new JLabel(planType + " (#" + plan.getPlanId() + ")");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel status = new JLabel(plan.getStatus().toUpperCase());
        status.setFont(new Font("SansSerif", Font.BOLD, 12));
        status.setForeground(plan.getStatus().equalsIgnoreCase("Active") ? FWTheme.SUCCESS : 
                             plan.getStatus().equalsIgnoreCase("Pending") ? FWTheme.ACCENT : FWTheme.TEXT_SECONDARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 6));
        content.setOpaque(false);

        content.add(createRow("Start Date:", plan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        content.add(createRow("Duration:", plan.getDurationMonths() + " Months"));

        if (plan instanceof PersonalPlan) {
            PersonalPlan pp = (PersonalPlan) plan;
            content.add(createRow("Dietary Restrictions:", pp.getDietaryRestrictions()));
        } else if (plan instanceof GroupPlan) {
            GroupPlan gp = (GroupPlan) plan;
            content.add(createRow("Age Range:", gp.getAgeRange()));
            content.add(createRow("Preferred Classes:", gp.getPreferredClassTypes()));
        }

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        
        JLabel lbl = new JLabel(label);
        lbl.setForeground(FWTheme.TEXT_SECONDARY);
        lbl.setFont(FWTheme.FONT_BODY);
        
        JLabel val = new JLabel(value);
        val.setForeground(FWTheme.TEXT_PRIMARY);
        val.setFont(new Font("SansSerif", Font.BOLD, 13));
        
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }
}
