package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;
import service.ServiceException;

import java.util.Collection;

/** handles HTTP requests for listing, creating, and joining a chess game. */

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            Collection<GameData> games = gameService.listGames(authToken);
            ctx.status(200).result(gson.toJson(new ListGamesResponse(games)));
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            var body = gson.fromJson(ctx.body(), CreateGameRequest.class);
            if (body == null) {
                ctx.status(400).result(gson.toJson(new ErrorResponse("Error: bad request")));
                return;
            }
            var game = gameService.createGame(authToken, body.gameName());
            ctx.status(200).result(gson.toJson(new CreateGameResponse(game.gameID())));
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            var body = gson.fromJson(ctx.body(), JoinGameRequest.class);
            if (body == null) {
                ctx.status(400).result(gson.toJson(new ErrorResponse("Error: bad request")));
                return;
            }
            gameService.joinGame(authToken, body.playerColor(), body.gameID());
            ctx.status(200).result("{}");
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }

    private record CreateGameRequest(String gameName) {}
    private record CreateGameResponse(int gameID) {}
    private record JoinGameRequest(String playerColor, int gameID) {}
    private record ListGamesResponse(Collection<GameData> games) {}
}