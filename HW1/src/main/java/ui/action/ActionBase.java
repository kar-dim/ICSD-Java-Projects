package ui.action;

import util.Session;

import javax.swing.JTabbedPane;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public abstract class ActionBase {
    protected static final Logger LOGGER = Logger.getLogger(ActionBase.class.getName());
    protected final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    protected final JTabbedPane tabs;
    protected final PublicKey publicKey;
    protected final PrivateKey privateKey;

    public ActionBase(JTabbedPane tabs) {
        this.tabs = tabs;
        this.publicKey = Session.getPublicKey();
        this.privateKey = Session.getPrivateKey();
    }

    public abstract boolean doAction();
    protected void initKeys() {

    }
}
