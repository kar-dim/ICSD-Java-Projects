package domain;

import domain.enums.AnimalType;
import domain.enums.Coord;
import domain.interfaces.AnimalActions;

import static domain.enums.AnimalType.ANT;
import static domain.enums.AnimalType.EMPTY;

public class Ant extends Animal implements AnimalActions {
    public Ant(int row, int col, int multiplyCycles, boolean canMove) {
        super(row, col, multiplyCycles, canMove);
    }

    public Ant(int row, int col) {
        super(row, col);
    }

    @Override
    public void move(GameGrid gameGrid) {
        for (Coord coord : Coord.values()) {
            if (canMove(gameGrid, currentRow, currentCol, coord, EMPTY)) {
                moveAnimalNearby(gameGrid, coord.getRowOffset(), coord.getColOffset(), 0);
                return;
            }
        }
    }

    @Override
    public void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles) {
        Animal animal = new Ant(currentRow + rowOffset, currentCol + colOffset, multiplyCycles + 1, false);
        gameGrid.set(currentRow, currentCol, new EmptyAnimal(currentRow, currentCol));
        gameGrid.set(currentRow + rowOffset, currentCol + colOffset, animal);
    }

    @Override
    public AnimalType getType() {
        return ANT;
    }

}
