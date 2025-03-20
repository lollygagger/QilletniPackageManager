package max.burdett.qpm.command.fetch;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DependencyDownloader {

    private static final String API_URL = "";

    private static final OkHttpClient client = new OkHttpClient();


    /**
     * downloadPackage takes in a ExactDependency dependency and downloads it
     * @param dependency
     * @param saveDir
     * @throws IOException
     */
    public static void downloadPackage(ExactDependency dependency, Path saveDir) throws IOException {
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

    /**
     * getSignedUrlFromApi generates signed URLs for downloading packages from R2
     * @param dependency the ExactDependency to generate a signed URL for downloading
     * @return a signed URL which can be used to download the ExactDependency
     * @throws IOException
     */
    private static String getSignedUrlFromApi(ExactDependency dependency) throws IOException {
        // Create URL object from the API endpoint
        String url = API_URL + "?package=" + dependency.name() + "&version=" + dependency.version().getVersionString();

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        // Send the HTTP GET request
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Read the response body as a string
                String responseJson = response.body().string();

                // Extract and return the signed URL from the response JSON
                return extractSignedUrl(responseJson);
            } else {
                System.out.println("API request failed with response code: " + response.code());
                return null;
            }
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
