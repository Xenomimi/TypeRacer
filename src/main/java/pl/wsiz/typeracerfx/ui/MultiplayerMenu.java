package pl.wsiz.typeracerfx.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FXGLButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import pl.wsiz.typeracerfx.HelloApplication;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class MultiplayerMenu extends FXGLMenu {

    private VBox menuBox;

    public MultiplayerMenu() {
        super(MenuType.MAIN_MENU);

        // Load and set the background image
        ImageView background = new ImageView(FXGL.image("main_background.png"));
        background.setFitWidth(FXGL.getAppWidth());
        background.setFitHeight(FXGL.getAppHeight());
        getContentRoot().getChildren().add(background);
        getContentRoot().getStylesheets().add(getClass().getResource("/assets/ui/css/style.css").toExternalForm());

        // Create buttons
        FXGLButton btnCreate = new FXGLButton("Create game");
        FXGLButton btnJoin = new FXGLButton("Join game");
        FXGLButton btnBack = new FXGLButton("Back");

        // Set CSS
        btnCreate.getStyleClass().add("button-style");
        btnJoin.getStyleClass().add("button-style");
        btnBack.getStyleClass().add("button-style");

        // Set button actions
        btnCreate.setOnAction(e -> {
            HelloApplication gameApp = (HelloApplication) getApp();
            gameApp.setMyFlag(true);
            fireNewGame();
        });

        TextField inputField = new TextField();
        inputField.setPromptText("Enter game code");

        Button btnJoinGame = getUIFactoryService().newButton("Join");
        Button btnGoBack = getUIFactoryService().newButton("Back");

        VBox content = new VBox(10,
                getUIFactoryService().newText("Enter GAME CODE to join:"),
                inputField,
                btnJoinGame,
                btnGoBack
        );
        content.setAlignment(Pos.CENTER);

        btnJoinGame.setOnAction(e -> {
            if (!inputField.getText().isEmpty()) {
                HelloApplication gameApp = (HelloApplication) getApp();
                gameApp.setMyFlag(false);
                fireNewGame();
            }
        });

        btnJoin.setOnAction(e ->
                FXGL.getDialogService().showBox("Join Game", content, btnGoBack)
        );

        btnBack.setOnAction(e -> gotoMainMenu());

        Text title = new Text("Multiplayer");
        title.setFill(Color.WHITE);
        title.setFont(FXGL.getUIFactoryService().newFont(48));

        menuBox = new VBox(10, title, btnCreate, btnJoin, btnBack);
        menuBox.setAlignment(Pos.CENTER);

        getContentRoot().getChildren().add(menuBox);

        menuBox.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            menuBox.setTranslateX((FXGL.getAppWidth() - newValue.getWidth()) / 2);
            menuBox.setTranslateY((FXGL.getAppHeight() - newValue.getHeight()) / 2);
        });
    }

    private void gotoMainMenu() {
        FXGL.getSceneService().popSubScene();
        FXGL.getSceneService().pushSubScene(new CustomMainMenu());
    }
}