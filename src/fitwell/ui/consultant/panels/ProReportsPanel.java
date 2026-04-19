package fitwell.ui.consultant.panels;

import fitwell.controller.InventoryReportController;
import fitwell.service.attendance.LowAttendanceReportService;
import fitwell.ui.components.ProTextBlocks;
import fitwell.ui.theme.FWTheme;
import fitwell.ui.theme.FWUi;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings({"serial","this-escape"})
public class ProReportsPanel extends JPanel {

    public interface NavigationHandler {
        void openEquipmentTab();
    }

    private final InventoryReportController inventoryReportController;
    private final LowAttendanceReportService lowAttendanceReportService;
    private final NavigationHandler navigationHandler;

    public ProReportsPanel(InventoryReportController inventoryReportController,
                           LowAttendanceReportService lowAttendanceReportService,
                           NavigationHandler navigationHandler) {
        this.inventoryReportController = inventoryReportController;
        this.lowAttendanceReportService = lowAttendanceReportService;
        this.navigationHandler = navigationHandler;

        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        setBackground(FWTheme.DASH_BG);
        setOpaque(true);

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 12));
        grid.setOpaque(false);

        grid.add(buildUnregisteredReportCard());
        grid.add(buildEquipmentReportCard());

        add(grid, BorderLayout.NORTH);
    }

    private JPanel buildUnregisteredReportCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Unregistered Class Report");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JComponent desc = ProTextBlocks.description(
                "Generate trainees who attended fewer than N classes in a selected period."
        );

        JButton openBtn = FWUi.primaryButton("Open Report");
        openBtn.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            new UnregisteredClassReportProDialog(owner, lowAttendanceReportService).setVisible(true);
        });

        JButton pdfBtn = FWUi.primaryButton("Generate PDF");
        pdfBtn.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            new JasperPdfReportProDialog(owner).setVisible(true);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(openBtn);
        actions.add(pdfBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildEquipmentReportCard() {
        JPanel card = FWUi.cardPanel(FWTheme.CARD_BG, 18);
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Equipment Inventory Report");
        title.setForeground(FWTheme.TEXT_PRIMARY);
        title.setFont(FWTheme.FONT_H2);

        JComponent desc = ProTextBlocks.description(
                "Overview of equipment inventory + XML export preview for SwiftFit integration."
        );

        JButton openEquipmentBtn = FWUi.ghostDarkButton("Open Equipment");
        openEquipmentBtn.addActionListener(e -> {
            if (navigationHandler != null) navigationHandler.openEquipmentTab();
        });

        JButton openXmlBtn = FWUi.primaryButton("Open XML Report");
        openXmlBtn.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            new EquipmentInventoryReportProDialog(owner, inventoryReportController).setVisible(true);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(openEquipmentBtn);
        actions.add(openXmlBtn);

        card.add(title, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }
}