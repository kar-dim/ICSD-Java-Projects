import java.util.Scanner;

import static domain.enums.AnimalType.ANT;
import static domain.enums.AnimalType.BEETLE;

public class Main {
    private static final int BEETLE_NUM = 10;
    private static final int ANT_NUM = 60;

    public static void main(String[] args) {
        SurvivalGame game = new SurvivalGame(BEETLE_NUM, ANT_NUM);
        Scanner scan = new Scanner(System.in);

        int rounds = 0;
        while (!game.isEnd()) {
            System.out.println("Round: " + (rounds + 1));
            game.display();
            //αναπαραγωγή μυρμιγκιών και έλεγχος για το αν πρέπει να πεθάνουν σκαθάρια
            if (rounds % 3 == 0) {
                game.killStarvingBeetles();
                game.multiplyAnimals(ANT, 3);
            }
            //αναπαραγωγή σκαθαριών
            if (rounds % 7 == 0)
                game.multiplyAnimals(BEETLE, 7);

            scan.nextLine();
            game.moveTime();
            rounds++;
        }
    }
}
