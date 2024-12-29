package max.burdett.qpm;
import max.burdett.qpm.command.fetch.CommandFetch;
import max.burdett.qpm.command.lock.CommandLock;
import picocli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandLine.Command(name = "qpm", subcommands = {CommandLock.class, CommandFetch.class})
public class QpmApplication {


    private static final Logger LOGGER = LoggerFactory.getLogger(QpmApplication.class);

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display a help message")
    private boolean helpRequested = false;

    public static void main(String[] args) {
        var application = new QpmApplication();

        if (args.length == 0) {
            LOGGER.error("Invalid Command");
            return;
        }

        new CommandLine(application).execute(args);
    }

}
