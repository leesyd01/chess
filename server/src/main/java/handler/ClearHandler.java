package handler;

import io.javalin.http.Context;
import service.ClearService;
import service.ServiceException;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void clear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200).json("{}");
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).json("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }
}