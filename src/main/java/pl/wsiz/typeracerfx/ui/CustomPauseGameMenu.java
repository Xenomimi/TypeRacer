package pl.wsiz.typeracerfx.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontType;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pl.wsiz.typeracerfx.ApiCall;
import pl.wsiz.typeracerfx.HelloApplication;

public class CustomPauseGameMenu extends FXGLMenu {
    private static final int TILE_SIZE = 200;
    private static final int TILE_GAP = 10;
    private ApiCall apiCall;

    public CustomPauseGameMenu(ApiCall apiCall) {
        super(MenuType.GAME_MENU);
        this.apiCall = apiCall;


        var background = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        background.setFill(Color.color(0.1, 0.1, 0.1, 0.8));

        var menuBox = new VBox(TILE_GAP);
        menuBox.setTranslateX(50);
        menuBox.setTranslateY((FXGL.getAppHeight() - (TILE_SIZE * 2 + TILE_GAP)) / 2.0);

        menuBox.getChildren().addAll(
                createMenuButton("Kontynuuj", this::fireResume),
                createMenuButton("Wyjdź", this::returnToMainMenu)
        );

        getContentRoot().getChildren().addAll(background, menuBox);
    }

    private Node createMenuButton(String name, Runnable action) {
        var button = new StackPane();
        button.setPrefSize(TILE_SIZE, TILE_SIZE);

        var bg = new Rectangle(TILE_SIZE, TILE_SIZE);
        bg.setArcWidth(15);
        bg.setArcHeight(15);
        bg.setFill(Color.color(0.5, 0.5, 0.5, 0.75));

        var text = FXGL.getUIFactoryService().newText(name, Color.WHITE, FontType.GAME, 24.0);

        button.getChildren().addAll(bg, text);

        button.setOnMouseEntered(e -> {
            bg.setFill(Color.color(0.6, 0.6, 0.6, 0.75));
            button.setEffect(new Glow(0.5));
        });

        button.setOnMouseExited(e -> {
            bg.setFill(Color.color(0.5, 0.5, 0.5, 0.75));
            button.setEffect(null);
        });

        button.setOnMouseClicked(e -> action.run());

        return button;
    }

    private void returnToMainMenu() {
        FXGL.<HelloApplication>getAppCast().cleanupAndExit();
        FXGL.<HelloApplication>getAppCast().setMyFlag(true);
        FXGL.<HelloApplication>getAppCast().setTraining(false);
        FXGL.getSceneService().pushSubScene(new CustomMainMenu(apiCall));

    }
}
