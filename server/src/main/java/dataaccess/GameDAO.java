package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    int createGame(AuthData data, String gameName);
    Collection<GameData> getGames(AuthData data);
    int joinGame(AuthData data, ChessGame.TeamColor color, int gameID);
    void deleteAll();
}
