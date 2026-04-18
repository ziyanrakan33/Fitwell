package fitwell.util;

import fitwell.entity.Equipment;
import fitwell.entity.EquipmentLocation;
import fitwell.entity.EquipmentStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class XmlUtil {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String buildInventoryXml(int year, List<Equipment> items) {
        int totalQty = items.stream().mapToInt(Equipment::getQuantity).sum();
        long inService = items.stream().filter(e -> e.getStatus() == EquipmentStatus.IN_SERVICE).count();
        long flagged = items.stream().filter(Equipment::isFlagged).count();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<EquipmentInventoryReport year=\"").append(year).append("\">\n");
        sb.append("\n");
        sb.append("  <ReportMeta>\n");
        sb.append("    <GeneratedAt>").append(LocalDateTime.now().format(DT)).append("</GeneratedAt>\n");
        sb.append("    <GeneratedBy>FitWell System</GeneratedBy>\n");
        sb.append("    <System>FitWell Equipment Management</System>\n");
        sb.append("  </ReportMeta>\n");
        sb.append("\n");
        sb.append("  <Summary>\n");
        sb.append("    <TotalItems>").append(items.size()).append("</TotalItems>\n");
        sb.append("    <TotalQuantity>").append(totalQty).append("</TotalQuantity>\n");
        sb.append("    <InServiceItems>").append(inService).append("</InServiceItems>\n");
        sb.append("    <FlaggedItems>").append(flagged).append("</FlaggedItems>\n");
        sb.append("  </Summary>\n");
        sb.append("\n");
        sb.append("  <EquipmentList>\n");

        for (Equipment e : items) {
            EquipmentLocation loc = e.getLocation();
            sb.append("    <Equipment>\n");
            sb.append("      <SerialNumber>").append(escape(e.getSerialNumber())).append("</SerialNumber>\n");
            sb.append("      <Name>").append(escape(e.getName())).append("</Name>\n");
            sb.append("      <Description>").append(escape(e.getDescription())).append("</Description>\n");
            sb.append("      <Category>").append(e.getCategory()).append("</Category>\n");
            sb.append("      <Quantity>").append(e.getQuantity()).append("</Quantity>\n");
            sb.append("      <Status>").append(e.getStatus()).append("</Status>\n");
            sb.append("      <TimesUsedThisYear>").append(e.getTimesUsedThisYear()).append("</TimesUsedThisYear>\n");
            sb.append("      <Flagged>").append(e.isFlagged()).append("</Flagged>\n");
            if (e.isFlagged() && e.getFlagReason() != null && !e.getFlagReason().isBlank()) {
                sb.append("      <FlagReason>").append(escape(e.getFlagReason())).append("</FlagReason>\n");
            }
            sb.append("      <Location x=\"").append(loc == null ? 0 : loc.getX())
              .append("\" y=\"").append(loc == null ? 0 : loc.getY())
              .append("\" shelf=\"").append(loc == null ? 0 : loc.getShelfNumber())
              .append("\"/>\n");
            sb.append("    </Equipment>\n");
        }

        sb.append("  </EquipmentList>\n");
        sb.append("\n");
        sb.append("</EquipmentInventoryReport>\n");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
