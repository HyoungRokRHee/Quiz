import java.io.*;
import java.net.*;
import java.util.*;

public class QuizServer {
    private static final int PORT = 1234;
    private static final int POINT_PER_QUESTION = 20;
    private static final List<Question> questions = Arrays.asList(
        new Question("조선의 건국 시조는 누구인가요?", "이성계"),
        new Question("훈민정음을 창제한 왕은 누구인가요?", "세종대왕"),
        new Question("일제강점기, 한국의 독립을 위해 상하이에 설립된 임시 정부의 이름은 무엇인가요?", "대한민국 임시정부"),
        new Question("광복절은 몇 월 며칠인가요?", "8월 15일"),
        new Question("을사늑약이 체결된 해는 언제인가요?", "1905년"),
        new Question("일제강점기, 만주에서 한국 독립군을 이끌었던 장군은 누구인가요?", "김좌진"),
        new Question("3.1 운동이 일어난 해는 언제인가요?", "1919년"),
        new Question("조선의 마지막 황제는 누구인가요?", "순종"),
        new Question("일제강점기 때 국내에서 독립운동을 펼친 대표적인 비폭력 저항 운동은 무엇인가요?", "물산 장려 운동"),
        new Question("한국의 국보 1호는 무엇인가요?", "숭례문"),
        new Question("대한민국의 수도는 어디인가요?", "서울"),
        new Question("한글날은 몇 월 며칠인가요?", "10월 9일"),
        new Question("일제강점기, 독립운동가 유관순이 활약한 운동은 무엇인가요?", "3.1 운동"),
        new Question("고려의 건국 시조는 누구인가요?", "왕건"),
        new Question("임진왜란 때 활약한 장군은 누구인가요?", "이순신"),
        new Question("신라의 삼국통일을 완수한 왕은 누구인가요?", "문무왕"),
        new Question("대한민국 임시정부가 수립된 도시는 어디인가요?", "상하이"),
        new Question("독립운동가 안중근이 암살한 일본의 정치인은 누구인가요?", "이토 히로부미"),
        new Question("조선의 첫 번째 왕은 누구인가요?", "태조"),
        new Question("백제의 마지막 왕은 누구인가요?", "의자왕")
    );

    private final Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        new QuizServer().start();
    }

    public void start() {
        System.out.println("Starting the Quiz Server...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Quiz Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    public void removeClient(ClientHandler handler) {
        clientHandlers.remove(handler);
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final QuizServer server;
        private int score = 0;

        public ClientHandler(Socket clientSocket, QuizServer server) {
            this.clientSocket = clientSocket;
            this.server = server;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                out.println("Welcome to the History Quiz Game!");

                List<Question> selectedQuestions = getRandomQuestions(5);
                for (Question question : selectedQuestions) {
                    out.println("QUESTION: " + question.getQuestion());
                    String answer = in.readLine();

                    if (answer == null || answer.trim().isEmpty()) { // Handle empty answer
                        out.println("Invalid answer! Please try again.");
                        continue;
                    }

                    if (answer.equalsIgnoreCase(question.getAnswer())) {
                        out.println("Correct!");
                        score += POINT_PER_QUESTION;
                    } else {
                        out.println("Incorrect! The correct answer was: " + question.getAnswer());
                    }
                }
                out.println("Quiz complete! Your final score is: " + score + "/" + (selectedQuestions.size() * POINT_PER_QUESTION));

            } catch (IOException e) {
                System.err.println("Client handler error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
                server.removeClient(this);
            }
        }

        private List<Question> getRandomQuestions(int numberOfQuestions) {
            Collections.shuffle(questions);
            return questions.subList(0, numberOfQuestions);
        }
    }

    private static class Question {
        private final String question;
        private final String answer;

        public Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
