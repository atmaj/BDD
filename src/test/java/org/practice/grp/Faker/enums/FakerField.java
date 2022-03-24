package org.practice.grp.Faker.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An enum for the fields supported by JavaFaker
 *
 * @author A113845
 *
 */
public enum FakerField {
    CITY(FakerFieldType.FAKER_DATA_EXTENSION, "City", "city"),
    CITY_NAME(FakerFieldType.FAKER_DATA_EXTENSION, "City Name", "cityName"),
    FULL_ADDRESS(FakerFieldType.FAKER_DATA_EXTENSION, "Full Address", "fullAddress"),
    STATE(FakerFieldType.FAKER_DATA_EXTENSION, "State", "state"),
    STATE_ABBR(FakerFieldType.FAKER_DATA_EXTENSION, "State Abbr", "stateAbbr"),
    ZIP_CODE(FakerFieldType.FAKER_DATA_EXTENSION, "Zip Code", "zipCode"),
    IBAN(FakerFieldType.FAKER_DATA_EXTENSION, "Iban", "iban"),
    CELL_PHONE(FakerFieldType.FAKER_DATA_EXTENSION, "Cell Phone", "phoneNumber"),
    PHONE_NUMBER(FakerFieldType.FAKER_DATA_EXTENSION, "Phone Number", "phoneNumber"),

    BUILDING_NUMBER(FakerFieldType.ADDRESS, "Building Number", "buildingNumber"),
    CITY_PREFIX(FakerFieldType.ADDRESS, "City Prefix", "cityPrefix"),
    CITY_SUFFIX(FakerFieldType.ADDRESS, "City Suffix", "citySuffix"),
    COUNTRY(FakerFieldType.ADDRESS, "Country", "country"),
    COUNTRY_CODE(FakerFieldType.ADDRESS, "Country Code", "countryCode"),
    LATITUDE(FakerFieldType.ADDRESS, "Latitude", "latitude"),
    LONGITUDE(FakerFieldType.ADDRESS, "Longitude", "longitude"),
    SECONDARY_ADDRESS(FakerFieldType.ADDRESS, "Secondary Address", "secondaryAddress"),
    STREET_ADDRESS(FakerFieldType.ADDRESS, "Street Address", "streetAddress"),
    STREET_ADDRESS_NUMBER(FakerFieldType.ADDRESS, "Street Address Number", "streetAddressNumber"),
    STREET_NAME(FakerFieldType.ADDRESS, "Street Name", "streetName"),
    STREET_PREFIX(FakerFieldType.ADDRESS, "Street Prefix", "streetPrefix"),
    STREET_SUFFIX(FakerFieldType.ADDRESS, "Street Suffix", "streetSuffix"),
    TIME_ZONE(FakerFieldType.ADDRESS, "Time Zone", "timeZone"),

    CREDIT_CARD_EXPIRY(FakerFieldType.BUSINESS, "Credit Card Expiry", "creditCardExpiry"),
    CREDIT_CARD_NUMBER(FakerFieldType.BUSINESS, "Credit Card Number", "creditCardNumber"),
    CREDIT_CARD_TYPE(FakerFieldType.BUSINESS, "Credit Card Type", "creditCardType"),

    ASIN(FakerFieldType.CODE, "Asin", "asin"), EAN13(FakerFieldType.CODE, "Ean13", "ean13"),
    EAN8(FakerFieldType.CODE, "Ean8", "ean8"), GTIN13(FakerFieldType.CODE, "Gtin13", "gtin13"),
    GTIN8(FakerFieldType.CODE, "Gtin8", "gtin8"), IMEI(FakerFieldType.CODE, "Imei", "imei"),
    ISBN10(FakerFieldType.CODE, "Isbn10", "isbn10"), ISBN13(FakerFieldType.CODE, "Isbn13", "isbn13"),
    ISBN_GROUP(FakerFieldType.CODE, "Isbn Group", "isbnGroup"), ISBN_GS1(FakerFieldType.CODE, "Isbn Gs1", "isbnGs1"),
    ISBN_REGISTRANT(FakerFieldType.CODE, "Isbn Registrant", "isbnRegistrant"),

    BS(FakerFieldType.COMPANY, "Bs", "bs"), BUZZWORD(FakerFieldType.COMPANY, "Buzzword", "buzzword"),
    CATCH_PHRASE(FakerFieldType.COMPANY, "Catch Phrase", "catchPhrase"),
    INDUSTRY(FakerFieldType.COMPANY, "Industry", "industry"), LOGO(FakerFieldType.COMPANY, "Logo", "logo"),
    COMPANY_NAME(FakerFieldType.COMPANY, "Name", "name"),
    PROFESSION(FakerFieldType.COMPANY, "Profession", "profession"),
    COMPANY_SUFFIX(FakerFieldType.COMPANY, "Suffix", "suffix"), COMPANY_URL(FakerFieldType.COMPANY, "Url", "url"),

    CAPITAL(FakerFieldType.COUNTRY, "Capital", "capital"),
    COUNTRY_CODE2(FakerFieldType.COUNTRY, "Country Code2", "countryCode2"),
    COUNTRY_CODE3(FakerFieldType.COUNTRY, "Country Code3", "countryCode3"),
    CURRENCY(FakerFieldType.COUNTRY, "Currency", "currency"),
    CURRENCY_CODE(FakerFieldType.COUNTRY, "Currency Code", "currencyCode"),
    FLAG(FakerFieldType.COUNTRY, "Flag", "flag"), COUNTRY_NAME(FakerFieldType.COUNTRY, "Name", "name"),

    BIRTHDAY(FakerFieldType.DATE_TIME, "Birthday", "birthday"),

    DEMONYM(FakerFieldType.DEMOGRAPHIC, "Demonym", "demonym"),
    EDUCATIONAL_ATTAINMENT(FakerFieldType.DEMOGRAPHIC, "Educational Attainment", "educationalAttainment"),
    MARITAL_STATUS(FakerFieldType.DEMOGRAPHIC, "Marital Status", "maritalStatus"),
    RACE(FakerFieldType.DEMOGRAPHIC, "Race", "race"), SEX(FakerFieldType.DEMOGRAPHIC, "Sex", "sex"),

    BIC(FakerFieldType.FINANCE, "Bic", "bic"), CREDIT_CARD(FakerFieldType.FINANCE, "Credit Card", "creditCard"),

    INVALID(FakerFieldType.ID_NUMBER, "Invalid", "invalid"),
    SSN_VALID(FakerFieldType.ID_NUMBER, "Ssn Valid", "ssnValid"), VALID(FakerFieldType.ID_NUMBER, "Valid", "valid"),

    DOMAIN_NAME(FakerFieldType.INTERNET, "Domain Name", "domainName"),
    DOMAIN_SUFFIX(FakerFieldType.INTERNET, "Domain Suffix", "domainSuffix"),
    DOMAIN_WORD(FakerFieldType.INTERNET, "Domain Word", "domainWord"),
    EMAIL_ADDRESS(FakerFieldType.INTERNET, "Email Address", "emailAddress"),
    IMAGE(FakerFieldType.INTERNET, "Image", "image"),
    IP_V4_ADDRESS(FakerFieldType.INTERNET, "Ip V4 Address", "ipV4Address"),
    IP_V4_CIDR(FakerFieldType.INTERNET, "Ip V4 Cidr", "ipV4Cidr"),
    IP_V6_ADDRESS(FakerFieldType.INTERNET, "Ip V6 Address", "ipV6Address"),
    IP_V6_CIDR(FakerFieldType.INTERNET, "Ip V6 Cidr", "ipV6Cidr"),
    MAC_ADDRESS(FakerFieldType.INTERNET, "Mac Address", "macAddress"),
    PASSWORD(FakerFieldType.INTERNET, "Password", "password"),
    PRIVATE_IP_V4_ADDRESS(FakerFieldType.INTERNET, "Private Ip V4 Address", "privateIpV4Address"),
    PUBLIC_IP_V4_ADDRESS(FakerFieldType.INTERNET, "Public Ip V4 Address", "publicIpV4Address"),
    SAFE_EMAIL_ADDRESS(FakerFieldType.INTERNET, "Safe Email Address", "safeEmailAddress"),
    SLUG(FakerFieldType.INTERNET, "Slug", "slug"), URL(FakerFieldType.INTERNET, "Url", "url"),
    USER_AGENT_ANY(FakerFieldType.INTERNET, "User Agent Any", "userAgentAny"),
    UUID(FakerFieldType.INTERNET, "Uuid", "uuid"),

    FIELD(FakerFieldType.JOB, "Field", "field"), KEY_SKILLS(FakerFieldType.JOB, "Key Skills", "keySkills"),
    POSITION(FakerFieldType.JOB, "Position", "position"), SENIORITY(FakerFieldType.JOB, "Seniority", "seniority"),
    JOB_TITLE(FakerFieldType.JOB, "Title", "title"),

    BLOOD_GROUP(FakerFieldType.NAME, "Blood Group", "bloodGroup"),
    FIRST_NAME(FakerFieldType.NAME, "First Name", "firstName"), FULL_NAME(FakerFieldType.NAME, "Full Name", "fullName"),
    LAST_NAME(FakerFieldType.NAME, "Last Name", "lastName"), NAME(FakerFieldType.NAME, "Name", "name"),
    WITH_MIDDLE_NAME(FakerFieldType.NAME, "With Middle Name", "nameWithMiddle"),
    PREFIX(FakerFieldType.NAME, "Prefix", "prefix"), SUFFIX(FakerFieldType.NAME, "Suffix", "suffix"),
    TITLE(FakerFieldType.NAME, "Title", "title"), USERNAME(FakerFieldType.NAME, "Username", "username"),

    CAPITAL_CITY(FakerFieldType.NATION, "Capital City", "capitalCity"),
    LANGUAGE(FakerFieldType.NATION, "Language", "language"),
    NATIONALITY(FakerFieldType.NATION, "Nationality", "nationality"),

    DIGIT(FakerFieldType.NUMBER, "Digit", "digit"),
    RANDOM_NUMBER(FakerFieldType.NUMBER, "Random Number", "randomNumber"),
    RANDOM_DIGIT(FakerFieldType.NUMBER, "Random Digit", "randomDigit"),
    RANDOM_DIGIT_NON_ZERO(FakerFieldType.NUMBER, "Non Zero Digit", "randomDigitNotZero"),

    EXTENSION(FakerFieldType.PHONE_NUMBER, "Extension", "extension"),
    SUBSCRIBER_NUMBER(FakerFieldType.PHONE_NUMBER, "Subscriber Number", "subscriberNumber");

    private String reflectionMethodName;
    private String fieldName;
    private FakerFieldType parentField;

    private FakerField(FakerFieldType parentType, String fName, String inlineMethodName) {
        reflectionMethodName = inlineMethodName;
        parentField = parentType;
        fieldName = fName;
    }

    public String getReflection() {
        return reflectionMethodName;
    }

    public Class<?> getFakerClass() {
        return parentField.getFakerClass();
    }

    public FakerFieldType getParent() {
        return parentField;
    }

    public String getMethodName() {
        return reflectionMethodName;
    }

    /**
     * To get a list of all FakerFields tagged to a specific group
     *
     * @param parentType - Parent group of fields
     * @return A list of enum objects
     */
    public static List<FakerField> getAllFields(FakerFieldType parentType) {
        List<FakerField> result = Stream.of(values()).filter(fkrFld -> fkrFld.parentField.equals(parentType))
                .collect(Collectors.toList());
        return result;
    }

    public static List<FakerField> getFields(List<String> fieldNames) {
        List<FakerField> result = Stream.of(values()).filter(fkrFld -> fieldNames.contains(fkrFld.toString()))
                .collect(Collectors.toList());
        return result;
    }

    public static List<FakerField> getFields(String[] fieldNames) {
        return getFields(Arrays.asList(fieldNames));
    }

    @Override
    public String toString() {
        return fieldName;
    }
}