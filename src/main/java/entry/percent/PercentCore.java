package entry.percent;

import common.datastore.Pair;
import common.datastore.action.Action;
import common.datastore.blocks.LongPieces;
import common.datastore.blocks.Pieces;
import common.tree.AnalyzeTree;
import concurrent.checker.CheckerNoHoldThreadLocal;
import concurrent.checker.CheckerUsingHoldThreadLocal;
import concurrent.checker.invoker.CheckerCommonObj;
import concurrent.checker.invoker.ConcurrentCheckerInvoker;
import concurrent.checker.invoker.no_hold.ConcurrentCheckerNoHoldInvoker;
import concurrent.checker.invoker.no_hold.SingleCheckerNoHoldInvoker;
import concurrent.checker.invoker.using_hold.ConcurrentCheckerUsingHoldInvoker;
import concurrent.checker.invoker.using_hold.SingleCheckerUsingHoldInvoker;
import core.action.candidate.Candidate;
import core.action.reachable.Reachable;
import core.field.Field;
import core.mino.MinoFactory;
import exceptions.FinderExecuteException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class PercentCore {
    private final ConcurrentCheckerInvoker invoker;

    private AnalyzeTree resultTree;
    private List<Pair<Pieces, Boolean>> resultPairs;

    PercentCore(ThreadLocal<? extends Candidate<Action>> candidateThreadLocal, boolean isUsingHold, ThreadLocal<? extends Reachable> reachableThreadLocal, MinoFactory minoFactory) {
        this.invoker = createConcurrentCheckerInvoker(null, candidateThreadLocal, isUsingHold, reachableThreadLocal, minoFactory);
    }

    public PercentCore(ThreadPoolExecutor executorService, ThreadLocal<? extends Candidate<Action>> candidateThreadLocal, boolean isUsingHold, ThreadLocal<? extends Reachable> reachableThreadLocal, MinoFactory minoFactory) {
        this.invoker = createConcurrentCheckerInvoker(executorService, candidateThreadLocal, isUsingHold, reachableThreadLocal, minoFactory);
    }

    /**
     * Pass null to ExecutorService if executing on single thread
     */
    private ConcurrentCheckerInvoker createConcurrentCheckerInvoker(ThreadPoolExecutor executorService, ThreadLocal<? extends Candidate<Action>> candidateThreadLocal, boolean isUsingHold, ThreadLocal<? extends Reachable> reachableThreadLocal, MinoFactory minoFactory) {
        if (isUsingHold) {
            CheckerUsingHoldThreadLocal<Action> checkerThreadLocal = new CheckerUsingHoldThreadLocal<>();
            CheckerCommonObj commonObj = new CheckerCommonObj(minoFactory, candidateThreadLocal, checkerThreadLocal, reachableThreadLocal);
            if (executorService == null)
                return new SingleCheckerUsingHoldInvoker(commonObj);
            return new ConcurrentCheckerUsingHoldInvoker(executorService, commonObj);
        } else {
            CheckerNoHoldThreadLocal<Action> checkerThreadLocal = new CheckerNoHoldThreadLocal<>();
            CheckerCommonObj commonObj = new CheckerCommonObj(minoFactory, candidateThreadLocal, checkerThreadLocal, reachableThreadLocal);
            if (executorService == null)
                return new SingleCheckerNoHoldInvoker(commonObj);
            return new ConcurrentCheckerNoHoldInvoker(executorService, commonObj);
        }
    }

    public void run(Field field, Set<LongPieces> searchingPiecesSet, int maxClearLine, int maxDepth) throws FinderExecuteException {
        List<Pieces> searchingPieces = new ArrayList<>(searchingPiecesSet);

        this.resultPairs = invoker.search(field, searchingPieces, maxClearLine, maxDepth);

        // 最低限の探索結果を集計する
        this.resultTree = new AnalyzeTree();
        for (Pair<Pieces, Boolean> resultPair : resultPairs) {
            Pieces pieces = resultPair.getKey();
            Boolean result = resultPair.getValue();
            resultTree.set(result, pieces);
        }
    }

    public void run(Field field, Set<LongPieces> searchingPiecesSet, int maxClearLine, int maxDepth, int maxFailures) throws FinderExecuteException {
        List<Pieces> searchingPieces = new ArrayList<>(searchingPiecesSet);

        this.resultPairs = invoker.search(field, searchingPieces, maxClearLine, maxDepth, maxFailures);

        // 最低限の探索結果を集計する
        this.resultTree = new AnalyzeTree();
        for (Pair<Pieces, Boolean> resultPair : resultPairs) {
            Pieces pieces = resultPair.getKey();
            Boolean result = resultPair.getValue();
            resultTree.set(result, pieces);
        }
    }

    public AnalyzeTree getResultTree() {
        return resultTree;
    }

    List<Pair<Pieces, Boolean>> getResultPairs() {
        return resultPairs;
    }
}
