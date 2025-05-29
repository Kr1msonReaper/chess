package model;

public record UserData(String username, String password, String email) {
    public UserData assignPassword(String newPass){
        return new UserData(username, newPass, email);
    }
}
