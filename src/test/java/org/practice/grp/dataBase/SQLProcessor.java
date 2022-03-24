package org.practice.grp.dataBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * Created by C115190 on 04/30/2020.
 */
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.httprpc.io.JSONEncoder;
import org.httprpc.sql.ResultSetAdapter;
import org.practice.grp.base.BasePage;
import org.practice.grp.utilities.Crypter;

public class SQLProcessor extends BasePage {
    private CRUD currentOperation = CRUD.READ;
    private String connectionUrl;
    private boolean connectionInitiated = false;
    //	private Connection connection;
    private DataBaseType dataBaseType;
    private String dbHost;
    private String dbUser;
    private String dbPassword;
    private String dbName;
    private Object outcome;
    private boolean exceptionEncountered = false;
    private Throwable encounteredException = null;
    private int retryThreshold = 0;
    private int recoveryThreshold;
    private int retryCounter = 0;
//	private boolean scriptFileInExecution;

    public static List<String> scriptQueue = new ArrayList<>();

    public SQLProcessor() {
        dbUser = System.getProperty("DB.USERNAME");
        dbPassword = System.getProperty("DB.PASSWORD");
        dataBaseType = DataBaseType.INVALID_DATABASE_TYPE;
    }

    public SQLProcessor(DataBaseType dbType) {
        dbUser = System.getProperty("DB.USERNAME");
        dbPassword = System.getProperty("DB.PASSWORD");
        dataBaseType = dbType;
    }

    public SQLProcessor(String... dbConnectionParams) {
        initialize(dbConnectionParams);
    }

    public boolean isConnectionInitiated() {
        return this.connectionInitiated;
    }

//	public void setScriptFileInExecution(boolean scriptFileInExecution) {
//		this.scriptFileInExecution = scriptFileInExecution;
//	}

    public boolean isExceptionEncountered() {
        return exceptionEncountered;
    }

    public Throwable getEncounteredException() {
        return encounteredException;
    }

    public void setDataBaseType(String dbTypeName) {
        dataBaseType = DataBaseType.getDataBaseType(dbTypeName);
    }

    public void setDataBase(String dbName) {
        this.dbName = dbName;
    }

    private void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public void setCurrentOperation(CRUD currentOperation) {
        this.currentOperation = currentOperation;
    }

    protected void initialize(String... dbConnectionParams) {
        init(dbConnectionParams);
    }

    public void init() {
        init(dbHost, dbName);
    }

    private void init(String... connectionParams) {
        retryThreshold = Integer.parseInt(System.getProperty("DB.CONNECTION.RETRY.THRESHOLD", "0"));
        recoveryThreshold = Integer.parseInt(System.getProperty("DB.CONNECTION.RECOVERYTIME.THRESHOLD", "0"));
        dbHost = connectionParams[0];
        dataBaseType = dataBaseType.equals(DataBaseType.INVALID_DATABASE_TYPE)
                ? DataBaseType.getDataBaseType(System.getProperty("DB.TYPE"))
                : dataBaseType;
        try {
            String driverClass = dataBaseType.getDriverClass();
            Class.forName(driverClass);
            switch (dataBaseType) {
                case MS_SQL_SERVER:
                    setConnectionUrl(String.format(dataBaseType.getConnectionURL(), dbHost, dbName, "", ""));
                    break;
                case TERADATA:
                    setConnectionUrl(String.format(dataBaseType.getConnectionURL(), dbHost));
                    break;
                default:
                    throw new SQLException(dataBaseType.toString() + " has not been implemented yet");
            }
//			if (dbName != null) {
//				initDataBaseConnection();
//			}
        } catch (Exception e) {
            exceptionEncountered = true;
            encounteredException = e;
            e.printStackTrace();
            logMan.error("Failed to capture connection properties", e);
            return;
        }
        this.connectionInitiated = true;
        logMan.info("Connection properties captured for " + dataBaseType.toString());
    }

    protected Object execute(boolean isStoredProc, String queryString, Object[] queryParams) throws Exception {
        if (!isConnectionInitiated()) {
            init();
        }

        String queryToBeExecuted = queryString;
        if (isStoredProc) {
            queryToBeExecuted = String
                    .format("Call %s(%s)", queryString, String.join("", Collections.nCopies(queryParams.length, "?, ")))
                    .trim();
            queryToBeExecuted = queryParams.length > 0 ? queryToBeExecuted.substring(0, queryToBeExecuted.length())
                    : queryToBeExecuted;
        }
        return isStoredProc ? executeStoredProcedure(queryString, queryParams) : executeQuery(queryString, queryParams);
    }

    private Object executeStoredProcedure(String storedProcName, Object... queryParams) throws Exception {
        Object outcome = null;
        try (Connection connection = getConnection()) {
            String queryString = String.format("Call %s(%s)", storedProcName,
                    String.join("", Collections.nCopies(queryParams.length, "?, "))).trim();
            queryString = queryParams.length > 0 ? queryString.substring(0, queryString.length()) : queryString;

            executeQuery(connection, CallableStatement.class, queryString, queryParams);
        } catch (SQLException e) {
            throw e;
        }
        return outcome;
    }

    private Object executeQuery(String queryString, Object... queryParams) throws Exception {
        try (Connection connection = getConnection()) {
            executeQuery(connection, PreparedStatement.class, queryString, queryParams);
        } catch (Exception e) {
            throw e;
        }
        return outcome;
    }

    protected Object executeQuery_Thread(String queryString, Object... queryParams) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try (Connection connection = getConnection()) {
                    executeQuery(connection, PreparedStatement.class, queryString, queryParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return outcome;
    }

    private Connection getConnection() throws Exception {
        logMessage("Connection Setup");
        Connection connection;
        while (true) {
            try {
                if (dataBaseType.isUserDetailsRequired()) {
                    dbUser = Crypter.decrypt(dbUser);
                    dbPassword = Crypter.decrypt(dbPassword);
                    connection = DriverManager.getConnection(connectionUrl, dbUser, dbPassword);
                } else
                    connection = DriverManager.getConnection(connectionUrl);
                break;
            } catch (Exception e) {
                if (retryCounter >= retryThreshold) {
                    exceptionEncountered = true;
                    encounteredException = e;
                    e.printStackTrace();
                    logMan.error("Execution failed", e);
                    throw e;
                } else {
                    retryCounter++;
                    logMan.warn("Connection failed. Giving the system " + recoveryThreshold + " seconds to recover...");
                    Thread.sleep(recoveryThreshold * 1000);
                    logMan.warn("Re-executing the query. Attempt #" + retryCounter);
                }
            }
        }
        retryCounter = 0;

        logMessage("Connection Established");
        return connection;
    }

    private <T extends PreparedStatement> void executeQuery(Connection connection, Class<T> statementClass,
                                                            String queryString, Object[] queryParams) throws Exception {
        try (T statement = getStatement(connection, statementClass, queryString, queryParams)) {
            switch (currentOperation) {
                case READ:
                    long startTime = System.currentTimeMillis();
                    try (ResultSet resultSet = statement.executeQuery()) {
                        long endTime = System.currentTimeMillis();
                        long execuDuration = endTime - startTime;
                        logMan.info(String.format("Query Execution Time: %.2f seconds", (float) execuDuration / 1000));
                        outcome = transformResultSet(resultSet);
                    }
                    break;
                default:
                    outcome = statement.executeUpdate();
            }
        } catch (Exception e) {
            exceptionEncountered = true;
            encounteredException = e;
            e.printStackTrace();
            logMan.error("SQL Script Execution failed - " + System.getProperty("QUERY_NAME", ""), e);
            FileUtils.write(
                    new File(System.getProperty("user.dir") + "/" + System.getProperty("RESULTSET.DIR")
                            + "/Failed_Query_List"),
                    System.getProperty("QUERY_NAME", "") + "~" + e.getLocalizedMessage() + "\n", StandardCharsets.UTF_8,
                    true);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PreparedStatement> T getStatement(Connection connection, Class<T> statementClass,
                                                         String queryString, Object[] queryParams) throws SQLException {
        T statement = null;
        String statementClassName = statementClass.getSimpleName();

        if (statementClassName.equals("CallableStatement"))
            statement = (T) connection.prepareCall(queryString, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        else if (statementClassName.equals("PreparedStatement"))
            statement = (T) connection.prepareStatement(queryString, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(50);
        if (queryParams != null && queryParams.length > 0) {
            return setQueryParam(statement, queryParams);
        }
        return statement;
    }

    public <T extends PreparedStatement> T setQueryParam(T statement, Object... queryParams) throws SQLException {
        int queryParamIndex = 0;
        for (Object queryParam : queryParams) {
            String paramType = queryParam.getClass().getSimpleName();
            switch (paramType) {
                case "Integer":
                    statement.setInt(++queryParamIndex, (int) queryParam);
                    break;
                case "BigDecimal":
                    statement.setBigDecimal(++queryParamIndex, (BigDecimal) queryParam);
                    break;
                case "Float":
                    statement.setFloat(++queryParamIndex, (float) queryParam);
                    break;
                case "Double":
                    statement.setDouble(++queryParamIndex, (double) queryParam);
                    break;
                case "Long":
                    statement.setLong(++queryParamIndex, (long) queryParam);
                    break;
                case "Boolean":
                    statement.setBoolean(++queryParamIndex, (boolean) queryParam);
                    break;
                case "Date":
                    statement.setDate(++queryParamIndex, (Date) queryParam);
                    break;
                case "String":
                    statement.setString(++queryParamIndex, String.valueOf(queryParam));
                    break;
                default:
                    throw new SQLException(paramType + " query parameter has not been implemented yet");
            }
        }
        return statement;
    }

    ResultSetData transformResultSetToCollection_NotToUse(ResultSet result) throws SQLException {
        try {
            ResultSetData resultSetData = new ResultSetData();
            if (result.isBeforeFirst()) {
                ResultSetMetaData rsMetaData = result.getMetaData();
                while (result.next()) {
                    for (int counter = 1; counter <= rsMetaData.getColumnCount(); counter++) {
                        String columnName = rsMetaData.getColumnName(counter);
                        int columnDataType = rsMetaData.getColumnType(counter);
                        switch (columnDataType) {
                            case Types.VARCHAR:
                            case Types.NVARCHAR:
                            case Types.LONGVARCHAR:
                            case Types.LONGNVARCHAR:
                                resultSetData.pushData(columnName, result.getString(columnName), columnDataType);
                                break;
                            case Types.INTEGER:
                            case Types.NUMERIC:
                                resultSetData.pushData(columnName, result.getInt(columnName), columnDataType);
                                break;
                            case Types.DOUBLE:
                            case Types.DECIMAL:
                                resultSetData.pushData(columnName, result.getDouble(columnName), columnDataType);
                                break;
                            case Types.FLOAT:
                                resultSetData.pushData(columnName, result.getFloat(columnName), columnDataType);
                                break;
                            case Types.DATE:
                                resultSetData.pushData(columnName, result.getDate(columnName), columnDataType);
                                break;
                            case Types.BOOLEAN:
                                resultSetData.pushData(columnName, result.getBoolean(columnName), columnDataType);
                                break;
                            default:
                                throw new SQLException(
                                        rsMetaData.getColumnTypeName(counter) + " has not been implemented yet");
                        }
                    }
                    resultSetData.processRecord();
                }
            }
            return resultSetData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ResultSetData transformResultSet(ResultSet result) throws SQLException {
        try {
            ResultSetData resultSetData = new ResultSetData();
            resultSetData.upddateMetaData(result);
            try (ResultSetAdapter resultSetAdapter = new ResultSetAdapter(result)) {
                JSONEncoder jsonEncoder = new JSONEncoder();
                FileOutputStream fos = new FileOutputStream(resultSetData.getTempJsonFile());
                jsonEncoder.write(resultSetAdapter, fos);
                fos.close();
                if (System.getProperty("DB.ATTACH.QUERYRESULT").equals("TRUE")) {
                    resultSetData.generateCSV();
                }
            }
            return resultSetData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

//	protected void releaseConnection() {
//		try {
//			if (connection != null || !connection.isClosed()) {
//				connection.close();
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		connection = null;
//	}

    protected enum CRUD {
        CREATE, READ, UPDATE, DELETE;
    }
}