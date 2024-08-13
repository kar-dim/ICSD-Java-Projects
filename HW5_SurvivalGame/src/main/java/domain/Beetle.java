package domain;

import domain.enums.AnimalType;
import domain.enums.Coord;
import domain.interfaces.AnimalActions;

import static domain.enums.AnimalType.*;

public class Beetle extends Animal implements AnimalActions {
    private final int noFoodCycles;

    public Beetle(int row, int col, int multiplyCycles, int noFoodCycles, boolean canMove) {
        super(row, col, multiplyCycles, canMove);
        this.noFoodCycles = noFoodCycles;
    }

    public Beetle(int row, int col) {
        super(row, col);
        noFoodCycles = 0;
    }

    public int getNoFoodCycles() {
        return noFoodCycles;
    }

    @Override
    public void move(GameGrid gameGrid) {
        for (Coord coord : Coord.values()) {
            //πρώτα ψάχνει για ants
            if (canMove(gameGrid, currentRow, currentCol, coord, ANT)) {
                moveAnimalNearby(gameGrid, coord.getRowOffset(), coord.getColOffset(), 0);
                return;
            }
            //αν δεν βρει ant, ψαχνει κενη θεση
            if (canMove(gameGrid, currentRow, currentCol, coord, EMPTY)) {
                moveAnimalNearby(gameGrid, coord.getRowOffset(), coord.getColOffset(), noFoodCycles + 1);
                return;
            }
        }
    }

    @Override
    public void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles) {
        Animal animal = new Beetle(currentRow + rowOffset, currentCol + colOffset, multiplyCycles + 1, noFoodCycles, false);
        gameGrid.set(currentRow, currentCol, new EmptyAnimal(currentRow, currentCol));
        gameGrid.set(currentRow + rowOffset, currentCol + colOffset, animal);
    }

    @Override
    public AnimalType getType() {
        return BEETLE;
    }
}
