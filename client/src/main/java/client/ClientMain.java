package client;

import chess.*;
import model.AuthData;
import ui.PostloginUI;
import ui.PreloginUI;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        ServerFacade facade = new ServerFacade(port);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            PreloginUI prelogin = new PreloginUI(facade, scanner);
            AuthData auth = prelogin.run();

            if (auth == null) {
                break;
            }

            PostloginUI postlogin = new PostloginUI(facade, scanner, auth);
            boolean loggedOut = postlogin.run();

            if (!loggedOut) {
                break;
            }
        }
    }
}
