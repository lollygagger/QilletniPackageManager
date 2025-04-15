package max.burdett.qpm.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import java.nio.file.Path;
import java.nio.file.Files;


public class Lockfile {
    public String projectName;
    public String version;
    public List<ExactDependency> dependencies;
    public String manifestHash;

    public Lockfile(String projectName, String version, List<ExactDependency> dependencies, String manifestHash) {
        this.projectName = projectName;
        this.version = version;
        this.dependencies = dependencies;
        this.manifestHash = manifestHash;
    }

    // Serialize to JSON and save to file
    public void saveToFile(String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(this, writer);
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }


    /**
     * Creates a lockfile object from a lockfile
     * @param filePath the path to the lockfile to be deserialized
     * @return a {@link Lockfile} representing the lockfile located at the filePath
     * @throws IOException
     */
    public static Lockfile fromFile(String filePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Lockfile.class);
        }
    }

    /**
     * Gets the manifest file hash stored in the lockfile
     * @param lockfilePath the path to the lockfile
     * @return the hash of a qilletni manifest file stored in the lockfile
     * @throws IOException
     */
    public static String getManifestHash(Path lockfilePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(lockfilePath.toFile())) {
            Lockfile lockfile = gson.fromJson(reader, Lockfile.class);
            return lockfile.manifestHash;
        }
    }

    /**
     * Computes the hash of a given file
     * @param filePath the path to the file to be hashes
     * @return the hash of the file at the provided path
     * @throws IOException
     */
    public static String computeFileHash(Path filePath) throws IOException {
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);
            return HexFormat.of().formatHex(hashBytes); // Java 17+
        } catch (Exception e) {
            throw new IOException("Failed to compute hash of file: " + filePath, e);
        }
    }

}
