package org.practice.grp.Faker.enums;

import com.github.javafaker.Address;
import com.github.javafaker.Business;
import com.github.javafaker.Code;
import com.github.javafaker.Company;
import com.github.javafaker.Country;
import com.github.javafaker.DateAndTime;
import com.github.javafaker.Demographic;
import com.github.javafaker.Finance;
import com.github.javafaker.IdNumber;
import com.github.javafaker.Internet;
import com.github.javafaker.Job;
import com.github.javafaker.Name;
import com.github.javafaker.Nation;
import com.github.javafaker.Number;
import com.github.javafaker.Options;
import com.github.javafaker.PhoneNumber;
import org.practice.grp.Faker.extender.FakerDataExtension;

/**
 * An enum for the parent groups of the fields supported by JavaFaker
 *
 * @author A113845
 *
 */

public enum FakerFieldType {
    ADDRESS("Address", "address", Address.class), BUSINESS("Business", "business", Business.class),
    CODE("Code", "code", Code.class), COMPANY("Company", "company", Company.class),
    COUNTRY("Country", "country", Country.class), DATE_TIME("Date Time", "date", DateAndTime.class),
    DEMOGRAPHIC("Demographic", "demographic", Demographic.class), FINANCE("Finance", "finance", Finance.class),
    ID_NUMBER("Id Number", "idNumber", IdNumber.class), INTERNET("Internet", "internet", Internet.class),
    JOB("Job", "job", Job.class), NAME("Name", "name", Name.class), NATION("Nation", "nation", Nation.class),
    NUMBER("Number", "number", Number.class), OPTIONS("Options", "options", Options.class),
    PHONE_NUMBER("Phone Number", "phoneNumber", PhoneNumber.class),
    FAKER_DATA_EXTENSION("Faker Data Extension", "dataExtenstion", FakerDataExtension.class);

    private String reflectionMethodName;
    private FakerFieldType parentField;
    private Class<?> fakerClass;
    private String fieldTypeName;

    private FakerFieldType(String typeName, String inlineMethodName, Class<?> fkrClass) {
        reflectionMethodName = inlineMethodName;
        fakerClass = fkrClass;
        parentField = null;
        fieldTypeName = typeName;
    }

    public String getReflection() {
        return reflectionMethodName;
    }

    public boolean isRootNode() {
        return parentField == null;
    }

    public FakerFieldType getParent() {
        return parentField;
    }

    public Class<?> getFakerClass() {
        return fakerClass;
    }

    public String getMethodName() {
        return reflectionMethodName;
    }

    @Override
    public String toString() {
        return fieldTypeName;
    }
}