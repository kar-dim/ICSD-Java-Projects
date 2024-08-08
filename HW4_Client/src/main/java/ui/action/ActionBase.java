package ui.action;

import javax.swing.JTabbedPane;
import java.util.logging.Logger;

public abstract class ActionBase {
    protected final Logger LOGGER = Logger.getLogger(ActionBase.class.getName());
    protected final JTabbedPane tabs;

    public ActionBase(JTabbedPane tabs) {
        this.tabs = tabs;
    }

    public abstract void doAction();
}
