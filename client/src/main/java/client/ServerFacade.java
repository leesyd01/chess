package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Handles all HTTP communication with the chess server.
 * Each method corresponds to one server API endpoint.
 */
public class ServerFacade {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    // auth endpoints

    /**
     * Registers a new user and returns their auth data.
     */
    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);
        return makeRequest("POST", "/user", null, body, AuthData.class);
    }

    /**
     * Logs in an existing user and returns their auth data.
     */
    public AuthData login(String username, String password) throws Exception {
        var body = Map.of("username", username, "password", password);
        return makeRequest("POST", "/session", null, body, AuthData.class);
    }

    /**
     * Logs out the currently logged-in user.
     */
    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", authToken, null, null);
    }

    // game endpoints

    /**
     * Returns the list of all games on the server.
     */
    public Collection<GameData> listGames(String authToken) throws Exception {
        // Server returns { "games": [ ... ] }
        var result = makeRequest("GET", "/game", authToken, null, GamesResponse.class);
        return result.games();
    }

    /**
     * Creates a new game with the given name and returns the new game's ID.
     */
    public int createGame(String authToken, String gameName) throws Exception {
        var body = Map.of("gameName", gameName);
        var result = makeRequest("POST", "/game", authToken, body, CreateGameResponse.class);
        return result.gameID();
    }

    /**
     * Joins an existing game as the given color ("WHITE" or "BLACK").
     * Pass null for playerColor to observe.
     */
    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var body = playerColor != null
                ? Map.of("gameID", gameID, "playerColor", playerColor)
                : Map.of("gameID", gameID);
        makeRequest("PUT", "/game", authToken, body, null);
    }

    // HTTP helper

    <T> T makeRequest(String method, String path, String authToken,
                              Object requestBody, Class<T> responseClass) throws Exception {
        var url = URI.create(baseUrl + path).toURL();
        var connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");

            if (authToken != null) {
                connection.setRequestProperty("Authorization", authToken);
            }

            if (requestBody != null) {
                connection.setDoOutput(true);
                try (var out = connection.getOutputStream()) {
                    out.write(gson.toJson(requestBody).getBytes());
                }
            }

            connection.connect();

            int status = connection.getResponseCode();
            if (status / 100 != 2) {
                // Read error body for a helpful message
                String errorMsg = readBody(connection.getErrorStream());
                var errorResponse = gson.fromJson(errorMsg, ErrorResponse.class);
                String message = (errorResponse != null && errorResponse.message() != null)
                        ? errorResponse.message()
                        : "Request failed (status " + status + ")";
                throw new Exception(message);
            }

            if (responseClass == null) {
                return null;
            }
            return gson.fromJson(readBody(connection.getInputStream()), responseClass);

        } finally {
            connection.disconnect();
        }
    }

    private String readBody(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (var reader = new BufferedReader(new InputStreamReader(stream))) {
            var sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    // private response record types for deserialization

    private record GamesResponse(List<GameData> games) {}
    private record CreateGameResponse(int gameID) {}
    private record ErrorResponse(String message) {}
}