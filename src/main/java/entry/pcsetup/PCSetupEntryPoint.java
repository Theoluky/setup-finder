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
import lib.Stopwatch;
import searcher.PutterNoHold;
import searcher.PutterUsingHold;
import searcher.common.validator.PerfectValidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        for (String pattern : solve_patterns)
            output("   " + pattern);
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
        PercentCore percentCore = new PercentCore(executorService, candidateThreadLocal, settings.isUsingHold(), reachableThreadLocal, solve_minoFactory);


        // setup finding intialization:
        Field field = FieldFactory.createField(4);

        MinoFactory minoFactory = new MinoFactory();
        MinoShifter minoShifter = new MinoShifter();
        MinoRotation minoRotation = MinoRotation.create();
        ColorConverter colorConverter = new ColorConverter();
        PerfectValidator perfectValidator = new PerfectValidator();
        PutterNoHold<Action> putter = new PutterNoHold<>(minoFactory, perfectValidator);

        output("Start Setup Finding");

        List<Pieces> pieces = setup_generator.blocksStream().collect(Collectors.toList());

        for (Pieces piece : pieces) {
            String using = piece.blockStream().map(Piece::getName).collect(Collectors.joining());
            output("   -> " + using);
            TreeSet<Order> first = putter.first(field, piece.getPieceArray(), new LockedCandidate(minoFactory, minoShifter, minoRotation, maxClearLine), maxClearLine, setup_maxDepth);
            output(" Total fields found: " + first.size());
            for (Order order : first) {
                Stream<Operation> operationStream = order.getHistory().getOperationStream();
                List<MinoOperationWithKey> operationWithKeys = OperationTransform.parseToOperationWithKeys(field, new Operations(operationStream), minoFactory, maxClearLine);
                BlockField blockField = OperationTransform.parseToBlockField(operationWithKeys, minoFactory, maxClearLine);

                //output("   --> " + using + " " + encodeColor(field, minoFactory, colorConverter, blockField));
                //output();
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
            return Executors.newFixedThreadPool(threadCount);
        } else {
            // NOT specified thread count
            int core = Runtime.getRuntime().availableProcessors();
            output("Threads = " + core);
            return Executors.newFixedThreadPool(core);
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
