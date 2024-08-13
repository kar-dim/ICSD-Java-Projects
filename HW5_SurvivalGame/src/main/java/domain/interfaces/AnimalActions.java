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

    default boolean canMove(GameGrid gameGrid, int currentRow, int currentCol, Coord pos, AnimalType allowedNeighbor) {
        if (pos == Coord.ABOVE && currentRow == 0) {
            return false;
        } else if (pos == Coord.BELOW && currentRow == gameGrid.getGridSize() - 1) {
            return false;
        } else if (pos == Coord.LEFT && currentCol == 0) {
            return false;
        } else if (pos == Coord.RIGHT && currentCol == gameGrid.getGridSize() - 1){
            return false;
        }
        return gameGrid.get(currentRow + pos.getRowOffset(), currentCol + pos.getColOffset()).getType() == allowedNeighbor;
    }

    void move(GameGrid gameGrid);
    void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles);
}
