package org.practice.grp.dataBase;

import java.util.ArrayList;
import java.util.stream.Stream;

public enum DataBaseType {
    MS_SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jdbc:sqlserver://%s;integratedSecurity=true;databaseName=%s;", "SQL SERVER"),
    MS_SQL_SERVER_NTLM("com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jdbc:sqlserver://%s;authenticationScheme=NTLM;integratedSecurity=true;domain=boigroup.net;databaseName=%s;",
            "NTML MS-SS", true),
    TERADATA("com.teradata.jdbc.TeraDriver", "jdbc:teradata://%s/LOGMECH=LDAP", "TeraData", true),
    INVALID_DATABASE_TYPE("", "", "");

    private final String driverClass;
    private final String connectionURL;
    private final String dbTypeName;
    private boolean userDetailsRequired = false;

    private DataBaseType(DataBaseType dbType) {
        this.driverClass = dbType.driverClass;
        this.connectionURL = dbType.connectionURL;
        this.dbTypeName = dbType.dbTypeName;
    }

    private DataBaseType(String driverClass, String connectionURL, String dbTypeName) {
        this.driverClass = driverClass.trim();
        this.connectionURL = connectionURL.trim();
        this.dbTypeName = dbTypeName.trim();
    }

    private DataBaseType(String driverClass, String connectionURL, String dbTypeName, boolean userDetailsRequired) {
        this.driverClass = driverClass.trim();
        this.connectionURL = connectionURL.trim();
        this.dbTypeName = dbTypeName.trim();
        this.userDetailsRequired = userDetailsRequired;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public boolean isUserDetailsRequired() {
        return userDetailsRequired;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public static DataBaseType getDataBaseType(String dbTypeName) {
        return Stream.of(values()).filter(dbType -> dbType.toString().equalsIgnoreCase(dbTypeName.trim())).findFirst()
                .orElse(null);
        // return Stream.of(values()).filter(dbType ->
        // dbType.toString().equalsIgnoreCase(dbTypeName.trim()))
        // .reduce((first, second) -> first).orElse(null);
    }

    public static ArrayList<String> listAll() {
        ArrayList<String> dbTypeList = new ArrayList<String>();
        Stream.of(values()).forEach(dbType -> dbTypeList.add(dbType.toString()));
        return dbTypeList;
    }

    @Override
    public String toString() {
        return dbTypeName;
    }

}