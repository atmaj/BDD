package org.practice.grp.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogManagerPreRun {
    public Logger logger;
    private static LogManagerPreRun instance = null;
    public static String ScenarioName = "";

    private LogManagerPreRun() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH_mm_ss");
        if (System.getProperty("ScenarioID") != null) {
            System.setProperty("FileName", System.getProperty("ScenarioName") + "_" + System.getProperty("ScenarioID")
                    + "-" + sdf.format(cal.getTime()) + ".Log");
        } else {
            System.setProperty("FileName",
                    System.getProperty("ScenarioName") + "-" + sdf.format(cal.getTime()) + ".Log");
        }
        PropertyConfigurator.configure(System.getProperty("user.dir") + "/src/test/resources/log4j.properties");
        logger = Logger.getLogger("devpinoyLogger");
    }

    public static Logger getInstance() {
//		if (ScenarioName.trim().equalsIgnoreCase(System.getProperty("ScenarioName")) == false) {
        ScenarioName = System.getProperty("ScenarioName", "NA");
        resetLogger();
//		}
        if (instance == null) {
            instance = new LogManagerPreRun();
        }
        return instance.logger;
    }

    public static void resetLogger() {
        instance = null;
    }
}