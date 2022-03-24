package org.practice.grp.dataBase;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TeraDataEncryPassKeyGenerator {
    public static final int BOUNDARY = 512;

    public static void main(String[] args)
            throws ClassNotFoundException, GeneralSecurityException, IOException, SQLException {
        if (args.length != 8)
            throw new IllegalArgumentException(
                    "Parameters: Transformation KeySizeInBits MAC PasswordEncryptionKeyFileName EncryptedPasswordFileName Hostname Username Password");

        String sTransformation = args[0]; // transformation argument for the
        // Cipher.getInstance method
        String sKeySizeInBits = args[1]; // keysize argument for the
        // KeyGenerator.init method
        String sMac = args[2]; // algorithm argument for the Mac.getInstance
        // method
        String sPasswordEncryptionKeyFileName = args[3];
        String sEncryptedPasswordFileName = args[4];
        String sHostname = args[5];
        String sUsername = args[6];
        String sPassword = args[7];

        createPasswordEncryptionKeyFile(sTransformation, sKeySizeInBits, sMac, sPasswordEncryptionKeyFileName);

        createEncryptedPasswordFile(sPasswordEncryptionKeyFileName, sEncryptedPasswordFileName, sPassword);

        decryptPassword(sPasswordEncryptionKeyFileName, sEncryptedPasswordFileName);

        String sResource1 = "file:" + sPasswordEncryptionKeyFileName;
        String sResource2 = "file:" + sEncryptedPasswordFileName;

        String sEncryptedPassword = "ENCRYPTED_PASSWORD(" + sResource1 + "," + sResource2 + ")";

        Class.forName("com.teradata.jdbc.TeraDriver");

        String sURL = "jdbc:teradata://" + sHostname + "/TMODE=ANSI,CHARSET=UTF8";
        System.out.println("Connecting to " + sURL + " with user " + sUsername + " and password " + sEncryptedPassword);
        Connection con = DriverManager.getConnection(sURL, sUsername, sEncryptedPassword);

        DatabaseMetaData dbmd = con.getMetaData();
        System.out.println(
                "Teradata JDBC Driver " + dbmd.getDriverVersion() + " and " + dbmd.getDatabaseProductVersion());

        con.close();

    } // end main

    public static void createPasswordEncryptionKeyFile(String sTransformation, String sKeySizeInBits, String sMac,
                                                       String sPasswordEncryptionKeyFileName) throws GeneralSecurityException, IOException {
        String sAlgorithm = sTransformation.replaceFirst("/.*", "");

        KeyGenerator kgCipher = KeyGenerator.getInstance(sAlgorithm);

        if (!"-default".equals(sKeySizeInBits))
            kgCipher.init(Integer.parseInt(sKeySizeInBits));

        SecretKey keyCipher = kgCipher.generateKey();

        KeyGenerator kgMac = KeyGenerator.getInstance(sMac);
        SecretKey keyMac = kgMac.generateKey();

        Properties props = new Properties();
        props.setProperty("version", "1");
        props.setProperty("match", new SimpleDateFormat("yyyyMMdd.HHmmss.SSS").format(new java.util.Date()));
        props.setProperty("transformation", sTransformation);
        props.setProperty("algorithm", sAlgorithm);
        props.setProperty("key", formatAsHexDigits(keyCipher.getEncoded()));
        props.setProperty("mac", sMac);
        props.setProperty("mackey", formatAsHexDigits(keyMac.getEncoded()));

        storePropertiesToFile(props, sPasswordEncryptionKeyFileName,
                "Teradata JDBC Driver password encryption key file");

    } // end createPasswordEncryptionKeyFile

    public static void createEncryptedPasswordFile(String sPasswordEncryptionKeyFileName,
                                                   String sEncryptedPasswordFileName, String sPassword) throws GeneralSecurityException, IOException {
        if (sPassword.length() == 0)
            throw new IllegalArgumentException("Password cannot be a zero-length string");

        sPassword = decodeUnicodeEscapeSequences(sPassword);

        Properties propsKey = loadPropertiesFromFile(sPasswordEncryptionKeyFileName);

        String sVersionNumber = propsKey.getProperty("version");
        String sMatchValue = propsKey.getProperty("match");
        String sTransformation = propsKey.getProperty("transformation");
        String sAlgorithm = propsKey.getProperty("algorithm");
        byte[] abyKey = parseFromHexDigits(propsKey.getProperty("key"));
        String sMac = propsKey.getProperty("mac");
        byte[] abyMacKey = parseFromHexDigits(propsKey.getProperty("mackey"));

        if (!"1".equals(sVersionNumber))
            throw new IllegalArgumentException("Properties file " + sPasswordEncryptionKeyFileName
                    + " has unexpected or nonexistent version " + sVersionNumber);

        System.out.println(sPasswordEncryptionKeyFileName + " specifies " + sTransformation + " with "
                + (abyKey.length * 8) + "-bit key" + ", then " + sMac);

        byte[] abyPassword = sPassword.getBytes("UTF-8");
        int nPlaintextByteCount = ((abyPassword.length / BOUNDARY) + 1) * BOUNDARY; // pad
        // boundary
        int nTrailerByteCount = nPlaintextByteCount - abyPassword.length;

        ByteArrayOutputStream osPlaintext = new ByteArrayOutputStream();
        osPlaintext.write(abyPassword);
        osPlaintext.write(new byte[nTrailerByteCount]); // null bytes

        byte[] abyPlaintext = osPlaintext.toByteArray();

        SecretKeySpec keyCipher = new SecretKeySpec(abyKey, sAlgorithm);

        Cipher cipher = Cipher.getInstance(sTransformation);

        cipher.init(Cipher.ENCRYPT_MODE, keyCipher);

        byte[] abyCiphertext = cipher.doFinal(abyPlaintext);

        AlgorithmParameters params = cipher.getParameters();
        byte[] abyParams = params != null ? params.getEncoded() : null;

        ByteArrayOutputStream osContent = new ByteArrayOutputStream();
        osContent.write(abyCiphertext);
        osContent.write(sTransformation.getBytes("UTF-8"));
        if (abyParams != null)
            osContent.write(abyParams);

        SecretKeySpec keyMac = new SecretKeySpec(abyMacKey, sMac);

        Mac mac = Mac.getInstance(sMac);
        mac.init(keyMac);

        byte[] abyHash = mac.doFinal(osContent.toByteArray());

        Properties propsPassword = new Properties();
        propsPassword.setProperty("version", sVersionNumber);
        propsPassword.setProperty("match", sMatchValue);
        propsPassword.setProperty("password", formatAsHexDigits(abyCiphertext));
        propsPassword.setProperty("hash", formatAsHexDigits(abyHash));
        if (abyParams != null)
            propsPassword.setProperty("params", formatAsHexDigits(abyParams));

        storePropertiesToFile(propsPassword, sEncryptedPasswordFileName,
                "Teradata JDBC Driver encrypted password file");

    } // end createEncryptedPasswordFile

    public static void decryptPassword(String sPasswordEncryptionKeyFileName, String sEncryptedPasswordFileName)
            throws IOException, GeneralSecurityException {
        Properties propsKey = loadPropertiesFromFile(sPasswordEncryptionKeyFileName);
        Properties propsPassword = loadPropertiesFromFile(sEncryptedPasswordFileName);

        String sVersionNumberA = propsKey.getProperty("version");
        String sMatchValueA = propsKey.getProperty("match");
        String sTransformation = propsKey.getProperty("transformation");
        String sAlgorithm = propsKey.getProperty("algorithm");
        byte[] abyKey = parseFromHexDigits(propsKey.getProperty("key"));
        String sMac = propsKey.getProperty("mac");
        byte[] abyMacKey = parseFromHexDigits(propsKey.getProperty("mackey"));

        String sVersionNumberB = propsPassword.getProperty("version");
        String sMatchValueB = propsPassword.getProperty("match");
        byte[] abyCiphertext = parseFromHexDigits(propsPassword.getProperty("password"));
        byte[] abyExpectedHash = parseFromHexDigits(propsPassword.getProperty("hash"));
        String sParams = propsPassword.getProperty("params");
        byte[] abyParams = sParams != null ? parseFromHexDigits(sParams) : null;

        if (!"1".equals(sVersionNumberA))
            throw new IllegalArgumentException("Properties file " + sPasswordEncryptionKeyFileName
                    + " has unexpected or nonexistent version " + sVersionNumberA);

        if (!"1".equals(sVersionNumberB))
            throw new IllegalArgumentException("Properties file " + sEncryptedPasswordFileName
                    + " has unexpected or nonexistent version " + sVersionNumberB);

        if (sMatchValueA == null)
            throw new IllegalArgumentException(
                    "Properties file " + sPasswordEncryptionKeyFileName + " is missing a match value");

        if (sMatchValueB == null)
            throw new IllegalArgumentException(
                    "Properties file " + sEncryptedPasswordFileName + " is missing a match value");

        if (!sMatchValueA.equals(sMatchValueB))
            throw new IllegalArgumentException("Properties file " + sPasswordEncryptionKeyFileName + " match value "
                    + sMatchValueA + " differs from properties file " + sEncryptedPasswordFileName + " match value "
                    + sMatchValueB);

        ByteArrayOutputStream osContent = new ByteArrayOutputStream();
        osContent.write(abyCiphertext);
        osContent.write(sTransformation.getBytes("UTF-8"));
        if (abyParams != null)
            osContent.write(abyParams);

        SecretKeySpec keyMac = new SecretKeySpec(abyMacKey, sMac);

        Mac mac = Mac.getInstance(sMac);
        mac.init(keyMac);

        byte[] abyActualHash = mac.doFinal(osContent.toByteArray());
        if (!Arrays.equals(abyExpectedHash, abyActualHash))
            throw new IllegalArgumentException("Hash mismatch indicates possible tampering with properties file "
                    + sPasswordEncryptionKeyFileName + " and/or " + sEncryptedPasswordFileName);

        Cipher cipher = Cipher.getInstance(sTransformation);

        SecretKeySpec keyCipher = new SecretKeySpec(abyKey, sAlgorithm);

        AlgorithmParameters params = abyParams != null ? AlgorithmParameters.getInstance(sAlgorithm) : null;
        if (params != null)
            params.init(abyParams);

        cipher.init(Cipher.DECRYPT_MODE, keyCipher, params);

        byte[] abyPlaintext = cipher.doFinal(abyCiphertext);

        int nDecodeCount = 0;
        while (abyPlaintext[nDecodeCount] != 0) // find the null byte
            nDecodeCount++;

        String sDecryptedPassword = new String(abyPlaintext, 0, nDecodeCount, "UTF-8");

        System.out.println("Decrypted password: " + sDecryptedPassword);

    } // end decryptPassword

    public static void storePropertiesToFile(Properties props, String sFileName, String sTitle) throws IOException {
        OutputStream os = new FileOutputStream(sFileName);
        try {
            props.store(os, sTitle);
        } finally {
            os.close();
        }

        System.out.println("Created " + sFileName);

    } // end storePropertiesToFile

    public static Properties loadPropertiesFromFile(String sFileName) throws IOException {
        Properties props = new Properties();
        InputStream is = new FileInputStream(sFileName);
        try {
            props.load(is);
        } finally {
            is.close();
        }

        return props;

    } // end loadPropertiesFromFile

    public static String formatAsHexDigits(byte[] aby) {
        String sOutput = "";
        for (int i = 0; i < aby.length; i++) {
            String s = Integer.toHexString(aby[i] & 0xFF);
            sOutput += (s.length() < 2 ? "0" : "") + s;
        }

        return sOutput;

    } // end formatAsHexDigits

    public static byte[] parseFromHexDigits(String s) {
        if ((s.length() & 1) != 0)
            throw new IllegalArgumentException("Odd number of characters: " + s.length());

        byte[] aby = new byte[s.length() / 2];
        for (int i = 0; i < aby.length; i++)
            aby[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);

        return aby;

    } // end parseFromHexDigits

    public static String decodeUnicodeEscapeSequences(String s) {
        String sOutput = "";
        for (int i = 0; i < s.length();) {
            if (s.regionMatches(i, "\\u", 0, 2)) {
                String sHexDigits = s.substring(i + 2, i + 6);
                char c = (char) Integer.parseInt(sHexDigits, 16);
                sOutput += c;
                i += 6;
            } else {
                sOutput += s.charAt(i);
                i++;
            }
        }

        return sOutput;

    } // end decodeUnicodeEscapeSequences

} // end class TJEncryptPassword