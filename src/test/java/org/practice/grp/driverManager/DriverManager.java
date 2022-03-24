package org.practice.grp.driverManager;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.microsoft.edge.seleniumtools.EdgeDriver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.practice.grp.utilities.LogManager;

/**
 * Created by C112083 on 22/10/2020.
 */
public class DriverManager {

    public Logger logman = null;

    public DriverManager() {
        logman = LogManager.getInstance();
    }

    public WebDriver GetDriver(String Platform, String Type) {
        WebDriver driver = null;
        try {
            switch (Platform.trim().toUpperCase()) {
                case "WINDOWS":
                    switch (Type.trim().toUpperCase()) {
                        case "BROWSER":
                            driver = GetDriverForBrowsers(System.getProperty("PREFERRED_BROWSER"));
                            break;
                        case "APPLICATION":
                            driver = GetDriverForDevices(System.getProperty("DEVICEPLATFORM"));
                            break;
                        case "MOBILEBROWSER":
                            driver = GetDriverForDeviceBrowser(System.getProperty("DEVICEPLATFORM"),
                                    System.getProperty("PREFERRED_BROWSER"));
                            break;
                        case "API":
                            // GetMockApiUpAndRunning();
                            break;
                        case "DATABASE":
                            break;

                    }
                    break;

                case "GRID":
                    switch (Type.trim().toUpperCase()) {
                        case "BROWSER":
                            driver = GetDriverForGRID(System.getProperty("PREFERRED_BROWSER"));
                            break;
                    }
                    break;

                case "MOBILECENTER":
                    switch (Type.trim().toUpperCase()) {
                        case "BROWSER":
                            driver = GetDriverForMobileCenterDeviceBrowsers(System.getProperty("DEVICEPLATFORM"),
                                    System.getProperty("PREFERRED_BROWSER"));
                            break;
                        case "APPLICATION":
                            driver = GetDriverForMobileCenterDeviceApp(System.getProperty("DEVICEPLATFORM"));
                            break;
                    }
                    break;
                case "BROWSERSTACK":

                    setSystemProxyForBrowserStack(System.getProperty("PROXYHOST"), System.getProperty("PROXYPORT"),
                            System.getProperty("BROWSERSTACKHOSTUSER"), System.getProperty("BROWSERSTACKPASSWORD"));

                    switch (Type.trim().toUpperCase()) {
                        case "BROWSER":
                            driver = GetDriverForBrowserStackBrowsers();
                            break;
                        case "APPLICATION":
                            driver = GetDriverForBrowserStackDeviceApp(System.getProperty("DEVICEPLATFORM"));
                            break;
                        case "MOBILEBROWSER":
                            driver = GetDriverForBrowserStackDeviceBrowser(System.getProperty("DEVICEPLATFORM"));
                            break;
                    }
                    break;
                case "LINUX":
                    break;
                case "MAC":
                    break;
                default:
                    break;
            }
            if (System.getProperty("TYPE").equalsIgnoreCase("API")
                    || System.getProperty("TYPE").equalsIgnoreCase("DATABASE")) {
                logman.info("Test Type is , =  " + System.getProperty("TYPE"));
            } else {
                if (driver != null) {
                    logman.info("Driver object is created successfully");
                } else {
                    logman.error(
                            "Error Occurred Inside GetDriver method in DriverManager, The webdriver is null in GetDriver function, Please consult the automation team");
                }
            }

        } catch (Exception e) {
            logman.error(
                    "Error Occurred Inside GetDriver method in DriverManager, Error Description=" + e.getMessage());
        }
        return driver;
    }

    public WebDriver GetDriverForBrowsers(String Browser) {
        WebDriver driver = null;
        CapabilityManager capabilityManager = new CapabilityManager();
        try {
            switch (Browser.trim().toUpperCase()) {
                case "IE":
                    System.setProperty("webdriver.ie.driver", System.getProperty("user.dir")
                            + "/src/test/resources/webdrivers/windows/internetExplorer/IEDriverServer.exe");
                    driver = new InternetExplorerDriver(capabilityManager.interExplorerOptions());
                    logman.info("driver object of IE for GetDriverForBrowsers is instantiated");
                    break;
                case "EDGE":

                    System.setProperty("webdriver.edge.driver", System.getProperty("user.dir")
                            + "/src/test/resources/webdrivers/windows/edge/msedgedriver.exe");
                    driver = new EdgeDriver(capabilityManager.edgeOptions());
                    logman.info("driver object of EDGE for GetDriverForBrowsers is instantiated");
                    break;
                case "CHROME":
                    System.setProperty("webdriver.chrome.driver",
                            System.getProperty("user.dir") + "/src/test/resources/webdrivers/windows/chrome/"
                                    + System.getProperty("CHROME_VERSION") + "/chromedriver.exe");
                    driver = new ChromeDriver(capabilityManager.chromeOptions());
                    logman.info("driver object of CHROME for GetDriverForBrowsers is instantiated");
                    break;
                case "HEADLESSCHROME":
                    System.setProperty("webdriver.chrome.driver",
                            System.getProperty("user.dir") + "/src/test/resources/webdrivers/windows/chrome/"
                                    + System.getProperty("CHROME_VERSION") + "/chromedriver.exe");
                    driver = new ChromeDriver(capabilityManager.headlessChromeOptions(System.getProperty("WINDOWSIZE")));
                    logman.info("driver object of HEADLESSCHROME for GetDriverForBrowsers is instantiated");
                    break;
                case "HEADLESSEDGE":
                    System.setProperty("webdriver.edge.driver", System.getProperty("user.dir")
                            + "/src/test/resources/webdrivers/windows/edge/msedgedriver.exe");
                    driver = new EdgeDriver(capabilityManager.headlessEdgeOptions());
                    logman.info("driver object of HEADLESSEDGE for GetDriverForBrowsers is instantiated");
                    break;
            }
            System.setProperty("URL", System.getProperty("DESKTOP_URL"));
            System.setProperty("DEVICETYPE", "WEB");
        } catch (Exception e) {
            logman.error("Error Occurred Inside GetDriverForBrowsers method, Error Description=" + e.getMessage());
        }
        return driver;
    }

    public WebDriver GetDriverForDevices(String DevicePlatform) {
        AppiumDriver driver = null;
        CapabilityManager capabilityManager = new CapabilityManager();
        try {
            switch (DevicePlatform.trim().toUpperCase()) {
                case "ANDROID":
                    driver = new AndroidDriver(new URL("APPIUMURL"),
                            capabilityManager.androidAPPDesiredCapabilities(System.getProperty("UDID"),
                                    System.getProperty("DEVICENAME"), System.getProperty("VERSION"),
                                    System.getProperty("APPPACKAGENAME"), System.getProperty("APPACTIVITYNAME")));
                    logman.info("driver object of ANDROID for GetDriverForDevices is instantiated");
                    break;
                case "IOS":
                    driver = new IOSDriver(new URL("APPIUMURL"),
                            capabilityManager.iOSAppDesiredCapabilities(System.getProperty("UDID"),
                                    System.getProperty("VERSION"), System.getProperty("DEVICENAME"),
                                    System.getProperty("IOSBUNDLEID")));
                    logman.info("driver object of IOS for GetDriverForDevices is instantiated");
                    break;
            }
            System.setProperty("DEVICETYPE", "APP");
        } catch (Exception e) {
            logman.error("Error Occurred Inside GetDriverForDevices method, Error Description=" + e.getMessage());
        }
        return driver;
    }

    public AppiumDriver GetDriverForDeviceBrowser(String DevicePlatform, String Browser) {
        CapabilityManager capabilityManager = new CapabilityManager();
        AppiumDriver driver = null;
        try {
            switch (DevicePlatform.trim().toUpperCase()) {
                case "ANDROID":
                    switch (Browser.trim().toUpperCase()) {
                        case "CHROME":
                            try {
                                driver = new AndroidDriver(new URL("APPIUMURL"),
                                        capabilityManager.androidWebDesiredCapabilities(System.getProperty("UDID"),
                                                System.getProperty("DEVICENAME"), System.getProperty("VERSION"),
                                                System.getProperty("PREFERRED_BROWSER"),
                                                System.getProperty("DRIVEREXECUTABLEPATH")));
                                logman.info("Connection Established between local mobile and ANDROID Mobile Browser "
                                        + System.getProperty("PREFERRED_BROWSER"));
                            } catch (MalformedURLException e) {
                                logman.error(
                                        "Error Occurred Inside GetDriverForDeviceBrowser method and ANDROID block, Error Description="
                                                + e.getMessage());
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "IOS":
                    switch (Browser.trim().toUpperCase()) {
                        case "CHROME":
                            try {
                                driver = new IOSDriver(new URL("APPIUMURL"),
                                        capabilityManager.iOSWebDesiredCapabilities(System.getProperty("UDID"),
                                                System.getProperty("DEVICENAME"), System.getProperty("PREFERRED_BROWSER"),
                                                System.getProperty("VERSION")));
                                logman.info("Connection Established between local mobile and IOS Mobile Browser "
                                        + System.getProperty("PREFERRED_BROWSER"));
                            } catch (MalformedURLException e) {
                                logman.error(
                                        "Error Occurred Inside GetDriverForDeviceBrowser method and IOS CHROME block, Error Description="
                                                + e.getMessage());
                            }
                            break;
                        case "SAFARI":
                            try {
                                driver = new IOSDriver(new URL("APPIUMURL"),
                                        capabilityManager.iOSWebDesiredCapabilities(System.getProperty("UDID"),
                                                System.getProperty("DEVICENAME"), System.getProperty("PREFERRED_BROWSER"),
                                                System.getProperty("VERSION")));
                                logman.info("Connection Established between Mobile Center Connector and IOS Mobile Browser "
                                        + System.getProperty("PREFERRED_BROWSER"));
                            } catch (MalformedURLException e) {
                                logman.error(
                                        "Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method and IOS SAFARI block, Error Description="
                                                + e.getMessage());
                            }
                            break;
                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
            System.setProperty("URL", System.getProperty("MOBILE_URL"));
            System.setProperty("DEVICETYPE", "MOBILEWEB");
        } catch (Exception e) {
            logman.error("Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method, Error Description="
                    + e.getMessage());
        }
        return driver;

    }

    public RemoteWebDriver GetDriverForGRID(String Browser) {
        RemoteWebDriver driver = null;
        CapabilityManager capabilityManager = new CapabilityManager();
        try {
            switch (Browser.trim().toUpperCase()) {
                case "IE":
                    driver = new RemoteWebDriver(new URL(System.getProperty("HUBURL")),
                            capabilityManager.ieCapabilityForLocalGrid());
                    logman.info("driver object of GRID for IE browser is instantiated");
                    break;
                case "CHROME":
                    driver = new RemoteWebDriver(new URL(System.getProperty("HUBURL")),
                            capabilityManager.chromeCapabilityForLocalGrid());
                    logman.info("driver object of GRID for chrome browser is instantiated");
                    break;
            }
            System.setProperty("URL", System.getProperty("DESKTOP_URL"));
            System.setProperty("DEVICETYPE", "WEB");
        } catch (Exception e) {
            logman.error("Error Occurred Inside GetDriverForGRID method, Error Description=" + e.getMessage());
        }
        return driver;
    }

    public AppiumDriver GetDriverForMobileCenterDeviceApp(String devicePlatform) {
        CapabilityManager capabilityManager = new CapabilityManager();
        AppiumDriver driver = null;
        try {
            switch (devicePlatform.trim().toUpperCase()) {
                case "ANDROID":
                    try {
                        DesiredCapabilities desiredCapabilities = capabilityManager.mobileCenterAndroidDesiredCapabilities(
                                System.getProperty("MOBILECENTERUSER"), System.getProperty("MOBILECENTERPASSWORD"),
                                System.getProperty("UDID"), System.getProperty("DEVICENAME"),
                                System.getProperty("APPPACKAGENAME"), System.getProperty("APPACTIVITYNAME"));
                        driver = new AndroidDriver(new URL(System.getProperty("MOBILECENTERHOST")), desiredCapabilities);
                        logman.info("Connection Established between Mobile Center Connector and ANDROID Mobile Device");
                    } catch (MalformedURLException e) {
                        logman.error(
                                "Error Occurred Inside GetDriverForMobileCenterDeviceApp method and ANDROID block, Error Description="
                                        + e.getMessage());
                    }
                    break;
                case "IOS":
                    try {
                        DesiredCapabilities desiredCapabilities = capabilityManager.mobileCenteriOSDesiredCapabilities(
                                System.getProperty("MOBILECENTERUSER"), System.getProperty("MOBILECENTERPASSWORD"),
                                System.getProperty("UDID"), System.getProperty("DEVICENAME"),
                                System.getProperty("IOSBUNDLEID"), System.getProperty("VERSION"));
                        driver = new IOSDriver(new URL(System.getProperty("MOBILECENTERHOST")), desiredCapabilities);
                        logman.info("Connection Established between Mobile Center Connector and IOS Mobile Device");
                    } catch (Exception e) {
                        logman.error(
                                "Error Occurred Inside GetDriverForMobileCenterDeviceApp method and IOS block, Error Description="
                                        + e.getMessage());
                    }
                    break;
                default:
                    break;
            }
            System.setProperty("DEVICETYPE", "APP");
        } catch (Exception e) {
            logman.error("Error Occurred Inside GetDriverForMobileCenterDeviceApp method, Error Description="
                    + e.getMessage());
        }
        return driver;
    }

    public AppiumDriver GetDriverForMobileCenterDeviceBrowsers(String devicePlatform, String browserType) {
        CapabilityManager obj = new CapabilityManager();
        AppiumDriver driver = null;
        try {
            switch (devicePlatform.trim().toUpperCase()) {
                case "ANDROID":
                    switch (browserType.trim().toUpperCase()) {
                        case "CHROME":
                            try {
                                DesiredCapabilities desiredCapabilities = obj.mobileCenterWebAndroid(
                                        System.getProperty("MOBILECENTERUSER"), System.getProperty("MOBILECENTERPASSWORD"),
                                        System.getProperty("UDID"), System.getProperty("DEVICENAME"),
                                        System.getProperty("PREFERRED_BROWSER"));
                                driver = new AndroidDriver(new URL(System.getProperty("MOBILECENTERHOST")),
                                        desiredCapabilities);
                                logman.info("Connection Established between Mobile Center Connector and ANDROID Mobile Browser "
                                        + System.getProperty("PREFERRED_BROWSER"));
                            } catch (MalformedURLException e) {
                                logman.error(
                                        "Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method and ANDROID block, Error Description="
                                                + e.getMessage());
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "IOS":
                    switch (browserType.trim().toUpperCase()) {
                        case "CHROME":
                            try {
                                DesiredCapabilities desiredCapabilities = obj.mobileCenterWebiOS(
                                        System.getProperty("MOBILECENTERUSER"), System.getProperty("MOBILECENTERPASSWORD"),
                                        System.getProperty("UDID"), System.getProperty("DEVICENAME"),
                                        System.getProperty("PREFERRED_BROWSER"));
                                driver = new IOSDriver(new URL(System.getProperty("MOBILECENTERHOST")), desiredCapabilities);
                                logman.info("Connection Established between Mobile Center Connector and IOS Mobile Browser "
                                        + System.getProperty("PREFERRED_BROWSER"));
                            } catch (MalformedURLException e) {
                                logman.error(
                                        "Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method and IOS CHROME block, Error Description="
                                                + e.getMessage());
                            }
                            break;
                        case "SAFARI":
                            try {
                                DesiredCapabilities desiredCapabilities = obj.mobileCenterWebiOS(
                                        System.getProperty("MOBILECENTERUSER"), System.getProperty("MOBILECENTERPASSWORD"),
                                        System.getProperty("UDID"), System.getProperty("DEVICENAME"),
                                        System.getProperty("PREFERRED_BROWSER"));
                                driver = new IOSDriver(new URL(System.getProperty("MOBILECENTERHOST")), desiredCapabilities);
                                logman.info("Connection Established between Mobile Center Connector and IOS Mobile Browser "
                                        + System.getProperty("PREFERRED_BROWSER"));
                            } catch (MalformedURLException e) {
                                logman.error(
                                        "Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method and IOS SAFARI block, Error Description="
                                                + e.getMessage());
                            }
                            break;
                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
            System.setProperty("URL", System.getProperty("MOBILE_URL"));
            System.setProperty("DEVICETYPE", "MOBILEWEB");
        } catch (Exception e) {
            logman.error("Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method, Error Description="
                    + e.getMessage());
        }
        return driver;
    }

    public WebDriver GetDriverForBrowserMCStackBrowsers(String browserType) {
        CapabilityManager obj = new CapabilityManager();
        WebDriver driver = null;
        try {
            switch (browserType.trim().toUpperCase()) {
                case "CHROME":
                    try {
                        DesiredCapabilities desiredCapabilities = obj.browserStackMCBrowser(
                                System.getProperty("MOBILECENTERUSER"), System.getProperty("MOBILECENTERPASSWORD"),
                                System.getProperty("UDID"), System.getProperty("DEVICENAME"),
                                System.getProperty("PREFERRED_BROWSER"));
                        driver = new RemoteWebDriver(new URL(System.getProperty("BrowserStackHost")), desiredCapabilities);
                        logman.info("Connection Established between Mobile Center Connector and ANDROID Mobile Browser "
                                + System.getProperty("PREFERRED_BROWSER"));
                    } catch (MalformedURLException e) {
                        logman.error(
                                "Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method and ANDROID block, Error Description="
                                        + e.getMessage());
                    }
                    break;
                default:
                    break;
            }
            System.setProperty("URL", System.getProperty("DESKTOP_URL"));
            System.setProperty("DEVICETYPE", "WEB");
        } catch (Exception e) {
            logman.error("Error Occurred Inside GetDriverForMobileCenterDeviceBrowsers method, Error Description="
                    + e.getMessage());
        }
        return driver;
    }

    public WebDriver GetDriverForBrowserStackBrowsers() {
        CapabilityManager obj = new CapabilityManager();
        WebDriver driver = null;

        try {
            DesiredCapabilities desiredCapabilities = obj.browserStackBrowser(
                    System.getProperty("BROWSERSTACKHOSTUSER"), System.getProperty("BROWSERSTACKPASSWORD"),
                    System.getProperty("PREFERRED_BROWSER"), System.getProperty("VERSION"),
                    System.getProperty("BROWSERSTACKOS"), System.getProperty("BROWSERSTACKOSVERSION"),
                    System.getProperty("RESOLUTION"), Boolean.parseBoolean(System.getProperty("LOCAL")),
                    System.getProperty("PROJECTNAME"), System.getProperty("BUILD"),
                    System.getProperty("ScenarioName") + "_" + System.getProperty("ScenarioID"));

            driver = new RemoteWebDriver(new URL(System.getProperty("BROWSERSTACKHOST")), desiredCapabilities);
            logman.info("Connection Established with BrowserStack host " + System.getProperty("BROWSERSTACKHOST")
                    + " on Browser " + System.getProperty("PREFERRED_BROWSER"));
        } catch (Exception e) {
            e.printStackTrace();
            logman.error(
                    "Error Occurred Inside GetDriverForBrowserStackBrowsers method, Error Description=" + e.getCause());
        }
        System.setProperty("URL", System.getProperty("DESKTOP_URL"));
        System.setProperty("DEVICETYPE", "WEB");
        return driver;
    }

    public AppiumDriver GetDriverForBrowserStackDeviceApp(String devicePlatform) {
        CapabilityManager obj = new CapabilityManager();
        AppiumDriver driver = null;

        try {
            switch (devicePlatform.trim().toUpperCase()) {
                case "ANDROID":
                    try {
                        DesiredCapabilities desiredCapabilities = obj.browserStackMobileApp(
                                System.getProperty("BROWSERSTACKHOSTUSER"), System.getProperty("BROWSERSTACKPASSWORD"),
                                System.getProperty("DEVICENAME"), System.getProperty("DEVICEOSVERSION"),
                                System.getProperty("APP"), System.getProperty("RESOLUTION"),
                                Boolean.parseBoolean(System.getProperty("LOCAL")), System.getProperty("PROJECTNAME"),
                                System.getProperty("BUILD"),
                                System.getProperty("ScenarioName") + "_" + System.getProperty("ScenarioID"));

                        driver = new AndroidDriver(new URL(System.getProperty("BROWSERSTACKHOST")), desiredCapabilities);
                        logman.info(
                                "Connection Established with BrowserStack host " + System.getProperty("BROWSERSTACKHOST")
                                        + " on Device " + System.getProperty("DEVICENAME"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        logman.error("Error Occurred Inside GetDriverForBrowserStackDeviceApp method, Error Description="
                                + e.getCause());
                    }

                    break;
                case "IOS":
                    try {
                        DesiredCapabilities desiredCapabilities = obj.browserStackMobileApp(
                                System.getProperty("BROWSERSTACKHOSTUSER"), System.getProperty("BROWSERSTACKPASSWORD"),
                                System.getProperty("DEVICENAME"), System.getProperty("DEVICEOSVERSION"),
                                System.getProperty("APP"), System.getProperty("RESOLUTION"),
                                Boolean.parseBoolean(System.getProperty("LOCAL")), System.getProperty("PROJECTNAME"),
                                System.getProperty("BUILD"),
                                System.getProperty("ScenarioName") + "_" + System.getProperty("ScenarioID"));

                        driver = new IOSDriver(new URL(System.getProperty("BROWSERSTACKHOST")), desiredCapabilities);
                        logman.info(
                                "Connection Established with BrowserStack host " + System.getProperty("BROWSERSTACKHOST")
                                        + " on Device " + System.getProperty("DEVICENAME"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        logman.error("Error Occurred Inside GetDriverForBrowserStackDeviceApp method, Error Description="
                                + e.getCause());
                    }
                    break;
                default:
                    break;
            }
            System.setProperty("DEVICETYPE", "APP");
        } catch (Exception e) {
            e.printStackTrace();
            logman.error("Error Occurred Inside GetDriverForBrowserStackDeviceApp method, Error Description="
                    + e.getCause());
        }

        return driver;
    }

    public WebDriver GetDriverForBrowserStackDeviceBrowser(String devicePlatform) {
        CapabilityManager obj = new CapabilityManager();
        AppiumDriver driver = null;

        try {
            switch (devicePlatform.trim().toUpperCase()) {
                case "ANDROID":
                    try {
                        DesiredCapabilities desiredCapabilities = obj.browserStackMobilBrowser(
                                System.getProperty("BROWSERSTACKHOSTUSER"), System.getProperty("BROWSERSTACKPASSWORD"),
                                System.getProperty("DEVICENAME"), System.getProperty("DEVICEOSVERSION"),
                                System.getProperty("PREFERRED_BROWSER"), System.getProperty("RESOLUTION"),
                                Boolean.parseBoolean(System.getProperty("LOCAL")), System.getProperty("PROJECTNAME"),
                                System.getProperty("BUILD"),
                                System.getProperty("ScenarioName") + "_" + System.getProperty("ScenarioID"));

                        driver = new AndroidDriver(new URL(System.getProperty("BROWSERSTACKHOST")), desiredCapabilities);
                        logman.info(
                                "Connection Established with BrowserStack host " + System.getProperty("BROWSERSTACKHOST")
                                        + " on Device " + System.getProperty("DEVICENAME"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        logman.error("Error Occurred Inside GetDriverForBrowserStackDeviceApp method, Error Description="
                                + e.getCause());
                    }

                    break;
                case "IOS":
                    try {
                        DesiredCapabilities desiredCapabilities = obj.browserStackMobilBrowser(
                                System.getProperty("BROWSERSTACKHOSTUSER"), System.getProperty("BROWSERSTACKPASSWORD"),
                                System.getProperty("DEVICENAME"), System.getProperty("DEVICEOSVERSION"),
                                System.getProperty("PREFERRED_BROWSER"), System.getProperty("RESOLUTION"),
                                Boolean.parseBoolean(System.getProperty("LOCAL")), System.getProperty("PROJECTNAME"),
                                System.getProperty("BUILD"),
                                System.getProperty("ScenarioName") + "_" + System.getProperty("ScenarioID"));

                        driver = new IOSDriver(new URL(System.getProperty("BROWSERSTACKHOST")), desiredCapabilities);
                        logman.info(
                                "Connection Established with BrowserStack host " + System.getProperty("BROWSERSTACKHOST")
                                        + " on Device " + System.getProperty("DEVICENAME"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        logman.error("Error Occurred Inside GetDriverForBrowserStackDeviceApp method, Error Description="
                                + e.getCause());
                    }
                    break;
                default:
                    break;
            }
            System.setProperty("URL", System.getProperty("MOBILE_URL"));
            System.setProperty("DEVICETYPE", "MOBILEWEB");
        } catch (Exception e) {
            e.printStackTrace();
            logman.error("Error Occurred Inside GetDriverForBrowserStackDeviceApp method, Error Description="
                    + e.getCause());
        }

        return driver;
    }

    public void setSystemProxyForBrowserStack(String proxyHost, String proxyPort, String proxyUser, String proxyPass) {
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("http.proxyUser", proxyUser);
        System.setProperty("http.proxyPass", proxyPass);
    }

}