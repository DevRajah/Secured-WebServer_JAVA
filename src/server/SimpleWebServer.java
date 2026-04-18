package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.ServerConfig;
import handlers.ClientHandler;
import utils.Logger;





public class SimpleWebServer {
     private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    



    public static void main(String[] args) {
       

        try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {
            System.out.println("Server running on port " + ServerConfig.PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            threadPool.shutdown();
            System.out.println("Server shutting down...");
        }));

            while (true) {
                Socket clientSocket = serverSocket.accept();


                
                // Handle each client in a thread pool for better performance
                //new Thread(() -> ClientHandler.handleClient(clientSocket)).start();
               
                threadPool.execute(() -> ClientHandler.handleClient(clientSocket));

            }

        } catch (IOException e) {
            Logger.log("ERROR: " + e.getMessage());
        }
    }
}


