package server;

public class PlayerInfo {
    public String authToken;
    public String role; // "player" or "observer"
    public boolean hasResigned = false;

    public PlayerInfo(String authToken, String role, boolean hasResigned) {
        this.authToken = authToken;
        this.role = role;
        this.hasResigned = hasResigned;
    }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean getResigned() { return hasResigned; }
    public void setResigned(boolean hasResigned) { this.hasResigned = hasResigned; }
}
