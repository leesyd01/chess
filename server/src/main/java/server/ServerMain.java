package server;

import chess.*;
import dataAccess.MemoryDataAccess;
import io.javalin.Javalin;
import dataAccess.DataAccess;
import handler.ClearHandler;
import handler.GameHandler;
import handler.UserHandler;
import service.ClearService;
import service.GameService;
import service.UserService;

public class ServerMain {
    private Javalin app;

    public static void main(String[] args) {
        new ServerMain().run(8080);
    }

    public int run(int desiredPort) {
        DataAccess dataAccess = new MemoryDataAccess();
        UserHandler userHandler = new UserHandler(new UserService(dataAccess));
        GameHandler gameHandler = new GameHandler(new GameService(dataAccess));
        ClearHandler clearHandler = new ClearHandler(new ClearService(dataAccess));

        app = Javalin.create().start(desiredPort);

        // endpoints to register
        app.post("/user", userHandler::register);
        app.post("/session", userHandler::login);
        app.delete("/session", userHandler::logout);

        app.get("/game", gameHandler::listGames);
        app.post("/game", gameHandler::createGame);
        app.put("/game", gameHandler::joinGame);

        app.delete("/db", clearHandler::clear);

        return app.port();
    }
}
