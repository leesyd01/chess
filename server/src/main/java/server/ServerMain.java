package server;

import chess.*;
import io.javalin.Javalin;
import dataAccess.DataAccess;

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



    }

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
