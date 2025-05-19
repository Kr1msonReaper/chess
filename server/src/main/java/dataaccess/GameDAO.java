package dataaccess;

import chess.ChessGame;
import model.AuthData;

import java.util.Collection;

public interface GameDAO {
    int createGame(AuthData data, String gameName);
    Collection<ChessGame> getGames(AuthData data);
    int joinGame(AuthData data, ChessGame.TeamColor color, String gameID);
    int deleteAll();
}
