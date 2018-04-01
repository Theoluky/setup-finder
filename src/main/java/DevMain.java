import entry.EntryPointMain;
import output.HTMLColumn;

import java.util.Optional;

public class DevMain {
    public static void main(String[] args) throws Exception {
//        String command = "path -f csv -k p -t v115@9gF8DeF8DeF8DeF8NeAgH -p T,*p4";
//        String command = "setup -t v115@Pg1hDe1hBeB8FtCeA8FtB8AeB8EtA8BeD8CtA8CeE8?AtB8AeI8AeG8JeAgH -p *p7 -m i -f z";

//        String command = "percent -t http://fumen.zui.jp/?v115@DhD8HeC8HeG8BeB8JeAgH -p L,L,S,J,J,T,O -fc 0 -td 1";
//        String command = "percent -t v115@wgF8FeG8CeB8GeC8DeF8GeB8JeAgH -p L,L,Z,S,Z,I,J,I,Z,T,O -c 7 --hold no";
//        String command = "move -t v115@9gA8IeA8IeA8IeA8SeAgH -p [TIO]p2";
//        String command = "path -t v115@zgyhGexhHexhGeAtxhC8BeA8BtyhE8AtA8JeAgWBAV?AAAAvhAAAPBAUAAAA -P 2 -p [IJLOS]p5,S --split yes";
//        String command = "path -t v115@9gB8HeC8GeE8EeF8NeAgWMA0no2ANI98AQPcQB";
         String command = "setup -p [^T]! --fill i --margin o -t v115@zgdpwhUpxhCe3hAe1hZpJeAgH";
//        String command = "setup -p [^T]! --fill i --margin o -t v115@zgTpwhYpAeUpzhAe3hQpAeQpzhTpAeUpJeAgH";
//        String command = "setup -p [^T]! --fill i --margin o -t v115@zgUpwhYpAeTp0hAe3hQpAeQpyhUpAeTpJeAgH";
        int returnCode = EntryPointMain.main(command.split(" "));
        System.exit(returnCode);

//        ExecutorService executorService = Executors.newFixedThreadPool(6);
//        try (AsyncBufferedFileWriter writer = new AsyncBufferedFileWriter(new File("output/test"), Charset.defaultCharset(), false, 10L)) {
//            for (int i = 0; i < 10; i++) {
//                int finalI = i;
//                executorService.submit(() -> {
//                    for (int j = 0; j < 10; j++) {
//                        writer.writeAndNewLine(String.format("hello %d-%03d", finalI, j));
//                    }
//                });
//            }
//            executorService.shutdown();
//            executorService.awaitTermination(100L, TimeUnit.SECONDS);
//        }
//        LoadedPatternGenerator generator = new LoadedPatternGenerator("TISZ*![tisz]");
//        int depth = generator.getDepth();
//        System.out.println(depth);
//        generator.blocksStream().map(Pieces::getPieces).forEach(System.out::println);

//        HTMLBuilder<TestColumn> builder = new HTMLBuilder<>("hello");
//        builder.addColumn(TestColumn.SECTION1, "hello");
//        builder.addColumn(TestColumn.SECTION2, "test1");
//        builder.addColumn(TestColumn.SECTION1, "world");
//        builder.addColumn(TestColumn.SECTION2, "test2");
//        List<String> list = builder.toList(Arrays.asList(TestColumn.SECTION1, TestColumn.SECTION2), true);
//
//        StringBuilder builder1 = new StringBuilder();
//        String lineSeparator = System.lineSeparator();
//        for (String s : list) {
//            builder1.append(s).append(lineSeparator);
//        }
//        System.out.println(builder1.toString());

    }

    public enum TestColumn implements HTMLColumn {
        SECTION1,
        SECTION2;

        @Override
        public String getTitle() {
            return name();
        }

        @Override
        public String getId() {
            return name();
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.empty();
        }
    }
}
