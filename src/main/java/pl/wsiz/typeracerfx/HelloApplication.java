package pl.wsiz.typeracerfx;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.net.Server;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import pl.wsiz.typeracerfx.ui.CustomMainMenu;

import java.io.IOException;

import javafx.util.Duration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import pl.wsiz.typeracerfx.ui.CustomPauseGameMenu;


public class HelloApplication extends GameApplication {

    private TextFlow textFlow;
    private TextField textField;
    private Text exp_letter;
    private Text time;
    private boolean uiInitialized = false;
    private Entity player;
    private Entity player1;
    private double timeLeft;

    private String textToType;
    private int currentIndex = 0;
    private int currentWordLetterIndex = 0;
    private int currentWordIndex = 0;
    private String[] wordsInTextToType;

    private Server<Bundle> server;
    private Optional<Connection<Bundle>> clientConnection = Optional.empty();
    private boolean isServer = false;
    private boolean isTraining = false;
    private Connection<Bundle> connection;

    // Licznik WPM
    private long startTime;
    private int correctCharCount = 0;
    private Text wpmText;
    private boolean isGameFinished = false;

    // Stan gry na serwerze
    private Bundle gameStateBundle;
    private boolean textInitialized = false;
    private Entity localPlayer;
    private Entity remotePlayer;

    private ApiCall apiCall;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1200);
        settings.setHeight(800);
        settings.setTitle("TypeRacer");
        settings.setVersion("0.1");
        settings.setAppIcon("icon.png");
        settings.setMainMenuEnabled(true);
        settings.addEngineService(MultiplayerService.class);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                apiCall = new ApiCall();
                return new CustomMainMenu(apiCall);
            }
            @Override
            public FXGLMenu newGameMenu() {
                return new CustomPauseGameMenu(apiCall);
            }
        });
        System.out.println("Settings initialized. isServer: " + isServer + ", isTraining: " + isTraining);

    }

    @Override
    protected void initGame() {


        System.out.println("Initializing game. isServer: " + isServer + ", isTraining: " + isTraining);

        FXGL.getGameWorld().addEntityFactory(new MyPlayerFactory());
        System.out.println("EntityFactory added to GameWorld");

        FXGL.runOnce(() -> {
            System.out.println("Running initialization. isServer: " + isServer + ", isTraining: " + isTraining);
            if (isTraining) {
                System.out.println("Initializing training mode");
                timeLeft = 100;
                spawnPlayer();
            } else if (isServer) {
                System.out.println("Initializing server");
                timeLeft = 100;
                initServer();
                spawnPlayer();  // Spawn player immediately for server
            } else {
                System.out.println("Initializing client");
                initClient();
            }
        }, Duration.seconds(0.2));
    }

    private void spawnPlayer() {
        System.out.println("Attempting to spawn player");
        SpawnData data = new SpawnData(100, 100).put("name", "Player 1");
        localPlayer = FXGL.spawn("player", data);
        if (localPlayer != null) {
            System.out.println("Player spawned successfully. Position: " + localPlayer.getPosition());
        } else {
            System.out.println("Failed to spawn player!");
        }
    }


    private void initServer() {
        System.out.println("Setting up server");
        server = FXGL.getNetService().newTCPServer(55555);

        server.setOnConnected(conn -> {
            System.out.println("Client connected to server");
            conn.addMessageHandlerFX((connection, message) -> {
                System.out.println("Server received message: " + message.getName());
                if (message.getName().equals("progressData")) {
                    double progress = message.get("progress");
                    boolean isFromServer = message.get("isServer");
                    if (!isFromServer) {
                        moveRemotePlayer(progress);
                    }
                    // Broadcast the progress to all clients
                    server.broadcast(message);
                }
            });

            // Przygotuj bundle z aktualnym stanem gry
            gameStateBundle = new Bundle("gameState");
            gameStateBundle.put("timeLeft", timeLeft);
            gameStateBundle.put("textToType", textToType);

            // Wyślij stan gry do klienta
            conn.send(gameStateBundle);
            System.out.println("Server sent game state to client");
        });
        server.startAsync();
        System.out.println("Server started asynchronously");
    }

    private void initClient() {
        System.out.println("Setting up client");
        var client = FXGL.getNetService().newTCPClient("localhost", 55555);

        client.setOnConnected(conn -> {
            System.out.println("Client connected to server");
            clientConnection = Optional.of(conn);
            conn.addMessageHandlerFX((conn2, message) -> {
                System.out.println("Client received message: " + message.getName());
                if (message.getName().equals("gameState")) {
                    // Odbierz i ustaw stan gry
                    timeLeft = message.get("timeLeft");
                    textToType = message.get("textToType");
                    Platform.runLater(() -> {
                        updateDisplayedTime();
                        fillTextFlow();
                        textInitialized = true;
                    });
                    System.out.println("Client received and set game state");
                    spawnPlayer();  // Spawn the client's player when game state is received
                } else if (message.getName().equals("progressData")) {
                    double progress = message.get("progress");
                    boolean isFromServer = message.get("isServer");
                    if (isFromServer) {
                        moveRemotePlayer(progress);
                    }
                }
            });
        });

        // Dodajemy obsługę błędu połączenia
        FXGL.getExecutor().schedule(() -> {
            if (!clientConnection.isPresent()) {
                System.out.println("Connection failed: Timeout");
                Platform.runLater(this::showConnectionErrorDialog);
            }
        }, Duration.seconds(2));

        client.connectAsync();
        System.out.println("Client started connection asynchronously");
    }

    private void showConnectionErrorDialog() {
        FXGL.getDialogService().showMessageBox("Nie znaleziono serwera!", () -> {
            FXGL.getGameController().gotoMainMenu();
        });
    }

    @Override
    protected void initUI() {
        try {
            // Load UI from FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            AnchorPane root = loader.load();

            // Pobierz kontroler
            Controller controller = loader.getController();
            textFlow = controller.getTextFlow();
            textField = controller.getTextField();
            exp_letter = controller.getEx_letter();
            time = controller.getTime();

            // Uruchamia timer dopiero po zainicjalizowaniu UI
            FXGL.run(() -> {
                timeLeft--;
                updateDisplayedTime();

                if (timeLeft <= 0) {
                    FXGL.getGameTimer().clear();
                    FXGL.getDialogService().showMessageBox("Czas się skończył!", () -> {
                        // Przejście do menu głównego
                        resetGameVariables();
                        FXGL.getGameController().gotoMainMenu();
                    });
                }
            }, Duration.seconds(1));

            updateDisplayedTime();
            FXGL.addUINode(root);

            // Dodaj styl CSS
            root.getStylesheets().add(getClass().getResource("/assets/ui/css/style.css").toExternalForm());

            // Dodaj logikę dla pola tekstowego
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() < oldValue.length() && !newValue.isEmpty()) {
                    handleBackspacePressed();
                } else if (newValue.length() > oldValue.length()) {
                    handleKeyPressed(newValue.charAt(oldValue.length()));
                }
            });

            // Inicjalizuj tekst tylko jeśli nie jesteśmy klientem lub tekst już został zainicjalizowany
            if (isServer || textInitialized || isTraining) {
                fillTextFlow();
            }

            wpmText = controller.getWPM();
            updateWPM();

            startWPMTimer();

        } catch (IOException e) {
            System.out.println("Failed to load FXML file.");
            e.printStackTrace();
        }
    }

    private void movePlayer(double progress) {
        if (localPlayer != null) {
            double newX = 100 + (progress * 1000);
            localPlayer.setX(newX);
            System.out.println("Moved local player to X: " + newX);

            // Broadcast progress to server or other clients if not training
            if (!isTraining) {
                Bundle bundle = new Bundle("progressData");
                bundle.put("progress", progress);
                bundle.put("isServer", isServer);
                if (isServer) {
                    server.broadcast(bundle);
                    System.out.println("Server broadcast progress update");
                } else {
                    clientConnection.ifPresent(conn -> {
                        conn.send(bundle);
                        System.out.println("Client sent progress update to server");
                    });
                }
            }
        } else {
            System.out.println("Cannot move player: local player is null");
        }
    }

    private void moveRemotePlayer(double progress) {
        if (remotePlayer == null) {
            System.out.println("Spawning remote player");
            SpawnData data = new SpawnData(100, 150).put("name", "Player 2");
            remotePlayer = FXGL.spawn("player", data);
        }
        if (remotePlayer != null) {
            double newX = 100 + (progress * 1000);
            remotePlayer.setX(newX);
            System.out.println("Moved remote player to X: " + newX);
        } else {
            System.out.println("Failed to spawn or move remote player");
        }
    }

    private void handleProgressUpdate() {
        if (localPlayer != null) {
            double progress = (double) currentWordIndex / wordsInTextToType.length;
            movePlayer(progress);
        } else {
            System.out.println("Cannot handle progress update: local player is null");
        }
    }

    private void handleBackspacePressed() {
        System.out.println("Backspace was pressed");
        // Dodaj tutaj więcej logiki odpowiedniej dla twojej aplikacji
    }

    private void updateDisplayedTime() {
        // Konwersja sekund na format MM:SS
        int minutes = (int) timeLeft / 60;
        int seconds = (int) timeLeft % 60;

        String timeText = String.format("%02d:%02d", minutes, seconds);
        time.setText(timeText);
    }

    private void handleKeyPressed(char key) {
        if (currentWordIndex >= wordsInTextToType.length) {
            System.out.println("Koniec tekstu");
            textField.setDisable(true);
            return;
        }

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        String currentWord = wordsInTextToType[currentWordIndex];
        char expectedChar = currentWordLetterIndex < currentWord.length() ? currentWord.charAt(currentWordLetterIndex) : ' ';

        HBox wordContainer = (HBox) textFlow.getChildren().get(currentWordIndex * 2);

        if (key == expectedChar) {
            if (currentWordLetterIndex < currentWord.length()) {
                Label correctLabel = (Label) wordContainer.getChildren().get(currentWordLetterIndex);
                correctLabel.getStyleClass().clear();
                correctLabel.getStyleClass().add("text-correct");
                correctCharCount++;
            }

            currentWordLetterIndex++;
            updateExpectedLetterDisplay();

            if (currentWordLetterIndex > currentWord.length()) {
                currentWordIndex++;
                currentWordLetterIndex = 0;
                underlineWordInTextFlow(textFlow, currentWordIndex - 1, false);
                if (currentWordIndex < wordsInTextToType.length) {
                    underlineWordInTextFlow(textFlow, currentWordIndex, true);
                }

                Platform.runLater(() -> {
                    textField.clear();
                    textField.setText("");
                });

                updateExpectedLetterDisplay();
            }
        } else {
            if (currentWordLetterIndex < currentWord.length()) {
                Label errorLabel = (Label) wordContainer.getChildren().get(currentWordLetterIndex);
                errorLabel.getStyleClass().clear();
                errorLabel.getStyleClass().add("text-error");
            }
        }

        if (currentWordIndex >= wordsInTextToType.length) {
            finishGame();
        }

        handleProgressUpdate();
    }

    private void updateExpectedLetterDisplay() {
        if (currentWordIndex >= wordsInTextToType.length) {
            exp_letter.setText("- Koniec tekstu -");
            return;
        }

        String currentWord = wordsInTextToType[currentWordIndex];
        if (currentWordLetterIndex < currentWord.length()) {
            // Pokazujemy następną literę, którą użytkownik powinien wpisać
            exp_letter.setText(String.valueOf(currentWord.charAt(currentWordLetterIndex)));
        } else if (currentWordIndex + 1 < wordsInTextToType.length) {
            // Jeśli to koniec słowa, pokazujemy "spacja"
            exp_letter.setText("spacja");
        } else {
            // Jeśli to ostatnie słowo i ostatnia litera, pokazujemy koniec tekstu
            exp_letter.setText("- Koniec tekstu -");
        }
    }

    private void fillTextFlow() {
        if (!textInitialized) {
            if (isServer || isTraining) {
                // Losowanie odpowiednio długiego tekstu do przepisania tylko na serwerze
                String textToAdd = apiCall.sendGetRequest();
                while (textToAdd.length() < 150 || textToAdd.length() > 600) {
                    textToAdd = apiCall.sendGetRequest();
                }
                textToType = textToAdd;
            }
            // W przeciwnym razie używamy textToType ustawionego przez klienta

            // Rozdzielenie tekstu na słowa
            wordsInTextToType = textToType.split("\\s+");
        }

        textFlow.getChildren().clear();

        // Uzupełnienie pola TextFlow literami o odpowiednich klasach CSS
        for (String word : wordsInTextToType) {
            HBox wordContainer = new HBox();

            for (int i = 0; i < word.length(); i++) {
                Label label = new Label(String.valueOf(word.charAt(i)));
                label.setFont(Font.font("Arial", 25));
                wordContainer.getChildren().add(label);
            }

            Label space = new Label(" ");
            space.setFont(Font.font("Arial", 25));

            textFlow.getChildren().add(wordContainer);
            textFlow.getChildren().add(space);
        }
        underlineWordInTextFlow(textFlow, currentWordIndex, true);
        textInitialized = true;
    }

    public void underlineWordInTextFlow(TextFlow textFlow, int wordIndex, boolean underline) {
        int currentWordIndex = 0; // Licznik do śledzenia indeksu słów

        // Iteracja przez wszystkie dzieci TextFlow
        for (Node node : textFlow.getChildren()) {
            if (node instanceof Label) {
                continue;
            } else if (node instanceof HBox) {
                // Sprawdzamy, czy bieżący HBox to ten, który zawiera słowo do podkreślenia
                if (currentWordIndex == wordIndex) {
                    HBox wordContainer = (HBox) node;

                    // Ustawienie lub usunięcie klasy podkreślającej w zależności od parametru underline
                    if (underline) {
                        if (!wordContainer.getStyleClass().contains("underline")) {
                            wordContainer.getStyleClass().add("underline");
                        }
                    } else {
                        wordContainer.getStyleClass().remove("underline");
                    }
                    break; // Zakończenie pętli po znalezieniu i podkreśleniu odpowiedniego słowa
                }
                currentWordIndex++; // Inkrementacja licznika słów
            }
        }
    }

    public void cleanupAndExit() {
        if (isServer && server != null) {
            System.out.println("Shutting down server...");
            server.stop();
            server = null;
        }
        resetGameVariables();
        FXGL.getGameController().gotoMainMenu();
    }

    public void resetGameVariables() {
        currentIndex = 0;
        currentWordLetterIndex = 0;
        currentWordIndex = 0;
        textToType = "";
        wordsInTextToType = null;
        timeLeft = 0;
        textInitialized = false;
        clientConnection = Optional.empty();
        localPlayer = null;
        remotePlayer = null;
        startTime = 0;
        correctCharCount = 0;
        isGameFinished = false;
        updateWPM();
        System.out.println("Game variables have been reset");
    }

    public void setMyFlag(boolean flag) {
        this.isServer = flag;
        this.isTraining = false;
        System.out.println("Set isServer flag to: " + flag + ", isTraining set to false");
    }

    public void setTraining(boolean flag) {
        this.isTraining = flag;
        this.isServer = false;
        System.out.println("Set isTraining flag to: " + flag + ", isServer set to false");
    }

    private void startWPMTimer() {
        FXGL.run(() -> {
            if (!isGameFinished) {
                updateWPM();
            }
        }, Duration.seconds(1));
    }

    private void updateWPM() {
        if (startTime == 0) return;

        long elapsedTime = System.currentTimeMillis() - startTime;
        double minutes = elapsedTime / 60000.0;
        double wpm = (correctCharCount / 5.0) / minutes;

        Platform.runLater(() -> wpmText.setText(String.format("%.0f WPM", wpm)));
    }

    private void showGameFinishedDialog(double finalWPM, long totalTimeSeconds) {
        Button btnGoBack = FXGL.getUIFactoryService().newButton("Back to Main Menu");
        btnGoBack.setOnAction(e -> {
            cleanupAndExit();
            setMyFlag(true);
            setTraining(false);
            FXGL.getSceneService().pushSubScene(new CustomMainMenu(apiCall));
        });

        Text titleText = FXGL.getUIFactoryService().newText("Game Finished");
        titleText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text wpmText = FXGL.getUIFactoryService().newText(String.format("Your WPM: %.0f", finalWPM));
        Text timeText = FXGL.getUIFactoryService().newText(String.format("Total Time: %d seconds", totalTimeSeconds));

        VBox content = new VBox(10,
                titleText,
                wpmText,
                timeText,
                btnGoBack
        );
        content.setAlignment(Pos.CENTER);

        // Używamy showBox zamiast showMessageBox
        FXGL.getDialogService().showBox("Game Results", content);
    }

    private void finishGame() {
        isGameFinished = true;
        long endTime = System.currentTimeMillis();
        long totalTimeMillis = endTime - startTime;
        long totalTimeSeconds = totalTimeMillis / 1000;
        double finalWPM = (correctCharCount / 5.0) / (totalTimeMillis / 60000.0);

        // Używamy Platform.runLater, aby upewnić się, że UI jest aktualizowane w wątku JavaFX
        Platform.runLater(() -> showGameFinishedDialog(finalWPM, totalTimeSeconds));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
