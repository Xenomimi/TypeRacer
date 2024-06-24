package pl.wsiz.typeracerfx;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.scene.Scene;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.ui.DialogFactoryService;
import com.almasb.fxgl.ui.DialogService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import pl.wsiz.typeracerfx.ui.CustomMainMenu;

import java.io.IOException;
import javafx.util.Duration;
import java.util.Arrays;
import java.util.Objects;
import javafx.application.Platform;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;


public class HelloApplication extends GameApplication {

    private TextFlow textFlow;
    private TextField textField;
    private Text exp_letter;

    private Text time;
    private boolean uiInitialized = false;
    private Entity player;
    private double timeLeft = 120;

    private String textToType;
    private int currentIndex = 0;
    private int currentWordLetterIndex = 0;
    private String currentWordStack;
    private int currentWordIndex = 0;
    private String[] wordsInTextToType;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1200);
        settings.setHeight(800);
        settings.setTitle("TypeRacer");
        settings.setVersion("0.1");
        settings.setAppIcon("icon.png");
        settings.setMainMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new CustomMainMenu();
            }
        });
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new MyPlayerFactory());
        player = spawn("player");
    }

    @Override
    protected void initUI() {
        try {
            // Load UI from FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            AnchorPane root = loader.load();  // Załaduj główny layout z FXML

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
                        FXGL.getGameController().gotoMainMenu();
                    });
                }
            }, Duration.seconds(1));

            updateDisplayedTime(); // Pierwsze aktualizowanie wyświetlacza czasu
            // Dodaje root do sceny FXGL, nie bezpośrednio do JavaFX Scene
            FXGL.addUINode(root);

            // Załaduj tekst do przepisania
            fillTextFlow();

            // Dodaj styl CSS
            root.getStylesheets().add(getClass().getResource("/assets/ui/css/style.css").toExternalForm());

            // Dodaj logikę dla pola tekstowego
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() < oldValue.length()) {
                    // Tekst został skrócony, co oznacza użycie backspace
                    handleBackspacePressed();
                } else if (newValue.length() > oldValue.length()) {
                    // Nowy znak został dodany
                    handleKeyPressed(newValue.charAt(oldValue.length()));
                }
            });


        } catch (IOException e) {
            System.out.println("Failed to load FXML file.");
            e.printStackTrace();
        }
    }

    private void movePlayer(double progress) {
        double newX = 100 + (progress * 1000);
        player.setX(newX);
    }

    private void handleProgressUpdate() {
        double progress = (double) currentWordIndex / wordsInTextToType.length;
        movePlayer(progress);
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

        String currentWord = wordsInTextToType[currentWordIndex];
        char expectedChar = currentWordLetterIndex < currentWord.length() ? currentWord.charAt(currentWordLetterIndex) : ' ';

        // Update expected letter display
        updateExpectedLetterDisplay();

        // Get the container for the current word
        HBox wordContainer = (HBox) textFlow.getChildren().get(currentWordIndex * 2);

        if (key == expectedChar) {
            // Correct key pressed
            if (currentWordLetterIndex < currentWord.length()) {
                Label correctLabel = (Label) wordContainer.getChildren().get(currentWordLetterIndex);
                correctLabel.getStyleClass().clear();
                correctLabel.getStyleClass().add("text-correct");
            }

            currentWordLetterIndex++;

            // Move to next word if current word is completed
            if (currentWordLetterIndex > currentWord.length()) {
                currentWordIndex++;
                currentWordLetterIndex = 0;
                underlineWordInTextFlow(textFlow, currentWordIndex - 1, false);
                if (currentWordIndex < wordsInTextToType.length) {
                    underlineWordInTextFlow(textFlow, currentWordIndex, true);
                }

                // Clear the TextField after completing a word
                Platform.runLater(() -> {
                    textField.clear();
                    textField.setText("");
                });
            }
        } else {
            // Incorrect key pressed
            if (currentWordLetterIndex < currentWord.length()) {
                Label errorLabel = (Label) wordContainer.getChildren().get(currentWordLetterIndex);
                errorLabel.getStyleClass().clear();
                errorLabel.getStyleClass().add("text-error");
            }
            // Do not increment currentWordLetterIndex for incorrect input
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
            exp_letter.setText(String.valueOf(currentWord.charAt(currentWordLetterIndex)));
        } else if (currentWordIndex + 1 < wordsInTextToType.length) {
            exp_letter.setText("spacja");
        } else {
            exp_letter.setText("- Koniec tekstu -");
        }
    }

    private void fillTextFlow() {
        // Losowanie odpowiednio długiego tekstu do przepisania
        ApiCall apiCall = new ApiCall();
        String textToAdd = apiCall.sendGetRequest();
        while (textToAdd.length() < 150 || textToAdd.length() > 600) {
            textToAdd = apiCall.sendGetRequest();
        }

        // Rozdzielenie tekstu na słowa
        wordsInTextToType = textToAdd.split("\\s+");  // Dzieli tekst na słowa używając spacji jako separatora

        // Przypisanie do zmiennej globalnej
        textToType = textToAdd;
        textFlow.getChildren().clear();

        // Uzupełnienie pola TextFlow literami o odpowiednich klasach CSS
        for (String word : wordsInTextToType) {
            // Dla każdego słowa tworzymy osobny kontener (HBox), aby łatwiej było zarządzać stylem całego słowa
            HBox wordContainer = new HBox();

            for (int i = 0; i < word.length(); i++) {
                Label label = new Label(String.valueOf(word.charAt(i)));
                label.setFont(Font.font("Arial", 25));
                wordContainer.getChildren().add(label);
            }

            // Dodanie spacji jako Label pomiędzy słowami, aby zachować odstępy
            Label space = new Label(" ");
            space.setFont(Font.font("Arial", 25));

            // Dodawanie słowa do TextFlow
            textFlow.getChildren().add(wordContainer);
            textFlow.getChildren().add(space);
        }
        underlineWordInTextFlow(textFlow, currentWordIndex, true);
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

    public static void main(String[] args) {
        launch(args);
    }
}
