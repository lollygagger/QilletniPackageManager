package max.burdett.qpm;
import max.burdett.qpm.command.fetch.CommandFetch;
import max.burdett.qpm.command.lock.CommandLock;

import picocli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@CommandLine.Command(name = "qpm", subcommands = {CommandLock.class, CommandFetch.class})
public class QpmApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(QpmApplication.class);

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display a help message")
    private boolean helpRequested = false;

    public static void main(String[] args) {
        var application = new QpmApplication();

        System.out.println("HELLO WORLD");

        CommandLock lock = new CommandLock();

        try{
            lock.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        new CommandLine(application).execute(args);
    }

}
