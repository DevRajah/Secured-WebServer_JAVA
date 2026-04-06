package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import config.ServerConfig;
import handlers.ClientHandler;
import utils.Logger;





public class SimpleWebServer {



    public static void main(String[] args) {
       

        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {
            System.out.println("Server running on port " + ServerConfig.PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Handle each client in a new thread
                new Thread(() -> ClientHandler.handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            Logger.log("ERROR: " + e.getMessage());
        }
    }
}


