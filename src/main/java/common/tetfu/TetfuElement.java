package common.tetfu;

import common.tetfu.common.ColorType;
import core.srs.Rotate;

public class TetfuElement {
    public static TetfuElement EMPTY = new TetfuElement(ColorType.Empty, Rotate.Reverse, 0, 0);

    private final String comment;
    private final ColorType colorType;
    private final Rotate rotate;
    private final int x;
    private final int y;

    public TetfuElement(ColorType colorType, Rotate rotate, int x, int y) {
        this(colorType, rotate, x, y, "");
    }

    public TetfuElement(ColorType colorType, Rotate rotate, int x, int y, String comment) {
        this.colorType = colorType;
        this.rotate = rotate;
        this.x = x;
        this.y = y;
        this.comment = comment;
    }

    String getEscapedComment() {
        return TetfuTable.escape(comment);
    }

    public ColorType getColorType() {
        return colorType;
    }

    public Rotate getRotate() {
        return rotate;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getComment() {
        return comment;
    }
}
