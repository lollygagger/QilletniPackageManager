package max.burdett.qpm.util.download;


import max.burdett.qpm.util.ExactDependency;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;


public class DependencyManager {

    // Method to resolve dependencies concurrently

    /**
     * resolveDependencies manages the process of downloading the needed dependencies. This method acts as an orchestrator
     * to download all the required dependencies at the same time.
     *
     * @param requiredDependencies A set containg all the {@link ExactDependency} needed
     * @return A map containing the dependency object as the key and the file location as the value
     * @throws IOException
     */
    public static Map<ExactDependency, String> resolveDependencies(Set<ExactDependency> requiredDependencies) throws IOException{
        String qilDir = System.getProperty("user.home") + "/.Qilletni/packages";
        Map<ExactDependency, String> satisfiedDependencies = checkExistingDependencies(requiredDependencies);

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


    /**
     * Checks for existing dependencies in ~/.Qilletni/packages
     * @param requiredDependencies a set of {@link ExactDependency} objects to check locally for
     * @return a map containing the found dependencies mapped to their location on the local filesystem
     * @throws IOException
     */
    public static Map<ExactDependency, String> checkExistingDependencies(Set<ExactDependency> requiredDependencies) throws IOException {
        Map<ExactDependency, String> satisfiedDependencies = new HashMap<>();
        Path qilPath = Paths.get(System.getProperty("user.home"), "/.Qilletni/packages");

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
