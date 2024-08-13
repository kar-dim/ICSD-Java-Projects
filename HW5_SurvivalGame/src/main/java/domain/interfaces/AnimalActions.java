package domain.interfaces;

import domain.*;
import domain.enums.AnimalType;
import domain.enums.Coord;

import static domain.enums.AnimalType.EMPTY;

public interface AnimalActions {
    default void multiply(GameGrid gameGrid, int currentRow, int currentCol) {
        for (Coord coord : Coord.values()) {
            if (canMove(gameGrid, currentRow, currentCol, coord, EMPTY)) {
                multiplyAnimalNearby(gameGrid, currentRow, currentCol, coord);
                break; //1 απόγονος μονο τη φορα επιτρεπεται
            }
        }
    }

    default void multiplyAnimalNearby(GameGrid gameGrid, int currentRow, int currentCol, Coord pos) {
        int rowOffset = pos.getRowOffset();
        int colOffset = pos.getColOffset();
        Animal animal = gameGrid.get(currentRow,currentCol) instanceof Ant ?
                new Ant(currentRow + rowOffset, currentCol + colOffset) :
                new Beetle(currentRow + rowOffset, currentCol + colOffset);
        gameGrid.get(currentRow, currentCol).setZeroMultiplyCycles();
        gameGrid.set(currentRow + rowOffset, currentCol + colOffset, animal);
    }

    private boolean outOfBoundsMove(int currentRow, int currentCol, int gridSize, Coord pos) {
        return (pos == Coord.ABOVE && currentRow == 0) ||
                (pos == Coord.BELOW && currentRow == gridSize - 1) ||
                (pos == Coord.LEFT && currentCol == 0) ||
                (pos == Coord.RIGHT && currentCol == gridSize - 1);
    }

    default boolean canMove(GameGrid gameGrid, int currentRow, int currentCol, Coord pos, AnimalType allowedNeighbor) {
        if (outOfBoundsMove(currentRow, currentCol, gameGrid.getGridSize(), pos))
            return false;
        return gameGrid.get(currentRow + pos.getRowOffset(), currentCol + pos.getColOffset()).getType() == allowedNeighbor;
    }

    void move(GameGrid gameGrid);
    void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles);
}
