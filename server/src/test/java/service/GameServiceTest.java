package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDataAccess;
import dataaccess.MemoryGameDataAccess;
import dataaccess.MemoryUserDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.ListGamesRequest;
import service.results.CreateGameResult;
import service.results.ListGamesResult;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
  private static GameService service;
  private static MemoryAuthDataAccess authDAO;
  private static MemoryGameDataAccess gameDAO;
  private static final String LOGGED_IN_USERNAME = "user1";
  private static final String LOGGED_OUT_USERNAME = "user2";
  private static final String PASSWORD = "correctPassword";
  private static final String EMAIL = "user@example.com";
  private static AuthData user1Auth;
  private static GameData existingGame1 = new GameData(1, null, null, "game1", new ChessGame());
  private static GameData existingGame2 = new GameData(2, null, null, "game2", new ChessGame());
  private static GameData fullGame = new GameData(3, LOGGED_IN_USERNAME, LOGGED_OUT_USERNAME, "fullGame", new ChessGame());

  @BeforeAll
  public static void init() {
    MemoryUserDataAccess userDAO = new MemoryUserDataAccess();
    authDAO = new MemoryAuthDataAccess();

    UserData loggedInUser = new UserData(LOGGED_IN_USERNAME, PASSWORD, EMAIL);
    UserData loggedOutUser = new UserData(LOGGED_OUT_USERNAME, PASSWORD, EMAIL);
    userDAO.userDataList.add(loggedInUser);
    userDAO.userDataList.add(loggedOutUser);

    user1Auth = new AuthData("auth1", LOGGED_IN_USERNAME);
    authDAO.authDataList.add(user1Auth);

    existingGame1 = new GameData(1, null, null, "game1", new ChessGame());
    existingGame2 = new GameData(2, null, null, "game2", new ChessGame());
    fullGame = new GameData(3, LOGGED_IN_USERNAME, LOGGED_OUT_USERNAME, "fullGame", new ChessGame());
  }

  @BeforeEach
  public void setUp() {
    gameDAO = new MemoryGameDataAccess();
    service = new GameService(authDAO, gameDAO);

    gameDAO.gameDataList.add(existingGame1);
    gameDAO.gameDataList.add(existingGame2);
    gameDAO.gameDataList.add(fullGame);
    gameDAO.nextGameID = 4;
  }
  @Test
  @DisplayName("Successfully list games")
  public void listGamesSuccess() {
    ListGamesRequest listGamesRequest = new ListGamesRequest(user1Auth.authToken());
    try {
      ListGamesResult listGamesResult = service.listGames(listGamesRequest);
      assertEquals(3, listGamesResult.games().size());
    } catch (DataAccessException e) {
      fail();
    }

  }

  @Test
  @DisplayName("Unauthorized list games")
  public void listGamesFail() {
    ListGamesRequest listGamesRequest = new ListGamesRequest(null);
    DataAccessException exception = assertThrows(DataAccessException.class, () -> service.listGames(listGamesRequest));
    assertEquals("Error: unauthorized", exception.getMessage());
  }

  @Test
  @DisplayName("Successfully create new game")
  public void createGameSuccess() {
    CreateGameRequest createGameRequest1 = new CreateGameRequest(user1Auth.authToken(), "newGame1");
    CreateGameRequest createGameRequest2 = new CreateGameRequest(user1Auth.authToken(), "newGame2");
    try {
      CreateGameResult createGameResult1 = service.createGame(createGameRequest1);
      assertEquals(4, gameDAO.gameDataList.size());
      assertEquals(4, createGameResult1.gameID());

      CreateGameResult createGameResult2 = service.createGame(createGameRequest2);
      assertEquals(5, gameDAO.gameDataList.size());
      assertEquals(5, createGameResult2.gameID());
    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @DisplayName("Unsuccessfully create game with unauthorized authToken")
  public void createGameFail() {
    CreateGameRequest createGameRequest = new CreateGameRequest("unauthorized", "gameName");
    DataAccessException exception = assertThrows(DataAccessException.class, () -> service.createGame(createGameRequest));
    assertEquals(3, gameDAO.gameDataList.size());
    assertEquals("Error: unauthorized", exception.getMessage());
  }

  @Test
  @DisplayName("Successfully join game")
  public void joinGameSuccess() {
    JoinGameRequest joinGameRequest1 = new JoinGameRequest(user1Auth.authToken(), ChessGame.TeamColor.WHITE, 1);
    JoinGameRequest joinGameRequest2 = new JoinGameRequest(user1Auth.authToken(), ChessGame.TeamColor.BLACK, 1);
    GameData expectedGameData1 = new GameData(1, user1Auth.username(), null, existingGame1.gameName(), existingGame1.game());
    GameData expectedGameData2 = new GameData(1, user1Auth.username(), user1Auth.username(), existingGame1.gameName(), existingGame1.game());
    try {
      service.joinGame(joinGameRequest1);
      assertTrue(gameDAO.gameDataList.contains(expectedGameData1));
      assertEquals(3, gameDAO.gameDataList.size());

      service.joinGame(joinGameRequest2);
      assertTrue(gameDAO.gameDataList.contains(expectedGameData2));
      assertEquals(3, gameDAO.gameDataList.size());
    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @DisplayName("Unsuccessfully join full game")
  public void joinGameFail() {
    JoinGameRequest joinGameRequest1 = new JoinGameRequest(user1Auth.authToken(), ChessGame.TeamColor.WHITE, 3);
    DataAccessException exception1 = assertThrows(DataAccessException.class, () -> service.joinGame(joinGameRequest1));
    assertEquals("Error: already taken", exception1.getMessage());

    JoinGameRequest joinGameRequest2 = new JoinGameRequest(user1Auth.authToken(), ChessGame.TeamColor.BLACK, 3);
    DataAccessException exception2 = assertThrows(DataAccessException.class, () -> service.joinGame(joinGameRequest2));
    assertEquals("Error: already taken", exception2.getMessage());

    assertTrue(gameDAO.gameDataList.contains(fullGame));
    assertEquals(3, gameDAO.gameDataList.size());
  }
}