package entry.pcsetup;

import entry.common.option.NoArgOption;
import entry.common.option.OptionBuilder;
import entry.common.option.SingleArgOption;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public enum PCSetupOptions {
    Help(NoArgOption.full("h", "help", "Usage")),
    SetupPatterns(SingleArgOption.full("sp","setup-patterns", "definition", "Specify pattern definition to build setup, directly")),
    SetupPatternsPath(SingleArgOption.full("spp", "setup-patterns-path", "path", "File path of setup pattern definition")),
    SolvePatterns(SingleArgOption.full("p", "solve-patterns","definition", "Specify pattern definition to solve setup, directly")),
    SolvePatternsPath(SingleArgOption.full("pp", "solve-patterns-path", "path", "File path of solve pattern definition")),
    Hold(SingleArgOption.full("H", "hold", "use or avoid", "If use hold, set 'use'. If not use hold, set 'avoid'")),
    Drop(SingleArgOption.full("d", "drop", "hard or soft", "Specify drop")),
    Threads(SingleArgOption.full("th", "threads", "number", "Specify number of used thread")),
    LogPath(SingleArgOption.full("lp", "log-path", "path", "File path of output log")),
    BestKnownSetup(SingleArgOption.full("bksf", "best-known-setup","v115@~","Specify fumen of best known setup")),
    BestKnownSetupPage(SingleArgOption.full("bksfpp", "best-known-setup-page", "number", "Page of best known setup in fumen")),
    BestKnownSetupPath(SingleArgOption.full("bksfp", "best-known-setup-path", "path", "Path to best known setup")),
    StartField(SingleArgOption.full("f", "start-field", "v115@~", "Specify field to start with")),
    StartFieldPath(SingleArgOption.full("fp", "start-field-path", "path", "Path of starting field")),
    StartFieldPage(SingleArgOption.full("fpp", "start-field-page", "number", "Page of starting field in fumen")),
    PercentCutoff(SingleArgOption.full("co", "cutoff-percent", "number", "Specify cutoff percent for field search")),
    SetupHold(SingleArgOption.full("sh", "setup-hold", "use or avoid", "If use hold to build setup, set 'use'. Otherwise, set 'avoid'"));
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
