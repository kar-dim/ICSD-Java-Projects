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
    private Socket sock;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public Socket getSock() {
        return sock;
    }

    public void setSock(Socket sock) {
        this.sock = sock;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public void writeMessage(Message message) throws IOException {
        oos.writeObject(message);
        oos.flush();
    }

    public void initializeConnection() throws IOException, ClassNotFoundException {
        sock = new Socket("localhost", 5555);
        oos = new ObjectOutputStream(sock.getOutputStream());
        ois = new ObjectInputStream(sock.getInputStream());
        writeMessage(new Message(MessageType.START));
        ois.readObject();
    }

    public void closeConnection() {
        if (ois != null && oos != null && sock != null) {
            try {
                ois.close();
                oos.close();
                sock.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}
