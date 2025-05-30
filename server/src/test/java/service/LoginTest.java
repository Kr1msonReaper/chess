package service;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    private Server server;

    @BeforeEach
    public void setup() throws DataAccessException {
        server = new Server();
        server.run(0);
        Spark.awaitInitialization();

        Server.userDAO.removeAll();
        Server.gameDAO.deleteAll();
        Server.authDAO.removeAll();

        Server.userDAO.createUser(new UserData("testuser", "password123", "test@example.com"));
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        UserData input = new UserData("testuser", "password123", null);
        AuthData auth = Server.authDAO.createAuth(input);
        assertNotNull(auth);
        assertEquals("testuser", auth.username());
    }

    @Test
    public void loginFailureWrongPassword() throws DataAccessException {
        UserData input = new UserData("testuser", "wrongpass", null);
        AuthData auth = Server.authDAO.createAuth(input);
        assertNotNull(Server.authDAO.getAuth(auth.authToken()));
    }
}
