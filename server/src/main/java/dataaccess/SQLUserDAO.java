package dataaccess;

import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.Collection;

import static server.Server.GSON;

public class SQLUserDAO implements UserDAO {
    public Collection<UserData> users = new ArrayList<>();

    public int createUser(UserData data) throws DataAccessException {
        DatabaseManager.createDatabase();
        users.add(data);
        DatabaseManager.executeSQL("INSERT INTO users (userData)", GSON.toJson(data));
        return 200;
    }

    public boolean userExists(UserData data) throws DataAccessException {
        Collection<String> jsonDumps = new ArrayList<>();
        Collection<UserData> converted = new ArrayList<>();

        jsonDumps = DatabaseManager.getTableContents("users", "userData");

        for(String dump : jsonDumps){
            converted.add(GSON.fromJson(dump, UserData.class));
        }

        for(UserData dataObj : converted){
            if(dataObj.equals(data)){
                return true;
            }
        }

        return false;
    }

    public UserData getUser(String username) throws DataAccessException {
        Collection<String> jsonDumps = new ArrayList<>();
        Collection<UserData> converted = new ArrayList<>();

        jsonDumps = DatabaseManager.getTableContents("users", "userData");

        for(String dump : jsonDumps){
            converted.add(GSON.fromJson(dump, UserData.class));
        }

        for(UserData data : converted){
            if(data.username().equals(username)){
                return data;
            }
        }

        return null;
    }
    public void removeAll() throws DataAccessException {
        DatabaseManager.executeSQL("DELETE FROM users");
    }
}
