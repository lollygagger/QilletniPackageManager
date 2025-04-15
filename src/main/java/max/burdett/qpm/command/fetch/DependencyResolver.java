package max.burdett.qpm.command.fetch;

import max.burdett.qpm.util.ExactDependency;

import java.io.IOException;
import java.util.Map;

public interface DependencyResolver {
    Map<ExactDependency, String> resolveDependencies() throws IOException;
}
