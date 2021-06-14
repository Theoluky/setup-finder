package entry.pcsetup;

import common.datastore.Pair;
import common.datastore.action.Action;
import common.datastore.blocks.LongPieces;
import common.datastore.blocks.Pieces;
import common.pattern.PatternGenerator;
import common.tree.AnalyzeTree;
import concurrent.*;
import core.FinderConstant;
import core.action.candidate.Candidate;
import core.action.reachable.Reachable;
import core.field.Field;
import core.field.FieldView;
import core.mino.MinoFactory;
import entry.DropType;
import entry.EntryPoint;
import entry.Verify;
import entry.path.output.MyFile;
import entry.pcsetup.PCSetupSettings;
import entry.percent.PercentSettings;
import entry.searching_pieces.NormalEnumeratePieces;
import exceptions.FinderException;
import exceptions.FinderExecuteException;
import exceptions.FinderInitializeException;
import exceptions.FinderTerminateException;
import lib.Stopwatch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        output("# PC Setup Finder");
        output("# Setup Queues:");
        List<String> setup_patterns = settings.getSetupPatterns();
        PatternGenerator generator = Verify.patterns(setup_patterns);
        for (String pattern : setup_patterns)
            output("   " + pattern);
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
