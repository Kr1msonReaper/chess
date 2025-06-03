import server.ServerFacade;
import server.Server;

import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static ServerFacade facade;
    public static Server server = new Server();

    public static void main(String[] args) {
        var port = server.run(0);
        facade = new ServerFacade(port);
        System.out.println("♕ 240 Chess Client. Type \'Help\' to get started.\n");
        while(true){
            Scanner scanner = new Scanner(System.in);
            String[] line = scanner.nextLine().toLowerCase(Locale.ROOT).split(" ");

            if(line[0].contains("help")){
                System.out.println("Register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                                   "login <USERNAME> <PASSWORD> - to play chess\n" +
                                   "quit - playing chess\n" +
                                   "help - with possible commands");
            }
        }
    }
}