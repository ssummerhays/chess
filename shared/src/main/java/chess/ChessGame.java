package chess;

import chess.calculators.*;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard chessBoard = new ChessBoard();

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        ChessPiece.PieceType type = piece.getPieceType();
        ChessGame.TeamColor color = piece.getTeamColor();

        Collection<ChessMove> moves = new HashSet<>();
        if (isInCheck(color)) {
            switch (type) {
                case KING -> moves=new KingMovesCalculator().pieceMovesCheck(chessBoard, startPosition);
                case QUEEN -> moves=new QueenMovesCalculator().pieceMovesCheck(chessBoard, startPosition);
                case ROOK -> moves=new RookMovesCalculator().pieceMovesCheck(chessBoard, startPosition);
                case BISHOP -> moves=new BishopMovesCalculator().pieceMovesCheck(chessBoard, startPosition);
                case KNIGHT -> moves=new KnightMovesCalculator().pieceMovesCheck(chessBoard, startPosition);
                case PAWN -> moves=new PawnMovesCalculator().pieceMovesCheck(chessBoard, startPosition);
            }
        } else {
            switch (type) {
                case KING -> moves=new KingMovesCalculator().pieceMoves(chessBoard, startPosition);
                case QUEEN -> moves=new QueenMovesCalculator().pieceMoves(chessBoard, startPosition);
                case ROOK -> moves=new RookMovesCalculator().pieceMoves(chessBoard, startPosition);
                case BISHOP -> moves=new BishopMovesCalculator().pieceMoves(chessBoard, startPosition);
                case KNIGHT -> moves=new KnightMovesCalculator().pieceMoves(chessBoard, startPosition);
                case PAWN -> moves=new PawnMovesCalculator().pieceMoves(chessBoard, startPosition);
            }
        }
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> potentialMoves = new HashSet<>();
        ChessPosition kingPosition = new ChessPosition(1, 1);
        ChessPosition currentPosition;
        ChessPiece currentPiece;

        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                currentPosition = new ChessPosition(r, c);
                currentPiece = chessBoard.getPiece(currentPosition);
                if (currentPiece.getTeamColor() != teamColor) {
                    potentialMoves.addAll(currentPiece.pieceMoves(chessBoard, currentPosition));
                } else if (currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = currentPosition;
                }

            }
        }

      for (ChessMove move : potentialMoves) {
          ChessPosition end = move.getEndPosition();
          if (end == kingPosition) {
              return true;
          }
      }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented");
    }
}
