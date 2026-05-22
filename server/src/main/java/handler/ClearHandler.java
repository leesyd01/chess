package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;
import service.ServiceException;

/** Handles HTTP requests for clearing all application data. */
public class ClearHandler {

    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void clear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200).result("{}");
        } catch (ServiceException e) {
            ctx.status(e.statusCode()).result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }
}
