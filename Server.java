package sockets;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 13337;
    private static final int TIMEOUT_MS = 30000; // Timeout in milliseconds

    private static List<Player> players = new ArrayList<>();//list or lonked list or 
    private static List<String> tickets = new ArrayList<>();
    private static String[] leaderboard = new String[5]; // Assuming only top 5

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);//start with server
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();//the server recieves client requests 
                clientSocket.setSoTimeout(TIMEOUT_MS); // Set timeout for client
                System.out.println("Client connected: " + clientSocket);

                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Player currentPlayer;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Client identification
                out.println("Enter your nickname:");
                String nickname = in.readLine();
                currentPlayer = getPlayerByNickname(nickname);

                if (currentPlayer == null) {
                    currentPlayer = new Player(nickname);
                    players.add(currentPlayer);
                    String ticket = generateTicket();
                    tickets.add(ticket);
                }

                // Send leaderboard to client
                sendLeaderboard();

                // Handle client messages
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Handle client messages here
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendLeaderboard() {
            StringBuilder leaderboardMsg = new StringBuilder("Leaderboard:\n");
            int count = 1;
            for (String entry : leaderboard) {
                if (entry != null) {
                    leaderboardMsg.append(count++).append(". ").append(entry).append("\n");
                }
                if (count > 5) break; // Display only top 5
            }
            out.println(leaderboardMsg.toString());
        }

        private String generateTicket() {
            return UUID.randomUUID().toString();
        }

        private Player getPlayerByNickname(String nickname) {
            for (Player player : players) {
                if (player.getNickname().equals(nickname)) {
                    return player;
                }
            }
            return null;
        }
    }
}