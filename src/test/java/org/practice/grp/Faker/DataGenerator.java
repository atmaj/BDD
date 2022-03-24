package org.practice.grp.Faker;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import org.practice.grp.Faker.enums.FakerField;
import org.practice.grp.Faker.enums.FakerFieldType;
import org.practice.grp.Faker.enums.FakerLocale;
import org.practice.grp.Faker.extender.FakerExtension;

/**
 * @author A113845
 *
 *         This is the primary class to generate random data using JavaFaker
 *
 */
public class DataGenerator {
    FakerLocale dataLanguage;
    FakeValuesService service;
    FakerExtension generator;

    public DataGenerator() {
        this(FakerLocale.getDefault());
    }

    public DataGenerator(FakerLocale language) {
        dataLanguage = language;
        service = new FakeValuesService(new Locale(dataLanguage.toString()), new RandomService());
        generator = new FakerExtension(language);
    }

    /**
     * To generate fake data for a specific FakerField.
     *
     * @param field - This is an onject of Fakerfield enum
     * @return It will return the data for the field passed to the method
     */
    public Object getData(FakerField field) {
        Object data = null;
        try {
            FakerFieldType tmpField = field.getParent();
            Object genFaker = new FakerExtension();
            Class<?> reflectionClass = FakerExtension.class;
            List<FakerFieldType> fieldStack = new ArrayList<FakerFieldType>();
            fieldStack.add(0, tmpField);
            while (!tmpField.isRootNode()) {
                fieldStack.add(0, tmpField.getParent());
                tmpField = tmpField.getParent();
            }

            for (FakerFieldType fakerField : fieldStack) {
                Method generatorMethod = reflectionClass.getMethod(fakerField.getMethodName());
                reflectionClass = fakerField.getFakerClass();
                genFaker = fakerField.getFakerClass().cast(generatorMethod.invoke(genFaker));
            }
            Method generatorMethod = reflectionClass.getMethod(field.getMethodName());
            data = generatorMethod.invoke(genFaker);
        } catch (Exception e) {
            // e.printStackTrace();
            System.err.println(field.toString());
        }
        return data;
    }

    /**
     * To generate a CSV of fake data for a specific FakerField list.
     *
     *
     * @param fieldList   - List of fields for which data has to be generated
     * @param recordCount - Number of dataset to be generated
     * @throws IOException
     */
    public void getData(List<FakerField> fieldList, int recordCount) {
        getData(fieldList, recordCount, System.getProperty("user.dir") + "/generatedData.csv");
    }

    public void getData(String[] fieldNames, int recordCount, String resultFilePath) {
        getData(FakerField.getFields(fieldNames), recordCount, resultFilePath);
    }

    public void getData(String[] fieldNames, int recordCount) {
        getData(FakerField.getFields(fieldNames), recordCount);
    }

    /**
     * To generate a CSV of fake data for a specific FakerField list.
     *
     *
     * @param fieldList      - List of fields for which data has to be generated
     * @param recordCount    - Number of dataset to be generated
     * @param resultFilePath - Number of dataset to be generated
     * @throws IOException
     */
    public void getData(List<FakerField> fieldList, int recordCount, String resultFilePath) {
        try {
            FileWriter writer = new FileWriter(resultFilePath);
            for (FakerField fakerField : fieldList) {
                writer.append(fakerField.toString() + ",");
            }
            for (int counter = 0; counter < recordCount; counter++) {
                writer.append(System.getProperty("line.separator"));
                for (FakerField fakerField : fieldList) {
                    writer.append(getData(fakerField) + ",");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * To generate data based on a regular expression
     *
     * @param expressString - Regular expression to generate data
     * @return A fake data matching the regular expression
     */
    public String resolveExpression(String expressString) {
        return service.regexify(expressString);
    }

    /**
     * To generate a random word of specific length
     *
     * @param wordSize - Length of word
     * @return - A String of specified length
     */
    public String getRandomWord(int wordSize) {
        String expression = String.join("", Collections.nCopies(wordSize, "?"));
        return shuffle(service.letterify(expression));
    }

    /**
     * To generate a random number of specific length
     *
     * @param numberLength - size of number
     * @return - A string of numbers of specified length
     */
    public String getRandomNumber(int numberLength) {
        String expression = String.join("", Collections.nCopies(numberLength, "#"));
        return service.numerify(expression);
    }

    /**
     * To generate a random alphanumeric word of specific length
     *
     * @param size - size of word
     * @return - A string of \specified length
     */
    public String getAlphaNumeric(int size) {
        Random rnd = new Random();
        int alphaSize = rnd.nextInt(size / 2);
        int numSize = size - alphaSize;
        return shuffle(getAlphaNumeric(alphaSize, numSize));
    }

    /**
     * To generate a random alphanumeric word of specific length
     *
     * @param alphaSize - Number of alphabets to be included
     * @param numSize   - Number of digits to include
     * @return - An alphanumeric string with specified number of alphabets and
     *         digits
     */
    public String getAlphaNumeric(int alphaSize, int numSize) {
        String aplhaString = getRandomWord(alphaSize);
        String numString = getRandomNumber(numSize);
        return shuffle(aplhaString + numString);
    }

    // public String getRandomEmailAddress() {
    // Random rnd = new Random();
    // String emailId = getRandomWord(rnd.ints(1, 8).findFirst().getAsInt()) +
    // "."
    // + getRandomWord(rnd.ints(1, 9).findFirst().getAsInt());
    // String domain = getRandomWord(rnd.ints(2, 6).findFirst().getAsInt()) +
    // "." + getRandomWord(3);
    // return String.format("%s@%s", emailId, domain);
    // }

    // public String getRandomEmailAddress(int emailIdLength) {
    // Random rnd = new Random();
    // int idPart1Length = rnd.ints(1, emailIdLength /
    // 2).findFirst().getAsInt();
    // int idPart2Length = emailIdLength - idPart1Length - 1;
    // String emailId = getRandomWord(idPart1Length) + "." +
    // getRandomWord(idPart2Length);
    // String domain = getRandomWord(rnd.ints(2, 6).findFirst().getAsInt()) +
    // "." + getRandomWord(3);
    // return String.format("%s@%s", emailId, domain);
    // }

    /**
     * To shuffle a string
     *
     * @param raw - Actual string
     * @return - A shuffled String
     */
    private String shuffle(String raw) {
        List<Character> charList = new ArrayList<Character>();
        for (char c : raw.toCharArray()) {
            charList.add(c);
        }
        StringBuilder output = new StringBuilder(raw.length());
        while (charList.size() != 0) {
            int randPicker = (int) (Math.random() * charList.size());
            output.append(charList.remove(randPicker));
        }
        return output.toString();
    }
}