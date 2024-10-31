package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.PrintedGameData;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.ListGamesRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

import java.util.Collection;

public class GameService {
  public final AuthDataAccess authDAO;
  public final GameDataAccess gameDAO;

  public GameService(AuthDataAccess authDAO, GameDataAccess gameDAO) {
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
  }
  public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
    String authToken = listGamesRequest.authToken();
    AuthData authData = authDAO.getAuth(authToken);

    Collection<PrintedGameData> gameDataList = gameDAO.getGames();
    return new ListGamesResult(gameDataList);
  }

  public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException {
    String authToken = createGameRequest.authToken();
    String gameName = createGameRequest.gameName();
    AuthData authData = authDAO.getAuth(authToken);
    int gameID = gameDAO.createGame(gameName);
    return new CreateGameResult(gameID);
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
