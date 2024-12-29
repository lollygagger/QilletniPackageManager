package max.burdett.qpm.command.lock;
import picocli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "lock", description = "Creates a lockfile from your Qilletni manifest")
public class CommandLock implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLock.class);


    @Override
    public Integer call() throws Exception {
        LOGGER.debug("Parsing Qilletni Manifest");
        return -1;
    }
}
