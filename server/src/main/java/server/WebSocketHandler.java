package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import io.javalin.websocket.*;
import model.GameData;
import service.GameService;
import service.ServiceException;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler {

    private static final ConcurrentHashMap<Integer, Set<WsContext>> GAME_SESSIONS = new ConcurrentHashMap<>();

    private final GameService gameService;
    private final UserService userService;
    private final Gson gson = new Gson();

    public WebSocketHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    public void onConnect(WsConnectContext ctx) {}

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand base = gson.fromJson(ctx.message(), UserGameCommand.class);
            switch (base.getCommandType()) {
                case CONNECT   -> handleConnect(ctx, base);
                case MAKE_MOVE -> handleMakeMove(ctx, gson.fromJson(ctx.message(), MakeMoveCommand.class));
                case LEAVE     -> handleLeave(ctx, base);
                case RESIGN    -> handleResign(ctx, base);
            }
        } catch (Exception e) {
            sendTo(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void onClose(WsCloseContext ctx) {
        GAME_SESSIONS.values().forEach(set -> set.remove(ctx));
    }

    public void onError(WsErrorContext ctx) {
        System.err.println("WebSocket error: " + ctx.error());
    }

    private void handleConnect(WsContext ctx, UserGameCommand cmd) {
        try {
            String username = userService.getUsername(cmd.getAuthToken());
            GameData game = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());

            GAME_SESSIONS.computeIfAbsent(cmd.getGameID(), k -> ConcurrentHashMap.newKeySet()).add(ctx);

            sendTo(ctx, new LoadGameMessage(game));

            String role;
            if (username.equals(game.whiteUsername())) { role = "joined as WHITE"; }
            else if (username.equals(game.blackUsername())) { role = "joined as BLACK"; }
            else { role = "joined as an observer"; }

            broadcastExcept(cmd.getGameID(), ctx, new NotificationMessage(username + " " + role));

        } catch (ServiceException e) {
            sendTo(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleMakeMove(WsContext ctx, MakeMoveCommand cmd) {
        try {
            String username   = userService.getUsername(cmd.getAuthToken());
            GameData gameData = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());
            ChessGame game    = gameData.game();

            if (game.isOver()) {
                sendTo(ctx, new ErrorMessage("Error: the game is already over"));
                return;
            }

            ChessGame.TeamColor playerColor = colorOf(username, gameData);
            if (playerColor == null) {
                sendTo(ctx, new ErrorMessage("Error: observers cannot make moves"));
                return;
            }
            if (game.getTeamTurn() != playerColor) {
                sendTo(ctx, new ErrorMessage("Error: it is not your turn"));
                return;
            }

            game.makeMove(cmd.move);
            gameService.updateGame(cmd.getAuthToken(), gameData);

            broadcastAll(cmd.getGameID(), new LoadGameMessage(gameData));

            String moveDesc = moveString(cmd.move);
            broadcastExcept(cmd.getGameID(), ctx, new NotificationMessage(username + " moved " + moveDesc));

            ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            String opponentName = (opponent == ChessGame.TeamColor.WHITE)
                    ? gameData.whiteUsername() : gameData.blackUsername();
            if (opponentName == null) { opponentName = opponent.name(); }

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
            sendTo(ctx, new ErrorMessage("Error: invalid move"));
        } catch (ServiceException e) {
            sendTo(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) {
        try {
            String username   = userService.getUsername(cmd.getAuthToken());
            GameData gameData = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());

            ChessGame.TeamColor color = colorOf(username, gameData);
            if (color != null) {
                gameService.leaveGame(cmd.getAuthToken(), cmd.getGameID(), color);
            }

            GAME_SESSIONS.getOrDefault(cmd.getGameID(), ConcurrentHashMap.newKeySet()).remove(ctx);
            broadcastExcept(cmd.getGameID(), ctx, new NotificationMessage(username + " left the game"));

        } catch (ServiceException e) {
            sendTo(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void handleResign(WsContext ctx, UserGameCommand cmd) {
        try {
            String username   = userService.getUsername(cmd.getAuthToken());
            GameData gameData = gameService.getGame(cmd.getAuthToken(), cmd.getGameID());
            ChessGame game    = gameData.game();

            if (colorOf(username, gameData) == null) {
                sendTo(ctx, new ErrorMessage("Error: observers cannot resign"));
                return;
            }
            if (game.isOver()) {
                sendTo(ctx, new ErrorMessage("Error: the game is already over"));
                return;
            }

            game.setOver(true);
            gameService.updateGame(cmd.getAuthToken(), gameData);
            broadcastAll(cmd.getGameID(), new NotificationMessage(username + " resigned. Game over."));

        } catch (ServiceException e) {
            sendTo(ctx, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private ChessGame.TeamColor colorOf(String username, GameData game) {
        if (username.equals(game.whiteUsername())) { return ChessGame.TeamColor.WHITE; }
        if (username.equals(game.blackUsername()))  { return ChessGame.TeamColor.BLACK; }
        return null;
    }

    private void sendTo(WsContext ctx, Object msg) {
        ctx.send(gson.toJson(msg));
    }

    private void broadcastAll(int gameID, Object msg) {
        String json = gson.toJson(msg);
        for (WsContext ctx : GAME_SESSIONS.getOrDefault(gameID, Set.of())) {
            ctx.send(json);
        }
    }

    private void broadcastExcept(int gameID, WsContext exclude, Object msg) {
        String json = gson.toJson(msg);
        for (WsContext ctx : GAME_SESSIONS.getOrDefault(gameID, Set.of())) {
            if (!ctx.equals(exclude)) { ctx.send(json); }
        }
    }

    private String moveString(chess.ChessMove move) {
        char[] cols = {'a','b','c','d','e','f','g','h'};
        var s = move.getStartPosition();
        var e = move.getEndPosition();
        return "" + cols[s.getColumn()-1] + s.getRow()
                + "->" + cols[e.getColumn()-1] + e.getRow();
    }
}