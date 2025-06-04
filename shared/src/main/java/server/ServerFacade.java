package server;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import service.*;

public class ServerFacade {
    private final int port;
    private final String baseUrl;
    public static final Gson GSON = new Gson();
    public static ServerFacade facade;

    public ServerFacade(int port) {
        this.port = port;
        this.baseUrl = "http://localhost:" + port;
        facade = this;
    }

    public String sendGetRequest(String endpoint, String auth) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        if(auth != null && !auth.isEmpty()){
            connection.setRequestProperty("Authorization", auth);
        }

        return readResponse(connection);
    }

    public String sendPostRequest(String endpoint, String jsonBody, String auth) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        if(auth != null && !auth.isEmpty()){
            connection.setRequestProperty("Authorization", auth);
        }
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        return readResponse(connection);
    }

    public String sendPutRequest(String endpoint, String jsonBody, String auth) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        if(auth != null && !auth.isEmpty()){
            connection.setRequestProperty("Authorization", auth);
        }
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        return readResponse(connection);
    }

    public String sendDeleteRequest(String endpoint, String jsonBody, String auth) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Content-Type", "application/json");
        if(auth != null && !auth.isEmpty()){
            connection.setRequestProperty("Authorization", auth);
        }
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        return readResponse(connection);
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST
                ? connection.getInputStream()
                : connection.getErrorStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    public AuthData register(UserData data) throws IOException {
        String response = sendPostRequest("/user", GSON.toJson(data), "");
        try {
            AuthData responseAuth = GSON.fromJson(response, AuthData.class);
            return responseAuth;
        } catch(Exception e){
            return null;
        }
    }

    public AuthData login(UserData data) throws IOException {
        String response = sendPostRequest("/session", GSON.toJson(data), "");
        try {
            AuthData responseAuth = GSON.fromJson(response, AuthData.class);
            return responseAuth;
        } catch(Exception e){
            return null;
        }
    }

    public void logout(AuthData data) throws IOException {
        sendDeleteRequest("/session", "", data.authToken());
    }

    public int createGame(AuthData data, CreateGameRequest req) throws IOException {
        return GSON.fromJson(sendPostRequest("/game", GSON.toJson(req), data.authToken()), CreateGameResult.class).gameID;
    }

    public Collection<GameData> listGames(AuthData data) throws IOException {
        ListGamesResult res = GSON.fromJson(sendGetRequest("/game", data.authToken()), ListGamesResult.class);
        return res.games;
    }

    public String joinGame(JoinGameRequest req, AuthData data) throws IOException {
        return sendPutRequest("/game", GSON.toJson(req), data.authToken());
    }
}
