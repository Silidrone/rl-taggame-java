package chapter2;

import java.io.*;
import java.net.*;

public class Communicator {
    public static final String RESET = "reset";
    public static final String EXIT = "exit";
    private static final int SERVER_PORT = 12345;
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;

    public Communicator() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Waiting for a client to connect...");
        clientSocket = serverSocket.accept();
        System.out.println("Client connected.");
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public String receiveAction() throws IOException {
        System.out.println("Waiting for action...");
        String action = in.readLine();
        if (action == null) {
            System.out.println("Connection closed by client.");
            throw new IOException("Connection closed by client.");
        }
        System.out.println("Received action: " + action);
        return action;
    }

    public void sendState(String state) {
        out.println(state);
        System.out.println("Sent state: " + state);
    }

    public void close() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (clientSocket != null) clientSocket.close();
        if (serverSocket != null) serverSocket.close();
        System.out.println("Server shut down.");
    }
}
