package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MySqlDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    public MySqlDataAccess() throws DataAccessException{
        configureDatabase();
    }

    // USER
    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (getUser(user.username()) != null) {
            throw new DataAccessException("already taken");
        }
        String hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, hashed);
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
    }

    /** returns true if provided plain text password matches stored bcrypt hash */
    public boolean verifyPassword(String username, String providedPassword) throws DataAccessException {
        UserData user = getUser(username);
        if (user == null) {
            return false;
        }
        return BCrypt.checkpw(providedPassword, user.password());
    }

    // AUTH
    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, username);
            ps.executeUpdate();
            return new AuthData(token, username);
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new AuthData(
                        rs.getString("authToken"),
                        rs.getString("username")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error getting auth: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth: " + e.getMessage());
        }
    }

    // GAME
    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        ChessGame newGame = new ChessGame();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, null);
            ps.setString(2, null);
            ps.setString(3, gameName);
            ps.setString(4, gson.toJson(newGame));
            ps.executeUpdate();
            var keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                return new GameData(id, null, null, gameName, newGame);
            }
            throw new DataAccessException("Error: game ID not generated");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        gson.fromJson(rs.getString("game"), ChessGame.class)
                );
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            var rs = ps.executeQuery();
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        gson.fromJson(rs.getString("game"), ChessGame.class)
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gson.toJson(game.game()));
            ps.setInt(5, game.gameID());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("game not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    // UTIL
    @Override
    public void clear() throws DataAccessException {
        for (String table : new String[]{"auth", "games", "users"}) {
            try (var conn = DatabaseManager.getConnection();
                var ps = conn.prepareStatement("TRUNCATE TABLE " + table)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("Error clearing " + table + ": " + e.getMessage());
            }
        }
    }

    // DB SETUP
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        String[] tables = {
                """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(256) NOT NULL,
                password VARCHAR(256) NOT NULL,
                email    VARCHAR(256) NOT NULL,
                PRIMARY KEY (username)
            )""",
                """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(256) NOT NULL,
                username  VARCHAR(256) NOT NULL,
                PRIMARY KEY (authToken)
            )""",
                """
            CREATE TABLE IF NOT EXISTS games (
                gameID        INT          NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(256),
                blackUsername VARCHAR(256),
                gameName      VARCHAR(256) NOT NULL,
                game          TEXT         NOT NULL,
                PRIMARY KEY (gameID)
            )"""
        };
        try (var conn = DatabaseManager.getConnection()) {
            for (String sql : tables) {
                try (var ps = conn.prepareStatement(sql)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error configuring database: " + e.getMessage());
        }
    }
}