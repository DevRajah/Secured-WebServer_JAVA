import java.io.*;
import java.net.*;

public class SimpleWebServer {
    private static final String ROOT_DIR = "www";
    private static final String LOG_FILE = "server.log";

    public static void main(String[] args) {
        int port = 9090;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Handle each client in a new thread
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            log("ERROR: " + e.getMessage());
        }
    }

    //Logging 
    private static void log(String message) {

    String timestamp = java.time.LocalDateTime.now().toString();
    String logEntry = "[" + timestamp + "] " + message;

    System.out.println(logEntry); // also print to console

    try (FileWriter fw = new FileWriter(LOG_FILE, true);
         BufferedWriter bw = new BufferedWriter(fw)) {

        bw.write(logEntry);
        bw.newLine();

    } catch (IOException e) {
        System.out.println("Logging failed: " + e.getMessage());
    }
}
    //handling client
    private static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream out = socket.getOutputStream()) {
            String requestLine = in.readLine();
            System.out.println("Request: " + requestLine);

            if (requestLine == null)
                return;
            if (requestLine.length() > 2048) {
                sendResponse(out, "400 Bad Request", "Request too long");
                return;
            }
            log("Incoming request: " + requestLine);

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            if (method.equals("GET")) {
                handleGet(path, out);
            } else if (method.equals("POST")) {
                handlePost(in, out);
            } else {
                sendResponse(out, "405 Method Not Allowed", "Invalid method");
            }

        } catch (IOException e) {
            log("ERROR: " + e.getMessage());
        }
    }
    private static String sanitizeInput(String input) {

    // Remove dangerous characters
    input = input.replaceAll("<", "");
    input = input.replaceAll(">", "");

    // Remove directory traversal patterns
    input = input.replaceAll("\\.\\.=.", "");

    // Limit length
    if (input.length() > 500) {
        input = input.substring(0, 500);
    }

    return input;
}

private static void saveToFile(String data) {

    File file = new File("www/data.txt");

    try (FileWriter fw = new FileWriter(file, true);
         BufferedWriter bw = new BufferedWriter(fw)) {

        bw.write(data);
        bw.newLine();

    } catch (IOException e) {
        System.out.println("Error saving data: " + e.getMessage());
    }
}
//Post request handler with security checks
private static void handlePost(BufferedReader in, OutputStream out) throws IOException {

    int contentLength = 0;
    String line;

    //Read headers to find Content-Length
    while (!(line = in.readLine()).isEmpty()) {
        if (line.startsWith("Content-Length:")) {
            contentLength = Integer.parseInt(line.split(":")[1].trim());
        }
    }

    //Limit size (DoS protection)
    if (contentLength > 1024) {
        sendResponse(out, "413 Payload Too Large", "Too much data");
        return;
    }

    //Read POST body
    char[] body = new char[contentLength];
    in.read(body, 0, contentLength);

    String postData = new String(body);
    log("POST request received");

    //Validate input
    String safeData = sanitizeInput(postData);
    log("POST data (sanitised): " + safeData);

    //Store safely
    saveToFile(safeData);
    log("POST data saved successfully");

    sendResponse(out, "200 OK", "Form submitted securely!");

}


   

    //Get request handler with security checks
    private static void handleGet(String path, OutputStream out) throws IOException {

        if (path.equals("/")) {
            path = "/index.html";
        }

        // Remove query parameters (e.g. ?name=abc)
        path = path.split("\\?")[0];

        // Prevent null bytes or weird tricks
        path = path.replace("\0", "");

        // Normalize path to prevent traversal
        File root = new File(ROOT_DIR).getCanonicalFile();
        File requestedFile = new File(root, path).getCanonicalFile();

        // SECURITY CHECK: ensure file is inside ROOT_DIR
        if (!requestedFile.getPath().startsWith(root.getPath())) {
    log("SECURITY: Blocked directory traversal attempt → " + path);
    sendResponse(out, "403 Forbidden", "Access denied");
    return;
}

        if (requestedFile.exists() && !requestedFile.isDirectory()) {
            byte[] content = readFile(requestedFile);
            sendResponse(out, "200 OK", content);
             log("GET " + path + " → 200 OK");
        } else {
            sendResponse(out, "404 Not Found", "File not found");
            log("GET " + path + " → 404 Not Found");
        }
        
    }

    // private static void handleGet(String path, OutputStream out) throws
    // IOException {
    // if (path.equals("/")) {
    // path = "/index.html";
    // }

    // File file = new File("www" + path);

    // if (file.exists() && !file.isDirectory()) {
    // byte[] content = readFile(file);
    // sendResponse(out, "200 OK", content);
    // } else {
    // sendResponse(out, "404 Not Found", "File not found");
    // }
    // }

    private static byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return fis.readAllBytes();
    }

    private static void sendResponse(OutputStream out, String status, String message) throws IOException {
        String response = "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;

        out.write(response.getBytes());
        out.flush();
    }

    private static void sendResponse(OutputStream out, String status, byte[] content) throws IOException {
        String headers = "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "\r\n";

        out.write(headers.getBytes());
        out.write(content);
        out.flush();
    }
}