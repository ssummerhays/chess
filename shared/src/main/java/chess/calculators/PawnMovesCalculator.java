package chess.calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnMovesCalculator {
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
    ChessPiece piece = board.getPiece(myPosition);
    ChessGame.TeamColor pieceColor = piece.getTeamColor();

    Collection<ChessMove> moves=new HashSet<>();

    int newRow = myPosition.getRow();
    int newCol = myPosition.getColumn();

    if (pieceColor == ChessGame.TeamColor.WHITE) {
      newRow += 1;

      ChessPosition endPosition = new ChessPosition(newRow, newCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);

      boolean rightWall = newCol == 8;
      boolean leftWall = newCol == 1;

      if (board.getPiece(endPosition) != null) {
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        }
      }

      if (myPosition.getRow() == 2) {
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }

        newRow += 1;
        endPosition = new ChessPosition(newRow, newCol);
        move = new ChessMove(myPosition, endPosition, null);
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }

        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        }

      } else if (myPosition.getRow() == 7) {
        endPosition = new ChessPosition(newRow, newCol);

        if (board.getPiece(endPosition) == null) {
          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
          moves.add(move);
        }

        if (newCol < 8) {
          endPosition = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }

        if (newCol > 1) {
          endPosition = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }

      } else {
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          }
        } else {
          if (board.getPiece(endPosition) == null) {
            moves.add(move);
          }
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        }
      }
    }

//    BlackPawn
    else {
      newRow -= 1;

      ChessPosition endPosition = new ChessPosition(newRow, newCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);

      boolean rightWall = newCol == 8;
      boolean leftWall = newCol == 1;

      if (board.getPiece(endPosition) != null) {
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        }
      }

      if (myPosition.getRow() == 7) {
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }

        newRow -= 1;
        endPosition = new ChessPosition(newRow, newCol);
        move = new ChessMove(myPosition, endPosition, null);
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }

        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        }

      } else if (myPosition.getRow() == 2) {
        endPosition = new ChessPosition(newRow, newCol);

        if (board.getPiece(endPosition) == null) {
          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
          moves.add(move);

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
          moves.add(move);
        }

        if (newCol < 8) {
          endPosition = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }

        if (newCol > 1) {
          endPosition = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            moves.add(move);

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            moves.add(move);
          }
        }

      } else {
        if (board.getPiece(endPosition) == null) {
          moves.add(move);
        }
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          }
        } else {
          if (board.getPiece(endPosition) == null) {
            moves.add(move);
          }
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              moves.add(rightMove);
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              moves.add(leftMove);
            }
          }
        }
      }
    }

    return moves;
  }

  public Collection<ChessMove> pieceMovesCheck(ChessGame game, ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor teamColor) {
    ChessPiece piece = board.getPiece(myPosition);
    ChessGame.TeamColor pieceColor = piece.getTeamColor();

    Collection<ChessMove> moves=new HashSet<>();

    int newRow = myPosition.getRow();
    int newCol = myPosition.getColumn();

    if (pieceColor == ChessGame.TeamColor.WHITE) {
      newRow+=1;

      ChessPosition endPosition=new ChessPosition(newRow, newCol);
      ChessMove move=new ChessMove(myPosition, endPosition, null);

      boolean rightWall=newCol == 8;
      boolean leftWall=newCol == 1;

      if (board.getPiece(endPosition) != null) {
        if (rightWall) {
          ChessPosition left=new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove=new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        } else if (leftWall) {
          ChessPosition right=new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove=new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          }
        } else {
          ChessPosition right=new ChessPosition(newRow, newCol + 1);
          ChessPosition left=new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove=new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove=new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove rightMove=new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
              ChessMove leftMove=new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        }
      }

      if (myPosition.getRow() == 2) {
        if (board.getPiece(endPosition) == null) {
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }

          newRow+=1;
          endPosition=new ChessPosition(newRow, newCol);
          move=new ChessMove(myPosition, endPosition, null);
          if (board.getPiece(endPosition) == null) {
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }

          if (rightWall) {
            ChessPosition left=new ChessPosition(newRow, newCol - 1);
            if (board.getPiece(left) == null) {
              return moves;
            } else {
              if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove leftMove=new ChessMove(myPosition, left, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(leftMove);
                }
              }
            }
          } else if (leftWall) {
            ChessPosition right=new ChessPosition(newRow, newCol + 1);
            if (board.getPiece(right) == null) {
              return moves;
            } else {
              if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove rightMove=new ChessMove(myPosition, right, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(rightMove);
                }
              }
            }
          } else {
            ChessPosition right=new ChessPosition(newRow, newCol + 1);
            ChessPosition left=new ChessPosition(newRow, newCol - 1);
            if (board.getPiece(right) == null && board.getPiece(left) == null) {
              return moves;
            } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
              if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove rightMove=new ChessMove(myPosition, right, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(rightMove);
                }
              }
              if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove leftMove=new ChessMove(myPosition, left, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(leftMove);
                }
              }
            } else if (board.getPiece(right) != null) {
              if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove rightMove=new ChessMove(myPosition, right, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(rightMove);
                }
              }
            } else if (board.getPiece(left) != null) {
              if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove leftMove=new ChessMove(myPosition, left, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(leftMove);
                }
              }
            }
          }

        } else if (myPosition.getRow() == 7) {
          endPosition=new ChessPosition(newRow, newCol);

          if (board.getPiece(endPosition) == null) {
            move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }

          if (newCol < 8) {
            endPosition=new ChessPosition(newRow, newCol + 1);
            if (board.getPiece(endPosition) != null) {
              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }

              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }

              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }

              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }
            }
          }

          if (newCol > 1) {
            endPosition=new ChessPosition(newRow, newCol - 1);
            if (board.getPiece(endPosition) != null) {
              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }

              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }

              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }

              move=new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }
            }
          }

        } else {
          if (board.getPiece(endPosition) == null) {
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
          if (rightWall) {
            ChessPosition left=new ChessPosition(newRow, newCol - 1);
            if (board.getPiece(left) == null) {
              return moves;
            } else {
              if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove leftMove=new ChessMove(myPosition, left, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(leftMove);
                }
              }
            }
          } else if (leftWall) {
            ChessPosition right=new ChessPosition(newRow, newCol + 1);
            if (board.getPiece(right) == null) {
              return moves;
            } else {
              if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove rightMove=new ChessMove(myPosition, right, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(rightMove);
                }
              }
            }
          } else {
            if (board.getPiece(endPosition) == null) {
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(move);
              }
            }
            ChessPosition right=new ChessPosition(newRow, newCol + 1);
            ChessPosition left=new ChessPosition(newRow, newCol - 1);
            if (board.getPiece(right) == null && board.getPiece(left) == null) {
              return moves;
            } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
              if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove rightMove=new ChessMove(myPosition, right, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(rightMove);
                }
              }
              if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove leftMove=new ChessMove(myPosition, left, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(leftMove);
                }
              }
            } else if (board.getPiece(right) != null) {
              if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove rightMove=new ChessMove(myPosition, right, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(rightMove);
                }
              }
            } else if (board.getPiece(left) != null) {
              if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.BLACK) {
                ChessMove leftMove=new ChessMove(myPosition, left, null);
                if (!game.willBeInCheck(move, teamColor)) {
                  moves.add(leftMove);
                }
              }
            }
          }
        }
      }
    }

//    BlackPawn
    else {
      newRow -= 1;

      ChessPosition endPosition = new ChessPosition(newRow, newCol);
      ChessMove move = new ChessMove(myPosition, endPosition, null);

      boolean rightWall = newCol == 8;
      boolean leftWall = newCol == 1;

      if (board.getPiece(endPosition) != null) {
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        }
      }

      if (myPosition.getRow() == 7) {
        if (board.getPiece(endPosition) == null) {
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }
        }

        newRow -= 1;
        endPosition = new ChessPosition(newRow, newCol);
        move = new ChessMove(myPosition, endPosition, null);
        if (board.getPiece(endPosition) == null) {
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }
        }

        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          }
        } else {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        }

      } else if (myPosition.getRow() == 2) {
        endPosition = new ChessPosition(newRow, newCol);

        if (board.getPiece(endPosition) == null) {
          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }

          move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }
        }

        if (newCol < 8) {
          endPosition = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
        }

        if (newCol > 1) {
          endPosition = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(endPosition) != null) {
            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.QUEEN);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.ROOK);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.BISHOP);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }

            move = new ChessMove(myPosition, endPosition, ChessPiece.PieceType.KNIGHT);
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
        }

      } else {
        if (board.getPiece(endPosition) == null) {
          if (!game.willBeInCheck(move, teamColor)) {
            moves.add(move);
          }
        }
        if (rightWall) {
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(left) == null) {
            return moves;
          } else {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        } else if (leftWall) {
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          if (board.getPiece(right) == null) {
            return moves;
          } else {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          }
        } else {
          if (board.getPiece(endPosition) == null) {
            if (!game.willBeInCheck(move, teamColor)) {
              moves.add(move);
            }
          }
          ChessPosition right = new ChessPosition(newRow, newCol + 1);
          ChessPosition left = new ChessPosition(newRow, newCol - 1);
          if (board.getPiece(right) == null && board.getPiece(left) == null) {
            return moves;
          } else if (board.getPiece(right) != null && board.getPiece(left) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          } else if (board.getPiece(right) != null) {
            if (board.getPiece(right).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove rightMove = new ChessMove(myPosition, right, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(rightMove);
              }
            }
          } else if (board.getPiece(left) != null) {
            if (board.getPiece(left).getTeamColor() == ChessGame.TeamColor.WHITE) {
              ChessMove leftMove = new ChessMove(myPosition, left, null);
              if (!game.willBeInCheck(move, teamColor)) {
                moves.add(leftMove);
              }
            }
          }
        }
      }
    }

    return moves;
  }

}
