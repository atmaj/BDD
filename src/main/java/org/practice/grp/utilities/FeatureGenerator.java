package org.practice.grp.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.DataTable;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;

public class FeatureGenerator {
    private Logger logMan;
    private Map<String, List<List<String>>> testDataSet = new HashMap<String, List<List<String>>>();

    public FeatureGenerator(String testDataPath) {
        logMan = LogManagerPreRun.getInstance();
        testDataSet = getTestData(testDataPath);
    }

    public Map<String, List<List<String>>> getTestDataSet() {
        return testDataSet;
    }

    public List<String> generateFeatues(String featureRootFolderPath, String featureTargetFolder) {
        List<String> listSelectedTestCases = new ArrayList<String>();
        featureRootFolderPath = System.getProperty("user.dir") + "/" + featureRootFolderPath;
        featureTargetFolder = System.getProperty("user.dir") + "/" + featureTargetFolder;

        List<GherkinDocument> documents = null;
        List<String> featurePaths = null;
        try {
            FileUtils.deleteDirectory(new File(featureTargetFolder));
            logMan.info("Deleted generated feature target folder: " + featureTargetFolder);
            featurePaths = Files.walk(Paths.get(featureRootFolderPath)).map(Path::toString)
                    .filter(f -> f.endsWith(".feature")).collect(Collectors.toList());
            documents = getEnvelopes(featurePaths);
        } catch (IOException e) {
            logMan.error(e);
        }

        for (GherkinDocument gherkinDocument : documents) {
            try {
                StringBuilder builder = new StringBuilder();
                StringBuilder featureBuilder = new StringBuilder();
                Feature feature = gherkinDocument.getFeature();

                logMan.info("Parsing Feature: " + feature.getName());
                if (feature.getTags().size() > 0) {
                    for (Tag tag : feature.getTags()) {
                        featureBuilder.append(tag.getName()).append(" ");
                    }
                    featureBuilder.append("\n");
                }
                featureBuilder.append("Feature: ").append(feature.getName()).append("\n");
                if (feature.getDescription() != null)
                    featureBuilder.append("\n").append(feature.getDescription()).append("\n");

                boolean isBackgroundAvailable = gherkinDocument.getFeature().getChildren().get(0).getKeyword()
                        .equals("Background");
                int firstChildIndex = isBackgroundAvailable ? 1 : 0;
                int featureChildrenSize = gherkinDocument.getFeature().getChildren().size();

                if (isBackgroundAvailable) {
                    ScenarioDefinition background = gherkinDocument.getFeature().getChildren().get(0);
                    featureBuilder.append("\n\t").append(background.getKeyword()).append(": ");
                    featureBuilder.append(background.getName()).append("\n");

                    for (Step step : background.getSteps()) {
                        featureBuilder.append("\t\t").append(step.getKeyword()).append(": ");
                        featureBuilder.append(step.getText()).append("\n");

                        DataTable stepArg = (DataTable) step.getArgument();
                        if (stepArg != null) {
                            for (TableRow row : stepArg.getRows()) {
                                featureBuilder.append("\t\t");
                                for (TableCell cell : row.getCells()) {
                                    featureBuilder.append("|").append(cell.getValue());
                                }
                                featureBuilder.append("|\n");
                            }
                        }
                    }
                    featureBuilder.append("\n");
                }

                for (int counter = firstChildIndex; counter < featureChildrenSize; counter++) {
                    ScenarioDefinition scenarioDetails = gherkinDocument.getFeature().getChildren().get(counter);
                    String scenarioKeyword = scenarioDetails.getKeyword();
                    String scenarioName = scenarioDetails.getName();

                    if (scenarioKeyword.trim().equalsIgnoreCase("Scenario")
                            || (scenarioKeyword.trim().equalsIgnoreCase("Scenario Outline")
                            && testDataSet.containsKey(scenarioName))) {
                        List<Tag> tagList;
                        StringBuilder scenarioBuilder = new StringBuilder();
                        scenarioBuilder.append("\n\t");
                        if (scenarioKeyword.trim().equalsIgnoreCase("Scenario"))
                            tagList = ((Scenario) scenarioDetails).getTags();
                        else
                            tagList = ((ScenarioOutline) scenarioDetails).getTags();

                        List<String> tagNameList = new ArrayList<String>();
                        tagList.stream().forEach(tag -> tagNameList.add(tag.getName()));

                        String targetedTag = System.getProperty("cucumber.tags", "");
                        if (!targetedTag.isEmpty()) {
                            if (!tagNameList.contains(targetedTag)) {
                                logMan.info(scenarioName + " - is not associated with tag: " + targetedTag
                                        + ". Associated Tags: " + tagNameList);
                                continue;
                            }
                        }

                        for (Tag tag : tagList) {
                            scenarioBuilder.append(tag.getName()).append(" ");
                        }
                        scenarioBuilder.append("\n");
                        scenarioBuilder.append("\t").append(scenarioKeyword).append(": ");
                        scenarioBuilder.append(scenarioName).append("\n");

                        listSelectedTestCases.add(scenarioName);

                        for (Step step : scenarioDetails.getSteps()) {
                            scenarioBuilder.append("\t\t").append(step.getKeyword());
                            scenarioBuilder.append(step.getText()).append("\n");

                            DataTable stepArg = (DataTable) step.getArgument();
                            if (stepArg != null) {
                                for (TableRow row : stepArg.getRows()) {
                                    scenarioBuilder.append("\t\t");
                                    for (TableCell cell : row.getCells()) {
                                        scenarioBuilder.append("|").append(cell.getValue());
                                    }
                                    scenarioBuilder.append("|\n");
                                }
                                scenarioBuilder.append("\n");
                            }
                        }
                        if (scenarioKeyword.trim().equalsIgnoreCase("Scenario Outline")) {
                            List<List<String>> exampleList = testDataSet.get(scenarioName);
                            scenarioBuilder.append("\n\t\t").append("Examples: \n");
                            for (List<String> example : exampleList) {
                                scenarioBuilder.append("\t\t\t");
                                for (String entry : example) {
                                    scenarioBuilder.append("|").append(entry);
                                }
                                scenarioBuilder.append("|\n");
                            }
                        }
                        builder.append(scenarioBuilder);
                        logMan.info(scenarioName + " - Generated with data");
                    }
                }

                if (builder.length() > 0) {
                    builder.insert(0, featureBuilder);
                    File outputFile = new File(featurePaths.get(documents.indexOf(gherkinDocument)).replace(
                            new File(featureRootFolderPath).getAbsolutePath(),
                            new File(featureTargetFolder).getAbsolutePath()));
                    outputFile.getParentFile().mkdirs();
                    FileWriter writer = new FileWriter(outputFile);
                    writer.write(builder.toString());
                    writer.close();

                    logMan.info("Feature file generated: " + outputFile.getAbsolutePath());
                } else {
                    logMan.info("No Scenario of this feature is marked for execution");
                }
            } catch (Exception e) {
                logMan.error("Feature Parsing Failed..", e);
            }
        }
        return listSelectedTestCases;
    }

    private List<GherkinDocument> getEnvelopes(List<String> featurePaths) {
        List<GherkinDocument> docList = new ArrayList<GherkinDocument>();
        for (String path : featurePaths) {
            try {
                File file = new File(path);
                String data = FileUtils.readFileToString(file, "UTF-8");
                TokenMatcher matcher = new TokenMatcher("en");
                Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
                GherkinDocument gherkinDocument = parser.parse(data, matcher);
                docList.add(gherkinDocument);
            } catch (Exception e) {
            }
        }
        return docList;
    }

    private Map<String, List<List<String>>> getTestData(String testDataPath) {
        Properties dataConfig = getTestDataConfig();
//		String dataSheetName = dataConfig.getProperty("TESTDATATAB");
        String scenarioColHeader = dataConfig.getProperty("KEY_COLUMN_NAME1");
        String execFlagColHeader = dataConfig.getProperty("EXECUTION_COLUMN");

        File testdataFile = new File(testDataPath);
        File[] fileList = new File[] { testdataFile };
        if (testdataFile.isDirectory()) {
            fileList = testdataFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase().endsWith(".xlsx");
                }
            });
        }
        Map<String, List<List<String>>> dataSet = new HashMap<String, List<List<String>>>();
        for (File dataFile : fileList) {
            logMan.info("Parsing - File : " + dataFile.getAbsolutePath());
            ExcelUtils excelReader = new ExcelUtils(dataFile.getAbsolutePath());

            for (String dataSheetName : excelReader.getAllSheetNames()) {
                logMan.info("Parsing - Sheet : " + dataSheetName);
                Map<String, List<List<String>>> tmpDataSet = excelReader.getTestScenarioDataSet(dataSheetName,
                        scenarioColHeader, execFlagColHeader);
                dataSet.putAll(tmpDataSet);
            }
        }
        return dataSet;
    }

    public Properties getTestDataConfig() {
        Properties properties = new Properties();
        try {
            InputStream fis = new FileInputStream(
                    System.getProperty("user.dir") + "/src/test/resources/testData/TestDataConfig.properties");
            properties.load(fis);
            fis.close();
        } catch (Exception e) {
            logMan.error("");
        }
        return properties;
    }
}