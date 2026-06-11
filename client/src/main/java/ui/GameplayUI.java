package ui;

import chess.*;
import client.ServerMessageObserver;
import client.WebSocketFacade;
import model.GameData;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Scanner;

public class GameplayUI implements ServerMessageObserver {

    private final WebSocketFacade ws;
    private final Scanner scanner;
    private final String authToken;
    private final int gameID;
    private final String playerColor; // "WHITE", "BLACK", or null for observer
    private final String username;

    private ChessGame currentGame;
    private volatile boolean gameOver = false;

    public GameplayUI(WebSocketFacade ws, Scanner scanner, String authToken,
                      int gameID, String playerColor, String username) {
        this.ws = ws;
        this.scanner = scanner;
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.username = username;
    }

    /** blocks until the user leaves or quits */
    public void run() {
        printHelp();
        while (true) {
            System.out.print("[IN-GAME] >>> ");
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {
                case "help"     -> printHelp();
                case "redraw"   -> redraw();
                case "leave"    -> { handleLeave(); return; }
                case "move"     -> handleMove(parts);
                case "resign"   -> handleResign();
                case "highlight"-> handleHighlight(parts);
                default -> System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }

    // ServerMessageObserver callbacks, called from WS thread

    @Override
    public void onLoadGame(LoadGameMessage msg) {
        currentGame = msg.game.game();
        System.out.println(); // blank line before board
        BoardDrawer.drawGame(currentGame, playerColor);
        System.out.print("[IN-GAME] >>> "); // re-print prompt
    }

    @Override
    public void onError(ErrorMessage msg) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED
                + "Error: " + msg.errorMessage + EscapeSequences.RESET_TEXT_COLOR);
        System.out.print("[IN-GAME] >>> ");
    }

    @Override
    public void onNotification(NotificationMessage msg) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_YELLOW
                + ">> " + msg.message + EscapeSequences.RESET_TEXT_COLOR);
        System.out.print("[IN-GAME] >>> ");
    }

    // command handlers

    private void printHelp() {
        System.out.println("""
                  help                        - show this menu
                  redraw                      - redraw the board
                  move <e2> <e4> [promotion]  - make a move (e.g. move e2 e4)
                  highlight <e2>              - show legal moves for a piece
                  resign                      - forfeit the game
                  leave                       - leave (game continues)
                """);
    }

    private void redraw() {
        if (currentGame != null) {
            BoardDrawer.drawGame(currentGame, playerColor);
        } else {
            System.out.println("No board to draw yet.");
        }
    }

    private void handleLeave() {
        try {
            ws.sendLeave(authToken, gameID);
            ws.close();
            System.out.println("You left the game.");
        } catch (Exception e) {
            System.out.println("Error leaving: " + e.getMessage());
        }
    }

    private void handleMove(String[] parts) {
        if (playerColor == null) {
            System.out.println("Observers cannot make moves.");
            return;
        }
        if (parts.length < 3) {
            System.out.println("Usage: move <from> <to>  e.g. move e2 e4");
            return;
        }
        ChessPosition from = parsePosition(parts[1]);
        ChessPosition to   = parsePosition(parts[2]);
        if (from == null || to == null) {
            System.out.println("Invalid position. Use format like e2 or a1.");
            return;
        }

        // optional promotion piece
        ChessPiece.PieceType promotion = null;
        if (parts.length >= 4) {
            promotion = parsePromotion(parts[3]);
            if (promotion == null) {
                System.out.println("Invalid promotion piece. Use queen/rook/bishop/knight.");
                return;
            }
        }

        try {
            ws.sendMakeMove(authToken, gameID, new ChessMove(from, to, promotion));
        } catch (Exception e) {
            System.out.println("Error sending move: " + e.getMessage());
        }
    }

    private void handleResign() {
        if (playerColor == null) {
            System.out.println("Observers cannot resign.");
            return;
        }
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            try {
                ws.sendResign(authToken, gameID);
            } catch (Exception e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private void handleHighlight(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: highlight <position>  e.g. highlight e2");
            return;
        }
        ChessPosition pos = parsePosition(parts[1]);
        if (pos == null) {
            System.out.println("Invalid position. Use format like e2 or a1.");
            return;
        }
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }
        BoardDrawer.drawHighlighted(currentGame, playerColor, pos);
    }

    // parse helpers

    /** parses "e2" -> ChessPosition(2, 5). returns null on bad input */
    private ChessPosition parsePosition(String s) {
        if (s == null || s.length() != 2) return null;
        char colChar = Character.toLowerCase(s.charAt(0));
        char rowChar = s.charAt(1);
        if (colChar < 'a' || colChar > 'h') return null;
        if (rowChar < '1' || rowChar > '8') return null;
        int col = colChar - 'a' + 1;
        int row = rowChar - '0';
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String s) {
        return switch (s.toLowerCase()) {
            case "queen",  "q" -> ChessPiece.PieceType.QUEEN;
            case "rook",   "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }
}