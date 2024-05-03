package pl.wsiz.typeracerfx.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FXGLButton;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

public class SettingsMenu extends FXGLMenu {

    public SettingsMenu() {
        super(MenuType.GAME_MENU);

        VBox menuBox = new VBox(10);
        menuBox.setAlignment(Pos.CENTER);

        // Title
        Label settingsTitle = new Label("Settings");
        settingsTitle.setFont(FXGL.getUIFactoryService().newFont(48));



        // Add a button to go back to the main menu
        FXGLButton btnBack = new FXGLButton("Back");
        btnBack.setOnAction(e -> {
            FXGL.getSceneService().popSubScene();
            FXGL.getSceneService().pushSubScene(new CustomMainMenu());
        });

        // Add all components to the VBox
        menuBox.getChildren().addAll(settingsTitle, btnBack);

        // Add the VBox to the scene
        getContentRoot().getChildren().add(menuBox);
    }
}
