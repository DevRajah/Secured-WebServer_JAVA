package handlers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import utils.Logger;
import http.HttpResponse;
import config.ServerConfig;
import utils.FileManager;

// Get request handler with security checks
public class GetHandler {
    public static void handleGet(String path, OutputStream out) throws IOException {

        try {

            if (path.equals("/")) {
                path = "/index.html";
            }
            // Prevent accessing POST endpoint via GET
            if (path.equals("/submit")) {
                HttpResponse.sendResponse(out, "405 Method Not Allowed", "Use POST for /submit");
                return;
            }

            // Remove query parameters (e.g. ?name=abc)
            path = path.split("\\?")[0];
            //Block null byte attack (do NOT sanitize silently)
            if (path.contains("\0")) {
                Logger.log("SECURITY: Null byte attack detected → " + path);
                HttpResponse.sendResponse(out, "400 Bad Request", "Invalid request");
                return;
            }

            //Limit URL length (DoS protection)
            if (path.length() > 2048) {
                HttpResponse.sendResponse(out, "414 URI Too Long", "Request too long");
                return;
            }

            // Normalize path to prevent traversal
            File root = new File(ServerConfig.ROOT_DIR).getCanonicalFile();

            File requestedFile = new File(root, path).getCanonicalFile();

            // 🚨 SECURITY CHECK: ensure file is inside ROOT_DIR
            if (!requestedFile.getCanonicalPath().startsWith(root.getCanonicalPath())) {

                Logger.log("SECURITY: Blocked directory traversal attempt → " + path);
                HttpResponse.sendResponse(out, "403 Forbidden", "Access denied");
                return;
            }

            // Serve file if exists
            if (requestedFile.exists() && !requestedFile.isDirectory()) {

                byte[] content = FileManager.readFile(requestedFile);
                HttpResponse.sendResponse(out, "200 OK", content);

                Logger.log("GET " + path + " → 200 OK");
            }

            else {

                HttpResponse.sendResponse(out, "404 Not Found", "File not found");

                Logger.log("GET " + path + " → 404 Not Found");
            }

        }

        catch (IOException e) {

            Logger.log("ERROR (GET): " + e.getMessage());

            HttpResponse.sendResponse(out, "500 Internal Server Error", "Server error");
        }
    }

}
