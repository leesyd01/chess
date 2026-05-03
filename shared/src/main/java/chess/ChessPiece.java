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
            int[][] directions = {{0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {1, 0}, {-1, 0}};

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

            int r = myPosition.getRow() + direction;
            int c = myPosition.getColumn();

            if (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition endPos = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(endPos);

                if (board.getPiece(new ChessPosition(r, c)) == null) {
                    if (r == 8 || r == 1) {
                        moves.add(new ChessMove(myPosition, endPos, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, endPos, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, endPos, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, endPos, PieceType.BISHOP));
                    } else {
                        moves.add(new ChessMove(myPosition, endPos, null));
                    }                }

                if (myPosition.getRow() == 2 && occupant == null && piece.getTeamColor() == ChessGame.TeamColor.WHITE) { // white pawn moves two spaces forward
                    ChessPosition pawnTwoMoves = new ChessPosition(myPosition.getRow() + direction * 2, c);
                    if (board.getPiece(pawnTwoMoves) == null) {
                        moves.add(new ChessMove(myPosition, pawnTwoMoves, null));
                    }
                }

                if (myPosition.getRow() == 7 && occupant == null && piece.getTeamColor() == ChessGame.TeamColor.BLACK) { // black pawn moves two spaces forward
                    ChessPosition pawnTwoMoves = new ChessPosition(myPosition.getRow() + direction * 2, c);
                    if (board.getPiece(pawnTwoMoves) == null) {
                        moves.add(new ChessMove(myPosition, pawnTwoMoves, null));
                    }
                }

                if (c + 1 <= 8) {
                    // right side capture
                    ChessPosition pawnCaptureRight = new ChessPosition(r, c + 1);
                    ChessPiece pawnCaptureRightOcc = board.getPiece(pawnCaptureRight);

                    if (pawnCaptureRightOcc != null && pawnCaptureRightOcc.getTeamColor() != piece.getTeamColor()) {
                        if (r == 8 || r == 1) {
                            moves.add(new ChessMove(myPosition, pawnCaptureRight, PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, pawnCaptureRight, PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, pawnCaptureRight, PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, pawnCaptureRight, PieceType.BISHOP));
                        } else {
                            moves.add(new ChessMove(myPosition, pawnCaptureRight, null));
                        }
                    }
                }

                if (c - 1 >= 1) {
                    // left side capture
                    ChessPosition pawnCaptureLeft = new ChessPosition(r, c - 1);
                    ChessPiece pawnCaptureLeftOcc = board.getPiece(pawnCaptureLeft);

                    if (pawnCaptureLeftOcc != null && pawnCaptureLeftOcc.getTeamColor() != piece.getTeamColor()) {
                        if (r == 8 || r == 1) {
                            moves.add(new ChessMove(myPosition, pawnCaptureLeft, PieceType.QUEEN));
                            moves.add(new ChessMove(myPosition, pawnCaptureLeft, PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, pawnCaptureLeft, PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, pawnCaptureLeft, PieceType.BISHOP));
                        } else {
                            moves.add(new ChessMove(myPosition, pawnCaptureLeft, null));
                            }
                        }
                    }
            }
            return moves;
        }
        return List.of();
    }
}
