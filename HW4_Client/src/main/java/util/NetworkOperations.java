package util;

import domain.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkOperations {

    private static final Logger LOGGER = Logger.getLogger(NetworkOperations.class.getName());
    private static Socket sock;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;

    public static void writeMessage(Message message) throws IOException {
        oos.writeObject(message);
        oos.flush();
    }

    public static Message readMessage() throws IOException, ClassNotFoundException {
        return (Message) ois.readObject();
    }

    public static void initializeConnection() throws IOException, ClassNotFoundException {
        if (sock == null && oos == null && ois == null) {
            sock = new Socket("localhost", 5555);
            oos = new ObjectOutputStream(sock.getOutputStream());
            ois = new ObjectInputStream(sock.getInputStream());
        }
        writeMessage(new Message(MessageType.START));
        ois.readObject();
    }

    public static void closeConnection() {
        if (ois != null && oos != null && sock != null) {
            try {
                ois.close();
                oos.close();
                sock.close();
                ois = null;
                oos = null;
                sock = null;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}
