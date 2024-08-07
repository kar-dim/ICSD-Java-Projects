package util;

import domain.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
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

    public void writeObject(Object obj) throws IOException {
        oos.writeObject(obj);
        oos.flush();
    }

    public void writeUTF(String message) throws IOException {
        oos.writeUTF(message);
        oos.flush();
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        return ois.readObject();
    }

    public String readUTF() throws IOException {
        return ois.readUTF();
    }

    public void initializeConnection() throws IOException {
        ServerSocket server = new ServerSocket(1312);
        sock = server.accept();
        ois = new ObjectInputStream(sock.getInputStream());
        oos = new ObjectOutputStream(sock.getOutputStream());
    }

    public void initializeConnectionAsClient() throws IOException {
        sock = new Socket("127.0.0.1",1312);
        oos = new ObjectOutputStream(sock.getOutputStream());
        ois = new ObjectInputStream(sock.getInputStream());
    }

    public void closeConnection() {
        if (ois != null && oos != null && sock != null && !sock.isClosed()) {
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
