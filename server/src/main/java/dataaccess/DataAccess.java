// interface
package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
    // user
    void createUser(UserData userData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // auth
    AuthData createAuth(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // game
    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData gameData) throws DataAccessException;

    // util
    void clear() throws DataAccessException;
}