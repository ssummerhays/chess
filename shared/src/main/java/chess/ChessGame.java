package chess;

import chess.calculators.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    public ChessBoard chessBoard;
    private TeamColor turnColor;

    public ChessGame() {
        this.chessBoard = new ChessBoard();
        chessBoard.resetBoard();
        this.turnColor = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turnColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turnColor = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ChessGame chessGame=(ChessGame) o;
        return Objects.equals(chessBoard, chessGame.chessBoard) && turnColor == chessGame.turnColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chessBoard, turnColor);
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

        Collection<ChessMove> potentialMoves = new HashSet<>();
        switch (type) {
            case KING -> potentialMoves=new KingMovesCalculator().pieceMoves(chessBoard, startPosition);
            case QUEEN -> potentialMoves=new QueenMovesCalculator().pieceMoves(chessBoard, startPosition);
            case ROOK -> potentialMoves=new RookMovesCalculator().pieceMoves(chessBoard, startPosition);
            case BISHOP -> potentialMoves=new BishopMovesCalculator().pieceMoves(chessBoard, startPosition);
            case KNIGHT -> potentialMoves=new KnightMovesCalculator().pieceMoves(chessBoard, startPosition);
            case PAWN -> potentialMoves=new PawnMovesCalculator().pieceMoves(chessBoard, startPosition);
        }

        Collection<ChessMove> validMovesResult = new HashSet<>();
        for (ChessMove move : potentialMoves) {
            if (wontBeInCheck(move, color)) {
                validMovesResult.add(move);
            }
        }
        return validMovesResult;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition=move.getStartPosition();
        ChessPosition endPosition=move.getEndPosition();
        ChessPiece.PieceType promotionType=move.getPromotionPiece();

        ChessPiece piece=chessBoard.getPiece(startPosition);
        if (piece == null) {
            throw new InvalidMoveException();
        } else {
            if (validMoves(move.getStartPosition()).contains(move)) {
                if (piece.getTeamColor() == turnColor && wontBeInCheck(move, piece.getTeamColor())) {
                    if (promotionType == null) {
                        chessBoard.addPiece(endPosition, piece);
                        chessBoard.addPiece(startPosition, null);
                    } else {
                        ChessPiece promotionPiece=new ChessPiece(piece.getTeamColor(), promotionType);
                        chessBoard.addPiece(endPosition, promotionPiece);
                        chessBoard.addPiece(startPosition, null);
                    }
                    if (piece.getTeamColor() == TeamColor.WHITE) {
                        setTeamTurn(TeamColor.BLACK);
                    } else {
                        setTeamTurn(TeamColor.WHITE);
                    }

                } else {
                    throw new InvalidMoveException();
                }


            } else {
                throw new InvalidMoveException();
            }
        }

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
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() != teamColor) {
                        potentialMoves.addAll(currentPiece.pieceMoves(chessBoard, currentPosition));
                    } else if (currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPosition=currentPosition;
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

    public boolean wontBeInCheck(ChessMove move, TeamColor teamColor) {
        ChessBoard board = new ChessBoard(chessBoard);
        board.makeHypotheticalMove(move);
      return !board.hypotheticalIsInCheck(teamColor);
    }

    public boolean noMoves(TeamColor teamColor) {
        Collection<ChessMove> allMoves = new HashSet<>();

        ChessPosition currentPosition;
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                currentPosition = new ChessPosition(r, c);
                ChessPiece currentPiece = chessBoard.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() == teamColor) {
                        Collection<ChessMove> validMoveCollection = validMoves(currentPosition);
                        allMoves.addAll(validMoveCollection);
                    }
                }
            }
        }
        return allMoves.isEmpty();
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return noMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return noMoves(teamColor);
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
        return chessBoard;
    }
}
