package utils;

public class Sanitizer {
    public static String sanitizeInput(String input) {
          // store original before changes
        String original = input;

        // Remove dangerous characters
        input = input.replaceAll("<","");

        input = input.replaceAll(">","");

        // Remove directory traversal patterns
        input = input.replaceAll("\\.\\./","");

        // Limit length
        if (input.length() > 500) {

            input = input.substring(0,500);
        }

        // Detect if anything changed (attack attempt)
        if (!original.equals(input)) {
            Logger.log("SECURITY: Malicious input detected → " + original);
        }
        return input;
    }
    
}
