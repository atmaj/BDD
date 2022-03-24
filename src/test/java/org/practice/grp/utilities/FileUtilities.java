package org.practice.grp.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Created by C112083 on 13/10/2020.
 */
public class FileUtilities {
    Logger logMan;

    public FileUtilities() {
        logMan = LogManager.getInstance();
    }

    public void GenerateConsolidatedFailuresList() {
        logMan.info("GenerateConsolidatedFailuresList Function - Start");
        try {
            File[] files = new File(System.getProperty("user.dir") + "\\target\\Failures\\").listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    File tempFile = new File(file.getAbsolutePath() + "\\Failure.txt");
                    String ValueInFailure = ReadFromTextFile(tempFile);
                    if (!(ValueInFailure == null)) {
                        WriteToFinalFailureLog(ValueInFailure);
                    }

                } else {
                    System.out.println("File: " + file.getName());
                    logMan.info("Info - " + "File: " + file.getName());
                }
            }

        } catch (Throwable t) {
            System.out.println("Error Occured inside the GenerateConsolidatedFailuresList function, Error Message ="
                    + t.getMessage());
            logMan.error("Error Occured inside the GenerateConsolidatedFailuresList function, Error Message ="
                    + t.getMessage());
        }
        logMan.info("GenerateConsolidatedFailuresList Function - End");
    }

    public String ReadFromTextFile(File file) {
        String returvalue = "";
        logMan.info("ReadFromTextFile Function - Start");
        try {
            FileReader Fr;
            BufferedReader Br;
            Fr = new FileReader(file);
            Br = new BufferedReader(Fr);
            String FailureString = Br.readLine();
            Br.close();
            returvalue = FailureString;
        } catch (Throwable t) {
            System.out.println("Error Occured Reading file, in function ReadFromTextFile " + t.getMessage());
            logMan.error("Error Occured Reading file, in function ReadFromTextFile " + t.getMessage());
            // return "";
        }
        logMan.info("ReadFromTextFile Function - END");
        return returvalue;
    }

    public void WriteToFinalFailureLog(String FailureData) {
        logMan.info("WriteToFinalFailureLog Function - Start");
        try {
            if (!(FailureData == "")) {
                Boolean NeedSeparator = true;
                File file = new File(
                        System.getProperty("user.dir") + "\\target\\Failures\\ConsolidatedFailuresList.txt");
                if (!file.exists()) {
                    file.createNewFile();
                    NeedSeparator = false;
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);
                if (NeedSeparator == true) {
                    bw.write(" ");
                }
                bw.write(FailureData);
                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
            System.out.println("Error Occured in WriteToFinalFailureLog function");
            logMan.error("Error Occured in WriteToFinalFailureLog function");
            e.printStackTrace();
        }
        logMan.info("WriteToFinalFailureLog Function - End");
    }

    public boolean FileExists(String path) {
        logMan.info("FileExists Function - Start");
        boolean returnvalue = false;
        try {
            File f = new File(path);
            if (f.exists()) {
                returnvalue = true;
            }
        } catch (Throwable t) {
            System.out.println("Error occured in FileExists , file path= " + path + " ,Error Desc = " + t.getMessage());
            logMan.error("Error occured in FileExists , file path= " + path + " ,Error Desc = " + t.getMessage());
            // returnvalue= false;
        }
        logMan.info("FileExists Function - END");
        return returnvalue;
    }

    public boolean Deletefile(String path) {
        logMan.info("Deletefile Function - Start");
        boolean returnvalue = false;
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
                returnvalue = true;
            }
        } catch (Throwable t) {
            System.out.println("Error occured in Deletefile , file path= " + path + " ,Error Desc = " + t.getMessage());
            logMan.error("Error occured in Deletefile , file path= " + path + " ,Error Desc = " + t.getMessage());
        }
        logMan.info("Deletefile Function - END");
        return returnvalue;
    }

    public void DeleteFailedJsonFiles(String ConsolidatedFailureListdocumentPath) {
        logMan.info("DeleteFailedJsonFiles Function - Start");
        try {
            FileUtilities fileUtil = new FileUtilities();
            String AllFailures;
            AllFailures = fileUtil
                    .ReadFromTextFile(new File(System.getProperty("user.dir") + ConsolidatedFailureListdocumentPath));
            System.out.println(AllFailures);
            String[] FailureArray = AllFailures.split(" ");
            System.out.println(FailureArray.length);
            for (int FailureStringNo = 0; FailureStringNo < FailureArray.length; FailureStringNo++) {
                String IndividualFailurestring = FailureArray[FailureStringNo];
                System.out.println(IndividualFailurestring);
                logMan.info(IndividualFailurestring);
                String[] IndividualFailuresArray = IndividualFailurestring.split("/");
                String FailedScenarioData = IndividualFailuresArray[IndividualFailuresArray.length - 1];
                System.out.println("FailedScenarioData =" + FailedScenarioData);
                logMan.info("FailedScenarioData =" + FailedScenarioData);
                String[] FailedScenarioDataArray = FailedScenarioData.split(":");
                String ExactFailedScenario = FailedScenarioDataArray[0].replace(".feature", "");
                System.out.println(ExactFailedScenario);
                logMan.info(ExactFailedScenario);
                String JsonReportPath = System.getProperty("user.dir") + "\\target\\JSONReports\\" + ExactFailedScenario
                        + ".json";
                if (fileUtil.FileExists(JsonReportPath)) {

                    fileUtil.Deletefile(JsonReportPath);
                    System.out.println("Deleted json file =" + ExactFailedScenario);
                    logMan.info("Deleted json file =" + ExactFailedScenario);
                }
            }
        } catch (Throwable t) {
            System.out.println("Error Occured in DeleteFailureJsonFiles function, error desc =" + t.getMessage());
            logMan.error("Error Occured in DeleteFailureJsonFiles function, error desc =" + t.getMessage());
        }
        logMan.info("DeleteFailedJsonFiles Function - End");
    }

    public String matchExpressionInaFile(String Filepath, String RegExpression) {
        logMan.info("matchExpressionInaFile Function - Start");
        String returnvalue = "";
        BufferedReader br;
        StringBuilder sb;
        String line;
        String everything;
        try {
            br = new BufferedReader(new FileReader(Filepath));

            sb = new StringBuilder();
            line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
            Pattern regexp = Pattern.compile(RegExpression);
            Matcher matcher = regexp.matcher(everything);
            int matchfound = 0;
            while (matcher.find()) {

                System.out.println("Regular Expression =" + RegExpression);
                System.out.println(("Match:" + (matchfound + 1) + "=") + matcher.group(matchfound));
                logMan.info("Regular Expression =" + RegExpression);
                logMan.info(("Match:" + (matchfound + 1) + "=") + matcher.group(matchfound));

                if (returnvalue == "") {
                    returnvalue = matcher.group(matchfound);
                } else {
                    returnvalue = returnvalue + "/n" + matcher.group(matchfound);
                }
            }

        } catch (Throwable t) {
            logMan.error("Error Occured in matchExpressionInaFile function, error desc =" + t.getMessage());
        }
        logMan.info("matchExpressionInaFile Function - End");
        return returnvalue;
    }

    public String returnFileAsASingleString(String FilePath) {
        BufferedReader br;
        StringBuilder sb;
        String line;
        String everything = "";
        try {
            br = new BufferedReader(new FileReader(FilePath));
            sb = new StringBuilder();
            line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
            br.close();
        } catch (Throwable t) {
            System.out.println("Error Occured Inside returnFileAsASingleString function for path =" + FilePath);
        }
        return everything;
    }

    public boolean CreateFileInResources(String FileContent, String FileName) {
        logMan.info("CreateFile function - START");
        boolean returnvalue = false;
        try {
            File file = new File(System.getProperty("user.dir") + "//src//test//resources//" + FileName);
            if (file.exists()) {
                file.delete();
                logMan.info("Info= File" + FileName + " already exists , hence deleted it");
            }
            file.createNewFile();
            logMan.info("Info= A New File as " + FileName + " is created successfully");
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(FileContent);
            bw.flush();
            bw.close();
            if (file.exists()) {
                logMan.info("Info= File" + FileName + " is created successfully");
            } else {
                logMan.error("Error= File" + FileName + " creation failed");
            }

        } catch (Throwable t) {
            logMan.error("Error Occured inside CreateFile function while creating a file with name " + FileName);
        }
        logMan.info("CreateFile function - END");
        return returnvalue;
    }

}