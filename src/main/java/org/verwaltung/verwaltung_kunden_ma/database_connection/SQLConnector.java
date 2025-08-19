package org.verwaltung.verwaltung_kunden_ma.database_connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for establishing JDBC connections to a MySQL database.
 * <p>
 * Encapsulates connection parameters such as host, database name, user, and password,
 * and provides a convenient method to create new {@link Connection} instances.
 * Each call to {@link #getConnection()} returns a fresh connection.
 */
public class SQLConnector
{
    private final String url;
    private final String user;
    private final String password;

    /**
     * Constructs a new connector with the given database credentials.
     *
     * @param host     database host (e.g. {@code localhost} or an IP address)
     * @param database name of the database schema
     * @param user     database username
     * @param password database password
     */
    public SQLConnector(String host, String database, String user, String password)
    {
        this.url = "jdbc:mysql://" + host + ":3306/" + database
                + "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
        this.user = user;
        this.password = password;
    }

    /**
     * Opens and returns a new JDBC connection to the configured database.
     * <p>
     * The caller is responsible for closing the connection after use.
     *
     * @return a new {@link Connection} object
     * @throws SQLException if the connection attempt fails
     */
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(url, user, password);
    }
}
