package domain;

public class EmptyAnimal extends Animal {

    public EmptyAnimal(int row, int col) {
        super(row, col);
    }

    @Override
    public void move(GameGrid gameGrid) {
    }

    @Override
    protected void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles) {
    }

    @Override
    public void multiply(GameGrid gameGrid) {
    }
}
