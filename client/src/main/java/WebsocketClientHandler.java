import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ClientEndpoint
public class WebsocketClientHandler {
    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.put(session, "");
    }

    @OnMessage
    public void onMessage(String message) {

    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}
