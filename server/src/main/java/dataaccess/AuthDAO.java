package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth(UserData data) throws DataAccessException;
    AuthData getAuth(String token) throws DataAccessException;
    void removeAll() throws DataAccessException;
}
