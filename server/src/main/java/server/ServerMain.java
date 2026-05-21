package server;

import chess.*;
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

        app.post("/user", userHandler::register);
        app.post("/session", userHandler::login);
        app.delete("/session", userHandler::logout);

        app.get("/game", gameHandler::listGames);
        app.get("/game", gameHandler::createGame);
        app.get("/game", gameHandler::joinGame);

        app.delete("/db", clearHandler::clear);

        return app.port();
    }

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
