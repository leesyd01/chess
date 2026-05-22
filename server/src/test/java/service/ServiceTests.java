package service;

import dataAccess.DataAccessException;
import dataAccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private MemoryDataAccess dataAccess;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws Exception {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);
    }

    // register
    @Test
    public void registerSuccess() throws Exception {
        AuthData auth = userService.register("alice", "password123", "alice@test.com");
        assertNotNull(auth);
        assertEquals("alice", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void registerDuplicateUsername() throws Exception {
        userService.register("alice", "password123", "alice@test.com");
        ServiceException ex = assertThrows(ServiceException.class, () ->
                userService.register("alice", "otherpass", "other@test.com"));
        assertEquals(403, ex.statusCode());
    }

    // login
    @Test
    public void loginSuccess() throws Exception {
        userService.register("bob", "pass456", "bob@test.com");
        AuthData auth = userService.login("bob", "pass456");
        assertNotNull(auth);
        assertEquals("bob", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        userService.register("bob", "pass456", "bob@test.com");
        ServiceException ex = assertThrows(ServiceException.class, () ->
                userService.login("bob", "wrongpassword"));
        assertEquals(401, ex.statusCode());
    }

    // logout
    @Test
    public void logoutSuccess() throws Exception {
        AuthData auth = userService.register("carol", "pass789", "carol@test.com");
        assertDoesNotThrow(() -> userService.logout(auth.authToken()));
    }

    @Test
    public void logoutInvalidToken() {
        ServiceException ex = assertThrows(ServiceException.class, () ->
                userService.logout("not-a-real-token"));
        assertEquals(401, ex.statusCode());
    }

    // create game
    @Test
    public void createGameSuccess() throws Exception {
        AuthData auth = userService.register("dave", "pass", "dave@test.com");
        GameData game = gameService.createGame(auth.authToken(), "My Game");
        assertNotNull(game);
        assertEquals("My Game", game.gameName());
        assertTrue(game.gameID() > 0);
    }

    @Test
    public void createGameUnauthorized() {
        ServiceException ex = assertThrows(ServiceException.class, () ->
               gameService.createGame("fake-token", "My Game"));
        assertEquals(401, ex.statusCode());
    }

    // list games
    @Test
    public void listGamesSuccess() throws Exception {
        AuthData auth = userService.register("eve", "pass", "eve@test.com");
        gameService.createGame(auth.authToken(), "Game 1");
        gameService.createGame(auth.authToken(), "Game 2");
        Collection<GameData> games = gameService.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesUnauthorized() {
        ServiceException ex = assertThrows(ServiceException.class, () ->
                gameService.listGames("fake-token"));
        assertEquals(401, ex.statusCode());
    }

    // join game
    @Test
    public void joinGameSuccess() throws Exception {
        AuthData auth = userService.register("frank", "pass", "frank@test.com");
        GameData game = gameService.createGame(auth.authToken(), "Chess Match");
        assertDoesNotThrow(() -> gameService.joinGame(auth.authToken(), "WHITE", game.gameID()));
    }

    @Test
    public void joinGameColorAlreadyTaken() throws Exception {
        AuthData auth1 = userService.register("grace", "pass", "grace@test.com");
        AuthData auth2 = userService.register("henry", "pass", "henry@test.com");
        GameData game = gameService.createGame(auth1.authToken(), "Chess Match");
        gameService.joinGame(auth1.authToken(), "WHITE", game.gameID());
        ServiceException ex = assertThrows(ServiceException.class, () ->
                gameService.joinGame(auth2.authToken(), "WHITE", game.gameID()));
        assertEquals(403, ex.statusCode());
    }

    // clear
    @Test
    public void clearSuccess() throws Exception {
        AuthData auth = userService.register("ivan", "pass", "ivan@test.com");
        gameService.createGame(auth.authToken(), "Some Game");
        assertDoesNotThrow(() -> clearService.clear());

        // auth token should be gone now
        ServiceException ex = assertThrows(ServiceException.class, () ->
                gameService.listGames(auth.authToken()));
        assertEquals(401, ex.statusCode());
    }
}