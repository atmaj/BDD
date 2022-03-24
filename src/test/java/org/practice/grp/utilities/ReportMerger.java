package org.practice.grp.utilities;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ReportMerger {
    public Logger logman;

    public ReportMerger() {
        logman = LogManager.getInstance();
    }

    private static String reportFileName = "report.js";
    private static String reportImageExtension = "png";

    /**
     * Merge all reports together into master report in given reportDirectory
     *
     * @param reportDirectory
     * @throws Exception
     */
    public void mergeReports(File reportDirectory) {
        try {
            Collection<File> existingReports = FileUtils.listFiles(reportDirectory, new String[] { "js" }, true);
            File mergedReport = null;
            for (File report : existingReports) {
                // only address report files
                if (report.getName().equals(reportFileName)) {
                    // rename all the image files (to give unique names) in
                    // report directory and update report
                    renameEmbededImages(report);
                    logman.info("renameEmbededImages method called successfully");
                    // if we are on the first pass, copy the directory of the
                    // file to use as basis for merge
                    if (mergedReport == null) {
                        FileUtils.copyDirectory(report.getParentFile(), reportDirectory);
                        mergedReport = new File(reportDirectory, reportFileName);
                        logman.info("copy the directory of the file to use as basis for merge");
                        // otherwise merge this report into existing master
                        // report
                    } else {
                        mergeFiles(mergedReport, report);
                        logman.info("merge this report into existing master report");
                    }
                }
            }
        } catch (Throwable throwable) {
            logman.error("Error in merging Reports, error = " + throwable.getMessage());
        }
    }

    /**
     * merge source file into target
     *
     * @param target
     * @param source
     */
    public void mergeFiles(File target, File source) throws Throwable {
        System.out.println(target.getAbsolutePath());
        System.out.println(source.getAbsolutePath());
        // copy embeded images
        try {
            Collection<File> embeddedImages = FileUtils.listFiles(source.getParentFile(),
                    new String[] { reportImageExtension }, true);
            for (File image : embeddedImages) {
                FileUtils.copyFileToDirectory(image, target.getParentFile());
                logman.info("copy embeded images");
            }

            // merge report files
//			String targetReport = FileUtils.readFileToString(target, "UTF-8");
            String sourceReport = FileUtils.readFileToString(source, "UTF-8");

            // FileUtils.writeStringToFile(target, targetReport + sourceReport);
            FileUtils.writeStringToFile(target, sourceReport, "UTF-8", true);
            logman.info("merged report files successfully");
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(target.getAbsolutePath());
            System.out.println(source.getAbsolutePath());
            logman.error("Error in mergingFiles, error = " + e.getMessage());
        }
    }

    /**
     * Give unique names to embedded images to ensure they aren't lost during merge
     * Update report file to reflect new image names
     *
     * @param reportFile
     */

    public void renameEmbededImages(File reportFile) throws Throwable {
        try {
            File reportDirectory = reportFile.getParentFile();
            Collection<File> embeddedImages = FileUtils.listFiles(reportDirectory,
                    new String[] { reportImageExtension }, true);

            String fileAsString = FileUtils.readFileToString(reportFile, "UTF-8");

            for (File image : embeddedImages) {
                String curImageName = image.getName();
                String uniqueImageName = UUID.randomUUID().toString() + "." + reportImageExtension;

                image.renameTo(new File(reportDirectory, uniqueImageName));
                fileAsString = fileAsString.replace(curImageName, uniqueImageName);
            }
            FileUtils.writeStringToFile(reportFile, fileAsString, "UTF-8");
            logman.info("Rename happened successfully");
        } catch (Throwable e) {
            logman.error("Error occurred in renameEmbededImages method, error = " + e.getMessage());
        }
    }
}