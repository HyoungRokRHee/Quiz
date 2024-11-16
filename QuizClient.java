import java.io.*;
import java.net.*;
import java.util.Properties;

public class QuizClient {
    private static String SERVER_IP;
    private static int SERVER_PORT;

    public static void main(String[] args) {
        loadServerConfig();

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to Quiz Server at " + SERVER_IP + ":" + SERVER_PORT);

            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.startsWith("QUESTION:")) {
                    System.out.println(serverResponse);
                    System.out.print("Your answer: ");
                    String answer = console.readLine();
                    out.println(answer);
                } else {
                    System.out.println(serverResponse);
                }
            }

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static void loadServerConfig() {
        try (InputStream input = new FileInputStream("server_info.dat")) {
            Properties prop = new Properties();
            prop.load(input);
            SERVER_IP = prop.getProperty("server_ip", "localhost");
            SERVER_PORT = Integer.parseInt(prop.getProperty("server_port", "1234"));
        } catch (IOException ex) {
            SERVER_IP = "localhost";
            SERVER_PORT = 1234;
        }
    }
}
