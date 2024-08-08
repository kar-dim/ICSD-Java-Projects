/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */

import ui.MainMenu;
import util.Session;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Constants.CONFIG_FILE;
import static util.Constants.SIGNATURE_FILE_NAME;
import static util.EncryptionUtils.generateKeys;
import static util.EncryptionUtils.signUserFiles;
import static util.FileUtils.areKeysPresent;

public class Main {
    public static void main(String[] args) throws Exception {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            Properties properties = new Properties();
            properties.load(fis);
            System.setProperty("jdk.crypto.KeyAgreement.legacyKDF", properties.getProperty("jdk.crypto.KeyAgreement.legacyKDF"));
        }
        if (!areKeysPresent())
            generateKeys();
        Session.setUpKeys();

        JFrame mainFrame = new JFrame("ISEC 3");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        mainFrame.add(new MainMenu(), BorderLayout.CENTER);
        mainFrame.pack();
        //όταν ο χρήστης πατήσει το "Χ" θέλει να κλείσει την εφαρμογή, άρα εδώ θα υπολογίσουμε τα hash των κρυπτογραφημένων αρχείων "expenses.dat" και "income.dat"
        //για τον logged_in χρήστη, στη συνέχεια θα υπογράψουμε ψηφιακά αυτά τα 2 αρχεία και θα αποθηκεύσουμε τη ψηφιακή υπογραφή στο αρχείο "signature.dat"
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    //αν δεν έχει γίνει login δεν υπογράφουμε κάτι
                    if (!Session.getLoggedInUser().isEmpty()) {
                        byte[] signedData = signUserFiles();
                        //αποθήκευση στο αρχείο
                        if (signedData != null) {
                            try (FileOutputStream fos = new FileOutputStream(Session.getLoggedInUser() + SIGNATURE_FILE_NAME)) {
                                fos.write(signedData);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
