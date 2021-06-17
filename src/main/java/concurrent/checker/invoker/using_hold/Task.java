package concurrent.checker.invoker.using_hold;

import common.ResultHelper;
import common.buildup.BuildUpStream;
import common.datastore.*;
import common.datastore.action.Action;
import common.datastore.blocks.Pieces;
import common.order.OrderLookup;
import common.parser.OperationTransform;
import common.tree.VisitedTree;
import concurrent.checker.invoker.CheckerCommonObj;
import core.action.candidate.Candidate;
import core.mino.Piece;
import searcher.checker.Checker;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

class Task implements Callable<Pair<Pieces, Boolean>> {
    private final Obj obj;
    private final CheckerCommonObj commonObj;
    private final Pieces target;

    Task(Obj obj, CheckerCommonObj commonObj, Pieces target) {
        this.obj = obj;
        this.commonObj = commonObj;
        this.target = target;
    }

    @Override
    public Pair<Pieces, Boolean> call() throws Exception {
        List<Piece> pieceList = target.getPieces();
        if (Thread.currentThread().isInterrupted()) System.out.println("Interrupted 1");
        // すでに探索済みならそのまま結果を追加
        int succeed = obj.visitedTree.isSucceed(pieceList);
        if (Thread.currentThread().isInterrupted()) System.out.println("Interrupted 2");
        if (succeed != VisitedTree.NO_RESULT)
            return new Pair<>(target, succeed == VisitedTree.SUCCEED);
        if (Thread.currentThread().isInterrupted()) System.out.println("Interrupted 3");
        // 探索準備
        Checker<Action> checker = commonObj.checkerThreadLocal.get();
        Candidate<Action> candidate = commonObj.candidateThreadLocal.get();
        if (Thread.currentThread().isInterrupted()) System.out.println("Interrupted 4");
        // 探索
        boolean checkResult = checker.check(obj.field, pieceList, candidate, obj.maxClearLine, obj.maxDepth);
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        if (Thread.currentThread().isInterrupted()) System.out.println("Interrupted enye");
        obj.visitedTree.set(checkResult, pieceList);
        // here we are often interrupted
        if (Thread.currentThread().isInterrupted()) {
            return null;
        }
        if (Thread.currentThread().isInterrupted()) System.out.println("Interrupted 5");
        // もし探索に成功した場合
        // パフェが見つかったツモ順(≠探索時のツモ順)へと、ホールドを使ってできるパターンを逆算
        if (checkResult) {
            Result result = checker.getResult();
            List<Piece> resultPieceList = ResultHelper.createOperationStream(result)
                    .map(Operation::getPiece)
                    .collect(Collectors.toList());

            int reverseMaxDepth = result.getLastHold() != null ? resultPieceList.size() + 1 : resultPieceList.size();
            OrderLookup.reverseBlocks(resultPieceList, reverseMaxDepth)
                    .forEach(piece -> obj.visitedTree.set(true, piece.toList()));
        }
        if (Thread.currentThread().isInterrupted()) System.out.println("Interrupted 6");
        return new Pair<>(target, checkResult);
    }
}
