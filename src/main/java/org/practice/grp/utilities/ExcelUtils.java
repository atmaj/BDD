package org.practice.grp.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;

public class ExcelUtils {
    private String path;
    private XSSFWorkbook workbook = null;
    private Logger logMan;

    public ExcelUtils(String path) {
        logMan = LogManagerPreRun.getInstance();
        this.path = path;
        initializeWorkbook();
    }

    private void initializeWorkbook() {
        try {
            FileInputStream fis = new FileInputStream(path);
            workbook = new XSSFWorkbook(fis);
            fis.close();
            logMan.info("Excel XSSFWorkbook object is created successfully.\nPath: " + path);
        } catch (Exception e) {
            logMan.error("Error in creating object of excel file, Error = " + e.getMessage(), e);
        }
    }

    private boolean writeWorkbook() {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            fos.close();
            return true;
        } catch (Exception e) {
            logMan.error("Error in WriteWorkbook method", e);
            return false;
        }
    }

    @Override
    public void finalize() throws IOException {
        if (workbook != null)
            workbook.close();
    }

    public boolean addColumn(String sheetName, String colName) {
        boolean result = false;
        if (isSheetExist(sheetName)) {
            XSSFSheet sheet = workbook.getSheet(sheetName);
            XSSFRow row = sheet.getRow(0);
            if (row == null)
                row = sheet.createRow(0);

            XSSFCell cell;
            if (row.getLastCellNum() == -1)
                cell = row.createCell(0);
            else
                cell = row.createCell(row.getLastCellNum());

            cell.setCellValue(colName);
            result = writeWorkbook();
            logMan.info(colName + " Column added successfully for sheet = " + sheetName);
        }
        return result;
    }

    // returns true if sheet is created successfully else false
    public boolean addSheet(String sheetName) {
        boolean result = false;
        if (isSheetExist(sheetName))
            logMan.error("Sheet with name '" + sheetName + "' already exists in workbook");
        else {
            workbook.createSheet(sheetName);
            result = writeWorkbook();
            logMan.info("Sheet , = " + sheetName + " added successfully");
        }
        return result;
    }

    public void clearPreviousData(String sheetName) {
        removeSheet(sheetName);
        addSheet(sheetName);
    }

    public List<String> getAllCellDataForColumn(String sheetName, String columnName) {
        List<String> valuelist = new ArrayList<String>();
        int colIndex = getCellColumnNo(sheetName, columnName);
        XSSFSheet sheet = workbook.getSheet(sheetName);
        for (int counter = 1; counter < sheet.getLastRowNum() + 1; counter++) {
            String value = getCellData(sheetName, colIndex, counter);
            valuelist.add(value);
        }
        return valuelist;
    }

    public List<String> getAllCellDataForRow(String sheetName, int rowNum, boolean filterEmpty) {
        List<String> rowDataList = null;
        rowDataList = new ArrayList<String>();
        XSSFRow row = workbook.getSheet(sheetName).getRow(rowNum - 1);
        for (int counter = 0; counter < row.getLastCellNum(); counter++) {
            String key = getCellData(sheetName, counter, rowNum);
            rowDataList.add(key);
        }

        if (filterEmpty) {
            rowDataList = rowDataList.stream().filter(data -> !data.isEmpty()).collect(Collectors.toList());
        }
        return rowDataList;
    }

    public List<String> getAllColumnName(String sheetName) {
        return getAllCellDataForRow(sheetName, 1, true);
    }

    public Map<String, String> getRowDataAsMap(String sheetName, int rowNum) {
        List<String> columnNames = getAllColumnName(sheetName);
        List<String> rowData = getAllCellDataForRow(sheetName, rowNum, false);
        Map<String, String> dataMap = new HashMap<String, String>();

        while (rowData.size() < columnNames.size())
            rowData.add("");

        for (int i = 0; i < columnNames.size(); i++) {
            dataMap.put(columnNames.get(i), rowData.get(i));
        }

        return dataMap;
    }

    public int getCellColumnNo(String sheetName, String columnName) {
        XSSFSheet sheet = workbook.getSheet(sheetName);
        XSSFRow row = sheet.getRow(0);

        return getCellColumnNo(row, columnName);
    }

    public int getCellColumnNo(XSSFRow row, String columnName) {
        int columnNum = -1;
        try {
            for (int counter = 0; counter < row.getLastCellNum(); counter++) {
                if (row.getCell(counter).getStringCellValue().equalsIgnoreCase(columnName)) {
                    columnNum = counter;
                    break;
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getCellColumnNo method, Error = " + e.getMessage());
        }
        logMan.info("Column Number for the cloumn = " + columnName + " is = " + columnNum);
        return columnNum;
    }

    public String getCellData(String sheetName, String colName, int rowNum) {
        XSSFSheet sheet = workbook.getSheet(sheetName);
        return getCellData(sheet, getCellColumnNo(sheetName, colName), rowNum);
    }

    public String getCellData(String sheetName, int colNum, int rowNum) {
        if (!isSheetExist(sheetName)) {
            logMan.error("Invalid sheetName, in getCellData method");
            return "";
        } else if (rowNum <= 0) {
            logMan.error("Invalid row number, in getCellData method");
            return "";
        }
        return getCellData(workbook.getSheet(sheetName), colNum, rowNum);
    }

    private String getCellData(XSSFSheet sheet, int colNum, int rowNum) {
        XSSFRow row = sheet.getRow(rowNum - 1);
        if (row == null) {
            logMan.warn("Row " + rowNum + " is empty. Occured in getCellData method");
            return null;
        }
        return getCellData(row, colNum);
    }

    private String getCellData(XSSFRow row, int colNum) {
        XSSFCell cell = row.getCell(colNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (cell == null) {
            logMan.error("No Data avilable for column number " + colNum);
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
            case FORMULA:
                double d = cell.getNumericCellValue();
                if (DateUtil.isCellDateFormatted(cell)) {
                    // format in form of M/D/YY
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(DateUtil.getJavaDate(d));
                    return cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + 1 + "/"
                            + ((String.valueOf(cal.get(Calendar.YEAR))).substring(2));
                } else {
                    return String.valueOf(d);
                }
            case BLANK:
                return "";
            default:
                return String.valueOf(cell.getBooleanCellValue());
        }
    }

    public int getCellRowNum(String sheetName, String colName, String cellValue) {
        for (int i = 2; i <= getRowCount(sheetName); i++) {
            if (getCellData(sheetName, colName, i).equalsIgnoreCase(cellValue)) {
                logMan.info(
                        "row number for the cell with value = " + cellValue + "in cloumn = " + colName + ", is = " + i);
                return i;
            }
        }
        logMan.error("row number for the cell with value = " + cellValue + "in cloumn = " + colName + ", is not found");
        return -1;
    }

    public String getCellValueFromVisibleRows(String sheetName, String columnName) {
        try {
            String cellValue;
            List<Integer> visibleRows = getVisibleRowIndexes(sheetName);

            for (Integer i : visibleRows) {
                cellValue = getCellData(sheetName, columnName, i);
                return cellValue;
            }
        } catch (Exception e) {
            logMan.error("Error in getCellValueFromVisibleRows method, Error = " + e.getMessage());
        }
        return "";
    }

    public int getColumnCount(String sheetName) {
        try {
            if (!isSheetExist(sheetName))
                return -1;

            XSSFSheet sheet = workbook.getSheet(sheetName);
            XSSFRow row = sheet.getRow(0);

            if (row == null)
                return -1;
            logMan.info("Number of column in sheet = " + sheetName + ", is = " + row.getLastCellNum());
            return row.getLastCellNum();
        } catch (Exception e) {
            logMan.error("Error in getColumnCount method for sheet = " + sheetName);
            return -1;
        }
    }

    public List<String> getColumnNamesPerScenarioForData(XSSFSheet sheet, int rowNumber) {
        XSSFRow rowPerScenario = sheet.getRow(rowNumber);
        List<String> columnArrayList = new ArrayList<String>();
        try {
            for (int i = 0; i < rowPerScenario.getLastCellNum(); i++) {
                String value = getCellData(sheet, i, rowNumber);
                if (!value.isEmpty()) {
                    columnArrayList.add(value);
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getColumnNamesPerScenarioForData method, Error = " + e.getMessage());
        }
        return columnArrayList;
    }

    public Map<String, String> getDataAsMapForFilteredRow(String sheetName) {
        Map<String, String> map = null;
        try {
            map = new HashMap<String, String>();
            List<Integer> visibleRows = getVisibleRowIndexes(sheetName);
            int rownumber = visibleRows.get(0) - 1;

            List<String> keyArrayList = getAllColumnName(sheetName);
            List<String> valueArrayList = getAllCellDataForRow(sheetName, rownumber, false);

            for (int i = 0; i < valueArrayList.size(); i++) {
                if (valueArrayList.get(i) != null) {
                    if (!(valueArrayList.get(i).equals(""))) {
                        map.put(keyArrayList.get(i), valueArrayList.get(i));
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getDataAsMapForFilteredRow, Error = " + e.getMessage());
        }
        logMan.info("Data as map = " + map);
        return map;
    }

    public Map<String, String> getDataForListOfColNames(XSSFRow row, List<String> columnNames) {
        Map<String, String> newmap = new LinkedHashMap<String, String>();

        for (String colName : columnNames) {
            newmap.put(colName, getCellData(row, getCellColumnNo(row, colName)));
        }
        return newmap;
    }

    public List<List<String>> getDataInFormOfListOfList(String sheetName) {
        List<List<String>> data = new ArrayList<List<String>>();
        int rowSize = getRowCount(sheetName);
        for (int i = 1; i < rowSize + 1; i++) {
            List<String> tempRowData = getAllCellDataForRow(sheetName, i, false);
            data.add(tempRowData);
        }
        return data;
    }

    public List<Map<String, String>> getDataInFormOfListOfMap(String sheetName) {
        List<String> columnNames = getAllColumnName(sheetName);
        List<List<String>> data = getDataInFormOfListOfList(sheetName);
        List<Map<String, String>> listMapData = new ArrayList<>();

        for (List<String> list : data) {
            HashMap<String, String> tempMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                tempMap.put(columnNames.get(i), list.get(i));
            }
            listMapData.add(tempMap);
        }
        logMan.info("Data in form of List of Map for sheet = " + sheetName + ", is = " + listMapData);
        return listMapData;
    }

    public List<Map<String, String>> getDataInFormOfListOfMapByLinkingTwoSheets(String sheetName, String linkedSheet,
                                                                                String linkedColumnName) {
        List<String> columnNames = getAllColumnName(sheetName);
        List<List<String>> data = getDataInFormOfListOfList(sheetName);
        Map<String, Map<String, String>> mapOfMap = getDataInFormOfMapofMap(linkedSheet, linkedColumnName);

        List<Map<String, String>> listMapData = new ArrayList<>();
        for (List<String> list : data) {
            HashMap<String, String> tempMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                tempMap.put(columnNames.get(i), list.get(i));
            }
            if (mapOfMap.get(tempMap.get(linkedColumnName)) != null) {
                tempMap.putAll(mapOfMap.get(tempMap.get(linkedColumnName)));
                listMapData.add(tempMap);
            }
        }
        logMan.info("Data in form of List of Map linking two sheet, Sheet1 =" + sheetName + " and  Sheet2 = "
                + linkedSheet + ", is = " + listMapData);
        return listMapData;
    }

    public Map<String, Map<String, String>> getDataInFormOfMapofMap(String sheetName, String linkedColumnName) {
//		List<String> columnNames = getAllColumnName(sheetName);
//		List<Map<String, String>> dataList = getDataInFormOfListOfMap(sheetName);
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        int rowCount = getRowCount(sheetName);
        List<String> rowHeaderData = getAllCellDataForRow(sheetName, 1, false);
        List<Integer> blankIndexList = new ArrayList<Integer>();

        for (int index = 0; index < rowHeaderData.size(); index++) {
            if (rowHeaderData.get(index).trim().isEmpty())
                blankIndexList.add(index);
        }
        Collections.sort(blankIndexList, Collections.reverseOrder());

        for (int i = 1; i < rowCount; i++) {
            List<String> rowData = getAllCellDataForRow(sheetName, i, false);
            if (rowData.size() > 0) {
                for (Integer integer : blankIndexList) {
                    rowData.remove(integer.intValue());
                }

                Map<String, String> tempMap = getRowDataAsMap(sheetName, i);
                map.put(tempMap.get(linkedColumnName), tempMap);
            }
        }
        logMan.info("Data in Map of Map for sheet =" + sheetName + " and linked column = " + linkedColumnName
                + ", is = " + map);
        return map;
    }

    public Map<String, Map<String, String>> getDataInFormOfMapofMapForTestData(String sheetName,
                                                                               String linkedColumnName, String linkedColumnName2) {

        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        int rowCount = getRowCount(sheetName);
        for (int i = 1; i < rowCount; i++) {
            Map<String, String> tempMap = getRowDataAsMap(sheetName, i);
            if (tempMap.get(linkedColumnName2).isEmpty()) {
                map.put(tempMap.get(linkedColumnName), tempMap);
            } else {
                map.put(tempMap.get(linkedColumnName) + ":" + tempMap.get(linkedColumnName2), tempMap);
            }
        }
        logMan.info("Data in Map of Map for sheet =" + sheetName + " and linked column = " + linkedColumnName
                + ", is = " + map);
        return map;
    }

    public Map<String, List<String>> getDataInFormOfMapWithListValue(String sheetName) {
        int rowCount = getRowCount(sheetName);
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (int i = 1; i < rowCount; i++) {
            List<String> tempRowData = getAllCellDataForRow(sheetName, i, false);
            map.put(tempRowData.get(0), tempRowData);
        }
        logMan.info("Data in Map of list for sheet =" + sheetName + ",is = " + map);
        return map;
    }

    // returns the row count in a sheet
    public int getRowCount(String sheetName) {
        try {
            if (workbook.getSheetIndex(sheetName) == -1) {
                logMan.error("Invalid sheetName, in getRowCount method");
                return 0;
            } else {
                XSSFSheet sheet = workbook.getSheet(sheetName);
                int number = sheet.getLastRowNum() + 1;
                logMan.info(number + " row(s) available in sheet: " + sheetName);
                return number;
            }
        } catch (Exception e) {
            logMan.error("Error in getRowCount method, Error = " + e.getMessage());
            return 0;
        }
    }

    /**
     * This will return test data in Map
     *
     * @param sheetName
     * @param linkedColumnName
     * @param linkedColumnName2
     * @param executionColumnName
     * @return
     */
    // TODO: Check logic
    public Map<String, List<Map<String, String>>> getTestDataInFormOfMap(String sheetName, String linkedColumnName,
                                                                         String linkedColumnName2, String executionColumnName) {
        List<String> columnNamesForZerothRow = getAllColumnName(sheetName);
        int likedColumnNumber = getCellColumnNo(sheetName, linkedColumnName);
        Map<String, List<Map<String, String>>> map = null;
        try {
            map = new HashMap<String, List<Map<String, String>>>();
            String value;

            XSSFSheet sheet = workbook.getSheet(sheetName);

            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                XSSFRow row = sheet.getRow(i);
                String scenarioNamePerRow = getCellData(row, likedColumnNumber);
                if (!scenarioNamePerRow.isEmpty()) {
                    List<String> columnNamesForScenario = getColumnNamesPerScenarioForData(sheet, i - 1);

                    List<String> columnNames = new ArrayList<String>();
                    columnNames.addAll(columnNamesForZerothRow);
                    columnNames.addAll(columnNamesForScenario);

                    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    Map<String, String> tempMap = new LinkedHashMap<String, String>();
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        XSSFCell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        if (cell.getCellType() == CellType.STRING) {
                            value = cell.getStringCellValue();
                        } else if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                            // DataFormatter dataFormatter=new DataFormatter();
                            // value=dataFormatter.formatCellValue(cell);
                            value = cell.getRawValue();
                        } else if (cell.getCellType() == CellType.BLANK) {
                            value = "";
                        } else {
                            value = "";
                        }
                        if (j < columnNames.size()) {
                            tempMap.put(columnNames.get(j), value);
                        } else {
                            break;
                        }
                    }
                    String initialLinkedValue = tempMap.get(linkedColumnName);
                    if (tempMap.get(executionColumnName).equalsIgnoreCase("YES")) {
                        list.add(tempMap);
                    }
                    if (tempMap.get(linkedColumnName2).isEmpty()) {
                        if (tempMap.get(executionColumnName).equalsIgnoreCase("YES")) {
                            map.put(tempMap.get(linkedColumnName), list);
                        }

                    } else {
                        i++;
                        while (i < sheet.getLastRowNum() + 1) {
                            row = sheet.getRow(i);
                            String nextRowValue = row.getCell(likedColumnNumber).getStringCellValue();
                            if (initialLinkedValue.equalsIgnoreCase(nextRowValue)) {
                                Map<String, String> map1 = getDataForListOfColNames(row, columnNames);
                                if (map1.get(executionColumnName).equalsIgnoreCase("YES")) {
                                    list.add(map1);
                                }
                                i++;
                            } else {
                                i--;
                                break;
                            }
                        }
                        map.put(tempMap.get(linkedColumnName), list);
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getTestDataInFormOfMap method, Error = " + e.getMessage(), e);
        }
        logMan.info("Data in Map of List of Map for sheet =" + sheetName + " and linked column = " + linkedColumnName
                + ", is = " + map);
        return map;
    }

    public Map<String, List<Map<String, String>>> getTestDataWhenColumnNameIsWrittenInFeatureFile(String sheetName,
                                                                                                  String linkedColumnName, String linkedColumnName2) {
        List<String> columnNames = getAllColumnName(sheetName);
        int likedColumnNumber = getCellColumnNo(sheetName, linkedColumnName);
        Map<String, List<Map<String, String>>> map = null;
        try {
            map = new HashMap<String, List<Map<String, String>>>();
            String value;

            XSSFSheet sheet = workbook.getSheet(sheetName);
            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                XSSFRow row = sheet.getRow(i);
                Map<String, String> tempMap = new HashMap<String, String>();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    XSSFCell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    if (cell.getCellType() == CellType.STRING) {
                        value = cell.getStringCellValue();
                    } else if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                        // DataFormatter dataFormatter=new DataFormatter();
                        // value=dataFormatter.formatCellValue(cell);
                        value = cell.getRawValue();
                    } else if (cell.getCellType() == CellType.BLANK) {
                        value = "";
                    } else {
                        value = "";
                    }
                    tempMap.put(columnNames.get(j), value);
                }
                String initialLinkedValue = tempMap.get(linkedColumnName);
                list.add(tempMap);
                if (tempMap.get(linkedColumnName2).isEmpty()) {
                    map.put(tempMap.get(linkedColumnName), list);
                } else {
                    i++;
                    while (i < sheet.getLastRowNum() + 1) {
                        row = sheet.getRow(i);
                        String nextRowValue = row.getCell(likedColumnNumber).getStringCellValue();
                        if (initialLinkedValue.equalsIgnoreCase(nextRowValue)) {
                            Map<String, String> map1 = getDataForListOfColNames(row, columnNames);
                            list.add(map1);
                            i++;
                        } else {
                            i--;
                            break;
                        }
                    }
                    map.put(tempMap.get(linkedColumnName), list);
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getTestDataWhenColumnNameIsWrittenInFeatureFile method, Error = " + e.getMessage());
        }
        logMan.info("Data in Map of List Map for sheet =" + sheetName + " and linked column = " + linkedColumnName
                + ", is = " + map);
        return map;
    }

    public int getVisibleRowCount(String sheetName) {
        int count = 0;
        try {
            if (isSheetExist(sheetName)) {
                XSSFSheet sheet = workbook.getSheet(sheetName);
                for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                    if (!sheet.getRow(i).getZeroHeight()) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getVisibleRowCount method, Error = " + e.getMessage());
        }
        logMan.info("visible run count = " + count);
        return count;
    }

    public List<Integer> getVisibleRowIndexes(String sheetName) {
        List<Integer> visibleRows = new ArrayList<Integer>();
        try {
            if (isSheetExist(sheetName)) {
                XSSFSheet sheet = workbook.getSheet(sheetName);
                for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                    if (!sheet.getRow(i).getZeroHeight()) {
                        visibleRows.add(i + 1);
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getVisibleRowIndexes method, Error = " + e.getMessage());
        }
        return visibleRows;
    }

    // find whether sheets exists
    public boolean isSheetExist(String sheetName) {
        try {
            int index = workbook.getSheetIndex(sheetName);
            if (index == -1) {
                index = workbook.getSheetIndex(sheetName.toUpperCase());
                if (index == -1) {
                    index = workbook.getSheetIndex(sheetName.toLowerCase());
                    if (index == -1) {
                        logMan.error(sheetName + " does not exists");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in isSheetExist method", e);
            return false;
        }
        return true;
    }

    // removes a column and all the contents
    public boolean removeColumn(String sheetName, int colNum) {
        try {
            if (!isSheetExist(sheetName))
                return false;

            FileInputStream fis = new FileInputStream(path);
            workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheet(sheetName);
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
            workbook.getCreationHelper();
            style.setFillPattern(FillPatternType.NO_FILL);

            for (int i = 0; i < getRowCount(sheetName); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row != null) {
                    XSSFCell cell = row.getCell(colNum);
                    if (cell != null) {
                        cell.setCellStyle(style);
                        row.removeCell(cell);
                    }
                }
            }
            OutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            fos.close();
            logMan.info("Successfully removed the contents of cloumnNo = " + colNum + " for sheet = " + sheetName);
        } catch (Exception e) {
            logMan.error("Error in removeColumn method and all it's contents ");
            return false;
        }
        return true;
    }

    // returns true if sheet is removed successfully else false if sheet does not
    // exist
    public boolean removeSheet(String sheetName) {
        try {
            int index = workbook.getSheetIndex(sheetName);
            if (index == -1) {
                logMan.error("Invalid sheetName, in getCellData method");
                return false;
            }

            workbook.removeSheetAt(index);
            FileOutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            fos.close();
            logMan.info("Sheet , = " + sheetName + " removed successfully");
        } catch (Exception e) {
            logMan.error("Error in addSheet method, Error = " + e.getMessage());
            return false;
        }
        return true;
    }

    public Map<String, List<String>> scenarioTestDataColumnName(String sheetName, String linkedColumnName) {
        Map<String, List<String>> map = null;
        try {
            int likedColumnNumber = getCellColumnNo(sheetName, linkedColumnName);
            map = new HashMap<String, List<String>>();
            XSSFSheet sheet = workbook.getSheet(sheetName);
            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                String scenarioNamePerRow = getCellData(sheet.getRow(i), likedColumnNumber);
                if (!scenarioNamePerRow.isEmpty()) {
                    List<String> columnNamesForScenario = getColumnNamesPerScenarioForData(sheet, i - 1);
                    if (!map.containsKey(scenarioNamePerRow)) {
                        map.put(scenarioNamePerRow, columnNamesForScenario);
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in scenarioTestDataColumnName method, Error = " + e.getMessage());
        }
        return map;
    }

    // returns true if data is set successfully else false
    public boolean setCellData(String sheetName, String colName, int rowNum, String data) {
        return setCellData(sheetName, colName, rowNum, data, null);
    }

    // returns true if data is set successfully else false
    public boolean setCellData(String sheetName, String colName, int rowNum, String data, String url) {
        try {
            FileInputStream fis = new FileInputStream(path);
            workbook = new XSSFWorkbook(fis);

            if (workbook.getSheetIndex(sheetName) == -1) {
                logMan.error("Invalid sheetName, in getCellData method");
                return false;
            }
            if (rowNum <= 0) {
                logMan.error("Invalid row number, in getCellData method");
                return false;
            }

            int colNum = -1;

            XSSFSheet sheet = workbook.getSheet(sheetName);
            XSSFRow row = sheet.getRow(0);
            for (int i = 0; i < row.getLastCellNum(); i++) {
                if (row.getCell(i).getStringCellValue().trim().equals(colName.trim()))
                    colNum = i;
            }
            if (colNum == -1) {
                logMan.error("Invalid column Name, in getCellData method. " + colName + " not available in sheet: "
                        + sheetName);
                return false;
            }

            sheet.autoSizeColumn(colNum);
            row = sheet.getRow(rowNum - 1);
            if (row == null)
                row = sheet.createRow(rowNum - 1);

            XSSFCell cell = row.getCell(colNum);
            if (cell == null)
                cell = row.createCell(colNum);

            cell.setCellValue(data);

            if (url != null && !url.isEmpty()) {
                XSSFCreationHelper createHelper = workbook.getCreationHelper();
                // cell style for hyperlinks
                // by default hypelrinks are blue and underlined
                CellStyle hlink_style = workbook.createCellStyle();
                XSSFFont hlink_font = workbook.createFont();
                hlink_font.setUnderline(XSSFFont.U_SINGLE);
                hlink_font.setColor(IndexedColors.BLUE.getIndex());
                hlink_style.setFont(hlink_font);
                // hlink_style.setWrapText(true);
                XSSFHyperlink link = createHelper.createHyperlink(HyperlinkType.FILE);
                link.setAddress(url);
                cell.setHyperlink(link);
                cell.setCellStyle(hlink_style);
            }
            FileOutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            fos.close();
        } catch (Exception e) {
            logMan.error("Error in setCellData method with hyperlink, Error = " + e.getMessage());
            return false;
        }
        return true;
    }

    public void setFilter(String sheetName, int rowNo, String columnName, String filterValue) {
        try {
            XSSFSheet sheet = workbook.getSheet(sheetName);
            sheet.setAutoFilter(new CellRangeAddress(0, 5100, 0, sheet.getRow(0).getLastCellNum()));
            int cellIndex = getCellColumnNo(sheetName, columnName);

            CTAutoFilter filter = sheet.getCTWorksheet().getAutoFilter();
            CTFilterColumn filterColumn = filter.addNewFilterColumn();
            filterColumn.setColId(cellIndex);

            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                if (!(sheet.getRow(i).getZeroHeight())) {
                    XSSFRow row = sheet.getRow(i);
                    if (row.getCell(cellIndex) == null) {
                        row.setZeroHeight(true);
                    } else {
                        String value = row.getCell(cellIndex).toString();
                        if (value.endsWith(".0")) {
                            value = value.substring(0, value.length() - 2);
                        } else if (value.endsWith(".00")) {
                            value = value.substring(0, value.length() - 3);
                        }
                        if (!value.equalsIgnoreCase(filterValue)) {
                            logMan.info("Filter is set successfully in cloumn = " + columnName + " with value = "
                                    + filterValue);
                            row.setZeroHeight(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in setFilter method, Error = " + e.getMessage());
        }
    }

    public void setFilterForMultipleValues(String sheetName, int rowNo, String columnName, List<String> filterValue) {
        try {
            XSSFSheet sheet = workbook.getSheet(sheetName);
            sheet.setAutoFilter(new CellRangeAddress(0, 7000, 0, sheet.getRow(0).getLastCellNum()));
            int cellIndex = getCellColumnNo(sheetName, columnName);

            CTAutoFilter filter = sheet.getCTWorksheet().getAutoFilter();
            CTFilterColumn filterColumn = filter.addNewFilterColumn();
            filterColumn.setColId(cellIndex);

            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                if (!sheet.getRow(i).getZeroHeight()) {
                    XSSFRow row = sheet.getRow(i);
                    if (row.getCell(cellIndex) == null) {
                        row.setZeroHeight(true);
                    } else {
                        String value = row.getCell(cellIndex).toString();
                        if (value.endsWith(".0")) {
                            value = value.substring(0, value.length() - 2);
                        } else if (value.endsWith(".00")) {
                            value = value.substring(0, value.length() - 3);
                        }
                        if (!filterValue.contains(value)) {
                            logMan.info("Filter is set successfully in cloumn = " + columnName + " with value = "
                                    + filterValue);
                            row.setZeroHeight(true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in setFilterForMultipleValues method, Error = " + e.getMessage());
        }
    }

    public void writeOutputToFile(String myFilename) {
        try {
            OutputStream fos = new FileOutputStream(new File(myFilename));
            workbook.write(fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            logMan.error("Error in writeOutputToFile method, Error = " + e.getMessage());
        }
    }

    public Map<String, Map<String, String>> getMappedData(String sheetName, String mappedColumnName) {

        List<String> columnNamesForZerothRow = getAllColumnName(sheetName);
        int mappedColumnNumber = getCellColumnNo(sheetName, mappedColumnName);
        Map<String, Map<String, String>> map = null;
        try {
            map = new HashMap<String, Map<String, String>>();
            String value;

            XSSFSheet sheet = workbook.getSheet(sheetName);
            for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null) {
                    row = sheet.createRow(i);
                }
                String mappedValue = getCellData(row, mappedColumnNumber);
                if (!mappedValue.isEmpty()) {
                    List<String> columnNamesForScenario = getColumnNamesPerScenarioForData(sheet, i - 1);

                    List<String> columnNames = new ArrayList<String>();
                    columnNames.addAll(columnNamesForZerothRow);
                    columnNames.addAll(columnNamesForScenario);

                    Map<String, String> tempMap = new LinkedHashMap<String, String>();
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        XSSFCell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        if (cell.getCellType() == CellType.STRING) {
                            value = cell.getStringCellValue();
                        } else if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                            // DataFormatter dataFormatter=new DataFormatter();
                            // value=dataFormatter.formatCellValue(cell);
                            value = cell.getRawValue();
                        } else if (cell.getCellType() == CellType.BLANK) {
                            value = "";
                        } else {
                            value = "";
                        }
                        if (j < columnNames.size()) {
                            tempMap.put(columnNames.get(j), value);
                        } else {
                            break;
                        }
                    }
                    String mappedValueFromMap = tempMap.get(mappedColumnName);
                    map.put(mappedValueFromMap, tempMap);

                    if (checkValueIsNotNotBlankInNextRow(sheet, i + 1, mappedColumnNumber)) {
                        i++;
                        while (i < sheet.getLastRowNum() + 1) {
                            row = sheet.getRow(i);
                            if (row == null) {
                                row = sheet.createRow(i);
                            }
                            String nextRowMappedValue = getCellData(row, mappedColumnNumber);
                            Map<String, String> map1 = getDataForListOfColNames(row, columnNames);
                            map.put(nextRowMappedValue, map1);

                            if (checkValueIsNotNotBlankInNextRow(sheet, i + 1, mappedColumnNumber)) {
                                i++;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logMan.error("Error in getTestDataInFormOfMap method, Error = " + e.getMessage());
        }
        return map;
        // logMan.info("Data in Map of List of Map for sheet ="+sheetName+" and
        // linked column = "+linkedColumnName+ ", is = "+map);
    }

    public boolean checkValueIsNotNotBlankInNextRow(XSSFSheet sheet, int rowNumber, int colNumber) {
        boolean flag = false;
        if (rowNumber < sheet.getLastRowNum() + 1) {
            XSSFRow rowPerData = sheet.getRow(rowNumber);
            if (rowPerData == null) {
                rowPerData = sheet.createRow(rowNumber);
            }
            String value = null;
            try {
                // for(int i =0 ; i<rowPerData.getLastCellNum();i++){
                XSSFCell cellForScenario = rowPerData.getCell(colNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (cellForScenario.getCellType() == CellType.STRING) {
                    value = cellForScenario.getStringCellValue();
                } else if (cellForScenario.getCellType() == CellType.NUMERIC
                        || cellForScenario.getCellType() == CellType.FORMULA) {
                    // DataFormatter dataFormatter=new DataFormatter();
                    // value=dataFormatter.formatCellValue(cell);
                    value = cellForScenario.getRawValue();
                } else if (cellForScenario.getCellType() == CellType.BLANK) {
                    value = "";
                } else {
                    value = "";
                }
                // }
                if (!value.isEmpty()) {
                    flag = true;
                }
            } catch (Exception e) {
                logMan.error("Error in getColumnNamesPerScenarioForData method, Error = " + e.getMessage());
            }
        }
        return flag;
    }

    public Map<String, List<List<String>>> getTestScenarioDataSet(String sheetName, String keyColumn,
                                                                  String execFlagColumn) {
        int rowCount = getRowCount(sheetName);
        int keyCellNumber = getCellColumnNo(sheetName, keyColumn);
        int execFlagCellNumber = getCellColumnNo(sheetName, execFlagColumn);
        String scenarioName = "";
        List<Integer> blankIndexList = new ArrayList<Integer>();
        List<String> dataHeader = new ArrayList<String>();
        Map<String, List<List<String>>> scenarioDataMap = new HashMap<String, List<List<String>>>();

        if (!(getAllColumnName(sheetName).containsAll(Arrays.asList(new String[] { keyColumn, execFlagColumn }))))
            return scenarioDataMap;

        int counter = 2;
        while (counter <= rowCount + 1) {
            String prevKeyCellData = getCellData(sheetName, keyCellNumber, counter - 1);
            if (prevKeyCellData == null)
                prevKeyCellData = "";
            else
                prevKeyCellData = prevKeyCellData.trim();
            String currentKeyCellData = prevKeyCellData;
            if (counter <= rowCount) {
                currentKeyCellData = getCellData(sheetName, keyCellNumber, counter);
                if (currentKeyCellData == null) {
                    counter++;
                    continue;
                } else {
                    currentKeyCellData = currentKeyCellData.trim();
                }
            }

            if (!currentKeyCellData.isEmpty()) {
                if (prevKeyCellData.isEmpty()) {
                    scenarioName = currentKeyCellData;
                    dataHeader = getAllCellDataForRow(sheetName, counter - 1, false);
                    blankIndexList.clear();
                    for (int index = 0; index < dataHeader.size(); index++) {
                        if (dataHeader.get(index).trim().isEmpty())
                            blankIndexList.add(index);
                    }
                    Collections.sort(blankIndexList, Collections.reverseOrder());
                    for (Integer integer : blankIndexList) {
                        dataHeader.remove(integer.intValue());
                    }
                    counter++;
                    continue;
                } else {
                    List<String> scenarioData = getAllCellDataForRow(sheetName, counter - 1, false);
                    scenarioName = scenarioData.get(keyCellNumber).trim();
                    List<List<String>> tmpData = new ArrayList<List<String>>();
                    if (scenarioData.get(execFlagCellNumber).equalsIgnoreCase("Yes")) {
                        if (scenarioDataMap.containsKey(scenarioName)) {
                            tmpData = scenarioDataMap.get(scenarioName);
                        } else {
                            tmpData.add(dataHeader);
                        }
                        for (Integer integer : blankIndexList) {
                            scenarioData.remove(integer.intValue());
                        }

                        while (dataHeader.size() != scenarioData.size())
                            scenarioData.remove(scenarioData.size() - 1);

                        tmpData.add(scenarioData);
                        scenarioDataMap.put(scenarioName, tmpData);
                    }
                }
            } else {
                if (prevKeyCellData.isEmpty()) {
                    counter++;
                    continue;
                } else if (counter > 2) {
                    List<String> scenarioData = getAllCellDataForRow(sheetName, counter - 1, false);
                    scenarioName = scenarioData.get(keyCellNumber).trim();
                    List<List<String>> tmpData = new ArrayList<List<String>>();
                    if (scenarioData.get(execFlagCellNumber).equalsIgnoreCase("Yes")) {
                        if (scenarioDataMap.containsKey(scenarioName)) {
                            tmpData = scenarioDataMap.get(scenarioName);
                        } else {
                            tmpData.add(dataHeader);
                        }
                        for (Integer integer : blankIndexList) {
                            scenarioData.remove(integer.intValue());
                        }

                        while (dataHeader.size() != scenarioData.size())
                            scenarioData.remove(scenarioData.size() - 1);

                        tmpData.add(scenarioData);
                        scenarioDataMap.put(scenarioName, tmpData);
                    }
                }
                if (!scenarioName.isEmpty()) {
                    scenarioName = "";
                }
            }
            counter++;
        }

        return scenarioDataMap;
    }

    public List<String> getAllSheetNames() {
        List<String> sheetNames = new ArrayList<String>();
        int totalSheetCount = workbook.getNumberOfSheets();
        for (int counter = 0; counter < totalSheetCount; counter++) {
            sheetNames.add(workbook.getSheetAt(counter).getSheetName());
        }
        return sheetNames;
    }
}