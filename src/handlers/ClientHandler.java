package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import utils.Logger;
import http.HttpResponse;
//import handlers.GetHandler;
//import handlers.PostHandler;

public class ClientHandler {

    /**
     * @param socket
     */
    public static void handleClient(Socket socket) {

        try (socket) {
            // Socket client = socket; // Auto-close socket

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            OutputStream out = socket.getOutputStream();
            {

                String requestLine = in.readLine();

                System.out.println("Request: " + requestLine);

                if (requestLine == null) {
                    Logger.log("WARNING: Empty or invalid request received");
                    return;
                }
                Logger.log("Incoming request: " + requestLine);

                if (requestLine.length() > 2048) {
                    HttpResponse.sendResponse(out, "400 Bad Request", "Request too long");
                    return;
                }

                String[] parts = requestLine.split(" ");

                if (parts.length < 2) {
                    HttpResponse.sendResponse(out, "400 Bad Request", "Invalid request format");
                    Logger.log("ERROR: Malformed request → " + requestLine);
                    return;
                }

                socket.setSoTimeout(5000);

                String method = parts[0];
                String path = parts[1];

                if (method.equals("GET")) {
                    GetHandler.handleGet(path, out);
                }

                // else if (method.equals("POST")) {
                // PostHandler.handlePost(in, out);
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
