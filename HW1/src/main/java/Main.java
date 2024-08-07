/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */
package sec3;

import sec3.util.EncryptionUtils;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("ISEC 3");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        MainMenu menu = new MainMenu();
        frame.add(menu, BorderLayout.CENTER);
        frame.pack();
        //όταν ο χρήστης πατήσει το "Χ" θέλει να κλείσει την εφαρμογή, άρα εδώ θα υπολογίσουμε τα hash των κρυπτογραφημένων αρχείων "expenses.dat" και "income.dat"
        //για τον logged_in χρήστη, στη συνέχεια θα υπογράψουμε ψηφιακά αυτά τα 2 αρχεία και θα αποθηκεύσουμε τη ψηφιακή υπογραφή στο αρχείο "signature.dat"
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    String userFolder = MainMenu.getLoggedInUsernamePath(); //π.χ "./dim_kar/"
                    //αν δεν έχει γίνει login δεν υπογράφουμε κάτι
                    if (userFolder != null) {
                        Signature sign = Signature.getInstance("SHA256withRSA"); //αντικείμενο υπογραφής
                        //πρέπει να υπογράψουμε τώρα, δεν χρησιμοποιούμε τον SHA-256 και μετά κρυπτογράφηση με RSA, αυτό γίνεται αυτόματα στην υπογραφή, το δηλώσαμε στο getInstance("SHA256withRSA");
                        sign.initSign((PrivateKey) EncryptionUtils.getKey("private.key"));
                        boolean expenseFileExists = new File(userFolder + "expenses.dat").exists();
                        boolean incomeFileExists = new File(userFolder + "income.dat").exists();
                        if (expenseFileExists && incomeFileExists) {
                            sign.update(Files.readAllBytes(new File(userFolder + "expenses.dat").toPath()));
                            sign.update(Files.readAllBytes(new File(userFolder + "income.dat").toPath()));
                        } else if (expenseFileExists) {
                            sign.update(Files.readAllBytes(new File(userFolder + "expenses.dat").toPath()));
                        } else if (incomeFileExists) {
                            sign.update(Files.readAllBytes(new File(userFolder + "income.dat").toPath()));
                        }
                        else {
                            return;
                        }
                        //αποθήκευση στο αρχείο
                        FileOutputStream fos = new FileOutputStream(userFolder + "signature.dat");
                        fos.write(sign.sign());
                        fos.flush();
                        fos.close();
                        //από αυτό το αρχείο η εφαρμογή μετά το Login θα πάρει τη ψηφιακή υπογραφή (χρησιμοποιόντας το privatekey της)
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

}
