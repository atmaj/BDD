package org.practice.grp.Faker.enums;

import java.util.Locale;

/**
 * An enum for to configure the locale to generate data using JavaFaker
 *
 * @author A113845
 *
 */

public enum FakerLocale {

    ENGLISH_UK("en_UK"), ENGLISH_IE("en_IE"), IRISH("ga_IE");

    private String locale;
    private String countryCode;

    private FakerLocale(String localeCode) {
        locale = localeCode;
        if (localeCode.contains("_")) {
            String[] localeSplits = localeCode.split("_");
            countryCode = localeSplits[localeSplits.length - 1];
        } else {
            countryCode = localeCode;
        }
    }

    private FakerLocale(String localeCode, String countryCode) {
        locale = localeCode;
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        return locale;
    }

    /**
     * To get the default locale from the enum.
     *
     * @return An enum object for Locale
     */
    public static FakerLocale getDefault() {
        return ENGLISH_IE;
    }

    public Locale getLocale() {
        return new Locale(locale);
    }

    public String getCountryCode() {
        return countryCode;
    };
}