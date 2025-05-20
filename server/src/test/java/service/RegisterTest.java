package service;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTest {
    private Server server;

    @BeforeEach
    public void setup() {
        server = new Server();
        server.run(0);
        Spark.awaitInitialization();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void registerSuccess() {
        UserData user = new UserData("newuser", "pass", "email@test.com");
        Server.userDAO.createUser(user);
        assertTrue(Server.userDAO.userExists(user));
    }

    @Test
    public void registerFailure_duplicateUsername() {
        UserData user = new UserData("dupuser", "pass", "email@test.com");
        Server.userDAO.createUser(user);
        assertThrows(RuntimeException.class, () -> Server.userDAO.createUser(user));
    }
}

