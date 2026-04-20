package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.Logger;
import http.HttpResponse;

public class ClientHandler {

    /**
     * @param socket
     */
    // Thread pool (shared across all clients)
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void handleClient(Socket socket) {

        try (socket) {
            String clientIP = socket.getInetAddress().getHostAddress();
            // Socket client = socket;

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            OutputStream out = socket.getOutputStream();
            {

                String requestLine = in.readLine();
                // Handle empty or malformed requests
                if (requestLine == null || requestLine.trim().isEmpty()) {
                    Logger.log("WARNING: Empty or invalid request received" + clientIP);
                    HttpResponse.sendResponse(out, "400 Bad Request", "Empty request");
                    return;
                }

                // System.out.println("Request: " + requestLine);

                // if (requestLine == null) {
                // Logger.log("WARNING: Empty or invalid request received");
                // return;
                // }
                Logger.log("Incoming request from " + clientIP + ": " + requestLine);
                // Basic DoS protection: limit request line length
                if (requestLine.length() > 2048) {
                    HttpResponse.sendResponse(out, "400 Bad Request", "Request too long");
                    return;
                }

                String[] parts = requestLine.split(" ");

                if (parts.length < 2) {
                    Logger.log("ERROR: Malformed request → " + clientIP + ": " + requestLine);
                    HttpResponse.sendResponse(out, "400 Bad Request", "Invalid request");
                    return;
                }

                socket.setSoTimeout(5000);

                String method = parts[0];
                String path = parts[1];

                if (method.equals("GET")) {
                    GetHandler.handleGet(path, out);
                }

            
                // Post handled in thread pool for Isolation
                // else if (method.equals("POST") && path.equals("/submit")) {

                //     threadPool.execute(() -> {
                //         try {
                //             PostHandler.handlePost(in, out);
                //         } catch (IOException e) {
                //             Logger.log("ERROR (POST task) from " + clientIP + ": " + e.getMessage());
                //         }
                //     });
                // }
                
                else if (method.equals("POST") && path.equals("/submit")) {
                    PostHandler.handlePost(in, out);
                }

                else {
                    HttpResponse.sendResponse(out, "405 Method Not Allowed", "Invalid method");
                }
            }

        } catch (IOException e) {
            Logger.log("ERROR (client handling): " + e.getMessage());
        }
    }
}
