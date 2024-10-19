package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
  UserData getUser(String username);

  void createUser(UserData userData);

  void deleteAllUsers();

  AuthData getAuth(String authToken);

  void createAuth(AuthData authData);

  void deleteAuth(AuthData authData);

  void deleteAllAuthTokens();

  Collection<GameData> getGames();

  GameData getGame(int gameID);

  int createGame(String gameName);

  void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor);

  void deleteAllGames();
}