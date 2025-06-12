package server;

public class PlayerInfo {
    public String authToken;
    public String role; // "player" or "observer"
    public boolean hasResigned = false;

    public PlayerInfo(String authToken, String role) {
        this.authToken = authToken;
        this.role = role;
    }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
