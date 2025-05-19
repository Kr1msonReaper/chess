package dataaccess;

import model.UserData;

public interface UserDAO {
    int createUser(UserData data);
    UserData getUser(String username);
    void removeAll();
}
