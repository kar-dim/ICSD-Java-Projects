package domain;

import domain.enums.AnimalType;

import java.util.HashMap;
import java.util.Map;

public class GameGrid {
    private final Map<Integer, Animal> grid;
    private final int maxGridSize;

    public GameGrid(int maxGridSize) {
        grid = new HashMap<>();
        this.maxGridSize = maxGridSize;
        for (int i = 0; i < maxGridSize; i++)
            for (int j = 0; j < maxGridSize; j++)
                grid.put(linearId(i,j), new EmptyAnimal(i, j));
    }
    private int linearId(int row, int col) {
        return (row * maxGridSize) + col;
    }

    public int getGridSize() {
        return maxGridSize;
    }

    public Animal get(int row, int col) {
        return grid.get(linearId(row,col));
    }

    public void set(int row, int col, Animal animal) {
        grid.put(linearId(row,col), animal);
    }

    public void enableMovement() {
        grid.forEach((id, animal) -> animal.setCanMoveThisRound(true));
    }

    public int getAnimalCount(AnimalType animalType) {
        return (int) grid.values().stream()
                .filter(animal -> animal.getType() == animalType)
                .count();
    }

    public void killStarvingBeetles() {
        grid.entrySet().stream()
                .filter(entry -> entry.getValue().getType() == AnimalType.BEETLE)  // Filter to only beetles
                .filter(entry -> ((Beetle) entry.getValue()).getNoFoodCycles() >= 3)  // Check if they are starving
                .forEach(entry -> grid.put(entry.getKey(), new EmptyAnimal(entry.getKey() / maxGridSize,entry.getKey() % maxGridSize)));  // Replace with EmptyAnimal
    }

    public void multiplyAnimals(AnimalType type, int multiplyCyclesRequired) {
        grid.entrySet().stream()
                .filter(entry -> entry.getValue().getType() == type && entry.getValue().getMultiplyCycles() == multiplyCyclesRequired)
                .forEach(entry -> entry.getValue().multiply(this, entry.getKey() / maxGridSize,entry.getKey() % maxGridSize));
    }

    public void moveAnimals(AnimalType type) {
        grid.entrySet().stream()
                .filter(entry -> entry.getValue().getType() == type && entry.getValue().canMoveThisRound())
                .forEach(entry -> entry.getValue().move(this));
    }

    public String display() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < maxGridSize; i++) {
            builder.append("|");
            for (int j = 0; j < maxGridSize; j++) {
                switch(grid.get(linearId(i, j)).getType()) {
                    case EMPTY ->  builder.append(" |");
                    case BEETLE -> builder.append("X|");
                    case ANT -> builder.append("O|");
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
