package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    AuthData createAuth(UserData data);
    AuthData getAuth(String token);
    void removeAll();
}
