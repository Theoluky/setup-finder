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
    private final BlockingQueue<Future<Pair<Pieces,Boolean>>> executeQueue;
    private CompletionService<Pair<Pieces,Boolean>> executorCompletionService;

    public ConcurrentCheckerUsingHoldInvoker(ExecutorService executorService, CheckerCommonObj commonObj) {
        this.executorService = executorService;
        this.commonObj = commonObj;
        this.executeQueue = new ArrayBlockingQueue<>(5040);
        this.executorCompletionService = new ExecutorCompletionService<>(this.executorService, this.executeQueue);
    }

    @Override
    public List<Pair<Pieces, Boolean>> search(Field field, List<Pieces> searchingPieces, int maxClearLine, int maxDepth) throws FinderExecuteException {
        ConcurrentVisitedTree visitedTree = new ConcurrentVisitedTree();

        Obj obj = new Obj(field, maxClearLine, maxDepth, visitedTree);
        ArrayList<Task> tasks = new ArrayList<>();
        for (Pieces target : searchingPieces)
            tasks.add(new Task(obj, commonObj, target));

        try {
            return execute(tasks, tasks.size());
        } catch (InterruptedException | ExecutionException e) {
            throw new FinderExecuteException(e);
        }
    }

    @Override
    public List<Pair<Pieces, Boolean>> search(Field field, List<Pieces> searchingPieces, int maxClearLine, int maxDepth, int maxFailures) throws FinderExecuteException {
        ConcurrentVisitedTree visitedTree = new ConcurrentVisitedTree();

        Obj obj = new Obj(field, maxClearLine, maxDepth, visitedTree);
        ArrayList<Task> tasks = new ArrayList<>();
        for (Pieces target : searchingPieces)
            tasks.add(new Task(obj, commonObj, target));

        try {
            return execute(tasks, maxFailures);
        } catch (InterruptedException | ExecutionException e) {
            throw new FinderExecuteException(e);
        }
    }

    private ArrayList<Pair<Pieces, Boolean>> execute(ArrayList<Task> tasks, int maxFailures) throws InterruptedException, ExecutionException {
        //List<Future<Pair<Pieces, Boolean>>> futureResults = executorService.invokeAll(tasks);
        List<Future<Pair<Pieces,Boolean>>> futures = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            futures.add(executorCompletionService.submit(task));
        }
        // 結果をリストに追加する
        int toComplete = tasks.size();
        int received = 0;
        int failed = 0;
        ArrayList<Pair<Pieces, Boolean>> pairs = new ArrayList<>();
        // TODO: update to a CompletionService (https://stackoverflow.com/questions/19348248/waiting-on-a-list-of-future)
//        System.out.println("Starting to receive futures");
        while (received < toComplete) {
            Future<Pair<Pieces, Boolean>> resFuture = executorCompletionService.take();
            received++;
            Pair<Pieces, Boolean> res = resFuture.get();
            if (!res.getValue()) {
                failed++;
            }
            if (failed > maxFailures) {
                break;
            }
            pairs.add(res);

        }
        if (failed > maxFailures) {
//            System.out.println("Cancelling all futures");
//            while (received < toComplete) {
//                executorCompletionService.take();
//                received++;
//            }
            executeQueue.clear();
            //executorCompletionService = new ExecutorCompletionService<>(this.executorService);
//            System.out.println("Cancelled futures");
        }

//        for (Future<Pair<Pieces, Boolean>> future : futureResults) {
//            pairs.add(future.get());
//        }
//        System.out.print("Had " + failed + " failures");
        return pairs;
    }
}

