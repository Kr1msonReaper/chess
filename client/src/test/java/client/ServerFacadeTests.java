package client;

import client.ServerFacade;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import model.AuthData;
import model.UserData;
import server.Server;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        // things to do here:
        assertTrue(true);
    }

    @Test
    void register() throws Exception {
        UserData reqData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.register(reqData);
        assertTrue(authData.authToken().length() > 10);
    }

}
