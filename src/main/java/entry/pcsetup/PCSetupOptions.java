package entry.pcsetup;

import entry.common.option.NoArgOption;
import entry.common.option.OptionBuilder;
import entry.common.option.SingleArgOption;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public enum PCSetupOptions {
    Help(NoArgOption.full("h", "help", "Usage")),
    SetupPatterns(SingleArgOption.full("sp","setup-patterns", "definition", "Specify pattern definition to build setup, directly")),
    SolvePatterns(SingleArgOption.full("p", "solve-patterns","definition", "Specify pattern definition to solve setup, directly")),
    Hold(SingleArgOption.full("H", "hold", "use or avoid", "If use hold, set 'use'. If not use hold, set 'avoid'")),
    Drop(SingleArgOption.full("d", "drop", "hard or soft", "Specify drop")),
    Threads(SingleArgOption.full("th", "threads", "number", "Specify number of used thread")),
    LogPath(SingleArgOption.full("lp", "log-path", "path", "File path of output log")),;

    private final OptionBuilder optionBuilder;

    PCSetupOptions(OptionBuilder optionBuilder) {
        this.optionBuilder = optionBuilder;
    }

    public String optName() {
        return optionBuilder.getLongName();
    }

    public static Options create() {
        Options allOptions = new Options();

        for (PCSetupOptions options : PCSetupOptions.values()) {
            Option option = options.optionBuilder.toOption();
            allOptions.addOption(option);
        }

        return allOptions;
    }
}