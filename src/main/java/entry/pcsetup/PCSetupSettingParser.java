package entry.pcsetup;

import common.tetfu.common.ColorConverter;
import common.tetfu.field.ColoredField;
import common.tetfu.field.ColoredFieldFactory;
import core.mino.MinoFactory;
import entry.CommandLineWrapper;
import entry.NormalCommandLineWrapper;
import entry.PriorityCommandLineWrapper;
import entry.common.CommandLineFactory;
import entry.common.Loader;
import entry.common.SettingParser;
import entry.common.field.FieldData;
import entry.common.field.FumenLoader;
import entry.percent.PercentOptions;
import entry.percent.PercentSettings;
import exceptions.FinderParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PCSetupSettingParser extends SettingParser<PCSetupSettings>{

    private static final String CHARSET_NAME = "utf-8";
    private static final String DEFAULT_PATTERNS_TXT = "input/patterns.txt";
    private static final String DEFAULT_FIELD_TXT = "input/field.txt";
    private static final String PATTERN_DELIMITER = ";";

    public PCSetupSettingParser(Options options, CommandLineParser parser) {
        super(options, parser);
    }

    @Override
    protected Optional<PCSetupSettings> parse(CommandLineWrapper wrapper) throws FinderParseException {
        PCSetupSettings settings = new PCSetupSettings();

        CommandLineFactory commandLineFactory = this.getCommandLineFactory();
        MinoFactory minoFactory = new MinoFactory();
        ColorConverter colorConverter = new ColorConverter();
        FumenLoader fumenLoader = new FumenLoader(commandLineFactory, minoFactory, colorConverter);

        // Reading fields
//        Optional<FieldData> fieldDataOptional = Loader.loadFieldData(
//                wrapper,
//                fumenLoader,
//                PercentOptions.Page.optName(),
//                PercentOptions.Fumen.optName(),
//                PercentOptions.FieldPath.optName(),
//                DEFAULT_FIELD_TXT,
//                Charset.forName(CHARSET_NAME),
//                Optional::of,
//                fieldLines -> {
//                    try {
//                        // 最大削除ラインの設定
//                        String firstLine = fieldLines.pollFirst();
//                        int maxClearLine = Integer.valueOf(firstLine != null ? firstLine : "error");
//
//                        // フィールドの設定
//                        String fieldMarks = String.join("", fieldLines);
//                        ColoredField coloredField = ColoredFieldFactory.createColoredField(fieldMarks);
//
//                        // 最大削除ラインをコマンドラインのオプションに設定
//                        CommandLine commandLineTetfu = commandLineFactory.parse(Arrays.asList("--" + PercentOptions.ClearLine.optName(), String.valueOf(maxClearLine)));
//                        CommandLineWrapper newWrapper = new NormalCommandLineWrapper(commandLineTetfu);
//                        return Optional.of(new FieldData(coloredField, newWrapper));
//                    } catch (NumberFormatException e) {
//                        throw new FinderParseException("Cannot read clear-line from field file");
//                    }
//                }
//        );
//
//        if (fieldDataOptional.isPresent()) {
//            FieldData fieldData = fieldDataOptional.get();
//
//            Optional<CommandLineWrapper> commandLineWrapper = fieldData.getCommandLineWrapper();
//            if (commandLineWrapper.isPresent()) {
//                wrapper = new PriorityCommandLineWrapper(Arrays.asList(wrapper, commandLineWrapper.get()));
//            }
//
//            // フィールドの設定
//            Optional<Integer> heightOptinal = wrapper.getIntegerOption(PercentOptions.ClearLine.optName());
//            if (heightOptinal.isPresent()) {
//                int height = heightOptinal.get();
//                settings.setField(fieldData.toColoredField(), height);
//                settings.setMaxClearLine(height);
//            } else {
//                settings.setField(fieldData.toColoredField());
//            }
//        }

        // パターンの読み込み

        if (wrapper.getStringOption(PCSetupOptions.BestKnownSetup.optName()).isPresent() ||
                wrapper.getStringOption(PCSetupOptions.BestKnownSetupPath.optName()).isPresent()) {
            Optional<FieldData> bestKnownSetup = Loader.loadFieldData(
                    wrapper,
                    fumenLoader,
                    PCSetupOptions.BestKnownSetupPage.optName(),
                    PCSetupOptions.BestKnownSetup.optName(),
                    PCSetupOptions.BestKnownSetupPath.optName(),
                    "input/bksf.txt",
                    Charset.forName(CHARSET_NAME),
                    Optional::of,
                    fieldLines -> {
                        try {
                            String firstLine = fieldLines.poll();
                            int maxClearLine = Integer.valueOf(firstLine != null ? firstLine : "error");

                            String fieldMarks = String.join("", fieldLines);
                            ColoredField coloredField = ColoredFieldFactory.createColoredField(fieldMarks);

                            CommandLine commandLineTetfu = commandLineFactory.parse(Arrays.asList("4"));
                            CommandLineWrapper newWrapper = new NormalCommandLineWrapper(commandLineTetfu);

                            return Optional.of(new FieldData(coloredField, newWrapper));

                        } catch (NumberFormatException e) {
                            throw new FinderParseException("Cannot read clear-line from field file");
                        }
                    }
            );
            if (bestKnownSetup.isPresent()) {
                FieldData fieldData = bestKnownSetup.get();

                Optional<CommandLineWrapper> commandLineWrapper = fieldData.getCommandLineWrapper();
                if (commandLineWrapper.isPresent()) {
                    wrapper = new PriorityCommandLineWrapper(Arrays.asList(wrapper, commandLineWrapper.get()));
                }

                int height = 4;
                settings.setField(fieldData.toColoredField(), height);
                settings.setMaxClearLine(height);
            }
        } else {
            settings.setBestKnownSetup(null);
        }

        if (wrapper.getStringOption(PCSetupOptions.StartField.optName()).isPresent() ||
            wrapper.getStringOption(PCSetupOptions.StartFieldPath.optName()).isPresent()) {
            Optional<FieldData> startField = Loader.loadFieldData(
                    wrapper,
                    fumenLoader,
                    PCSetupOptions.StartFieldPage.optName(),
                    PCSetupOptions.StartField.optName(),
                    PCSetupOptions.StartFieldPath.optName(),
                    "input/startfield.txt",
                    Charset.forName(CHARSET_NAME),
                    Optional::of,
                    fieldLines -> {
                        try {
                            String firstLine = fieldLines.poll();
                            int maxClearLine = Integer.valueOf(firstLine != null ? firstLine : "error");

                            String fieldMarks = String.join("", fieldLines);
                            ColoredField coloredField = ColoredFieldFactory.createColoredField(fieldMarks);

                            CommandLine commandLineTetfu = commandLineFactory.parse(Arrays.asList("4"));
                            CommandLineWrapper newWrapper = new NormalCommandLineWrapper(commandLineTetfu);

                            return Optional.of(new FieldData(coloredField, newWrapper));
                        } catch (NumberFormatException e) {
                            throw new FinderParseException("Cannot read clear-line from field file");
                        }
                    }

            );

            if (startField.isPresent()) {
                FieldData fieldData = startField.get();

                Optional<CommandLineWrapper> commandLineWrapper = fieldData.getCommandLineWrapper();
                if (commandLineWrapper.isPresent()) {
                    wrapper = new PriorityCommandLineWrapper(Arrays.asList(wrapper, commandLineWrapper.get()));
                }

                int height = 4;
                settings.setStart(fieldData.toColoredField(), height);
            } else {
                settings.setStart(null, 4);
            }

        } else {
            settings.setStart(null, 4);
        }




        List<String> setup_patterns = Loader.loadPatterns(
                wrapper,
                PCSetupOptions.SetupPatterns.optName(),
                PATTERN_DELIMITER,
                PCSetupOptions.SetupPatternsPath.optName(),
                DEFAULT_PATTERNS_TXT,
                Charset.forName(CHARSET_NAME)
        );
        settings.setSetupPatterns(setup_patterns);

        List<String> solve_patterns = Loader.loadPatterns(
                wrapper,
                PCSetupOptions.SolvePatterns.optName(),
                PATTERN_DELIMITER,
                PCSetupOptions.SolvePatternsPath.optName(),
                DEFAULT_PATTERNS_TXT,
                Charset.forName(CHARSET_NAME)
        );
        settings.setSolvePatterns(solve_patterns);

        // ドロップの設定
        Optional<String> dropType = wrapper.getStringOption(PercentOptions.Drop.optName());
        try {
            dropType.ifPresent(type -> {
                String key = dropType.orElse("softdrop");
                try {
                    settings.setDropType(key);
                } catch (FinderParseException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new FinderParseException("Unsupported format: format=" + dropType.orElse("<empty>"));
        }

        // パフェ成功確率ツリーの深さの設定
        Optional<Integer> treeDepth = wrapper.getIntegerOption(PercentOptions.TreeDepth.optName());
        treeDepth.ifPresent(settings::setTreeDepth);

        // パフェ失敗パターンの表示個数の設定
//        Optional<Integer> failedCount = wrapper.getIntegerOption(PercentOptions.FailedCount.optName());
//        failedCount.ifPresent(settings::setFailedCount);

        // ログファイルの設定
        Optional<String> logFilePath = wrapper.getStringOption(PCSetupOptions.LogPath.optName());
        logFilePath.ifPresent(settings::setLogFilePath);

        // ホールドの設定
        Optional<Boolean> isUsingHold = wrapper.getBoolOption(PCSetupOptions.Hold.optName());
        isUsingHold.ifPresent(settings::setUsingHold);

        // スレッド数の設定
        Optional<Integer> threadCount = wrapper.getIntegerOption(PCSetupOptions.Threads.optName());
        threadCount.ifPresent(settings::setThreadCount);

        Optional<Double> cutoffPercent = wrapper.getDoubleOption(PCSetupOptions.PercentCutoff.optName());
        cutoffPercent.ifPresent(settings::setCutoffPercent);

        Optional<Boolean> setupUsingHold = wrapper.getBoolOption(PCSetupOptions.SetupHold.optName());
        setupUsingHold.ifPresent(settings::setSetupUsingHold);

        return Optional.of(settings);
    }
}
