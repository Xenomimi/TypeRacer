package pl.wsiz.typeracerfx.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FXGLButton;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import pl.wsiz.typeracerfx.ApiCall;


public class SettingsMenu extends FXGLMenu {
    private VBox menuBox;
    private ApiCall apiCall;
    public SettingsMenu(ApiCall apiCall) {
        super(MenuType.GAME_MENU);

        this.apiCall = apiCall;

        // Load and set the background image
        ImageView background = new ImageView(FXGL.image("main_background.png"));
        background.setFitWidth(FXGL.getAppWidth());
        background.setFitHeight(FXGL.getAppHeight());
        getContentRoot().getChildren().add(background);
        getContentRoot().getStylesheets().add(getClass().getResource("/assets/ui/css/style.css").toExternalForm());

        menuBox = new VBox(10);
        menuBox.setAlignment(Pos.CENTER);

        // Title
        Text settingsTitle = new Text("Settings");
        settingsTitle.setFill(Color.WHITE);
        settingsTitle.setFont(FXGL.getUIFactoryService().newFont(48));

        // Book selection
        Text bookText = new Text("Select Book");
        bookText.setFill(Color.WHITE);
        bookText.setFont(FXGL.getUIFactoryService().newFont(24));

        ComboBox<String> bookComboBox = new ComboBox<>();
        bookComboBox.getItems().addAll(
                "Lalka Tom Pierwszy",
                "Pan Tadeusz",
                "W pustyni i w puszczy"
        );

        bookComboBox.setOnAction(e -> {
            String selectedBook = bookComboBox.getValue();
            switch (selectedBook) {
                case "Lalka Tom Pierwszy":
                    apiCall.setBookUrl("https://wolnelektury.pl/media/book/txt/lalka-tom-pierwszy.txt");
                    System.out.println(apiCall.bookUrl);
                    break;
                case "Pan Tadeusz":
                    apiCall.setBookUrl("https://wolnelektury.pl/media/book/txt/pan-tadeusz.txt");
                    System.out.println(apiCall.bookUrl);
                    break;
                case "W pustyni i w puszczy":
                    apiCall.setBookUrl("https://wolnelektury.pl/media/book/txt/w-pustyni-i-w-puszczy.txt");
                    System.out.println(apiCall.bookUrl);
                    break;
            }
        });

        // Add a button to go back to the main menu
        FXGLButton btnBack = new FXGLButton("Back");
        btnBack.getStyleClass().add("button-style");
        btnBack.setOnAction(e -> {
            FXGL.getSceneService().popSubScene();
            FXGL.getSceneService().pushSubScene(new CustomMainMenu(apiCall));
        });

        // Add all components to the VBox
        menuBox.getChildren().addAll(settingsTitle, bookText, bookComboBox, btnBack);

        // Add the VBox to the scene
        getContentRoot().getChildren().add(menuBox);

        // Center the menu after the scene has been rendered
        Platform.runLater(() -> {
            menuBox.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
                double width = newValue.getWidth();
                double height = newValue.getHeight();

                menuBox.setTranslateX((FXGL.getAppWidth() - width) / 2);
                menuBox.setTranslateY((FXGL.getAppHeight() - height) / 2);
            });
        });
    }
}