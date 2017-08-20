package _usecase;

import core.field.Field;
import core.field.FieldFactory;
import core.field.SmallField;
import entry.EntryPointMain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: write irregular unittest
class PercentUseCaseTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static class Messages {
        private static String success(int successCount, int allCount) {
            double percent = successCount * 100.0 / allCount;
            return String.format("success = %.2f%% (%d/%d)", percent, successCount, allCount);
        }

        private static String patternSize(int noDuplicateCount) {
            return String.format("Searching pattern size ( no dup. ) = %d", noDuplicateCount);
        }

        private static String treeHeadSize(int count) {
            return String.format("Head %d pieces", count);
        }

        private static String tree(String sequence, double percent) {
            return String.format("%s -> %.2f %%", sequence, percent);
        }

        private static String tree(String sequence) {
            return String.format("%s ->", sequence);
        }

        private static String failPatternSize(int max) {
            return String.format("Fail pattern (max. %d)", max);
        }

        private static String failNothing() {
            return "nothing";
        }

        private static String clearLine(int height) {
            return String.format("Max clear lines: %d", height);
        }

        private static String useHold() {
            return "Using hold: use";
        }

        private static String noUseHold() {
            return "Using hold: avoid";
        }

        private static String failPatternAllSize() {
            return "Fail pattern (all)";

        }
    }

    @BeforeEach
    void setUp() throws IOException {
        ConfigFileHelper.deleteFieldFile();
        ConfigFileHelper.deletePatternFile();
    }

    @Nested
    class FileCase {
        @Test
        void useFieldFileAndPatternsFile1() throws Exception {
            // フィールドファイル + パターンファイル

            Field field = FieldFactory.createField("" +
                    "__X_______" +
                    "__XX_____X" +
                    "XXXX_____X" +
                    "XXXXX___XX"
            );

            ConfigFileHelper.createFieldFile(field, 4);
            ConfigFileHelper.createPatternFile("*p7");

            String command = "percent";
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(5016, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 99.52))
                    .contains(Messages.tree("T", 98.89))
                    .contains(Messages.tree("LT", 96.67))
                    .contains(Messages.tree("JTL", 83.33))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void useFieldFileAndPatternsFile2() throws Exception {
            // フィールドファイル + パターンファイル

            Field field = FieldFactory.createField("" +
                    "_______XXX" +
                    "_______XXX" +
                    "XX_____XXX" +
                    "XX_____XXX"
            );

            ConfigFileHelper.createFieldFile(field, 4);
            ConfigFileHelper.createPatternFile("*p7");

            String command = "percent";
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(5038, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 99.96))
                    .contains(Messages.tree("T", 100.00))
                    .contains(Messages.tree("SL", 99.17))
                    .contains(Messages.tree("LSJ", 95.83))
                    .contains(Messages.failPatternSize(100))
                    .contains("[S, L, J, T, Z, O, I]")
                    .contains("[L, S, J, T, Z, O, I]");

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void useFieldFileAndCommand1() throws Exception {
            // フィールドファイル + パターンコマンド

            Field field = FieldFactory.createField("" +
                    "XXX______X" +
                    "XX_______X" +
                    "XXXX_____X" +
                    "XXX______X"
            );

            ConfigFileHelper.createFieldFile(field, 4);

            String command = "percent -p *p7";
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4282, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 84.96))
                    .contains(Messages.tree("J", 82.64))
                    .contains(Messages.tree("ST", 100.00))
                    .contains(Messages.tree("ZSO", 45.83))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void useFieldFileAndCommand2() throws Exception {
            // フィールドファイル + パターンコマンド (パターンファイル無視)

            Field field = FieldFactory.createField("" +
                    "XXX_______" +
                    "XX________" +
                    "XXXX______" +
                    "XXXXXXX___"
            );

            ConfigFileHelper.createFieldFile(field, 4);
            ConfigFileHelper.createPatternFile("*p4");

            String command = "percent -p *p7";
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4524, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 89.76))
                    .contains(Messages.tree("L", 85.14))
                    .contains(Messages.tree("SZ", 85.00))
                    .contains(Messages.tree("OLZ", 91.67))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void getLog() throws Exception {
            // フィールドファイル, パターンファイル, ログファイル (場所を変更する)

            Field field = FieldFactory.createField("" +
                    "______XXXX" +
                    "______XXXX" +
                    "_____XXXXX" +
                    "_______XXX"
            );

            int height = 4;

            ConfigFileHelper.createFieldFile(field, "input", "test_field", height);
            ConfigFileHelper.createPatternFile("*p7", "input", "test_patterns");

            String command = "percent -fp input/test_field.txt -pp input/test_patterns.txt --log-path test_output_log/test_last_output.txt";
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            String logFile = Files.lines(Paths.get("test_output_log/test_last_output.txt")).collect(Collectors.joining(LINE_SEPARATOR)) + LINE_SEPARATOR;
            assertThat(log.getOutput())
                    .isEqualTo(logFile);

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4784, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 94.92))
                    .contains(Messages.tree("Z", 91.94))
                    .contains(Messages.tree("TS", 97.50))
                    .contains(Messages.tree("LIS", 83.33))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }
    }

    @Nested
    class TetfuCase {
        @Test
        void useTetfuOnly1() throws Exception {
            // テト譜 + パターンコメント

            /*
            comment: 4 -p T,*p7
            __________
            ___X______
            XXXXXX____
            XXXXX_____
             */
            String tetfu = "v115@KhA8FeF8DeE8OeAgWQA0no2ANI98AQe88AjPcQB";

            String command = String.format("percent -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(5040, 5040))
                    .contains("T,*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("T", 100.0))
                    .contains(Messages.tree("TI", 100.0))
                    .contains(Messages.tree("TSL", 100.0))
                    .contains(Messages.failPatternSize(100))
                    .contains(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        @Tag("long")
        void useTetfuAndCommand1() throws Exception {
            // テト譜 + パターンコマンド

           /*
            comment: 4
            __________
            __________
            __________
            __________
             */
            String tetfu = "v115@vhAAgWBAUAAAA";

            String command = String.format("percent -t %s -p %s", tetfu, "S,[TZ]p2,*p7");
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(10080, 10080))
                    .contains("S,[TZ]p2,*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(10080))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("S", 100.0))
                    .contains(Messages.tree("ST", 100.0))
                    .contains(Messages.tree("SZ", 100.0))
                    .doesNotContain(Messages.tree("*"))
                    .doesNotContain(Messages.tree("I"))
                    .doesNotContain(Messages.tree("TO"))
                    .contains(Messages.failPatternSize(100))
                    .contains(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        @Tag("long")
        void useTetfuAndPatternsFile1() throws Exception {
            // テト譜 + パターンファイル

           /*
            comment: 4
            __________
            __________
            _XX_______
            XX________
             */
            String tetfu = "v115@ShB8GeB8ReAgWBAUAAAA";

            ConfigFileHelper.createPatternFile("[TZ]p2,*p7");

            String command = String.format("percent -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(10080, 10080))
                    .contains("[TZ]p2,*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(10080))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 100.0))
                    .contains(Messages.tree("T", 100.0))
                    .contains(Messages.tree("ZT", 100.0))
                    .contains(Messages.failPatternSize(100))
                    .contains(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void useTetfuAndCommand2() throws Exception {
            // テト譜 + パターンコマンド (フィールドファイル・パターンファイル無視)

            /*
            comment: 4
            X_________
            XXXX______
            XXXXXX_____
            XXXXXXX___
             */

            String tetfu = "d115@9gA8IeD8FeE8EeF8NeAgWBAUAAAA";

            Field field = FieldFactory.createField("" +
                    "X___XXX___" +
                    "XX_X_XXX__" +
                    "X__XX_XX__" +
                    "XXX_X_X_X_"
            );

            ConfigFileHelper.createFieldFile(field, 4);
            ConfigFileHelper.createPatternFile("*p4");

            String command = String.format("percent -p *p7 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4636, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 91.98))
                    .contains(Messages.tree("T", 100.00))
                    .contains(Messages.tree("OI", 98.33))
                    .contains(Messages.tree("ILZ", 87.50))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void useTetfuAndCommand3() throws Exception {
            // テト譜 (無関係なコメント付き) + パターンコマンド (パターンファイルを無視)

            /*
            comment: 1ページ目: 無関係なコメントです
            XX________
            XXX______X
            XXX_____XX
            XXXX_____X
             */

            String tetfu = "v115@9gB8HeC8FeD8EeF8EeA8JeAgWbBxXHDBQGDSA1d0AC?DYHDBQzuRA1Dq9BF4CwBFbcRA1zW9AxXXXB1RhRAV/d3ByX?HDBQxCSA1dUzBzXHDBwHfRA1d0ACzXHDBw0uRA1d0KB3XHD?Bwv4AA";

            ConfigFileHelper.createFieldFile(new SmallField(), 4);
            ConfigFileHelper.createPatternFile("*p7");

            String command = String.format("percent -c 4 -p *p7 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(5008, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 99.37))
                    .contains(Messages.tree("S", 98.33))
                    .contains(Messages.tree("OS", 95.83))
                    .contains(Messages.tree("ZOS", 83.33))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void useTetfuAndCommand4() throws Exception {
            // テト譜 (無関係なコメント付き) + パターンコマンド (パターンファイルを無視)

            /*
            comment: 日本語開始のコメント
            XXXXXX____
            XXXXXX____
            XXXXXX_X__
            XXXXXXXXX_
             */

            String tetfu = "v115@9gF8DeF8DeF8AeA8BeI8KeAgW8AlfrHBFwDfE2Cx2B?l/PwB5HEfE5fmzBlPJVBjDEfET4p9Blvs2ACtDfETor6Alv?s2AGtDfETIPSB";

            ConfigFileHelper.createFieldFile(new SmallField(), 4);
            ConfigFileHelper.createPatternFile("*p7");

            String command = String.format("percent -c 4 -p *p4 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(312, 840))
                    .contains("*p4")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(840))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 37.14))
                    .contains(Messages.tree("I", 40.00))
                    .contains(Messages.tree("OS", 5.0))
                    .contains(Messages.tree("IOS", 0.0))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void useTetfuOnly2() throws Exception {
            // テト譜 (ホールドnoにする) + パターンコマンド (フィールドファイル・パターンファイル無視)

            /*
            comment: 4 -p *p2 -H no
            _______X__
            ______XX__
            ____XXXXXX
            ___XXXXXXX
             */

            String tetfu = "m115@EhA8HeB8FeF8CeG8JeAgWWA0no2ANI98AQPk/AFbcs?AIoo2Au3BAA";

            ConfigFileHelper.createFieldFile(FieldFactory.createField(""), 4);
            ConfigFileHelper.createPatternFile("*p2");

            String command = String.format("percent -p *p6 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.noUseHold())
                    .contains(Messages.success(744, 5040))
                    .contains("*p6")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 14.76))
                    .contains(Messages.tree("T", 16.25))
                    .contains(Messages.tree("IL", 13.33))
                    .contains(Messages.tree("JOZ", 0.0))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void colorTetfu() throws Exception {
            // テト譜 + パターンコマンド (フィールドファイル・パターンファイル無視)

            /*
            comment: <Empty>
            I_________
            I___Z_____
            IOOZZSS___
            IOOZSS____
             */

            String tetfu = "http://fumen.zui.jp/?d115@9gwhIewhIewhRpBeR4CewhRpAeR4NesKJ";

            String command = String.format("percent -p *p7 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4736, 5040))
                    .contains("*p7")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 93.97))
                    .contains(Messages.tree("T", 100.00))
                    .contains(Messages.tree("SL", 85.83))
                    .contains(Messages.tree("JLZ", 91.67))
                    .contains(Messages.failPatternSize(100))
                    .doesNotContain(Messages.failNothing());

            assertThat(log.getError()).isEmpty();
        }
    }

    @Nested
    class SpecialCase {
        @Test
        void filledLine() throws Exception {
            // ラインがすでに埋まっているケース

            Field field = FieldFactory.createField("" +
                    "XXXXXX____" +
                    "XXXXX_____" +
                    "XXXXXXXXXX" +
                    "XXXXXX___X"
            );

            ConfigFileHelper.createFieldFile(field, 4);
            ConfigFileHelper.createPatternFile("T, *p3");

            String command = "percent";
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(190, 210))
                    .contains("T, *p3")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(210))
                    .contains(Messages.treeHeadSize(3))
                    .doesNotContain(Messages.tree("*"))
                    .contains(Messages.tree("T", 90.48))
                    .contains(Messages.tree("TJ", 90.00))
                    .contains(Messages.tree("TOI", 60.00))
                    .contains(Messages.failPatternSize(100))
                    .contains("[T, S, T, J]")
                    .contains("[T, O, I, L]")
                    .contains("[T, T, J, I]");

            assertThat(log.getError()).isEmpty();
        }
    }

    @Nested
    class OptionCase {
        @Test
        void withoutHold1() throws Exception {
            // ホールドなしコマンド

            Field field = FieldFactory.createField("" +
                    "XXXXXXXXX_" +
                    "XXXXXXXXX_" +
                    "__XXXXXXX_" +
                    "__XXXXXXX_"
            );

            ConfigFileHelper.createFieldFile(field, 4);

            String command = "percent -p [IO]p2 --hold avoid";
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.noUseHold())
                    .contains(Messages.success(1, 2))
                    .contains("[IO]p2")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(2))
                    .contains(Messages.treeHeadSize(2))
                    .contains(Messages.tree("*", 50.00))
                    .contains(Messages.tree("I", 100.00))
                    .contains(Messages.tree("O", 0.0))
                    .contains(Messages.failPatternSize(100))
                    .contains("[O, I]");

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void page() throws Exception {
            // ページの指定

            /*
            comment: 4 -p [JT]!,*p4
            XXX_______
            XXXXX_____
            XXXXXX____
            XXXXXX____
             */
            String tetfu = "v115@IhA8HeB8HeA8SeRPYMAkQnGE5VrGEtIReEJhRpHeRp?ZevrB9gi0Geg0meAAPaA0no2ANI98AwXfzBqeEHBEoA6AFL?/iAQfAAA";

            String command = String.format("percent -t %s -P 3", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(1582, 1680))
                    .contains("[JT]!,*p4")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(1680))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 94.17))
                    .contains(Messages.failPatternSize(100))
                    .contains("[T, J, O, S, T, Z]")
                    .contains("[T, J, S, Z, O, T]");

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void treeDepth() throws Exception {
            // ツリーの深さを変更

            /*
            comment: 4 -p *!
            XXX_______
            XX_______X
            XX_______X
            XXXXX___XX
             */
            String tetfu = "v115@9gC8GeB8GeC8GeB8GeB8JeRPYNA0no2ANI98AQf78A?RAAAA";

            String command = String.format("percent -t %s -td 4", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4992, 5040))
                    .contains("*!")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(4))
                    .contains(Messages.tree("*", 99.05))
                    .contains(Messages.tree("S", 98.33))
                    .contains(Messages.tree("ZT", 98.33))
                    .contains(Messages.tree("TSJL", 66.67))
                    .contains(Messages.failPatternSize(100))
                    .contains("[Z, T, S, L, O, J, I]")
                    .contains("[T, L, J, S, Z, I, O]");

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void failedCount() throws Exception {
            // ページの指定

            /*
            comment: 4 -p *!
            XXX_______
            XX________
            XX______XX
            XXXXX___XX
             */
            String tetfu = "v115@9gC8GeB8HeB8FeC8GeB8JeRPYNA0no2ANI98AQf78A?RAAAA";

            String command = String.format("percent -t %s -fc -1", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4716, 5040))
                    .contains("*!")
                    .contains(Messages.clearLine(4))
                    .contains(Messages.patternSize(5040))
                    .contains(Messages.treeHeadSize(3))
                    .contains(Messages.tree("*", 93.57))
                    .contains(Messages.failPatternAllSize())
                    .contains("[J, L, S, Z, O, I, T]")
                    .contains("[S, I, J, L, Z, T, O]")
                    .contains("[L, S, O, Z, J, I, T]")
                    .contains("[J, T, L, S, Z, I, O]")
                    .contains("[S, L, I, O, Z, J, T]")
                    .contains("[L, I, S, O, Z, J, T]")
                    .contains("[S, T, Z, I, L, J, O]")
                    .contains("[T, S, Z, I, L, J, O]")
                    .contains("[J, I, S, Z, L, T, O]")
                    .contains("[S, J, L, O, I, Z, T]");

            assertThat(log.getError()).isEmpty();
        }
    }

    @Nested
    class CountCase {
        @Test
        void pattern1() throws Exception {
            /*
            comment: <Empty>
            _______XXX
            ________XX
            XX____XXXX
            XX_____XXX
             */

            String tetfu = "m115@EhC8HeD8DeF8EeC8JeAgH";

            String command = String.format("percent -p *p7 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(4374, 5040))
                    .contains("*p7");

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void pattern2() throws Exception {
            /*
            comment: <Empty>
            X____X____
            XX__XX____
            XX__XX____
            XXXXXX____
             */

            String tetfu = "http://harddrop.com/fumen/?v115@9gA8DeA8DeB8BeB8DeB8BeB8DeF8NeAgH";

            String command = String.format("percent -p T,I,O,*p4 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.useHold())
                    .contains(Messages.success(742, 840))
                    .contains("T,I,O,*p4");

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void pattern3() throws Exception {
            /*
            comment: <Empty>
            __________
            __________
            _________X
            ___XX__XXX
             */

            String tetfu = "http://harddrop.com/fumen/?d115@ahA8CeB8BeC8JeAgH";

            String command = String.format("percent -c 3 -p *p7 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.clearLine(3))
                    .contains(Messages.useHold())
                    .contains(Messages.success(2368, 5040))
                    .contains("*p7");

            assertThat(log.getError()).isEmpty();
        }
        
        @Test
        void pattern4() throws Exception {
            /*
            comment: <Empty>
            __________
            __________
            X________X
            X______XXX
             */

            String tetfu = "http://harddrop.com/fumen/?m115@RhA8HeB8FeC8JeAgH";

            String command = String.format("percent -c 3 -p *p7 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.clearLine(3))
                    .contains(Messages.useHold())
                    .contains(Messages.success(5028, 5040))
                    .contains("*p7");

            assertThat(log.getError()).isEmpty();
        }

        @Test
        void pattern5() throws Exception {
            /*
            comment: <Empty>
            XXXX_____X
            XX______XX
            XXX____XXX
            XXX_____XX
             */

            String tetfu = "http://fumen.zui.jp/?m115@9gzhEewwRpFexwRpglDeBtwwilEeBtJeAgH";

            String command = String.format("percent -p [OSZTLJ]p6 -t %s", tetfu);
            Log log = RunnerHelper.runnerCatchingLog(() -> {
                EntryPointMain.main(command.split(" "));
            });

            assertThat(log.getOutput())
                    .contains(Messages.clearLine(4))
                    .contains(Messages.useHold())
                    .contains(Messages.success(702, 720))
                    .contains("[OSZTLJ]p6");

            assertThat(log.getError()).isEmpty();
        }
    }
}
