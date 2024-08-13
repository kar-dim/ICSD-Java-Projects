package domain;
import domain.enums.Coord;

public abstract class Animal {
    protected int currentRow, currentCol;
    protected int multiplyCycles;
    protected boolean canMove;

    protected Animal(int row, int col) {
        currentRow = row;
        currentCol = col;
        multiplyCycles = 0;
        canMove = true;
    }

    protected Animal(int row, int col, int noOfMultiplyCycles, boolean moveAllowed) {
        currentRow = row;
        currentCol = col;
        multiplyCycles = noOfMultiplyCycles;
        canMove = moveAllowed;
    }

    public boolean canMoveThisRound() {
        return canMove;
    }

    protected void setZeroMultiplyCycles() {
        multiplyCycles = 0;
    }

    public int getMultiplyCycles() {
        return multiplyCycles;
    }

    public void setCanMoveThisRound(boolean canMove) {
        this.canMove = canMove;
    }

    public <T extends Animal> boolean canMove(GameGrid gameGrid, Coord pos, Class<T> allowedNeighbor) {
        int newRow = currentRow + pos.getRowOffset();
        int newCol = currentCol + pos.getColOffset();
        if (pos == Coord.ABOVE && currentRow == 0) {
            return false;
        } else if (pos == Coord.BELOW && currentRow == gameGrid.getGridSize() - 1) {
            return false;
        } else if (pos == Coord.LEFT && currentCol == 0) {
            return false;
        } else if (pos == Coord.RIGHT && currentCol == gameGrid.getGridSize() - 1){
            return false;
        }
        return allowedNeighbor.isInstance(gameGrid.get(newRow, newCol));
    }
    public void multiply(GameGrid gameGrid) {
        for (Coord coord : Coord.values()) {
            if (canMove(gameGrid, coord, EmptyAnimal.class)) {
                multiplyAnimalNearby(gameGrid, coord.getRowOffset(), coord.getColOffset());
                break; //1 απόγονος μονο τη φορα επιτρεπεται
            }
        }
    }

    protected void multiplyAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset) {
        Animal animal = gameGrid.get(currentRow,currentCol) instanceof Ant ?
                new Ant(currentRow + rowOffset, currentCol + colOffset) :
                new Beetle(currentRow + rowOffset, currentCol + colOffset);
        gameGrid.get(currentRow, currentCol).setZeroMultiplyCycles();
        gameGrid.set(currentRow + rowOffset, currentCol + colOffset, animal);
    }

    public abstract void move(GameGrid gameGrid);
    protected abstract void moveAnimalNearby(GameGrid gameGrid, int rowOffset, int colOffset, int noFoodCycles);
}
