package org.practice.grp.hooks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cucumber.api.Result;
import io.cucumber.core.event.Status;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;


import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.practice.grp.base.BasePage;
import org.practice.grp.base.LaunchService;
import org.practice.grp.dataBase.DataBaseUtil;
import org.practice.grp.driverManager.DriverManager;
import org.practice.grp.utilities.LogManager;
import org.practice.grp.zephyrJira.JiraEntity;
import org.practice.grp.zephyrJira.JiraZephyrTestCaseManagement;

import static org.practice.grp.zephyrJira.JiraZephyrTestCaseManagement.prepareExecutionResults;
import static org.practice.grp.zephyrJira.JiraZephyrTestCaseManagement.readJiraCycleMap;

public class Hooks {
    public DataBaseUtil dbUtil;
    public WebDriver driver;
    public WebDriver engagementBuilderDriver;
    public WebDriverWait wait;
    public Logger logman;
    public static Scenario testScenario;
    public static final String GLOBAL_CONFIG_PROPERTY = "src/test/java/org/practice/grp/globalConfig/GlobalConfig.properties";
    private static final String ERROR_PROPERTY = "src/test/resources/test_props/{ENV}/errors.properties";
    private static final String END_POINT_PROPERTY = "src/test/resources/test_props/{ENV}/apiEndpoints.properties";
    public Properties globalConfig;
    public static SoftAssertions softAssertions;
    public FileInputStream FIS = null;
    public Map<String, Map<String, String>> deserializeData;

    @Before
    public void init(Scenario scenario) {
        testScenario = scenario;
        System.setProperty("ScenarioName", scenario.getName());
        System.setProperty("ScenarioID", parseScenarioId(scenario.getId()));
        System.setProperty("PropFilePath", GLOBAL_CONFIG_PROPERTY);
        softAssertions = new SoftAssertions();
        LogManager.resetLogger();
        logman = LogManager.getInstance();
        logman.debug("Scenario Name is =" + System.getProperty("ScenarioName"));
        loadGlobalConfig();
        failSafePropertyGenenration();
        setDataInSystemPropertiesForRunConfigurationUsingExcel();
        writePlatformAndTypeDetailsForReport();
        setJiraEntity();
        readJiraCycleMap();
        if (System.getProperty("TYPE", "").equalsIgnoreCase("DATABASE")) {
            dbUtil = new DataBaseUtil();
        } else if (System.getProperty("TYPE", "").equalsIgnoreCase("API")) {
        } else {
            setObjectRepoInSystemVariables();
            driver = new DriverManager().GetDriver(System.getProperty("PLATFORM"), System.getProperty("TYPE"));
            invoke();
        }

        if (System.getProperty("ENGAGEMENT_BUILDER_REQUIRED", "").equals("TRUE")) {
            engagementBuilderDriver = new DriverManager()
                    .GetDriverForBrowsers(System.getProperty("ENGAGEMENT_BUILDER_BROWSER"));
        }
    }

    @After
    public void tearDown() throws Error {
        try {
            prepareExecutionResults(testScenario.getName(), "", !testScenario.getStatus().equals(Status.PASSED),logError(testScenario));
            if (!(driver.toString().contains("(null)"))) {
                driver.quit();
            }
            if(System.getProperty("PLATFORM").equalsIgnoreCase("BROWSERSTACK")
                    && System.getProperty("TYPE").equalsIgnoreCase("APPLICATION")){
                //HIDHelper helper = new HIDHelper();
                //helper.deleteDeviceByUser(System.getProperty("HID_Device_UserId"));
            }
            handleSoftAssertions();
        } catch (Throwable e) {
            logman.warn("Error in teardown, message =  " + e.getMessage());
            //throw new Error("There are Soft Assertion Failures");
        }
    }

    public void handleSoftAssertions() {
        List<Throwable> list = softAssertions.errorsCollected();
        StringBuilder builder = null;
        if (list.size() > 0) {
            builder = new StringBuilder();
            for (Throwable errorValue : list) {
                builder.append("There are Soft Assertion Failures : \n ").append(errorValue.getMessage()).append("\n");
            }
            new BasePage().insertErrorMessageToHtmlReport(builder.toString());
        } else {
            new BasePage().insertMessageToHtmlReport("There are no SoftAssertions Failures");
        }
        try {
            softAssertions.assertAll();
        } catch (Throwable e) {
            logman.warn("There are " + builder.toString());
            throw new Error("There are Soft Assertion Failures");
        }
    }

    public String parseScenarioId(String scenarioId) {
        try {
            String[] arr = scenarioId.split("/");
            return arr[arr.length - 1].replaceAll(":", "_");
        } catch (Throwable e) {
            logman.error("Error in parsing please check parseScenarioId method, error = " + e.getMessage());
            return "";
        }
    }

    public void loadGlobalConfig() {
        try {
            FIS = new FileInputStream(GLOBAL_CONFIG_PROPERTY);
            globalConfig = new Properties();
            FileInputStream globalProps = new FileInputStream(GLOBAL_CONFIG_PROPERTY);
            globalConfig.load(globalProps);
            globalProps.close();

            if (globalConfig.getProperty("TYPE").equalsIgnoreCase("API")) {
                failSafePropertyGenenration();
                FileInputStream endpointProps = new FileInputStream(
                        END_POINT_PROPERTY.replace("{ENV}", globalConfig.getProperty("ENVIRONMENT")));
                globalConfig.load(endpointProps);
                endpointProps.close();

                FileInputStream errorProps = new FileInputStream(
                        ERROR_PROPERTY.replace("{ENV}", globalConfig.getProperty("ENVIRONMENT")));
                globalConfig.load(errorProps);
                errorProps.close();
            }
            logman.debug("property files are loaded successfully");
        } catch (Exception e) {
            logman.error("Error Occurred Inside init block in Hooks, Error Description=" + e.getMessage());
        }
    }

    public void failSafePropertyGenenration() {
        try {
            for (Object prop : globalConfig.keySet()) {
                if (System.getenv(prop.toString()) != null) {
                    System.setProperty(prop.toString().trim().toUpperCase(), System.getenv(prop.toString()));
                    logman.info(prop.toString().trim().toUpperCase() + " property is set from environment to "
                            + System.getenv(prop.toString()));
                } else {
                    System.setProperty(prop.toString().trim().toUpperCase(), globalConfig.getProperty(prop.toString()));
                    logman.info(prop.toString().trim().toUpperCase() + " property is set from environment to "
                            + globalConfig.getProperty(prop.toString()));
                }
            }
        } catch (Exception e) {
            logman.error("Error Occurred Inside failSafePropertyGenenration block in Hooks, Error Description="
                    + e.getMessage());
        }
    }

    public WebDriver GetDriver() {
        return this.driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void setDataInSystemPropertiesForRunConfigurationUsingExcel() {
        try {
            if (System.getProperty("EXCEL_CONFIGURATION").equalsIgnoreCase("TRUE")) {
                List<Map<String, String>> data = getDeserializeDataForExcelConfiguration();
                for (Map<String, String> map : data) {
                    if (System.getProperty("ScenarioName").equalsIgnoreCase(map.get("ScenarioName"))) {
                        map.remove("ScenarioName");
                        map.remove("DEVICECONFIGURATION");
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if (!entry.getValue().isEmpty()) {
                                System.setProperty(entry.getKey(), entry.getValue());
                                logman.info(entry.getKey().trim().toUpperCase()
                                        + " property is set from environment to " + entry.getValue());
                            }
                        }
                    }
                }
                logman.info("Added System variables for all the deserialize data");
            }
        } catch (Exception e) {
            logman.error("Error in setDataInSystemPropertiesForMobileCenterParallelConfiguration mrthod, Error = "
                    + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getDeserializeDataForExcelConfiguration() {
        List<Map<String, String>> excelConfiguration = new ArrayList<Map<String, String>>();

        try {
            FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/target/listData");
            ObjectInputStream ois = new ObjectInputStream(fis);
            excelConfiguration = (List<Map<String, String>>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            logman.error("Error in getDeserializeDataForMCConfiguration, Error = " + e.getMessage());
            return excelConfiguration;
        } catch (ClassNotFoundException e) {
            logman.error("Class not found, Error = " + e.getMessage());
            return excelConfiguration;
        }
        logman.info("Deserialization of excel data is complete");
        return excelConfiguration;
    }

    public void writePlatformAndTypeDetailsForReport() {
        try {
            Properties prop = new Properties();
            prop.load(new FileReader(System.getProperty("user.dir") + "/src/test/resources/report.properties"));
            // two keys REPORT_PLATFORM,REPORT_PLATFORM_TYPE,DEVICENAME
            if (prop.getProperty("REPORT_PLATFORM") == null) {
                setKeyInProperties();
            } else {
                boolean deviceNameFlag = false;
                String existingDeviceName = null;
                String existingPlatform = prop.getProperty("REPORT_PLATFORM");
                String existingType = prop.getProperty("REPORT_PLATFORM_TYPE");
                if (prop.getProperty("REPORT_DEVICE_NAME") != null) {
                    existingDeviceName = prop.getProperty("REPORT_DEVICE_NAME");
                    deviceNameFlag = true;
                }
                if (!existingPlatform.contains(System.getProperty("PLATFORM"))) {
                    amendKeysInExistingPropertiesFile("REPORT_PLATFORM",
                            existingPlatform + "," + System.getProperty("PLATFORM"));
                }

                if (!existingType.contains(System.getProperty("TYPE"))) {
                    amendKeysInExistingPropertiesFile("REPORT_PLATFORM_TYPE",
                            existingType + "," + System.getProperty("TYPE"));
                }

                if (deviceNameFlag) {
                    if (!existingDeviceName.contains(System.getProperty("DEVICENAME"))) {
                        amendKeysInExistingPropertiesFile("REPORT_DEVICE_NAME",
                                existingDeviceName + "," + System.getProperty("DEVICENAME"));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setObjectRepoInSystemVariables() {
        try {
            File file = new File(System.getProperty("user.dir") + "/src/test/resources/repository");
            Properties properties = new Properties();
            File[] fileList = file.listFiles();
            String eachFile;
            assert fileList != null;
            for (File fileTemp : fileList) {
                eachFile = fileTemp.getAbsolutePath();
                FileInputStream fis = new FileInputStream(eachFile);
                properties.load(fis);
                Set<Object> setOfKeys = properties.keySet();
                for (Object key : setOfKeys) {
                    if (System.getenv(key.toString()) != null) {
                        System.setProperty(key.toString().trim(), System.getenv(key.toString()));
                    } else {
                        System.setProperty(key.toString().trim(), properties.getProperty(key.toString()));
                    }
                    // System.setProperty(key.toString().trim(),
                    // properties.getProperty(key.toString()));
                }
            }
        } catch (Throwable e) {
            logman.error("Error in storing repository values in System properties, Error = " + e.getMessage());
        }
    }

    public void setKeyInProperties() {
        OutputStream outputStream = null;
        try {
            Properties properties = new Properties();
            outputStream = new FileOutputStream(
                    System.getProperty("user.dir") + "/src/test/resources/report.properties");
            properties.setProperty("REPORT_PLATFORM", System.getProperty("PLATFORM"));
            properties.setProperty("REPORT_PLATFORM_TYPE", System.getProperty("TYPE"));
            if (System.getProperty("DEVICENAME") != null) {
                properties.setProperty("REPORT_DEVICE_NAME", System.getProperty("DEVICENAME"));
            }
            properties.store(outputStream, "");
        } catch (Exception e) {
            logman.error("Error in setKeyInProperties method, Error = " + e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logman.error(
                        "Error in closing output steam while appending data in globalConfig properties file, Error =  "
                                + e.getMessage());
            }
        }
    }

    public void amendKeysInExistingPropertiesFile(String key, String data) {
        OutputStream outputStream = null;
        FileInputStream fileIn;
        try {
            Properties properties = new Properties();
            File file = new File(System.getProperty("user.dir") + "/src/test/resources/report.properties");
            fileIn = new FileInputStream(file);
            properties.load(fileIn);
            outputStream = new FileOutputStream(
                    System.getProperty("user.dir") + "/src/test/resources/report.properties");
            properties.setProperty(key, data);
            properties.store(outputStream, "");
        } catch (Exception e) {
            logman.error("Error in amendKeysInExistingPropertiesFile method, Error = " + e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logman.error(
                        "Error in closing output steam while appending data in globalConfig properties file, Error =  "
                                + e.getMessage());
            }
        }
    }

    private static void setJiraEntity(){
        JiraEntity.setJiraUrl(System.getProperty("JIRA_URL"));
        JiraEntity.setJiraUpdate(System.getProperty("JIRA.UPDATE"));
        JiraEntity.setJiraUsername(System.getProperty("JIRA_USERNAME"));
        JiraEntity.setJiraPassword(System.getProperty("JIRA_PASSWORD"));
        JiraEntity.setJiraInputFormat(System.getProperty("JIRA.INPUT_FORMAT"));
        JiraEntity.setJiraInputLocation(System.getProperty("JIRA.INPUT_LOCATION"));
        JiraEntity.setHeaderAo7deabf(JiraZephyrTestCaseManagement.getJiraCustomHeader("AO-7DEABF"));
        JiraEntity.setJiraProject(System.getProperty("JIRA.PROJECT_NAME"));
        JiraEntity.setJiraCycleName(System.getProperty("JIRA.CYCLE_NAME"));
        JiraEntity.setJiraFolderName(System.getProperty("JIRA.FOLDER_NAME"));
        JiraEntity.setJiraVersion(System.getProperty("JIRA.VERSION"));
        JiraEntity.setJiraReportType(System.getProperty("JIRA.REPORT_TYPE"));
        JiraEntity.setJiraReportsDir(System.getProperty("JIRA.REPORTS_DIR"));
        JiraEntity.setJiraBuildNumber(System.getProperty("JIRA.BUILD_NUMBER"));
        JiraEntity.setJiraThreadsCount(Integer.parseInt(System.getProperty("JIRA.THREADS_COUNT", "1")));
    }

    private String logError(Scenario scenario) {
        String errorMessage = null;
        try {
            Class clasz = ClassUtils.getClass("cucumber.runtime.java.JavaHookDefinition$ScenarioAdaptor");
            Field fieldScenario = FieldUtils.getField(clasz, "scenario", true);
            fieldScenario.setAccessible(true);
            Object objectScenario =  fieldScenario.get(scenario);

            Field fieldStepResults = objectScenario.getClass().getDeclaredField("stepResults");
            fieldStepResults.setAccessible(true);

            ArrayList<Result> results = (ArrayList<Result>) fieldStepResults.get(objectScenario);
            for (Result result : results) {
                if (result.getError() != null) {
                    errorMessage = result.getError().toString();
                    System.out.println("Error in Scenario"+errorMessage);
                    logman.info(" Error in Scenario, Error = "+errorMessage );
                    break;
                }
            }
        } catch (Throwable e) {
            logman.error("Error while logging Scenario error",e);
        }
        return errorMessage;
    }

    public void invoke() {
        new LaunchService(GetDriver()).invoke();
    }
}