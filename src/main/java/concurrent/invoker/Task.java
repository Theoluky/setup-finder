package concurrent.invoker;

import action.candidate.Candidate;
import core.mino.Block;
import searcher.checker.Checker;
import searcher.common.Operation;
import searcher.common.Result;
import searcher.common.action.Action;
import tree.VisitedTree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

class Task implements Callable<Pair<List<Block>, Boolean>> {
    private final Obj obj;
    private final List<Block> target;

    Task(Obj obj, List<Block> target) {
        this.obj = obj;
        this.target = target;
    }

    @Override
    public Pair<List<Block>, Boolean> call() throws Exception {
        // すでに探索済みならそのまま結果を追加
        int succeed = obj.visitedTree.isSucceed(target);
        if (succeed != VisitedTree.NO_RESULT)
            return new Pair<>(target, succeed == VisitedTree.SUCCEED);

        // 探索準備
        Checker<Action> checker = obj.checkerThreadLocal.get();
        Candidate<Action> candidate = obj.candidateThreadLocal.get();

        // 探索
        boolean checkResult = checker.check(obj.field, target, candidate, obj.maxClearLine, obj.maxDepth);
        obj.visitedTree.set(checkResult, target);

        // もし探索に成功した場合
        // パフェが見つかったツモ順(≠探索時のツモ順)へと、ホールドを使ってできるパターンを逆算
        if (checkResult) {
            Result result = checker.getResult();
            List<Operation> operations = result.createOperations();
            ArrayList<Block> operationBlocks = parseOperationsToBlockList(operations);

            int reverseMaxDepth = result.getLastHold() != null ? operationBlocks.size() + 1 : operationBlocks.size();
            ArrayList<Pieces> reversePieces = OrderLookup.reverse(operationBlocks, reverseMaxDepth);
            for (Pieces piece : reversePieces)
                obj.visitedTree.set(true, piece.getBlocks());
        }

        return new Pair<>(target, checkResult);
    }

    private ArrayList<Block> parseOperationsToBlockList(List<Operation> operations) {
        ArrayList<Block> operationBlocks = new ArrayList<>();
        for (Operation operation : operations)
            operationBlocks.add(operation.getBlock());
        return operationBlocks;
    }
}
