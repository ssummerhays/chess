package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.PrintedGameData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public class MySqlGameDataAccess implements GameDataAccess {

  public MySqlGameDataAccess() throws DataAccessException{
    DatabaseManager.createDatabase();
    try (var conn = DatabaseManager.getConnection()) {
      String[] createStatements={
              """
            CREATE TABLE IF NOT EXISTS  gameData (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256) NOT NULL UNIQUE,
              `gameJSON` varchar(2048) NOT NULL,
              PRIMARY KEY (`gameID`)
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
      String statement = "INSERT INTO gameData (whiteUsername, blackUsername, gameName, gameJSON) VALUES (NULL, NULL, ?, ?)";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setString(1, gameName);
        ChessGame newGame = new ChessGame();
        String jsonGame = new Gson().toJson(newGame);
        preparedStatement.setString(2, jsonGame);
        preparedStatement.executeUpdate();
        int gameID;
        try (var nextPreparedStatement = conn.prepareStatement("SELECT gameID FROM gameData WHERE gameName=?")) {
          nextPreparedStatement.setString(1, gameName);
          var rs = nextPreparedStatement.executeQuery();
          rs.next();
          gameID = rs.getInt(1);
        }
        return gameID;
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
      String whiteUsername;
      String blackUsername;
      String selectStatement = "SELECT whiteUsername, blackUsername FROM gameData WHERE gameID = ?";
      try (var preparedSelectStatement = conn.prepareStatement(selectStatement)) {
        preparedSelectStatement.setInt(1, gameData.gameID());
        var rs = preparedSelectStatement.executeQuery();
        rs.next();
        whiteUsername = rs.getString(1);
        blackUsername = rs.getString(2);
      }

      String statement = teamColor == ChessGame.TeamColor.WHITE?
              "UPDATE gameData SET whiteUsername = ? WHERE gameID = ?" : "UPDATE gameData SET blackUsername = ? WHERE gameID = ?";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setString(1, username);
        preparedStatement.setInt(2, gameData.gameID());
        if (whiteUsername == null && teamColor == ChessGame.TeamColor.WHITE) {
          preparedStatement.executeUpdate();
        } else if (blackUsername == null && teamColor == ChessGame.TeamColor.BLACK) {
          preparedStatement.executeUpdate();
        } else {
          throw new DataAccessException("Error: already taken");
        }
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
