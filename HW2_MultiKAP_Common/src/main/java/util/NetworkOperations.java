package util;

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
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public Socket getSock() {
        return sock;
    }

    public void writeObject(Object obj) throws IOException {
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
    }

    public void writeUTF(String message) throws IOException {
        objectOutputStream.writeUTF(message);
        objectOutputStream.flush();
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        return objectInputStream.readObject();
    }

    public String readUTF() throws IOException {
        return objectInputStream.readUTF();
    }

    public void initializeConnection() throws IOException {
        ServerSocket server = new ServerSocket(1312);
        sock = server.accept();
        objectInputStream = new ObjectInputStream(sock.getInputStream());
        objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
    }

    public void initializeConnectionAsClient() throws IOException {
        sock = new Socket("127.0.0.1",1312);
        objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
        objectInputStream = new ObjectInputStream(sock.getInputStream());
    }

    public void closeConnection() {
        if (objectInputStream != null && objectOutputStream != null && sock != null && !sock.isClosed()) {
            try {
                objectInputStream.close();
                objectOutputStream.close();
                sock.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}
