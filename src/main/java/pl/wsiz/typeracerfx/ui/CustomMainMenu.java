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

public class CustomMainMenu extends FXGLMenu {

    private VBox menuBox;

    public CustomMainMenu() {
        super(MenuType.MAIN_MENU);

        // Load and set the background image
        ImageView background = new ImageView(FXGL.image("main_background.png"));
        background.setFitWidth(FXGL.getAppWidth());
        background.setFitHeight(FXGL.getAppHeight());
        getContentRoot().getChildren().add(background);

        // Create buttons
        FXGLButton btnStart = new FXGLButton("START");
        FXGLButton btnSettings = new FXGLButton("SETTINGS");
        FXGLButton btnExit = new FXGLButton("EXIT");

        // Set button actions
        btnStart.setOnAction(e -> fireNewGame());
        btnExit.setOnAction(e -> FXGL.getGameController().exit());
        btnSettings.setOnAction(e -> gotoSettingsMenu());  // Updated action for settings

        // Styling for demonstration
        Text title = new Text("TypeRacerFX");
        title.setFill(Color.WHITE);
        title.setFont(FXGL.getUIFactoryService().newFont(48));

        // Layout
        menuBox = new VBox(10, title, btnStart, btnSettings, btnExit);  // Added btnSettings to the layout
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
        FXGL.getSceneService().pushSubScene(new SettingsMenu());
    }
}