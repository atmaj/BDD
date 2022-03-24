package org.practice.grp.driverManager;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.collect.ImmutableMap;
import com.microsoft.edge.seleniumtools.EdgeOptions;
import org.practice.grp.utilities.LogManager;

public class CapabilityManager {

    public Logger logman = null;

    public CapabilityManager() {
        logman = LogManager.getInstance();
    }

    public ChromeOptions chromeOptions() {
        ChromeOptions options = null;
        try {
            options = new ChromeOptions();
            options.setExperimentalOption("useAutomationExtension", false);
            options.setAcceptInsecureCerts(true);
            logman.info("Chrome Options is set successfully");
        } catch (Exception e) {
            logman.error("Error in chromeOptions, error =  " + e.getMessage());
        }
        return options;
    }

    public InternetExplorerOptions interExplorerOptions() {
        InternetExplorerOptions options = null;
        try {
            options = new InternetExplorerOptions();
            DesiredCapabilities ieCapabilities = new DesiredCapabilities();
            ieCapabilities.setCapability("nativeEvents", false);
            ieCapabilities.setCapability("unexpectedAlertBehaviour", "accept");
            ieCapabilities.setCapability("ignoreProtectedModeSettings", true);
            ieCapabilities.setCapability("disable-popup-blocking", true);
            ieCapabilities.setCapability("enablePersistentHover", true);
            ieCapabilities.setCapability("ignoreZoomSetting", true);
            options.merge(ieCapabilities);
            logman.info("IE Options is set successfully");
        } catch (Exception e) {
            logman.error("Error in InternetOptions, error =  " + e.getMessage());
        }
        return options;
    }

    public ChromeOptions headlessChromeOptions(String size) {
        ChromeOptions chromeOptions = null;
        try {
            chromeOptions = new ChromeOptions();
            chromeOptions.setExperimentalOption("useAutomationExtension", false);
            chromeOptions.setAcceptInsecureCerts(true);
            chromeOptions.addArguments("--disable-extensions");
            chromeOptions.addArguments("--start-maximized");
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--window-size=" + size);
            logman.info("Headless Chrome Options is set successfully");
        } catch (Exception e) {
            logman.error("Error in headlessChromeOptions, error =  " + e.getMessage());
        }
        return chromeOptions;
    }

    public EdgeOptions edgeOptions() {
        EdgeOptions options = null;
        try {
            options = new EdgeOptions();
            // need to add the capability as per the unit testing
            options.setCapability("", "");
            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            logman.info("Edge Options is set successfully");
        } catch (Exception e) {
            logman.error("Error in EdgeOptions, error =  " + e.getMessage());
        }
        return options;
    }

    public InternetExplorerOptions ieCapabilityForLocalGrid() {
        InternetExplorerOptions ieOption = null;
        try {
            DesiredCapabilities cap1 = new DesiredCapabilities();
            cap1.setBrowserName("internet explorer");
            cap1.setPlatform(Platform.VISTA);
            ieOption = new InternetExplorerOptions();
            ieOption.merge(cap1);
            logman.info("Internet Explorer Options is set successfully for GRID");
        } catch (Exception e) {
            logman.error("Error in ieCapabilityForLocalGrid, error =  " + e.getMessage());
        }
        return ieOption;

    }

    public ChromeOptions chromeCapabilityForLocalGrid() {
        ChromeOptions chromeOption = null;
        try {
            DesiredCapabilities cap = new DesiredCapabilities();
            cap.setBrowserName("chrome");
            cap.setPlatform(Platform.WINDOWS);
            chromeOption = new ChromeOptions();
            chromeOption.merge(cap);
            chromeOption.setHeadless(false);
            logman.info("Chrome Options is set successfully for GRID");
        } catch (Exception e) {
            logman.error("Error in chromeCapabilityForLocalGrid, error =  " + e.getMessage());
        }
        return chromeOption;
    }

    public DesiredCapabilities androidWebDesiredCapabilities(String UDID, String deviceName, String version,
                                                             String browserName, String driverExecutable) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("platformName", "Android");
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("platformVersion", version);
            desiredCapabilities.setCapability("automationName", "uiautomator2");
            desiredCapabilities.setCapability("nativeWebScreenshot", true);
            desiredCapabilities.setCapability("browserName", browserName);
            desiredCapabilities.setCapability("chromedriverUseSystemExecutable", false);
            desiredCapabilities.setCapability("newCommandTimeout", 300);
            desiredCapabilities.setCapability("chromedriverExecutable", driverExecutable);
            desiredCapabilities.setCapability("autoAcceptAlerts", true);
            desiredCapabilities.setCapability("locationServicesAuthorized", true);
            logman.info("androidWebDesiredCapabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in androidWebDesiredCapabilities, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities iOSWebDesiredCapabilities(String UDID, String deviceName, String browserName,
                                                         String version) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("platformName", "ios");
            desiredCapabilities.setCapability("platformVersion", version);
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("browserName", browserName);
            desiredCapabilities.setCapability("newCommandTimeout", 300);
            logman.info("iOSWebDesiredCapabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in iOSWebDesiredCapabilities, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities androidAPPDesiredCapabilities(String UDID, String deviceName, String version,
                                                             String appPackageName, String appActivityName) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("platformName", "Android");
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("platformVersion", version);
            desiredCapabilities.setCapability("automationName", "uiautomator2");
            desiredCapabilities.setCapability("appPackage", appPackageName);
            desiredCapabilities.setCapability("appActivity", appActivityName);
            desiredCapabilities.setCapability("default", false);
            desiredCapabilities.setCapability("fullReset", false);
            desiredCapabilities.setCapability("noReset", true);
            desiredCapabilities.setCapability("nativeWebScreenshot", true);
            desiredCapabilities.setCapability("autoAcceptAlerts", true);
            desiredCapabilities.setCapability("locationServicesAuthorized", true);
            desiredCapabilities.setCapability("newCommandTimeout", 300);
            logman.info("androidAPPDesiredCapabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in androidAPPDesiredCapabilities, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities iOSAppDesiredCapabilities(String UDID, String version, String deviceName,
                                                         String bundleId) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("platformName", "ios");
            desiredCapabilities.setCapability("platformVersion", version);
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("bundleId", bundleId);
            desiredCapabilities.setCapability("newCommandTimeout", 120);
            logman.info("iOSAppDesiredCapabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in iOSAppDesiredCapabilities, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities mobileCenterAndroidDesiredCapabilities(String mobileCenterUser,
                                                                      String mobileCenterPassword, String UDID, String deviceName, String appPackage, String appActivity) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("default", false);
            desiredCapabilities.setCapability("fullReset", false);
            desiredCapabilities.setCapability("noReset", true);
            desiredCapabilities.setCapability("userName", mobileCenterUser);
            desiredCapabilities.setCapability("password", mobileCenterPassword);
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("platformName", "Android");
            desiredCapabilities.setCapability("appPackage", appPackage);
            desiredCapabilities.setCapability("appActivity", appActivity);
            desiredCapabilities.setCapability("autoAcceptAlerts", true);
            desiredCapabilities.setCapability("locationServicesAuthorized", true);
            desiredCapabilities.setCapability("autoGrantPermissions", true);
            desiredCapabilities.setCapability("automationName", "uiautomator2");
            desiredCapabilities.setCapability("newCommandTimeout", 120);
            logman.info("mobileCenterAndroidDesiredCapabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in mobileCenterAndroidDesiredCapabilities, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities mobileCenteriOSDesiredCapabilities(String mobileCenterUser, String mobileCenterPassword,
                                                                  String UDID, String deviceName, String iPhoneBundleID, String platformVersion) {

        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("userName", mobileCenterUser);
            desiredCapabilities.setCapability("password", mobileCenterPassword);
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("platformName", "iOS");
            desiredCapabilities.setCapability("newCommandTimeout", 6000);
            desiredCapabilities.setCapability("bundleId", iPhoneBundleID);
            desiredCapabilities.setCapability("automationName", "XCUITest");
            desiredCapabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR,
                    UnexpectedAlertBehaviour.ACCEPT);
            desiredCapabilities.setCapability("autoAcceptAlerts", true);

            desiredCapabilities.setCapability("platformVersion", platformVersion);
            desiredCapabilities.setCapability("simpleIsVisibleCheck", true);
            desiredCapabilities.setCapability("default", false);
            desiredCapabilities.setCapability("fullReset", false);
            desiredCapabilities.setCapability("noReset", true);
            desiredCapabilities.setCapability("nativeWebScreenshot", true);
            desiredCapabilities.setCapability("ignoreUnimportantViews", true);
            logman.info("mobileCenteriOSDesiredCapabilities is set successfully");
            logman.debug("Username = " + mobileCenterUser);
            logman.debug("UDID = " + UDID);
        } catch (Exception e) {
            logman.error("Error in mobileCenteriOSDesiredCapabilities, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities mobileCenterWebAndroid(String mobileCenterUser, String mobileCenterPassword, String UDID,
                                                      String deviceName, String browserName) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("userName", mobileCenterUser);
            desiredCapabilities.setCapability("password", mobileCenterPassword);
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("platformName", "Android");
            desiredCapabilities.setCapability("automationName", "uiautomator2");
            desiredCapabilities.setCapability("appium:chromeOptions", ImmutableMap.of("w3c", false));
            desiredCapabilities.setCapability("browserName", browserName);
            desiredCapabilities.setCapability("autoGrantPermissions", true);
            desiredCapabilities.setCapability("autoAcceptAlerts", true);
            desiredCapabilities.setCapability("newCommandTimeout", 300);
            logman.info("mobileCenterWebAndroid is set successfully");
        } catch (Exception e) {
            logman.error("Error in mobileCenterWebAndroid, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities mobileCenterWebiOS(String mobileCenterUser, String mobileCenterPassword, String UDID,
                                                  String deviceName, String browserName) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("userName", mobileCenterUser);
            desiredCapabilities.setCapability("password", mobileCenterPassword);
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("platformName", "ios");
            desiredCapabilities.setCapability("automationName", "XCUITest");
            // desiredCapabilities.setCapability("automationName","iOS");
            desiredCapabilities.setCapability("browserName", browserName);
            desiredCapabilities.setCapability("newCommandTimeout", 300);
            logman.info("mobileCenterWebiOS is set successfully");
        } catch (Exception e) {
            logman.error("Error in mobileCenterWebiOS, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities browserStackMCBrowser(String mobileCenterUser, String mobileCenterPassword, String UDID,
                                                     String deviceName, String browserName) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("userName", mobileCenterUser);
            desiredCapabilities.setCapability("password", mobileCenterPassword);
            desiredCapabilities.setCapability("udid", UDID);
            desiredCapabilities.setCapability("deviceName", deviceName);
            desiredCapabilities.setCapability("platformName", "Android");
            desiredCapabilities.setCapability("browserName", browserName);
            desiredCapabilities.setCapability("newCommandTimeout", 300);
            logman.info("mobileCenterWebAndroid is set successfully");
        } catch (Exception e) {
            logman.error("Error in mobileCenterWebAndroid, error =  " + e.getMessage());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities browserStackBrowser(String userName, String passKey, String browserName,
                                                   String browserVersion, String osName, String osVersion, String resolution, Boolean local, String project,
                                                   String build, String name) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("browserstack.user", userName);
            desiredCapabilities.setCapability("browserstack.key", passKey);
            desiredCapabilities.setCapability("browser", browserName);
            desiredCapabilities.setCapability("browser_version", browserVersion);
            desiredCapabilities.setCapability("os", osName);
            desiredCapabilities.setCapability("os_version", osVersion);
//            desiredCapabilities.setCapability("resolution", resolution);
//            desiredCapabilities.setCapability("local", local);
            desiredCapabilities.setCapability("project", project);
            desiredCapabilities.setCapability("build", build);
            desiredCapabilities.setCapability("name", name);
            desiredCapabilities.setCapability("acceptSslCerts", true);
            logman.info("browserStackBrowser capabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in browserStackBrowser, error =  " + e.getCause());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities browserStackMobileApp(String userName, String passKey, String deviceName,
                                                     String osVersion, String app, String resolution, Boolean local, String project, String build, String name) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("browserstack.user", userName);
            desiredCapabilities.setCapability("browserstack.key", passKey);
            desiredCapabilities.setCapability("device", deviceName);
            desiredCapabilities.setCapability("os_version", osVersion);
            desiredCapabilities.setCapability("app", app);
//            desiredCapabilities.setCapability("resolution", resolution);
//            desiredCapabilities.setCapability("local", local);
//            desiredCapabilities.setCapability("browserstack.resignApp", "false");
            desiredCapabilities.setCapability("browserstack.debug", true);
            desiredCapabilities.setCapability("project", project);
            desiredCapabilities.setCapability("build", build);
            desiredCapabilities.setCapability("name", name);
            desiredCapabilities.setCapability("nativeWebScreenshot", true);
            desiredCapabilities.setCapability("autoAcceptAlerts", true);
            desiredCapabilities.setCapability("locationServicesAuthorized", true);
            desiredCapabilities.setCapability("newCommandTimeout", 300);
            desiredCapabilities.setCapability("autoGrantPermissions", true);
            desiredCapabilities.setCapability("browserstack.resignApp", true);
            logman.info("browserStackMobileApp capabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in browserStackBrowser, error =  " + e.getCause());
        }
        return desiredCapabilities;
    }

    public DesiredCapabilities browserStackMobilBrowser(String userName, String passKey, String deviceName,
                                                        String osVersion, String browserName, String resolution, Boolean local, String project, String build,
                                                        String name) {
        DesiredCapabilities desiredCapabilities = null;
        try {
            desiredCapabilities = new DesiredCapabilities();
            desiredCapabilities.setCapability("browserstack.user", userName);
            desiredCapabilities.setCapability("browserstack.key", passKey);
            desiredCapabilities.setCapability("device", deviceName);
            desiredCapabilities.setCapability("os_version", osVersion);
            desiredCapabilities.setCapability("browserName", browserName);
//            desiredCapabilities.setCapability("browser",browserName);
            desiredCapabilities.setCapability("realMobile", true);
//            desiredCapabilities.setCapability("resolution", resolution);
//            desiredCapabilities.setCapability("local", local);
            desiredCapabilities.setCapability("project", project);
            desiredCapabilities.setCapability("build", build);
            desiredCapabilities.setCapability("name", name);
            desiredCapabilities.setCapability("nativeWebScreenshot", true);
            desiredCapabilities.setCapability("autoAcceptAlerts", true);
            desiredCapabilities.setCapability("locationServicesAuthorized", true);
            //desiredCapabilities.setCapability("newCommandTimeout", 300);
            desiredCapabilities.setCapability("acceptSslCert", true);
            logman.info("browserStackBrowser capabilities is set successfully");
        } catch (Exception e) {
            logman.error("Error in browserStackBrowser, error =  " + e.getCause());
        }
        return desiredCapabilities;
    }

    public EdgeOptions headlessEdgeOptions() {
        EdgeOptions options = null;
        try {
            options = new EdgeOptions();
            options.setCapability("UseChromium", true);
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--start-maximized");
            options.setExperimentalOption("useAutomationExtension", false);
            logman.info("Headless Edge Options is set successfully");
        } catch (Exception e) {
            logman.error("Error in headlessEdgeOptions, error = " + e.getMessage());
        }
        return options;
    }

}