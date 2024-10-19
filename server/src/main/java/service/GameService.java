package service;

import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
  public Collection<GameData> listGames(AuthData authData) {}

  public GameData createGame(AuthData authData, GameData gameData) {}

  public void joinGame(AuthData authData, GameData gameData) {}
}
