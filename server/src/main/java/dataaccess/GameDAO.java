// declares what methods exist
package dataaccess;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(GameData gameData) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    void clear() throws DataAccessException;
}