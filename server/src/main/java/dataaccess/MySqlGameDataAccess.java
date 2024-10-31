package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.PrintedGameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class MySqlGameDataAccess implements GameDataAccess {
  int gameID = 1;

  public MySqlGameDataAccess() throws DataAccessException{
    DatabaseManager.createDatabase();
    try (var conn = DatabaseManager.getConnection()) {
      String[] createStatements={
              """
            CREATE TABLE IF NOT EXISTS  gameData (
              `gameID` int NOT NULL,
              `whiteUsername` varchar(256) UNIQUE,
              `blackUsername` varchar(256) UNIQUE,
              `gameName` varchar(256) NOT NULL UNIQUE,
              `gameJSON` varchar(256) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
      };
      for (var statement : createStatements) {
        try (var preparedStatement = conn.prepareStatement(statement)) {
          preparedStatement.executeUpdate();
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Unable to configure database");
    }
  }
  public Collection<PrintedGameData> getGames() throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "SELECT * FROM gameData";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        Collection<PrintedGameData> printedGameDataList = new HashSet<>();
        var rs = preparedStatement.executeQuery();
        while (rs.next()) {
          int gameID = rs.getInt(1);
          String whiteUsername = rs.getString(2);
          String blackUserName = rs.getString(3);
          String gameName = rs.getString(4);
          PrintedGameData printedGameData = new PrintedGameData(gameID, whiteUsername, blackUserName, gameName);
          printedGameDataList.add(printedGameData);
        }
        return printedGameDataList;
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error: unauthorized");
    }
  }

  public GameData getGame(int gameID) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameJSON FROM gameData WHERE gameID=?";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setInt(1, gameID);
        var rs = preparedStatement.executeQuery();
        rs.next();
        String whiteUsername = rs.getString(2);
        String blackUserName = rs.getString(3);
        String gameName = rs.getString(4);
        String gameJSON = rs.getString(5);
        ChessGame game = new Gson().fromJson(gameJSON, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUserName, gameName, game);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error: unauthorized");
    }
  }

  public int createGame(String gameName) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "INSERT INTO gameData (gameID, whiteUsername, blackUsername, gameName, gameJSON) VALUES (?, NULL, NULL, ?, ?)";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setInt(1, gameID);
        int prevGameID = gameID;
        gameID++;
        preparedStatement.setString(2, gameName);
        ChessGame newGame = new ChessGame();
        String jsonGame = new Gson().toJson(newGame);
        preparedStatement.setString(1, jsonGame);
        preparedStatement.executeUpdate();
        return prevGameID;
      }
    } catch (SQLException e) {
      if (e.getMessage().contains("Duplicate")) {
        throw new DataAccessException("Error: already taken");
      } else {
        throw new DataAccessException("Error: bad request");
      }
    }
  }
  
  public void joinGame(GameData gameData, String username, ChessGame.TeamColor teamColor) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = teamColor == ChessGame.TeamColor.WHITE?
              "UPDATE gameData SET whiteUsername = ? WHERE gameID = ?" : "UPDATE gameData SET blackUsername = ? WHERE gameID = ?";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setString(1, username);
        preparedStatement.setInt(2, gameData.gameID());
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      if (e.getMessage().contains("Duplicate")) {
        throw new DataAccessException("Error: already taken");
      } else {
        throw new DataAccessException("Error: bad request");
      }
    }
  }

  public void deleteAllGames() throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "TRUNCATE gameData";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Database Error");
    }
  }
}
