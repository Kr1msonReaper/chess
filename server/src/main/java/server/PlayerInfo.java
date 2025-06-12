package server;

public class PlayerInfo {
    private String authToken;
    private String role; // "player" or "observer"

    public PlayerInfo(String authToken, String role) {
        this.authToken = authToken;
        this.role = role;
    }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
