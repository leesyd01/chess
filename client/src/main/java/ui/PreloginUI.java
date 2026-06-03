package ui;

import client.ServerFacade;
import model.AuthData;
import java.util.Scanner;

public class PreloginUI {
    private final ServerFacade facade;
    private final Scanner scanner;

    public PreloginUI(ServerFacade facade, Scanner scanner) {
        this.facade = facade;
        this.scanner = scanner;
    }

    /** runs prelogin REPL. returns AuthData when user logs in or null to quit */

    public AuthData run() {
        System.out.println("Welcome to 240 Chess! Type 'help' to get started.");
        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch(cmd) {
                case "help" -> printHelp();
                case "quit" -> {
                    System.out.println("Goodbye!");
                    return null;
                }
                case "login" -> {
                    AuthData auth = handleLogin();
                    if (auth != null) return auth;
                }
                case "register" -> {
                    AuthData auth = handleRegister();
                    if (auth != null) return auth;
                }
                default -> System.out.println("Unknown command. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                register <USERNAME> <PASSWORD> <EMAIL> - create an account
                login <USERNAME> <PASSWORD> - log in to your account
                quit - exit the program
                help - show this menu
                """);
    }

    private AuthData handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        try {
            AuthData auth = facade.login(username, password);
            System.out.println("Logged in as " + auth.username() + ".");
            return auth;
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private AuthData handleRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        try {
            AuthData auth = facade.register(username, password, email);
            System.out.println("Registered and logged in as " + auth.username() + ".");
            return auth;
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }
}