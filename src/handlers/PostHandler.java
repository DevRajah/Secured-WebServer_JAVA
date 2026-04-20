package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            //Read headers to find Content-Length
            while ((line = in.readLine()) != null && !line.isEmpty()) {

                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            //Limit size (DoS protection)
            if (contentLength > 1024) {
                HttpResponse.sendResponse(out, "413 Payload Too Large", "Too much data");
                return;
            }

            //Read POST body
            //char[] body = new char[contentLength];
            //in.read(body, 0, contentLength);

            char[] body = new char[contentLength];

int totalRead = 0;
while (totalRead < contentLength) {
    int bytesRead = in.read(body, totalRead, contentLength - totalRead);
    if (bytesRead == -1) break;
    totalRead += bytesRead;
}

            String postData = new String(body);

            //Decode (handle encoding + double encoding attacks)
            postData = URLDecoder.decode(postData, "UTF-8");
            postData = URLDecoder.decode(postData, "UTF-8");

            Logger.log("POST raw data: " + postData);
            Logger.log("POST request received");

            //Detect completely malformed input
            if (!postData.contains("=") && !postData.trim().startsWith("{")) {
                Logger.log("SECURITY: Malformed POST data → " + postData);
                HttpResponse.sendResponse(out, "400 Bad Request", "Malformed request");
                return;
            }

            String name = "";
            String message = "";

            // JSON SUPPORT (for Postman testing)
            if (postData.trim().startsWith("{")) {

                Logger.log("INFO: JSON input detected");

                name = extractJsonValue(postData, "name");
                message = extractJsonValue(postData, "message");

            } else {

                //Sanitize ONLY for form data
                String safeData = Sanitizer.sanitizeInput(postData);

                //Parse form data
                String[] pairs = safeData.split("&");

                for (String pair : pairs) {

                    String[] keyValue = pair.split("=", 2);

                    //Detect malformed structure
                    if (keyValue.length != 2) {
                        Logger.log("SECURITY: Malformed input → " + pair);
                        HttpResponse.sendResponse(out, "400 Bad Request", "Malformed request");
                        return;
                    }

                    if (keyValue[0].equals("name")) {
                        name = keyValue[1].trim();
                    } else if (keyValue[0].equals("message")) {
                        message = keyValue[1].trim();
                    }
                }

                Logger.log("POST data (sanitised): " + safeData);
            }

            //Detect suspicious patterns
            if (name.contains("=") || message.contains("=")) {
                Logger.log("SECURITY: Suspicious input pattern detected → " + postData);
                HttpResponse.sendResponse(out, "400 Bad Request", "Invalid input structure");
                return;
            }

            //Validate characters FIRST
            if (!name.matches("[a-zA-Z0-9 ]*") || !message.matches("[a-zA-Z0-9 ]*")) {
                Logger.log("SECURITY: Invalid characters detected");
                HttpResponse.sendResponse(out, "400 Bad Request", "Invalid input format");
                return;
            }

            //Validate empty fields
            if (name.isEmpty() || message.isEmpty()) {
                Logger.log("SECURITY: Empty fields detected");
                HttpResponse.sendResponse(out, "400 Bad Request", "All fields are required");
                return;
            }

            //Validate length
            if (name.length() > 50 || message.length() > 200) {
                Logger.log("SECURITY: Input too long");
                HttpResponse.sendResponse(out, "400 Bad Request", "Input too long");
                return;
            }

            //Store safely
            FileManager.saveToFile("name=" + name + "&message=" + message);
            Logger.log("POST data saved successfully");

            //Redirect to success page
            String response =
                    "HTTP/1.1 302 Found\r\n" +
                    "Location: /success.html\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(response.getBytes());
            out.flush();

        } catch (Exception e) {

            Logger.log("ERROR (POST): " + e.getMessage());
            HttpResponse.sendResponse(out, "500 Internal Server Error", "Server error");
        }
    }

    //Simple JSON extractor
    private static String extractJsonValue(String json, String key) {

        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(json);

        if (m.find()) {
            return m.group(1);
        }

        return "";
    }
}