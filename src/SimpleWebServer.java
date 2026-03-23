import java.io.*;
import java.net.*;


public class SimpleWebServer {
    private static final String ROOT_DIR = "www";

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
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            System.out.println("Request: " + requestLine);

            if (requestLine == null) return;

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            if (method.equals("GET")) {
                handleGet(path, out);
            } else {
                sendResponse(out, "501 Not Implemented", "Method not supported");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleGet(String path, OutputStream out) throws IOException {
        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File("www" + path);

        if (file.exists() && !file.isDirectory()) {
            byte[] content = readFile(file);
            sendResponse(out, "200 OK", content);
        } else {
            sendResponse(out, "404 Not Found", "File not found");
        }
    }

    private static byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return fis.readAllBytes();
    }

    private static void sendResponse(OutputStream out, String status, String message) throws IOException {
        String response =
                "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;

        out.write(response.getBytes());
        out.flush();
    }

    private static void sendResponse(OutputStream out, String status, byte[] content) throws IOException {
        String headers =
                "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "\r\n";

        out.write(headers.getBytes());
        out.write(content);
        out.flush();
    }
}