package org.practice.grp.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import static org.practice.grp.zephyrJira.JiraZephyrTestCaseManagement.updateExecutionResults;

public class PostRunUtility {

    public FileInputStream FIS = null;
    public static Properties Config = null;
    public static Logger logman;

    public PostRunUtility() {
        try {
            System.setProperty("ScenarioName", "PostRunLog");
            System.setProperty("ScenarioID", "PostRunId");
            logman = LogManager.getInstance();
            FIS = new FileInputStream(
                    System.getProperty("user.dir") + "/src/test/java/com/boi/grp/globalconfig/GlobalConfig.properties");
            Config = new Properties();
            Config.load(FIS);
            failSafePropertyGeneration();
            generateReportProperties();
        } catch (Throwable e) {
            logman.error("Unable to create PostRun Log , error = " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Throwable {
        PostRunUtility postRunUtility = new PostRunUtility();
        // System.setProperty("mavenLocalRepo","C:\\maven\\repository\\org\\aspectj\\aspectjweaver\\1.8.10\\aspectjweaver-1.8.10.jar");
        System.out.println("path = " + System.getProperty("mavenLocalRepo"));
        ReportUtils reportUtils = new ReportUtils();
        try {
            postRunUtility.logMessage("Report Type = " + System.getProperty("REPORT_TYPE"));
            switch (System.getProperty("REPORT_TYPE").toUpperCase()) {
                case "ALLURE":
                    reportUtils.generateAllureReport(System.getProperty("mavenLocalRepo"));
                    break;
                case "CONSOLIDATEDREPORT":
                    reportUtils.generateConsolidatedReport();
                    break;
                case "DEFAULT":
                    reportUtils.generateCucumberDefaultReport();
                    break;
                case "ALL":
                    reportUtils.generateCucumberDefaultReport();
                    reportUtils.generateConsolidatedReport();
                    reportUtils.generateAllureReport(System.getProperty("mavenLocalRepo"));
                    break;
                default:
                    reportUtils.generateAllureReport(System.getProperty("mavenLocalRepo"));
                    break;
            }

            postRunUtility.copyTargetFolderToReportBackupFolder();
            updateExecutionResults();
        } catch (Throwable e) {
            postRunUtility.logError("Error in generating Report, for Report type = " + e.getMessage());
        }
    }

    public static void failSafePropertyGeneration() {
        try {
            for (Object prop : Config.keySet()) {
                if (System.getenv(prop.toString()) != null) {
                    System.setProperty(prop.toString().trim().toUpperCase(), System.getenv(prop.toString()));
                } else {
                    System.setProperty(prop.toString().trim().toUpperCase(), Config.getProperty(prop.toString()));
                }
            }
            // for adding report environment section
            Properties properties = new Properties();
            properties.load(new FileReader(System.getProperty("user.dir") + "/src/test/resources/report.properties"));
            for (Object prop : properties.keySet()) {
                if (System.getenv(prop.toString()) != null) {
                    System.setProperty(prop.toString().trim().toUpperCase(), System.getenv(prop.toString()));
                } else {
                    System.setProperty(prop.toString().trim().toUpperCase(), properties.getProperty(prop.toString()));
                }

            }
        } catch (Exception e) {
            logman.error("Error Occurred Inside failSafePropertyGenenration block in PostRun, Error Description="
                    + e.getMessage());
        }
    }

    public void logMessage(String message) {
        logman.info(message);
        System.out.println(message);
    }

    public void logError(String message) {
        logman.error(message);
        System.out.println(message);
    }

    public void copyTargetFolderToReportBackupFolder() {
        try {
            if ((!System.getProperty("RESULT_BACKUP").isEmpty())
                    && (!System.getProperty("RESULT_BACKUP").equalsIgnoreCase("NA"))) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String time = dateFormat.format(new Date());
                String appendedData = "Target_folder_generated_at_" + time.replaceAll(":", "_");
                File dir = new File(System.getProperty("RESULT_BACKUP") + "/" + appendedData);
                boolean fileFolder = dir.mkdir();
                if (fileFolder) {
                    FileUtils.copyDirectory(new File(System.getProperty("user.dir") + "/target"), dir);
                }
                logMessage("Successfully copied target folder to backup folder at location = "
                        + System.getProperty("RESULT_BACKUP") + "/" + appendedData);
            }

        } catch (IOException e) {
            logError("Error in copyTargetFolderToReportFolder method, Error = " + e.getMessage());
        }
    }

    private void generateReportProperties() {
        Map<String, String> htmlReportProperties = new HashMap<String, String>();
        htmlReportProperties.put("Report Date", DateFormat.getInstance().format(new Date()));
        htmlReportProperties.put("Project Name",
                System.getProperty("PROJECT.NAME", System.getProperty("PROJECTNAME", "Not Available")));
        htmlReportProperties.put("Execution Mode", System.getProperty("TYPE", "Not Available"));
        htmlReportProperties.put("Build Number", System.getProperty("BUILD", "Not Available"));
        htmlReportProperties.put("Environment", System.getProperty("ENVIRONMENT", "Not Available"));

        switch (System.getProperty("TYPE", "").trim().toUpperCase()) {
            case "DATABASE":
                htmlReportProperties.put("Database Instance", System.getProperty("DB.HOST").toUpperCase());
                break;
            case "API":
                htmlReportProperties.put("SSL Configured", System.getProperty("SSL_CONFIGURED", "NA").toUpperCase());
                htmlReportProperties.put("SSL Handshake Mode",
                        System.getProperty("SSL_HANDSHAKE_KEYSTORE_TYPE", "NA").toUpperCase());
                break;
            default:
                htmlReportProperties.put("URL", System.getProperty("DESKTOP_URL", "Not Applicable"));
                htmlReportProperties.put("Mobile URL", System.getProperty("MOBILE_URL", "Not Applicable"));
                htmlReportProperties.put("BrowserStack Host", System.getProperty("BROWSERSTACKHOST", "Not Applicable"));
                htmlReportProperties.put("Execution Browser",
                        System.getProperty("PREFERRED_BROWSER", "Not Applicable").toUpperCase());
                break;
        }

        Properties prop = new Properties();
        for (Entry<String, String> reportEntry : htmlReportProperties.entrySet()) {
            prop.setProperty(reportEntry.getKey().trim(), reportEntry.getValue().trim());
        }

        try {
            FileOutputStream fos = new FileOutputStream(
                    System.getProperty("user.dir") + "/target/html-report.properties");
            prop.store(fos, null);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}