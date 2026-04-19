package fitwell.controller;

import fitwell.config.AppPaths;
import fitwell.persistence.db.Db;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class JasperReportController {

    public void previewUnregisteredClasses(int year) throws Exception {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year + 1, 1, 1);
        previewUnregisteredTrainees(from, to);
    }

    public Path unregisteredClassesPdf(int year) throws Exception {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to   = LocalDate.of(year + 1, 1, 1);
        return unregisteredTraineesPdf(from, to);
    }

    public void previewUnregisteredTrainees(LocalDate from, LocalDate toExclusive) throws Exception {
        Path jrxmlPath = AppPaths.jrxmlPath();
        if (!jrxmlPath.toFile().exists()) {
            JOptionPane.showMessageDialog(null,
                    "JRXML template not found at: " + jrxmlPath,
                    "Report Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection conn = Db.getConnection();
            Map<String, Object> params = buildParams(from, toExclusive);

            Class<?> compileManagerClass = Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
            Object jasperReport = compileManagerClass
                    .getMethod("compileReport", String.class)
                    .invoke(null, jrxmlPath.toString());

            Class<?> fillManagerClass = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
            Object jasperPrint = fillManagerClass
                    .getMethod("fillReport", Class.forName("net.sf.jasperreports.engine.JasperReport"), Map.class, Connection.class)
                    .invoke(null, jasperReport, params, conn);

            Class<?> viewerClass = Class.forName("net.sf.jasperreports.view.JasperViewer");
            viewerClass.getMethod("viewReport", Class.forName("net.sf.jasperreports.engine.JasperPrint"), boolean.class)
                    .invoke(null, jasperPrint, false);

        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null,
                    "JasperReports library not found on classpath.\n"
                    + "Please ensure jasperreports JAR is in the lib folder.\n\n"
                    + "Period requested: " + from + " to " + toExclusive,
                    "Reports", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public Path unregisteredTraineesPdf(LocalDate from, LocalDate toExclusive) throws Exception {
        Path jrxmlPath = AppPaths.jrxmlPath();
        if (!jrxmlPath.toFile().exists()) {
            JOptionPane.showMessageDialog(null,
                    "JRXML template not found at: " + jrxmlPath,
                    "Report Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            Connection conn = Db.getConnection();
            Map<String, Object> params = buildParams(from, toExclusive);

            Path outputDir = AppPaths.reportsOutputDir();
            Path outputFile = outputDir.resolve("UnregisteredTrainees_" + from + "_" + toExclusive + ".pdf");

            Class<?> compileManagerClass = Class.forName("net.sf.jasperreports.engine.JasperCompileManager");
            Object jasperReport = compileManagerClass
                    .getMethod("compileReport", String.class)
                    .invoke(null, jrxmlPath.toString());

            Class<?> fillManagerClass = Class.forName("net.sf.jasperreports.engine.JasperFillManager");
            Object jasperPrint = fillManagerClass
                    .getMethod("fillReport", Class.forName("net.sf.jasperreports.engine.JasperReport"), Map.class, Connection.class)
                    .invoke(null, jasperReport, params, conn);

            Class<?> exportManagerClass = Class.forName("net.sf.jasperreports.engine.JasperExportManager");
            exportManagerClass
                    .getMethod("exportReportToPdfFile", Class.forName("net.sf.jasperreports.engine.JasperPrint"), String.class)
                    .invoke(null, jasperPrint, outputFile.toString());

            return outputFile;

        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null,
                    "JasperReports library not found on classpath.\n"
                    + "PDF export is not available.\n\n"
                    + "Period requested: " + from + " to " + toExclusive,
                    "Reports", JOptionPane.INFORMATION_MESSAGE);
            return null;
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Exception) throw new RuntimeException(cause);
            throw new RuntimeException(cause != null ? cause : ex);
        }
    }

    private Map<String, Object> buildParams(LocalDate from, LocalDate toExclusive) {
        Map<String, Object> params = new HashMap<>();
        params.put("DATE_FROM", java.sql.Date.valueOf(from));
        params.put("DATE_TO", java.sql.Date.valueOf(toExclusive));
        return params;
    }
}
