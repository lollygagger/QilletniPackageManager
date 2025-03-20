package max.burdett.qpm.command.fetch;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import is.yarr.qilletni.api.lib.qll.ComparableVersion;
import is.yarr.qilletni.api.lib.qll.QilletniInfoData;

import java.io.IOException;
import java.util.*;

public class DependencyResolver {
    private static final String API_URL = "";

    private static final OkHttpClient client = new OkHttpClient();

    private static final Gson gson = new Gson();

    private final Map<String, ComparableVersion> resolvedDependencies = new HashMap<>();

    private final Set<String> currentlyResolving = new HashSet<>();


    public void resolveDependencies(QilletniInfoData root) {
        for (QilletniInfoData.Dependency dep : root.dependencies()) {
            resolveDependency(dep.name(), dep.version());
        }
    }

    private void resolveDependency(String name, ComparableVersion requestedVersion) {
        // Check if the dependency has already been resolved
        if (resolvedDependencies.containsKey(name)) {
            // If the version is already resolved, check compatibility
            ComparableVersion existingVersion = resolvedDependencies.get(name);
            if (!existingVersion.permitsVersion(requestedVersion)) {
                throw new RuntimeException("Version conflict: " + name + " requires " + requestedVersion
                        + " but " + existingVersion + " is already resolved.");
            }
            return; // Already resolved and compatible
        }

        // Detect circular dependency
        if (currentlyResolving.contains(name)) {
            throw new RuntimeException("Circular dependency detected for package: " + name);
        }

        // Add this dependency to the stack of currently resolving dependencies
        currentlyResolving.add(name);

        // Fetch the dependency's sub-dependencies
        Set<QilletniInfoData.Dependency> subDependencies = fetchDependencySubDependencies(name, requestedVersion);

        // Find the best compatible version of the current dependency
        ComparableVersion bestVersion = findBestVersion(name, requestedVersion);
        resolvedDependencies.put(name, bestVersion);  // Mark the dependency as resolved

        // Recursively resolve sub-dependencies
        for (QilletniInfoData.Dependency subDep : subDependencies) {
            resolveDependency(subDep.name(), subDep.version());
        }

        // Remove this dependency from the stack of currently resolving dependencies
        currentlyResolving.remove(name);
    }

    private ComparableVersion findBestVersion(String name, ComparableVersion requested) {
        List<ComparableVersion> availableVersions = getAvailableVersions(name); // Assume this fetches available versions

        // Filter out versions that don't match the requested range
        return availableVersions.stream()
                .filter(requested::permitsVersion)
                .max(Comparator.naturalOrder()) // Pick the highest allowed version
                .orElseThrow(() -> new RuntimeException("No compatible version found for " + name));
    }

    private Set<QilletniInfoData.Dependency> fetchDependencySubDependencies(String name, ComparableVersion version) {
        String urlString = API_URL + "/info/dependencies/" + name + "/" + version;

        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            String jsonResponse = response.body().string();
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            // Extract dependencies array
            JsonArray dependenciesArray = jsonObject.getAsJsonArray("dependencies");

            // Convert JSON array to a set of dependencies
            Set<QilletniInfoData.Dependency> dependencies = new HashSet<>();
            for (JsonElement element : dependenciesArray) {

                JsonObject depObject = element.getAsJsonObject();
                String depName = depObject.get("name").getAsString();
                String depVersionString = depObject.get("version").getAsString();
                Optional<ComparableVersion> optDepVersion = ComparableVersion.parseComparableVersionString(depVersionString);

                if (optDepVersion.isPresent()) {
                    ComparableVersion depVersion = optDepVersion.get();
                    dependencies.add(new QilletniInfoData.Dependency(depName, depVersion));
                }
            }

            return dependencies;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ComparableVersion> getAvailableVersions(String name) {

        String url = API_URL + "/info/versions/" + name;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            // Convert response body to JSON object
            String jsonResponse = response.body().string();
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            // Extract the "versions" field (which is a string)
            String versionsString = jsonObject.get("versions").getAsString();

            // Convert the versions string into a List (split by commas)
            String[] versionArray = versionsString.replace("{", "").replace("}", "").split(", ");
            List<ComparableVersion> versions = new ArrayList<>();
            for (String version : versionArray) {
                Optional<ComparableVersion> optVersion = ComparableVersion.parseComparableVersionString(version);
                optVersion.ifPresent(versions::add);

            }

            return versions;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}