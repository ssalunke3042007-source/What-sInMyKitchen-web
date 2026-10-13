package com.whatsinmykitchen.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.whatsinmykitchen.util.HttpUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StaticFileHandler implements HttpHandler {
    private final File publicDirectory;
    private final Map<String, String> mimeTypes = new HashMap<>();

    public StaticFileHandler(String publicDirectoryName) {
        this.publicDirectory = new File(System.getProperty("user.dir"), publicDirectoryName);
        mimeTypes.put(".html", "text/html");
        mimeTypes.put(".css", "text/css");
        mimeTypes.put(".js", "application/javascript");
        mimeTypes.put(".json", "application/json");
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".gif", "image/gif");
        mimeTypes.put(".svg", "image/svg+xml");
        mimeTypes.put(".ico", "image/x-icon");
        mimeTypes.put(".webp", "image/webp");
        mimeTypes.put(".txt", "text/plain");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            HttpUtil.sendError(exchange, 405, "METHOD_NOT_ALLOWED", "This endpoint only supports GET and HEAD.");
            return;
        }

        if (path == null || path.isBlank()) {
            path = "/";
        }
        String safePath = path;
        if (safePath.contains("..")) {
            HttpUtil.sendError(exchange, 403, "INVALID_PATH", "Path traversal is not allowed.");
            return;
        }

        File targetFile;
        if ("/".equals(safePath) || safePath.isEmpty()) {
            targetFile = new File(publicDirectory, "index.html");
        } else {
            String normalized = safePath.startsWith("/") ? safePath.substring(1) : safePath;
            File candidate = new File(publicDirectory, normalized);
            if (!isWithinPublicDirectory(candidate)) {
                HttpUtil.sendError(exchange, 403, "INVALID_PATH", "Path traversal is not allowed.");
                return;
            }
            targetFile = candidate;
        }

        if (!targetFile.exists() || !targetFile.isFile()) {
            HttpUtil.sendError(exchange, 404, "NOT_FOUND", "The requested file was not found.");
            return;
        }

        String fileName = targetFile.getName();
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')).toLowerCase() : "";
        String contentType = mimeTypes.getOrDefault(extension, "application/octet-stream");
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://fonts.gstatic.com; script-src 'self' 'unsafe-inline'; img-src 'self' https: data:; font-src 'self' data: https://fonts.gstatic.com; connect-src 'self'; base-uri 'self'; form-action 'self';");

        long length = targetFile.length();
        exchange.sendResponseHeaders(200, "HEAD".equalsIgnoreCase(method) ? -1 : length);

        if ("HEAD".equalsIgnoreCase(method)) {
            return;
        }

        try (FileInputStream in = new FileInputStream(targetFile);
             OutputStream out = exchange.getResponseBody()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    private boolean isWithinPublicDirectory(File candidate) {
        try {
            Path publicPath = publicDirectory.toPath().toRealPath();
            Path candidatePath = candidate.toPath().toRealPath();
            return candidatePath.startsWith(publicPath);
        } catch (IOException ex) {
            return false;
        }
    }
}
