import model.AuthData;
import model.UserData;
import server.ServerFacade;
import server.Server;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static ServerFacade facade;
    public static Server server = new Server();

    public static void main(String[] args) throws IOException {
        var port = server.run(0);
        facade = new ServerFacade(port);

        boolean isLoggedIn = false;
        UserData currentUser;
        AuthData currentToken = new AuthData("", "");

        System.out.println("♕ 240 Chess Client. Type \'Help\' to get started.");
        while(true){
            if(isLoggedIn){
                System.out.print("[LOGGED_IN] >>>");
            } else {
                System.out.print("[LOGGED_OUT] >>>");
            }
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
                            "create <NAME> - create a new game.\n" +
                            "list - list existing game id's.\n" +
                            "join <GAME-ID> <DESIRED-COLOR> - join an existing game.\n" +
                            "observe <GAME-ID> - observe a game in progress.");
                }
            } else if(line[0].contains("register")){
                if(line.length == 4){
                    try{
                        UserData newUser = new UserData(line[1], line[2], line[3]);
                        currentToken = facade.register(newUser);
                        isLoggedIn = true;
                        currentUser = newUser;
                        System.out.println("Logged in as " + line[1]);
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
                        currentToken = facade.login(newUser);
                        isLoggedIn = true;
                        currentUser = newUser;
                        System.out.println("Logged in as " + line[1]);
                    } catch(Exception e){
                        System.out.println("Error: " + e);
                    }
                } else {
                    System.out.println("Error: Incorrect number of arguments.");
                }
            } else if(line[0].contains("quit")){
                facade.logout(currentToken);
                System.out.println("Logged out");
                System.exit(0);
            } else if(line[0].contains("logout")){
                try{
                    facade.logout(currentToken);
                    isLoggedIn = false;
                    System.out.println("Logged out");
                } catch(Exception e){
                    System.out.println("Error: " + e);
                }
            } else if(line[0].contains("create")){

            } else if(line[0].contains("list")){

            } else if(line[0].contains("join")){

            } else if(line[0].contains("observe")){

            } else {
                System.out.println("Error: Command not recognized.");
            }
        }
    }
}