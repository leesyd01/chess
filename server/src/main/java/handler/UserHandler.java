package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ServiceException;
import service.UserService;

/** handles HTTP requests for user registration, login, and logout. */

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            var body = gson.fromJson(ctx.body(), RegisterRequest.class);
            if (body == null) {
                ctx.status(400).json(new ErrorResponse("Error: bad request"));
                return;
            }
            var auth = userService.register(body.username(), body.password(), body.email());
            ctx.status(200).json(new RegisterResponse(auth.username(), auth.authToken()));
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            var body = gson.fromJson(ctx.body(), LoginRequest.class);
            if (body == null) {
                ctx.status(400).json(new ErrorResponse("Error: bad request"));
                return;
            }
            var auth = userService.login(body.username(), body.password());
            ctx.status(200).json(new LoginResponse(auth.username(), auth.authToken()));
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    public void logout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            userService.logout(authToken);
            ctx.status(200).json("{}");
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    // records for request and response
    private record RegisterRequest(String username, String password, String email) {}
    private record RegisterResponse(String username, String authToken) {}
    private record LoginRequest(String username, String password) {}
    private record LoginResponse(String username, String authToken) {}
    private record ErrorResponse(String message) {}
}