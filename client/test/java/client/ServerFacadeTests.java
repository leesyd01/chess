package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        // Adjust this if your clear endpoint path differs
        facade.makeRequestPublic("DELETE", "/db", null, null, null);
    }

   // register

    @Test
    @DisplayName("Register - success")
    void registerPositive() throws Exception {
        AuthData auth = facade.register("alice", "password123", "alice@example.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
        assertEquals("alice", auth.username());
    }

    @Test
    @DisplayName("Register - duplicate username fails")
    void registerNegative() throws Exception {
        facade.register("bob", "pass", "bob@example.com");
        assertThrows(Exception.class, () ->
                facade.register("bob", "different", "bob2@example.com"));
    }

    // login

    @Test
    @DisplayName("Login - success")
    void loginPositive() throws Exception {
        facade.register("carol", "mypassword", "carol@example.com");
        AuthData auth = facade.login("carol", "mypassword");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("carol", auth.username());
    }

    @Test
    @DisplayName("Login - wrong password fails")
    void loginNegative() throws Exception {
        facade.register("dave", "correctpass", "dave@example.com");
        assertThrows(Exception.class, () ->
                facade.login("dave", "wrongpass"));
    }

    // logout

    @Test
    @DisplayName("Logout - success")
    void logoutPositive() throws Exception {
        AuthData auth = facade.register("eve", "pass", "eve@example.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    @DisplayName("Logout - invalid token fails")
    void logoutNegative() {
        assertThrows(Exception.class, () ->
                facade.logout("not-a-real-token"));
    }

    // list games

    @Test
    @DisplayName("List games - returns games")
    void listGamesPositive() throws Exception {
        AuthData auth = facade.register("frank", "pass", "frank@example.com");
        facade.createGame(auth.authToken(), "Game1");
        facade.createGame(auth.authToken(), "Game2");

        Collection<GameData> games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List games - bad auth token fails")
    void listGamesNegative() {
        assertThrows(Exception.class, () ->
                facade.listGames("invalid-token"));
    }

    // create game

    @Test
    @DisplayName("Create game - returns valid game ID")
    void createGamePositive() throws Exception {
        AuthData auth = facade.register("grace", "pass", "grace@example.com");
        int gameID = facade.createGame(auth.authToken(), "MyGame");
        assertTrue(gameID > 0);
    }

    @Test
    @DisplayName("Create game - bad auth token fails")
    void createGameNegative() {
        assertThrows(Exception.class, () ->
                facade.createGame("bad-token", "SomeGame"));
    }

    // join game

    @Test
    @DisplayName("Join game - success as white")
    void joinGamePositive() throws Exception {
        AuthData auth = facade.register("henry", "pass", "henry@example.com");
        int gameID = facade.createGame(auth.authToken(), "JoinTest");
        assertDoesNotThrow(() -> facade.joinGame(auth.authToken(), gameID, "WHITE"));
    }

    @Test
    @DisplayName("Join game - color already taken fails")
    void joinGameNegative() throws Exception {
        AuthData auth1 = facade.register("ivan", "pass", "ivan@example.com");
        AuthData auth2 = facade.register("judy", "pass", "judy@example.com");
        int gameID = facade.createGame(auth1.authToken(), "TakenTest");

        facade.joinGame(auth1.authToken(), gameID, "WHITE");

        // a second player trying to take WHITE should fail
        assertThrows(Exception.class, () ->
                facade.joinGame(auth2.authToken(), gameID, "WHITE"));
    }
}