import java.io.*;
import java.net.*;
import java.util.Properties;

public class QuizClient {
    private static String SERVER_IP;
    private static int SERVER_PORT;

    public static void main(String[] args) {
        loadServerConfig();

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("Connected to Quiz Server at " + SERVER_IP + ":" + SERVER_PORT);

            Thread listenerThread = new Thread(new ClientListener(socket));
            Thread senderThread = new Thread(new ClientSender(socket));

            listenerThread.start();
            senderThread.start();

            listenerThread.join();
            senderThread.join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void loadServerConfig() {
        try (InputStream input = new FileInputStream("server_info.dat")) {
            Properties prop = new Properties();
            prop.load(input);
            SERVER_IP = prop.getProperty("server_ip", "localhost");
            SERVER_PORT = Integer.parseInt(prop.getProperty("server_port", "1234"));
        } catch (IOException ex) {
            System.err.println("Error loading server configuration, using defaults.");
            SERVER_IP = "localhost";
            SERVER_PORT = 1234;
        } catch (NumberFormatException ex) {
            System.err.println("Invalid port format in server_info.dat, using default port 1234.");
            SERVER_PORT = 1234;
        }
    }

    private static class ClientListener implements Runnable {
        private final Socket socket;

        public ClientListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    if (response.contains("final score")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Listener error: " + e.getMessage());
            }
        }
    }

    private static class ClientSender implements Runnable {
        private final Socket socket;

        public ClientSender(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
                String userInput;
                while ((userInput = console.readLine()) != null) {
                    if (userInput.trim().isEmpty()) { // Handle empty input
                        System.out.println("Invalid input! Please provide an answer.");
                        continue;
                    }
                    out.println(userInput);
                    if (userInput.equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Sender error: " + e.getMessage());
            }
        }
    }
}
