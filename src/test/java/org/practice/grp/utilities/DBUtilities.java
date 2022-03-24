package org.practice.grp.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Created by C112083 on 02/12/2020.
 */
public class DBUtilities {
    private static DBUtilities instance;
    private Connection connection = null;
    public Logger logman;
    private Statement statement;
    private static final int ROW_MAX_LENGTH = 100;
    private static final String COL_DELIM = "|";
    private static final String CSV_COL_DELIM = "\t,";

    private DBUtilities(String url, String userName, String password) {
        logman = LogManager.getInstance();
        try {
            switch (System.getProperty("DB_TYPE").toUpperCase()) {
                case "ORACLE":
                    Class.forName("oracle.jdbc.driver.OracleDriver");
                    break;
                case "MYSQL":
                    //
                    break;
                case "TERADATA":
                    Class.forName("com.teradata.jdbc.TeraDriver");
                    break;

            }
            connection = DriverManager.getConnection(url, userName, password);
            statement = connection.createStatement();
        } catch (Exception e) {
            logman.error("Error in loading class for DBUtilities = " + System.getProperty("DB_TYPE"));
        }
    }

    public static DBUtilities getInstance() {
        if (instance == null) {
            instance = new DBUtilities("", "", "");
        } else
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DBUtilities("", "", "");
                }
            } catch (SQLException e) {
                System.out.println("Error in connection , Error = " + e.getMessage());
            }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public String runSqlQuery(String sqlQuery) throws SQLException {
        String result = "";
        try {
            ResultSet resultData = runSqlQueryToSet(sqlQuery);
            result = Joiner.on("\n").join(asMaps(resultData));
            logman.debug("Result of query = " + sqlQuery + " is = " + result);
        } catch (SQLException e) {
            logman.error("Execution of query = " + sqlQuery + " is failed , Error = " + e.getMessage());
        }
        return result;
    }

    // get resultset
    public ResultSet runSqlQueryToSet(String sqlQuery) throws SQLException {
        ResultSet resultData = null;
        try {
            Statement statement = connection.createStatement();
            logman.info("Running query {} on database {}, query = " + sqlQuery);
            resultData = statement.executeQuery(sqlQuery);
        } catch (SQLException e) {
            logman.error("Error in getting ResulSet, Error = " + e.getMessage());
        }
        return resultData;
    }

    public List<Map<String, Object>> resultSetToArrayList(ResultSet rs) throws SQLException {
        try {
            List<Map<String, Object>> list = null;
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            list = new ArrayList<Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<String, Object>(columns);
                for (int i = 1; i <= columns; ++i) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(row);
            }
            return list;
        } catch (SQLException e) {
            logman.error("Error in adding data on Method resultSetToArrayList, Message = " + e.getMessage());
        }
        logman.error("Data not added in the form of List of Map in Method resultSetToArrayList");
        return null;
    }

    private List<Map<String, String>> asMaps1(ResultSet queryData) throws SQLException {
        List<Map<String, Object>> result = null;
        try {
            BasicRowProcessor processor = new BasicRowProcessor();
            result = new ArrayList<Map<String, Object>>();
            while (queryData.next()) {
                Map<String, Object> res = processor.toMap(queryData);
                result.add(res);
            }
        } catch (SQLException e) {
            logman.error("Error in converting ResultSet to list of map of type String, Error = " + e.getMessage());
        }
        return toStringMap(result);
    }

    private List<Map<String, String>> toStringMap(List<Map<String, Object>> m) {
        List<Map<String, String>> res = new ArrayList<Map<String, String>>();
        try {
            for (Map<String, Object> row : m) {
                Map<String, String> copy = row.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
                res.add(copy);
            }
        } catch (Exception e) {
            logman.error("Error in converting list of map of type Object to list of map of type String, Error = "
                    + e.getMessage());
        }
        return res;
    }

    public List<Map<String, Object>> asMaps(ResultSet queryData) throws SQLException {
        BasicRowProcessor processor = new BasicRowProcessor();
        List<Map<String, Object>> result = Lists.newArrayList();
        while (queryData.next()) {
            Map<String, Object> res = processor.toMap(queryData);
            result.add(res);
        }
        return result;
    }

    public void runUpdateQueryImpl(String sql) {
        try {
            logman.info("SQL Query : " + sql);
            statement.executeUpdate(sql);
            connection.close();
        } catch (Exception e) {
            logman.error("Error running query: \n" + sql + "\n Error , = " + e.getMessage());
        }
    }

    public void runBulkUpdateQueryImpl(List<String> sql) {
        try {
            for (String query : sql) {
                statement.addBatch(query);
            }
            statement.executeBatch();
            statement.close();
            connection.close();
            logman.info("Bulk update is successful");
        } catch (SQLException e) {
            logman.error("Error running query: \n" + sql + "\n" + e);
        }
    }

    private List<Integer> findLength(List<Map<String, String>> table) {
        List<Integer> maxLengths = new ArrayList<Integer>();
        try {
            for (String key : table.get(0).keySet()) {
                int len = key.length();
                if (len > ROW_MAX_LENGTH) {
                    len = ROW_MAX_LENGTH;
                }
                maxLengths.add(len);
            }
            int cnt = 0;
            for (Map<String, String> row : table) {
                cnt = 0;
                for (String value : row.values()) {
                    Integer curr = maxLengths.get(cnt);
                    if (curr == ROW_MAX_LENGTH) {
                        continue;
                    }
                    int len = value.length();
                    if (len > ROW_MAX_LENGTH) {
                        maxLengths.set(cnt, ROW_MAX_LENGTH);
                    } else if (len > curr) {
                        maxLengths.set(cnt, len);
                    }
                    cnt++;
                }
            }
        } catch (Exception e) {
            logman.error("Error in finding length, Error = " + e.getMessage());
        }
        return maxLengths;
    }

    private String createTable(List<Map<String, String>> table) {
        try {
            if (table.isEmpty() || table.get(0).isEmpty()) {
                return "";
            }
            List<Integer> maxLengths = findLength(table);
            StringBuilder sb = new StringBuilder();
            String tableHeaders = createRow(maxLengths, new ArrayList<Object>(table.get(0).keySet()));
            sb.append(tableHeaders);
            for (Map<String, String> row : table) {
                sb.append(createRow(maxLengths, new ArrayList<Object>(row.values())));
            }
            logman.debug("data = " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            logman.error("Error in Creating table, Error = " + e.getMessage());
            return "";
        }
    }

    private String createRow(List<Integer> colMaxLengths, List<Object> elements) {
        try {
            StringBuilder res = new StringBuilder();
            res.append(COL_DELIM);
            for (int i = 0; i < elements.size(); i++) {
                String val = elements.get(i).toString();
                if (val.length() > colMaxLengths.get(i)) {
                    val = val.substring(0, colMaxLengths.get(i) - 2) + "...";
                    res.append(val);
                } else {
                    res.append(val);
                    res.append(getSpaces(colMaxLengths.get(i) - val.length()));
                }
                res.append(COL_DELIM);
            }
            logman.debug("Create Row data = " + res.toString());
            res.append("\n");
            return res.toString();
        } catch (Exception e) {
            logman.error("Error in Creating row, Error = " + e.getMessage());
            return "";
        }
    }

    private String createRow(List<Integer> colMaxLengths, List<Object> elements, String delim) {
        try {
            StringBuilder res = new StringBuilder();
            // res.append(delim);
            for (int i = 0; i < elements.size(); i++) {
                String val = elements.get(i).toString();
                if (val.length() > colMaxLengths.get(i)) {
                    val = val.substring(0, colMaxLengths.get(i) - 2) + "...";
                    res.append(val);
                } else {
                    res.append(val);
                    res.append(getSpaces(colMaxLengths.get(i) - val.length()));
                }
                res.append(delim);
            }
            res.append("\n");
            logman.debug("Create Row data with delimiter = " + res.toString());
            return res.toString();
        } catch (Exception e) {
            logman.error("Error in Creating row, Error = " + e.getMessage());
            return "";
        }
    }

    private String getSpaces(int len) {
        StringBuilder spaces = new StringBuilder();
        try {
            for (int i = 0; i <= len; i++) {
                spaces.append(" ");
            }
        } catch (Exception e) {
            logman.error("Error in getSpaces, Error = " + e.getMessage());
        }
        return spaces.toString();
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logman.error("Error closing db connection! , Error = " + e.getMessage());
            }
        }
    }

    private void close(Statement stat) {
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                logman.error("Error closing statement! , Error = " + e.getMessage());
            }
        }
    }

    private void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logman.error("Error closing ResultSet! , Error = " + e.getMessage());
            }
        }
    }

    public String createCSVTable(List<Map<String, String>> table) {
        try {
            if (table.isEmpty() || table.get(0).isEmpty()) {
                return "";
            }
            List<Integer> maxLengths = findLength(table);
            String tableHeaders = createRow(maxLengths, new ArrayList<Object>(table.get(0).keySet()), CSV_COL_DELIM);
            StringBuilder sb = new StringBuilder();
            sb.append(tableHeaders);
            for (Map<String, String> row : table) {
                sb.append(createRow(maxLengths, new ArrayList<Object>(row.values()), CSV_COL_DELIM));
            }
            logman.debug("Data for Create csv table = " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            logman.error("Error in creating CSV table, Error = " + e.getMessage());
            return "";
        }
    }

}