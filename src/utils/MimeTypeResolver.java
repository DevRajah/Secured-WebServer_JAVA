package utils;

public class MimeTypeResolver {

    public static String getMimeType(String fileName) {

        fileName = fileName.toLowerCase();

        if (fileName.endsWith(".html") || fileName.endsWith(".htm"))
            return "text/html";

        if (fileName.endsWith(".css"))
            return "text/css";

        if (fileName.endsWith(".js"))
            return "application/javascript";

        if (fileName.endsWith(".json"))
            return "application/json";

        if (fileName.endsWith(".png"))
            return "image/png";

        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return "image/jpeg";

        if (fileName.endsWith(".gif"))
            return "image/gif";

        if (fileName.endsWith(".svg"))
            return "image/svg+xml";

        if (fileName.endsWith(".txt"))
            return "text/plain";

        return "application/octet-stream";
    }
}