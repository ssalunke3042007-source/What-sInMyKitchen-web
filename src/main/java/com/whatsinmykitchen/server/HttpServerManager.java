package com.whatsinmykitchen.server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class HttpServerManager {
    private HttpServerManager() {
    }

    public static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));
        Router.registerRoutes(server);
        return server;
    }
}
