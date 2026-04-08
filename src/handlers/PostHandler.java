package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import utils.Logger;
import http.HttpResponse;

import utils.FileManager;
import utils.Sanitizer;

// Post request handler with security checks
public class PostHandler {
    public static void handlePost(BufferedReader in, OutputStream out) throws IOException {

        try {

            int contentLength = 0;
            String line;

            // Read headers to find Content-Length
            while (!(line = in.readLine()).isEmpty()) {

                if (line.startsWith("Content-Length:")) {

                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            // Limit size (DoS protection)
            if (contentLength > 1024) {

                HttpResponse.sendResponse(out, "413 Payload Too Large", "Too much data");
                return;
            }

            // Read POST body
            char[] body = new char[contentLength];

            in.read(body, 0, contentLength);

            String postData = new String(body);
            // Decode first
            postData = URLDecoder.decode(postData, "UTF-8");

            // Step 2: Log raw (optional but good)
            Logger.log("POST raw data: " + postData);

            Logger.log("POST request received");

            // Validate input
            String safeData = Sanitizer.sanitizeInput(postData);

            Logger.log("POST data (sanitised): " + safeData);

            // Store safely
            FileManager.saveToFile(safeData);

            Logger.log("POST data saved successfully");

            // HttpResponse.sendResponse(out,"200 OK","Form submitted securely!");

            String responseBody = "<h1>Form submitted successfully</h1>" +
            "<a href=\"/\">Go back</a>";

            HttpResponse.sendResponse(out, "200 OK", responseBody);

        }

        catch (Exception e) {

            Logger.log("ERROR (POST): " + e.getMessage());

            HttpResponse.sendResponse(out, "500 Internal Server Error", "Server error");
        }
    }

}
