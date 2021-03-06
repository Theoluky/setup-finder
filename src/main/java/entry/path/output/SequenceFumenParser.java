package entry.path.output;

import common.buildup.BuildUpStream;
import common.datastore.MinoOperationWithKey;
import common.datastore.Operation;
import common.datastore.Operations;
import common.parser.OperationTransform;
import common.tetfu.Tetfu;
import common.tetfu.TetfuElement;
import common.tetfu.common.ColorConverter;
import common.tetfu.common.ColorType;
import common.tetfu.field.ColoredField;
import common.tetfu.field.ColoredFieldFactory;
import concurrent.LockedReachableThreadLocal;
import core.action.reachable.DeepdropReachable;
import core.action.reachable.LockedReachable;
import core.field.Field;
import core.mino.MinoFactory;
import core.mino.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SequenceFumenParser implements FumenParser {
    private final MinoFactory minoFactory;
    private final ColorConverter colorConverter;
    private final LockedReachableThreadLocal reachableThreadLocal;

    public SequenceFumenParser(MinoFactory minoFactory, ColorConverter colorConverter) {
        this.minoFactory = minoFactory;
        this.colorConverter = colorConverter;
        this.reachableThreadLocal = new LockedReachableThreadLocal(24);
    }

    @Override
    public String parse(List<MinoOperationWithKey> operationsWithKey, Field field, int maxClearLine) {
        List<MinoOperationWithKey> validOperationsWithKey = get(operationsWithKey, field, maxClearLine);
        Operations operations = OperationTransform.parseToOperations(field, validOperationsWithKey, maxClearLine);
        return parse(operations, field, maxClearLine);
    }

    public List<MinoOperationWithKey> get(List<MinoOperationWithKey> operationsWithKey, Field field, int maxClearLine) {
        {
            LockedReachable reachable = reachableThreadLocal.get();
            BuildUpStream stream = new BuildUpStream(reachable, maxClearLine);
            Optional<List<MinoOperationWithKey>> first = stream.existsValidBuildPattern(field, operationsWithKey).findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        {
            DeepdropReachable reachable = new DeepdropReachable();
            BuildUpStream stream = new BuildUpStream(reachable, maxClearLine);
            Optional<List<MinoOperationWithKey>> first = stream.existsValidBuildPattern(field, operationsWithKey).findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        return operationsWithKey;
    }

    @Override
    public String parse(Operations operations, Field field, int maxClearLine) {
        List<? extends Operation> operationsList = operations.getOperations();

        // ????????????????????????
        List<Piece> pieceList = operationsList.stream()
                .map(Operation::getPiece)
                .collect(Collectors.toList());

        // ??????????????????
        String quiz = Tetfu.encodeForQuiz(pieceList);
        ArrayList<TetfuElement> tetfuElements = new ArrayList<>();

        // ?????????????????????????????????????????????
        Field freeze = field.freeze();
        freeze.clearLine();

        // ?????????element
        Operation firstKey = operationsList.get(0);
        ColorType colorType1 = colorConverter.parseToColorType(firstKey.getPiece());
        ColoredField coloredField = createInitColoredField(freeze, maxClearLine);
        TetfuElement firstElement = new TetfuElement(coloredField, colorType1, firstKey.getRotate(), firstKey.getX(), firstKey.getY(), quiz);
        tetfuElements.add(firstElement);

        // 2???????????????element
        if (1 < operationsList.size()) {
            operationsList.subList(1, operationsList.size()).stream()
                    .map(operation -> {
                        ColorType colorType = colorConverter.parseToColorType(operation.getPiece());
                        return new TetfuElement(colorType, operation.getRotate(), operation.getX(), operation.getY(), quiz);
                    })
                    .forEach(tetfuElements::add);
        }

        Tetfu tetfu = new Tetfu(minoFactory, colorConverter);
        return tetfu.encode(tetfuElements);
    }

    private ColoredField createInitColoredField(Field initField, int maxClearLine) {
        ColoredField coloredField = ColoredFieldFactory.createField(24);
        fillInField(coloredField, ColorType.Gray, initField, maxClearLine);
        return coloredField;
    }

    private void fillInField(ColoredField coloredField, ColorType colorType, Field target, int maxClearLine) {
        for (int y = 0; y < maxClearLine; y++)
            for (int x = 0; x < 10; x++)
                if (!target.isEmpty(x, y))
                    coloredField.setColorType(colorType, x, y);
    }
}
