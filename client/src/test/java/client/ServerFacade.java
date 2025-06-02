package client;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerFacade {
    private final int port;
    private final String baseUrl;
    public static final Gson GSON = new Gson();

    public ServerFacade(int port) {
        this.port = port;
        this.baseUrl = "http://localhost:" + port;
    }

    public String sendGetRequest(String endpoint) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return readResponse(connection);
    }

    public String sendPostRequest(String endpoint, String jsonBody) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
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
        String response = sendPostRequest("/user", GSON.toJson(data));
        try {
            AuthData responseAuth = GSON.fromJson(response, AuthData.class);
            return responseAuth;
        } catch(Exception e){
            return null;
        }
    }
}
