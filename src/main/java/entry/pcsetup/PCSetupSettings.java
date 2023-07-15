package entry.pcsetup;

import common.tetfu.common.ColorType;
import common.tetfu.field.ColoredField;
import core.field.Field;
import core.field.FieldFactory;
import entry.DropType;
import exceptions.FinderParseException;

import java.util.ArrayList;
import java.util.List;

public class PCSetupSettings {
    private static final int EMPTY_BLOCK_NUMBER = ColorType.Empty.getNumber();
    private static final String DEFAULT_LOG_FILE_PATH = "output/last_output.txt";

    private boolean isUsingHold = true;
    private int maxClearLine = 4;
    private Field bestKnownSetup = null;
    private Field startField = null;
    private String logFilePath = DEFAULT_LOG_FILE_PATH;
    private List<String> setup_patterns = new ArrayList<>();
    private List<String> solve_patterns = new ArrayList<>();
    private int treeDepth = 3;
    private int failedCount = 100;
    private int threadCount = -1;
    private DropType dropType = DropType.Softdrop;
    private double cutoffPercent = 0.0;
    private boolean setupUsingHold = true;

    // ********* Getter ************
    public boolean isUsingHold() {
        return isUsingHold;
    }

    boolean isOutputToConsole() {
        return true;
    }

    Field getBestKnownSetup() {
        return bestKnownSetup;
    }

    Field getStartfield() {return startField;}

    int getMaxClearLine() {
        return maxClearLine;
    }

    String getLogFilePath() {
        return logFilePath;
    }

    List<String> getSetupPatterns() {
        return setup_patterns;
    }

    List<String> getSolvePatterns() {return solve_patterns; }

    int getTreeDepth() {
        return treeDepth;
    }

    int getFailedCount() {
        return failedCount;
    }

    DropType getDropType() {
        return dropType;
    }

    int getThreadCount() {
        return threadCount;
    }

    double getCutoffPercent() { return cutoffPercent; }

    boolean isSetupUsingHold() {return setupUsingHold; }

    // ********* Setter ************
    public void setMaxClearLine(int maxClearLine) {
        this.maxClearLine = maxClearLine;
    }

    void setUsingHold(Boolean isUsingHold) {
        this.isUsingHold = isUsingHold;
    }

    void setBestKnownSetup(ColoredField coloredField) {
        if (coloredField != null)
            setField(coloredField, this.maxClearLine);
    }

    void setStart(ColoredField coloredField, int height) {
        Field field = FieldFactory.createField(height);
        if (coloredField != null) {
            for (int y = 0; y < height; y++)
                for (int x = 0; x < 10; x++)
                    if (coloredField.getBlockNumber(x, y) != EMPTY_BLOCK_NUMBER)
                        field.setBlock(x, y);
        }
        this.startField = field;
    }

    void setField(ColoredField coloredField, int height) {
        Field field = FieldFactory.createField(height);
        for (int y = 0; y < height; y++)
            for (int x = 0; x < 10; x++)
                if (coloredField.getBlockNumber(x, y) != EMPTY_BLOCK_NUMBER)
                    field.setBlock(x, y);
        setFieldFilePath(field);
    }

    void setFieldFilePath(Field field) {
        this.bestKnownSetup = field;
    }

    void setLogFilePath(String path) {
        this.logFilePath = path;
    }

    void setSolvePatterns(List<String> patterns) {
        this.solve_patterns = patterns;
    }

    void setSetupPatterns(List<String> patterns) { this.setup_patterns = patterns; }

    void setTreeDepth(int depth) {
        this.treeDepth = depth;
    }

    void setFailedCount(int maxCount) {
        this.failedCount = maxCount;
    }

    void setThreadCount(int thread) {
        this.threadCount = thread;
    }

    void setCutoffPercent(double cutoff) {
        this.cutoffPercent = cutoff;
    }

    void setSetupUsingHold(boolean setupUsingHold) {this.setupUsingHold = setupUsingHold; }

    void setDropType(String type) throws FinderParseException {
        switch (type.trim().toLowerCase()) {
            case "soft":
            case "softdrop":
                this.dropType = DropType.Softdrop;
                return;
            case "hard":
            case "harddrop":
                this.dropType = DropType.Harddrop;
                return;
            case "180":
                this.dropType = DropType.Rotation180;
                return;
            case "tsoft":
            case "tsoftdrop":
            case "t-soft":
            case "t-softdrop":
            case "t_soft":
            case "t_softdrop":
                this.dropType = DropType.SoftdropTOnly;
                return;
            default:
                throw new FinderParseException("Unsupported droptype: type=" + type);
        }
    }

}
