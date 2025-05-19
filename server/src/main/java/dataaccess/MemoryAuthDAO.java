package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO{

    Collection<AuthData> authTokens = new ArrayList<>();

    public AuthData createAuth(UserData data){
        String newToken = UUID.randomUUID().toString();
        AuthData newData = new AuthData(newToken, data.username());
        authTokens.add(newData);
        return newData;
    }

    public AuthData getAuth(UserData data){
        for(AuthData x : authTokens){
            if(x.username() == data.username()){
                return x;
            }
        }
        return null;
    }

    public void removeAll(){
        authTokens.clear();
    }

}
