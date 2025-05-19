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
