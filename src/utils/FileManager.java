package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import http.HttpResponse;

public class FileManager {
    // Root directory where the server serves files
    private static final String ROOT_DIR = "www";

    public static void saveToFile(String data) {

        File file = new File(ROOT_DIR + "data.txt");

        try (
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(data);
            bw.newLine();

        } catch (IOException e) {
            Logger.log("ERROR: Failed to save data → " + e.getMessage());
        }
    }

    public static byte[] readFile(File file) throws IOException {

        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    public static File getSafeFile(String path) throws IOException {

        File file = FileManager.getSafeFile(path);

        if (file == null || !file.exists()) {
            return HttpResponse.forbidden();
        }

        File root = new File(ROOT_DIR).getCanonicalFile();
        File requestedFile = new File(root, path).getCanonicalFile();

        if (!requestedFile.getCanonicalPath().startsWith(root.getCanonicalPath())) {
            return null;
        }
        Logger.log("Accessing file: " + requestedFile.getPath());

        return requestedFile;
        
    }

}
