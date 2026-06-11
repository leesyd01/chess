package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class WebSocketFacade {

    private Session session;
    private final Gson gson = new Gson();
    private final ServerMessageObserver observer;
    private final CountDownLatch connectLatch = new CountDownLatch(1);

    public WebSocketFacade(String serverUrl, ServerMessageObserver observer) throws Exception {
        this.observer = observer;
        String wsUrl = serverUrl.replace("http", "ws") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(wsUrl));
        if (!connectLatch.await(5, TimeUnit.SECONDS)) {
            throw new Exception("WebSocket connection timed out");
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        connectLatch.countDown();
    }

    @OnMessage
    public void onMessage(String msg) {
        try {
            ServerMessage base = gson.fromJson(msg, ServerMessage.class);
            switch (base.getServerMessageType()) {
                case LOAD_GAME    -> observer.onLoadGame(gson.fromJson(msg, LoadGameMessage.class));
                case ERROR        -> observer.onError(gson.fromJson(msg, ErrorMessage.class));
                case NOTIFICATION -> observer.onNotification(gson.fromJson(msg, NotificationMessage.class));
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("WS error: " + t.getMessage());
    }

    public void sendConnect(String authToken, int gameID) throws IOException {
        send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void sendMakeMove(String authToken, int gameID, ChessMove move) throws IOException {
        send(new MakeMoveCommand(authToken, gameID, move));
    }

    public void sendLeave(String authToken, int gameID) throws IOException {
        send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
    }

    public void sendResign(String authToken, int gameID) throws IOException {
        send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    private void send(Object obj) throws IOException {
        if (session == null || !session.isOpen()) {
            throw new IOException("WebSocket not connected");
        }
        session.getBasicRemote().sendText(gson.toJson(obj));
    }
}