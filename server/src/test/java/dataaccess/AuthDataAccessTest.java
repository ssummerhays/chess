package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

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

  }

  @Test
  @Order(4)
  @DisplayName("Negative createAuth MySqlDAO test")
  public void negCreateAuthTest() {

  }

  @Test
  @Order(5)
  @DisplayName("Positive deleteAuth MySqlDAO test")
  public void posDeleteAuthTest() {

  }

  @Test
  @Order(6)
  @DisplayName("Negative deleteAuth MySqlDAO test")
  public void negDeleteAuthTest() {

  }

  @Test
  @Order(7)
  @DisplayName("Positive deleteAllAuthTokens MySqlDAO test")
  public void posDeleteAllAuthTest() {

  }
}