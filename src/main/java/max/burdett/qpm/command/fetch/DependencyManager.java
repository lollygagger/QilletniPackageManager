package max.burdett.qpm.command.fetch;


import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;


public class DependencyManager {

    // Method to resolve dependencies concurrently

    /**
     * resolveDependencies manages the process of resolving the needed dependencies. This method acts as an orchestrator
     * to download all the required dependencies at the same time.
     *
     * @param requiredDependencies A set
     * @return A map containing the dependency object as the key and the file location as the value
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static Map<ExactDependency, String> resolveDependencies(Set<ExactDependency> requiredDependencies) throws IOException, InterruptedException, ExecutionException {
        String qilDir = System.getProperty("user.home") + "/.Qilletni";
        Map<ExactDependency, String> satisfiedDependencies = checkExistingDependencies(qilDir, requiredDependencies);

        // Executor for concurrent downloading
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Callable<Void>> tasks = new ArrayList<>();

        // For each unsatisfied dependency, create a download task
        for (ExactDependency dependency : requiredDependencies) {
            if (!satisfiedDependencies.containsKey(dependency)) {
                tasks.add(() -> {
                    try {
                        Path destination = Paths.get(qilDir, dependency.name()); // e.g., ~/.Qil/dependency.jar
                        DependencyDownloader.downloadPackage(dependency, destination);
                        satisfiedDependencies.put(dependency, destination.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            }
        }
        return satisfiedDependencies;
    }

    // Check for existing dependencies in ~/.Qil directory
    public static Map<ExactDependency, String> checkExistingDependencies(String qilDir, Set<ExactDependency> requiredDependencies) throws IOException {
        Map<ExactDependency, String> satisfiedDependencies = new HashMap<>();
        Path qilPath = Paths.get(System.getProperty("user.home"), ".Qilletni");

        // Iterate over each required dependency and check if the file exists
        for (ExactDependency dep : requiredDependencies) {
            // Construct the path based on the dependency's name and version
            Path dependencyPath = qilPath.resolve(dep.name())
                    .resolve(dep.version().getVersionString())
                    .resolve(dep.name() + ".qll");

            // Check if the file exists and if it does, add to the map
            if (Files.exists(dependencyPath)) {
                satisfiedDependencies.put(dep, dependencyPath.toString());
            }
        }

        return satisfiedDependencies;
    }



}
