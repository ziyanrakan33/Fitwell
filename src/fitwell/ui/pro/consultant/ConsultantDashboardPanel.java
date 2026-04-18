package fitwell.ui.pro.consultant;

import fitwell.control.AuthenticationService;
import fitwell.control.EmergencyAlertService;
import fitwell.control.EquipmentImportService;
import fitwell.control.EquipmentReviewService;
import fitwell.control.FitWellServiceRegistry;
import fitwell.control.InspectionWorkflowService;
import fitwell.domain.user.Consultant;
import fitwell.domain.emergency.EmergencyAlert;
import fitwell.domain.equipment.Equipment;
import fitwell.domain.equipment.EquipmentInspection;
import fitwell.domain.equipment.InspectionSeverity;
import fitwell.domain.training.TrainingClass;
import fitwell.repo.ConsultantRepository;
import fitwell.ui.pro.AppShellFrame;
import fitwell.ui.pro.components.SidebarButton;
import fitwell.ui.pro.components.StatCard;
import fitwell.ui.pro.components.TopBarPanel;
import fitwell.ui.pro.theme.FWTheme;
import fitwell.ui.pro.theme.FWUi;
import fitwell.util.FileUtil;
import fitwell.util.JsonMvpUtil;

import fitwell.control.EquipmentManagementController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultantDashboardPanel extends JPanel {
    private final AppShellFrame shell;
    private final FitWellServiceRegistry services = FitWellServiceRegistry.getInstance();

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);

    private final SidebarButton btnDashboard = new SidebarButton("Dashboard");
    private final SidebarButton btnClasses = new SidebarButton("Classes");
    private final SidebarButton btnTrainees = new SidebarButton("Trainees");
    private final SidebarButton btnPlans = new SidebarButton("Training Plans");
    private final SidebarButton btnEquipment = new SidebarButton("Equipment");
    private final SidebarButton btnReports = new SidebarButton("Reports");
    private final SidebarButton btnAlerts = new SidebarButton("Alerts");
    private final SidebarButton btnApprovals = new SidebarButton("Approvals");
    private final SidebarButton btnProfile = new SidebarButton("Profile");

    private final StatCard statActiveClasses = new StatCard("Active Classes", "—", "service view");
    private final StatCard statEquipmentAlerts = new StatCard("Equipment Alerts", "—", "flagged items");
    private final StatCard statRegistrations = new StatCard("Registrations", "—", "reporting source");

    private final JLabel welcomeTitle = new JLabel("Welcome");

    private final DefaultListModel<String> alertsModel = new DefaultListModel<>();
    private final JList<String> alertsList = new JList<>(alertsModel);

    private final DefaultTableModel equipmentModel = new DefaultTableModel(
            new Object[]{"Serial", "Name", "Category", "Qty", "Status", "Location", "Flagged"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable equipmentTable = new JTable(equipmentModel);

    private final DefaultTableModel inspectionTableModel = new DefaultTableModel(
            new Object[]{"ID", "Class Name", "Start Time"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable inspectionTable = new JTable(inspectionTableModel);
    private final JTextArea importResultArea = new JTextArea("No SwiftFit import executed yet.");

    private final ConsultantRepository consultantRepo = new ConsultantRepository();
    private final DefaultTableModel approvalsModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Email", "Phone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable approvalsTable = new JTable(approvalsModel);

    private final EmergencyAlertService emergencyAlertService = services.emergencyAlertService();
    private final EquipmentImportService equipmentImportService = services.equipmentImportService();
    private final EquipmentReviewService equipmentReviewService = services.equipmentReviewService();
    private final InspectionWorkflowService inspectionWorkflowService = services.inspectionWorkflowService();
    private final EquipmentManagementController equipmentManagementController = services.equipmentManagementController();

    private final ConsultantProfilePanel consultantProfilePanel = new ConsultantProfilePanel();

    public ConsultantDashboardPanel(AppShellFrame shell) {
        this.shell = shell;
        setLayout(new BorderLayout());
        setBackground(FWTheme.DASH_BG);
        configureViews();

        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(true);
        main.setBackground(FWTheme.DASH_BG);
        main.add(new TopBarPanel("Consultant Portal",
                "Manage classes, equipment, reports and emergency workflows", shell::logout),
                BorderLayout.NORTH);
        main.add(buildMainContent(), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        selectTab("dashboard");
        refreshStats();
    }

    private void configureViews() {
        alertsList.setBackground(FWTheme.CARD_BG);
        alertsList.setForeground(FWTheme.TEXT_PRIMARY);
        alertsList.setBorder(new EmptyBorder(8, 8, 8, 8));
        inspectionTable.setBackground(FWTheme.CARD_BG);
        inspectionTable.setForeground(FWTheme.TEXT_PRIMARY);
        inspectionTable.setSelectionBackground(new Color(34, 50, 83));
        inspectionTable.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        inspectionTable.setRowHeight(28);

        importResultArea.setEditable(false);
        importResultArea.setBackground(FWTheme.CARD_BG);
        importResultArea.setForeground(FWTheme.TEXT_SECONDARY);
        importResultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        importResultArea.setLineWrap(true);
        importResultArea.setWrapStyleWord(true);

        equipmentTable.setBackground(FWTheme.CARD_BG);
        equipmentTable.setForeground(FWTheme.TEXT_PRIMARY);
        equipmentTable.setSelectionBackground(new Color(34, 50, 83));
        equipmentTable.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        equipmentTable.setRowHeight(28);
        equipmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && equipmentTable.getSelectedRow() != -1) {
                    String serial = (String) equipmentModel.getValueAt(equipmentTable.getSelectedRow(), 0);
                    showEquipmentDetails(serial);
                }
            }
        });
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
        fitwell.domain.user.ConsultantRole consultantRole = AuthenticationService.getInstance().getCurrentConsultantRole();
        String roleLabel = consultantRole != null ? consultantRole.name() : "Consultant";
        JLabel role = new JLabel(roleLabel);
        role.setForeground(FWTheme.TEXT_SECONDARY);
        top.add(logo);
        top.add(Box.createVerticalStrut(4));
        top.add(role);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        btnDashboard.addActionListener(e -> selectTab("dashboard"));
        btnClasses.addActionListener(e -> selectTab("classes"));
        btnTrainees.addActionListener(e -> selectTab("trainees"));
        btnPlans.addActionListener(e -> selectTab("plans"));
        btnEquipment.addActionListener(e -> selectTab("equipment"));
        btnReports.addActionListener(e -> selectTab("reports"));
        btnAlerts.addActionListener(e -> selectTab("alerts"));
        btnApprovals.addActionListener(e -> selectTab("approvals"));
        btnProfile.addActionListener(e -> selectTab("profile"));

        nav.add(btnDashboard);
        nav.add(Box.createVerticalStrut(6));
        nav.add(btnClasses);
        nav.add(Box.createVerticalStrut(6));
        nav.add(btnTrainees);
        nav.add(Box.createVerticalStrut(6));
        nav.add(btnPlans);
        nav.add(Box.createVerticalStrut(6));
        nav.add(btnEquipment);
        nav.add(Box.createVerticalStrut(6));
        nav.add(btnReports);
        nav.add(Box.createVerticalStrut(6));
        nav.add(btnAlerts);
        if (AuthenticationService.getInstance().isManager()) {
            nav.add(Box.createVerticalStrut(6));
            nav.add(btnApprovals);
        }
        nav.add(Box.createVerticalStrut(6));
        nav.add(btnProfile);

        side.add(top, BorderLayout.NORTH);
        side.add(nav, BorderLayout.CENTER);
        return side;
    }

    private JComponent buildMainContent() {
        contentPanel.setOpaque(true);
        contentPanel.setBackground(FWTheme.DASH_BG);
        contentPanel.add(buildDashboardHome(), "dashboard");
        contentPanel.add(new ConsultantClassesPanel(
                services.trainingClassService(),
                services.trainingClassQueryService(),
                services.attendanceService(),
                services.lowAttendanceReportService(),
                services.equipmentAssignmentController(),
                services.inspectionWorkflowService()), "classes");
        contentPanel.add(new ConsultantTraineesPanel(), "trainees");
        contentPanel.add(new ConsultantPlansPanel(), "plans");
        contentPanel.add(buildEquipmentPanel(), "equipment");
        contentPanel.add(new ProReportsPanel(services.inventoryReportController(),
                services.lowAttendanceReportService(), () -> selectTab("equipment")), "reports");
        contentPanel.add(buildAlertsPanel(), "alerts");
        contentPanel.add(buildApprovalsPanel(), "approvals");
        contentPanel.add(consultantProfilePanel, "profile");

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

        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setOpaque(false);

        welcomeTitle.setForeground(FWTheme.TEXT_PRIMARY);
        welcomeTitle.setFont(FWTheme.FONT_TITLE);
        topSection.add(welcomeTitle, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 12));
        stats.setOpaque(false);
        stats.add(statActiveClasses);
        stats.add(statEquipmentAlerts);
        stats.add(statRegistrations);
        
        topSection.add(stats, BorderLayout.SOUTH);

        JPanel actions = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        actions.setLayout(new GridLayout(2, 3, 12, 12));

        JButton openClasses = FWUi.primaryButton("Open Classes");
        openClasses.addActionListener(e -> selectTab("classes"));
        JButton openPlans = FWUi.primaryButton("Training Plans");
        openPlans.addActionListener(e -> selectTab("plans"));
        JButton openEquipment = FWUi.ghostDarkButton("Equipment Workflow");
        openEquipment.addActionListener(e -> selectTab("equipment"));
        JButton openReports = FWUi.ghostDarkButton("Reports");
        openReports.addActionListener(e -> selectTab("reports"));
        JButton openAlerts = FWUi.ghostDarkButton("Emergency Center");
        openAlerts.addActionListener(e -> selectTab("alerts"));

        actions.add(openClasses);
        actions.add(openPlans);
        actions.add(openEquipment);
        actions.add(openReports);
        actions.add(openAlerts);

        page.add(topSection, BorderLayout.NORTH);
        page.add(actions, BorderLayout.CENTER);
        return page;
    }

    private JComponent buildEquipmentPanel() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setOpaque(true);
        page.setBackground(FWTheme.DASH_BG);

        JPanel top = new JPanel(new GridLayout(1, 2, 12, 12));
        top.setOpaque(false);
        top.add(buildSwiftFitImportCard());
        top.add(buildInspectionCard());

        JPanel equipmentCard = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        equipmentCard.setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Equipment Review Queue");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        equipmentCard.add(title, BorderLayout.NORTH);
        equipmentCard.add(new JScrollPane(equipmentTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton addBtn = FWUi.primaryButton("Add New");
        addBtn.addActionListener(e -> addNewEquipment());
        JButton editBtn = FWUi.ghostDarkButton("Edit Selected");
        editBtn.addActionListener(e -> editSelectedEquipment());
        JButton deleteBtn = FWUi.ghostDarkButton("Delete Selected");
        deleteBtn.addActionListener(e -> deleteSelectedEquipment());
        JButton refresh = FWUi.ghostDarkButton("Reload");
        refresh.addActionListener(e -> reloadEquipment());
        JButton markOut = FWUi.primaryButton("Mark Out of Service");
        markOut.addActionListener(e -> markSelectedEquipmentOutOfService());
        JButton returnToService = FWUi.ghostDarkButton("Return to Service");
        returnToService.addActionListener(e -> returnSelectedEquipmentToService());
        JButton markReviewed = FWUi.ghostDarkButton("Mark Reviewed");
        markReviewed.addActionListener(e -> markSelectedReviewed());
        JButton reduceQty = FWUi.ghostDarkButton("Reduce Quantity");
        reduceQty.addActionListener(e -> reduceSelectedEquipmentQuantity());
        JButton remove = FWUi.ghostDarkButton("Remove Non-Usable");
        remove.addActionListener(e -> removeSelectedEquipment());
        
        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(deleteBtn);
        actions.add(refresh);
        actions.add(markOut);
        actions.add(returnToService);
        actions.add(markReviewed);
        actions.add(reduceQty);
        actions.add(remove);
        equipmentCard.add(actions, BorderLayout.SOUTH);

        page.add(top, BorderLayout.NORTH);
        page.add(equipmentCard, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildSwiftFitImportCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        card.setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("SwiftFit Monthly Import");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        JButton importBtn = FWUi.primaryButton("Import JSON");
        importBtn.addActionListener(e -> importSwiftFitJson());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(importBtn);
        card.add(title, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(importResultArea);
        sp.setPreferredSize(new Dimension(0, 140));
        card.add(sp, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildInspectionCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        card.setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Due Inspections");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        JButton inspectBtn = FWUi.primaryButton("Inspect Selected Class Equipment");
        
        inspectBtn.setEnabled(false);
        inspectionTable.getSelectionModel().addListSelectionListener(e -> {
            inspectBtn.setEnabled(inspectionTable.getSelectedRow() >= 0);
        });
        inspectBtn.addActionListener(e -> inspectSelectedDueClass());
        
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(inspectBtn);
        card.add(title, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(inspectionTable);
        sp.setPreferredSize(new Dimension(0, 140));
        card.add(sp, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildAlertsPanel() {
        JPanel page = new JPanel(new GridLayout(1, 2, 12, 12));
        page.setOpaque(true);
        page.setBackground(FWTheme.DASH_BG);

        JPanel emergencyCard = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        emergencyCard.setLayout(new BorderLayout(8, 8));
        JLabel title = new JLabel("Emergency Control Center");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);
        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setOpaque(false);
        info.setForeground(FWTheme.TEXT_SECONDARY);
        info.setFont(new Font("Monospaced", Font.PLAIN, 12));
        refreshEmergencyInfo(info);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton activate = FWUi.primaryButton("Activate Alert");
        activate.addActionListener(e -> {
            emergencyAlertService.activate("Automated emergency alert");
            addAlert("Emergency alert activated");
            refreshEmergencyInfo(info);
            refreshStats();
        });
        JButton deactivate = FWUi.ghostDarkButton("Deactivate Alert");
        deactivate.addActionListener(e -> {
            emergencyAlertService.deactivate();
            addAlert("Emergency alert deactivated. Auto-resume scheduled in 30 minutes.");
            refreshEmergencyInfo(info);
            refreshStats();
        });
        JButton resume = FWUi.ghostDarkButton("Manual Resume");
        resume.addActionListener(e -> {
            emergencyAlertService.manualResume();
            addAlert("Suspended classes were manually resumed.");
            refreshEmergencyInfo(info);
            refreshStats();
        });
        actions.add(activate);
        actions.add(deactivate);
        actions.add(resume);
        emergencyCard.add(title, BorderLayout.NORTH);
        emergencyCard.add(info, BorderLayout.CENTER);
        emergencyCard.add(actions, BorderLayout.SOUTH);

        JPanel feedCard = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        feedCard.setLayout(new BorderLayout(8, 8));
        JLabel feedTitle = new JLabel("Alerts Feed");
        feedTitle.setForeground(FWTheme.TEXT_PRIMARY);
        feedTitle.setFont(FWTheme.FONT_H2);
        feedCard.add(feedTitle, BorderLayout.NORTH);
        feedCard.add(new JScrollPane(alertsList), BorderLayout.CENTER);
        page.add(emergencyCard);
        page.add(feedCard);
        return page;
    }

    private void importSwiftFitJson() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select SwiftFit JSON Update File");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            String json = FileUtil.readTextFile(file.toPath());
            JsonMvpUtil.validateOrThrow(json);
            String normalized = JsonMvpUtil.preprocess(json);
            var batch = equipmentImportService.importMonthlyUpdate(normalized);
            importResultArea.setText("Imported batch " + batch.getBatchId() + "\nSource: " + batch.getSource()
                    + "\nUpdates: " + batch.getUpdates().size() + "\nImported at: " + LocalDateTime.now());
            addAlert("SwiftFit import completed: " + file.getName());
            reloadEquipment();
            refreshStats();
        } catch (Exception ex) {
            importResultArea.setText("Import failed: " + ex.getMessage());
            addAlert("SwiftFit import failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadEquipment() {
        equipmentModel.setRowCount(0);
        for (Equipment equipment : equipmentReviewService.getAllEquipment()) {
            String locStr = "Not set";
            if (equipment.getLocation() != null) {
                locStr = "X: " + equipment.getLocation().getX() + ", Y: " + equipment.getLocation().getY() + ", Shelf: " + equipment.getLocation().getShelfNumber();
            }
            equipmentModel.addRow(new Object[]{
                    equipment.getSerialNumber(),
                    equipment.getName(),
                    equipment.getCategory(),
                    equipment.getQuantity(),
                    equipment.getStatus(),
                    locStr,
                    equipment.isFlagged()
            });
        }
        inspectionTableModel.setRowCount(0);
        for (TrainingClass trainingClass : inspectionWorkflowService.getClassesDueForInspection()) {
            inspectionTableModel.addRow(new Object[]{
                    trainingClass.getClassId(),
                    trainingClass.getName(),
                    trainingClass.getStartTime()
            });
        }
    }

    private void showEquipmentDetails(String serial) {
        if (serial == null) return;
        Equipment eq = equipmentManagementController.getRepo().findBySerial(serial);
        if (eq == null) {
            JOptionPane.showMessageDialog(this, "Equipment details not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("<html><b>Serial Number:</b></html>")); panel.add(new JLabel(eq.getSerialNumber()));
        panel.add(new JLabel("<html><b>Name:</b></html>")); panel.add(new JLabel(eq.getName()));
        panel.add(new JLabel("<html><b>Category:</b></html>")); panel.add(new JLabel(eq.getCategory() != null ? eq.getCategory().name() : "Other"));
        panel.add(new JLabel("<html><b>Quantity:</b></html>")); panel.add(new JLabel(String.valueOf(eq.getQuantity())));
        panel.add(new JLabel("<html><b>Status:</b></html>")); panel.add(new JLabel(eq.getStatus() != null ? eq.getStatus().name() : "Unknown"));
        
        String locStr = "Not set";
        if (eq.getLocation() != null) {
            locStr = "X: " + eq.getLocation().getX() + ", Y: " + eq.getLocation().getY() + ", Shelf: " + eq.getLocation().getShelfNumber();
        }
        panel.add(new JLabel("<html><b>Location:</b></html>")); panel.add(new JLabel(locStr));
        
        panel.add(new JLabel("<html><b>Flagged:</b></html>")); panel.add(new JLabel(eq.isFlagged() ? "Yes" : "No"));
        if (eq.isFlagged()) {
            panel.add(new JLabel("<html><b>Flag Reason:</b></html>")); panel.add(new JLabel(eq.getFlagReason() == null || eq.getFlagReason().isBlank() ? "N/A" : eq.getFlagReason()));
        }
        panel.add(new JLabel("<html><b>Usages This Year:</b></html>")); panel.add(new JLabel(String.valueOf(eq.getTimesUsedThisYear())));

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("<html><b>Description:</b></html>"), BorderLayout.NORTH);
        JTextArea descArea = new JTextArea(eq.getDescription() == null || eq.getDescription().isBlank() ? "No description provided." : eq.getDescription());
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setFont(new JLabel().getFont());
        descPanel.add(descArea, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(panel, BorderLayout.NORTH);
        mainPanel.add(descPanel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, mainPanel, "Equipment Details - " + eq.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void addNewEquipment() {
        EquipmentFormDialog dialog = new EquipmentFormDialog(shell, EquipmentFormDialog.Mode.ADD);
        Equipment newEq = dialog.showDialogAndGetResult();
        if (newEq != null) {
            try {
                equipmentManagementController.createOrUpdateEquipment(newEq);
                addAlert("Added new equipment: " + newEq.getSerialNumber());
                reloadEquipment();
                refreshStats();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Adding Equipment", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedEquipment() {
        String serial = selectedEquipmentSerial();
        if (serial == null) return;
        
        Equipment currentEq = equipmentManagementController.getRepo().findBySerial(serial);
        if (currentEq == null) {
            JOptionPane.showMessageDialog(this, "Equipment not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EquipmentFormDialog dialog = new EquipmentFormDialog(shell, EquipmentFormDialog.Mode.UPDATE);
        dialog.setEquipment(currentEq);
        Equipment updatedEq = dialog.showDialogAndGetResult();

        if (updatedEq != null) {
            try {
                equipmentManagementController.updateEquipmentDetails(
                    updatedEq.getSerialNumber(),
                    updatedEq.getName(),
                    updatedEq.getDescription(),
                    updatedEq.getCategory(),
                    updatedEq.getStatus(),
                    updatedEq.getQuantity(),
                    updatedEq.getLocation().getX(),
                    updatedEq.getLocation().getY(),
                    updatedEq.getLocation().getShelfNumber()
                );
                addAlert("Updated equipment: " + updatedEq.getSerialNumber());
                reloadEquipment();
                refreshStats();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Updating Equipment", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedEquipment() {
        String serial = selectedEquipmentSerial();
        if (serial == null) return;

        int row = equipmentTable.getSelectedRow();
        String name = (String) equipmentModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to completely delete equipment \"" + name + "\" (" + serial + ")?\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                equipmentManagementController.removeEquipment(serial);
                addAlert("Deleted equipment: " + serial);
                reloadEquipment();
                refreshStats();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error Deleting Equipment", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void markSelectedEquipmentOutOfService() {
        String serial = selectedEquipmentSerial();
        if (serial == null) {
            return;
        }
        String reason = JOptionPane.showInputDialog(this, "Issue found during review:", "Safety inspection issue");
        if (reason == null) {
            return;
        }
        equipmentReviewService.markOutOfService(serial, reason);
        addAlert("Equipment " + serial + " marked out of service.");
        reloadEquipment();
        refreshStats();
    }

    private void returnSelectedEquipmentToService() {
        String serial = selectedEquipmentSerial();
        if (serial == null) {
            return;
        }
        try {
            equipmentReviewService.returnToService(serial);
            addAlert("Equipment " + serial + " returned to service.");
            reloadEquipment();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Return to Service", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void reduceSelectedEquipmentQuantity() {
        String serial = selectedEquipmentSerial();
        if (serial == null) {
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Quantity to reduce for " + serial + ":", "1");
        if (input == null || input.isBlank()) {
            return;
        }
        try {
            int amount = Integer.parseInt(input.trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0.");
                return;
            }
            Equipment eq = equipmentReviewService.findBySerial(serial);
            if (eq == null) {
                JOptionPane.showMessageDialog(this, "Equipment not found.");
                return;
            }
            eq.decreaseQuantity(amount);
            equipmentReviewService.save(eq);
            addAlert("Equipment " + serial + " quantity reduced by " + amount + ".");
            reloadEquipment();
            refreshStats();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        }
    }

    private void markSelectedReviewed() {
        String serial = selectedEquipmentSerial();
        if (serial == null) {
            return;
        }
        equipmentReviewService.markReviewed(serial);
        addAlert("Equipment " + serial + " marked as reviewed.");
        reloadEquipment();
        refreshStats();
    }

    private void removeSelectedEquipment() {
        String serial = selectedEquipmentSerial();
        if (serial == null) {
            return;
        }
        equipmentReviewService.markNonUsableAndRemove(serial);
        addAlert("Equipment " + serial + " removed from inventory.");
        reloadEquipment();
        refreshStats();
    }

    private void inspectSelectedDueClass() {
        int index = inspectionTable.getSelectedRow();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select a due class inspection first.");
            return;
        }
        int classId = (int) inspectionTableModel.getValueAt(index, 0);
        TrainingClass trainingClass = null;
        for (TrainingClass tc : inspectionWorkflowService.getClassesDueForInspection()) {
            if (tc.getClassId() == classId) {
                trainingClass = tc;
                break;
            }
        }
        if (trainingClass == null) return;
        List<Equipment> equipment = inspectionWorkflowService.findAssignedEquipment(trainingClass.getClassId());
        if (equipment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No equipment is assigned to this class.");
            return;
        }

        String[] equipmentNames = new String[equipment.size()];
        for (int i = 0; i < equipment.size(); i++) {
            equipmentNames[i] = equipment.get(i).getName() + " (" + equipment.get(i).getSerialNumber() + ")";
        }
        String selectedName = (String) JOptionPane.showInputDialog(this,
                "Select equipment to inspect:", "Equipment Inspection",
                JOptionPane.QUESTION_MESSAGE, null, equipmentNames, equipmentNames[0]);
        if (selectedName == null) return;

        int eqIndex = 0;
        for (int i = 0; i < equipmentNames.length; i++) {
            if (equipmentNames[i].equals(selectedName)) { eqIndex = i; break; }
        }
        Equipment selectedEquipment = equipment.get(eqIndex);

        InspectionSeverity severity = (InspectionSeverity) JOptionPane.showInputDialog(this,
                "Severity of the issue:\n- LOW / MEDIUM: class proceeds with limited functionality\n- HIGH: class will be suspended for rescheduling",
                "Issue Severity", JOptionPane.QUESTION_MESSAGE, null,
                InspectionSeverity.values(), InspectionSeverity.HIGH);
        if (severity == null) return;

        String reason = JOptionPane.showInputDialog(this,
                "Describe the issue for " + selectedEquipment.getName() + ":",
                "Fundamental issue identified");
        if (reason == null || reason.isBlank()) return;

        EquipmentInspection inspection = inspectionWorkflowService.inspectEquipment(
                trainingClass.getClassId(), selectedEquipment.getSerialNumber(),
                AuthenticationService.getInstance().getCurrentUserId(), severity, reason);
        String severityAction = severity.requiresReschedule() ? "Class SUSPENDED for rescheduling" : "Class proceeds with LIMITED FUNCTIONALITY";
        addAlert("Inspection: class=" + inspection.getClassId()
                + " | equipment=" + inspection.getEquipmentSerial()
                + " | severity=" + inspection.getSeverity()
                + " | " + severityAction);
        reloadEquipment();
        refreshStats();
    }

    private String selectedEquipmentSerial() {
        int row = equipmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an equipment row first.");
            return null;
        }
        Object value = equipmentModel.getValueAt(row, 0);
        return value == null ? null : value.toString();
    }

    private void refreshEmergencyInfo(JTextArea info) {
        EmergencyAlert alert = emergencyAlertService.getCurrentAlert();
        if (alert == null) {
            info.setText("No active emergency alert.\n\nUpcoming and ongoing classes will run normally.");
            return;
        }
        info.setText("Active: " + alert.isActive()
                + "\nActivated: " + alert.getActivatedAt()
                + "\nDeactivated: " + alert.getDeactivatedAt()
                + "\nAuto resume: " + alert.getAutoResumeAt()
                + "\n\nAll ongoing and upcoming classes are suspended while the alert remains active.\n"
                + "Classes can resume manually or automatically after the 30-minute cooling period.");
    }

    public void refreshStats() {
        String name = AuthenticationService.getInstance().getCurrentUserName();
        welcomeTitle.setText("Welcome, " + (name != null ? name : "Consultant"));

        emergencyAlertService.autoResumeIfDue();
        statActiveClasses.setValue(String.valueOf(services.trainingClassService().getAllClasses().size()));
        statActiveClasses.setSubText(emergencyAlertService.isEmergencyActive() ? "suspended by emergency" : "current schedule");
        statEquipmentAlerts.setValue(String.valueOf(equipmentReviewService.getFlaggedEquipment().size()));
        statEquipmentAlerts.setSubText("flagged review items");
        statRegistrations.setValue(String.valueOf(services.registrationRepository().findAll().size()));
        statRegistrations.setSubText("registration records");
        reloadEquipment();
        consultantProfilePanel.reload();
    }

    private void addAlert(String message) {
        alertsModel.add(0, LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " | " + message);
        while (alertsModel.size() > 200) {
            alertsModel.removeElementAt(alertsModel.size() - 1);
        }
    }

    private JComponent buildApprovalsPanel() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setOpaque(true);
        page.setBackground(FWTheme.DASH_BG);

        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        card.setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Pending Consultant Approvals");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        approvalsTable.setBackground(FWTheme.CARD_BG);
        approvalsTable.setForeground(FWTheme.TEXT_PRIMARY);
        approvalsTable.setSelectionBackground(new Color(34, 50, 83));
        approvalsTable.setSelectionForeground(FWTheme.TEXT_PRIMARY);
        approvalsTable.setRowHeight(28);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton approveBtn = FWUi.primaryButton("Approve");
        approveBtn.addActionListener(e -> approveSelectedConsultant());
        JButton rejectBtn = FWUi.ghostDarkButton("Reject");
        rejectBtn.addActionListener(e -> rejectSelectedConsultant());
        JButton refreshBtn = FWUi.ghostDarkButton("Refresh");
        refreshBtn.addActionListener(e -> reloadApprovals());

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(refreshBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(approvalsTable), BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        page.add(card, BorderLayout.CENTER);
        return page;
    }

    private void reloadApprovals() {
        approvalsModel.setRowCount(0);
        for (Consultant c : consultantRepo.findPendingApprovals()) {
            approvalsModel.addRow(new Object[]{
                    c.getId(),
                    c.getFirstName() + " " + c.getLastName(),
                    c.getEmail(),
                    c.getPhone()
            });
        }
    }

    private void approveSelectedConsultant() {
        int row = approvalsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a pending consultant first.");
            return;
        }
        int id = (int) approvalsModel.getValueAt(row, 0);
        String name = (String) approvalsModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve consultant \"" + name + "\"?",
                "Confirm Approval", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            consultantRepo.approve(id);
            addAlert("Consultant approved: " + name);
            reloadApprovals();
        }
    }

    private void rejectSelectedConsultant() {
        int row = approvalsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a pending consultant first.");
            return;
        }
        int id = (int) approvalsModel.getValueAt(row, 0);
        String name = (String) approvalsModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reject and remove consultant \"" + name + "\"?\nThis cannot be undone.",
                "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            consultantRepo.reject(id);
            addAlert("Consultant rejected: " + name);
            reloadApprovals();
        }
    }

    private void selectTab(String key) {
        btnDashboard.setSelectedState("dashboard".equals(key));
        btnClasses.setSelectedState("classes".equals(key));
        btnTrainees.setSelectedState("trainees".equals(key));
        btnPlans.setSelectedState("plans".equals(key));
        btnEquipment.setSelectedState("equipment".equals(key));
        btnReports.setSelectedState("reports".equals(key));
        btnAlerts.setSelectedState("alerts".equals(key));
        btnApprovals.setSelectedState("approvals".equals(key));
        btnProfile.setSelectedState("profile".equals(key));
        contentLayout.show(contentPanel, key);
        if ("dashboard".equals(key) || "equipment".equals(key) || "alerts".equals(key)) {
            refreshStats();
        }
        if ("approvals".equals(key)) {
            reloadApprovals();
        }
    }
}
