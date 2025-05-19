package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryUserDAO {
    public Collection<UserData> users = new ArrayList<>();

    public int createUser(UserData data){

        users.add(data);
        return 200;
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
            if(user.username() == username){
                return user;
            }
        }
        return null;
    }
    public void removeAll(){
        users.clear();
    }
}
