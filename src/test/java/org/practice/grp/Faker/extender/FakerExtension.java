package org.practice.grp.Faker.extender;

import com.github.javafaker.Faker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import org.practice.grp.Faker.enums.FakerLocale;

/*
 **
 * @author A113845
 *
 */

public class FakerExtension extends Faker {
    private final FakerLocale fakerLocale;
    private final FakerDataExtension dataExtenstion;
    private String extensionMappingJson;
    private ParseContext parseContext;

    public FakerExtension() {
        this(FakerLocale.getDefault());
    }

    public FakerExtension(FakerLocale language) {
        super(language.getLocale());
        fakerLocale = language;
        Configuration conf = Configuration.builder().jsonProvider(new GsonJsonProvider())
                .options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS).build();
        parseContext = JsonPath.using(conf);
        extensionMappingJson = ((JsonArray) parseContext
                .parse(getClass().getClassLoader().getResourceAsStream("faker_config/config.json"))
                .read("$." + fakerLocale.toString())).get(0).toString();
        dataExtenstion = new FakerDataExtension(this);
    }

    public FakerLocale getLoale() {
        return fakerLocale;
    }

    public FakerDataExtension dataExtenstion() {
        return dataExtenstion;
    }

    public JsonElement evalJsonPath(String jsonPath) {
        return evalJsonPath(extensionMappingJson, jsonPath);
    }

    public JsonElement evalJsonPath(String json, String jsonPath) {
        return ((JsonArray) parseContext.parse(json).read(jsonPath.length() > 0 ? "$." + jsonPath : "$")).get(0);
    }
}