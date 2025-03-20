package max.burdett.qpm.command.lock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Lockfile {
    public String packageName;
    public String version;
    public List<String> dependencies;

    public Lockfile(String packageName, String version, List<String> dependencies) {
        this.packageName = packageName;
        this.version = version;
        this.dependencies = dependencies;
    }

    // Serialize to JSON and save to file
    public void saveToFile(String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(this, writer);
        }
    }

    // Load lockfile from JSON file
    public static Lockfile fromFile(String filePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Lockfile.class);
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
