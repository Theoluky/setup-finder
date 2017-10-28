package core.mino.piece;

import common.ActionParser;
import core.field.Field;
import core.field.FieldFactory;
import core.mino.Block;
import core.mino.Mino;
import core.srs.Rotate;

// TODO: write unittest
public class OriginalPiece {
    static final OriginalPiece EMPTY_COLLIDER_PIECE = new OriginalPiece();

    private final Field minoField;
    private final Field harddropCollider;
    private final int hash;
    private final Piece piece;

    public OriginalPiece(Mino mino, int x, int y, int fieldHeight) {
        this.piece = new Piece(mino, x, y, 0L);
        this.minoField = createMinoField();
        this.harddropCollider = createHarddropCollider(mino, x, y, fieldHeight);
        this.hash = ActionParser.parseToInt(mino.getBlock(), mino.getRotate(), x, y);
    }

    private OriginalPiece() {
        this.piece = new Piece(new Mino(Block.I, Rotate.Spawn), -1, -1, 0L);
        this.minoField = FieldFactory.createField(1);
        this.harddropCollider = FieldFactory.createField(1);
        this.hash = -1;
    }

    private Field createMinoField() {
        Mino mino = piece.getMino();
        int x = piece.getX();
        int y = piece.getY();
        Field field = FieldFactory.createField(y + mino.getMaxY() + 1);
        field.put(mino, x, y);
        return field;
    }

    private Field createHarddropCollider(Mino mino, int x, int y, int fieldHeight) {
        Field field = FieldFactory.createField(fieldHeight);
        for (int yIndex = y; yIndex < fieldHeight - mino.getMinY(); yIndex++)
            field.put(mino, x, yIndex);
        for (int yIndex = fieldHeight; yIndex < field.getMaxFieldHeight(); yIndex++)
            for (int xIndex = 0; xIndex < 10; xIndex++)
                field.removeBlock(xIndex, yIndex);
        return field;
    }

    public Field getMinoField() {
        return minoField;
    }

    @Override
    public String toString() {
        return "Piece{" +
                piece.toString() +
                '}';
    }

    public Mino getMino() {
        return piece.getMino();
    }

    public int getX() {
        return piece.getX();
    }

    public int getY() {
        return piece.getY();
    }

    public Field getHarddropCollider() {
        return harddropCollider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OriginalPiece piece = (OriginalPiece) o;
        return this.getX() == piece.getX() & this.getY() == piece.getY() & this.getMino().equals(piece.getMino());
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
