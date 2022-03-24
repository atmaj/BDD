package org.practice.grp.dataBase;

/**
 * Created by C115190 on 04/30/2020.
 */
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;

import io.cucumber.datatable.DataTable;

public class ResultSetData {
    private int totalRecordCount = -1;
    private int totalColumnCount;
    private ArrayList<ResultSetRecord> resultSetData = new ArrayList<ResultSetRecord>();
    private ResultSetRecord currentRecord = null;
    private HashMap<String, Integer> metadataMap = new HashMap<String, Integer>();
    private List<String> columnList = new ArrayList<>();
    private List<List<String>> dataTableRaw = new ArrayList<>();
    private ArrayList<ResultSetRecord> filteredResultSet = new ArrayList<ResultSetRecord>();
    private String filterErrors = "";
    // private String timeStamp;
    private File tempJsonFile, filterJsonFile, tempFolder, resultsetDumpFolder, fullDumpCSV, filteredCSV;
    //	private boolean resultFiltered;
    // private boolean filteredData;
    private int filteredRecordCount = 0;

    public ResultSetData() throws IOException {
        // timeStamp = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new
        // java.util.Date());
        tempFolder = new File(System.getProperty("user.dir") + "/" + System.getProperty("RESULTSET.DIR.TEMP") + "/");
        resultsetDumpFolder = new File(System.getProperty("user.dir") + "/" + System.getProperty("RESULTSET.DIR")
                + (System.getProperty("QUERY_NAME", "").length() > 0 ? "/" + System.getProperty("QUERY_NAME") + "/"
                : "/"));
        tempFolder.mkdirs();
        resultsetDumpFolder.mkdirs();
        tempJsonFile = System.getProperty("SCRIPT_EXEC", "").equalsIgnoreCase("Y")
                ? new File(resultsetDumpFolder + "/" + System.getProperty("QUERY_NAME") + ".json")
                : File.createTempFile("rsData", ".json", tempFolder);
        filterJsonFile = File.createTempFile("rsData", ".json", tempFolder);
        fullDumpCSV = File.createTempFile("rsData_FULL_", ".csv", resultsetDumpFolder);
        filteredCSV = File.createTempFile("rsData_FILTERED_", ".csv", resultsetDumpFolder);
    }

    public int getFilteredRecordCount() {
        return filteredRecordCount;
    }

    public File getTempJsonFile() {
        return tempJsonFile;
    }

    public int getTotalRecordCount() {
        return totalRecordCount;
    }

    public int getTotalColumnCount() {
        return totalColumnCount;
    }

    public ArrayList<ResultSetRecord> getFilteredResultSet() {
        return filteredResultSet;
    }

    public boolean isResultFiltered() {
        return filteredRecordCount > 0;
    }

    public boolean isErrorEncounteredInFilter() {
        return filterErrors.length() != 0;
    }

    public String getFilterErrors() {
        return filterErrors;
    }

    public boolean isEmpty() {
        return totalRecordCount == 0;
    }

    public void pushData(String columnName, Object data, int dataType) {
        if (currentRecord == null) {
            currentRecord = new ResultSetRecord();
        }
        metadataMap.put(columnName, dataType);
        columnList.add(columnName);
        currentRecord.addData(columnName, (data == null ? "" : data));
    }

    public void processRecord() {
        List<String> tableEntry = new ArrayList<String>();
        resultSetData.add(currentRecord);
        totalRecordCount++;
        totalColumnCount = metadataMap.size();
        for (String column : columnList) {
            tableEntry.add(String.valueOf(currentRecord.getData(column)));
        }
        dataTableRaw.add(tableEntry);
        currentRecord = null;
    }

    public ResultSetRecord getRecord(int recordIndex) {
        return resultSetData.get(recordIndex);
    }

    public List<Object> getData(String columnName) {
        ArrayList<Object> columnData = new ArrayList<Object>();
        for (ResultSetRecord record : resultSetData) {
            columnData.add(record.getData(columnName));
        }
        return Collections.unmodifiableList(columnData);
    }

    public Object getData(int recordIndex, String columnName) {
        return resultSetData.get(recordIndex).getData(columnName);
    }

    public Object getData(int recordIndex, int columnIndex) {
        String columnName = String.valueOf(metadataMap.keySet().toArray()[columnIndex]);
        return getData(recordIndex, columnName);
    }

    public DataTable getDataTable() {
        dataTableRaw.add(0, columnList);
        return DataTable.create(dataTableRaw);
    }

    public void filterRecords(String filterData) {
        filterRecords(null, filterData);
    }

    public void filterRecords(String sourceFileName, String columns, String filterData) {
        resultsetDumpFolder = new File(System.getProperty("user.dir") + "/" + System.getProperty("RESULTSET.DIR") + "/"
                + sourceFileName + "/");
//			tempJsonFile.delete();
        tempJsonFile = new File(resultsetDumpFolder + "/" + sourceFileName + ".json");
        totalRecordCount = ResultSetJsonUtil.isRecordsAvailableInJson(tempJsonFile) ? 1 : 0;
        columnList = ResultSetJsonUtil.getColumnList(tempJsonFile);

        fullDumpCSV.delete();
        filteredCSV.delete();
        try {
//			fullDumpCSV = File.createTempFile("rsData_FULL_", ".csv", resultsetDumpFolder);
            filteredCSV = File.createTempFile("rsData_FILTERED_", ".csv", resultsetDumpFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        filterRecords(columns, filterData);
    }

    public void filterRecords(String filterColumn, String filterData) {
        if (totalRecordCount > 0) {
//			if (columnList.stream().map(columnName -> columnName.trim()).anyMatch(filterColumn::equalsIgnoreCase)) {
            if (columnList.contains(filterColumn)) {
                filteredRecordCount = ResultSetJsonUtil.filterJson(tempJsonFile, filteredCSV, 5, filterColumn,
                        filterData);
            } else {
                filterErrors = filterColumn + " is not available in the resultset. Check your test data\n";
                String potentialColumn = columnList.stream()
                        .filter(columnName -> columnName.equalsIgnoreCase(filterColumn)).findAny().orElse("");
                if (!potentialColumn.isEmpty())
                    filterErrors += "Potential Column: " + potentialColumn;
            }
        } else {
            filterErrors = "Query didn't return any resultset. Check the script";
        }
    }

    public String getFilteredCSV() {
        return filteredCSV.getPath();
    }

    public String generateCSV() {
        return generateCSV(false);
    }

    public String generateCSV(boolean filteredSetOnly) {
        File targetFile = filteredSetOnly ? filterJsonFile : tempJsonFile;
        File resultFile = filteredSetOnly ? filteredCSV : fullDumpCSV;
        try {
            JsonNode jsonTree = new ObjectMapper().readTree(targetFile);
            Builder csvBuilder = CsvSchema.builder();
            JsonNode first = jsonTree.elements().next();
            first.fieldNames().forEachRemaining(fieldName -> {
                csvBuilder.addColumn(fieldName);
            });
            CsvSchema csvSchema = csvBuilder.build().withHeader();

            CsvMapper csvMapper = new CsvMapper();
            csvMapper.writerFor(JsonNode.class).with(csvSchema).writeValue(resultFile, jsonTree);
            return (resultFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class ResultSetRecord {
        private HashMap<String, Object> recordData = new HashMap<String, Object>();

        public int getRecordSize() {
            return recordData.size();
        }

        public boolean isDataAvailable(String columnName) {
            return recordData.containsKey(columnName);
        }

        public Object getData(String columnName) {
            return recordData.getOrDefault(columnName, null);
        }

        private void addData(String columnName, Object data) {
            recordData.put(columnName, data);
        }
    }

    public void upddateMetaData(ResultSet result) throws JSONException, SQLException, IOException {
        result.last();
        totalRecordCount = result.getRow();
        result.beforeFirst();

        ResultSetMetaData rsMetadata = result.getMetaData();
        totalColumnCount = rsMetadata.getColumnCount();
        for (int counter = 1; counter <= totalColumnCount; counter++) {
            String columnName = rsMetadata.getColumnName(counter);
            int columnType = rsMetadata.getColumnType(counter);
            columnList.add(columnName);
            metadataMap.put(columnName, columnType);
        }

        if (totalRecordCount <= 0) {
            FileUtils.write(
                    new File(System.getProperty("user.dir") + "/" + System.getProperty("RESULTSET.DIR")
                            + "/Failed_Query_List"),
                    System.getProperty("QUERY_NAME", "") + "~Query didn't return any resultset\n",
                    StandardCharsets.UTF_8, true);
        }
    }

    public void clearTempFiles() {
//		if (!System.getProperty("SCRIPT_EXEC", "").equalsIgnoreCase("Y")) {
////			tempJsonFile.delete();
//		}
//		filterJsonFile.delete();
    }
}