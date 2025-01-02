import java.io.FileReader;
import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShamirSecretSharing {

    public static void main(String[] args) throws Exception {
        // Load the JSON file
        File file = new File("input.json");
        Scanner scanner = new Scanner(new FileReader(file));
        StringBuilder jsonData = new StringBuilder();

        while (scanner.hasNextLine()) {
            jsonData.append(scanner.nextLine());
        }
        scanner.close();

        // Parse JSON manually
        String inputJson = jsonData.toString();
        Map<String, Object> jsonMap = parseNestedJson(inputJson);

        // Extract keys n and k
        @SuppressWarnings("unchecked")
        Map<String, Object> keys = (Map<String, Object>) jsonMap.get("keys");
        if (keys == null) {
            throw new IllegalArgumentException("Keys object not found in JSON.");
        }

        int n = Integer.parseInt(keys.get("n").toString().trim()); // Convert to int
        int k = Integer.parseInt(keys.get("k").toString().trim()); // Convert to int

        // Parse roots
        Map<BigInteger, BigInteger> points = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            if (entry.getKey().equals("keys")) continue;

            try {
                BigInteger x = new BigInteger(entry.getKey().trim());
                @SuppressWarnings("unchecked")
                Map<String, Object> pointData = (Map<String, Object>) entry.getValue();

                int base = Integer.parseInt(pointData.get("base").toString().trim());
                BigInteger y = new BigInteger(pointData.get("value").toString().trim(), base);

                points.put(x, y);
            } catch (Exception e) {
                // Skip invalid or non-root entries
                System.out.println("Skipping invalid entry: " + entry.getKey());
            }
        }

        // Find the constant term using Lagrange Interpolation
        BigInteger constant = findConstant(points, k);
        System.out.println("The secret (constant term c) is: " + constant);
    }

    private static BigInteger findConstant(Map<BigInteger, BigInteger> points, int k) {
        BigInteger result = BigInteger.ZERO;
        BigInteger[] xValues = points.keySet().toArray(new BigInteger[0]);
        BigInteger[] yValues = points.values().toArray(new BigInteger[0]);

        for (int i = 0; i < k; i++) {
            BigInteger xi = xValues[i];
            BigInteger yi = yValues[i];

            BigInteger term = yi;
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = xValues[j];
                    term = term.multiply(xj.negate()).divide(xi.subtract(xj));
                }
            }
            result = result.add(term);
        }

        return result;
    }

    private static Map<String, Object> parseNestedJson(String json) {
        Map<String, Object> result = new HashMap<>();

        json = json.trim().substring(1, json.length() - 1).trim(); // Remove outer {}
        String[] entries = json.split("(?<!\\\\),"); // Split by ',' but ignore escaped ','

        for (String entry : entries) {
            String[] keyValue = entry.split(":", 2);
            if (keyValue.length < 2) continue; // Skip invalid entries

            String key = keyValue[0].trim().replaceAll("\"", "");
            String value = keyValue[1].trim();

            if (value.startsWith("{")) {
                // Nested JSON object
                result.put(key, parseNestedJson(value));
            } else if (value.matches("^\\d+$")) {
                // Integer value
                result.put(key, Integer.parseInt(value));
            } else if (value.startsWith("\"") && value.endsWith("\"")) {
                // String value
                result.put(key, value.substring(1, value.length() - 1));
            } else {
                // Fallback to raw string
                result.put(key, value);
            }
        }

        return result;
    }
}
