package dataaccess;

import server.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static server.Server.GSON;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();

            var selectDB = "USE " + databaseName;
            conn.prepareStatement(selectDB).executeUpdate();

            var createGameData = "CREATE TABLE IF NOT EXISTS gameData (\n" +
                                 "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                                 "    gameDataJSON JSON" +
                                 ");";
            conn.prepareStatement(createGameData).executeUpdate();

            var createUserData = "CREATE TABLE IF NOT EXISTS users (\n" +
                                 "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                                 "    userData JSON" +
                                 ");";
            conn.prepareStatement(createUserData).executeUpdate();

            var createAuthData = "CREATE TABLE IF NOT EXISTS authTokens (\n" +
                    "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    authData JSON" +
                    ");";
            conn.prepareStatement(createAuthData).executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    static public void executeSQL(String command) throws DataAccessException{
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)){
            conn.prepareStatement("USE " + databaseName + ";").executeUpdate();
            //System.out.println(command);
            conn.prepareStatement(command).executeUpdate();

        } catch (SQLException e){
            System.out.println(e);
            throw new DataAccessException("failed to create database", e);
        }
    }

    static public void executeSQL(String command, String dump) throws DataAccessException{
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)){
            conn.prepareStatement("USE " + databaseName).executeUpdate();
            //System.out.println(command + " VALUES (" + "\'" + dump + "\'" + ");");
            var statement = conn.prepareStatement(command + " VALUES (?);");
            statement.setString(1, dump);
            statement.executeUpdate();
        } catch (SQLException e){
            System.out.println(e);
            throw new DataAccessException("failed to create database", e);
        }
    }

    static public void deleteInsertSQL(String command, String dump) throws DataAccessException{
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)){
            conn.prepareStatement("USE " + databaseName).executeUpdate();
            var statement = conn.prepareStatement(command + ";");
            statement.setString(1, dump);
            //System.out.println(statement);
            statement.executeUpdate();
        } catch (SQLException e){
            System.out.println(e);
            throw new DataAccessException("failed to create database", e);
        }
    }

    static public void deleteIfLikeSQL(String command, String dump) throws DataAccessException{
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)){
            conn.prepareStatement("USE " + databaseName).executeUpdate();
            var statement = conn.prepareStatement(command);
            statement.setString(1, "%" + dump + "%");
            //System.out.println(statement);
            statement.executeUpdate();
        } catch (SQLException e){
            System.out.println(e);
            throw new DataAccessException("failed to create database", e);
        }
    }

    static public List<String> getTableContents(String table, String child) throws DataAccessException{
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword)){
            conn.prepareStatement("USE " + databaseName).executeUpdate();
            List<String> results = new ArrayList<>();
            var command = conn.prepareStatement("SELECT * FROM " + table);
            //System.out.println(command);
            var rs = command.executeQuery();
            while(rs.next()){
                results.add(rs.getString(child));
            }
            return results;
        } catch (SQLException e){
            System.out.println(e);
            throw new DataAccessException("failed to create database", e);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}
