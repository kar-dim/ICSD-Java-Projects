import domain.*;

import java.util.Random;

public class SurvivalGame {
    private final GameGrid gameGrid;
    private final int MAX_GRID_SIZE = 20;

    public SurvivalGame(int beetleNum, int antsNum) {
        gameGrid = new GameGrid(MAX_GRID_SIZE);
        fillAnimals(Beetle.class, beetleNum);
        fillAnimals(Ant.class, antsNum);
    }

    private  <T extends Animal> void fillAnimals(Class<T> type, int animalNum) {
        Random random = new Random();
        int row, col;
        for (int i = 0; i < animalNum; i++) {
            row = random.nextInt(MAX_GRID_SIZE);
            col = random.nextInt(MAX_GRID_SIZE);
            Animal animal = type.equals(Ant.class) ? new Ant(row,col) : new Beetle(row,col);
            if (gameGrid.get(row, col) instanceof EmptyAnimal) {
                gameGrid.set(row, col, animal);
            } else
                i--;
        }
    }

    public boolean end() {
        int beetleCounter = gameGrid.getAnimalCount(Beetle.class);
        int antCounter = gameGrid.getAnimalCount(Ant.class);
        if (beetleCounter == 0)
            System.out.println("Ants have conquered the game!");
        else if (antCounter == 0)
            System.out.println("Beetles have conquered the game!");
        else
            return false;
        return true;
    }

    public void moveTime() {
        //πρώτα τα σκαθάρια
        moveAnimals(Beetle.class);
        moveAnimals(Ant.class);
        enableMovementAgain();
    }

    private <T extends Animal> void moveAnimals(Class<T> type) {
        for (int i = 0; i < MAX_GRID_SIZE; i++)
            for (int j = 0; j < MAX_GRID_SIZE; j++)
                if (type.isInstance(gameGrid.get(i, j)) && gameGrid.get(i, j).canMoveThisRound())
                    gameGrid.get(i, j).move(gameGrid);
    }
    public <T extends Animal> void multiplyAnimals(Class<T> type, int multiplyCyclesRequired) {
        for (int i = 0; i < MAX_GRID_SIZE; i++) {
            for (int j = 0; j < MAX_GRID_SIZE; j++) {
                if (type.isInstance(gameGrid.get(i, j)) && gameGrid.get(i, j).getMultiplyCycles() == multiplyCyclesRequired)
                    gameGrid.get(i, j).multiply(gameGrid);
            }
        }
    }


    private void enableMovementAgain() {
        gameGrid.enableMovement();
    }

    //έλεγχος για τα σκαθάρια (αν πρέπει να πεθάνουν, θα πρέπει να καλείται σε κάθε 3 κύκλους)
    public void killStarvingBeetles() {
        gameGrid.killStarvingBeetles();
    }

    public void display() {
        for (int i = 0; i < MAX_GRID_SIZE; i++) {
            for (int j = 0; j < MAX_GRID_SIZE; j++) {
                if (j == 0)
                    System.out.print("|");
                if (gameGrid.get(i, j) instanceof EmptyAnimal)
                    System.out.print(" |");
                else if (gameGrid.get(i, j) instanceof Beetle)
                    System.out.print("X|");
                else if (gameGrid.get(i, j) instanceof Ant)
                    System.out.print("O|");
            }
            System.out.println();
        }
    }
}
