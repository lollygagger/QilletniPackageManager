package max.burdett.qpm.command.lock;

import java.util.HashMap;
import java.util.Map;

public class LockFileManager {

    private Map<String, String> dependencies = new HashMap<String, String>();

    public LockFileManager(ManifestData manifest){
        matchedDependencies = findMatchedDependencies(manifest);
    }
}
