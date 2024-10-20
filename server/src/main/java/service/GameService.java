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
import service.results.ListGamesResult;

import java.util.Collection;

public class GameService {
  public final MemoryAuthDataAccess authDAO;
  public final MemoryGameDataAccess gameDAO;

  public GameService(MemoryAuthDataAccess authDAO, MemoryGameDataAccess gameDAO) {
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
  }
  public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
    String authToken = listGamesRequest.authToken();
    AuthData authData = authDAO.getAuth(authToken);

    Collection<GameData> gameDataList = gameDAO.getGames();
    return new ListGamesResult(gameDataList);
  }

  public int createGame(CreateGameRequest createGameRequest) throws DataAccessException {
    String authToken = createGameRequest.authToken();
    String gameName = createGameRequest.gameName();
    AuthData authData = authDAO.getAuth(authToken);
    return gameDAO.createGame(gameName);
  }

  public void joinGame(JoinGameRequest joinGameRequest) throws DataAccessException{
    String authToken = joinGameRequest.authToken();
    ChessGame.TeamColor playerColor = joinGameRequest.playerColor();
    int gameID = joinGameRequest.gameID();

    AuthData authData = authDAO.getAuth(authToken);
    GameData gameData = gameDAO.getGame(gameID);

    gameDAO.joinGame(gameData, authData.username(), playerColor);
  }
}
