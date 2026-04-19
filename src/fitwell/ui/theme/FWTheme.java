package fitwell.ui.theme;

import java.awt.*;

public final class FWTheme {
    private FWTheme() {}

    // ===== Entry (Dark, matching Dashboard) =====
    public static final Color ENTRY_BG = new Color(15, 23, 42);
    public static final Color ENTRY_CARD = new Color(17, 28, 51);
    public static final Color ENTRY_TEXT = new Color(238, 242, 255);
    public static final Color ENTRY_SUBTEXT = new Color(160, 174, 197);

    // ===== Dashboard (Dark Pro) =====
    public static final Color DASH_BG = new Color(15, 23, 42);       // #0F172A
    public static final Color SIDEBAR_BG = new Color(11, 18, 32);    // #0B1220
    public static final Color CARD_BG = new Color(17, 28, 51);       // #111C33
    public static final Color CARD_BG_HOVER = new Color(22, 36, 64);
    public static final Color TEXT_PRIMARY = new Color(238, 242, 255);
    public static final Color TEXT_SECONDARY = new Color(160, 174, 197);
    public static final Color ACCENT = new Color(0, 200, 151);       // #00C897
    public static final Color ACCENT_DARK = new Color(0, 160, 121);
    public static final Color BORDER = new Color(39, 52, 78);
    public static final Color INPUT_BG = new Color(25, 38, 65);
    public static final Color SUCCESS = new Color(0, 200, 151);
    public static final Color DANGER = new Color(239, 68, 68);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_H2 = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 14);
}