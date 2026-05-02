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
        if (piece.getPieceType() == PieceType.KING) {
            ArrayList<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {1, 0}, {-1, 0}};

            for (int[] dir : directions) {
                int r = myPosition.getRow() + dir[0];
                int c = myPosition.getColumn() + dir[1];

                if (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                    ChessPosition endPos = new ChessPosition(r, c);
                    ChessPiece occupant = board.getPiece(endPos);

                    if (occupant == null) {
                        moves.add(new ChessMove(myPosition, endPos, null));
                    } else if (occupant.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, endPos, null));
                    }
                    r += dir[0];
                    c += dir[1];
                }
            }
            return moves;
        }

        if (piece.getPieceType() == PieceType.QUEEN) {
            ArrayList<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {1, 0}, {-1, 0}}; // put in moves

            for (int[] dir : directions) {
                int r = myPosition.getRow() + dir[0];
                int c = myPosition.getColumn() + dir[1];

                while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
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

        if (piece.getPieceType() == PieceType.BISHOP) {
            ArrayList<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

            for (int[] dir : directions) {
                int r = myPosition.getRow() + dir[0];
                int c = myPosition.getColumn() + dir[1];

                while (r >= 1 && r <= 8 && c >= 1 && c <=8 ) {
                    ChessPosition endPos = new ChessPosition(r, c); // end position
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

        if (piece.getPieceType() == PieceType.KNIGHT) {
            ArrayList<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{1, 2}, {2, 1}, {-2, -1}, {-1, -2}, {-1, 2}, {1, -2}, {-2, 1}, {2, -1}};

            for (int[] dir : directions) {
                int r = myPosition.getRow() + dir[0];
                int c = myPosition.getColumn() + dir[1];

                if (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                    ChessPosition endPos = new ChessPosition(r, c);
                    ChessPiece occupant = board.getPiece(endPos);

                    if (occupant == null) {
                        moves.add(new ChessMove(myPosition, endPos, null));
                    } else if (occupant.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, endPos, null));
                    }
                    r += dir[0];
                    c += dir[1];
                }
            }
            return moves;
        }

        if (piece.getPieceType() == PieceType.ROOK) {
            ArrayList<ChessMove> moves = new ArrayList<>();
            int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

            for (int[] dir : directions) {
                int r = myPosition.getRow() + dir[0];
                int c = myPosition.getColumn() + dir[1];

                while (r >=1 && r <= 8 && c >= 1 && c <= 8) {
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

        if (piece.getPieceType() == PieceType.PAWN) {
            ArrayList<ChessMove> moves = new ArrayList<>();

            int direction;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                direction = 1;
            } else {
                direction = -1;
            }

                if (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                    ChessPosition endPos = new ChessPosition(r, c);
                    ChessPiece occupant = board.getPiece(endPos);

                    // if occupant is empty: move forward one (1+ or 8- depending on color)
                    // if occupant and occupant + 1 is empty: move forward 2 (direction depends on color)
                    // if diagonal has enemy occupant (occupant != null): capture and move to spot
                    // promotion: turn into queen, rook, bishop, knight

                    if (occupant == null) {
                        moves.add(new ChessMove(myPosition, endPos, null));
                    } else if (occupant.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, endPos, null));
                    }

//                    r += dir[0];
//                    c += dir[1];
                }
            }
            return moves;
        }
        return List.of();
    }
}
