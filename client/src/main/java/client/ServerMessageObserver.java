package client;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface ServerMessageObserver {
    void onLoadGame(LoadGameMessage message);
    void onError(ErrorMessage message);
    void onNotification(NotificationMessage message);
}