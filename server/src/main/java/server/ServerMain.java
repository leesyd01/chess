package server;

import chess.*;
import io.javalin.Javalin;

public class ServerMain {
    private Javalin app;

    public static void main(String[] args) {
        new ServerMain().run(8080);
    }

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}
