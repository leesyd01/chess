package ui;

import chess.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static ui.EscapeSequences.*;

public class BoardDrawer {

    // board square colors
    private static final String LIGHT_SQUARE     = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE      = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_BG        = SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER_FG        = SET_TEXT_COLOR_WHITE;
    private static final String HIGHLIGHT_SQUARE = SET_BG_COLOR_GREEN;
    private static final String SELECTED_SQUARE  = SET_BG_COLOR_YELLOW;

    /** draws default starting board */
    public static void draw(String playerColor) {
        boolean flipped = "BLACK".equalsIgnoreCase(playerColor);
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println();
        drawBoard(board, flipped, null, null);
        System.out.println();
    }

    /** draws a live board from a ChessGame */
    public static void drawGame(ChessGame game, String playerColor) {
        boolean flipped = "BLACK".equalsIgnoreCase(playerColor);
        System.out.println();
        drawBoard(game.getBoard(), flipped, null, null);
        System.out.println();
    }

    /** draws a board with legal-move highlights for a selected piece */
    public static void drawHighlighted(ChessGame game, String playerColor, ChessPosition selected) {
        boolean flipped = "BLACK".equalsIgnoreCase(playerColor);
        Collection<ChessMove> moves = game.validMoves(selected);
        Set<ChessPosition> highlights = new HashSet<>();
        if (moves != null) {
            for (ChessMove m : moves) highlights.add(m.getEndPosition());
        }
        System.out.println();
        drawBoard(game.getBoard(), flipped, selected, highlights);
        System.out.println();
    }

    private static void drawBoard(ChessBoard board, boolean flipped,
                                  ChessPosition selected, Set<ChessPosition> highlights) {
        char[] cols = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

        int[] colOrder = flipped
                ? new int[]{7, 6, 5, 4, 3, 2, 1, 0}
                : new int[]{0, 1, 2, 3, 4, 5, 6, 7};

        int[] rowOrder = flipped
                ? new int[]{1, 2, 3, 4, 5, 6, 7, 8}
                : new int[]{8, 7, 6, 5, 4, 3, 2, 1};

        printColumnHeaders(cols, colOrder);
        for (int row : rowOrder) {
            printRow(board, row, colOrder, selected, highlights);
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

    private static void printRow(ChessBoard board, int row, int[] colOrder,
                                 ChessPosition selected, Set<ChessPosition> highlights) {
        System.out.print(BORDER_BG + BORDER_FG + " " + row + " ");

        for (int colIdx = 0; colIdx < 8; colIdx++) {
            int col = colOrder[colIdx] + 1;
            ChessPosition pos = new ChessPosition(row, col);

            String squareBg;
            if (selected != null && pos.equals(selected)) {
                squareBg = SELECTED_SQUARE;
            } else if (highlights != null && highlights.contains(pos)) {
                squareBg = HIGHLIGHT_SQUARE;
            } else {
                boolean isLight = (col + row) % 2 != 0;
                squareBg = isLight ? LIGHT_SQUARE : DARK_SQUARE;
            }

            ChessPiece piece = board.getPiece(pos);
            String pieceStr = piece != null ? getPieceSymbol(piece) : RESET_TEXT_COLOR + "   ";

            System.out.print(squareBg + pieceStr);
        }

        System.out.println(BORDER_BG + BORDER_FG + " " + row + " " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String color = isWhite ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
        String symbol = switch (piece.getPieceType()) {
            case KING   -> isWhite ? BLACK_KING   : WHITE_KING;
            case QUEEN  -> isWhite ? BLACK_QUEEN  : WHITE_QUEEN;
            case BISHOP -> isWhite ? BLACK_BISHOP : WHITE_BISHOP;
            case KNIGHT -> isWhite ? BLACK_KNIGHT : WHITE_KNIGHT;
            case ROOK   -> isWhite ? BLACK_ROOK   : WHITE_ROOK;
            case PAWN   -> isWhite ? BLACK_PAWN   : WHITE_PAWN;
        };
        return color + symbol;
    }
}