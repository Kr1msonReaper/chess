package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {

    private final Collection<AuthData> authTokens = new ArrayList<>();
    private static final Gson GSON = new Gson();

    @Override
    public AuthData createAuth(UserData data) throws DataAccessException {
        try {
            String newToken = UUID.randomUUID().toString();
            AuthData newData = new AuthData(newToken, data.username());
            authTokens.add(newData);

            String json = GSON.toJson(newData);
            DatabaseManager.deleteInsertSQL("INSERT INTO authTokens (authData) VALUES (?)", json);
            return newData;
        } catch (Exception e) {
            throw new DataAccessException("Failed to create auth data", e);
        }
    }

    @Override
    public Collection<AuthData> getAll() throws DataAccessException {
        try {
            Collection<String> jsonDumps = DatabaseManager.getTableContents("authTokens", "authData");
            Collection<AuthData> converted = new ArrayList<>();

            for (String dump : jsonDumps) {
                converted.add(GSON.fromJson(dump, AuthData.class));
            }

            return converted;
        } catch (Exception e) {
            throw new DataAccessException("Failed to get all auth data", e);
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        try {
            List<String> jsonDumps = DatabaseManager.getTableContents("authTokens", "authData");
            for (String dump : jsonDumps) {
                AuthData data = GSON.fromJson(dump, AuthData.class);
                if (data.authToken().equals(token)) {
                    return data;
                }
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Failed to get auth by token", e);
        }
    }

    @Override
    public void removeAuth(String token) throws DataAccessException {
        try {
            AuthData data = getAuth(token);
            if (data == null) {
                return;
            }

            String json = GSON.toJson(data).replace("\"", "\"\""); // Basic escape for SQL if needed
            String sql = "DELETE FROM authTokens WHERE authData = '" + json + "'";

            DatabaseManager.executeSQL(sql);
        } catch (Exception e) {
            throw new DataAccessException("Failed to remove auth by token", e);
        }
    }

    @Override
    public void removeAll() throws DataAccessException {
        try {
            DatabaseManager.executeSQL("DELETE FROM authTokens");
        } catch (Exception e) {
            throw new DataAccessException("Failed to remove all auth data", e);
        }
    }
}
