package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.PrintedGameData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class GameDataAccessTest {
  static MySqlGameDataAccess mySqlGameDAO;
  static ChessGame game1 = new ChessGame();
  static ChessGame game2 = new ChessGame();
  GameData gameData1 = new GameData(1, "whiteUser", "blackUser", "game1", game1);
  GameData gameData2 = new GameData(2, null, null, "game2", game2);
  PrintedGameData printedGameData1 = new PrintedGameData(1, "whiteUser", "blackUser", "game1");
  PrintedGameData printedGameData2 = new PrintedGameData(2, null, null, "game2");
  @BeforeAll
  public static void init() {
    try {
      mySqlGameDAO = new MySqlGameDataAccess();
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
      } catch (SQLException e) {
        fail(e.getMessage());
      }
    } catch (DataAccessException e) {
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

  }

  @Test
  @Order(6)
  @DisplayName("Negative createGame test")
  public void negCreateGameTest() {

  }

  @Test
  @Order(7)
  @DisplayName("Positive joinGame test")
  public void posJoinGameTest() {

  }

  @Test
  @Order(8)
  @DisplayName("Negative joinGame test")
  public void negJoinGameTest() {

  }

  @Test
  @Order(9)
  @DisplayName("Positive deleteAllGames test")
  public void posDeleteAllGamesTest() {

  }
}