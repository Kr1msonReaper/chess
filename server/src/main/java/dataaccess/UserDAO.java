package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

public interface UserDAO {
    int createUser(UserData data) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    Collection<UserData> getUsers() throws DataAccessException;
    boolean userExists(UserData data) throws DataAccessException;
    void removeAll() throws DataAccessException;
}
