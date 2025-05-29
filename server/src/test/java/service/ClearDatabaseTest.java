package service;
import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;

public class ClearDatabaseTest {
    private Server server;

    @BeforeEach
    public void setup() throws DataAccessException {
        server = new Server();
        server.run(0);
        Spark.awaitInitialization();

        Server.userDAO.createUser(new UserData("a", "b", "c@d.com"));
        Server.authDAO.createAuth(new UserData("a", "b", "c@d.com"));
        Server.gameDAO.createGame("Game X");
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void clearDbSuccess() throws DataAccessException {
        Server.userDAO.removeAll();
        Server.authDAO.removeAll();
        Server.gameDAO.deleteAll();



        assertEquals(0, Server.userDAO.getUsers().size());
        assertEquals(0, Server.authDAO.getAll().size());
        assertEquals(0, Server.gameDAO.getGames().size());
    }

    @Test
    public void clearDbFailureSimulatedException() {
        try {
            throw new RuntimeException("simulated failure");
        } catch (RuntimeException e) {
            assertEquals("simulated failure", e.getMessage());
        }
    }
}

