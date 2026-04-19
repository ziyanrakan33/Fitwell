package fitwell.ui.consultant.panels;

import fitwell.service.attendance.AttendanceService;
import fitwell.service.auth.AuthenticationService;
import fitwell.controller.EquipmentAssignmentController;
import fitwell.service.equipment.InspectionWorkflowService;
import fitwell.service.attendance.LowAttendanceReportService;
import fitwell.service.training.TrainingClassQueryService;
import fitwell.service.training.TrainingClassService;
import fitwell.domain.registration.AttendanceStatus;
import fitwell.domain.training.TrainingClass;
import fitwell.domain.training.TrainingClassStatus;
import fitwell.ui.theme.FWTheme;
import fitwell.ui.theme.FWUi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConsultantClassesPanel extends JPanel {
    private final TrainingClassService trainingClassService;
    private final TrainingClassQueryService trainingClassQueryService;
    private final AttendanceService attendanceService;
    private final LowAttendanceReportService lowAttendanceReportService;
    private final EquipmentAssignmentController equipmentAssignmentController;
    private final InspectionWorkflowService inspectionWorkflowService;
    private final AuthenticationService authenticationService;

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> typeFilterCombo = new JComboBox<>(new String[]{
            "All Types", "Yoga", "Pilates", "TRX", "Strength", "Cardio", "Functional", "HIIT", "Mobility"
    });
    private final JLabel summaryLabel = new JLabel("Classes: 0");
    private final JLabel selectedClassLabel = new JLabel("Selected: None");
    private final JLabel registrationsCountLabel = new JLabel("Registrations: 0");

    private final DefaultTableModel classesModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Type", "Start", "End", "Max", "Status", "Tips"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel registrationsModel = new DefaultTableModel(
            new Object[]{"ClassID", "TraineeID", "Trainee Name", "Registered At", "Attendance"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable classesTable = new JTable(classesModel);
    private final JTable registrationsTable = new JTable(registrationsModel);
    private List<TrainingClass> filteredClasses = new ArrayList<>();
    private List<AttendanceService.AttendanceView> attendanceViews = new ArrayList<>();

    public ConsultantClassesPanel(TrainingClassService trainingClassService,
                                  TrainingClassQueryService trainingClassQueryService,
                                  AttendanceService attendanceService,
                                  LowAttendanceReportService lowAttendanceReportService,
                                  EquipmentAssignmentController equipmentAssignmentController,
                                  InspectionWorkflowService inspectionWorkflowService,
                                  AuthenticationService authenticationService) {
        this.trainingClassService = trainingClassService;
        this.trainingClassQueryService = trainingClassQueryService;
        this.attendanceService = attendanceService;
        this.lowAttendanceReportService = lowAttendanceReportService;
        this.equipmentAssignmentController = equipmentAssignmentController;
        this.inspectionWorkflowService = inspectionWorkflowService;
        this.authenticationService = authenticationService;

        setLayout(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(FWTheme.DASH_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        configureTable(classesTable);
        configureTable(registrationsTable);

        add(buildHeaderCard(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 12, 12));
        center.setOpaque(false);
        center.add(buildTableCard("Training Classes", "Classes are managed through training class services.",
                new JScrollPane(classesTable)));
        center.add(buildTableCard("Registrations and Attendance",
                "Attendance marks drive the consultant low-attendance report.",
                new JScrollPane(registrationsTable)));

        add(center, BorderLayout.CENTER);
        add(buildFooterActionsCard(), BorderLayout.SOUTH);

        classesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadRegistrationsForSelectedClass();
            }
        });
        classesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && classesTable.getSelectedRow() >= 0) {
                    TrainingClass tc = selectedClass();
                    if (tc != null) {
                        new ClassDetailsProDialog(
                                SwingUtilities.getWindowAncestor(ConsultantClassesPanel.this),
                                tc, inspectionWorkflowService
                        ).setVisible(true);
                    }
                }
            }
        });

        reloadClasses();
    }

    public void reloadClasses() {
        filteredClasses = trainingClassQueryService.getClassesForConsultant(
                searchField.getText(), String.valueOf(typeFilterCombo.getSelectedItem()));
        classesModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (TrainingClass trainingClass : filteredClasses) {
            classesModel.addRow(new Object[]{
                    trainingClass.getClassId(),
                    trainingClass.getName(),
                    trainingClass.getType(),
                    trainingClass.getStartTime() == null ? "" : trainingClass.getStartTime().format(formatter),
                    trainingClass.getEndTime() == null ? "" : trainingClass.getEndTime().format(formatter),
                    trainingClass.getMaxParticipants(),
                    trainingClass.getStatus(),
                    String.join(" | ", trainingClass.getTips())
            });
        }
        summaryLabel.setText("Classes: " + filteredClasses.size());
        loadRegistrationsForSelectedClass();
    }

    private JComponent buildHeaderCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        card.setLayout(new BorderLayout(12, 12));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Consultant Class Operations");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        JLabel sub = new JLabel("Manage class scheduling, equipment assignment, attendance, and report data.");
        sub.setForeground(FWTheme.TEXT_SECONDARY);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(sub);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        styleTextField(searchField);
        searchField.setColumns(18);
        searchField.addActionListener(e -> reloadClasses());
        typeFilterCombo.addActionListener(e -> reloadClasses());
        JButton refreshBtn = FWUi.primaryButton("Refresh");
        refreshBtn.addActionListener(e -> reloadClasses());
        JButton clearBtn = FWUi.ghostDarkButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            typeFilterCombo.setSelectedIndex(0);
            reloadClasses();
        });
        JButton reportBtn = FWUi.ghostDarkButton("Low Attendance Report");
        reportBtn.addActionListener(e -> openLowAttendanceReport());
        JButton assignEquipmentBtn = FWUi.primaryButton("Assign Equipment");
        assignEquipmentBtn.addActionListener(e -> openEquipmentAssignment());

        filters.add(new JLabelStyled("Search"));
        filters.add(searchField);
        filters.add(new JLabelStyled("Type"));
        filters.add(typeFilterCombo);
        filters.add(refreshBtn);
        filters.add(clearBtn);
        filters.add(reportBtn);
        filters.add(assignEquipmentBtn);

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        summary.setOpaque(false);
        styleSummaryChip(summaryLabel);
        styleSummaryChip(selectedClassLabel);
        styleSummaryChip(registrationsCountLabel);
        summary.add(summaryLabel);
        summary.add(selectedClassLabel);
        summary.add(registrationsCountLabel);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(filters, BorderLayout.NORTH);
        body.add(summary, BorderLayout.SOUTH);

        card.add(titleBox, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildTableCard(String titleText, String subtitleText, JComponent center) {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 14);
        card.setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(FWTheme.TEXT_SECONDARY);
        top.add(title);
        top.add(Box.createVerticalStrut(2));
        top.add(subtitle);
        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildFooterActionsCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        JButton addBtn = FWUi.primaryButton("Add Class");
        addBtn.addActionListener(e -> openClassDialog(null));
        JButton editBtn = FWUi.ghostDarkButton("Edit Class");
        editBtn.addActionListener(e -> openClassDialog(selectedClass()));
        JButton deleteBtn = FWUi.ghostDarkButton("Delete Class");
        deleteBtn.addActionListener(e -> deleteSelectedClass());
        JButton attendedBtn = FWUi.primaryButton("Mark Attended");
        attendedBtn.addActionListener(e -> markSelectedAttendance(AttendanceStatus.ATTENDED));
        JButton missedBtn = FWUi.ghostDarkButton("Mark Missed");
        missedBtn.addActionListener(e -> markSelectedAttendance(AttendanceStatus.MISSED));
        JButton refreshBtn = FWUi.ghostDarkButton("Refresh All");
        refreshBtn.addActionListener(e -> reloadClasses());

        JButton startBtn = FWUi.primaryButton("▶ Start");
        startBtn.addActionListener(e -> changeClassStatus(TrainingClassStatus.IN_PROGRESS));
        JButton pauseBtn = FWUi.ghostDarkButton("⏸ Pause");
        pauseBtn.addActionListener(e -> changeClassStatus(TrainingClassStatus.PAUSED));
        JButton completeBtn = FWUi.ghostDarkButton("✓ Complete");
        completeBtn.addActionListener(e -> changeClassStatus(TrainingClassStatus.COMPLETED));
        JButton cancelBtn = FWUi.ghostDarkButton("✕ Cancel");
        cancelBtn.addActionListener(e -> changeClassStatus(TrainingClassStatus.CANCELLED));

        card.add(addBtn);
        card.add(editBtn);
        card.add(deleteBtn);
        card.add(attendedBtn);
        card.add(missedBtn);
        card.add(startBtn);
        card.add(pauseBtn);
        card.add(completeBtn);
        card.add(cancelBtn);
        card.add(refreshBtn);
        return card;
    }

    private void openClassDialog(TrainingClass trainingClass) {
        TrainingClassDialog dialog = new TrainingClassDialog(
                SwingUtilities.getWindowAncestor(this), trainingClassService, trainingClass);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadClasses();
        }
    }

    private void deleteSelectedClass() {
        int row = classesTable.getSelectedRow();
        TrainingClass trainingClass = selectedClass();
        if (trainingClass == null) {
            showInfo("Select a class first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete class #" + trainingClass.getClassId() + "?",
                "Delete Class", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        trainingClassService.deleteClass(trainingClass.getClassId());
        reloadClasses();
        if (classesModel.getRowCount() > 0) {
            int newRow = Math.min(row, classesModel.getRowCount() - 1);
            classesTable.setRowSelectionInterval(newRow, newRow);
        }
    }

    private void loadRegistrationsForSelectedClass() {
        registrationsModel.setRowCount(0);
        TrainingClass trainingClass = selectedClass();
        if (trainingClass == null) {
            selectedClassLabel.setText("Selected: None");
            registrationsCountLabel.setText("Registrations: 0");
            return;
        }
        selectedClassLabel.setText("Selected: Class ID " + trainingClass.getClassId());
        attendanceViews = attendanceService.getAttendanceForClass(trainingClass.getClassId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (AttendanceService.AttendanceView view : attendanceViews) {
            registrationsModel.addRow(new Object[]{
                    view.getRegistration().getClassId(),
                    view.getRegistration().getTraineeId(),
                    view.getTraineeName(),
                    view.getRegistration().getRegistrationDateTime() == null ? ""
                            : view.getRegistration().getRegistrationDateTime().format(formatter),
                    view.getRegistration().getAttendanceStatus()
            });
        }
        registrationsCountLabel.setText("Registrations: " + attendanceViews.size());
    }

    private void markSelectedAttendance(AttendanceStatus status) {
        int row = registrationsTable.getSelectedRow();
        if (row < 0 || row >= attendanceViews.size()) {
            showInfo("Select a registration first.");
            return;
        }
        AttendanceService.AttendanceView view = attendanceViews.get(row);
        attendanceService.markAttendance(view.getRegistration().getClassId(), view.getRegistration().getTraineeId(), status);
        loadRegistrationsForSelectedClass();
    }

    private void changeClassStatus(TrainingClassStatus newStatus) {
        TrainingClass tc = selectedClass();
        if (tc == null) {
            showInfo("Select a class first.");
            return;
        }
        trainingClassService.updateStatus(tc.getClassId(), newStatus);
        reloadClasses();
    }

    private void openLowAttendanceReport() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        new UnregisteredClassReportProDialog(owner, lowAttendanceReportService).setVisible(true);
    }

    private void openEquipmentAssignment() {
        TrainingClass trainingClass = selectedClass();
        if (trainingClass == null) {
            showInfo("Select a class first.");
            return;
        }
        new EquipmentAssignmentProDialog(SwingUtilities.getWindowAncestor(this),
                equipmentAssignmentController, trainingClass.getClassId(),
                authenticationService.getCurrentUserId()).setVisible(true);
    }

    private TrainingClass selectedClass() {
        int row = classesTable.getSelectedRow();
        if (row < 0 || row >= filteredClasses.size()) {
            return null;
        }
        return filteredClasses.get(row);
    }

    private void configureTable(JTable table) {
        table.setRowHeight(30);
        table.setBackground(FWTheme.CARD_BG);
        table.setForeground(FWTheme.TEXT_PRIMARY);
        table.setSelectionBackground(new Color(34, 50, 83));
        table.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        table.setGridColor(FWTheme.BORDER);
    }

    private void styleTextField(JTextField field) {
        field.setBackground(FWTheme.CARD_BG);
        field.setForeground(FWTheme.TEXT_PRIMARY);
        field.setCaretColor(FWTheme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)
        ));
    }

    private void styleSummaryChip(JLabel label) {
        label.setOpaque(true);
        label.setForeground(FWTheme.TEXT_SECONDARY);
        label.setBackground(new Color(14, 22, 40));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FWTheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class JLabelStyled extends JLabel {
        JLabelStyled(String text) {
            super(text);
            setForeground(FWTheme.TEXT_SECONDARY);
            setFont(new Font("SansSerif", Font.BOLD, 12));
        }
    }
}
