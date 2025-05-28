package service;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutTest {
    private Server server;
    private String token;

    @BeforeEach
    public void setup() throws DataAccessException {
        server = new Server();
        server.run(0);
        Spark.awaitInitialization();

        UserData user = new UserData("logme", "out", "test@test.com");
        Server.userDAO.createUser(user);
        AuthData auth = Server.authDAO.createAuth(user);
        token = auth.authToken();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void logoutSuccess() throws DataAccessException {
        assertNotNull(Server.authDAO.getAuth(token));
        Server.authDAO.removeAuth(token);
        assertNull(Server.authDAO.getAuth(token));
    }

    @Test
    public void logoutFailureInvalidToken() throws DataAccessException {
        String fakeToken = "invalid-token";
        assertNull(Server.authDAO.getAuth(fakeToken));
        // Attempting to remove should still be harmless
        Server.authDAO.removeAuth(fakeToken);
        assertNull(Server.authDAO.getAuth(fakeToken));
    }
}

