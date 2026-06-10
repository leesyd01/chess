package client;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface ServerMessageHandler {
    void handleLoadGame(LoadGameMessage message);
    void handleError(ErrorMessage message);
    void handleNotification(NotificationMessage message);
}