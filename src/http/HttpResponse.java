package http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class HttpResponse {
    public static void sendResponse(OutputStream out,String status,String message)
            throws IOException {

        String response =
                "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;

        out.write(response.getBytes());

        out.flush();
    }

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

    public static File forbidden() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'forbidden'");
    }
    
}

