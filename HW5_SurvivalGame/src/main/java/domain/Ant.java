package domain;

import domain.enums.Coord;

public class Ant extends Animal {
    public Ant(int row, int col, int multiplyCycles, boolean canMove) {
        super(row, col, multiplyCycles, canMove);
    }
    public Ant(int row, int col) {
        super(row, col);
    }

    @Override
    public void move(GameGrid gameGrid) {
        for (Coord coord : Coord.values()) {
            if (canMove(gameGrid, coord, EmptyAnimal.class)) {
                moveAnimalNearby(gameGrid, coord.getRowOffset(), coord.getColOffset(), 0);
                return;
            }
        }
    }

    @Override
    protected void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles) {
        Animal animal = new Ant(currentRow + rowOffset, currentCol + colOffset, multiplyCycles + 1, false);
        gameGrid.set(currentRow, currentCol, new EmptyAnimal(currentRow, currentCol));
        gameGrid.set(currentRow + rowOffset, currentCol + colOffset, animal);
    }
}
