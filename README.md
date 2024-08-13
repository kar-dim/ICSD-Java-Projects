# ICSD-Java-Projects
Κάποιες από τις εργασίες (σε Java) που εκπονήθηκαν κατα τη διάρκεια της φοίτησης (2013-2019) στο Τμήμα Μηχανικών Πληροφοριακών και Επικοινωνιακών Συστημάτων 

(Οι εργασίες έχουν γίνει refactored, ώστε να είναι πιο clean ο κώδικας, και με χρήση ποιοτικών code patterns).

- HW1: Τοπική εφαρμογή διαχείρισης εισόδων/εξόδων. Χρησιμοποίηση κρυπτογραφίας (συμμετρικής και ασύμμετρης), σύνοψης, salting καθώς και ψηφιακής υπογραφής. Τα ιδιωτικά κλειδιά κανονικά πρέπει να βρίσκονται σε ασφαλές σημείο (πράγμα που ΔΕΝ ισχύει στη συγκεκριμένη τοπική εφαρμογή) αλλά για λόγους απλότητας της εργασίας βρίσκονται στον ίδιο φάκελο με τα source files

- HW2: Κατανεμημένη εφαρμογή για ασφαλή μεταφορά κρυπτογραφικών κλειδιών (Key Exchange). MultiKAP ονομάζεται η εφαρμογή, "Client" είναι το μέλος που ξεκινάει πρώτος το πρωτόκολλο επικοινωνίας και "Server" το μέλος που περιμένει. Επιλέγεται μια μέθοδος Key Exchange (Diffie Hellman, RSA, Station-to-Station) και τα δύο μέλη εφαρμόζουν το αντίστοιχο πρωτόκολλο. Για να αποφευχθούν επιθέσεις Man in the Middle, χρησιμοποιήθηκαν certificates (self-signed για λόγους απλότητας).

- HW3: (Παρέχεται PDF αναφορά) Κατανεμημένη για κράτηση αεροπορικών εισητηρίων. Χρήση Java RMI.

- HW4: (Παρέχεται PDF αναφορά) Κατανεμημένη εφαρμογή για διαχείριση ανακοινώσεων.

- HW5: Τοπική εφαρμογή για την  δημιουργία μιας απλής προσομοίωσης δύο διαστάσεων (2D) της σχέσης μεταξύ θηρευτή και θηράματος.
