package pl.wsiz.typeracerfx;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
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
    private Rectangle car;
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
    protected void initUI() {
        try {
            // Load UI from FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            AnchorPane root = loader.load();

            // Get the controller
            Controller controller = loader.getController();
            textFlow = controller.getTextFlow();
            textField = controller.getTextField(); // Zainicjuj pole textField

            // Initialize the car
            car = new Rectangle(40, 20, Color.RED);
            car.setTranslateY(100);
            car.setTranslateX(50);

            // Add UI nodes to the game scene
            FXGL.addUINode(root);
            FXGL.addUINode(car);

            // Load text to type
            fillTextFlow();

            // Kolorowanie tekstu
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > oldValue.length()) {
                    handleKeyPressed(newValue.charAt(oldValue.length()));
                }
            });

            // Check if typed text matches


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKeyPressed(char key) {
        if (currentIndex < textToType.length()) {
            char expectedChar = textToType.charAt(currentIndex);
            if (key == expectedChar) {
                // Color the correct letter green
                Text text = (Text) textFlow.getChildren().get(currentIndex);
                text.setFill(Color.GREEN);
                currentIndex++;
                checkWinCondition();
            }
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
        String textToAdd = apiCall.sendGetRequest();

        // Dopóki tekst jest zbyt krótki lub zbyt długi, losuj ponownie
        while (textToAdd.length() < 150 || textToAdd.length() > 600) {
            textToAdd = apiCall.sendGetRequest();
        }

        textToType = textToAdd;

        textFlow.getChildren().clear();

        for (int i = 0; i < textToAdd.length(); i++) {
            Text text = new Text(String.valueOf(textToAdd.charAt(i)));
            text.setFill(Color.BLACK);
            text.setFont(Font.font("Arial", 25)); // Ustawienie czcionki i rozmiaru
            textFlow.getChildren().add(text);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
