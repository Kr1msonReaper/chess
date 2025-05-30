package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Collection;

import static server.Server.GSON;

public class SQLUserDAO implements UserDAO {

    private final Collection<UserData> users = new ArrayList<>();

    @Override
    public int createUser(UserData data) throws DataAccessException {
        try {
            DatabaseManager.createDatabase();

            var encryptedPass = BCrypt.hashpw(data.password(), BCrypt.gensalt());
            var alteredData = data.assignPassword(encryptedPass);
            users.add(alteredData);
            DatabaseManager.deleteInsertSQL("INSERT INTO users (userData) VALUES (?)", GSON.toJson(alteredData));
            return 200;
        } catch (Exception e) {
            throw new DataAccessException("Failed to create user " + data.username(), e);
        }
    }

    @Override
    public boolean userExists(UserData data) throws DataAccessException {
        try {
            Collection<String> jsonDumps = DatabaseManager.getTableContents("users", "userData");
            for (String dump : jsonDumps) {
                UserData dataObj = GSON.fromJson(dump, UserData.class);
                if (dataObj.username().equals(data.username()) && BCrypt.checkpw(data.password(), dataObj.password())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new DataAccessException("Failed to check if user exists: " + data.username(), e);
        }
    }

    @Override
    public Collection<UserData> getUsers() throws DataAccessException {
        try {
            Collection<String> jsonDumps = DatabaseManager.getTableContents("users", "userData");
            Collection<UserData> converted = new ArrayList<>();
            for (String dump : jsonDumps) {
                converted.add(GSON.fromJson(dump, UserData.class));
            }
            return converted;
        } catch (Exception e) {
            throw new DataAccessException("Failed to get users", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try {
            Collection<String> jsonDumps = DatabaseManager.getTableContents("users", "userData");
            for (String dump : jsonDumps) {
                UserData data = GSON.fromJson(dump, UserData.class);
                if (data.username().equals(username)) {
                    return data;
                }
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Failed to get user: " + username, e);
        }
    }

    @Override
    public void removeAll() throws DataAccessException {
        try {
            users.clear();
            DatabaseManager.executeSQL("DELETE FROM users");
        } catch (Exception e) {
            throw new DataAccessException("Failed to remove all users", e);
        }
    }
}
