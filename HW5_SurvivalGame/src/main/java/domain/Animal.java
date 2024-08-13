package domain;

import domain.enums.AnimalType;
import domain.interfaces.AnimalActions;

public abstract class Animal implements AnimalActions {
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

    public void setZeroMultiplyCycles() {
        multiplyCycles = 0;
    }

    public int getMultiplyCycles() {
        return multiplyCycles;
    }

    public void setCanMoveThisRound(boolean canMove) {
        this.canMove = canMove;
    }

    public abstract AnimalType getType();
}
