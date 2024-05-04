package pl.wsiz.typeracerfx;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.scene.Scene;
import com.almasb.fxgl.texture.Texture;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import pl.wsiz.typeracerfx.ui.CustomMainMenu;

import java.io.IOException;


public class HelloApplication extends GameApplication {

    private TextFlow textFlow;
    private TextField textField;
    private Entity player;
    private double playerStartX;
    private double playerEndX;
    private String textToType;
    private int currentIndex = 0;

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
        Texture playerTexture = FXGL.texture("black_car.png"); // Załaduj teksturę gracza
        player = FXGL.entityBuilder()
                .at(50, 300)
                .view(playerTexture) // Ustaw teksturę gracza
                .buildAndAttach(); // Dodaj gracza do sceny

        playerStartX = player.getX();
        playerEndX = player.getWidth() - 50;
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

            // Dodaj elementy UI do sceny gry
            FXGL.addUINode(root);  // Dodaje root do sceny FXGL, nie bezpośrednio do JavaFX Scene


            // Załaduj tekst do przepisania
            fillTextFlow();

            // Dodaj styl CSS
            root.getStylesheets().add(getClass().getResource("/assets/ui/css/style.css").toExternalForm()); // Załaduj i dodaj CSS

            // Dodaj logikę dla pola tekstowego
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > oldValue.length()) {
                    handleKeyPressed(newValue.charAt(oldValue.length()));
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void movePlayer(double progress) {
        double newX = playerStartX + (progress * (playerEndX - playerStartX));
        player.setX(newX);
    }

    private void handleProgressUpdate() {
        double progress = (double) currentIndex / textToType.length();
        movePlayer(progress);
    }

    private void handleKeyPressed(char key) {
        System.out.println("Naciśnięty klawisz: " + key);
        if (currentIndex < textToType.length()) {
            char expectedChar = textToType.charAt(currentIndex);
            Text text = (Text) textFlow.getChildren().get(currentIndex);
            Pane textContainer = (Pane) text.getParent(); // Pobranie kontenera zawierającego tekst

            if (key == expectedChar) {
                text.getStyleClass().clear(); // Usunięcie poprzednich stylów
                text.getStyleClass().add("text-correct"); // Dodanie klasy CSS dla poprawnych odpowiedzi
                currentIndex++;
                checkWinCondition(); // Sprawdzenie, czy cały tekst został poprawnie przepisany
            } else {
                System.out.println("Błąd w porównaniu znaków!");
                text.getStyleClass().clear();
                text.getStyleClass().add("text-error"); // Możesz dodać stylizację dla błędów
            }

            // Dodanie tła do kontenera zawierającego tekst
            textContainer.setStyle("-fx-background-color: derive(" + (key == expectedChar ? "limegreen" : "red") + ", 80%);");
            handleProgressUpdate();
        }
    }


    private void checkWinCondition() {
        // Sprawdzenie czy samochód dotarł do końca planszy
        if (currentIndex == textToType.length()) {
            // Gracz wygrał
            System.out.println("Wygrałeś!");
            // Tutaj możesz dodać kod, który wykonuje się po wygranej grze
        }
    }

    private void fillTextFlow() {
        ApiCall apiCall = new ApiCall();
        String textToAdd = apiCall.sendGetRequest(); // Załóżmy, że ta metoda zwraca tekst do przepisania

        while (textToAdd.length() < 150 || textToAdd.length() > 600) {
            textToAdd = apiCall.sendGetRequest();
        }

        textToType = textToAdd;
        textFlow.getChildren().clear();

        for (int i = 0; i < textToAdd.length(); i++) {
            Text text = new Text(String.valueOf(textToAdd.charAt(i)));
            text.getStyleClass().add("text-default"); // Przypisanie klasy CSS
            text.setFont(Font.font("Arial", 25)); // Ustawienie czcionki i rozmiaru
            textFlow.getChildren().add(text);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
