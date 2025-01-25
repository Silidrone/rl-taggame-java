package chapter2;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

import chapter2.taggame.TagArena;
import chapter2.taggame.TagGame;

public class GameStateSender implements Runnable {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private TagGame tagGame;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private long lastSentTime = 0;
    private CountDownLatch latch;

    public GameStateSender(TagGame tagGame, CountDownLatch latch) {
        this.tagGame = tagGame;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Waiting for a client to connect...");

            clientSocket = serverSocket.accept();
            System.out.println("Client connected.");
            latch.countDown(); // Notify the main thread that the connection is established.
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            while (true) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastSentTime >= 100) {
                    sendGameState();
                    lastSentTime = currentTime;
                }

                Thread.sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            cleanupResources();
        }
    }

    private void sendGameState() {
        try {
            TagArena arena = tagGame.getArena();
            String gameState = arena.getGameStateAsString(tagGame.getArena().getPlayers().get(0));
            out.println(gameState);
            System.out.println(gameState);
        } catch (Exception e) {
            System.err.println("Error while sending game state: " + e.getMessage());
        }
    }

    private void cleanupResources() {
        try {
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error while cleaning up resources: " + e.getMessage());
        }
    }
}
