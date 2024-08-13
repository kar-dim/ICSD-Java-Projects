package domain;

import domain.enums.Coord;

public class Beetle extends Animal {
    private final int noFoodCycles;
    public Beetle(int row, int col, int multiplyCycles,int noFoodCycles, boolean canMove) {
        super(row, col, multiplyCycles, canMove);
        this.noFoodCycles = noFoodCycles;
    }

    public Beetle(int row, int col) {
        super(row, col);
        noFoodCycles = 0;
    }

    @Override
    protected void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles) {
        Animal animal = new Beetle(currentRow + rowOffset, currentCol + colOffset, multiplyCycles + 1, noFoodCycles, false);
        gameGrid.set(currentRow, currentCol, new EmptyAnimal(currentRow, currentCol));
        gameGrid.set(currentRow + rowOffset, currentCol + colOffset, animal);
    }

    public int getNoFoodCycles() {
        return noFoodCycles;
    }

    @Override
    public void move(GameGrid gameGrid) {
        for (Coord coord : Coord.values()) {
            //πρώτα ψάχνει για ants
            if (canMove(gameGrid, coord, Ant.class)) {
                moveAnimalNearby(gameGrid, coord.getRowOffset(), coord.getColOffset(), 0);
                return;
            }
            //αν δεν βρει ant, ψαχνει κενη θεση
            if (canMove(gameGrid, coord, EmptyAnimal.class)) {
                moveAnimalNearby(gameGrid, coord.getRowOffset(), coord.getColOffset(), noFoodCycles + 1);
                return;
            }
        }
    }
}
