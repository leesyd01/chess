package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public MySqlGameDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gson.toJson(game.game()));
            ps.executeUpdate();
            var keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newID = keys.getInt(1);
                return new GameData(newID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
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
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gson.toJson(game.game()));
            ps.setInt(5, game.gameID());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Error: game not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE games";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }

    private void configureDatabase() throws DataAccessException {
        String createTable = """
                CREATE TABLE IF NOT EXISTS games (
                    gameID        INT          NOT NULL AUTO_INCREMENT,
                    whiteUsername VARCHAR(256),
                    blackUsername VARCHAR(256),
                    gameName      VARCHAR(256) NOT NULL,
                    game          TEXT         NOT NULL,
                    PRIMARY KEY (gameID)
                )""";
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(createTable)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error configuring games table: " + e.getMessage());
        }
    }
}
