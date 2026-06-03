package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class BoardDrawer {

    // board square colors
    private static final String LIGHT_SQUARE = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE  = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_BG    = SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER_FG    = SET_TEXT_COLOR_WHITE;

    /** draws board for given perspective
     * playerColor = "WHITE" -> a1 bottom left
     * playerColor = "BLACK" -> a1 top right (flipped)
     * playerColor = null -> observer
     */

    public static void draw(String playerColor) {
        boolean flipped = "BLACK".equalsIgnoreCase(playerColor);

        // build default starting board
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        System.out.println();
        drawBoard(board, flipped);
        System.out.println();
    }

    private static void drawBoard(ChessBoard board, boolean flipped) {
        char[] cols = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

        int[] colOrder = flipped
                ? new int[]{7, 6, 5, 4, 3, 2, 1, 0}
                : new int[]{0, 1, 2, 3, 4, 5, 6, 7};

        int[] rowOrder = flipped
                ? new int[]{7, 6, 5, 4, 3, 2, 1, 1}
                : new int[]{0, 1, 2, 3, 4, 5, 6, 7};

        printColumnHeaders(cols, colOrder);
        for (int row : rowOrder) {
            printRow(board, row, colOrder, flipped);
        }
        printColumnHeaders(cols, colOrder);
    }

    private static void printColumnHeaders(char[] cols, int[] colOrder) {
        System.out.print(BORDER_BG + BORDER_FG + "  ");
        for (int c : colOrder) {
            System.out.print(" " + cols[c] + " ");
        }
        System.out.println("    " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static void printRow(ChessBoard board, int row, int[] colOrder, boolean flipped) {
        System.out.print(BORDER_BG + BORDER_FG + " " + row + " ");

        for (int colIdx = 0; colIdx < 8; colIdx++) {
            int col = colOrder[colIdx] + 1;
            boolean isLight = (col + row) % 2 == 0;
            String squareBg = isLight ? LIGHT_SQUARE : DARK_SQUARE;

            ChessPiece piece = board.getPiece(new ChessPosition(row, col));
            String pieceStr = piece != null ? getPieceSymbol(piece) : "    ";

            System.out.print(squareBg + pieceStr);
        }

        System.out.println(BORDER_BG + BORDER_FG + " " + row + " " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String color = isWhite ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
        String symbol = switch (piece.getPieceType()) {
            case KING   -> isWhite ? WHITE_KING   : BLACK_KING;
            case QUEEN  -> isWhite ? WHITE_QUEEN  : BLACK_QUEEN;
            case BISHOP -> isWhite ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK   -> isWhite ? WHITE_ROOK   : BLACK_ROOK;
            case PAWN   -> isWhite ? WHITE_PAWN   : BLACK_PAWN;
        };
        return color + symbol;
    }
}