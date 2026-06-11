package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import service.ServiceException;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    // gameID -> set of sessions currently watching that game
    private static final ConcurrentHashMap<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    private final GameService gameService;
    private final UserService userService;
    private final Gson gson = new Gson();

    public WebSocketHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand base = gson.fromJson(message, UserGameCommand.class);
            switch (base.getCommandType()) {
                case CONNECT -> handleConnect(session, base);
                case MAKE_MOVE -> handleMakeMove(session, gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeave(session, base);
                case RESIGN -> handleResign(session, base);
            }
        } catch (Exception e) {
            sendTo(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        gameSessions.values().forEach(set -> set.remove(session));
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    // handlers

    private void handleConnect(Session session, UserGameCommand cmd) throws IOException {
        try {
            String username = userService.getUsername(cmd.getAuthToken());
            GameData game   = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());

            gameSessions.computeIfAbsent(cmd.getGameID(), k -> ConcurrentHashMap.newKeySet()).add(session);

            sendTo(session, new LoadGameMessage(game));

            String role;
            if (username.equals(game.whiteUsername()))      role = "joined as WHITE";
            else if (username.equals(game.blackUsername())) role = "joined as BLACK";
            else                                             role = "joined as an observer";

            broadcastExcept(cmd.getGameID(), session,
                    new NotificationMessage(username + " " + role));

        } catch (ServiceException e) {
            sendTo(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleMakeMove(Session session, MakeMoveCommand cmd) throws IOException {
        try {
            String username  = userService.getUsername(cmd.getAuthToken());
            GameData gameData = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());
            ChessGame game   = gameData.game();

            if (game.isOver()) {
                sendTo(session, new ErrorMessage("Error: the game is already over"));
                return;
            }

            ChessGame.TeamColor playerColor = colorOf(username, gameData);
            if (playerColor == null) {
                sendTo(session, new ErrorMessage("Error: observers cannot make moves"));
                return;
            }
            if (game.getTeamTurn() != playerColor) {
                sendTo(session, new ErrorMessage("Error: it is not your turn"));
                return;
            }

            ChessMove move = cmd.move;
            game.makeMove(move);
            gameService.updateGame(cmd.getAuthToken(), gameData);

            broadcastAll(cmd.getGameID(), new LoadGameMessage(gameData));

            String moveDesc = moveString(move);
            broadcastExcept(cmd.getGameID(), session,
                    new NotificationMessage(username + " moved " + moveDesc));

            // check / checkmate / stalemate
            ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            String opponentName = (opponent == ChessGame.TeamColor.WHITE)
                    ? gameData.whiteUsername() : gameData.blackUsername();
            if (opponentName == null) opponentName = opponent.name();

            if (game.isInCheckmate(opponent)) {
                game.setOver(true);
                gameService.updateGame(cmd.getAuthToken(), gameData);
                broadcastAll(cmd.getGameID(), new NotificationMessage("Checkmate! " + username + " wins!"));
            } else if (game.isInStalemate(opponent)) {
                game.setOver(true);
                gameService.updateGame(cmd.getAuthToken(), gameData);
                broadcastAll(cmd.getGameID(), new NotificationMessage("Stalemate! The game is a draw."));
            } else if (game.isInCheck(opponent)) {
                broadcastAll(cmd.getGameID(), new NotificationMessage(opponentName + " is in check!"));
            }

        } catch (InvalidMoveException e) {
            sendTo(session, new ErrorMessage("Error: invalid move"));
        } catch (ServiceException e) {
            sendTo(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleLeave(Session session, UserGameCommand cmd) throws IOException {
        try {
            String username  = userService.getUsername(cmd.getAuthToken());
            GameData gameData = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());

            ChessGame.TeamColor color = colorOf(username, gameData);
            if (color != null) {
                gameService.leaveGame(cmd.getAuthToken(), cmd.getGameID(), color);
            }

            gameSessions.getOrDefault(cmd.getGameID(), ConcurrentHashMap.newKeySet()).remove(session);

            broadcastExcept(cmd.getGameID(), session,
                    new NotificationMessage(username + " left the game"));

        } catch (ServiceException e) {
            sendTo(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleResign(Session session, UserGameCommand cmd) throws IOException {
        try {
            String username   = userService.getUsername(cmd.getAuthToken());
            GameData gameData = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());
            ChessGame game    = gameData.game();

            if (colorOf(username, gameData) == null) {
                sendTo(session, new ErrorMessage("Error: observers cannot resign"));
                return;
            }
            if (game.isOver()) {
                sendTo(session, new ErrorMessage("Error: the game is already over"));
                return;
            }

            game.setOver(true);
            gameService.updateGame(cmd.getAuthToken(), gameData);
            broadcastAll(cmd.getGameID(), new NotificationMessage(username + " resigned. Game over."));

        } catch (ServiceException e) {
            sendTo(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    // helpers

    private ChessGame.TeamColor colorOf(String username, GameData game) {
        if (username.equals(game.whiteUsername())) return ChessGame.TeamColor.WHITE;
        if (username.equals(game.blackUsername()))  return ChessGame.TeamColor.BLACK;
        return null;
    }

    private void sendTo(Session session, Object msg) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(gson.toJson(msg));
        }
    }

    private void broadcastAll(int gameID, Object msg) throws IOException {
        String json = gson.toJson(msg);
        for (Session s : gameSessions.getOrDefault(gameID, Set.of())) {
            if (s.isOpen()) s.getRemote().sendString(json);
        }
    }

    private void broadcastExcept(int gameID, Session exclude, Object msg) throws IOException {
        String json = gson.toJson(msg);
        for (Session s : gameSessions.getOrDefault(gameID, Set.of())) {
            if (s.isOpen() && !s.equals(exclude)) s.getRemote().sendString(json);
        }
    }

    private String moveString(ChessMove move) {
        char[] cols = {'a','b','c','d','e','f','g','h'};
        var s = move.getStartPosition();
        var e = move.getEndPosition();
        return "" + cols[s.getColumn()-1] + s.getRow()
                + "->" + cols[e.getColumn()-1] + e.getRow();
    }
}