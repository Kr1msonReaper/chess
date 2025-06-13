import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.AuthData;
import model.GameData;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@ClientEndpoint
public class WebsocketClientHandler {
    private static Session session;
    public static final Gson GSON = new Gson();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;

        onClose(null, null);
        onError(null, null);

        new Thread(() -> {
            while (session != null && session.isOpen()) {
                try {
                    Thread.sleep(15000); // 15 seconds
                    session.getBasicRemote().sendText("ping");
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        ServerMessage serverMessage = GSON.fromJson(message, ServerMessage.class);

        if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION){
            System.out.println("\n" + serverMessage.message);
        }
        if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME){
            loadGame(serverMessage, serverMessage.game, serverMessage.auth);
        }
        if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR){
            System.out.println(serverMessage.errorMessage);
        }
    }

    private void loadGame(ServerMessage serverMessage, GameData game, AuthData auth) throws IOException {
        System.out.print("\n");
        Main.redrawBoard(auth, game,-1, -1);
        //System.out.println("Redrew the board because of a server message!");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if(session == null){
            return;
        }
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        if(session == null){
            return;
        }
        error.printStackTrace();
    }

    public void sendMessage(String message){
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
