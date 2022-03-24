package org.practice.grp.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PreRunUtility {

    public static FileInputStream FIS = null;
    public static Properties Config = null;
    public static Logger logMan;
    public static List<String> listScenariosTargettedForExecution = new ArrayList<String>();

    public static void main(String[] args) {
        try {
            System.setProperty("ScenarioName", "PreRunLog");
            System.setProperty("ScenarioID", "PreRunId");
            logMan = LogManagerPreRun.getInstance();
            FIS = new FileInputStream(
                    System.getProperty("user.dir") + "/src/test/java/org/practice/grp/globalConfig/GlobalConfig.properties");
            Config = new Properties();
            Config.load(FIS);
            failSafePropertyGeneration();
        } catch (Throwable e) {
            logMan.error("Unable to create PreRun Log , error = " + e.getMessage());
        }

        handleTestDataFromExcelFile();
        handleRunConfigurationUsingExcel();
        invokeWireMockServer();
        removeReportPropertiesData();
    }

    public static void failSafePropertyGeneration() {
        try {
            for (Object prop : Config.keySet()) {
                if (System.getenv(prop.toString()) != null) {
                    logMan.info(prop + " details available in System env. Value: " + System.getenv(prop.toString()));
                    System.setProperty(prop.toString().trim().toUpperCase(), System.getenv(prop.toString()));
                } else {
                    System.setProperty(prop.toString().trim().toUpperCase(), Config.getProperty(prop.toString()));
                }
            }
        } catch (Exception e) {
            logMan.error("Error Occurred Inside failSafePropertyGenenration block in PreRun, Error Description="
                    + e.getMessage());
        }
    }

    public static void removeReportPropertiesData() {
        FileOutputStream outputStream = null;
        try {
            Properties prop = new Properties();
            prop.load(new FileReader(System.getProperty("user.dir") + "/src/test/resources/report.properties"));
            prop.clear();
            outputStream = new FileOutputStream(
                    System.getProperty("user.dir") + "/src/test/resources/report.properties");
            prop.store(outputStream, null);
        } catch (IOException e) {
            logMan.error("Error in removeReportPropertiesData , Error = " + e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logMan.error("Error in closing output steam while removing data from properties file, Error =  "
                        + e.getMessage());
            }
        }
    }

    public static void invokeWireMockServer() {
        try {
            if (System.getProperty("TYPE").equalsIgnoreCase("API")) {
                if (System.getProperty("ISMOCKINGENABLED") == null) {
                    System.setProperty("ISMOCKINGENABLED", "FALSE");
                }
                if (System.getProperty("ISMOCKINGENABLED").equalsIgnoreCase("TRUE")) {
                    logMan.info("wiremock process");
                    String path = System.getProperty("user.dir")
                            + "/src/test/resources/wiremock/wiremock-jre8-standalone-2.27.2.jar";
                    int port = Integer.valueOf(System.getProperty("URI").split(":")[2]);
                    try {
                        Runtime.getRuntime().exec("java.exe -jar " + path + " --port=" + port);
                        logMan.info("Wiremock up");
                    } catch (IOException e) {
                        logMan.info(
                                "There are some errors during setting up wiremock server, message =" + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logMan.info("There are some error in invokeWireMockServer, Message =  " + e.getMessage());
        }
    }

    public static void handleRunConfigurationUsingExcel() {
        if (System.getProperty("EXCEL_CONFIGURATION").equalsIgnoreCase("TRUE")) {
            String excelConfigPath;
            excelConfigPath = System.getProperty("EXCEL_CONFIGURATION_FILEPATH");
            if (excelConfigPath == null) {
                excelConfigPath = "NA";
            }
            getSerializedConfigDataUsingExcel(excelConfigPath);
            getSerializedPlatformDetailsData(excelConfigPath);
        }
    }

    public static void handleTestDataFromExcelFile() {
        try {
            String testDataPath;
            testDataPath = System.getProperty("TEST_DATA_FILEPATH");
            if (testDataPath == null) {
                testDataPath = "NA";
            }
            additionOfDataInFeatureFile(testDataPath);
        } catch (Exception e) {
            logMan.info("Error in handleTestDataFromExcelFile, Error = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void getSerializedConfigDataUsingExcel(String excelConfigPath) {
        try {
            FileInputStream fis = new FileInputStream(System.getProperty("user.dir")
                    + "/src/test/resources/RunConfiguration_dependencies/RunConfig.properties");
            Properties properties = new Properties();
            properties.load(fis);

            try {
                for (Object prop : properties.keySet()) {
                    if (System.getenv(prop.toString()) != null) {
                        System.setProperty(prop.toString().trim().toUpperCase(), System.getenv(prop.toString()));
                    } else {
                        System.setProperty(prop.toString().trim().toUpperCase(),
                                properties.getProperty(prop.toString()));
                    }
                }
            } catch (Exception e) {
                logMan.error(
                        "Error Occurred Inside getSerializedConfigDataForMobileCenterParallelRun, Error Description="
                                + e.getMessage());
            }

            File file = new File(excelConfigPath);
            ExcelUtils excelUtils;
            if (file.exists()) {
                if (excelConfigPath.equalsIgnoreCase("resources")) {
                    excelConfigPath = System.getProperty("user.dir")
                            + "/src/test/resources/RunConfiguration_dependencies/"
                            + System.getProperty("CONFIGURATION_WORKBOOK");
                    excelUtils = new ExcelUtils(excelConfigPath);
                } else {
                    excelUtils = new ExcelUtils(excelConfigPath);
                }
            } else {
                excelConfigPath = System.getProperty("user.dir") + "/src/test/resources/RunConfiguration_dependencies/"
                        + System.getProperty("CONFIGURATION_WORKBOOK");
                excelUtils = new ExcelUtils(excelConfigPath);
            }
            List<Map<String, String>> runConfigData = excelUtils.getDataInFormOfListOfMapByLinkingTwoSheets(
                    System.getProperty("PLATFORMDETAILSTAB"), System.getProperty("CONFIGURATIONTAB"),
                    System.getProperty("LINKEDCOLUMN"));

            try {
                FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/target/listData");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(runConfigData);
                oos.close();
                fos.close();
                logMan.info("Serialisation complete for configuration using excel file");
            } catch (IOException ioe) {
                logMan.error("Error in serialization , Error = " + ioe.getMessage());
            }
        } catch (Exception e) {
            logMan.error("Error in getSerializedConfigDataUsingExcel Method, Message = " + e.getMessage(), e);
        }
    }

    public static void additionOfDataInFeatureFile(String testDataPath) {

        try {
            FileInputStream fis = new FileInputStream(
                    System.getProperty("user.dir") + "/src/test/resources/testData/TestDataConfig.properties");
            Properties properties = new Properties();
            properties.load(fis);

            try {
                for (Object prop : properties.keySet()) {
                    if (System.getenv(prop.toString()) != null) {
                        System.setProperty(prop.toString().trim().toUpperCase(), System.getenv(prop.toString()));
                    } else {
                        System.setProperty(prop.toString().trim().toUpperCase(),
                                properties.getProperty(prop.toString()));
                    }
                }
            } catch (Exception e) {
                logMan.error("Error Occurred Inside getSerializedTestData, Error Description=" + e.getMessage());
            }
            // new test code for multiple excel file
            if (testDataPath.equalsIgnoreCase("resources") || testDataPath.equalsIgnoreCase("NA")
                    || testDataPath.isEmpty()) {
                testDataPath = System.getProperty("user.dir") + "/src/test/resources/testData";
            }

            listScenariosTargettedForExecution = new FeatureGenerator(testDataPath).generateFeatues(
                    System.getProperty("INITIAL_FEATURE_FOLDER"), System.getProperty("FINAL_FEATURE_FOLDER"));
            if (System.getProperty("TYPE").equalsIgnoreCase("API")) {
                Map<String, Map<String, String>> masterDataForJsonPayload = new HashMap<String, Map<String, String>>();
                serializationOfMappedDataForJsonPayLoadGeneration(masterDataForJsonPayload);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logMan.error("Error in additionOfDataInFeatureFile Method, Message = " + e.getMessage());
            logMan.info("Error in additionOfDataInFeatureFile Method, Message = " + e.getMessage());
        }
    }

    public static void serializationOfMappedDataForJsonPayLoadGeneration(
            Map<String, Map<String, String>> dataForJsonPayload) {
        try {
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/target/jsonPayLoadData");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataForJsonPayload);
            oos.close();
            fos.close();
            logMan.info("Serialisation complete for JsonPayLoad using excel file");
        } catch (IOException ioe) {
            logMan.error("Error in serialization , Error = " + ioe.getMessage());
        }
    }

    public static void getSerializedPlatformDetailsData(String excelConfigPath) {
        try {
            FileInputStream fis = new FileInputStream(System.getProperty("user.dir")
                    + "/src/test/resources/RunConfiguration_dependencies/RunConfig.properties");
            Properties properties = new Properties();
            properties.load(fis);

            try {
                for (Object prop : properties.keySet()) {
                    if (System.getenv(prop.toString()) != null) {
                        System.setProperty(prop.toString().trim().toUpperCase(), System.getenv(prop.toString()));
                    } else {
                        System.setProperty(prop.toString().trim().toUpperCase(),
                                properties.getProperty(prop.toString()));
                    }
                }
            } catch (Exception e) {
                logMan.error(
                        "Error Occurred Inside getSerializedConfigDataForMobileCenterParallelRun, Error Description="
                                + e.getMessage());
            }

            File file = new File(excelConfigPath);
            ExcelUtils excelUtils;
            if (file.exists()) {
                if (excelConfigPath.equalsIgnoreCase("resources")) {
                    excelConfigPath = System.getProperty("user.dir")
                            + "/src/test/resources/RunConfiguration_dependencies/"
                            + System.getProperty("CONFIGURATION_WORKBOOK");
                    excelUtils = new ExcelUtils(excelConfigPath);
                } else {
                    excelUtils = new ExcelUtils(excelConfigPath);
                }
            } else {
                excelConfigPath = System.getProperty("user.dir") + "/src/test/resources/RunConfiguration_dependencies/"
                        + System.getProperty("CONFIGURATION_WORKBOOK");
                excelUtils = new ExcelUtils(excelConfigPath);
            }
            // List<Map<String, String>> runConfigData =
            // excelUtils.getDataInFormOfListOfMapByLinkingTwoSheets(System.getProperty("PLATFORMDETAILSTAB"),System.getProperty("CONFIGURATIONTAB"),System.getProperty("LINKEDCOLUMN"));
            Map<String, Map<String, String>> platformDetailsData = excelUtils.getDataInFormOfMapofMap(
                    System.getProperty("CONFIGURATIONTAB"), System.getProperty("LINKEDCOLUMN"));

            try {
                FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/target/platformData");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(platformDetailsData);
                oos.close();
                fos.close();
            } catch (IOException ioe) {
                logMan.error("Error in getSerializedPlatformDetailsData, Error = " + ioe.getMessage());
            }
        } catch (Exception e) {
            logMan.error("Error in getSerializedPlatformDetailsData Method, Message = " + e.getMessage(), e);
            logMan.info("Error in getSerializedPlatformDetailsData Method, Message = " + e.getMessage());
        }
    }
}