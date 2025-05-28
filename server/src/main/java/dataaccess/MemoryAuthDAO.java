package dataaccess;

import model.AuthData;
import model.UserData;
import server.Server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO{

    public Collection<AuthData> authTokens = new ArrayList<>();

    public AuthData createAuth(UserData data) throws DataAccessException {
        String newToken = UUID.randomUUID().toString();
        AuthData newData = new AuthData(newToken, data.username());
        authTokens.add(newData);
        Server.sqlAuthDAO.createAuth(data);
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

    public void removeAuth(String token) throws DataAccessException {
        AuthData matchingToken = new AuthData("", "");
        for(AuthData x : authTokens){
            if(x.authToken().equals(token)){
                matchingToken = x;
            }
        }
        authTokens.remove(matchingToken);
        Server.sqlAuthDAO.removeAuth(token);
    }

    public void removeAll() throws DataAccessException {
        authTokens.clear();
        Server.sqlAuthDAO.removeAll();
    }

}
