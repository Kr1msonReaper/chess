package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO{

    public Collection<AuthData> authTokens = new ArrayList<>();

    public AuthData createAuth(UserData data){
        String newToken = UUID.randomUUID().toString();
        AuthData newData = new AuthData(newToken, data.username());
        authTokens.add(newData);
        return newData;
    }

    public AuthData getAuth(String token){
        for(AuthData x : authTokens){
            if(x.authToken().equals(token)){
                return x;
            }
        }
        return null;
    }

    public void removeAuth(String token){
        AuthData matchingToken = new AuthData("", "");
        for(AuthData x : authTokens){
            if(x.authToken().equals(token)){
                matchingToken = x;
            }
        }
        authTokens.remove(matchingToken);
    }

    public void removeAll(){
        authTokens.clear();
    }

}
