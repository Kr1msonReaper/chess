import model.UserData;
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

        boolean isLoggedIn = false;

        System.out.println("♕ 240 Chess Client. Type \'Help\' to get started.");
        while(true){
            Scanner scanner = new Scanner(System.in);
            String[] line = scanner.nextLine().toLowerCase(Locale.ROOT).split(" ");

            if(line[0].contains("help")){
                if(!isLoggedIn){
                    System.out.println("Register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                            "login <USERNAME> <PASSWORD> - to play chess\n" +
                            "quit - playing chess\n" +
                            "help - with possible commands");
                } else {
                    System.out.println("Register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                            "login <USERNAME> <PASSWORD> - to play chess\n" +
                            "quit - playing chess\n" +
                            "help - with possible commands\n" +
                            "logout - log out\n" +
                            "create game <NAME> - create a new game.\n" +
                            "list games - list existing game id's.\n" +
                            "play game <GAME-ID> <DESIRED-COLOR> - join an existing game.\n" +
                            "observe game <GAME-ID> - observe a game in progress.");
                }
            } else if(line[0].contains("register")){
                if(line.length == 4){
                    try{
                        UserData newUser = new UserData(line[1], line[2], line[3]);
                        facade.register(newUser);
                    } catch(Exception e){
                        System.out.println("Error: " + e);
                    }

                } else {
                    System.out.println("Error: Incorrect number of arguments.");
                }
            } else if(line[0].contains("login")){
                if(line.length == 3){
                    try{
                        UserData newUser = new UserData(line[1], line[2], "");
                        facade.login(newUser);
                        isLoggedIn = true;
                    } catch(Exception e){
                        System.out.println("Error: " + e);
                    }
                } else {
                    System.out.println("Error: Incorrect number of arguments.");
                }
            } else if(line[0].contains("quit")){
                System.exit(0);
            } else {
                System.out.println("Error: Command not recognized.");
            }
        }
    }
}