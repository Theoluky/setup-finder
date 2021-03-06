package searcher.spins.wall;

import common.datastore.PieceCounter;
import common.parser.OperationTransform;
import core.field.Field;
import core.field.FieldFactory;
import core.field.FieldView;
import core.field.KeyOperators;
import core.mino.Mino;
import core.mino.MinoFactory;
import core.mino.MinoShifter;
import core.mino.Piece;
import core.neighbor.SimpleOriginalPiece;
import core.srs.Rotate;
import module.LongTest;
import org.junit.jupiter.api.Test;
import searcher.spins.SpinCommons;
import searcher.spins.candidates.CandidateWithMask;
import searcher.spins.candidates.SimpleCandidate;
import searcher.spins.pieces.MinimalSimpleOriginalPieces;
import searcher.spins.pieces.Scaffolds;
import searcher.spins.pieces.SimpleOriginalPieceFactory;
import searcher.spins.pieces.bits.BitBlocks;
import searcher.spins.results.AddLastsResult;
import searcher.spins.results.EmptyResult;
import searcher.spins.results.Result;
import searcher.spins.scaffold.ScaffoldRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class WallRunnerTest {
    @Test
    void case1() {
        int allowFillMaxHeight = 8;
        int fieldHeight = 10;
        Field initField = FieldFactory.createField(fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        EmptyResult emptyResult = new EmptyResult(initField, new PieceCounter(Piece.valueList()), fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 2, 1, fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.I, Rotate.Spawn, 6, 0, fieldHeight),
                to(Piece.L, Rotate.Right, 0, 1, fieldHeight),
                to(Piece.J, Rotate.Left, 4, 1, fieldHeight),
                to(Piece.S, Rotate.Right, 8, 1, fieldHeight),
                tOperation
        );

        Result result = AddLastsResult.create(emptyResult, operations);
        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(2);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case2() {
        // ???????????????T-Spin Mini???????????????????????????
        int allowFillMaxHeight = 8;
        int fieldHeight = 10;
        Field initField = FieldFactory.createField(fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.L, Piece.L, Piece.I, Piece.O, Piece.O, Piece.T
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Right, 0, 1, KeyOperators.getBitKey(1), fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.Z, Rotate.Spawn, 1, 0, fieldHeight),
                to(Piece.L, Rotate.Spawn, 7, 1, fieldHeight),
                to(Piece.L, Rotate.Spawn, 8, 0, fieldHeight),
                to(Piece.I, Rotate.Spawn, 3, 1, fieldHeight),
                to(Piece.O, Rotate.Spawn, 3, 0, KeyOperators.getBitKey(1), fieldHeight),
                to(Piece.O, Rotate.Spawn, 5, 0, KeyOperators.getBitKey(1), fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(1);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case3() {
        // ???????????????T-Spin Mini???????????????????????????
        int allowFillMaxHeight = 8;
        int fieldHeight = 10;
        Field initField = FieldFactory.createField(fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.L, Piece.L, Piece.I, Piece.Z, Piece.O, Piece.J, Piece.T
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Spawn, 4, 0, KeyOperators.getBitKey(1), fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.Z, Rotate.Spawn, 1, 0, fieldHeight),
                to(Piece.L, Rotate.Spawn, 7, 1, fieldHeight),
                to(Piece.L, Rotate.Spawn, 8, 0, fieldHeight),
                to(Piece.I, Rotate.Spawn, 3, 1, fieldHeight),
                to(Piece.Z, Rotate.Left, 1, 1, KeyOperators.getBitKey(1), fieldHeight),
                to(Piece.O, Rotate.Spawn, 2, 2, fieldHeight),
                to(Piece.J, Rotate.Right, 6, 1, KeyOperators.getBitKey(1), fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(1);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case4() {
        int allowFillMaxHeight = 8;
        int fieldHeight = 10;
        Field initField = FieldFactory.createField(fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.L, Piece.L, Piece.I, Piece.I, Piece.L, Piece.T, Piece.Z, Piece.S
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 3, 1, KeyOperators.getBitKey(1), fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.Z, Rotate.Spawn, 1, 0, fieldHeight),
                to(Piece.L, Rotate.Spawn, 7, 1, fieldHeight),
                to(Piece.L, Rotate.Spawn, 8, 0, fieldHeight),
                to(Piece.I, Rotate.Spawn, 3, 1, fieldHeight),
                to(Piece.I, Rotate.Left, 0, 1, KeyOperators.getBitKey(1), fieldHeight),
                to(Piece.L, Rotate.Spawn, 5, 0, KeyOperators.getBitKey(1), fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(4);

        verify(results, initField, fieldHeight);
    }

    @Test
    @LongTest
    void case5() {
        int allowFillMaxHeight = 7;
        int fieldHeight = 10;
        Field initField = FieldFactory.createField(fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.S, Piece.I, Piece.J, Piece.I, Piece.I, Piece.O, Piece.J, Piece.O, Piece.T,
                Piece.J, Piece.L, Piece.S, Piece.Z, Piece.I
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 4, 4, KeyOperators.getBitKey(4), fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.Z, Rotate.Spawn, 1, 3, fieldHeight),
                to(Piece.S, Rotate.Spawn, 8, 3, fieldHeight),
                to(Piece.I, Rotate.Spawn, 3, 4, fieldHeight),
                to(Piece.J, Rotate.Right, 6, 3, fieldHeight),
                to(Piece.I, Rotate.Left, 0, 1, fieldHeight),
                to(Piece.I, Rotate.Left, 9, 1, fieldHeight),
                to(Piece.O, Rotate.Spawn, 8, 5, fieldHeight),
                to(Piece.J, Rotate.Spawn, 1, 5, fieldHeight),
                to(Piece.O, Rotate.Right, 6, 6, fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(104);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case6() {
        int allowFillMaxHeight = 5;
        int fieldHeight = 10;
        Field initField = FieldFactory.createField(fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 2, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.J, Piece.S, Piece.T, Piece.Z
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 8, 4, fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.J, Rotate.Left, 9, 2, fieldHeight),
                to(Piece.S, Rotate.Right, 6, 3, fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(1);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case7() {
        int allowFillMaxHeight = 5;
        int fieldHeight = 10;
        Field initField = FieldFactory.createField("" +
                        "XXXX______" +
                        "XXXXXX____" +
                        "XXXXXXX___" +
                        "XXXXXXXX__" +
                        "XXXXXXXXX_" +
                        ""
                , fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.J, Piece.T, Piece.I
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 8, 4, fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.Z, Rotate.Right, 8, 2, fieldHeight),
                to(Piece.J, Rotate.Reverse, 5, 4, fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(1);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case8() {
        int allowFillMaxHeight = 5;
        int fieldHeight = 7;
        Field initField = FieldFactory.createField("" +
                        "XXXX______" +
                        "XXXXX_____" +
                        "XXXXXX____" +
                        "XXXXXXX___" +
                        "_XXXXXXX__" +
                        ""
                , fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.J, Piece.T, Piece.O
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 7, 2, fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.Z, Rotate.Spawn, 5, 3, fieldHeight),
                to(Piece.J, Rotate.Reverse, 8, 3, fieldHeight),
                to(Piece.O, Rotate.Spawn, 8, 0, fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(0);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case8_LeftL() {
        int allowFillMaxHeight = 5;
        int fieldHeight = 7;
        Field initField = FieldFactory.createField("" +
                        "XXXX______" +
                        "XXXXX_____" +
                        "XXXXXX____" +
                        "XXXXXXX___" +
                        "_XXXXXXX__" +
                        ""
                , fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.J, Piece.T, Piece.O, Piece.L
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 7, 2, fieldHeight);
        List<SimpleOriginalPiece> operations = Arrays.asList(
                to(Piece.Z, Rotate.Spawn, 5, 3, fieldHeight),
                to(Piece.J, Rotate.Reverse, 8, 3, fieldHeight),
                to(Piece.O, Rotate.Spawn, 8, 0, fieldHeight),
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(3);

        verify(results, initField, fieldHeight);
    }

    @Test
    void case9() {
        int allowFillMaxHeight = 5;
        int fieldHeight = 7;
        Field initField = FieldFactory.createField("" +
                        "________XX" +
                        "XXXX___XXX" +
                        "XXXXX_XXXX" +
                        "XXXX____XX" +
                        ""
                , fieldHeight);
        WallRunner runner = createWallRunner(initField, allowFillMaxHeight, allowFillMaxHeight + 1, fieldHeight);

        PieceCounter reminderPieceCounter = new PieceCounter(Arrays.asList(
                Piece.Z, Piece.L, Piece.T
        ));
        EmptyResult emptyResult = new EmptyResult(initField, reminderPieceCounter, fieldHeight);
        SimpleOriginalPiece tOperation = to(Piece.T, Rotate.Reverse, 5, 2, fieldHeight);
        List<SimpleOriginalPiece> operations = Collections.singletonList(
                tOperation
        );
        Result result = AddLastsResult.create(emptyResult, operations);

        List<CandidateWithMask> results = runner.search(new SimpleCandidate(result, tOperation)).collect(Collectors.toList());

        assertThat(results).hasSize(10);

        verify(results, initField, fieldHeight);
    }

    private void verify(List<CandidateWithMask> results, Field initField, int height) {
        assertThat(results)
                .allSatisfy(candidateWithMask -> {
                    SimpleOriginalPiece operationT = candidateWithMask.getOperationT();
                    Result result = candidateWithMask.getResult();
                    List<SimpleOriginalPiece> operationsWithoutT = result.operationStream()
                            .filter(op -> !op.equals(operationT))
                            .collect(Collectors.toList());

                    // T??????????????????????????????????????????????????????
                    Field fieldWithoutT = initField.freeze(height);
                    for (SimpleOriginalPiece originalPiece : operationsWithoutT) {
                        if (fieldWithoutT.canMerge(originalPiece.getMinoField())) {
                            fieldWithoutT.merge(originalPiece.getMinoField());
                        } else {
                            fail("cannot merge");
                        }
                    }

                    // T???????????????????????????????????????????????????
                    long needDeletedKey = operationT.getNeedDeletedKey();
                    assertThat(fieldWithoutT.getFilledLine() & needDeletedKey).isEqualTo(needDeletedKey);

                    // T?????????????????????????????????
                    fieldWithoutT.deleteLineWithKey(needDeletedKey);
                    assertThat(fieldWithoutT.canPut(operationT.getMino(), operationT.getX(), operationT.getY())).isTrue();

                    // T??????????????????
                    System.out.println(FieldView.toString(fieldWithoutT));
                    System.out.println();

                    assertThat(SpinCommons.canTSpin(fieldWithoutT, operationT.getX(), operationT.getY())).isTrue();
                });
    }

    private WallRunner createWallRunner(Field initField, int allowFillMaxHeight, int maxTargetHeight, int fieldHeight) {
        MinoFactory minoFactory = new MinoFactory();
        MinoShifter minoShifter = new MinoShifter();
        SimpleOriginalPieceFactory factory = new SimpleOriginalPieceFactory(minoFactory, minoShifter, maxTargetHeight);
        MinimalSimpleOriginalPieces minimalPieces = factory.createMinimalPieces(initField);
        BitBlocks bitBlocks = BitBlocks.create(minimalPieces);
        Scaffolds scaffolds = Scaffolds.create(minimalPieces);
        ScaffoldRunner scaffoldRunner = new ScaffoldRunner(scaffolds);
        return WallRunner.create(bitBlocks, scaffoldRunner, allowFillMaxHeight, fieldHeight);
    }

    private SimpleOriginalPiece to(Piece piece, Rotate rotate, int x, int y, int fieldHeight) {
        return to(piece, rotate, x, y, 0L, fieldHeight);
    }

    private SimpleOriginalPiece to(Piece piece, Rotate rotate, int x, int y, long deletedKey, int fieldHeight) {
        return new SimpleOriginalPiece(
                OperationTransform.toFullOperationWithKey(new Mino(piece, rotate), x, y, deletedKey, fieldHeight), fieldHeight
        );
    }
}