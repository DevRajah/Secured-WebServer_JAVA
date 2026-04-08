package http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class HttpResponse {
    //send text response
    public static void sendResponse(OutputStream out,String status, String body)
            throws IOException {

        String contentType = "text/plain";

        // Detect HTML automatically
        if (body != null && body.trim().startsWith("<")) {
            contentType = "text/html";
        }

                
        String response =
                "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        out.write(response.getBytes("UTF-8"));

        out.flush();
    }
    // Send file / binary response
    public static void sendResponse(OutputStream out,String status,byte[] content)
            throws IOException {

        String headers =
                "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "\r\n";

        out.write(headers.getBytes());

        out.write(content);

        out.flush();
    }

   
    // Optional helper for forbidden responses
    public static void forbidden(OutputStream out) throws IOException {
        sendResponse(out, "403 Forbidden", "Access denied");
    }
    
}


