package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
  UserData getUser(String username) throws DataAccessException;

  void createUser(UserData userData) throws DataAccessException;

  void deleteAllUsers();

  AuthData getAuth(String authToken) throws DataAccessException;

  void createAuth(AuthData authData) throws DataAccessException;

  void deleteAuth(AuthData authData);

  void deleteAllAuthTokens();

  Collection<GameData> getGames() throws DataAccessException;

  GameData getGame(int gameID) throws DataAccessException;

  int createGame(String gameName)throws DataAccessException;

  void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException;

  void deleteAllGames() throws DataAccessException;
}