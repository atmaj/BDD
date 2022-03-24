package org.practice.grp.dataBase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import cucumber.runtime.CucumberException;

public class CompareUtil {
    private File compareFile1;
    private File compareFile2;

    public CompareUtil(String filePath1, String filePath2) {
        compareFile1 = new File(filePath1);
        compareFile2 = new File(filePath2);
    }

    public CompareUtil(File file1, File file2) {
        compareFile1 = file1;
        compareFile2 = file2;
    }

    public void compareCSVs() throws IOException {
        if (FilenameUtils.getExtension(compareFile1.getName()).equalsIgnoreCase(".csv")
                && FilenameUtils.getExtension(compareFile2.getName()).equalsIgnoreCase(".csv")) {
            String resultFile = compareFile1.getParentFile().getAbsolutePath() + "/csvCompareResult.csv";
            boolean mismatchIdentified = false;
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));

            String fileData = FileUtils.readFileToString(compareFile2, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(new FileReader(compareFile1));
            writer.write(reader.readLine());
            String line;
            while ((line = reader.readLine()) != null) {
                if (!fileData.contains(line)) {
                    writer.write(line);
                    mismatchIdentified = true;
                }
            }
            reader.close();
            fileData = FileUtils.readFileToString(compareFile1, StandardCharsets.UTF_8);
            reader = new BufferedReader(new FileReader(compareFile2));
            while ((line = reader.readLine()) != null) {
                if (!fileData.contains(line)) {
                    writer.write(line);
                    mismatchIdentified = true;
                }
            }
            writer.close();
            if (mismatchIdentified) {
                throw new CucumberException(String.format("Mismatch Found. File : %s", resultFile));
            }
        } else {
            throw new CucumberException(String.format("Both files are not CSV. File 1: %s, File 2: %s",
                    compareFile1.getAbsolutePath(), compareFile2.getAbsolutePath()));
        }
    }

    public void compareJSONs() {
        if (compareFile1.getAbsolutePath().toLowerCase().endsWith(".json")
                && compareFile2.getAbsolutePath().toLowerCase().endsWith(".json")) {

        } else {
            throw new CucumberException(String.format("Both files are not JSON. File 1: %s, File 2: %s",
                    compareFile1.getAbsolutePath(), compareFile2.getAbsolutePath()));
        }
    }
}