// list, create, join
package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Collection<GameData> listGames(String authToken) throws ServiceException {
        authorize(authToken);
        try {
            return dataAccess.listGames();
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public GameData createGame(String authToken, String gameName) throws ServiceException {
        authorize(authToken);
        if (gameName == null || gameName.isBlank()) {
            throw new ServiceException(400, "bad request");
        }
        try {
            return dataAccess.createGame(gameName);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ServiceException {
        AuthData auth = authorize(authToken);
        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                throw new ServiceException(400, "bad request");
            }
            if (playerColor == null || (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK"))) {
                throw new ServiceException(400, "bad request");
            }
            GameData updated;
            if (playerColor.equalsIgnoreCase("WHITE")) {
                if (game.whiteUsername() != null) {
                    throw new ServiceException(403, "already taken");
                }
                updated = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
            } else {
                if (game.blackUsername() != null) {
                    throw new ServiceException(403, "already taken");
                }
                updated = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
            }
            dataAccess.updateGame(updated);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    // Returns the AuthData if valid, throws 401 if not
    private AuthData authorize(String authToken) throws ServiceException {
        if (authToken == null) {
            throw new ServiceException(401, "unauthorized");
        }
        try {
            AuthData auth = dataAccess.getAuth(authToken);
            if (auth == null) {
                throw new ServiceException(401, "unauthorized");
            }
            return auth;
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }
}