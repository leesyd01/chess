package server;

import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
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
        DataAccess dataAccess = new MemoryDataAccess();

        UserHandler userHandler   = new UserHandler(new UserService(dataAccess));
        GameHandler gameHandler   = new GameHandler(new GameService(dataAccess));
        ClearHandler clearHandler = new ClearHandler(new ClearService(dataAccess));

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

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