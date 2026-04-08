package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import config.ServerConfig;


public class Logger {
    // Log file where server activity is stored
private static final String LOG_FILE = ServerConfig.LOG_FILE;

    public static void log(String message) {

        String timestamp = java.time.LocalDateTime.now().toString();
        String logEntry = "[" + timestamp + "] " + message;

        System.out.println(logEntry); // also print to console

        try (
            FileWriter fw = new FileWriter(LOG_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw)
        ) {

            bw.write(logEntry);
            bw.newLine();

        } catch (IOException e) {
            System.out.println("Logging failed: " + e.getMessage());
        }
    }
    
}

