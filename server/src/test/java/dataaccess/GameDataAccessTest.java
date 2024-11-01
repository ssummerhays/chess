package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.PrintedGameData;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameDataAccessTest {
  static MySqlGameDataAccess mySqlGameDAO;
  static ChessGame game1 = new ChessGame();
  static ChessGame game2 = new ChessGame();
  GameData gameData1 = new GameData(1, "whiteUser", "blackUser", "game1", game1);
  GameData gameData2 = new GameData(2, null, null, "game2", game2);
  PrintedGameData printedGameData1 = new PrintedGameData(1, "whiteUser", "blackUser", "game1");
  PrintedGameData printedGameData2 = new PrintedGameData(2, null, null, "game2");
  @BeforeAll
  public static void init() throws DataAccessException {
    mySqlGameDAO = new MySqlGameDataAccess();
    mySqlGameDAO.deleteAllGames();
    try (var conn=DatabaseManager.getConnection()) {

      String gameJSON1 = new Gson().toJson(game1);
      String gameJSON2 = new Gson().toJson(game2);
      String[] statements={
              "INSERT INTO gameData (gameID, whiteUsername, blackUsername, gameName, gameJSON) VALUES (1, 'whiteUser', 'blackUser', 'game1', ?);",
              "INSERT INTO gameData (gameID, whiteUsername, blackUsername, gameName, gameJSON) VALUES (2, NULL, NULL, 'game2', ?);"
      };
      int counter=1;
      for (var statement : statements) {
        try (var preparedStatement=conn.prepareStatement(statement)) {
          if (counter == 1) {
            preparedStatement.setString(1, gameJSON1);
          } else {
            preparedStatement.setString(1, gameJSON2);
          }
          preparedStatement.executeUpdate();
          counter++;
        } catch (SQLException e) {
          if (e.getMessage().contains("Duplicate")) {
            System.out.println("game" + counter + " already Exists");
            counter++;
          } else {
            fail(e.getMessage());
          }
        }
      }
      mySqlGameDAO.gameID = 3;
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }

  @Test
  @Order(1)
  @DisplayName("Positive getGames test")
  public void posGetGamesTest() {
    try {
      Collection<PrintedGameData> expected = new HashSet<>();
      expected.add(printedGameData1);
      expected.add(printedGameData2);
      Collection<PrintedGameData> actual = mySqlGameDAO.getGames();
      assertEquals(expected, actual);
    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @Order(3)
  @DisplayName("Positive getGame test")
  public void posGetGameTest() {
    try {
      GameData actualGameData1 = mySqlGameDAO.getGame(1);
      GameData actualGameData2 = mySqlGameDAO.getGame(2);

      assertEquals(gameData1, actualGameData1);
      assertEquals(gameData2, actualGameData2);
    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @Order(4)
  @DisplayName("Negative getGame test")
  public void negGetGameTest() {
    Exception e = assertThrows(DataAccessException.class, () -> mySqlGameDAO.getGame(0));
    assertTrue(e.getMessage().contains("unauthorized"));
  }

  @Test
  @Order(5)
  @DisplayName("Positive createGame test")
  public void posCreateGameTest() {
    try (var conn = DatabaseManager.getConnection()) {
      try (var preparedStatement = conn.prepareStatement("DELETE FROM gameData WHERE gameName = 'createdGame'")) {
        preparedStatement.executeUpdate();
      }

      mySqlGameDAO.createGame("createdGame");

      String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameJSON FROM gameData WHERE gameName='createdGame'";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        var rs = preparedStatement.executeQuery();
        rs.next();
        int gameID = rs.getInt(1);
        String whiteUsername = rs.getString(2);
        String blackUsername = rs.getString(3);
        String gameName = rs.getString(4);
        String gameJSON = rs.getString(5);
        ChessGame game = new Gson().fromJson(gameJSON, ChessGame.class);

        assertEquals(mySqlGameDAO.gameID - 1, gameID);
        assertNull(whiteUsername);
        assertNull(blackUsername);
        assertEquals("createdGame", gameName);
        ChessGame expectedGame = new ChessGame();
        assertEquals(expectedGame, game);
      }
    } catch (Throwable e) {
      fail();
    }

  }

  @Test
  @Order(6)
  @DisplayName("Negative createGame test")
  public void negCreateGameTest() {
    Exception e = assertThrows(DataAccessException.class, () -> mySqlGameDAO.createGame("game1"));
    assertTrue(e.getMessage().contains("already taken"));
  }

  @Test
  @Order(7)
  @DisplayName("Positive joinGame test")
  public void posJoinGameTest() {
    try (var conn = DatabaseManager.getConnection()) {
      mySqlGameDAO.joinGame(gameData2, "newUser", ChessGame.TeamColor.WHITE);
      String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameJSON FROM gameData WHERE gameID = ?";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setInt(1, gameData2.gameID());
        var rs = preparedStatement.executeQuery();
        rs.next();
        int gameID = rs.getInt(1);
        String whiteUsername = rs.getString(2);
        String blackUsername = rs.getString(3);
        String gameName = rs.getString(4);
        String gameJSON = rs.getString(5);
        ChessGame game = new Gson().fromJson(gameJSON, ChessGame.class);

        assertEquals(gameData2.gameID(), gameID);
        assertEquals("newUser", whiteUsername);
        assertEquals(gameData2.blackUsername(), blackUsername);
        assertEquals(gameData2.gameName(), gameName);
        assertEquals(gameData2.game(), game);
      }
    } catch (Throwable e) {
      fail(e.getMessage());
    }
  }

  @Test
  @Order(8)
  @DisplayName("Negative joinGame test")
  public void negJoinGameTest() {
    Exception e = assertThrows(DataAccessException.class, () -> mySqlGameDAO.joinGame(gameData1, "newUser", ChessGame.TeamColor.WHITE));
    assertTrue(e.getMessage().contains("already taken"));
  }

  @Test
  @Order(9)
  @DisplayName("Positive deleteAllGames test")
  public void posDeleteAllGamesTest() {
    try (var conn = DatabaseManager.getConnection()) {
      mySqlGameDAO.deleteAllGames();
      String statement = "SELECT COUNT(*) FROM gameData";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        var rs = preparedStatement.executeQuery();
        rs.next();
        int count = rs.getInt(1);

        assertEquals(0, count);
      }
    } catch (Throwable e) {
      fail();
    }

  }
}