package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import model.PrintedGameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;

import ui.ResponseException;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;


public class ServerFacadeTests {

    private static UserData existingUser;
    private static UserData newUser;
    private static String createRequest;
    private String existingAuth;
    private static Server server;
    static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port;
        serverFacade = new ServerFacade(url);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() throws ResponseException {
        serverFacade.clear();

        JsonObject regResult = serverFacade.register(existingUser);
        existingAuth = regResult.get("authToken").getAsString();

        createRequest = "testGame";
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Normal User Login")
    public void successLogin() throws ResponseException {
        JsonObject loginResult = serverFacade.login(existingUser.username(), existingUser.password());

        assertHttpOk(serverFacade.getStatusCode());

        Assertions.assertEquals(existingUser.username(), loginResult.get("username").getAsString(),
                "Response did not give the same username as user");
        Assertions.assertNotNull(loginResult.get("authToken").getAsString(), "Response did not return authentication String");
    }

    @Test
    @Order(2)
    @DisplayName("Login Invalid User")
    public void loginInvalidUser() {
        try {
            serverFacade.login(newUser.username(), newUser.password());
            Assertions.fail();
        } catch (ResponseException e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.statusCode(),
                    "Server response code was not 401");
            Assertions.assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Normal User Registration")
    public void successRegister() throws ResponseException {
        JsonObject registerResult = serverFacade.register(newUser);

        assertHttpOk(serverFacade.getStatusCode());

        Assertions.assertEquals(newUser.username(), registerResult.get("username").getAsString(),
                "Response did not have the same username as was registered");
        Assertions.assertNotNull(registerResult.get("authToken").getAsString(), "Response did not contain an authentication string");
    }

    @Test
    @Order(4)
    @DisplayName("Re-Register User")
    public void registerTwice() {
        try {
            serverFacade.register(existingUser);
            Assertions.fail();
        } catch (ResponseException e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.statusCode());
            Assertions.assertTrue(e.getMessage().contains("already taken"));
        }
    }

    @Test
    @Order(5)
    @DisplayName("Register Bad Request")
    public void failRegister() {
        try {
            UserData badData=new UserData(existingUser.username(), null, existingUser.email());
            serverFacade.register(badData);
            Assertions.fail();
        } catch (ResponseException e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.statusCode());
            Assertions.assertTrue(e.getMessage().contains("bad request"));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void successLogout() {
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(existingAuth));
        assertHttpOk(serverFacade.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("Invalid Auth Logout")
    public void failLogout() {
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(existingAuth));

        assertHttpOk(serverFacade.getStatusCode());

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.logout(existingAuth));

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.statusCode());
        Assertions.assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @Order(8)
    @DisplayName("Valid Creation")
    public void goodCreate() throws ResponseException {
        JsonObject createResult = serverFacade.createGame(existingAuth, createRequest);

        assertHttpOk(serverFacade.getStatusCode());
        Assertions.assertTrue(createResult.get("gameID").getAsInt() > 0, "Result returned invalid game ID");
    }

    @Test
    @Order(9)
    @DisplayName("Create with Bad Authentication")
    public void badAuthCreate() {
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(existingAuth));

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.createGame(existingAuth, createRequest));

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.statusCode());
        Assertions.assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @Order(10)
    @DisplayName("Join Created Game")
    public void goodJoin() {
        Assertions.assertDoesNotThrow(() -> {
            JsonObject createResult=serverFacade.createGame(existingAuth, createRequest);

            serverFacade.joinGame(existingAuth, "WHITE", createResult.get("gameID").getAsInt());

            assertHttpOk(serverFacade.getStatusCode());

            JsonObject listResult = serverFacade.listGames(existingAuth);

            boolean containsExpectedGameData = false;
            Type collectionType = new TypeToken<Collection<PrintedGameData>>(){}.getType();

            Collection<PrintedGameData> gameList = new Gson().fromJson(listResult.get("games"), collectionType);

            for (var game : gameList) {
                if (Objects.equals(game.whiteUsername(), existingUser.username()) && game.blackUsername() == null) {
                    containsExpectedGameData = true;
                    break;
                }
            }

            Assertions.assertTrue(containsExpectedGameData);
        });
    }

    @Test
    @Order(11)
    @DisplayName("Join Bad Authentication")
    public void badAuthJoin() throws ResponseException {
        JsonObject createResult = serverFacade.createGame(existingAuth, createRequest);

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame(existingAuth + "badStuff", "WHITE", createResult.get("gameID").getAsInt());
        });

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.statusCode());
        Assertions.assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @Order(11)
    @DisplayName("Join Bad Team Color")
    public void badColorJoin() throws ResponseException {
        JsonObject createResult = serverFacade.createGame(existingAuth, createRequest);

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame(existingAuth, null, createResult.get("gameID").getAsInt());
        });

        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.statusCode());
        Assertions.assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @Order(11)
    @DisplayName("Join Bad Game ID")
    public void badGameIDJoin() throws ResponseException {
        serverFacade.createGame(existingAuth, "Bad Join");

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame(existingAuth, "white", -1);
        });

        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.statusCode());
        Assertions.assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @Order(11)
    @DisplayName("Join Steal Team Color")
    public void stealColorJoin() throws ResponseException {
        JsonObject createResult = serverFacade.createGame(existingAuth, createRequest);

        serverFacade.joinGame(existingAuth, "BLACK", createResult.get("gameID").getAsInt());

        JsonObject registerResult = serverFacade.register(newUser);

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame(registerResult.get("authToken").getAsString(), "BLACK", createResult.get("gameID").getAsInt());
        });

        Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.statusCode());
        Assertions.assertTrue(e.getMessage().contains("already taken"));
    }

    @Test
    @Order(12)
    @DisplayName("List No Games")
    public void noGamesList() {
        Assertions.assertDoesNotThrow(() -> {
            JsonObject result = serverFacade.listGames(existingAuth);

            assertHttpOk(serverFacade.getStatusCode());

            Type collectionType = new TypeToken<Collection<PrintedGameData>>(){}.getType();

            Collection<PrintedGameData> gameList = new Gson().fromJson(result.get("games"), collectionType);
          Assertions.assertEquals(0, gameList.size(), "Found games when none should be there");
        });
    }

    @Test
    @Order(12)
    @DisplayName("List Multiple Games")
    public void gamesList() throws ResponseException {
        UserData userA = new UserData("a", "A", "a.A");
        UserData userB = new UserData("b", "B", "b.B");
        UserData userC = new UserData("c", "C", "c.C");

        JsonObject authA = serverFacade.register(userA);
        JsonObject authB = serverFacade.register(userB);
        JsonObject authC = serverFacade.register(userC);

        String authAToken = authA.get("authToken").getAsString();
        String authBToken = authB.get("authToken").getAsString();
        String authCToken = authC.get("authToken").getAsString();

        String authAUsername = authA.get("username").getAsString();
        String authBUsername = authB.get("username").getAsString();
        String authCUsername = authC.get("username").getAsString();

        Collection<PrintedGameData> expectedList = new HashSet<>();

        String game1Name = "I'm numbah one!";
        JsonObject game1 = serverFacade.createGame(authAToken, game1Name);
        serverFacade.joinGame(authAToken, "BLACK", game1.get("gameID").getAsInt());
        expectedList.add(new PrintedGameData(game1.get("gameID").getAsInt(), null, authAUsername, game1Name));


        String game2Name = "Lonely";
        JsonObject game2 = serverFacade.createGame(authBToken, game2Name);
        serverFacade.joinGame(authBToken, "WHITE", game2.get("gameID").getAsInt());
        expectedList.add(new PrintedGameData(game2.get("gameID").getAsInt(), authBUsername, null, game2Name));


        String game3Name = "GG";
        JsonObject game3 = serverFacade.createGame(authCToken, game3Name);
        int game3ID = game3.get("gameID").getAsInt();
        serverFacade.joinGame(authCToken, "WHITE", game3ID);
        serverFacade.joinGame(authAToken, "BLACK", game3ID);
        expectedList.add(new PrintedGameData(game3ID, authCUsername, authAUsername, game3Name));


        String game4Name = "All by myself";
        JsonObject game4 = serverFacade.createGame(authCToken, game4Name);
        int game4ID = game4.get("gameID").getAsInt();
        serverFacade.joinGame(authCToken, "WHITE", game4ID);
        serverFacade.joinGame(authCToken, "BLACK", game4ID);
        expectedList.add(new PrintedGameData(game4ID, authCUsername, authCUsername, game4Name));


        JsonObject listResult = serverFacade.listGames(existingAuth);
        assertHttpOk(serverFacade.getStatusCode());
        Type collectionType = new TypeToken<Collection<PrintedGameData>>(){}.getType();

        Collection<PrintedGameData> returnedList = new Gson().fromJson(listResult.get("games"), collectionType);

        for (PrintedGameData data : returnedList) {
            boolean inList = false;
            for (PrintedGameData actualData : expectedList) {
                if (data.equals(actualData)) {
                    inList = true;
                    break;
                }
            }
            Assertions.assertTrue(inList, "Returned Games list was incorrect");
        }
    }

    @Test
    @Order(13)
    @DisplayName("Unique Authtoken Each Login")
    public void uniqueAuthorizationTokens() {
        Assertions.assertDoesNotThrow(() -> {
            JsonObject loginOne = serverFacade.login(existingUser.username(), existingUser.password());
            assertHttpOk(serverFacade.getStatusCode());

            Assertions.assertNotNull(loginOne.get("authToken").getAsString(),
                    "Login result did not contain an authToken");

            JsonObject loginTwo = serverFacade.login(existingUser.username(), existingUser.password());
            assertHttpOk(serverFacade.getStatusCode());

            Assertions.assertNotNull(loginTwo.get("authToken").getAsString(),
                    "Login result did not contain an authToken");
            Assertions.assertNotEquals(existingAuth, loginOne.get("authToken").getAsString(),
                    "Authtoken returned by login matched authtoken from prior register");
            Assertions.assertNotEquals(existingAuth, loginTwo.get("authToken").getAsString(),
                    "Authtoken returned by login matched authtoken from prior register");
            Assertions.assertNotEquals(loginOne.get("authToken").getAsString(), loginTwo.get("authToken").getAsString(),
                    "Authtoken returned by login matched authtoken from prior login");


            JsonObject createResult = serverFacade.createGame(existingAuth, createRequest);
            assertHttpOk(serverFacade.getStatusCode());


            serverFacade.logout(existingAuth);
            assertHttpOk(serverFacade.getStatusCode());


            serverFacade.joinGame(loginOne.get("authToken").getAsString(), "WHITE", createResult.get("gameID").getAsInt());
            assertHttpOk(serverFacade.getStatusCode());


            JsonObject listResult = serverFacade.listGames(loginTwo.get("authToken").getAsString());
            assertHttpOk(serverFacade.getStatusCode());

            Type collectionType = new TypeToken<Collection<PrintedGameData>>(){}.getType();

            Collection<PrintedGameData> listedGames = new Gson().fromJson(listResult.get("games"), collectionType);

            Assertions.assertEquals(1, listedGames.size());
            Assertions.assertEquals(existingUser.username(), listedGames.iterator().next().whiteUsername());
        });
    }

    @Test
    @Order(14)
    @DisplayName("Clear Test")
    public void clearData() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.createGame(existingAuth, "Mediocre game");
            serverFacade.createGame(existingAuth, "Awesome game");

            UserData user = new UserData("ClearMe", "cleared", "clear@mail.com");
            JsonObject registerResult = serverFacade.register(user);

            JsonObject createResult = serverFacade.createGame(registerResult.get("authToken").getAsString(), "Clear game");

            serverFacade.joinGame(registerResult.get("authToken").getAsString(), "WHITE", createResult.get("gameID").getAsInt());

            serverFacade.clear();
            assertHttpOk(serverFacade.getStatusCode());

            ResponseException loginE = Assertions.assertThrows(ResponseException.class, () -> {
                serverFacade.login(existingUser.username(), existingUser.password());
            });

            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, loginE.statusCode());
            Assertions.assertTrue(loginE.getMessage().contains("unauthorized"));

            ResponseException listE = Assertions.assertThrows(ResponseException.class, () ->serverFacade.listGames(existingAuth));
            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, listE.statusCode());
            Assertions.assertTrue(listE.getMessage().contains("unauthorized"));

            registerResult = serverFacade.register(user);
            assertHttpOk(serverFacade.getStatusCode());

            JsonObject listResult = serverFacade.listGames(registerResult.get("authToken").getAsString());
            assertHttpOk(serverFacade.getStatusCode());

            Type collectionType = new TypeToken<Collection<PrintedGameData>>(){}.getType();

            Collection<PrintedGameData> gameList = new Gson().fromJson(listResult.get("games"), collectionType);

            Assertions.assertEquals(gameList.size(), 0, "list result did not return 0 games after clear");
        });
    }

    @Test
    @Order(14)
    @DisplayName("Multiple Clears")
    public void multipleClear() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.clear();
            serverFacade.clear();
            serverFacade.clear();

            assertHttpOk(serverFacade.getStatusCode());
        });
    }

    private void assertHttpOk(int status) {
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, status, "Server response code was not 200 ok");
    }

}
