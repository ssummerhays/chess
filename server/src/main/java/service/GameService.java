package service;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.requests.*;
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

    Collection<GameData> gameDataList = gameDAO.getGames();
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

  public void leaveGame(LeaveGameRequest leaveGameRequest) throws DataAccessException{
    String authToken = leaveGameRequest.authToken();
    ChessGame.TeamColor playerColor = leaveGameRequest.playerColor();
    int gameID = leaveGameRequest.gameID();

    AuthData authData = authDAO.getAuth(authToken);
    GameData gameData = gameDAO.getGame(gameID);

    gameDAO.leaveGame(gameData, authData.username(), playerColor);
  }

  public void updateGame(UpdateGameRequest updateGameRequest) throws DataAccessException{
    String authToken = updateGameRequest.authToken();
    AuthData authData = authDAO.getAuth(authToken);

    String jsonGameData = updateGameRequest.jsonGameData();
    GameData gameData = new Gson().fromJson(jsonGameData, GameData.class);

    gameDAO.updateGame(gameData);
  }
}
