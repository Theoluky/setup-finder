package main;

import a.Depend;
import a.Estimate;
import a.MinoPivot;
import action.candidate.FixPlaceLockedCandidate;
import action.candidate.LockedCandidate;
import action.reachable.LockedReachable;
import action.reachable.Reachable;
import analyzer.CheckerTree;
import analyzer.VisitedTree;
import core.field.Field;
import core.field.FieldFactory;
import core.field.MiddleField;
import core.mino.Block;
import core.mino.Mino;
import core.mino.MinoFactory;
import core.mino.MinoShifter;
import core.srs.MinoRotation;
import misc.Stopwatch;
import misc.iterable.CombinationIterable;
import misc.iterable.PermutationIterable;
import searcher.checker.Checker;
import searcher.common.Operation;
import searcher.common.Result;
import searcher.common.action.Action;
import searcher.common.validator.BuildValidator;
import searcher.common.validator.Validator;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static core.mino.Block.*;

public class Experiment {
    public static final int FIELD_WIDTH = 10;

    public static void main(String[] args) {
//        main1();

        String marks = "" +
                "____X_____" +
                "___XXXX___" +
                "____XXXXX_" +
                "XX_XXXXXXX" +
                "XX_XXXXXXX" +
                "";
        Field field = FieldFactory.createField(marks);

        List<Block> usingBlocks = Arrays.asList(I, T, S, Z, J, L, O);
        int combinationPopCount = 7;

        ArrayList<OrderPieces> searchingPieces = new ArrayList<>();
        // 組み合わせの列挙
        Iterable<List<Block>> combinationIterable = new CombinationIterable<>(usingBlocks, combinationPopCount);
        for (List<Block> combination : combinationIterable) {
            // 組み合わせから、順列を列挙
            Iterable<List<Block>> permutationIterable = new PermutationIterable<>(combination);
            for (List<Block> permutation : permutationIterable) {
                searchingPieces.add(new OrderPieces(permutation));
            }
        }

        int maxY = 5;
        MinoFactory minoFactory = new MinoFactory();
        MinoShifter minoShifter = new MinoShifter();
        MinoRotation minoRotation = new MinoRotation();
        FixPlaceLockedCandidate candidate = new FixPlaceLockedCandidate(minoFactory, minoShifter, minoRotation, maxY, field);
        BuildValidator validator = new BuildValidator(field);

        Checker<Action> builder = new Checker<>(minoFactory, validator);

        Stopwatch stopwatch = Stopwatch.createStoppedStopwatch();
        VisitedTree visitedTree = new VisitedTree();

        int maxDepth = usingBlocks.size();
        if (10 < maxDepth)
            throw new IllegalArgumentException("Max Depth should be <= 10");

        stopwatch.start();
        for (OrderPieces pieces : searchingPieces) {
            List<Block> blocks = pieces.getBlocks();

            if (visitedTree.isVisited(blocks)) {
                continue;
            }

            MiddleField currentField = new MiddleField();
            boolean checkResult = builder.check(currentField, blocks, candidate, maxY, maxDepth);
            visitedTree.set(checkResult, blocks);

            if (checkResult) {
                Result result = builder.getResult();
                List<Operation> operations = result.createOperations();
                ArrayList<Block> operationBlocks = new ArrayList<>();
                for (Operation operation : operations) {
                    operationBlocks.add(operation.getBlock());
                }

                int reverseMaxDepth = result.getLastHold() != null ? operationBlocks.size() + 1 : operationBlocks.size();
                ArrayList<Pieces> reversePieces = Experiment2.reverse(operationBlocks, reverseMaxDepth);
                for (Pieces piece : reversePieces) {
                    visitedTree.set(true, piece.getBlocks());
                }
            }
        }
        stopwatch.stop();

        CheckerTree tree = new CheckerTree();
        for (OrderPieces piece : searchingPieces) {
            List<Block> blocks = piece.getBlocks();
            int result = visitedTree.isSucceed(blocks);
            assert result != VisitedTree.NO_RESULT;
            tree.set(result == VisitedTree.SUCCEED, blocks);
        }

        System.out.println(stopwatch.toMessage(TimeUnit.MILLISECONDS));
        System.out.println(tree.show());
        System.out.println(tree.tree(2));
    }

    private static void main2() {
        String marks = "" +
                "____X_____" +
                "___XXXX___" +
                "____XXXXX_" +
                "XX_XXXXXXX" +
                "XX_XXXXXXX" +
                "";
        Field field = FieldFactory.createField(marks);

        List<Block> usingBlocks = Arrays.asList(I, T, S, Z, J, L, O);
        int combinationPopCount = 7;

        LinkedList<OrderPieces> searchingPieces = new LinkedList<>();
        // 組み合わせの列挙
        Iterable<List<Block>> combinationIterable = new CombinationIterable<>(usingBlocks, combinationPopCount);
        for (List<Block> combination : combinationIterable) {
            // 組み合わせから、順列を列挙
            Iterable<List<Block>> permutationIterable = new PermutationIterable<>(combination);
            for (List<Block> permutation : permutationIterable) {
                searchingPieces.add(new OrderPieces(permutation));
            }
        }

        int maxY = 5;
        MinoFactory minoFactory = new MinoFactory();
        MinoShifter minoShifter = new MinoShifter();
        MinoRotation minoRotation = new MinoRotation();
        LockedCandidate candidate = new LockedCandidate(minoFactory, minoShifter, minoRotation, maxY);
        BuildValidator validator = new BuildValidator(field);

        Reachable reachable = new LockedReachable(minoFactory, minoShifter, minoRotation, maxY);

        Checker<Action> builder = new Checker<>(minoFactory, validator);

        HashSet<OrderPieces> duplicated = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStoppedStopwatch();
        CheckerTree tree = new CheckerTree();

        int maxDepth = usingBlocks.size();
        if (10 < maxDepth)
            throw new IllegalArgumentException("Max Depth should be <= 10");

        while (!searchingPieces.isEmpty()) {
            OrderPieces pieces = searchingPieces.poll();
            List<Block> blocks = pieces.getBlocks();

            stopwatch.start();
            if (duplicated.contains(pieces)) {
                tree.set(true, blocks);
                stopwatch.stop();
                continue;
            }

            MiddleField currentField = new MiddleField();
            boolean isOK = builder.check(currentField, blocks, candidate, maxY, maxDepth);
            tree.set(isOK, blocks);
            if (isOK) {
                duplicated.add(pieces);
            } else {
                stopwatch.stop();
                continue;
            }
            System.out.println(duplicated.contains(pieces));

            Result result = builder.getResult();
            System.out.println(blocks);
            System.out.println(result);
            List<Operation> operations2 = result.createOperations();

            PermutationIterable<Operation> permutations = new PermutationIterable<>(operations2);
            for (List<Operation> allOperations : permutations) {
                ArrayList<Block> okPiece = new ArrayList<>();
                for (Operation allOperation : allOperations) {
                    okPiece.add(allOperation.getBlock());
                }
                OrderPieces okPieces = new OrderPieces(okPiece);
                if (duplicated.contains(okPieces)) {
                    continue;
                }

                if (isBuild(currentField, minoFactory, reachable, validator, allOperations, maxY)) {
                    duplicated.add(okPieces);
                }
            }
            System.out.println(duplicated.size());
//                System.out.println(stopwatch1.toMessage(TimeUnit.MICROSECONDS));


            stopwatch.stop();
        }
        System.out.println(stopwatch.toMessage(TimeUnit.MICROSECONDS));
        System.out.println(tree.tree(2));
    }

    private static boolean isBuild(Field currentField, MinoFactory minoFactory, Reachable reachable, Validator validator, List<Operation> operations, int maxY) {
        Field freeze = currentField.freeze(maxY);
        for (Operation operation : operations) {
            long deleteKey = freeze.clearLineReturnKey();
            Mino mino = minoFactory.create(operation.getBlock(), operation.getRotate());
            int x = operation.getX();
            int y = operation.getY();
            if (!freeze.canPutMino(mino, x, y) || !freeze.isOnGround(mino, x, y) || !reachable.checks(freeze, mino, x, y, maxY))
                return false;

            freeze.putMino(mino, x, y);

            if (!validator.validate(freeze, maxY))
                return false;

            freeze.insertLineWithKey(deleteKey);
        }
        return true;
    }

    private static void main1() {
//        String marks = "" +
//                "____6_____" +
//                "___6663___" +
//                "____55322_" +
//                "00_5543122" +
//                "00_4443111" +
//                "";
//
//        List<Block> blocks = Arrays.asList(O, J, Z, I, L, S, T);

        String marks = "" +
                "__________" +
                "___22____5" +
                "0___224445" +
                "00_1133345" +
                "_0_113___5" +
                "";

        List<Block> blocks = Arrays.asList(S, O, Z, L, J, I);

        // フィールド番号 -> 各ミノの回転方向と回転軸
        int[][] numberField = createNumberField(marks);
        MinoFactory minoFactory = new MinoFactory();
        Estimate estimate = new Estimate(minoFactory);
        List<MinoPivot> minoPivots = estimate.create(numberField, blocks);

        System.out.println(minoPivots);
        System.out.println(minoPivots.stream().map(MinoPivot::getMino).map(Mino::getRotate).collect(Collectors.toList()));

        //
        int maxY = 5;
        MinoShifter minoShifter = new MinoShifter();
        MinoRotation minoRotation = new MinoRotation();
        LockedReachable reachable = new LockedReachable(minoFactory, minoShifter, minoRotation, maxY);
        Depend depend = new Depend(reachable);

        Field field = FieldFactory.createField(marks);
        Map<Integer, Set<Integer>> results = depend.extract(field, numberField, minoPivots);
        System.out.println(results);
    }

    private static int[][] createNumberField(String marks) {
        if (marks.length() % FIELD_WIDTH != 0)
            throw new IllegalArgumentException("length of marks should be 'mod 10'");

        int maxY = marks.length() / FIELD_WIDTH;

        int[][] field = new int[maxY][FIELD_WIDTH];

        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < FIELD_WIDTH; x++) {
                char mark = marks.charAt((maxY - y - 1) * FIELD_WIDTH + x);
                if (mark != ' ' && mark != '_')
                    field[y][x] = Integer.valueOf(String.valueOf(mark));
                else
                    field[y][x] = -1;
            }
        }

        return field;
    }

    // num of blocks <= 10 であること
    private static class OrderPieces {
        private final List<Block> blocks;

        private final int hash;

        public OrderPieces(List<Block> blocks) {
            this.blocks = blocks;
            this.hash = calculateHash(blocks);
        }

        private int calculateHash(List<Block> blocks) {
            int size = blocks.size();
            int number = blocks.get(size - 1).getNumber();
            for (int index = size - 2; 0 <= index; index--) {
                number *= 8;
                number += blocks.get(index).getNumber();
            }
            return number;
        }

        public List<Block> getBlocks() {
            return blocks;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderPieces pieces = (OrderPieces) o;
            return hash == pieces.hash;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }
    }
}
