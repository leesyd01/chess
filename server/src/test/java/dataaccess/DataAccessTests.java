package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

/** tests for MySqlDataAccess
 * one positive and one negative test for each method except clear() */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTests {
    private static MySqlDataAccess dataAccess;

    @BeforeAll
    static void setup() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        dataAccess.clear();
    }

    // CLEAR
    @Test
    @Order(1)
    @DisplayName("Clear: removes all data successfully")
    void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("alice", "password123", "alice@example.com"));
        dataAccess.createGame("TestGame");
        dataAccess.clear();
        assertNull(dataAccess.getUser("alice"), "User should be gone after clear");
        assertTrue(dataAccess.listGames().isEmpty(), "Games should be gone after clear");
    }

    // CREATE USER
    @Test
    @Order(2)
    @DisplayName("createUser: successfully creates a new user")
    void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        dataAccess.createUser(user);

        UserData retrieved = dataAccess.getUser("alice");
        assertNotNull(retrieved, "User should exist after creation");
        assertEquals("alice", retrieved.username());
        assertEquals("alice@example.com", retrieved.email());
    }

    @Test
    @Order(3)
    @DisplayName("createUser: throws exception for duplicate username")
    void createUserDuplicate() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        dataAccess.createUser(user);

        assertThrows(DataAccessException.class, () ->
                        dataAccess.createUser(new UserData("alice", "other", "other@example.com")),
                "Should throw DataAccessException for duplicate username"
        );
    }

    // GET USER
    @Test
    @Order(4)
    @DisplayName("getUser: successfully retrieves an existing user")
    void getUserSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("bob", "pass", "bob@example.com"));

        UserData user = dataAccess.getUser("bob");
        assertNotNull(user);
        assertEquals("bob", user.username());
        assertEquals("bob@example.com", user.email());
    }

    @Test
    @Order(5)
    @DisplayName("getUser: returns null for non-existent user")
    void getUserNotFound() throws DataAccessException {
        UserData user = dataAccess.getUser("nobody");
        assertNull(user, "Should return null when user does not exist");
    }

    // CREATE AUTH
    @Test
    @Order(6)
    @DisplayName("createAuth: successfully creates an auth token")
    void createAuthSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("carol", "pass", "carol@example.com"));

        AuthData auth = dataAccess.createAuth("carol");
        assertNotNull(auth, "Auth should not be null");
        assertNotNull(auth.authToken(), "Auth token should not be null");
        assertEquals("carol", auth.username());
    }

    @Test
    @Order(7)
    @DisplayName("createAuth: two tokens for same user are unique")
    void createAuthTokensAreUnique() throws DataAccessException {
        dataAccess.createUser(new UserData("carol", "pass", "carol@example.com"));

        AuthData auth1 = dataAccess.createAuth("carol");
        AuthData auth2 = dataAccess.createAuth("carol");
        assertNotEquals(auth1.authToken(), auth2.authToken(),
                "Each auth token should be unique");
    }

    // GET AUTH
    @Test
    @Order(8)
    @DisplayName("getAuth: successfully retrieves an existing auth token")
    void getAuthSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("dave", "pass", "dave@example.com"));
        AuthData created = dataAccess.createAuth("dave");

        AuthData retrieved = dataAccess.getAuth(created.authToken());
        assertNotNull(retrieved);
        assertEquals(created.authToken(), retrieved.authToken());
        assertEquals("dave", retrieved.username());
    }

    @Test
    @Order(9)
    @DisplayName("getAuth: returns null for invalid token")
    void getAuthInvalidToken() throws DataAccessException {
        AuthData auth = dataAccess.getAuth("fake-token-that-does-not-exist");
        assertNull(auth, "Should return null for a non-existent token");
    }

    // DELETE AUTH
    @Test
    @Order(10)
    @DisplayName("deleteAuth: successfully deletes an auth token")
    void deleteAuthSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("eve", "pass", "eve@example.com"));
        AuthData auth = dataAccess.createAuth("eve");

        dataAccess.deleteAuth(auth.authToken());
        assertNull(dataAccess.getAuth(auth.authToken()),
                "Token should be null after deletion");
    }

    @Test
    @Order(11)
    @DisplayName("deleteAuth: deleting non-existent token does not throw")
    void deleteAuthNonExistent() {
        assertDoesNotThrow(() -> dataAccess.deleteAuth("non-existent-token"),
                "Deleting a non-existent token should not throw");
    }

    // CREATE GAME
    @Test
    @Order(12)
    @DisplayName("createGame: successfully creates a new game")
    void createGameSuccess() throws DataAccessException {
        GameData game = dataAccess.createGame("MyChessGame");

        assertNotNull(game);
        assertEquals("MyChessGame", game.gameName());
        assertTrue(game.gameID() > 0, "Game ID should be a positive integer");
        assertNotNull(game.game(), "ChessGame object should not be null");
    }

    @Test
    @Order(13)
    @DisplayName("createGame: two games get different IDs")
    void createGameUniqueIDs() throws DataAccessException {
        GameData game1 = dataAccess.createGame("Game One");
        GameData game2 = dataAccess.createGame("Game Two");

        assertNotEquals(game1.gameID(), game2.gameID(),
                "Each game should have a unique ID");
    }

    // GET GAME
    @Test
    @Order(14)
    @DisplayName("getGame: successfully retrieves an existing game")
    void getGameSuccess() throws DataAccessException {
        GameData created = dataAccess.createGame("RetrieveMe");

        GameData retrieved = dataAccess.getGame(created.gameID());
        assertNotNull(retrieved);
        assertEquals(created.gameID(), retrieved.gameID());
        assertEquals("RetrieveMe", retrieved.gameName());
    }

    @Test
    @Order(15)
    @DisplayName("getGame: returns null for non-existent game ID")
    void getGameNotFound() throws DataAccessException {
        GameData game = dataAccess.getGame(99999);
        assertNull(game, "Should return null for a game ID that doesn't exist");
    }

    // LIST GAMES
    @Test
    @Order(16)
    @DisplayName("listGames: returns all created games")
    void listGamesSuccess() throws DataAccessException {
        dataAccess.createGame("Alpha");
        dataAccess.createGame("Beta");
        dataAccess.createGame("Gamma");

        Collection<GameData> games = dataAccess.listGames();
        assertEquals(3, games.size(), "Should return all 3 created games");
    }

    @Test
    @Order(17)
    @DisplayName("listGames: returns empty list when no games exist")
    void listGamesEmpty() throws DataAccessException {
        Collection<GameData> games = dataAccess.listGames();
        assertNotNull(games, "Should return an empty collection, not null");
        assertTrue(games.isEmpty(), "Should be empty when no games exist");
    }

    // UPDATE GAME
    @Test
    @Order(18)
    @DisplayName("updateGame: successfully updates a game's players")
    void updateGameSuccess() throws DataAccessException {
        GameData game = dataAccess.createGame("UpdateMe");

        GameData updated = new GameData(
                game.gameID(), "whitePlayer", null, game.gameName(), game.game()
        );
        dataAccess.updateGame(updated);

        GameData retrieved = dataAccess.getGame(game.gameID());
        assertEquals("whitePlayer", retrieved.whiteUsername(),
                "White player should be updated");
    }

    @Test
    @Order(19)
    @DisplayName("updateGame: persists ChessGame board state after a move")
    void updateGamePersistsBoardState() throws DataAccessException {
        GameData game = dataAccess.createGame("BoardTest");

        ChessGame chessGame = game.game();
        GameData updated = new GameData(
                game.gameID(), "white", "black", game.gameName(), chessGame
        );
        dataAccess.updateGame(updated);

        GameData retrieved = dataAccess.getGame(game.gameID());
        assertNotNull(retrieved.game(), "Chess game state should be persisted");
        assertEquals("white", retrieved.whiteUsername());
        assertEquals("black", retrieved.blackUsername());
    }

    @Test
    @Order(20)
    @DisplayName("updateGame: throws exception for non-existent game ID")
    void updateGameNotFound() {
        GameData nonExistent = new GameData(99999, "white", "black", "Ghost", new ChessGame());

        assertThrows(DataAccessException.class, () ->
                        dataAccess.updateGame(nonExistent),
                "Should throw DataAccessException when updating a game that doesn't exist"
        );
    }
}