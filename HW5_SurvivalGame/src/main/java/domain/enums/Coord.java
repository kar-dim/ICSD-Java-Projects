package domain.enums;

public enum Coord {
    ABOVE(-1, 0),
    BELOW(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    private final int rowOffset;
    private final int colOffset;

    Coord(int rowOffset, int colOffset) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public int getColOffset() {
        return colOffset;
    }
}
