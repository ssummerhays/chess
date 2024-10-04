package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] squares = new ChessPiece[8][8];

    @Override
    public String toString() {
        String result = "";
        for (int r = 8; r > 0; r--) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = getPiece(pos);
                if (piece == null) {
                    result += "| ";
                } else {
                    result+= "|" + piece.toString();
                }
            }
            result += "|\n";
        }
        return result;
    }

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j <= 7; j++) {
                squares[i][j] = null;
            }
        }
        for (int i = 0; i <= 7; i++) {
            squares[1][i] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            squares[6][i] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
        // White reset
        squares[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        squares[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

        squares[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        squares[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);

        squares[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        squares[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);

        squares[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        squares[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);

        // Black reset
        squares[7][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        squares[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);

        squares[7][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        squares[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);

        squares[7][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        squares[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);

        squares[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        squares[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
    }

    public void makeHypotheticalMove(ChessMove move) {
        ChessPosition startPosition=move.getStartPosition();
        ChessPosition endPosition=move.getEndPosition();
        ChessPiece.PieceType promotionType=move.getPromotionPiece();

        ChessBoard board = this;

        ChessPiece piece=board.getPiece(startPosition);
        if (promotionType == null) {
            board.addPiece(endPosition, piece);
            board.addPiece(startPosition, null);
        } else {
            ChessPiece promotionPiece=new ChessPiece(piece.getTeamColor(), promotionType);
            board.addPiece(endPosition, promotionPiece);
            board.addPiece(startPosition, null);
        }
    }

    public boolean hypotheticalIsInCheck(ChessGame.TeamColor teamColor) {
        Collection<ChessMove> potentialMoves = new HashSet<>();
        ChessPosition kingPosition = new ChessPosition(1, 1);
        ChessPosition currentPosition;
        ChessPiece currentPiece;

        ChessBoard board = this;

        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                currentPosition = new ChessPosition(r, c);
                currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() != teamColor) {
                        potentialMoves.addAll(currentPiece.pieceMoves(board, currentPosition));
                    } else if (currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPosition= new ChessPosition(r, c);
                    }
                }
            }
        }

        for (ChessMove move : potentialMoves) {
            ChessPosition end = move.getEndPosition();
            if (end.getRow() == kingPosition.getRow() && end.getColumn() == kingPosition.getColumn()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that=(ChessBoard) o;
        return Arrays.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
