package dataaccess;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO{

    public Collection<AuthData> authTokens = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public AuthData createAuth(UserData data) throws DataAccessException {
        String newToken = UUID.randomUUID().toString();
        AuthData newData = new AuthData(newToken, data.username());
        authTokens.add(newData);
        DatabaseManager.executeSQL("INSERT INTO authTokens (authData)", GSON.toJson(newData));
        return newData;
    }

    public AuthData getAuth(String token) throws DataAccessException {
        List<String> jsonDumps = new ArrayList<>();
        List<AuthData> converted = new ArrayList<>();

        jsonDumps = DatabaseManager.getTableContents("authTokens", "authData");

        for(String dump : jsonDumps){
            converted.add(GSON.fromJson(dump, AuthData.class));
        }

        for(AuthData data : converted){
            if(data.authToken().equals(token)){
                return data;
            }
        }

        return null;
    }

    public void removeAuth(String token) throws DataAccessException {
        DatabaseManager.executeSQL("DELETE FROM authTokens WHERE authData = " + GSON.toJson(getAuth(token)));
    }

    public void removeAll() throws DataAccessException {
        DatabaseManager.executeSQL("DELETE FROM authTokens");
    }

}
