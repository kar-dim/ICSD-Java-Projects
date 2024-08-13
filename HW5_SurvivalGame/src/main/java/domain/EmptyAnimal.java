package domain;

import domain.enums.AnimalType;
import domain.interfaces.AnimalActions;

import static domain.enums.AnimalType.EMPTY;

public class EmptyAnimal extends Animal implements AnimalActions {

    public EmptyAnimal(int row, int col) {
        super(row, col);
    }

    @Override
    public void move(GameGrid gameGrid) {

    }

    @Override
    public void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles) {

    }

    @Override
    public AnimalType getType() {
        return EMPTY;
    }

}
