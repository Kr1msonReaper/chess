package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

import static server.Server.GSON;

public class MemoryUserDAO implements UserDAO {
    public Collection<UserData> users = new ArrayList<>();

    public int createUser(UserData data){

        users.add(data);
        return 200;
    }

    public Collection<UserData> getUsers() throws DataAccessException {
        Collection<String> jsonDumps = new ArrayList<>();
        Collection<UserData> converted = new ArrayList<>();

        jsonDumps = DatabaseManager.getTableContents("users", "userData");

        for(String dump : jsonDumps){
            converted.add(GSON.fromJson(dump, UserData.class));
        }

        return converted;
    }

    public boolean userExists(UserData data){
        for(UserData user : users){
            if(user.username().equals(data.username()) || user.email().equals(data.email())){
                return true;
            }
        }
        return false;
    }

    public UserData getUser(String username){
        for(UserData user : users){
            if(user.username().equals(username)){
                return user;
            }
        }
        return null;
    }
    public void removeAll(){
        users.clear();
    }
}
