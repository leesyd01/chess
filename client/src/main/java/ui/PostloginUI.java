package ui;

import client.ServerFacade;
import model.AuthData;
import model.GameData;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostloginUI {
    private final ServerFacade facade;
    private final Scanner scanner;
    private AuthData auth;

    public PostloginUI(ServerFacade facade, Scanner scanner, AuthData auth) {
        this.facade = facade;
        this.scanner = scanner;
        this.auth = auth;
    }

    /** runs postlogin REPL; returns true if logged out (go back to prelogin); returns false if user wants to quit */
    public boolean run() {
        System.out.println("Logged in as " + auth.username() + ". Type 'help for commands.");
        while (true) {
            System.out.print("[" + auth.username() + "] >>> ");
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch(cmd) {
                case "help" -> printHelp();
                case "logout" -> {
                    handleLogout();
                    return true; // redirects back to prelogin
                }
                case "create" -> handleCreateGame(parts);
                case "list" -> handleListGames();
                case "play" -> handleJoinGame(parts, false);
                case "observe" -> handleJoinGame(parts, true);
                case "quit" -> {
                    System.out.println("Goodbye!");
                    return false;
                }
                default -> System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                create <NAME> - create a new game
                list - list all games
                play <#> <WHITE|BLACK> - join a game as a player
                observe <#> - observe a game
                logout - log out
                quit - exit the program
                help - show this menu
                """);
    }

    private void handleLogout() {
        try {
            facade.logout(auth.authToken());
            System.out.println("Logged out.");
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private void handleCreateGame(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: create <NAME>");
            return;
        }
        String gameName = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        try {
            int gameID = facade.createGame(auth.authToken(), gameName);
            System.out.println("Created game '" + gameName + "'.");
        } catch (Exception e) {
            System.out.println("Could not create game: " + e.getMessage());
        }
    }

    private List<GameData> handleListGames() {
        try {
            Collection<GameData> games = facade.listGames(auth.authToken());
            List<GameData> gameList = new ArrayList<>(games);
            if (gameList.isEmpty()) {
                System.out.println("No games available. Create one with 'create <NAME>'.");
            } else {
                System.out.println("Games:");
                for (int i = 0; i < gameList.size(); i++) {
                    GameData g = gameList.get(i);
                    String white = g.whiteUsername() != null ? g.whiteUsername() : "(open)";
                    String black = g.blackUsername() != null ? g.blackUsername() : "(open)";
                    System.out.printf("  %d. %-20s  WHITE: %-15s  BLACK: %s%n",
                            i + 1, g.gameName(), white, black);
                }
            }
            return gameList;
        } catch (Exception e) {
            System.out.println("Could not retrieve games: " + e.getMessage());
            return List.of();
        }
    }

    private void handleJoinGame(String[] parts, boolean observe) {
        List<GameData> games;
        try {
            games = new ArrayList<>(facade.listGames(auth.authToken()));
        } catch (Exception e) {
            System.out.println("Could not retrieve games: " + e.getMessage());
            return;
        }

        if (observe) {
            if (parts.length < 2) {
                System.out.println("Usage: observe <#>");
                return;
            }
            int index = parseGameIndex(parts[1], games.size());
            if (index < 0) return;
            GameData game = games.get(index);
            System.out.println("Observing game: " + game.gameName());
            BoardDrawer.draw(null); // null = observer on white perspective
        } else {
            if (parts.length < 3) {
                System.out.println("Usage: play <#> <WHITE|BLACK>");
                return;
            }
            int index = parseGameIndex(parts[1], games.size());
            if (index < 0) return;

            String color = parts[2].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Color must be WHITE or BLACK.");
                return;
            }

            GameData game = games.get(index);
            try {
                facade.joinGame(auth.authToken(), game.gameID(), color);
                System.out.println("Joined game '" + game.gameName() + "' as " + color + ".");
                BoardDrawer.draw(color);
            } catch (Exception e) {
                System.out.println("Could not join game: " + e.getMessage());
            }
        }
    }

    /** parses a 1-based game number entered by the user; returns -1 on bad input */
    private int parseGameIndex(String token, int size) {
        try {
            int num = Integer.parseInt(token);
            if (num < 1 || num > size) {
                System.out.println("Please enter a number between 1 and " + size + ".");
                return -1;
            }
            return num - 1; // convert to 0-based
        } catch (NumberFormatException e) {
            System.out.println("'" + token + "' is not a valid game number.");
            return -1;
        }
    }
}