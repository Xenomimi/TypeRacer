package pl.wsiz.typeracerfx.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FXGLButton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import pl.wsiz.typeracerfx.ApiCall;
import pl.wsiz.typeracerfx.HelloApplication;

public class CustomMainMenu extends FXGLMenu {

    private VBox menuBox;
    private ApiCall apiCall;

    public CustomMainMenu(ApiCall apiCall) {
        super(MenuType.MAIN_MENU);
        this.apiCall = apiCall;

        // Load and set the background image
        ImageView background = new ImageView(FXGL.image("main_background.png"));
        background.setFitWidth(FXGL.getAppWidth());
        background.setFitHeight(FXGL.getAppHeight());
        getContentRoot().getChildren().add(background);
        getContentRoot().getStylesheets().add(getClass().getResource("/assets/ui/css/style.css").toExternalForm());


        // Create buttons
        FXGLButton btnMulti = new FXGLButton("MULTIPLAYER");
        FXGLButton btnStart = new FXGLButton("TRAINING");
        FXGLButton btnSettings = new FXGLButton("SETTINGS");
        FXGLButton btnExit = new FXGLButton("EXIT");

        btnStart.getStyleClass().add("button-style");
        btnMulti.getStyleClass().add("button-style");
        btnSettings.getStyleClass().add("button-style");
        btnExit.getStyleClass().add("button-style");

        // Set button actions
        btnMulti.setOnAction(e -> gotoMultiplayerMenu());
        btnStart.setOnAction(e -> {
            FXGL.<HelloApplication>getAppCast().setMyFlag(false);
            FXGL.<HelloApplication>getAppCast().setTraining(true);
            FXGL.getGameController().startNewGame();
        });
        btnExit.setOnAction(e -> FXGL.getGameController().exit());
        btnSettings.setOnAction(e -> gotoSettingsMenu());  // Updated action for settings

        // Styling for demonstration
        Text title = new Text("TypeRacerFX");
        title.setFill(Color.WHITE);
        title.setFont(FXGL.getUIFactoryService().newFont(48));

        // Layout
        menuBox = new VBox(10, title, btnMulti, btnStart, btnSettings, btnExit);  // Added btnSettings to the layout
        menuBox.setAlignment(Pos.CENTER); // Center alignment

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

    private void gotoSettingsMenu() {
        FXGL.getSceneService().popSubScene();
        FXGL.getSceneService().pushSubScene(new SettingsMenu(apiCall));
    }

    private void gotoMultiplayerMenu() {
        FXGL.getSceneService().popSubScene();
        FXGL.getSceneService().pushSubScene(new MultiplayerMenu(apiCall));
    }
}