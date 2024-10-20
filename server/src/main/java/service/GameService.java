package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import model.AuthData;
import model.GameData;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.ListGamesRequest;

import java.util.Collection;

public class GameService {
  public Collection<GameData> listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
    String authToken = listGamesRequest.authToken();
    MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();
    AuthData authData = authDAO.getAuth(authToken);

    MemoryGameDataAccess gameDAO = new MemoryGameDataAccess();
    return gameDAO.getGames();
  }

  public int createGame(CreateGameRequest createGameRequest) throws DataAccessException {
    String authToken = createGameRequest.authToken();
    String gameName = createGameRequest.gameName();

    MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();
    AuthData authData = authDAO.getAuth(authToken);

    MemoryGameDataAccess gameDAO = new MemoryGameDataAccess();
    return gameDAO.createGame(gameName);
  }

  public void joinGame(JoinGameRequest joinGameRequest) throws DataAccessException{
    String authToken = joinGameRequest.authToken();
    ChessGame.TeamColor playerColor = joinGameRequest.playerColor();
    int gameID = joinGameRequest.gameID();

    MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();
    AuthData authData = authDAO.getAuth(authToken);

    MemoryGameDataAccess gameDAO = new MemoryGameDataAccess();
    GameData gameData = gameDAO.getGame(gameID);

    gameDAO.joinGame(gameData, authData.username(), playerColor);
  }
}
