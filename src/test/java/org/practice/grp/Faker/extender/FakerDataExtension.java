package org.practice.grp.Faker.extender;

import java.math.BigInteger;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.PathNotFoundException;

/*
 **
 * @author A113845
 *
 */

public class FakerDataExtension {
    private final FakerExtension faker;

    protected FakerDataExtension(FakerExtension faker) {
        this.faker = faker;
        faker.getLoale().toString();
    }

    public String fullAddress() {
        String streetAddress = faker.address().streetAddress();
        String secStreetAddress = faker.address().secondaryAddress();
        // String buildingNum = faker.address().buildingNumber();
        String zipCode = zipCode();
        String postalId = zipCode.split(" ")[0];
        Map<String, String> countyInfo = getCountyData(postalId);
        String city = countyInfo.get("city");
        String county = countyInfo.get("county");

        return String.format("%s; %s; %s; %s %s", streetAddress, secStreetAddress, city,
                county.length() > 0 ? "Co. " + county : "", zipCode);
    }

    public String state() {
        String zipCode = getEirCode();
        Map<String, String> countyInfo = getCountyData(zipCode);
        return countyInfo.get("county").toString();
    }

    public String stateAbbr() {
        return state();
    }

    public String city() {
        String zipCode = getEirCode();
        Map<String, String> countyInfo = getCountyData(zipCode);
        return countyInfo.get("city").toString();
    }

    public String cityName() {
        String zipCode = getEirCode();
        Map<String, String> countyInfo = getCountyData(zipCode);
        return countyInfo.get("city").toString();
    }

    public String zipCode() {
        String eir_code = getEirCode();
        String eir_code_part = faker.bothify("???#", true);
        return String.format("%s %s", eir_code, eir_code_part);
    }

    public String phoneNumber() {
        String phoneNumExp = "";
        try {
            phoneNumExp = faker.evalJsonPath("phoneNumber").getAsString();
        } catch (PathNotFoundException e) {
        }

        return faker.regexify(phoneNumExp).toUpperCase();
    }

    public String iban() {
        String countryCode = faker.getLoale().getCountryCode();

        String basicBankAccountNumber = faker.regexify("BOFI900017(\\d){8}");
        String checkSum = calculateIbanChecksum(countryCode, basicBankAccountNumber);
        return countryCode + checkSum + basicBankAccountNumber;
    }

    private String getEirCode() {
        String eirCodeExp = "";
        try {
            eirCodeExp = faker.evalJsonPath("eir_code").getAsString();
        } catch (PathNotFoundException e) {
        }
        return faker.regexify(eirCodeExp).toUpperCase();
    }

    private static String calculateIbanChecksum(String countryCode, String basicBankAccountNumber) {
        String basis = basicBankAccountNumber + countryCode + "00";

        StringBuilder sb = new StringBuilder();
        char[] characters = basis.toLowerCase().toCharArray();
        for (char c : characters) {
            if (Character.isLetter(c)) {
                sb.append(String.valueOf((c - 'a') + 10));
            } else {
                sb.append(c);
            }
        }

        int mod97 = new BigInteger(sb.toString()).mod(BigInteger.valueOf(97L)).intValue();
        return StringUtils.leftPad(String.valueOf(98 - mod97), 2, '0');
    }

    private Map<String, String> getCountyData(String zipCode) {
        String filterPredicate = "eir_map." + zipCode.trim();
        JsonObject filteredJson = faker.evalJsonPath("{\"city\":\"\",\"county\":\"\"}", "").getAsJsonObject();

        try {
            filteredJson = faker.evalJsonPath(filterPredicate).getAsJsonArray().get(0).getAsJsonObject();
        } catch (PathNotFoundException e) {
        }
        return new Gson().fromJson(filteredJson, Map.class);
    }
}