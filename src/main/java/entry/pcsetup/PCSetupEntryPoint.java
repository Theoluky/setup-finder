package entry.pcsetup;

import common.datastore.*;
import common.datastore.action.Action;
import common.datastore.blocks.LongPieces;
import common.datastore.blocks.Pieces;
import common.datastore.order.Order;
import common.parser.OperationTransform;
import common.pattern.PatternGenerator;
import common.tetfu.Tetfu;
import common.tetfu.TetfuElement;
import common.tetfu.common.ColorConverter;
import common.tetfu.common.ColorType;
import common.tetfu.field.ColoredField;
import common.tetfu.field.ColoredFieldFactory;
import common.tree.AnalyzeTree;
import concurrent.*;
import core.FinderConstant;
import core.action.candidate.Candidate;
import core.action.candidate.LockedCandidate;
import core.action.reachable.Reachable;
import core.field.Field;
import core.field.FieldFactory;
import core.field.FieldView;
import core.mino.MinoFactory;
import core.mino.MinoShifter;
import core.mino.Piece;
import core.srs.MinoRotation;
import core.srs.Rotate;
import entry.DropType;
import entry.EntryPoint;
import entry.Verify;
import entry.path.output.MyFile;
import entry.pcsetup.PCSetupSettings;
import entry.percent.PercentCore;
import entry.percent.PercentSettings;
import entry.searching_pieces.NormalEnumeratePieces;
import exceptions.FinderException;
import exceptions.FinderExecuteException;
import exceptions.FinderInitializeException;
import exceptions.FinderTerminateException;
import jdk.nashorn.internal.ir.Block;
import lib.Stopwatch;
import searcher.PutterNoHold;
import searcher.PutterUsingHold;
import searcher.common.validator.PerfectValidator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PCSetupEntryPoint implements EntryPoint{
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final PCSetupSettings settings;
    private final BufferedWriter logWriter;

    public PCSetupEntryPoint(PCSetupSettings settings) throws FinderInitializeException {
        this.settings = settings;

        // Improve the output destination of the log file
        String logFilePath = settings.getLogFilePath();
        MyFile logFile = new MyFile(logFilePath);

        logFile.mkdirs();
        logFile.verify();

        try {
            this.logWriter = logFile.newBufferedWriter();
        } catch (IOException e) {
            throw new FinderInitializeException(e);
        }
    }

    @Override
    public void run() throws FinderException {
        output("# Setup");
        output("Queues:");
        List<String> setup_patterns = settings.getSetupPatterns();
        PatternGenerator setup_generator = Verify.patterns(setup_patterns);
        for (String pattern : setup_patterns)
            output("   " + pattern);
        output();

        // depth info for setup
        int setup_piecesDepth = setup_generator.getDepth();
        int setup_maxDepth = setup_piecesDepth + 1;
        if (setup_piecesDepth > 10 || setup_piecesDepth < 1) {
            throw new FinderInitializeException("Must be less than 10 and more than 1 pieces for valid setup, has " + setup_piecesDepth);
        }

        output("# Solve");
        output("Queues:");
        List<String> solve_patterns = settings.getSolvePatterns();
        PatternGenerator solve_generator = Verify.patterns(solve_patterns);
        if (solve_patterns.size() > 100) {
            for (int i = 0; i < 100; i++)
                output("   " + solve_patterns.get(i));
            output("...");
        } else {
            for (String pattern : solve_patterns)
                output("   " + pattern);
        }
        output("Using hold: " + (settings.isUsingHold() ? "use" : "avoid"));
        output("Drop: " + settings.getDropType().name().toLowerCase());

        // pc solve initialization
        int maxClearLine = 4;

        int solve_maxDepth = 10 - setup_piecesDepth;

        ExecutorService executorService = createExecutorService();
        output("# System Initialize");
        output("Version = " + FinderConstant.VERSION);
        output();

        int solve_piecesDepth = solve_generator.getDepth();
        int solve_popCount = settings.isUsingHold() && solve_maxDepth < solve_piecesDepth ? solve_maxDepth + 1 : solve_maxDepth;
        output("Solve piece pop count = " + solve_popCount);
        NormalEnumeratePieces normalEnumeratePieces = new NormalEnumeratePieces(solve_generator, solve_maxDepth, settings.isUsingHold());
        Set<LongPieces> searchingPieces = normalEnumeratePieces.enumerate();

        output("Searching pattern size for solve (duplicate) = " + normalEnumeratePieces.getCounter());
        output("Searching pattern size for solve ( no dup. ) = " + searchingPieces.size());

        output();

        ThreadLocal<? extends Candidate<Action>> candidateThreadLocal = createCandidateThreadLocal(settings.getDropType(), maxClearLine);
        ThreadLocal<? extends Reachable> reachableThreadLocal = createReachableThreadLocal(settings.getDropType(), maxClearLine);
        MinoFactory solve_minoFactory = new MinoFactory();
        PercentCore percentCore = new PercentCore((ThreadPoolExecutor) executorService, candidateThreadLocal, settings.isUsingHold(), reachableThreadLocal, solve_minoFactory);


        // setup finding intialization:
//        Field field = FieldFactory.createField(4);
        Field field = settings.getStartfield();
        output("Starting field:");
        output(FieldView.toString(field, 4));

        MinoFactory minoFactory = new MinoFactory();
        MinoShifter minoShifter = new MinoShifter();
        MinoRotation minoRotation = MinoRotation.create();
        ColorConverter colorConverter = new ColorConverter();
        PerfectValidator perfectValidator = new PerfectValidator();
        PutterUsingHold<Action> putterhold = new PutterUsingHold<>(minoFactory, perfectValidator);
        PutterNoHold<Action> putterwithouthold = new PutterNoHold<>(minoFactory, perfectValidator);
        boolean setupUsingHold = settings.isSetupUsingHold();

        output("Start Setup Finding");
        output();

        List<Pieces> pieces = setup_generator.blocksStream().collect(Collectors.toList());


        for (Pieces piece : pieces) {
            String using = piece.blockStream().map(Piece::getName).collect(Collectors.joining());
            output("   -> " + using);
            TreeSet<Order> first;
            if (setupUsingHold)
                first = putterhold.first(field, piece.getPieceArray(), new LockedCandidate(minoFactory, minoShifter, minoRotation, maxClearLine), maxClearLine, setup_maxDepth);
            else
                first = putterwithouthold.first(field, piece.getPieceArray(), new LockedCandidate(minoFactory, minoShifter, minoRotation, maxClearLine), maxClearLine, setup_maxDepth);
            double highest_percent = 0.0;
            Field bestSetup = field;
            int maxFailures = normalEnumeratePieces.getCounter();
            int checked = 0;
            output("  -> Stopwatch start");
            Stopwatch stopwatch = Stopwatch.createStartedStopwatch();
            long last100time = 0;
            BlockField blockField = null;
            int hundreds = 0;

            // provided percent cutoff takes precedence over best known field

            Field bestKnownSetup = settings.getBestKnownSetup();
            double cutoffPercent = settings.getCutoffPercent();
            if (cutoffPercent != 0.0) {
//                output("Pre max: " + maxFailures);
                maxFailures = (int)((maxFailures + 1) * (1 - cutoffPercent));
                if (maxFailures == 0)
                    maxFailures = 1;
                highest_percent = cutoffPercent;
                output("Maximum failures set to: " + maxFailures);
            }
            else if (bestKnownSetup != null) {
                output("Best known setup:");
                output(FieldView.toString(bestKnownSetup, 4));
                percentCore.run(bestKnownSetup, searchingPieces, maxClearLine, solve_maxDepth, maxFailures);
                highest_percent = percentCore.getResultTree().getSuccessPercent();
                output("Best known setup percent: " + highest_percent);
                bestSetup = bestKnownSetup;
                maxFailures = percentCore.getResultTree().getFailures();
            }

            output(" Total fields found: " + first.size());

            MyFile base = new MyFile("output/hundreds.txt");
            base.mkdirs();
            BufferedWriter bw;
            try {bw = base.newBufferedWriter();}
            catch (FileNotFoundException e) {return;}

            for (Order order : first) {
                checked++;
                if (checked%500 == 0) {
                    System.out.println("Checked " + checked + "/" + first.size() + ". Last 500 took " + (stopwatch.timesincestart()-last100time) + "\r");
                    last100time = stopwatch.timesincestart();
                }
                Stream<Operation> operationStream = order.getHistory().getOperationStream();
                List<MinoOperationWithKey> operationWithKeys = OperationTransform.parseToOperationWithKeys(field, new Operations(operationStream), minoFactory, maxClearLine);
//                BlockField blockField = OperationTransform.parseToBlockField(operationWithKeys, minoFactory, maxClearLine);

                Field toCheckField = FieldFactory.copyField(field);
                for (MinoOperationWithKey operationWithKey : operationWithKeys) {
                    toCheckField.put(operationWithKey.getMino(), operationWithKey.getX(), operationWithKey.getY());
                    //output("   " +  operationWithKey.getMino() + operationWithKey.getX() + operationWithKey.getY());
                }
//                output(FieldView.toString(toCheckField,maxClearLine));
                percentCore.run(toCheckField, searchingPieces, maxClearLine, solve_maxDepth, maxFailures);
                double percent = percentCore.getResultTree().getSuccessPercent();
//                double t = percentCore.getResultTree().getFailures();
//                double h = percentCore.getResultTree().getSuccesses();
//                output(""+percent + "    " + t + "    " + h);
                if (percent > highest_percent) {
                    maxFailures = percentCore.getResultTree().getFailures();
                    highest_percent = percent;
                    bestSetup = toCheckField;

                    output("New Best Field:");
                    output(FieldView.toString(toCheckField,maxClearLine));
                    output(""+percent);
                    output("Max Failures is now " + maxFailures);
                    blockField = OperationTransform.parseToBlockField(operationWithKeys, minoFactory, maxClearLine);
                }
                if (percent == 1.0) {
//                    break;
                    bestSetup = toCheckField;
                    blockField = OperationTransform.parseToBlockField(operationWithKeys, minoFactory, maxClearLine);
                    try {bw.write(encodeColor(toCheckField, minoFactory, colorConverter, blockField));
                    bw.newLine();
                    bw.flush();}
                    catch (IOException e) {output("Exception");};
                    output("Hundred: " + encodeColor(toCheckField, minoFactory, colorConverter, blockField));
                    output(FieldView.toString(toCheckField,maxClearLine));
                    maxFailures = 1;
                }

//                if (highest_percent == 1.0) break;


                // use: percentCore.run(field, searchingPieces, maxClearLine, maxDepth)
                // AnalyzeTree tree = percentCore.getResultTree();
                // tree.show() gives percent




                //output("   --> " + using + " " + encodeColor(field, minoFactory, colorConverter, blockField));
                //output();
            }

            stopwatch.stop();
            output("  -> Stopwatch stop : " + stopwatch.toMessage(TimeUnit.MILLISECONDS));

            output("Best success percent: " + highest_percent);
            if (highest_percent > 0) {
                output("Best setup for " + piece.toString() + " with solve queue " + solve_patterns.toString().substring(0, Math.min(solve_patterns.toString().length(), 100)) + ":");
                output(FieldView.toString(bestSetup));
                if (null != blockField)
                    output(encodeColor(field, minoFactory, colorConverter, blockField));
            } else {
                output("No viable setup found for " + piece.toString());
            }
        }


    }

    private String encodeColor(Field initField, MinoFactory minoFactory, ColorConverter colorConverter, BlockField blockField) {
        TetfuElement tetfuElement = parseColorElement(initField, colorConverter, blockField, "");
        return encodeOnePage(minoFactory, colorConverter, tetfuElement);
    }

    private TetfuElement parseColorElement(Field initField, ColorConverter colorConverter, BlockField blockField, String comment) {
        ColoredField coloredField = ColoredFieldFactory.createGrayField(initField);

        for (Piece piece : Piece.values()) {
            Field target = blockField.get(piece);
            ColorType colorType = colorConverter.parseToColorType(piece);
            fillInField(coloredField, colorType, target);
        }

        return new TetfuElement(coloredField, ColorType.Empty, Rotate.Reverse, 0, 0, comment);
    }

    private void fillInField(ColoredField coloredField, ColorType colorType, Field target) {
        for (int y = 0; y < target.getMaxFieldHeight(); y++) {
            for (int x = 0; x < 10; x++) {
                if (!target.isEmpty(x, y))
                    coloredField.setColorType(colorType, x, y);
            }
        }
    }

    private String encodeOnePage(MinoFactory minoFactory, ColorConverter colorConverter, TetfuElement tetfuElement) {
        Tetfu tetfu = new Tetfu(minoFactory, colorConverter);
        List<TetfuElement> elementOnePage = Collections.singletonList(tetfuElement);
        return "v115@" + tetfu.encode(elementOnePage);
    }

    private ExecutorService createExecutorService() throws FinderExecuteException {
        int threadCount = settings.getThreadCount();
        if (threadCount == 1) {
            // single thread
            output("Threads = 1");
            return null;
        } else if (1 < threadCount) {
            // Specified thread count
            output("Threads = " + threadCount);
            return new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
//            return Executors.newFixedThreadPool(threadCount);
        } else {
            // NOT specified thread count
            int core = Runtime.getRuntime().availableProcessors();
            output("Threads = " + core);
            return new ThreadPoolExecutor(core, core, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
//            return Executors.newFixedThreadPool(core);
        }
    }

    private ThreadLocal<? extends Candidate<Action>> createCandidateThreadLocal(DropType dropType, int maxClearLine) throws FinderInitializeException {
        switch (dropType) {
            case Softdrop:
                return new LockedCandidateThreadLocal(maxClearLine);
            case Harddrop:
                return new HarddropCandidateThreadLocal();
            case SoftdropTOnly:
                return new SoftdropTOnlyCandidateThreadLocal(maxClearLine);
            case Rotation180:
                return new SRSAnd180CandidateThreadLocal(maxClearLine);
        }
        throw new FinderInitializeException("Unsupport droptype: droptype=" + dropType);
    }

    private ThreadLocal<? extends Reachable> createReachableThreadLocal(DropType dropType, int maxClearLine) throws FinderInitializeException {
        switch (dropType) {
            case Softdrop:
                return new LockedReachableThreadLocal(maxClearLine);
            case Harddrop:
                return new HarddropReachableThreadLocal(maxClearLine);
            case SoftdropTOnly:
                return new SoftdropTOnlyReachableThreadLocal(maxClearLine);
            case Rotation180:
                return new SRSAnd180ReachableThreadLocal(maxClearLine);
        }
        throw new FinderInitializeException("Unsupport droptype: droptype=" + dropType);
    }

    private void output() throws FinderExecuteException {
        output("");
    }

    private void output(String str) throws FinderExecuteException {
        try {
            logWriter.append(str).append(LINE_SEPARATOR);
        } catch (IOException e) {
            throw new FinderExecuteException(e);
        }

        if (settings.isOutputToConsole())
            System.out.println(str);
    }

    private void flush() throws FinderExecuteException {
        try {
            logWriter.flush();
        } catch (IOException e) {
            throw new FinderExecuteException(e);
        }
    }

    @Override
    public void close() throws FinderTerminateException {
        try {
            flush();
            logWriter.close();
        } catch (IOException | FinderExecuteException e) {
            throw new FinderTerminateException(e);
        }
    }
}
