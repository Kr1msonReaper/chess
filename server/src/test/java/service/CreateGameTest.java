package service;
import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import spark.Spark;

import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTest {
    private Server server;

    @BeforeEach
    public void setup() throws DataAccessException {
        server = new Server();
        server.run(0);
        Spark.awaitInitialization();

        UserData user = new UserData("gamer", "chess", "mail@mail.com");
        Server.userDAO.createUser(user);
        Server.authDAO.createAuth(user);
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void createGameSuccess() {
        int id = Server.gameDAO.createGame("Epic Match");
        assertTrue(id >= 0);
    }

    @Test
    public void createGameFailureBlankName() {
        assertThrows(RuntimeException.class, () -> Server.gameDAO.createGame(""));
    }
}

