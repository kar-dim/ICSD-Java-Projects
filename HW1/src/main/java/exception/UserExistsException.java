/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */
package exception;
//μια απλή exception που εγείρεται όταν στο register υπάρχει ήδη χρήστης με κάποιον ήδη registered χρήστη
public class UserExistsException extends Exception {
    public UserExistsException(){}
}
