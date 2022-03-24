package org.practice.grp.dataBase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

public class DataBaseUtil extends SQLProcessor {
    private boolean sqlExecSkipped = false;
    private String queryExecutionException;

    public DataBaseUtil(String dbHost, String dbName, String dbUserName, String dbPassword) {
        super(dbHost, dbName, dbUserName, dbPassword);
//		scriptQueue.add("BBKG_1");
    }

    public DataBaseUtil(String dbHost, String dbUserName, String dbPassword) {
        super(dbHost, dbUserName, dbPassword);
    }

    public DataBaseUtil(String dbType) {
        super(DataBaseType.getDataBaseType(dbType));
    }

    public DataBaseUtil() {
        super();
    }

    public String getQueryExecutionException() {
        return queryExecutionException;
    }

    public boolean isScriptInQueue(String scriptName) {
        return scriptQueue.contains(scriptName);
    }

    public void initConnection(String... dbConnectionParams) {
        initialize(dbConnectionParams);
    }

    public Object executeQuery(String queryString, Object... queryParams) throws Exception {
        return execute(false, queryString, queryParams);
    }

    public Object executeQuery(String queryString) throws Exception {
        return executeQuery(queryString, new Object[0]);
    }

//	public void initConnection() throws Exception {
//		initDataBaseConnection();
//	}

//	@Override
//	public void finalize() {
//		releaseConnection();
//	}

    public boolean isQueryAlreadyExecuted(String scriptName) {
        scriptName = scriptName.replace(".sql", "");
        boolean queryAlreadyExecuted = false;
        File queryDumpFile = new File(System.getProperty("user.dir") + "/" + System.getProperty("RESULTSET.DIR") + "/"
                + scriptName + "/" + scriptName + ".json");
        if (queryDumpFile.exists()) {
            queryAlreadyExecuted = true;
        }
        return queryAlreadyExecuted;
    }

    public boolean isScriptInFailureList(String scriptName) {
        scriptName = scriptName.replace(".sql", "");
        boolean queryExecutedAndFailed = false;
        File failedScripts = new File(
                System.getProperty("user.dir") + "/" + System.getProperty("RESULTSET.DIR") + "/Failed_Query_List");

        if (failedScripts.exists()) {
            try {
                String[] failedScriptDetails = FileUtils.readFileToString(failedScripts, StandardCharsets.UTF_8)
                        .split("\n");
                for (String failedScript : failedScriptDetails) {
                    String[] failureInfo = failedScript.split("~");
                    if (failureInfo[0].trim().equalsIgnoreCase(scriptName)) {
                        queryExecutedAndFailed = true;
                        queryExecutionException = failureInfo[1].trim();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return queryExecutedAndFailed;
    }

    public void setSQLExecSkipped(boolean isSkipped) {
        sqlExecSkipped = isSkipped;
    }

    public boolean isSQLExecSkipped() {
        return sqlExecSkipped;
    }
}