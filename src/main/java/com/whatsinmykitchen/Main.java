package com.whatsinmykitchen;

import com.sun.net.httpserver.HttpServer;
import com.whatsinmykitchen.server.HttpServerManager;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServerManager.createServer(port);
            server.start();
            System.out.println("What’sInMyKitchen server started on http://0.0.0.0:" + port);
        } catch (IOException ex) {
            System.err.println("Failed to start HTTP server: " + ex.getMessage());
            System.exit(1);
        } catch (NumberFormatException ex) {
            System.err.println("PORT must be a valid integer value.");
            System.exit(1);
        }
    }
}
