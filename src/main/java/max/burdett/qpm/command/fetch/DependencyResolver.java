package max.burdett.qpm.command.fetch;


import is.yarr.qilletni.api.lib.qll.QilletniInfoData;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;
import java.util.stream.Stream;



public class DependencyResolver {

    // Method to resolve dependencies concurrently

    /**
     * resolveDependencies manages the process of resolving the needed dependencies. This method acts as an orchestrator
     * to download all the required dependencies at the same time.
     * @param requiredDependencies A set
     * @return A map containing the dependency object as the key and the file location as the value
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static Map<QilletniInfoData, String> resolveDependencies(Set<QilletniInfoData> requiredDependencies) throws IOException, InterruptedException, ExecutionException {
        String qilDir = System.getProperty("user.home") + "/.Qilletni";
        Map<QilletniInfoData, String> satisfiedDependencies = checkExistingDependencies(qilDir, requiredDependencies);

        // Executor for concurrent downloading
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Void>> tasks = new ArrayList<>();

        // For each unsatisfied dependency, create a download task
        for (QilletniInfoData dependency : requiredDependencies) {
            if (!satisfiedDependencies.containsKey(dependency)) {
                tasks.add(() -> {
                    try {
                        Path destination = Paths.get(qilDir, dependency.name()); // e.g., ~/.Qil/dependency.jar
                        DependencyDownloader.downloadPackage(dependency, destination);
                        satisfiedDependencies.put(dependency, destination.toString());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            }
        }
    }

    // Check for existing dependencies in ~/.Qil directory
    public static Map<QilletniInfoData, String> checkExistingDependencies(String qilDir, Set<QilletniInfoData> requiredDependencies) throws IOException {
        Map<QilletniInfoData, String> satisfiedDependencies = new HashMap<>();
        Path qilPath = Paths.get(System.getProperty("user.home"), ".Qilletni");

        // List files in ~/.Qil directory
        try (Stream<Path> paths = Files.walk(qilPath)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        // Match dependency name with the file (you may need to strip versions, extensions, etc.)
                        if (requiredDependencies.contains(fileName)) { //TODO might need to go and make a new object? that sounds terrible
                            satisfiedDependencies.put(fileName, path.toString());
                        }
                    });
        }
        return satisfiedDependencies;
    }



}
