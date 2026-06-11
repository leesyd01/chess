package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
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

        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

        UserHandler userHandler   = new UserHandler(userService);
        GameHandler gameHandler   = new GameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(new ClearService(dataAccess));

        WebSocketHandler wsHandler = new WebSocketHandler(gameService, userService);

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
        });

        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> wsHandler.onConnect(ctx));
            ws.onMessage(ctx -> wsHandler.onMessage(ctx));
            ws.onClose(ctx -> wsHandler.onClose(ctx));
            ws.onError(ctx -> wsHandler.onError(ctx));
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