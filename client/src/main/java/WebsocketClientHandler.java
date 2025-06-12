import com.google.gson.Gson;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
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
    public void onMessage(String message) {
        ServerMessage serverMessage = GSON.fromJson(message, ServerMessage.class);
        if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION){
            System.out.println("\n" + serverMessage.message);
        }
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
