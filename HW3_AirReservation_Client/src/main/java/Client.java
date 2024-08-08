/* Dimitris Karatzas icsd13072
   Apostolos Lazaros icsd13096
*/

//κλάση για τον client, απλώς δημιουργεί το GUI

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class Client extends JFrame{
    public static void main(String[] args) throws Exception {
        //δημιουργία του frame
        JFrame frame = new JFrame("Reservations");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        MainMenu menu = new MainMenu();
        frame.add(menu, BorderLayout.CENTER);
        frame.pack();
    }
    
}
