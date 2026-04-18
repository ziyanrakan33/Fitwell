package fitwell.ui.pro.trainee;

import fitwell.control.FitWellServiceRegistry;
import fitwell.control.RegistrationController;
import fitwell.control.TraineeProfileService;
import fitwell.control.TrainingClassQueryService;
import fitwell.domain.training.TrainingClass;
import fitwell.repo.RegistrationRepository;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TraineeClassRegistrationPanel extends JPanel {
    private final TrainingClassQueryService trainingClassQueryService;
    private final TraineeProfileService traineeProfileService;
    private final RegistrationController registrationController;
    private final RegistrationRepository registrationRepository;

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "All Types", "Yoga", "Pilates", "TRX", "Strength", "Cardio", "Functional", "HIIT", "Mobility"
    });
    private final JCheckBox myOnlyToggle = new JCheckBox("My Registrations Only");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Type", "Start", "End", "Max", "Registration", "Tips"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<TrainingClass> visibleClasses = new ArrayList<>();

    public TraineeClassRegistrationPanel(TrainingClassQueryService trainingClassQueryService,
                                         TraineeProfileService traineeProfileService,
                                         RegistrationController registrationController) {
        this.trainingClassQueryService = trainingClassQueryService;
        this.traineeProfileService = traineeProfileService;
        this.registrationController = registrationController;
        this.registrationRepository = FitWellServiceRegistry.getInstance().registrationRepository();
        setLayout(new BorderLayout(12, 12));
        setOpaque(true);
        setBackground(FWTheme.DASH_BG);
        buildUi();
        reload();
    }

    public void reload() {
        int traineeId = traineeProfileService.getCurrentTrainee().getId();
        List<TrainingClass> allAvailable = trainingClassQueryService.getClassesForTrainee(
                traineeId, searchField.getText(), String.valueOf(typeCombo.getSelectedItem()));

        visibleClasses = new ArrayList<>();
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (TrainingClass tc : allAvailable) {
            boolean registered = registrationRepository.isRegistered(tc.getClassId(), traineeId);

            if (myOnlyToggle.isSelected() && !registered) {
                continue;
            }

            visibleClasses.add(tc);
            tableModel.addRow(new Object[]{
                    tc.getClassId(),
                    tc.getName(),
                    tc.getType(),
                    tc.getStartTime() == null ? "" : tc.getStartTime().format(formatter),
                    tc.getEndTime() == null ? "" : tc.getEndTime().format(formatter),
                    tc.getMaxParticipants(),
                    registered ? "Registered" : "Not Registered",
                    String.join(" | ", tc.getTips())
            });
        }
    }

    private void buildUi() {
        JPanel toolbar = FWUi.cardPanel(FWTheme.CARD_BG, 12);
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        style(searchField);
        searchField.setColumns(14);
        searchField.addActionListener(e -> reload());
        typeCombo.addActionListener(e -> reload());

        myOnlyToggle.setOpaque(false);
        myOnlyToggle.setForeground(FWTheme.TEXT_PRIMARY);
        myOnlyToggle.setFont(FWTheme.FONT_BODY);
        myOnlyToggle.addActionListener(e -> reload());

        JButton refresh = FWUi.ghostDarkButton("Refresh");
        refresh.addActionListener(e -> reload());
        JButton register = FWUi.primaryButton("Register");
        register.addActionListener(e -> registerSelected());
        JButton unregister = FWUi.ghostDarkButton("Unregister");
        unregister.addActionListener(e -> unregisterSelected());

        toolbar.add(new JLabel("Search"));
        toolbar.add(searchField);
        toolbar.add(new JLabel("Type"));
        toolbar.add(typeCombo);
        toolbar.add(myOnlyToggle);
        toolbar.add(refresh);
        toolbar.add(register);
        toolbar.add(unregister);

        table.setBackground(FWTheme.CARD_BG);
        table.setForeground(FWTheme.TEXT_PRIMARY);
        table.setSelectionBackground(FWTheme.CARD_BG_HOVER);
        table.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        table.setRowHeight(28);

        int regCol = 6;
        table.getColumnModel().getColumn(regCol).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if ("Registered".equals(value)) {
                    c.setForeground(FWTheme.SUCCESS);
                } else {
                    c.setForeground(FWTheme.TEXT_SECONDARY);
                }
                if (isSelected) {
                    c.setBackground(FWTheme.CARD_BG_HOVER);
                } else {
                    c.setBackground(FWTheme.CARD_BG);
                }
                return c;
            }
        });

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void registerSelected() {
        TrainingClass trainingClass = selectedClass();
        if (trainingClass == null) {
            JOptionPane.showMessageDialog(this, "Select a class first.");
            return;
        }
        int traineeId = traineeProfileService.getCurrentTrainee().getId();
        try {
            registrationController.register(trainingClass.getClassId().intValue(), traineeId);
            JOptionPane.showMessageDialog(this, "Registration completed.");
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void unregisterSelected() {
        TrainingClass trainingClass = selectedClass();
        if (trainingClass == null) {
            JOptionPane.showMessageDialog(this, "Select a class first.");
            return;
        }
        int traineeId = traineeProfileService.getCurrentTrainee().getId();
        try {
            registrationController.unregister(trainingClass.getClassId().intValue(), traineeId);
            JOptionPane.showMessageDialog(this, "Registration removed.");
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration", JOptionPane.WARNING_MESSAGE);
        }
    }

    private TrainingClass selectedClass() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visibleClasses.size()) {
            return null;
        }
        return visibleClasses.get(row);
    }

    private void style(JTextField field) {
        field.setBackground(FWTheme.CARD_BG);
        field.setForeground(FWTheme.TEXT_PRIMARY);
        field.setCaretColor(FWTheme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createLineBorder(FWTheme.BORDER, 1, true));
    }
}
