package concurrent.checker.invoker.using_hold;

import common.datastore.Pair;
import common.datastore.blocks.Pieces;
import common.tree.ConcurrentVisitedTree;
import concurrent.checker.invoker.CheckerCommonObj;
import concurrent.checker.invoker.ConcurrentCheckerInvoker;
import core.field.Field;
import exceptions.FinderExecuteException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrentCheckerUsingHoldInvoker implements ConcurrentCheckerInvoker {
    private final ExecutorService executorService;
    private final CheckerCommonObj commonObj;
    private final CompletionService executorCompletionService;

    public ConcurrentCheckerUsingHoldInvoker(ExecutorService executorService, CheckerCommonObj commonObj) {
        this.executorService = executorService;
        this.commonObj = commonObj;
        this.executorCompletionService = new ExecutorCompletionService<>(executorService);
    }

    @Override
    public List<Pair<Pieces, Boolean>> search(Field field, List<Pieces> searchingPieces, int maxClearLine, int maxDepth) throws FinderExecuteException {
        ConcurrentVisitedTree visitedTree = new ConcurrentVisitedTree();

        Obj obj = new Obj(field, maxClearLine, maxDepth, visitedTree);
        ArrayList<Task> tasks = new ArrayList<>();
        for (Pieces target : searchingPieces)
            tasks.add(new Task(obj, commonObj, target));

        try {
            return execute(tasks);
        } catch (InterruptedException | ExecutionException e) {
            throw new FinderExecuteException(e);
        }
    }

    private ArrayList<Pair<Pieces, Boolean>> execute(ArrayList<Task> tasks) throws InterruptedException, ExecutionException {
        List<Future<Pair<Pieces, Boolean>>> futureResults = executorService.invokeAll(tasks);

        // 結果をリストに追加する
        ArrayList<Pair<Pieces, Boolean>> pairs = new ArrayList<>();
        //int failcount = 0;
        // TODO: update to a CompletionService (https://stackoverflow.com/questions/19348248/waiting-on-a-list-of-future)
        for (Future<Pair<Pieces, Boolean>> future : futureResults) {
            //Pair<Pieces, Boolean> p = future.get();
            pairs.add(future.get());
        }
        //System.out.println(failcount);
        return pairs;
    }
}

