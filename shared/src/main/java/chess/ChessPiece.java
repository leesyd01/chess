package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {

        this.pieceColor = pieceColor;
        this.type = type;
    }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (pieceColor == null ? 0 : pieceColor.hashCode());
            hash = 31 * hash + (type == null ? 0 : type.hashCode());
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            ChessPiece other = (ChessPiece) o;
            return this.pieceColor == other.pieceColor && this.type == other.type;
        }



    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        return switch (piece.getPieceType()) {
            case KING   -> getSingleStepMoves(board, myPosition, piece,
                    new int[][]{{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1},{1,0},{-1,0}});
            case KNIGHT -> getSingleStepMoves(board, myPosition, piece,
                    new int[][]{{1,2},{2,1},{-2,-1},{-1,-2},{-1,2},{1,-2},{-2,1},{2,-1}});
            case QUEEN  -> getSlidingMoves(board, myPosition, piece,
                    new int[][]{{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1},{1,0},{-1,0}});
            case BISHOP -> getSlidingMoves(board, myPosition, piece,
                    new int[][]{{1,1},{-1,1},{1,-1},{-1,-1}});
            case ROOK   -> getSlidingMoves(board, myPosition, piece,
                    new int[][]{{0,1},{0,-1},{-1,0},{1,0}});
            case PAWN   -> getPawnMoves(board, myPosition, piece);
        };
    }

    /**
     * Generates moves for pieces that slide until blocked (Queen, Rook, Bishop).
     */
    private Collection<ChessMove> getSlidingMoves(ChessBoard board, ChessPosition myPosition,
                                                  ChessPiece piece, int[][] directions) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        for (int[] dir : directions) {
            int r = myPosition.getRow() + dir[0];
            int c = myPosition.getColumn() + dir[1];
            while (isInBounds(r, c)) {
                ChessPosition endPos = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(endPos);
                if (occupant == null) {
                    moves.add(new ChessMove(myPosition, endPos, null));
                } else if (occupant.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, endPos, null));
                    break;
                } else {
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    /** Generates moves for pieces that move one step at a time (King, Knight). */
    private Collection<ChessMove> getSingleStepMoves(ChessBoard board, ChessPosition myPosition,
                                                     ChessPiece piece, int[][] directions) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        for (int[] dir : directions) {
            int r = myPosition.getRow() + dir[0];
            int c = myPosition.getColumn() + dir[1];
            if (isInBounds(r, c)) {
                ChessPosition endPos = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(endPos);
                if (occupant == null || occupant.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, endPos, null));
                }
            }
        }
        return moves;
    }

    /** Generates all pawn moves including forward, two-square advance, and captures. */
    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition, ChessPiece piece) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow  = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int r = myPosition.getRow() + direction;
        int c = myPosition.getColumn();

        if (!isInBounds(r, c)) {
            return moves;
        }

        // Forward one square
        if (board.getPiece(new ChessPosition(r, c)) == null) {
            addPawnMoves(moves, myPosition, new ChessPosition(r, c), r);

            // Forward two squares from starting position
            if (myPosition.getRow() == startRow) {
                ChessPosition twoSquares = new ChessPosition(r + direction, c);
                if (board.getPiece(twoSquares) == null) {
                    moves.add(new ChessMove(myPosition, twoSquares, null));
                }
            }
        }

        // Diagonal captures
        for (int dc : new int[]{-1, 1}) {
            if (isInBounds(r, c + dc)) {
                ChessPosition capturePos = new ChessPosition(r, c + dc);
                ChessPiece occupant = board.getPiece(capturePos);
                if (occupant != null && occupant.getTeamColor() != piece.getTeamColor()) {
                    addPawnMoves(moves, myPosition, capturePos, r);
                }
            }
        }

        return moves;
    }

    /** Adds a pawn move, expanding to all promotion pieces if the pawn reaches the back rank. */
    private void addPawnMoves(ArrayList<ChessMove> moves, ChessPosition from, ChessPosition to, int row) {
        if (row == 1 || row == 8) {
            moves.add(new ChessMove(from, to, PieceType.QUEEN));
            moves.add(new ChessMove(from, to, PieceType.ROOK));
            moves.add(new ChessMove(from, to, PieceType.KNIGHT));
            moves.add(new ChessMove(from, to, PieceType.BISHOP));
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }

    /** Returns true if the given row and column are within the chess board bounds. */
    private boolean isInBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
