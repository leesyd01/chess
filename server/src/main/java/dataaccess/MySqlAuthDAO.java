package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO {

    public MySqlAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth token: " + e.getMessage());
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
            throw new DataAccessException("Error getting auth token: " + e.getMessage());
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
            throw new DataAccessException("Error deleting auth token: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "TRUNCATE TABLE auth";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth table: " + e.getMessage());
        }
    }

    private void configureDatabase() throws DataAccessException {
        String createTable = """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(256) NOT NULL,
                    username  VARCHAR(256) NOT NULL,
                    PRIMARY KEY (authToken)
                )""";
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(createTable)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error configuring auth table: " + e.getMessage());
        }
    }
}
