package client;

import chess.ChessGame;
import model.PrintedGameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import service.requests.*;
import service.results.CreateGameResult;
import service.results.ListGamesResult;
import service.results.LoginResult;
import service.results.RegisterResult;
import ui.ResponseException;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;


public class ServerFacadeTests {

    private static UserData existingUser;
    private static UserData newUser;
    private static CreateGameRequest createRequest;
    private String existingAuth;
    private static Server server;
    static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port;
        serverFacade = new ServerFacade(url);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");

        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() throws ResponseException {
        serverFacade.clear();

        RegisterRequest registerRequest = new RegisterRequest(existingUser.username(), existingUser.password(), existingUser.email());
        RegisterResult regResult = serverFacade.register(registerRequest);
        existingAuth = regResult.authToken();

        createRequest = new CreateGameRequest(existingAuth, "testGame");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Normal User Login")
    public void successLogin() throws ResponseException {
        LoginRequest loginRequest = new LoginRequest(existingUser.username(), existingUser.password());
        LoginResult loginResult = serverFacade.login(loginRequest);

        assertHttpOk(serverFacade.getStatusCode());

        Assertions.assertEquals(existingUser.username(), loginResult.username(),
                "Response did not give the same username as user");
        Assertions.assertNotNull(loginResult.authToken(), "Response did not return authentication String");
    }

    @Test
    @Order(2)
    @DisplayName("Login Invalid User")
    public void loginInvalidUser() {
        try {
            LoginRequest loginRequest=new LoginRequest(newUser.username(), newUser.password());
            serverFacade.login(loginRequest);
        } catch (ResponseException e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.StatusCode(),
                    "Server response code was not 401");
            Assertions.assertTrue(e.getMessage().contains("unauthorized"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Normal User Registration")
    public void successRegister() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest(newUser.username(), newUser.password(), newUser.email());
        RegisterResult registerResult = serverFacade.register(registerRequest);

        assertHttpOk(serverFacade.getStatusCode());

        Assertions.assertEquals(newUser.username(), registerResult.username(),
                "Response did not have the same username as was registered");
        Assertions.assertNotNull(registerResult.authToken(), "Response did not contain an authentication string");
    }

    @Test
    @Order(4)
    @DisplayName("Re-Register User")
    public void registerTwice() {
        try {
            RegisterRequest registerRequest=new RegisterRequest(existingUser.username(), existingUser.password(), existingUser.email());
            serverFacade.register(registerRequest);
        } catch (ResponseException e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.StatusCode());
            Assertions.assertTrue(e.getMessage().contains("already taken"));
        }
    }

    @Test
    @Order(5)
    @DisplayName("Register Bad Request")
    public void failRegister() {
        try {
            RegisterRequest registerRequest=new RegisterRequest(existingUser.username(), null, existingUser.email());
            serverFacade.register(registerRequest);
        } catch (ResponseException e) {
            Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.StatusCode());
            Assertions.assertTrue(e.getMessage().contains("bad request"));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Normal Logout")
    public void successLogout() {
        LogoutRequest logoutRequest = new LogoutRequest(existingAuth);
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(logoutRequest));
        assertHttpOk(serverFacade.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("Invalid Auth Logout")
    public void failLogout() {
        LogoutRequest logoutRequest = new LogoutRequest(existingAuth);
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(logoutRequest));

        assertHttpOk(serverFacade.getStatusCode());

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.logout(logoutRequest));

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.StatusCode());
        Assertions.assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @Order(8)
    @DisplayName("Valid Creation")
    public void goodCreate() throws ResponseException {
        CreateGameResult createResult = serverFacade.createGame(createRequest);

        assertHttpOk(serverFacade.getStatusCode());
        Assertions.assertTrue(createResult.gameID() > 0, "Result returned invalid game ID");
    }

    @Test
    @Order(9)
    @DisplayName("Create with Bad Authentication")
    public void badAuthCreate() {
        LogoutRequest logoutRequest = new LogoutRequest(existingAuth);
        Assertions.assertDoesNotThrow(() -> serverFacade.logout(logoutRequest));

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.createGame(createRequest));

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.StatusCode());
        Assertions.assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @Order(10)
    @DisplayName("Join Created Game")
    public void goodJoin() {
        Assertions.assertDoesNotThrow(() -> {
            CreateGameResult createResult=serverFacade.createGame(createRequest);
            JoinGameRequest joinRequest = new JoinGameRequest(existingAuth, ChessGame.TeamColor.WHITE, createResult.gameID());
            serverFacade.joinGame(joinRequest);

            assertHttpOk(serverFacade.getStatusCode());

            ListGamesRequest listGamesRequest = new ListGamesRequest(existingAuth);
            ListGamesResult listResult = serverFacade.listGames(listGamesRequest);

            boolean containsExpectedGameData = false;

            for (var game : listResult.games()) {
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
        CreateGameResult createResult = serverFacade.createGame(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest(existingAuth + "bad stuff", ChessGame.TeamColor.WHITE, createResult.gameID());
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.joinGame(joinRequest));

        Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.StatusCode());
        Assertions.assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    @Order(11)
    @DisplayName("Join Bad Team Color")
    public void badColorJoin() throws ResponseException {
        CreateGameResult createResult = serverFacade.createGame(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest(existingAuth, null, createResult.gameID());
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.joinGame(joinRequest));

        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.StatusCode());
        Assertions.assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @Order(11)
    @DisplayName("Join Bad Game ID")
    public void badGameIDJoin() throws ResponseException {
        createRequest = new CreateGameRequest(existingAuth, "Bad Join");
        serverFacade.createGame(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest(existingAuth, ChessGame.TeamColor.WHITE, -1);
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.joinGame(joinRequest));

        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, e.StatusCode());
        Assertions.assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @Order(11)
    @DisplayName("Join Steal Team Color")
    public void stealColorJoin() throws ResponseException {
        CreateGameResult createResult = serverFacade.createGame(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest(existingAuth, ChessGame.TeamColor.BLACK, createResult.gameID());
        serverFacade.joinGame(joinRequest);

        RegisterRequest registerRequest = new RegisterRequest(newUser.username(), newUser.password(), newUser.email());
        RegisterResult registerResult = serverFacade.register(registerRequest);

        JoinGameRequest newJoinRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.BLACK, createResult.gameID());

        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> serverFacade.joinGame(newJoinRequest));

        Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.StatusCode());
        Assertions.assertTrue(e.getMessage().contains("already taken"));
    }

    @Test
    @Order(12)
    @DisplayName("List No Games")
    public void noGamesList() {
        Assertions.assertDoesNotThrow(() -> {
            ListGamesRequest listGamesRequest = new ListGamesRequest(existingAuth);
            ListGamesResult result = serverFacade.listGames(listGamesRequest);

            assertHttpOk(serverFacade.getStatusCode());
            Assertions.assertTrue(result.games() == null || result.games().isEmpty(),
                    "Found games when none should be there");
        });
    }

    @Test
    @Order(12)
    @DisplayName("List Multiple Games")
    public void gamesList() throws ResponseException {
        UserData userA = new UserData("a", "A", "a.A");
        UserData userB = new UserData("b", "B", "b.B");
        UserData userC = new UserData("c", "C", "c.C");

        RegisterRequest registerRequestA = new RegisterRequest(userA.username(), userA.password(), userA.email());
        RegisterRequest registerRequestB = new RegisterRequest(userB.username(), userB.password(), userB.email());
        RegisterRequest registerRequestC = new RegisterRequest(userC.username(), userC.password(), userC.email());

        RegisterResult authA = serverFacade.register(registerRequestA);
        RegisterResult authB = serverFacade.register(registerRequestB);
        RegisterResult authC = serverFacade.register(registerRequestC);

        Collection<PrintedGameData> expectedList = new HashSet<>();

        String game1Name = "I'm numbah one!";
        CreateGameResult game1 = serverFacade.createGame(new CreateGameRequest(authA.authToken(), game1Name));
        serverFacade.joinGame(new JoinGameRequest(authA.authToken(), ChessGame.TeamColor.BLACK, game1.gameID()));
        expectedList.add(new PrintedGameData(game1.gameID(), null, authA.username(), game1Name));


        String game2Name = "Lonely";
        CreateGameResult game2 = serverFacade.createGame(new CreateGameRequest(authB.authToken(), game2Name));
        serverFacade.joinGame(new JoinGameRequest(authB.authToken(), ChessGame.TeamColor.WHITE, game2.gameID()));
        expectedList.add(new PrintedGameData(game2.gameID(), authB.username(), null, game2Name));


        String game3Name = "GG";
        CreateGameResult game3 = serverFacade.createGame(new CreateGameRequest(authC.authToken(), game3Name));
        serverFacade.joinGame(new JoinGameRequest(authC.authToken(), ChessGame.TeamColor.WHITE, game3.gameID()));
        serverFacade.joinGame(new JoinGameRequest(authA.authToken(), ChessGame.TeamColor.BLACK, game3.gameID()));
        expectedList.add(new PrintedGameData(game3.gameID(), authC.username(), authA.username(), game3Name));


        String game4Name = "All by myself";
        CreateGameResult game4 = serverFacade.createGame(new CreateGameRequest(authC.authToken(), game4Name));
        serverFacade.joinGame(new JoinGameRequest(authC.authToken(), ChessGame.TeamColor.WHITE, game4.gameID()));
        serverFacade.joinGame(new JoinGameRequest(authC.authToken(), ChessGame.TeamColor.BLACK, game4.gameID()));
        expectedList.add(new PrintedGameData(game4.gameID(), authC.username(), authC.username(), game4Name));


        ListGamesResult listResult = serverFacade.listGames(new ListGamesRequest(existingAuth));
        assertHttpOk(serverFacade.getStatusCode());
        Collection<PrintedGameData> returnedList = listResult.games();

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
            LoginResult loginOne = serverFacade.login(new LoginRequest(existingUser.username(), existingUser.password()));
            assertHttpOk(serverFacade.getStatusCode());

            Assertions.assertNotNull(loginOne.authToken(),
                    "Login result did not contain an authToken");

            LoginResult loginTwo = serverFacade.login(new LoginRequest(existingUser.username(), existingUser.password()));
            assertHttpOk(serverFacade.getStatusCode());

            Assertions.assertNotNull(loginTwo.authToken(),
                    "Login result did not contain an authToken");
            Assertions.assertNotEquals(existingAuth, loginOne.authToken(),
                    "Authtoken returned by login matched authtoken from prior register");
            Assertions.assertNotEquals(existingAuth, loginTwo.authToken(),
                    "Authtoken returned by login matched authtoken from prior register");
            Assertions.assertNotEquals(loginOne.authToken(), loginTwo.authToken(),
                    "Authtoken returned by login matched authtoken from prior login");


            CreateGameResult createResult = serverFacade.createGame(createRequest);
            assertHttpOk(serverFacade.getStatusCode());


            serverFacade.logout(new LogoutRequest(existingAuth));
            assertHttpOk(serverFacade.getStatusCode());


            JoinGameRequest joinRequest = new JoinGameRequest(loginOne.authToken(), ChessGame.TeamColor.WHITE, createResult.gameID());
            serverFacade.joinGame(joinRequest);
            assertHttpOk(serverFacade.getStatusCode());


            ListGamesResult listResult = serverFacade.listGames(new ListGamesRequest(loginTwo.authToken()));
            assertHttpOk(serverFacade.getStatusCode());

            Assertions.assertEquals(1, listResult.games().size());
            Assertions.assertEquals(existingUser.username(), listResult.games().iterator().next().whiteUsername());
        });
    }

    @Test
    @Order(14)
    @DisplayName("Clear Test")
    public void clearData() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.createGame(new CreateGameRequest(existingAuth, "Mediocre game"));
            serverFacade.createGame(new CreateGameRequest(existingAuth, "Awesome game"));

            UserData user = new UserData("ClearMe", "cleared", "clear@mail.com");
            RegisterResult registerResult = serverFacade.register(new RegisterRequest(user.username(), user.password(), user.email()));

            CreateGameResult createResult = serverFacade.createGame(new CreateGameRequest(registerResult.authToken(), "Clear game"));

            JoinGameRequest joinRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.WHITE, createResult.gameID());
            serverFacade.joinGame(joinRequest);

            serverFacade.clear();
            assertHttpOk(serverFacade.getStatusCode());

            ResponseException loginE = Assertions.assertThrows(ResponseException.class, () -> serverFacade.login(new LoginRequest(existingUser.username(), existingUser.password())));

            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, loginE.StatusCode());
            Assertions.assertTrue(loginE.getMessage().contains("unauthorized"));

            ResponseException listE = Assertions.assertThrows(ResponseException.class, () ->serverFacade.listGames(new ListGamesRequest(existingAuth)));
            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, listE.StatusCode());
            Assertions.assertTrue(listE.getMessage().contains("unauthorized"));

            registerResult = serverFacade.register(new RegisterRequest(user.username(), user.password(), user.email()));
            assertHttpOk(serverFacade.getStatusCode());

            ListGamesResult listResult = serverFacade.listGames(new ListGamesRequest(registerResult.authToken()));
            assertHttpOk(serverFacade.getStatusCode());

            Assertions.assertEquals(0, listResult.games().size(), "list result did not return 0 games after clear");
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
