package max.burdett.qpm.command.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;

public class ManifestParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManifestParser.class);

    public static ManifestData parseYaml(String manifestDir) throws IOException {
        Yaml yaml = new Yaml();

        File yamlFile = new File(manifestDir, "Qilletni_info.yml");

        if (!yamlFile.exists()) {
            LOGGER.error("No Qilletni_info.yml manifest file located in directory: \n" + yamlFile.getAbsolutePath());
        }

        FileInputStream inputStream = new FileInputStream(yamlFile);

        Map<String, Object> yamlData = yaml.load(inputStream);

        ManifestData manifest = new ManifestData();
        manifest.setName((String) yamlData.get("name"));
        manifest.setVersion((String) yamlData.get("version"));
        manifest.setAuthor((String) yamlData.get("author"));

        List<String> dependencies = (List<String>) yamlData.get("dependencies");
        manifest.setDependencies(dependencies);

        return manifest;
    }

}