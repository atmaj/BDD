package org.practice.grp.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import io.appium.java_client.android.AndroidDriver;

/**
 * Created by C112083 on 07/05/2021.
 */
public class LaunchService extends BasePageForAllPlatform {

    public LaunchService(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        if (!driver.toString().contains("appium")) {
            driver.manage().window().maximize();
        }
    }

    public void invoke() {
        try {
            switch (System.getProperty("TYPE").trim().toUpperCase()) {
                case "BROWSER":
                    if (driver.toString().contains("appium")) {
                        String strAppUrl;
                        switch (System.getProperty("PREFERRED_BROWSER").toUpperCase()) {
                            case "SAFARI":
                                strAppUrl = System.getProperty("URL").trim();
                                driver.get(strAppUrl);
                                break;
                            case "CHROME":
                                AndroidDriver androidDriver = (AndroidDriver) driver;
                                androidDriver.context("CHROMIUM");
                                strAppUrl = System.getProperty("URL").trim();
                                driver.get(strAppUrl);
                                break;
                        }
                    } else {
                        String strAppUrl = System.getProperty("DESKTOP_URL").trim();
                        driver.get(strAppUrl);
                        if (System.getProperty("PREFERRED_BROWSER").equalsIgnoreCase("IE")) {
                            clickJS(getObjectBy("advanceIE"));
                            logMessage("InvokeChannelApplication: Advance link is clicked ");
                            waitForPageLoaded();
                        }
                    }
                    System.setProperty("RUNTYPE", "NONMOBILEAPP");
                    break;
                case "MOBILEBROWSER":
                    if (driver.toString().contains("appium")) {
                        String strAppUrl;
                        switch (System.getProperty("PREFERRED_BROWSER").toUpperCase()) {
                            case "SAFARI":
                                strAppUrl = System.getProperty("MOBILE_URL").trim();
                                driver.get(strAppUrl);
                                break;
                            case "CHROME":
                                AndroidDriver androidDriver = (AndroidDriver) driver;
                                androidDriver.context("CHROMIUM");
                                strAppUrl = System.getProperty("MOBILE_URL").trim();
                                driver.get(strAppUrl);
                                break;
                        }
                    } /*else {
					String strAppUrl = System.getProperty("DESKTOP_URL").trim();
					driver.get(strAppUrl);
					if (System.getProperty("PREFERRED_BROWSER").equalsIgnoreCase("IE")) {
						clickJS(getObjectBy("advanceIE"));
						logMessage("InvokeChannelApplication: Advance link is clicked ");
						waitForPageLoaded();
					}
				}*/
                    System.setProperty("RUNTYPE", "NONMOBILEAPP");
                    break;
                case "APPLICATION":
                    setRelevantWebViewTab();
                    waitForPageLoaded();
                    System.setProperty("RUNTYPE", "MOBILEAPP");
                    break;

            }

        } catch (Throwable e) {
            logError("Error in invoking method, Error = " + e.getMessage());
        }
    }
}