package searcher.spins.fill.line;

import common.datastore.PieceCounter;
import common.iterable.CombinationIterable;
import core.field.Field;
import core.field.KeyOperators;
import core.mino.Piece;
import core.neighbor.SimpleOriginalPiece;
import searcher.spins.TargetY;
import searcher.spins.fill.line.next.RemainderField;
import searcher.spins.fill.line.next.RemainderFieldRunner;
import searcher.spins.fill.line.spot.*;
import searcher.spins.pieces.SimpleOriginalPieces;
import searcher.spins.results.AddLastsResult;
import searcher.spins.results.EmptyResult;
import searcher.spins.results.Result;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LineFillRunner {
    private static final Set<PieceBlockCount> EMPTY_PIECE_BLOCK_COUNT_SET = Collections.emptySet();
    private static final int MAX_SIZE = 3;

    private final RemainderFieldRunner remainderFieldRunner;
    private final SpotRunner spotRunner;
    private final Map<Piece, Set<PieceBlockCount>> pieceToPieceBlockCounts;
    private final SimpleOriginalPieces simpleOriginalPieces;
    private final int maxTargetHeight;
    private final int fieldHeight;

    private final List<List<Integer>> indexes;
    private final ConcurrentMap<PieceBlockCounts, List<SpotResult>> spotResultCache = new ConcurrentHashMap<>();

    public LineFillRunner(
            Map<PieceBlockCount, List<MinoDiff>> pieceBlockCountToMinoDiffs,
            Map<Piece, Set<PieceBlockCount>> pieceToPieceBlockCounts,
            SimpleOriginalPieces simpleOriginalPieces,
            int maxPieceNum,
            int maxTargetHeight,
            int fieldHeight
    ) {
        assert maxTargetHeight <= fieldHeight;
        this.remainderFieldRunner = new RemainderFieldRunner();
        this.spotRunner = new SpotRunner(pieceBlockCountToMinoDiffs, simpleOriginalPieces);
        this.pieceToPieceBlockCounts = pieceToPieceBlockCounts;
        this.simpleOriginalPieces = simpleOriginalPieces;
        this.maxTargetHeight = maxTargetHeight;
        this.fieldHeight = fieldHeight;

        this.indexes = createIndexes(maxPieceNum);
    }

    private List<List<Integer>> createIndexes(int maxPieceNum) {
        List<List<Integer>> indexes = new ArrayList<>();
        for (int index = 0; index <= maxPieceNum; index++) {
            indexes.add(index, IntStream.range(0, index).boxed().collect(Collectors.toList()));
        }
        return indexes;
    }

    public Stream<Result> search(Field initField, PieceCounter pieceCounter, int targetY, long allowDeletedLine) {
        EmptyResult emptyResult = new EmptyResult(initField, pieceCounter, fieldHeight);

        long filledLine = emptyResult.getAllMergedFilledLine();
        assert (filledLine & KeyOperators.getBitKey(targetY)) == 0L;

        List<RemainderField> remainderFields = remainderFieldRunner.extract(initField, targetY);
        return search(emptyResult, filledLine, allowDeletedLine, targetY, remainderFields, 0);
    }

    private Stream<Result> search(
            Result prevResult, long filledLine, long allowDeletedLine, int targetY,
            List<RemainderField> remainderFields, int index
    ) {
        assert index < remainderFields.size() : index;

        RemainderField remainderField = remainderFields.get(index);
        int blockCount = remainderField.getTargetBlockCount();
        int startX = remainderField.getMinX();

        Field allMergedField = prevResult.getAllMergedField();
        SlidedField slidedField = SlidedField.create(allMergedField, targetY, allowDeletedLine);

        TargetY target = new TargetY(targetY);

        PieceCounter remainderPieceCounter = prevResult.getRemainderPieceCounter();
        if (remainderPieceCounter.isEmpty()) {
            return Stream.empty();
        }

        Stream<Result> stream = searchBlockCounts(remainderPieceCounter, blockCount)
                .flatMap(pieceBlockCountList -> spot(filledLine, slidedField, pieceBlockCountList, prevResult, startX, target));

        if (index == remainderFields.size() - 1) {
            return stream;
        }

        return stream.flatMap(result -> search(result, filledLine, allowDeletedLine, targetY, remainderFields, index + 1));
    }

    private Stream<List<PieceBlockCount>> searchBlockCounts(PieceCounter pieceCounter, int blockCount) {
        assert !pieceCounter.isEmpty();
        assert 0 < blockCount;
        Stream.Builder<List<PieceBlockCount>> builder = Stream.builder();
        searchBlockCounts(builder, new LinkedList<>(pieceCounter.getBlocks()), blockCount, new LinkedList<>());
        return builder.build();
    }

    private void searchBlockCounts(
            Stream.Builder<List<PieceBlockCount>> builder,
            LinkedList<Piece> remainderPieces, int remainderBlockCount, LinkedList<PieceBlockCount> result
    ) {
        // ?????????????????????????????????????????????
        if (remainderPieces.isEmpty()) {
            return;
        }

        Piece headPiece = remainderPieces.pollFirst();

        // `headPiece` ???????????????
        Set<PieceBlockCount> pieceBlockCounts = pieceToPieceBlockCounts.getOrDefault(headPiece, EMPTY_PIECE_BLOCK_COUNT_SET);
        for (PieceBlockCount candidateBlockCount : pieceBlockCounts) {
            // ????????????????????????
            int nextBlockCount = remainderBlockCount - candidateBlockCount.getBlockCount();

            // ??????????????????????????????????????????????????????????????????
            if (nextBlockCount < 0) {
                continue;
            }

            // ???????????????
            result.addLast(candidateBlockCount);

            if (nextBlockCount == 0) {
                builder.accept(new ArrayList<>(result));
            } else {
                searchBlockCounts(builder, remainderPieces, nextBlockCount, result);
            }

            result.removeLast();
        }

        // `headPiece` ?????????????????????
        searchBlockCounts(builder, remainderPieces, remainderBlockCount, result);

        remainderPieces.addFirst(headPiece);
    }

    private Stream<Result> spot(
            long filledLine, SlidedField slidedField, List<PieceBlockCount> pieceBlockCountList,
            Result prevResult, int startX, TargetY targetY
    ) {
        int size = pieceBlockCountList.size();
        long keyBelowY = targetY.getKeyBelowY() & ~filledLine;

        if (size <= MAX_SIZE) {
            // ???????????????????????? `MAX_SIZE` ?????????
            PieceBlockCounts pieceBlockCounts = new PieceBlockCounts(pieceBlockCountList);
            return spot(slidedField, prevResult, startX, targetY, pieceBlockCounts)
                    .filter(result -> isFilledBelowTarget(keyBelowY, result));
        }

        // ???????????????????????? `MAX_SIZE+1` ?????????
        CombinationIterable<Integer> iterable = new CombinationIterable<>(indexes.get(size), MAX_SIZE);

        HashSet<Long> visited = new HashSet<>();

        // ????????????
        return StreamSupport.stream(iterable.spliterator(), false)
                .flatMap(selectedIndexes -> {
                    assert selectedIndexes.size() == MAX_SIZE;

                    Integer i1 = selectedIndexes.get(0);
                    Integer i2 = selectedIndexes.get(1);
                    Integer i3 = selectedIndexes.get(2);

                    PieceBlockCounts pieceBlockCounts = new PieceBlockCounts(
                            Arrays.asList(pieceBlockCountList.get(i1), pieceBlockCountList.get(i2), pieceBlockCountList.get(i3))
                    );

                    if (!visited.add(pieceBlockCounts.getKey())) {
                        return Stream.empty();
                    }

                    int[] indexArray = new int[]{i1, i2, i3};
                    Arrays.sort(indexArray);

                    List<PieceBlockCount> remain = new LinkedList<>(pieceBlockCountList);

                    // ??????????????????????????????????????????index???????????????????????????????????????
                    remain.remove(indexArray[2]);
                    remain.remove(indexArray[1]);
                    remain.remove(indexArray[0]);

                    return spot(slidedField, prevResult, startX, targetY, pieceBlockCounts)
                            .filter(result -> isFilledBelowTarget(keyBelowY, result))
                            .flatMap(result -> {
                                int usingBlockCount = pieceBlockCounts.getUsingBlockCount();

                                Field allMergedField = result.getAllMergedField();
                                SlidedField nextSlidedField = SlidedField.create(allMergedField, slidedField);

                                return spot(filledLine, nextSlidedField, remain, result, startX + usingBlockCount, targetY);
                            });
                });
    }

    private boolean isFilledBelowTarget(long keyBelowY, Result result) {
        long filledLineBelowTarget = result.getAllMergedFilledLine() & keyBelowY;
        return filledLineBelowTarget == 0L;
    }

    private Stream<Result> spot(
            SlidedField slidedField, Result prevResult, int startX, TargetY targetY, PieceBlockCounts pieceBlockCounts
    ) {
        assert pieceBlockCounts.getPieceBlockCountList().size() <= MAX_SIZE;

        Field field = slidedField.getField();
        long filledLine = slidedField.getFilledLine();
        int slideDownY = slidedField.getSlideDownY();

        int slideY = 3 - targetY.getTargetY();

        Stream<Result> stream = spot(pieceBlockCounts).stream()
                .map(spotResult -> {
                    int slideX = startX - spotResult.getStartX();
                    if (slideX < 0) {
                        // ???????????????????????????????????????????????????????????????
                        return null;
                    }

                    int rightX = spotResult.getRightX() + slideX;
                    if (10 <= rightX) {
                        // ???????????????????????????????????????????????????
                        return null;
                    }

                    int minY = spotResult.getMinY();
                    if (minY - slideY < 0) {
                        // ???????????????????????????????????????????????????
                        return null;
                    }

                    int maxY = spotResult.getMaxY();
                    if (maxTargetHeight <= maxY - slideY) {
                        // ???????????????????????????????????????????????????
                        return null;
                    }

                    // ?????????????????????y=3?????????????????????
                    Field usingField = spotResult.getUsingField().freeze();
                    usingField.slideRight(slideX);
                    if (!field.canMerge(usingField)) {
                        // ?????????????????????
                        return null;
                    }

                    List<SimpleOriginalPiece> operations = spotResult.getOperations().stream()
                            .map(originalPiece -> {
                                SimpleOriginalPiece slidedPiece = simpleOriginalPieces.get(
                                        originalPiece.getPiece(), originalPiece.getRotate(),
                                        originalPiece.getX() + slideX, originalPiece.getY() + slideDownY
                                );
                                assert slidedPiece != null : originalPiece + " " + slideX + "," + slideDownY;
                                Field freeze = slidedPiece.getMinoField().freeze();
                                freeze.insertWhiteLineWithKey(filledLine);
                                return simpleOriginalPieces.get(freeze);
                            })
                            .collect(Collectors.toList());

                    return AddLastsResult.create(prevResult, operations);
                });

        return stream.filter(Objects::nonNull);
    }

    private List<SpotResult> spot(PieceBlockCounts pieceBlockCounts) {
        return spotResultCache.computeIfAbsent(pieceBlockCounts, (key) -> (
                spotRunner.search(pieceBlockCounts.getPieceBlockCountList()))
        );
    }
}