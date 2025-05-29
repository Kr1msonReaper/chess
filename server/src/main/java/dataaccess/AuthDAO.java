package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.Collection;

public interface AuthDAO {
    AuthData createAuth(UserData data) throws DataAccessException;
    AuthData getAuth(String token) throws DataAccessException;
    Collection<AuthData> getAll() throws DataAccessException;
    void removeAuth(String token) throws DataAccessException;
    void removeAll() throws DataAccessException;
}
