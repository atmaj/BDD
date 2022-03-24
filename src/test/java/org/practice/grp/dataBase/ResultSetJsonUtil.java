package org.practice.grp.dataBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;

public class ResultSetJsonUtil {
    public static String pathEvaluator(File json, String path) {
        Configuration conf = Configuration.builder().jsonProvider(new GsonJsonProvider())
                .options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS).build();
        ParseContext parseContext = JsonPath.using(conf);
        try {
            return ((JsonArray) parseContext.parse(json).read(path)).get(0).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fetchJsonSubString(String sourceJson, List<String> keyList, int retainSequenceCap,
                                            String[] additionalColumns) {
        String modifiedString = "";

        JsonArray json = JsonParser.parseString(sourceJson).getAsJsonArray();
        JsonArray json1 = new JsonArray();
        json.forEach(jsonEl -> {
            JsonObject newJsonEl = new JsonObject();
            JsonObject currentJsonElement = jsonEl.getAsJsonObject();
            for (int counter = 0; counter < retainSequenceCap; counter++) {
                String key = keyList.get(counter);
                newJsonEl.add(key, currentJsonElement.get(key));
            }
            for (int counter = 0; counter < additionalColumns.length; counter++) {
                String key = additionalColumns[counter];
                newJsonEl.add(key, currentJsonElement.get(key));
            }
            json1.add(newJsonEl);
        });
        modifiedString = json1.toString();
        return modifiedString;
    }

    public static int filterJson(File tempJsonFile, File filteredCSV, int retainColumnCount, String filterColumn,
                                 String filterData) {
        int filteredRecordCounter = 0;
        try (JsonReader reader = new JsonReader(new FileReader(tempJsonFile));
             FileWriter writer = new FileWriter(filteredCSV)) {
            reader.beginArray();
            boolean recordEntered = false;
            while (reader.hasNext()) {
                StringBuffer csvHeader = new StringBuffer();
                StringBuffer csvRecord = new StringBuffer();
                int counter = 0;
                reader.beginObject();
                while (reader.hasNext()) {
                    String key = getFieldValue(reader).trim();
                    String value = getFieldValue(reader).trim();

                    if (counter < retainColumnCount) {
                        csvHeader.append(key).append(",");
                        csvRecord.append(value).append(",");
                    } else if (key.equals(filterColumn)) {
                        if (value.equalsIgnoreCase(filterData)) {
                            csvHeader.append(key).append(",");
                            csvRecord.append(value).append(",");
                            if (csvRecord.length() > 0) {
                                if (!recordEntered) {
                                    writer.write(csvHeader.toString());
                                    writer.write(System.lineSeparator());
                                    recordEntered = true;
                                }
                                writer.write(csvRecord.toString());
                                writer.write(System.lineSeparator());
                                filteredRecordCounter++;
                            }
                        }
                        csvHeader = new StringBuffer();
                        csvRecord = new StringBuffer();
                        break;
                    }
                    counter++;
                }
                while (!reader.peek().equals(JsonToken.END_OBJECT)) {
                    reader.skipValue();
                }
                reader.endObject();
            }
            reader.endArray();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filteredRecordCounter;
    }

    private static String getFieldValue(JsonReader reader) throws IOException {
        while (reader.hasNext()) {
            JsonToken check = reader.peek();
            switch (check) {
                case NULL:
                    reader.nextNull();
                    return "";
                case NAME:
                    return reader.nextName();
                case BOOLEAN:
                    return String.valueOf(reader.nextBoolean());
                case NUMBER:
                case STRING:
                    return reader.nextString();
                default:
                    break;
            }
        }
        return null;
    }

    public static int getRecordCount(File jsonArrayFile) {
        int recordCount = 0;
        try (JsonReader reader = new JsonReader(new FileReader(jsonArrayFile))) {
            reader.beginArray();
            while (reader.hasNext()) {
                if (reader.peek().equals(JsonToken.BEGIN_OBJECT))
                    recordCount++;
                reader.skipValue();
            }
            reader.endArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recordCount;
    }

    public static List<String> getColumnList(File jsonArrayFile) {
        List<String> columnList = new ArrayList<String>();
        try (JsonReader reader = new JsonReader(new FileReader(jsonArrayFile))) {
            reader.beginArray();
            while (reader.hasNext()) {
                if (reader.peek().equals(JsonToken.NAME))
                    columnList.add(reader.nextName());
                else if (reader.peek().equals(JsonToken.END_OBJECT))
                    break;
                else if (reader.peek().equals(JsonToken.BEGIN_OBJECT))
                    reader.beginObject();
                else
                    reader.skipValue();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return columnList;
    }

    public static boolean isRecordsAvailableInJson(File jsonFile) {
        boolean recordExists = true;
        if (jsonFile.length() < 20) {
            try {
                int recordSize = Streams.parse(new JsonReader(new FileReader(jsonFile))).getAsJsonArray().size();
                if (recordSize == 0) {
                    recordExists = false;
                }
            } catch (JsonParseException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return recordExists;
    }
}