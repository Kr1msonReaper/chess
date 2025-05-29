package dataaccess;

import model.UserData;

public interface UserDAO {
    int createUser(UserData data) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    boolean userExists(UserData data) throws DataAccessException;
    void removeAll() throws DataAccessException;
}
