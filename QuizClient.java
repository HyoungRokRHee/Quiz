import java.io.*;
import java.net.*;
import java.util.Properties;

public class QuizClient {
    private static String SERVER_IP;
    private static int SERVER_PORT;

    public static void main(String[] args) {
        loadServerConfig(); // 서버 설정 파일 불러오기

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("서버에 연결되었습니다: " + SERVER_IP + ":" + SERVER_PORT);

            // 서버 메시지를 듣는 쓰레드
            Thread listenerThread = new Thread(new ClientListener(socket));
            // 사용자의 답변을 보내는 쓰레드
            Thread senderThread = new Thread(new ClientSender(socket));

            listenerThread.start();
            senderThread.start();

            // 두 쓰레드가 모두 종료될 때까지 대기
            listenerThread.join();
            senderThread.join();

        } catch (IOException e) {
            System.err.println("서버에 연결할 수 없습니다: " + SERVER_IP + ":" + SERVER_PORT);
        } catch (InterruptedException e) {
            System.err.println("쓰레드가 중단되었습니다: " + e.getMessage());
        }
    }

    private static void loadServerConfig() {
        // server_info.dat 파일에서 서버 설정 불러오기
        try (InputStream input = new FileInputStream("server_info.dat")) {
            Properties prop = new Properties();
            prop.load(input);
            SERVER_IP = prop.getProperty("server_ip", "localhost");
            SERVER_PORT = Integer.parseInt(prop.getProperty("server_port", "1234"));
        } catch (IOException ex) {
            System.err.println("서버 설정 파일 로드 오류, 기본 설정 사용.");
            SERVER_IP = "localhost";
            SERVER_PORT = 1234;
        } catch (NumberFormatException ex) {
            System.err.println("잘못된 포트 형식, 기본 포트 1234 사용.");
            SERVER_PORT = 1234;
        }
    }

    // 서버로부터 메시지를 수신하는 Listener 클래스
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
                    // "final score" 메시지를 받으면 종료
                    if (response.contains("final score")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Listener 오류: " + e.getMessage());
            }
        }
    }

    // 사용자의 입력을 서버로 보내는 Sender 클래스
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
                    // 빈 입력값을 허용하지 않음
                    if (userInput.trim().isEmpty()) {
                        System.out.println("잘못된 입력입니다. 답변을 입력하세요.");
                        continue;
                    }
                    out.println(userInput);
                    // 사용자가 "exit" 입력 시 종료
                    if (userInput.equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Sender 오류: " + e.getMessage());
            }
        }
    }
}
