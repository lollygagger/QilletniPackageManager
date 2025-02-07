package max.burdett.qpm.command.fetch;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import is.yarr.qilletni.api.lib.qll.QilletniInfoData;

public class DependencyDownloader {

    private static final String API_URL = "";

    /**
     * downloadPackage takes in a QilletniInfoData dependency and downloads
     * @param dependency
     * @param saveDir
     * @throws IOException
     */
    public static void downloadPackage(QilletniInfoData dependency, Path saveDir) throws IOException {
        // Send the request to the API to get the signed URL
        String signedUrl = getSignedUrlFromApi(dependency);

        if (signedUrl != null) {
            // Create the target directory path
            Path targetDir = Paths.get(saveDir.toString(), dependency.name(), dependency.version().getVersionString());
            Files.createDirectories(targetDir);  // Ensure the directory exists

            // Download the package using the signed URL
            downloadFile(signedUrl, targetDir.resolve(dependency.name() + "-" + dependency.version().getVersionString() + ".pkg").toString());
        } else {
            System.out.println("No signed URL received from the API.");
        }
    }

    private static String getSignedUrlFromApi(QilletniInfoData dependency) throws IOException {
        // Create URL object from the API endpoint
        URL url = new URL(API_URL + "?package=" + dependency.name() + "&version=" + dependency.version().getVersionString());

        // Send the HTTP GET request
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept", "application/json");

        // Check for successful response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Parse the JSON response (simplified here for demonstration)
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                 BufferedReader in = new BufferedReader(reader)) {

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // Assuming the response JSON contains a field called "signedUrl"
                String responseJson = response.toString();

                return extractSignedUrl(responseJson);
            }
        } else {
            System.out.println("API request failed with response code: " + responseCode);
            return null;
        }
    }

    private static String extractSignedUrl(String responseJson) {
        // Use Gson to parse the JSON response
        JsonObject jsonObject = JsonParser.parseString(responseJson).getAsJsonObject();

        // Assuming the response JSON contains a field "signedUrl"
        if (jsonObject.has("signedUrl")) {
            return jsonObject.get("signedUrl").getAsString();
        }

        return null;
    }

    private static void downloadFile(String fileUrl, String targetPath) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Open an input stream to the file
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(targetPath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("Package downloaded successfully to: " + targetPath);
        }
    }
}
