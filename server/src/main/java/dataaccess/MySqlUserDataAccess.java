package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class MySqlUserDataAccess implements UserDataAccess {
  public UserData getUser(String username) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "SELECT username, password, email FROM userData WHERE username=?";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setString(1, username);
        var rs = preparedStatement.executeQuery();
        rs.next();
        String hashed = rs.getString(2);
        String email = rs.getString(3);
        return new UserData(username, hashed, email);
      }
    } catch (SQLException e) {
      throw new DataAccessException("Database Error");
    }
  }

  public void createUser(UserData userData) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "INSERT INTO userData (username, password, email) VALUES (?, ?, ?)";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.setString(1, userData.username());
        String hashed = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
        preparedStatement.setString(2, hashed);
        preparedStatement.setString(3, userData.email());
        preparedStatement.executeQuery();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Database Error");
    }

  }

  public void deleteAllUsers() throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      String statement = "TRUNCATE userData";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        preparedStatement.executeQuery();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Database Error");
    }
  }
}
