package main.java.max.burdett.qpm;
import main.java.max.burdett.qpm.command.fetch.CommandFetch;
import main.java.max.burdett.qpm.command.init.CommandInit;
import main.java.max.burdett.qpm.command.lock.CommandLock;
import picocli.CommandLine;

@CommandLine.Command(name = "qpm", subcommands = {CommandInit.class, CommandLock.class, CommandFetch.class})
public class QpmApplication {

    public static void main(String[] args) {
        System.out.println("Hello World");
    }

}
