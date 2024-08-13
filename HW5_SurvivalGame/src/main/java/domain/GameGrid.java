package domain;

import java.util.ArrayList;
import java.util.List;

public class GameGrid {
    private final List<List<Animal>> grid;

    public GameGrid(int maxGridSize) {
        grid = new ArrayList<>();
        for (int i = 0; i < maxGridSize; i++) {
            grid.add(new ArrayList<>());
            for (int j = 0; j < maxGridSize; j++) {
                grid.get(i).add(new EmptyAnimal(i, j));
            }
        }
    }

    public int getGridSize() {
        return grid.size();
    }

    public Animal get(int row, int col) {
        return grid.get(row).get(col);
    }

    public void set(int row, int col, Animal animal) {
        grid.get(row).set(col, animal);
    }

    public void enableMovement() {
        grid.stream()
                .flatMap(List::stream)
                .forEach(animal -> animal.setCanMoveThisRound(true));
    }

    public <T extends Animal> int getAnimalCount(Class<T> animalType) {
        return (int) grid.stream()
                .flatMap(List::stream)
                .filter(animalType::isInstance)
                .count();
    }

    public void killStarvingBeetles() {
        for (int i = 0; i < getGridSize(); i++)
            for (int j = 0; j < getGridSize(); j++)
                if (grid.get(i).get(j) instanceof Beetle)
                    if (((Beetle) grid.get(i).get(j)).getNoFoodCycles() >= 3)
                        grid.get(i).set(j, new EmptyAnimal(i, j));
    }
}
