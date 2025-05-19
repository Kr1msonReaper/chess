package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDAO {
    int createAuth(AuthData data);
    UserData getAuth(AuthData data);
    int removeAll();
}
