package org.practice.grp.api;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.practice.grp.base.BasePage;
import org.practice.grp.utilities.FlatMapUtil;
import org.practice.grp.utilities.LogManager;

public class RestHelper extends BasePage {
    private Map<String, String> authenticationHeaders;
    private Logger logMan = null;
    private RestAssuredConfig restAssuredConfig = config;
    private String value;

    public RestHelper() {
        logMan = LogManager.getInstance();
        configureSSL();
    }

    public RestHelper(Map<String, String> authenticationDetails) {
        logMan = LogManager.getInstance();
        configureSSL();
        setAuthenticationHeaders(authenticationDetails);
    }

    public void setAuthenticationHeaders(Map<String, String> authenticationDetails) {
        this.authenticationHeaders = authenticationDetails;
    }

    public Response postMessageByMessageBody(String serviceURLPart, String messageBody) {
        return postMessageByMessageBody(serviceURLPart, messageBody, authenticationHeaders);
    }

    public Response postMessageByMessageBody(String serviceURLPart, String messageBody, ContentType contentType) {
        return postMessageByMessageBody(serviceURLPart, messageBody, authenticationHeaders, contentType);
    }

    public Response postMessageByMessageBody(String serviceURLPart, String messageBody,
                                             Map<String, String> authenticationDetails) {
        return postMessageByMessageBody(serviceURLPart, messageBody, authenticationDetails, ContentType.JSON);
    }

    public Response getMessage(String serviceURLPart) {
        return getMessage(serviceURLPart, authenticationHeaders);
    }

    public Response postMessageByMessageBodyContentTypeXML(String serviceURLPart, String messageBody,
                                                           ContentType contentType) {
        return postMessageByMessageBody(serviceURLPart, messageBody, authenticationHeaders, contentType);
    }

    public Response postMessageByMessageBodyContentTypeXML(String serviceURLPart, String messageBody) {
        return postMessageByMessageBody(serviceURLPart, messageBody, authenticationHeaders, ContentType.XML);
    }

    public Response postMessageByMessageBodyContentTypeXML(String serviceURLPart, String messageBody,
                                                           Map<String, String> authenticationDetails) {
        return postMessageByMessageBody(serviceURLPart, messageBody, authenticationDetails, ContentType.XML);
    }

    private Response postMessageByMessageBody(String serviceURLPart, String messageBody,
                                              Map<String, String> authenticationDetails, ContentType contentType) {
        Response response = null;
        try {
            logMan.info("---------------------------POST URL START--------------------------------");
            logMan.debug("Posting URL=" + serviceURLPart);
            logMan.info("----------------------------POST URL END-------------------------------");
            logMan.info("---------------------------REQUEST BODY START--------------------------------");
            logMan.debug("Posting request as-\n\n" + messageBody);
            logMan.info("----------------------------REQUEST BODY END-------------------------------");
            response = given().config(restAssuredConfig).config(restAssuredConfig).headers(authenticationDetails)
                    .contentType(contentType).body(messageBody).post(serviceURLPart);

            logMan.info("--------------------------RESPONSE BODY START---------------------------------");
            logMan.debug("Response Generated successfully and has value =\n\n" + response.asString());
            logMan.info("----------------------------RESPONSE BODY END-------------------------------");
        } catch (Throwable t) {
            logMan.error("Error Occured Inside PostMessageByMessageBody function while posting at URL ="
                    + serviceURLPart + " , Messagebody= " + messageBody, t);
            Assert.assertTrue("Unable to connect to Service or Error Occurred inside PostMessageByMessageBody function",
                    0 > 1);
        }

        return response;
    }

    public Response updateMessageWithoutBody(String serviceURLPart) {
        return updateMessage(serviceURLPart, null, ContentType.JSON);
    }

    public Response updateMessageWithBody(String serviceURLPart, String messageBody) {
        return updateMessage(serviceURLPart, null, ContentType.JSON);
    }

    public Response updateMessageWithXMLBody(String serviceURLPart, String messageBody) {
        return updateMessage(serviceURLPart, null, ContentType.XML);
    }

    private Response updateMessage(String serviceURLPart, String messageBody, ContentType contentType) {
        Response response = null;
        messageBody = messageBody == null ? "" : messageBody;
        try {
            logMan.info("---------------------------PUT URL START--------------------------------");
            logMan.debug("Put URL=" + serviceURLPart);
            logMan.info("----------------------------PUT URL END-------------------------------");
            logMan.info("---------------------------REQUEST BODY START--------------------------------");
            logMan.debug("Put request as-\n\n" + messageBody);
            logMan.info("----------------------------REQUEST BODY END-------------------------------");
            response = given().config(restAssuredConfig).config(restAssuredConfig).headers(authenticationHeaders)
                    .contentType(contentType).body(messageBody).when().put(serviceURLPart);
            String returnedStatisCode = String.valueOf(response.getStatusCode());
            logMan.info("--------------------------RESPONSE BODY START---------------------------------");
            if (returnedStatisCode.equalsIgnoreCase("200")) {
                logMan.info("UpdateUsingPutMessage function Executed successfully, response =" + response.asString());
            } else {
                logMan.error("Error Occured while Update using UpdateUsingPutMessage function, returnedStatisCode="
                        + returnedStatisCode + ",URL=" + serviceURLPart + ", Error Descripton=" + response.asString());
            }
            logMan.info("----------------------------RESPONSE BODY END-------------------------------");
        } catch (Throwable t) {
            logMan.error("Error Occured Inside UpdateUsingGetMessage function while updating using PUT" + serviceURLPart
                    + ",Error Description=" + t.getMessage(), t);
        }
        return response;
    }

    public Response getMessage(String serviceURLPart, Map<String, String> authenticationDetails) {
        logMan.info("GetMessage function START");
        Response response = null;
        try {
            logMan.info("---------------------------GET URL START--------------------------------");
            logMan.debug("Get Method, URL=" + serviceURLPart);
            logMan.info("----------------------------GET URL END-------------------------------");
            response = given().config(restAssuredConfig).headers(authenticationDetails).when().get(serviceURLPart);
            logMan.info("GetMessage function END");

            logMan.info("--------------------------RESPONSE BODY START---------------------------------");
            logMan.debug("Response Generated successfully and has value =\n\n" + response.asString());
            logMan.info("----------------------------RESPONSE BODY END-------------------------------");

        } catch (Throwable t) {
            logMan.error("Error Occured Inside GetMessage function while posting at URL =" + serviceURLPart
                    + " , Error= " + t.getMessage(), t);
            Assert.assertTrue("Unable to connect to Service or Error Occurred inside GETMessage function", 0 > 1);
        }
        return response;
    }

    public String returnFileAsASingleString(String filePath) {
        String everything = "";
        try {
            everything = FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
            logMan.debug("File read successfully, file path=" + filePath);
        } catch (Throwable t) {
            logMan.error("Error Occured Inside returnFileAsASingleString function for path =" + filePath, t);
        }

        return everything;
    }

    private void configureSSL() {
        if (System.getProperty("SSL_CONFIGURED").equalsIgnoreCase("Yes")) {
            SSLConfig sslConfig = new SSLConfig();
            switch (System.getProperty("SSL_HANDSHAKE_KEYSTORE_TYPE").trim().toUpperCase()) {
                case "JKS":
                    sslConfig = getJKSConfig();
                    break;
                case "P12":
                case "PKCS":
                case "PKCS12":
                    sslConfig = getPKCSConfig();
                    break;
            }
            restAssuredConfig = RestAssured.config().sslConfig(sslConfig);
        }
    }

    @SuppressWarnings("deprecation")
    private SSLConfig getJKSConfig() {
        SSLConfig config = SSLConfig.sslConfig();
        try {
            String keyStoreJKS = System.getProperty("KEYSTORE_JKS_FILE");
            String keyStorePassword = System.getProperty("KEYSTORE_PASSWORD");
            String trustStoreJKS = System.getProperty("TRUSTSTORE_JKS_FILE");
            String trustStorePassword = System.getProperty("TRUSTSTORE_PASSWORD");

            KeyStore keyStore = KeyStore.getInstance("JKS");
            KeyStore trustStore = KeyStore.getInstance("JKS");

            String jksPath = System.getProperty("CERTIFICATE_INFO_PATH");
            if (jksPath == null)
                jksPath = Thread.currentThread().getContextClassLoader()
                        .getResource("certs/" + System.getProperty("ENVIRONMENT") + "/jks/").getPath();

            keyStore.load(new FileInputStream(jksPath + "/" + keyStoreJKS), keyStorePassword.toCharArray());
            trustStore.load(new FileInputStream(jksPath + "/" + trustStoreJKS), trustStorePassword.toCharArray());

            SSLSocketFactory sslSocketFactory = new SSLSocketFactory(keyStore, keyStorePassword, trustStore);
            sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            config = SSLConfig.sslConfig().with().sslSocketFactory(sslSocketFactory).and().allowAllHostnames();
        } catch (Exception e) {
            logMan.error("Error while loading keystore  or trustStore>>>>>>>>>", e);
        }
        return config;
    }

    @SuppressWarnings("deprecation")
    private SSLConfig getPKCSConfig() {
        SSLConfig config = SSLConfig.sslConfig();
        try {
            String pcksFile = System.getProperty("PKCS_FILE");
            String pkcsPassword = System.getProperty("PKCS_PASSWORD");

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            String p12Path = System.getProperty("CERTIFICATE_INFO_PATH", Thread.currentThread().getContextClassLoader()
                    .getResource("certs/" + System.getProperty("ENVIRONMENT") + "/p12/").getPath());
            keyStore.load(new FileInputStream(p12Path + "/" + pcksFile), pkcsPassword.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, pkcsPassword.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManager[] trustManagers = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }
            } };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, new SecureRandom());
            SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext, new AllowAllHostnameVerifier());

            config = SSLConfig.sslConfig().with().sslSocketFactory(sslSocketFactory).and()
                    .x509HostnameVerifier(new AllowAllHostnameVerifier());

        } catch (Exception ex) {
            System.out.println("Error while loading keystore  or trustStore>>>>>>>>>");
            ex.printStackTrace();
        }
        return config;
    }

    /**
     * This method is used to compare Two Json String
     * @param expectedJson
     * @param actualJson
     * @return List of String comprising of all the mismatches found between two
     *         JSON Strings
     * @throws JsonSyntaxException
     * @author C112083
     * @modifiedDate 23/03/21
     */
    public List<String> compareJsonSchema(String expectedJson, String actualJson) {
        List<String> finalList = new ArrayList<String>();
        try {
            List<String> list = new ArrayList<String>();
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> firstMap = gson.fromJson(expectedJson, mapType);
            Map<String, Object> secondMap = gson.fromJson(actualJson, mapType);

            Map<String, Object> leftFlatMap = FlatMapUtil.flatten(firstMap);
            Map<String, Object> rightFlatMap = FlatMapUtil.flatten(secondMap);
            MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);

            difference.entriesOnlyOnLeft()
                    .forEach((key, value) -> list.add("Present only in Expected Json " + key + ": " + value));

            difference.entriesOnlyOnRight()
                    .forEach((key, value) -> list.add("Present only in Actual Json " + key + ": " + value));

            difference.entriesDiffering()
                    .forEach((key, value) -> list.add("Mismatch on common values " + key + ": " + value));

            finalList.addAll(list);
            // System.out.println(list.toString());
        } catch (JsonSyntaxException e) {
            logMan.error("Error in JSON comparison , error = " + e.getMessage());
        }
        return finalList;
    }

    public String fnCreateFinalUrlEndpoint(String baseURL, String sURI) {
        String baseFinalURI = null;
        try {
            baseFinalURI = baseURL + sURI;
            insertMessageToHtmlReport("Final Endpoint created successfully " + baseFinalURI);
            // step("Final Endpoint created successfully "+baseFinalURI);
        } catch (Exception e) {
            injectErrorToCucumberReport("Final Endpoint NOT created successfully " + baseFinalURI);
            // step("Final Endpoint NOT created successfully "+baseFinalURI);
            logMan.error("Error in fnCreateFinalUrlEndpoint method, Error = " + e.getMessage());
        }
        return baseFinalURI;
    }

    public boolean validateExceptedAndActualWithHelpOfJsonPath(Response result, String jsonPath, String expectedValue) {
        boolean returnValue = false;
        try {
            String actualValue = result.jsonPath().getString(jsonPath);
            if (actualValue.equalsIgnoreCase(expectedValue)) {
                Assert.assertTrue(
                        "Matched successfully, Expected = " + expectedValue + " and Actual value = " + actualValue,
                        true);
                insertMessageToHtmlReport(
                        "Matched Successfully , Expected = " + expectedValue + " and Actual = " + actualValue);
                insertMessageToHtmlReport("Response = " + result.asString());
                logMessage("Matched Successfully , Expected = " + expectedValue + " and Actual = " + actualValue);
                returnValue = true;
            } else {
                logMan.error("Mismatch in Expected and Actual. Expected value = " + expectedValue
                        + " and Actual value = " + actualValue);
                insertErrorMessageToHtmlReport("Mismatch in Expected and Actual. Expected value = " + expectedValue
                        + " and Actual value = " + actualValue);
                insertErrorMessageToHtmlReport("Response = " + result.asString());
                Assert.assertTrue("Mismatch in  Expected and Actual. Expected value = " + expectedValue
                        + " and Actual value = " + actualValue, false);
            }
        } catch (Exception t) {
            logMan.error("Error occurred in validateExceptedAndActualWithHelpOfJsonPath matching, Error = "
                    + t.getMessage());
        }
        return returnValue;
    }

    public void validateResponseStatusCode(Response response, int estatusCode) {
        try {
            if (response.getStatusCode() == Integer.valueOf(estatusCode)) {
                insertMessageToHtmlReport("Status code matched successfully. Expected Code =  " + estatusCode
                        + " Actual code = " + response.getStatusCode());
                Assert.assertTrue("Status code is matched successfully  " + estatusCode, true);
            } else {
                insertErrorMessageToHtmlReport("Status code NOT matched successfully. Expected code = " + estatusCode
                        + " Actual code = " + response.getStatusCode());
                logMan.error("Error: Mismatch in request code and response code. Expected code = " + estatusCode
                        + " Actual code = " + String.valueOf(response.getStatusCode()));
                Assert.assertTrue("Error: Mismatch in request code and response code. Expected code = " + estatusCode
                        + " Actual code = " + String.valueOf(response.getStatusCode()), false);
            }

        } catch (Exception e) {
            // insertErrorMessageToHtmlReport("Error occured in Response status code
            // "+response.getStatusCode());
            logMan.error("Error occured in Response status code");
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void validateResponseStatusLine(Response response, String estatusLine) {

        try {
            String astatusLine = response.getStatusLine();
            Assert.assertEquals("Correct Response status line ", estatusLine, astatusLine);
            insertMessageToHtmlReport("Response line actual value matched successfully " + response.getStatusLine());
            logMan.info("Response line actual value and matched successfully " + response.getStatusLine());

        } catch (Exception e) {
            logMan.error("Error occured in Response status line");
            insertErrorMessageToHtmlReport("Response line actual value NOT matched successfully " + e.getMessage());
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void validateResponseContentType(Response response, String econtentType) {
        try {
            String aRContentType = response.getContentType();
            Assert.assertEquals("Correct Response content type ", econtentType, aRContentType);
            insertMessageToHtmlReport("Content type matched successfully " + aRContentType);
            logMan.info("Content type matched successfully " + aRContentType);
        } catch (Exception e) {
            insertErrorMessageToHtmlReport("Content type NOT matched successfully " + e.getMessage());
            logMan.error("Error occured in Response in Content Type");
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void printResponseHeader(Response response) {

        try {
            Headers allHeaders = response.headers();
            for (Header header : allHeaders) {
                injectMessageToCucumberReport("Key: " + header.getName() + " Value: " + header.getValue());
                logMan.info("Key: " + header.getName() + " Value: " + header.getValue());
            }
        } catch (Exception e) {
            logMan.error("Error occured in Print Response headers");
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void printResponseBody(Response response) {
        try {
            String responseBody = response.getBody().prettyPrint();
            logMan.info("Response body is : " + responseBody);
            insertMessageToHtmlReport("Response body is : " + responseBody);
        } catch (Exception e) {
            insertErrorMessageToHtmlReport("Error occured in Print Response headers");
            logMan.error("Error occured in Print Response headers");
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void printResponseTime(Response response) {
        try {
            long responseTime = response.getTimeIn(TimeUnit.MILLISECONDS);
            insertMessageToHtmlReport("Response time is : " + responseTime);
            logMan.info("Response time is : " + responseTime);
        } catch (Exception e) {
            logMan.error("Error occured in Print Response headers");
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void validateTagValue(Response response, String key) {

        try {
            String resp = response.asString();
            JsonPath js = new JsonPath(resp);
            String[] splitVal = key.split(":");

            String tagKey = splitVal[0].toString();
            String tagValue = splitVal[1].toString();
            String acTagValue = response.path(tagKey);

            if (acTagValue.equals(tagValue)) {
                insertMessageToHtmlReport("Tag value matched successfully ");
                // step("Tag value matched successfully ");
                logMan.info("Tag value matched successfully");
            } else {
                insertErrorMessageToHtmlReport("Tag value NOT matched successfully ");
                logMan.error("Tag value NOT matched successfully");
            }
        } catch (Exception e) {
            logMan.error("Error in validateTagValue method, Error = " + e.getMessage());
        }
    }

    public void validateResponseBodyReceived(Response response) {

        String respBody = response.asString();
        if (respBody.isEmpty()) {
            insertErrorMessageToHtmlReport("Response body is empty ");
            logMan.error("Response body is empty ");
        } else {
            insertMessageToHtmlReport("Response body is received successfully ");
            logMessage("Response body is received successfully");
        }
    }

    public void validateBodyContains(Response response, String strBodyValue) {

        try {
            String responseBody = response.getBody().asString();
            Assert.assertTrue("Response body contains : ", responseBody.contains(strBodyValue));
            insertMessageToHtmlReport("Response Body contains : " + strBodyValue);
            logMan.info("Response Body contains : " + responseBody);
        } catch (Exception e) {
            insertErrorMessageToHtmlReport("Error occured in Body contains function");
            logMan.error("Error occured in Body contains function");
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void validateBodyContainsMultipleKeyTags(Response response, String strTagValue) {

        try {
            String responseBody = response.getBody().asString();
            String[] ArrsplitVal = strTagValue.split("\\|");
            for (String currVal : ArrsplitVal) {
                Assert.assertTrue("Response body contains : ", responseBody.contains(currVal));
                insertMessageToHtmlReport("Response Body contains : " + currVal);
                logMan.info("Response Body contains : " + currVal);
            }
        } catch (Exception e) {
            insertErrorMessageToHtmlReport("Response Body DOESNOT contains the required tag ");
            logMan.error("Error occured in Body contains function");
            logMan.error("Error description : " + e.getMessage());
        }
    }

    public void setValueForJsonKey(JSONObject jsonObject, String key) {
        this.value = (String) jsonObject.get(key);
        // return jsonObject.get(key);
    }

    public String getValueForJsonKey() {
        // to fetch the value, first call parseJson method and then call this method
        return value;
    }

    public void parseJson(JSONObject json, String key) {
        try {
            boolean keyExists = json.has(key);
            Iterator<?> iterator;
            String nextKey;
            if (!keyExists) {
                iterator = json.keys();
                while (iterator.hasNext()) {
                    nextKey = (String) iterator.next();
                    try {
                        if (json.get(nextKey) instanceof JSONObject) {
                            if (keyExists == false) {
                                parseJson(json.getJSONObject(nextKey), key);
                            }

                        } else if (json.get(nextKey) instanceof JSONArray) {
                            JSONArray jsonArray = json.getJSONArray(nextKey);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String jsonArrayString = jsonArray.get(i).toString();
                                JSONObject innerJsonObject = new JSONObject(jsonArrayString);
                                if (keyExists == false) {
                                    parseJson(innerJsonObject, key);
                                }
                            }
                        }

                    } catch (Exception e) {
                        logMan.error("Error in Json parsing, Error= " + e.getMessage());
                    }
                }
            } else {
                setValueForJsonKey(json, key);
            }
        } catch (Exception e) {
            logMan.error("Error in parseJson method, Error = " + e.getMessage());
        }
    }

    public String parseJsonPayload(String filePath, String parameter) {
        String value = null;
        try {
            Map<String, Map<String, String>> deSerialData = getDeserializeDataForJsonPayLoad();
            Map<String, String> data = deSerialData.get(parameter);

            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = bufferedReader.readLine();
            StringBuilder builder = new StringBuilder();
            while (line != null) {
                builder.append(line);
                line = bufferedReader.readLine();
            }
            value = builder.toString();

            int regexIndex = value.indexOf("$");
            while (regexIndex < value.length()) {
                int nextRegexIndex = value.indexOf("$", regexIndex + 1);
                String keyNameWithRegex = value.substring(regexIndex, nextRegexIndex + 1);
                String keyName = keyNameWithRegex.replaceAll("\\$", "");
                if (data.containsKey(keyName)) {
                    String valueToBeReplaced = data.get(keyName);
                    if (valueToBeReplaced.equalsIgnoreCase("NA")) {
                        int colonIndex = value.lastIndexOf(":", regexIndex);
                        int tempIndex = value.lastIndexOf("\"", colonIndex);
                        int keyIndex = value.lastIndexOf("\"", tempIndex - 1);

                        int firstQuotesIndex = value.indexOf("\"", regexIndex);
                        int secondQuotesIndex = value.indexOf("\"", firstQuotesIndex + 1);
                        if (secondQuotesIndex == -1) {
                            secondQuotesIndex = firstQuotesIndex + 1;
                        }

                        String firstPart = value.substring(0, keyIndex);
                        String secondPart = value.substring(secondQuotesIndex);

                        value = firstPart + secondPart;
                        regexIndex = value.indexOf("$");
                        if (regexIndex == -1) {
                            break;
                        }
                    } else {
                        String firstPart = value.substring(0, regexIndex);
                        String secondPart = value.substring(nextRegexIndex + 1);
                        value = firstPart + valueToBeReplaced + secondPart;
                        regexIndex = value.indexOf("$");
                        if (regexIndex == -1) {
                            break;
                        }
                    }

                } else {
                    logMan.error("Please check the columnName written in jsonPayload with key as, = " + keyName);
                }
            }
            System.out.println(value);
        } catch (Throwable e) {
            logMan.error("Error in parseJsonPayload method, Error = " + e.getMessage());
        }
        return value;
    }

    public String parseExpectedJsonResponseFromTextFile(String fileName, Map<String, String> data) {
        String value = null;
        String responsePath = System.getProperty("user.dir") + "/src/test/java/com/boi/grp/response/";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(responsePath + fileName));
            String line = bufferedReader.readLine();
            StringBuilder builder = new StringBuilder();
            while (line != null) {
                builder.append(line);
                line = bufferedReader.readLine();
            }
            value = builder.toString();
            if (data.size() > 0) {
                int regexIndex = value.indexOf("$");
                if (!(regexIndex == -1)) {
                    while (regexIndex < value.length()) {
                        int nextRegexIndex = value.indexOf("$", regexIndex + 1);
                        String keyNameWithRegex = value.substring(regexIndex, nextRegexIndex + 1);
                        String keyName = keyNameWithRegex.replaceAll("\\$", "");
                        if (data.containsKey(keyName)) {
                            String valueToBeReplaced = data.get(keyName);
                            String firstPart = value.substring(0, regexIndex);
                            String secondPart = value.substring(nextRegexIndex + 1);
                            value = firstPart + valueToBeReplaced + secondPart;
                            regexIndex = value.indexOf("$");
                            if (regexIndex == -1) {
                                break;
                            }
                        } else {
                            logMan.error(
                                    "Please check the columnName written in jsonPayload with key as, = " + keyName);
                        }
                    }
                }
            }
            System.out.println(value);
        } catch (Throwable e) {
            logMan.error("Error in parseJsonPayload method, Error = " + e.getMessage());
        }
        return value;
    }

    public Map<String, Map<String, String>> getDeserializeDataForJsonPayLoad() {
        Map<String, Map<String, String>> mappedData = new HashMap<String, Map<String, String>>();

        try {
            FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/target/jsonPayLoadData");
            ObjectInputStream ois = new ObjectInputStream(fis);
            mappedData = (Map<String, Map<String, String>>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            logMan.error("Error in getDeserializeDataForMCConfiguration, Error = " + e.getMessage());
            return mappedData;
        } catch (ClassNotFoundException e) {
            logMan.error("Class not found, Error = " + e.getMessage());
            return mappedData;
        }
        logMan.info("Deserialization of excel data is complete");
        return mappedData;
    }

    public Map<String, String> setHeader(String key, String value) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        map.put("", "");
        this.authenticationHeaders.putAll(map);
        return map;
    }
}