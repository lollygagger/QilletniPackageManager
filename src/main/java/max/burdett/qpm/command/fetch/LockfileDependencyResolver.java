package max.burdett.qpm.command.fetch;

import max.burdett.qpm.util.Lockfile;
import max.burdett.qpm.util.download.DependencyManager;
import max.burdett.qpm.util.ExactDependency;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LockfileDependencyResolver implements DependencyResolver {
    private Lockfile lockfile;

    public LockfileDependencyResolver(Lockfile lockfile) {
        this.lockfile = lockfile;
    }

    /**
     * Parses a {@link Lockfile} to download all the required dependencies
     * @return a map containing dependencies mapped to the best found version to meet the requirements in a qilletni manifest
     */
    public Map<ExactDependency, String> resolveDependencies() throws IOException {
        Set<ExactDependency> dependencySet = new HashSet<>(lockfile.dependencies);
        return DependencyManager.resolveDependencies(dependencySet);
    }
}
