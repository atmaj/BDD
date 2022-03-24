package org.practice.grp.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import net.masterthought.cucumber.presentation.PresentationMode;

public class PrettyReport {

    public static void main(String[] args) {
        System.out.println("PostRunUtility Class method");
        File reportOutputDirectory = new File("target/consolidatedreport/");
        List<String> jsonFiles = new ArrayList<>();

//		 File f = new File("target/cucumber-parallel/");
//         int count = 0;
//         for (File file : f.listFiles()) {
//                 if (file.getName().endsWith(".json")) {
//                	 jsonFiles.add(file.getName().replaceAll(".json", ".html"));
//                 }
//         }

        String TESTREPORTS = "./target/cucumber-parallel/";
        String TESTREPORTSPARALLEL = "./target/cucumber-parallel/";
        String reportOutPutDirectory = "./target/consolidatedreportparallel/";

        final File folder = new File(TESTREPORTSPARALLEL);
        List<String> resultRep = new ArrayList<>();
        search(".*\\.json", folder, resultRep);

        for (String s : resultRep) {
            jsonFiles.add(s);
        }
        String buildNumber = "1";
        String projectName = "Base";
        boolean runWithJenkins = false;
        boolean parallelTesting = true;

        Configuration configuration = new Configuration(reportOutputDirectory, projectName);

        configuration.setBuildNumber(buildNumber);
        configuration.addClassifications("Platform", "Windows");
        configuration.addClassifications("Browser", "Firefox");
        // configuration.addClassifications("Branch", "release/1.0");

        configuration.addPresentationModes(PresentationMode.PARALLEL_TESTING);
        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
        Reportable result = reportBuilder.generateReports();

    }

    public static void search(final String pattern, final File folder, List<String> result) {
        for (final File f : folder.listFiles()) {
            if (f.isDirectory()) {
                search(pattern, f, result);
            }
            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getAbsolutePath());
                }
            }
        }
    }
}