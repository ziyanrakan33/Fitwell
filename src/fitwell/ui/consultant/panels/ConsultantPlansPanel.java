package fitwell.ui.consultant.panels;

import fitwell.service.training.TraineeProfileService;
import fitwell.service.training.TrainingClassService;
import fitwell.service.training.TrainingPlanService;
import fitwell.domain.shared.DietaryRestriction;
import fitwell.domain.training.GroupPlan;
import fitwell.domain.training.PersonalPlan;
import fitwell.domain.training.Plan;
import fitwell.domain.training.PlanStatus;
import fitwell.domain.user.Trainee;
import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingPlanMember;
import fitwell.ui.theme.FWTheme;
import fitwell.ui.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConsultantPlansPanel extends JPanel {
    private final TrainingPlanService planService;
    private final TraineeProfileService traineeProfileService;
    private final TrainingClassService trainingClassService;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Type", "Owner", "Start", "Duration", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<Plan> visiblePlans = new ArrayList<>();

    private final JTextArea detailArea = new JTextArea();

    public ConsultantPlansPanel(TrainingPlanService planService,
                                TraineeProfileService traineeProfileService,
                                TrainingClassService trainingClassService) {
        this.planService = planService;
        this.traineeProfileService = traineeProfileService;
        this.trainingClassService = trainingClassService;
        setLayout(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(FWTheme.DASH_BG);
        buildUi();
        reload();
    }

    public void reload() {
        visiblePlans = new ArrayList<>(planService.getAllPlans());
        tableModel.setRowCount(0);
        for (Plan p : visiblePlans) {
            String type = p instanceof PersonalPlan ? "Personal" : "Group";
            Trainee owner = traineeProfileService.findById(p.getOwnerTraineeId());
            String ownerName = owner != null ? owner.fullName() : "Trainee #" + p.getOwnerTraineeId();
            tableModel.addRow(new Object[]{
                    p.getPlanId(),
                    type,
                    ownerName,
                    p.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    p.getDurationMonths() + " mo",
                    p.getStatus()
            });
        }
        detailArea.setText("");
        revalidate();
        repaint();
    }

    private void buildUi() {
        JPanel toolbar = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton addPersonal = FWUi.primaryButton("+ Personal Plan");
        addPersonal.addActionListener(e -> createPlan(true));
        JButton addGroup = FWUi.primaryButton("+ Group Plan");
        addGroup.addActionListener(e -> createPlan(false));
        JButton editBtn = FWUi.ghostDarkButton("Edit");
        editBtn.addActionListener(e -> editSelectedPlan());
        JButton statusBtn = FWUi.ghostDarkButton("Change Status");
        statusBtn.addActionListener(e -> changeStatusOfSelected());
        JButton membersBtn = FWUi.ghostDarkButton("Manage Members");
        membersBtn.addActionListener(e -> manageMembers());
        JButton classesBtn = FWUi.ghostDarkButton("Assign Classes");
        classesBtn.addActionListener(e -> assignClasses());
        JButton refreshBtn = FWUi.ghostDarkButton("Refresh");
        refreshBtn.addActionListener(e -> reload());

        JLabel title = new JLabel("  Training Plan Management");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        toolbar.add(addPersonal);
        toolbar.add(addGroup);
        toolbar.add(editBtn);
        toolbar.add(statusBtn);
        toolbar.add(membersBtn);
        toolbar.add(classesBtn);
        toolbar.add(refreshBtn);
        toolbar.add(title);

        table.setBackground(FWTheme.CARD_BG);
        table.setForeground(FWTheme.TEXT_PRIMARY);
        table.setSelectionBackground(new Color(34, 50, 83));
        table.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        table.setRowHeight(28);
        table.setGridColor(FWTheme.BORDER);
        table.getTableHeader().setBackground(FWTheme.SIDEBAR_BG);
        table.getTableHeader().setForeground(FWTheme.TEXT_SECONDARY);

        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel && value != null) {
                    switch (value.toString()) {
                        case "ACTIVE" -> setForeground(FWTheme.SUCCESS);
                        case "PAUSED" -> setForeground(new Color(251, 191, 36));
                        case "COMPLETED" -> setForeground(FWTheme.TEXT_SECONDARY);
                        case "CANCELLED" -> setForeground(FWTheme.DANGER);
                        default -> setForeground(FWTheme.TEXT_PRIMARY);
                    }
                }
                setBackground(sel ? new Color(34, 50, 83) : FWTheme.CARD_BG);
                return this;
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(statusRenderer);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showPlanDetails();
        });

        detailArea.setEditable(false);
        detailArea.setOpaque(true);
        detailArea.setBackground(FWTheme.CARD_BG);
        detailArea.setForeground(FWTheme.TEXT_SECONDARY);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER));
        detailScroll.setPreferredSize(new Dimension(0, 160));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(detailScroll, BorderLayout.SOUTH);

        add(toolbar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void showPlanDetails() {
        Plan plan = selectedPlan();
        if (plan == null) {
            detailArea.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(plan instanceof PersonalPlan ? "PERSONAL PLAN" : "GROUP PLAN");
        sb.append("  #").append(plan.getPlanId()).append("\n");
        sb.append("Status: ").append(plan.getStatus()).append("\n");
        sb.append("Start: ").append(plan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
        sb.append("Duration: ").append(plan.getDurationMonths()).append(" months\n");

        Trainee owner = traineeProfileService.findById(plan.getOwnerTraineeId());
        sb.append("Owner: ").append(owner != null ? owner.fullName() + " (#" + owner.getId() + ")" : "Trainee #" + plan.getOwnerTraineeId()).append("\n");

        if (plan instanceof PersonalPlan pp) {
            sb.append("\nDietary Restrictions: ").append(pp.getDietaryRestrictions()).append("\n");
            sb.append("Dietitian Notes: ").append(pp.getDietitianNotes()).append("\n");
        } else if (plan instanceof GroupPlan gp) {
            sb.append("\nAge Range: ").append(gp.getAgeRange()).append("\n");
            sb.append("Preferred Class Types: ").append(gp.getPreferredClassTypes()).append("\n");
            sb.append("Guidelines: ").append(gp.getGeneralGuidelines()).append("\n");

            List<TrainingPlanMember> members = planService.getMembersForPlan(plan.getPlanId());
            sb.append("\nMembers (").append(members.size()).append("):\n");
            for (TrainingPlanMember m : members) {
                Trainee t = traineeProfileService.findById(m.getTraineeId());
                sb.append("  - ").append(t != null ? t.fullName() : "Trainee #" + m.getTraineeId());
                sb.append(" (").append(m.getRole()).append(")\n");
            }
        }

        Set<Integer> classIds = planService.getClassIdsForPlan(plan.getPlanId());
        if (!classIds.isEmpty()) {
            sb.append("\nAssigned Classes (").append(classIds.size()).append("):\n");
            for (int cid : classIds) {
                TrainingClass tc = trainingClassService.findById(cid);
                sb.append("  - ").append(tc != null ? tc.getName() + " (#" + cid + ")" : "Class #" + cid).append("\n");
            }
        }

        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }

    private void createPlan(boolean personal) {
        PlanFormDialog dialog = new PlanFormDialog(
                SwingUtilities.getWindowAncestor(this), personal, null,
                planService, traineeProfileService);
        dialog.setVisible(true);
        if (dialog.isSaved()) reload();
    }

    private void editSelectedPlan() {
        Plan plan = selectedPlan();
        if (plan == null) {
            JOptionPane.showMessageDialog(this, "Select a plan first.");
            return;
        }
        boolean personal = plan instanceof PersonalPlan;
        PlanFormDialog dialog = new PlanFormDialog(
                SwingUtilities.getWindowAncestor(this), personal, plan,
                planService, traineeProfileService);
        dialog.setVisible(true);
        if (dialog.isSaved()) reload();
    }

    private void changeStatusOfSelected() {
        Plan plan = selectedPlan();
        if (plan == null) {
            JOptionPane.showMessageDialog(this, "Select a plan first.");
            return;
        }
        PlanStatus current = plan.getPlanStatus();
        PlanStatus[] statuses = PlanStatus.values();
        PlanStatus chosen = (PlanStatus) JOptionPane.showInputDialog(
                this, "Change status of Plan #" + plan.getPlanId() + " (currently " + current + "):",
                "Change Plan Status", JOptionPane.QUESTION_MESSAGE, null, statuses, current);
        if (chosen == null || chosen == current) return;
        plan.setStatus(chosen);
        planService.savePlan(plan);
        reload();
    }

    private void manageMembers() {
        Plan plan = selectedPlan();
        if (plan == null) {
            JOptionPane.showMessageDialog(this, "Select a plan first.");
            return;
        }
        if (!(plan instanceof GroupPlan)) {
            JOptionPane.showMessageDialog(this, "Member management is only for group plans.");
            return;
        }
        MemberManagementDialog dialog = new MemberManagementDialog(
                SwingUtilities.getWindowAncestor(this), plan.getPlanId(),
                planService, traineeProfileService);
        dialog.setVisible(true);
        reload();
    }

    private void assignClasses() {
        Plan plan = selectedPlan();
        if (plan == null) {
            JOptionPane.showMessageDialog(this, "Select a plan first.");
            return;
        }
        ClassAssignmentDialog dialog = new ClassAssignmentDialog(
                SwingUtilities.getWindowAncestor(this), plan.getPlanId(),
                planService, trainingClassService);
        dialog.setVisible(true);
        reload();
    }

    private Plan selectedPlan() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visiblePlans.size()) return null;
        return visiblePlans.get(row);
    }

    // ===== Plan Create / Edit Dialog =====
    static class PlanFormDialog extends JDialog {
        private final TrainingPlanService planService;
        private final boolean personal;
        private final Plan editing;
        private boolean saved = false;

        private final JComboBox<String> ownerCombo = new JComboBox<>();
        private final JTextField startDateField = new JTextField(12);
        private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 60, 1));
        private final JComboBox<PlanStatus> statusCombo = new JComboBox<>(PlanStatus.values());

        private final java.util.EnumSet<DietaryRestriction> selectedRestrictions =
                java.util.EnumSet.noneOf(DietaryRestriction.class);
        private final JTextField dietaryField = new JTextField(20);  // read-only display
        private final JTextArea dietitianNotesArea = new JTextArea(3, 20);

        private final JTextField ageRangeField = new JTextField(20);
        private final JTextField classTypesField = new JTextField(20);
        private final JTextArea guidelinesArea = new JTextArea(3, 20);

        private final List<Trainee> trainees;

        PlanFormDialog(Window owner, boolean personal, Plan editing,
                       TrainingPlanService planService, TraineeProfileService traineeProfileService) {
            super(owner, (editing == null ? "Create " : "Edit ") + (personal ? "Personal" : "Group") + " Plan",
                    ModalityType.APPLICATION_MODAL);
            this.personal = personal;
            this.editing = editing;
            this.planService = planService;
            this.trainees = traineeProfileService.getAllTrainees();
            buildUi();
            if (editing != null) loadData();
            pack();
            setMinimumSize(new Dimension(520, personal ? 420 : 470));
            setLocationRelativeTo(owner);
        }

        boolean isSaved() { return saved; }

        private void buildUi() {
            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBackground(FWTheme.DASH_BG);
            root.setBorder(new EmptyBorder(14, 14, 14, 14));
            setContentPane(root);

            JPanel form = FWUi.cardPanel(FWTheme.CARD_BG, 14);
            form.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 8, 6, 8);
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;

            for (Trainee t : trainees) {
                ownerCombo.addItem(t.fullName() + " (#" + t.getId() + ")");
            }
            ownerCombo.setBackground(FWTheme.CARD_BG);
            ownerCombo.setForeground(FWTheme.TEXT_PRIMARY);
            statusCombo.setBackground(FWTheme.CARD_BG);
            statusCombo.setForeground(FWTheme.TEXT_PRIMARY);
            styleField(startDateField);
            startDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            durationSpinner.getEditor().getComponent(0).setBackground(FWTheme.CARD_BG);
            durationSpinner.getEditor().getComponent(0).setForeground(FWTheme.TEXT_PRIMARY);

            int row = 0;
            addFormRow(form, gc, row++, "Owner Trainee", ownerCombo);
            addFormRow(form, gc, row++, "Start Date (yyyy-mm-dd)", startDateField);
            addFormRow(form, gc, row++, "Duration (months)", durationSpinner);
            addFormRow(form, gc, row++, "Status", statusCombo);

            if (personal) {
                dietaryField.setEditable(false);
                styleField(dietaryField);
                styleTextArea(dietitianNotesArea);
                JPanel dietaryRow = new JPanel(new BorderLayout(6, 0));
                dietaryRow.setOpaque(false);
                dietaryRow.add(dietaryField, BorderLayout.CENTER);
                JButton pickBtn = FWUi.ghostDarkButton("Pick...");
                pickBtn.addActionListener(e -> openRestrictionPicker());
                dietaryRow.add(pickBtn, BorderLayout.EAST);
                addFormRow(form, gc, row++, "Dietary Restrictions", dietaryRow);
                addFormRow(form, gc, row++, "Dietitian Notes", new JScrollPane(dietitianNotesArea));
            } else {
                styleField(ageRangeField);
                styleField(classTypesField);
                styleTextArea(guidelinesArea);
                addFormRow(form, gc, row++, "Age Range", ageRangeField);
                addFormRow(form, gc, row++, "Preferred Class Types", classTypesField);
                addFormRow(form, gc, row++, "Guidelines", new JScrollPane(guidelinesArea));
            }

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            footer.setOpaque(false);
            JButton cancelBtn = FWUi.ghostDarkButton("Cancel");
            cancelBtn.addActionListener(e -> dispose());
            JButton saveBtn = FWUi.primaryButton(editing == null ? "Create" : "Save");
            saveBtn.addActionListener(e -> onSave());
            footer.add(cancelBtn);
            footer.add(saveBtn);

            root.add(form, BorderLayout.CENTER);
            root.add(footer, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(saveBtn);
        }

        private void openRestrictionPicker() {
            DietaryRestriction[] all = DietaryRestriction.values();
            JCheckBox[] boxes = new JCheckBox[all.length];
            for (int i = 0; i < all.length; i++) {
                boxes[i] = new JCheckBox(all[i].getDisplayName(), selectedRestrictions.contains(all[i]));
                boxes[i].setOpaque(false);
                boxes[i].setForeground(FWTheme.TEXT_PRIMARY);
            }
            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 4));
            panel.setBackground(FWTheme.CARD_BG);
            for (JCheckBox cb : boxes) panel.add(cb);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Select Dietary Restrictions", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            selectedRestrictions.clear();
            for (int i = 0; i < all.length; i++) {
                if (boxes[i].isSelected()) selectedRestrictions.add(all[i]);
            }
            dietaryField.setText(DietaryRestriction.toCsv(selectedRestrictions));
        }

        private void loadData() {
            for (int i = 0; i < trainees.size(); i++) {
                if (trainees.get(i).getId() == editing.getOwnerTraineeId()) {
                    ownerCombo.setSelectedIndex(i);
                    break;
                }
            }
            startDateField.setText(editing.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            durationSpinner.setValue(editing.getDurationMonths());
            statusCombo.setSelectedItem(editing.getPlanStatus());

            if (editing instanceof PersonalPlan pp) {
                selectedRestrictions.clear();
                for (DietaryRestriction dr : DietaryRestriction.fromCsv(pp.getDietaryRestrictions())) {
                    selectedRestrictions.add(dr);
                }
                dietaryField.setText(DietaryRestriction.toCsv(selectedRestrictions));
                dietitianNotesArea.setText(pp.getDietitianNotes());
            } else if (editing instanceof GroupPlan gp) {
                ageRangeField.setText(gp.getAgeRange());
                classTypesField.setText(gp.getPreferredClassTypes());
                guidelinesArea.setText(gp.getGeneralGuidelines());
            }
        }

        private void onSave() {
            try {
                int ownerIdx = ownerCombo.getSelectedIndex();
                if (ownerIdx < 0) {
                    JOptionPane.showMessageDialog(this, "Select an owner trainee.");
                    return;
                }
                int ownerId = trainees.get(ownerIdx).getId();
                LocalDate start = LocalDate.parse(startDateField.getText().trim());
                int duration = (int) durationSpinner.getValue();
                PlanStatus status = (PlanStatus) statusCombo.getSelectedItem();

                Plan plan;
                String dietaryCsv = DietaryRestriction.toCsv(selectedRestrictions);
                if (personal) {
                    if (editing != null) {
                        PersonalPlan pp = (PersonalPlan) editing;
                        pp.setStartDate(start);
                        pp.setDurationMonths(duration);
                        pp.setStatus(status);
                        pp.setDietaryRestrictions(dietaryCsv);
                        pp.setDietitianNotes(dietitianNotesArea.getText());
                        plan = pp;
                    } else {
                        plan = new PersonalPlan(0, ownerId, start, duration, status,
                                dietaryCsv, dietitianNotesArea.getText());
                    }
                } else {
                    if (editing != null) {
                        GroupPlan gp = (GroupPlan) editing;
                        gp.setStartDate(start);
                        gp.setDurationMonths(duration);
                        gp.setStatus(status);
                        gp.setAgeRange(ageRangeField.getText());
                        gp.setPreferredClassTypes(classTypesField.getText());
                        gp.setGeneralGuidelines(guidelinesArea.getText());
                        plan = gp;
                    } else {
                        plan = new GroupPlan(0, ownerId, start, duration, status,
                                ageRangeField.getText(), classTypesField.getText(),
                                guidelinesArea.getText());
                    }
                }
                planService.savePlan(plan);
                saved = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                        "Validation", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void addFormRow(JPanel parent, GridBagConstraints gc, int row, String label, JComponent field) {
            gc.gridx = 0; gc.gridy = row; gc.weightx = 0.3;
            JLabel lbl = new JLabel(label);
            lbl.setForeground(FWTheme.TEXT_SECONDARY);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            parent.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.7;
            parent.add(field, gc);
        }

        private void styleField(JTextField tf) {
            tf.setBackground(FWTheme.CARD_BG);
            tf.setForeground(FWTheme.TEXT_PRIMARY);
            tf.setCaretColor(FWTheme.TEXT_PRIMARY);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                    new EmptyBorder(8, 10, 8, 10)));
        }

        private void styleTextArea(JTextArea ta) {
            ta.setBackground(FWTheme.CARD_BG);
            ta.setForeground(FWTheme.TEXT_PRIMARY);
            ta.setCaretColor(FWTheme.TEXT_PRIMARY);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setBorder(new EmptyBorder(6, 8, 6, 8));
        }
    }

    // ===== Member Management Dialog (Group Plans) =====
    static class MemberManagementDialog extends JDialog {
        private final int planId;
        private final TrainingPlanService planService;
        private final TraineeProfileService traineeProfileService;
        private final DefaultListModel<String> memberModel = new DefaultListModel<>();
        private final JList<String> memberList = new JList<>(memberModel);
        private List<TrainingPlanMember> currentMembers = new ArrayList<>();

        MemberManagementDialog(Window owner, int planId,
                               TrainingPlanService planService, TraineeProfileService traineeProfileService) {
            super(owner, "Manage Members — Plan #" + planId, ModalityType.APPLICATION_MODAL);
            this.planId = planId;
            this.planService = planService;
            this.traineeProfileService = traineeProfileService;
            buildUi();
            refreshMembers();
            pack();
            setMinimumSize(new Dimension(420, 350));
            setLocationRelativeTo(owner);
        }

        private void buildUi() {
            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBackground(FWTheme.DASH_BG);
            root.setBorder(new EmptyBorder(14, 14, 14, 14));
            setContentPane(root);

            memberList.setBackground(FWTheme.CARD_BG);
            memberList.setForeground(FWTheme.TEXT_PRIMARY);
            memberList.setBorder(new EmptyBorder(6, 8, 6, 8));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setOpaque(false);
            JButton addBtn = FWUi.primaryButton("Add Member");
            addBtn.addActionListener(e -> addMember());
            JButton removeBtn = FWUi.ghostDarkButton("Remove Selected");
            removeBtn.addActionListener(e -> removeSelected());
            JButton closeBtn = FWUi.ghostDarkButton("Close");
            closeBtn.addActionListener(e -> dispose());
            actions.add(addBtn);
            actions.add(removeBtn);
            actions.add(closeBtn);

            JLabel title = new JLabel("Members of Plan #" + planId);
            title.setForeground(FWTheme.TEXT_PRIMARY);
            title.setFont(FWTheme.FONT_H2);

            root.add(title, BorderLayout.NORTH);
            root.add(new JScrollPane(memberList), BorderLayout.CENTER);
            root.add(actions, BorderLayout.SOUTH);
        }

        private void refreshMembers() {
            currentMembers = planService.getMembersForPlan(planId);
            memberModel.clear();
            for (TrainingPlanMember m : currentMembers) {
                Trainee t = traineeProfileService.findById(m.getTraineeId());
                String name = t != null ? t.fullName() : "Unknown";
                memberModel.addElement(name + " (#" + m.getTraineeId() + ") — " + m.getRole());
            }
        }

        private void addMember() {
            List<Trainee> allTrainees = traineeProfileService.getAllTrainees();
            java.util.Set<Integer> existingIds = new java.util.HashSet<>();
            for (TrainingPlanMember m : currentMembers) existingIds.add(m.getTraineeId());

            Plan plan = planService.findById(planId);
            if (plan != null) existingIds.add(plan.getOwnerTraineeId());

            List<Trainee> available = new ArrayList<>();
            for (Trainee t : allTrainees) {
                if (!existingIds.contains(t.getId())) available.add(t);
            }
            if (available.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All trainees are already members or the owner.");
                return;
            }
            String[] names = new String[available.size()];
            for (int i = 0; i < available.size(); i++) {
                names[i] = available.get(i).fullName() + " (#" + available.get(i).getId() + ")";
            }
            String chosen = (String) JOptionPane.showInputDialog(this, "Select trainee to add:",
                    "Add Member", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if (chosen == null) return;

            int idx = 0;
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(chosen)) { idx = i; break; }
            }
            planService.addMemberToPlan(planId, available.get(idx).getId());
            refreshMembers();
        }

        private void removeSelected() {
            int idx = memberList.getSelectedIndex();
            if (idx < 0 || idx >= currentMembers.size()) {
                JOptionPane.showMessageDialog(this, "Select a member first.");
                return;
            }
            TrainingPlanMember m = currentMembers.get(idx);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove trainee #" + m.getTraineeId() + " from plan?",
                    "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                planService.removeMemberFromPlan(planId, m.getTraineeId());
                refreshMembers();
            }
        }
    }

    // ===== Class Assignment Dialog =====
    static class ClassAssignmentDialog extends JDialog {
        private final int planId;
        private final TrainingPlanService planService;
        private final TrainingClassService classService;
        private final DefaultListModel<String> assignedModel = new DefaultListModel<>();
        private final JList<String> assignedList = new JList<>(assignedModel);
        private List<Integer> assignedClassIds = new ArrayList<>();

        ClassAssignmentDialog(Window owner, int planId,
                              TrainingPlanService planService, TrainingClassService classService) {
            super(owner, "Assign Classes — Plan #" + planId, ModalityType.APPLICATION_MODAL);
            this.planId = planId;
            this.planService = planService;
            this.classService = classService;
            buildUi();
            refreshAssigned();
            pack();
            setMinimumSize(new Dimension(460, 370));
            setLocationRelativeTo(owner);
        }

        private void buildUi() {
            JPanel root = new JPanel(new BorderLayout(12, 12));
            root.setBackground(FWTheme.DASH_BG);
            root.setBorder(new EmptyBorder(14, 14, 14, 14));
            setContentPane(root);

            assignedList.setBackground(FWTheme.CARD_BG);
            assignedList.setForeground(FWTheme.TEXT_PRIMARY);
            assignedList.setBorder(new EmptyBorder(6, 8, 6, 8));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setOpaque(false);
            JButton addBtn = FWUi.primaryButton("Assign Class");
            addBtn.addActionListener(e -> assignClass());
            JButton removeBtn = FWUi.ghostDarkButton("Unassign Selected");
            removeBtn.addActionListener(e -> unassignSelected());
            JButton closeBtn = FWUi.ghostDarkButton("Close");
            closeBtn.addActionListener(e -> dispose());
            actions.add(addBtn);
            actions.add(removeBtn);
            actions.add(closeBtn);

            JLabel title = new JLabel("Classes assigned to Plan #" + planId);
            title.setForeground(FWTheme.TEXT_PRIMARY);
            title.setFont(FWTheme.FONT_H2);

            root.add(title, BorderLayout.NORTH);
            root.add(new JScrollPane(assignedList), BorderLayout.CENTER);
            root.add(actions, BorderLayout.SOUTH);
        }

        private void refreshAssigned() {
            Set<Integer> ids = planService.getClassIdsForPlan(planId);
            assignedClassIds = new ArrayList<>(ids);
            assignedModel.clear();
            for (int cid : assignedClassIds) {
                TrainingClass tc = classService.findById(cid);
                assignedModel.addElement(tc != null
                        ? tc.getName() + " (#" + cid + ") — " + tc.getType()
                        : "Class #" + cid);
            }
        }

        private void assignClass() {
            List<TrainingClass> allClasses = classService.getAllClasses();
            Set<Integer> existing = planService.getClassIdsForPlan(planId);
            List<TrainingClass> available = new ArrayList<>();
            for (TrainingClass tc : allClasses) {
                if (tc.getClassId() != null && !existing.contains(tc.getClassId())) {
                    available.add(tc);
                }
            }
            if (available.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All classes are already assigned to this plan.");
                return;
            }
            String[] names = new String[available.size()];
            for (int i = 0; i < available.size(); i++) {
                TrainingClass tc = available.get(i);
                names[i] = tc.getName() + " (#" + tc.getClassId() + ") — " + tc.getType();
            }
            String chosen = (String) JOptionPane.showInputDialog(this, "Select class to assign:",
                    "Assign Class", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
            if (chosen == null) return;

            int idx = 0;
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(chosen)) { idx = i; break; }
            }
            planService.assignClassToPlan(planId, available.get(idx).getClassId());
            refreshAssigned();
        }

        private void unassignSelected() {
            int idx = assignedList.getSelectedIndex();
            if (idx < 0 || idx >= assignedClassIds.size()) {
                JOptionPane.showMessageDialog(this, "Select a class first.");
                return;
            }
            int classId = assignedClassIds.get(idx);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Unassign class #" + classId + " from this plan?",
                    "Confirm Unassign", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                planService.unassignClassFromPlan(planId, classId);
                refreshAssigned();
            }
        }
    }
}
