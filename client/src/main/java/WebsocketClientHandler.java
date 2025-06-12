import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.AuthData;
import model.GameData;
import server.Server;
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
        JsonObject messageJson = JsonParser.parseString(message).getAsJsonObject();

        // 1. Extract ServerMessage fields
        ServerMessage.ServerMessageType type = ServerMessage.ServerMessageType.valueOf(
                messageJson.get("serverMessageType").getAsString()
        );

        // 2. Reconstruct the ServerMessage object
        ServerMessage serverMessage = new ServerMessage(type);

        // 3. Extract and deserialize the "game" field (if present)
        GameData game = null;
        if (messageJson.has("game") && !messageJson.get("game").isJsonNull()) {
            game = GSON.fromJson(messageJson.get("game"), GameData.class);
        }

        AuthData auth = null;
        if (messageJson.has("auth") && !messageJson.get("auth").isJsonNull()) {
            auth = GSON.fromJson(messageJson.get("auth"), AuthData.class);
        }

        String text = null;
        if (messageJson.has("message") && !messageJson.get("message").isJsonNull()) {
            text = GSON.fromJson(messageJson.get("message"), String.class);
        }

        if(type == ServerMessage.ServerMessageType.NOTIFICATION){
            System.out.println("\n" + text);
        }
        if(type == ServerMessage.ServerMessageType.LOAD_GAME){
            loadGame(serverMessage, game, auth);
        }
    }

    private void loadGame(ServerMessage serverMessage, GameData game, AuthData auth) throws IOException {
        System.out.print("\n");
        Main.redrawBoard(auth, game,-1, -1);
        //System.out.println("Redrew the board because of a server message!");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable error) {
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
