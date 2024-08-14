import domain.*;
import domain.enums.AnimalType;

import java.util.Random;

import static domain.enums.AnimalType.*;

public class SurvivalGame {
    private final GameGrid gameGrid;
    private final int MAX_GRID_SIZE = 20;

    public SurvivalGame(int beetleNum, int antsNum) {
        gameGrid = new GameGrid(MAX_GRID_SIZE);
        fillAnimals(BEETLE, beetleNum);
        fillAnimals(ANT, antsNum);
    }

    private  void fillAnimals(AnimalType type, int animalNum) {
        Random random = new Random();
        int row, col;
        for (int i = 0; i < animalNum; i++) {
            row = random.nextInt(MAX_GRID_SIZE);
            col = random.nextInt(MAX_GRID_SIZE);
            Animal animal = type == ANT ? new Ant(row,col) : new Beetle(row,col);
            if (gameGrid.get(row, col).getType() == EMPTY) {
                gameGrid.set(row, col, animal);
            } else
                i--;
        }
    }

    public boolean end() {
        int beetleCounter = gameGrid.getAnimalCount(BEETLE);
        int antCounter = gameGrid.getAnimalCount(ANT);
        if (beetleCounter == 0) {
            System.out.println("Ants have conquered the game!");
            return true;
        } else if (antCounter == 0) {
            System.out.println("Beetles have conquered the game!");
            return true;
        }
        System.out.println("Remaining Ants: " + antCounter + "\nRemaining Beetles: " + beetleCounter);
        return false;
    }

    public void moveTime() {
        //πρώτα τα σκαθάρια
        moveAnimals(BEETLE);
        moveAnimals(ANT);
        enableMovementAgain();
    }

    private void moveAnimals(AnimalType type) {
        gameGrid.moveAnimals(type);
    }
    public void multiplyAnimals(AnimalType type, int multiplyCyclesRequired) {
        gameGrid.multiplyAnimals(type, multiplyCyclesRequired);
    }


    private void enableMovementAgain() {
        gameGrid.enableMovement();
    }

    //έλεγχος για τα σκαθάρια (αν πρέπει να πεθάνουν, θα πρέπει να καλείται σε κάθε 3 κύκλους)
    public void killStarvingBeetles() {
        gameGrid.killStarvingBeetles();
    }

    public void display() {
        System.out.print(gameGrid.display());
    }
}
