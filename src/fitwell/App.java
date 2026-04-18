package fitwell;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.Color;

import fitwell.db.DbMigration;
import fitwell.repo.ConsultantRepository;
import fitwell.repo.TraineeRepository;
import fitwell.ui.pro.AppShellFrame;

public class App {
    public static void main(String[] args) {
        setNimbus();
        initDatabase();

        SwingUtilities.invokeLater(() -> {
            AppShellFrame app = new AppShellFrame();
            app.setVisible(true);
        });
    }

    private static void initDatabase() {
        ConsultantRepository consultantRepo = new ConsultantRepository();
        TraineeRepository traineeRepo = new TraineeRepository();

        consultantRepo.ensurePasswordColumn();
        consultantRepo.ensureApprovedColumn();
        traineeRepo.ensurePasswordColumn();

        consultantRepo.ensureDefaultExists();
        traineeRepo.ensureDefaultExists();

        DbMigration.ensureTablesExist();
    }

    private static void setNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    applyDarkNimbusDefaults();
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private static void applyDarkNimbusDefaults() {
        Color bg        = new Color(15, 23, 42);
        Color cardBg    = new Color(17, 28, 51);
        Color inputBg   = new Color(25, 38, 65);
        Color sidebarBg = new Color(11, 18, 32);
        Color border    = new Color(39, 52, 78);
        Color textColor = new Color(238, 242, 255);
        Color accent    = new Color(0, 200, 151);
        Color hoverBg   = new Color(22, 36, 64);

        UIManager.put("control", bg);
        UIManager.put("nimbusBase", sidebarBg);
        UIManager.put("nimbusBlueGrey", border);
        UIManager.put("nimbusFocus", accent);
        UIManager.put("nimbusLightBackground", inputBg);
        UIManager.put("nimbusSelectionBackground", accent);
        UIManager.put("text", textColor);
        UIManager.put("info", cardBg);

        UIManager.put("Panel.background", bg);
        UIManager.put("Panel.foreground", textColor);

        UIManager.put("Table.background", cardBg);
        UIManager.put("Table.foreground", textColor);
        UIManager.put("Table.alternateRowColor", hoverBg);
        UIManager.put("TableHeader.background", sidebarBg);
        UIManager.put("TableHeader.foreground", textColor);

        UIManager.put("TextField.background", inputBg);
        UIManager.put("TextField.foreground", textColor);
        UIManager.put("FormattedTextField.background", inputBg);
        UIManager.put("FormattedTextField.foreground", textColor);
        UIManager.put("TextArea.background", inputBg);
        UIManager.put("TextArea.foreground", textColor);
        UIManager.put("TextPane.background", inputBg);
        UIManager.put("TextPane.foreground", textColor);
        UIManager.put("EditorPane.background", inputBg);
        UIManager.put("EditorPane.foreground", textColor);
        UIManager.put("PasswordField.background", inputBg);
        UIManager.put("PasswordField.foreground", textColor);

        UIManager.put("ComboBox.background", inputBg);
        UIManager.put("ComboBox.foreground", textColor);
        UIManager.put("Spinner.background", inputBg);
        UIManager.put("Spinner.foreground", textColor);

        UIManager.put("List.background", cardBg);
        UIManager.put("List.foreground", textColor);

        UIManager.put("ScrollPane.background", bg);
        UIManager.put("Viewport.background", bg);
        UIManager.put("ScrollBar.background", sidebarBg);

        UIManager.put("TitledBorder.titleColor", textColor);
        UIManager.put("Label.foreground", textColor);

        UIManager.put("CheckBox.background", bg);
        UIManager.put("CheckBox.foreground", textColor);
        UIManager.put("RadioButton.background", bg);
        UIManager.put("RadioButton.foreground", textColor);

        UIManager.put("Button.background", border);
        UIManager.put("Button.foreground", textColor);

        UIManager.put("OptionPane.background", bg);
        UIManager.put("OptionPane.messageForeground", textColor);
        UIManager.put("OptionPane.foreground", textColor);

        UIManager.put("ToolTip.background", cardBg);
        UIManager.put("ToolTip.foreground", textColor);

        UIManager.put("MenuBar.background", sidebarBg);
        UIManager.put("MenuBar.foreground", textColor);
        UIManager.put("Menu.background", sidebarBg);
        UIManager.put("Menu.foreground", textColor);
        UIManager.put("MenuItem.background", cardBg);
        UIManager.put("MenuItem.foreground", textColor);
        UIManager.put("PopupMenu.background", cardBg);
        UIManager.put("PopupMenu.foreground", textColor);

        UIManager.put("FileChooser.background", bg);
        UIManager.put("FileChooser.foreground", textColor);
    }
}