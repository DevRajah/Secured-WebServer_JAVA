package handlers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import utils.Logger;
import http.HttpResponse;
import config.ServerConfig;
import utils.FileManager;
import utils.Sanitizer;

// Get request handler with security checks
public class GetHandler {
    public static void handleGet(String path, OutputStream out) throws IOException {

        try {

            // Handle GET form submission
            if (path.startsWith("/submit?")) {

                String query = path.split("\\?", 2)[1];

                // Decode URL
                query = URLDecoder.decode(query, "UTF-8");

                Logger.log("GET raw data: " + query);

                // Sanitize
                String safeData = Sanitizer.sanitizeInput(query);

                // Parse form data
                String[] pairs = safeData.split("&");

                String name = "";
                String message = "";

                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);

                    if (keyValue.length == 2) {
                        if (keyValue[0].equals("name")) {
                            name = keyValue[1].trim();
                        } else if (keyValue[0].equals("message")) {
                            message = keyValue[1].trim();
                        }
                    }
                }

                // Validate structure
                if (name.contains("=") || message.contains("=")) {
                    Logger.log("SECURITY: Suspicious GET input → " + safeData);
                    HttpResponse.sendResponse(out, "400 Bad Request", "Invalid input structure");
                    return;
                }

                // Validate characters
                if (!name.matches("[a-zA-Z0-9 ]*") || !message.matches("[a-zA-Z0-9 ]*")) {
                    Logger.log("SECURITY: Invalid GET characters detected");
                    HttpResponse.sendResponse(out, "400 Bad Request", "Invalid input format");
                    return;
                }

                // Validate empty
                if (name.isEmpty() || message.isEmpty()) {
                    Logger.log("SECURITY: Empty GET fields detected");
                    HttpResponse.sendResponse(out, "400 Bad Request", "All fields are required");
                    return;
                }

                // Validate length
                if (name.length() > 50 || message.length() > 200) {
                    Logger.log("SECURITY: GET input too long");
                    HttpResponse.sendResponse(out, "400 Bad Request", "Input too long");
                    return;
                }

                Logger.log("GET data (sanitised): " + safeData);

                // Save
                FileManager.saveToFile(safeData);

                Logger.log("GET data saved successfully");

                // Redirect to success page
                String response = "HTTP/1.1 302 Found\r\n" +
                        "Location: /success.html\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";

                out.write(response.getBytes());
                out.flush();

                return;
            }

            if (path.equals("/")) {
                path = "/index.html";
            }

            // Prevent accessing POST endpoint via GET
            if (path.equals("/submit")) {
                HttpResponse.sendResponse(out, "405 Method Not Allowed", "Use POST for /submit");
                return;
            }

            // Remove query parameters (normal GET)
            path = path.split("\\?")[0];

            // Block null byte attack
            if (path.contains("\0")) {
                Logger.log("SECURITY: Null byte attack detected → " + path);
                HttpResponse.sendResponse(out, "400 Bad Request", "Invalid request");
                return;
            }

            // Limit URL length
            if (path.length() > 2048) {
                HttpResponse.sendResponse(out, "414 URI Too Long", "Request too long");
                return;
            }

            // Normalize path
            File root = new File(ServerConfig.ROOT_DIR).getCanonicalFile();
            File requestedFile = new File(root, path).getCanonicalFile();

            // Directory traversal protection
            if (!requestedFile.getCanonicalPath().startsWith(root.getCanonicalPath())) {
                Logger.log("SECURITY: Blocked directory traversal attempt → " + path);
                HttpResponse.sendResponse(out, "403 Forbidden", "Access denied");
                return;
            }

            // Serve file
            if (requestedFile.exists() && !requestedFile.isDirectory()) {

                byte[] content = FileManager.readFile(requestedFile);
                HttpResponse.sendResponse(out, "200 OK", content);

                Logger.log("GET " + path + " → 200 OK");
            } else {
                HttpResponse.sendResponse(out, "404 Not Found", "File not found");
                Logger.log("GET " + path + " → 404 Not Found");
            }

        } catch (IOException e) {

            Logger.log("ERROR (GET): " + e.getMessage());
            HttpResponse.sendResponse(out, "500 Internal Server Error", "Server error");
        }
    }
}