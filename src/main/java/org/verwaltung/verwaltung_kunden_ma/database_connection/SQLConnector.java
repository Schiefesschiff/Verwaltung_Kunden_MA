package org.verwaltung.verwaltung_kunden_ma.database_connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SQLConnector
{
    private final String url;
    private final String user;
    private final String password;

    public SQLConnector(String host, String database, String user, String password)
    {
        this.url = "jdbc:mysql://" + host + ":3306/" + database
                + "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
        this.user = user;
        this.password = password;
    }

    /**
     * Gibt bei jedem Aufruf eine neue Connection zurück.
     */
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(url, user, password);
    }
}
