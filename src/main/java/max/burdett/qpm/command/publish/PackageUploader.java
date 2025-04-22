package max.burdett.qpm.command.publish;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import max.burdett.qpm.util.ExactDependency;
import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class PackageUploader {

    private static final String API_URL = "";

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Gets a pre-signed url for uploading a package from the signer/metadata API
     * @param dependency The dependency to generate a pre-signed url for uploading
     * @return a pre-signed URL as a {@link String}
     * @throws IOException
     */
    private static String getPresignedUrl(ExactDependency dependency) throws IOException {

        String apiUrl = API_URL + "/upload/" + dependency.name() + "/" + dependency.version() + "/";

        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code " + response.code());
            }
            String body = response.body().string();
            JsonObject json = new Gson().fromJson(body, JsonObject.class);
            return json.get("presignedUrl").getAsString();
        }
    }

    /**
     * Uploads a given file using a given pre-signed url
     * @param presignedUrl The pre-signed url to use to upload
     * @param file The (qll)file to be uploaded
     * @throws IOException
     */
    private static void uploadFile(String presignedUrl, File file) throws IOException {
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody fileBody = RequestBody.create(file, mediaType);

        Request request = new Request.Builder()
                .url(presignedUrl)
                .put(fileBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("File upload failed with response code " + response.code());
            }
        }
    }



}
