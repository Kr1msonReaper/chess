import server.ServerFacade;
import server.Server;

public class Main {
    public static ServerFacade facade;
    public static Server server = new Server();

    public static void main(String[] args) {
        var port = server.run(0);
        facade = new ServerFacade(port);

        System.out.println("♕ 240 Chess Client: ");


    }
}