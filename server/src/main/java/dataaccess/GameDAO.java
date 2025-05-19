package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName);
    Collection<GameData> getGames();
    int joinGame(AuthData data, ChessGame.TeamColor color, int gameID);
    void deleteAll();
}
