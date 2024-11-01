package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthDataAccessTest {
  static MySqlAuthDataAccess mySqlAuthDAO;

  @BeforeAll
  public static void init() {
    try {
      mySqlAuthDAO = new MySqlAuthDataAccess();
      try (var conn=DatabaseManager.getConnection()) {
        String[] statements={
                "INSERT INTO authData (authToken, username) VALUES (\"testAuth1\", \"testUsername1\");",
                "INSERT INTO authData (authToken, username) VALUES (\"testAuth2\", \"testUsername2\");"
        };
        int counter=1;
        for (var statement : statements) {
          try (var preparedStatement=conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
            counter++;
          } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
              System.out.println("testAuth" + counter + " already Exists");
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
  @DisplayName("Positive getAuth MySqlDAO test")
  public void posGetAuthTest() {
    try {
      AuthData actualAuthData1 = mySqlAuthDAO.getAuth("testAuth1");
      AuthData actualAuthData2 = mySqlAuthDAO.getAuth("testAuth2");
      AuthData expectedAuthData1 = new AuthData("testAuth1", "testUsername1");
      AuthData expectedAuthData2 = new AuthData("testAuth2", "testUsername2");

      assertEquals(expectedAuthData1, actualAuthData1);
      assertEquals(expectedAuthData2, actualAuthData2);
    } catch (DataAccessException e) {
      fail();
    }
  }

  @Test
  @Order(2)
  @DisplayName("Negative getAuth MySqlDAO test")
  public void negGetAuthTest() {
    Exception e = assertThrows(DataAccessException.class, () -> mySqlAuthDAO.getAuth(""));
    assertTrue(e.getMessage().contains("unauthorized"));
  }

  @Test
  @Order(3)
  @DisplayName("Positive createAuth MySqlDAO test")
  public void posCreateAuthTest() {
    try (var conn = DatabaseManager.getConnection()) {
      try (var preparedStatement = conn.prepareStatement("DELETE FROM authData WHERE authToken = 'createdAuth'")) {
        preparedStatement.executeUpdate();
      }

      AuthData authData=new AuthData("createdAuth", "createdUser");
      mySqlAuthDAO.createAuth(authData);

      String statement = "SELECT authToken, username FROM authData WHERE authToken='createdAuth'";
      try (var preparedStatement = conn.prepareStatement(statement)) {
        var rs = preparedStatement.executeQuery();
        rs.next();
        String authToken = rs.getString(1);
        String username = rs.getString(2);

        assertEquals("createdUser", username);
        assertEquals("createdAuth", authToken);
      }
    } catch (Throwable e) {
      fail();
    }
  }

  @Test
  @Order(4)
  @DisplayName("Negative createAuth MySqlDAO test")
  public void negCreateAuthTest() {
    AuthData authData = new AuthData(null, "nullAuthToken");
    Exception e = assertThrows(DataAccessException.class, () -> mySqlAuthDAO.createAuth(authData));
    assertTrue(e.getMessage().contains("bad request"));
  }

  @Test
  @Order(5)
  @DisplayName("Positive deleteAuth MySqlDAO test")
  public void posDeleteAuthTest() {
    try (var conn = DatabaseManager.getConnection()) {
      AuthData authData = new AuthData("testAuth1", "testUsername1");
      int countAfter;
      int countBefore;

      String statement1 = "SELECT COUNT(*) FROM authData";
      try (var preparedStatement = conn.prepareStatement(statement1)) {
        var rs = preparedStatement.executeQuery();
        rs.next();
        countBefore = rs.getInt(1);
      }

      mySqlAuthDAO.deleteAuth(authData);
      String statement2 = "SELECT COUNT(*) FROM authData";
      try (var preparedStatement = conn.prepareStatement(statement2)) {
        var rs = preparedStatement.executeQuery();
        rs.next();
        countAfter = rs.getInt(1);
      }
      assertEquals(countBefore - 1, countAfter);
    } catch (Throwable e) {
      fail();
    }

  }

  @Test
  @Order(7)
  @DisplayName("Positive deleteAllAuthTokens MySqlDAO test")
  public void posDeleteAllAuthTest() {
    try (var conn = DatabaseManager.getConnection()) {
      mySqlAuthDAO.deleteAllAuthTokens();
      String statement = "SELECT COUNT(*) FROM authData";
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