package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class MySqlUserDataAccess implements UserDataAccess {
  public UserData getUser(String username) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection();) {
      String statement = "SELECT username, password, email FROM chess WHERE username=?";
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

  }

  public void deleteAllUsers() {

  }
}
