package service.requests;

import chess.ChessGame;

public record LeaveGameRequest(String authToken, ChessGame.TeamColor playerColor, int gameID) {
}
