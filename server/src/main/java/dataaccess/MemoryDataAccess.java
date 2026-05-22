package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import chess.ChessGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("already taken");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public AuthData createAuth(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        auths.put(token, auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        auths.remove(authToken);
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        int id = nextGameID++;
        GameData game = new GameData(id, null, null, gameName, new ChessGame());
        games.put(id, game);
        return game;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("game not found");
        }
        games.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        auths.clear();
        games.clear();
        nextGameID = 1;
    }
}