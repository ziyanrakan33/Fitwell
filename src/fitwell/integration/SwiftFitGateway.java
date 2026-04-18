package fitwell.integration;

public class SwiftFitGateway {
    public void receiveMonthlyEquipmentUpdates(String json) {
        System.out.println("[SwiftFitGateway] Received JSON payload (" + (json == null ? 0 : json.length()) + " chars)");
    }

    public void sendAnnualInventoryReportXML(String xml) {
        System.out.println("[SwiftFitGateway] Sending annual inventory report XML...");
        System.out.println(xml == null ? "" : xml);
    }
}
