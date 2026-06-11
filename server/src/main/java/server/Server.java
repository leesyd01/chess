package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import handler.ClearHandler;
import handler.GameHandler;
import handler.UserHandler;
import io.javalin.Javalin;
import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {
    private final Javalin javalin;

    public Server() {
        DataAccess dataAccess;
        try {
            dataAccess = new MySqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }

        UserHandler userHandler   = new UserHandler(new UserService(dataAccess));
        GameHandler gameHandler   = new GameHandler(new GameService(dataAccess));
        ClearHandler clearHandler = new ClearHandler(new ClearService(dataAccess));

        WebSocketHandler wsHandler = new WebSocketHandler(gameService, userService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // WebSocket endpoint
        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {});
            es.onMessage(ctx -> wsHandler.onMessage(ctx.session, ctx.message()));
            ws.onClose(ctx -> wsHandler.onClose(ctx.session, ctx.status(), ctx.reason()));
            es.onError(ctx -> wsHandler.onError(ctx.session, ctx.error()));
        });

        javalin.post("/user",      userHandler::register);
        javalin.post("/session",   userHandler::login);
        javalin.delete("/session", userHandler::logout);

        javalin.get("/game",    gameHandler::listGames);
        javalin.post("/game",   gameHandler::createGame);
        javalin.put("/game",    gameHandler::joinGame);

        javalin.delete("/db", clearHandler::clear);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}