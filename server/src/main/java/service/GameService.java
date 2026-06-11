// list, create, join
package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

/** handles business logic for listing, creating, and joining a chess game. */

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /** returns all existing games.
     * 401 if auth token is invalid. */

    public Collection<GameData> listGames(String authToken) throws ServiceException {
        authorize(authToken);
        try {
            return dataAccess.listGames();
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    /** creates new game with given info.
     * 401 if unauthorized; 400 if game name is blank. */

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

    /** adds player to existing game in team color of choice.
     * 401 if unauthorized; 400 if game not found; 403 if requested color is taken. */

    public void joinGame(String authToken, String playerColor, int gameID) throws ServiceException {
        AuthData auth = authorize(authToken);
        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                throw new ServiceException(400, "bad request");
            }
            if (playerColor == null ||
                    (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK"))) {
                throw new ServiceException(400, "bad request");
            }
            GameData updated = buildUpdatedGame(game, auth.username(), playerColor);
            dataAccess.updateGame(updated);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    /** returns a copy of the game with the requesting user assigned to the chosen color.
     * 403 updated if the requested color is taken. */
    private GameData buildUpdatedGame(GameData game, String username, String playerColor) throws ServiceException {
        if (playerColor.equalsIgnoreCase("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new ServiceException(403, "already taken");
            }
            return new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else {
            if (game.blackUsername() != null) {
                throw new ServiceException(403, "already taken");
            }
            return new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        }
    }

    /** validates an auth token and returns the associated AuthData.
     * 401 if the token is null or not found. */

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

    // phase 6 additions
    /** gets a single game by ID, throws 401 if unauthorized, 400 if not found */
    public GameData getGame(String authToken, int gameID) throws ServiceException {
        authorize(authToken);
        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) throw new ServiceException(400, "game not found");
            return game;
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    /** saves updated game state, 401 if unauthorized */
    public void updateGame(String authToken, GameData game) throws ServiceException {
        authorize(authToken);
        try {
            dataAccess.updateGame(game);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    /** removes a player from a game when they leave, 401 if unauthorized */
    public void leaveGame(String authToken, int gameID, chess.ChessGame.TeamColor color) throws ServiceException {
        authorize(authToken);
        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) return;
            GameData updated;
            if (color == chess.ChessGame.TeamColor.WHITE) {
                updated = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            } else {
                updated = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            }
            dataAccess.updateGame(updated);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }
}