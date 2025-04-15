package max.burdett.qpm.command.fetch;

import dev.qilletni.api.lib.qll.ComparableVersion;
import dev.qilletni.api.lib.qll.QilletniInfoData;
import max.burdett.qpm.util.ExactDependency;
import max.burdett.qpm.util.Lockfile;
import max.burdett.qpm.util.QilletniInfoParser;
import picocli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "fetch", description = "Fetches dependencies described in the lockfile")
public class CommandFetch implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandFetch.class);

    private DependencyResolver resolver;

    private final QilletniInfoParser parser = new QilletniInfoParser();

    private QilletniInfoData manifestInfo;

    private Map<ExactDependency, String> dependencies;

    @Override
    public Integer call() throws Exception {
        LOGGER.debug("Checking for Qilletni manifest");
        String currentDirectory = System.getProperty("user.dir");

        File manifest = new File(currentDirectory, "Qilletni_info.yml");

        // Check if the qilletni manifest exists
        if (manifest.exists() && !manifest.isDirectory()) {
            LOGGER.debug("Qilletni_info.yml was found");
            manifestInfo = parser.readQilletniInfo(manifest.toPath());
        } else {
            LOGGER.debug("Qilletni_info.yml was not found in current directory: {}", currentDirectory);
            throw new FileNotFoundException("Qilletni_info.yml was not found in current directory");
        }

        File lockfile = new File(currentDirectory, "Qilletni_info.lock");

        if (lockfile.exists() && !lockfile.isDirectory()) {
            LOGGER.debug("lockfile was found");

            //Compute the hash for the manifest and compare it to the existing hash in the lockfile
            String newHash = Lockfile.computeFileHash(manifest.toPath());
            String existingHash = Lockfile.getManifestHash(lockfile.toPath());

            if(!newHash.equals(existingHash)) {
                LOGGER.info("Hashes do not match, updating lockfile");
                resolver = new LockfileDependencyResolver(Lockfile.fromFile(lockfile.toString()));
                dependencies = resolver.resolveDependencies();
            } else {
                LOGGER.info("Hashes match, lockfile up to date");
            }

        } else {
            LOGGER.debug("lockfile was not found in current directory: {}", currentDirectory);
            LOGGER.info("Creating new lockfile from Qilletni Manifest");
            resolver = new ManifestDependencyResolver(manifestInfo); {}
            dependencies = resolver.resolveDependencies();
        }


        return -1;
    }
}
