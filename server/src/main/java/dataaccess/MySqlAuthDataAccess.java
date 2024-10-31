package dataaccess;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class MySqlAuthDataAccess implements AuthDataAccess {

  public MySqlAuthDataAccess() throws DataAccessException {
    DatabaseManager.createDatabase();
    try (var conn = DatabaseManager.getConnection()) {
      String[] createStatements={
              """
            CREATE TABLE IF NOT EXISTS  authData (
              `authToken` varchar(256) NOT NULL UNIQUE,
              `username` varchar(256) NOT NULL UNIQUE,
              PRIMARY KEY (`authToken`)
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
  public AuthData getAuth(String authToken) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "SELECT authToken, username FROM authData WHERE authToken=?";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setString(1, authToken);
        var rs = preparedStatement.executeQuery();
        rs.next();
        String username = rs.getString(2);
        return new AuthData(authToken, username);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error: unauthorized");
    }
  }

  public void createAuth(AuthData authData) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "INSERT INTO authData (authData, username) VALUES (?, ?)";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setString(1, authData.authToken());
        preparedStatement.setString(2, authData.username());
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error: bad request");
    }
  }

  public void deleteAuth(AuthData authData) {

  }

  public void deleteAllAuthTokens() {

  }
}
