package pl.wsiz.typeracerfx;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.text.Text;

public class MyPlayerFactory implements EntityFactory {
    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        System.out.println("Creating new player entity at position: " + data.getX() + ", " + data.getY());

        // Odczytanie dodatkowej informacji, np. nazwy gracza
        String playerNameString = data.get("name");

        // Create the player entity
        Entity player = FXGL.entityBuilder()
                .at(data.getX(), data.getY())
                .viewWithBBox("black_car.png")  // Make sure this asset exists
                .buildAndAttach();

        // Create the text
        Text playerName = new Text(playerNameString);
        playerName.setTranslateX(data.getX() - playerName.getLayoutBounds().getWidth() - 10);  // Adjust the X position to be left of the player
        playerName.setTranslateY(data.getY() + player.getBoundingBoxComponent().getHeight() / 2);  // Align the text vertically with the player

        // Add the text to the game world
        FXGL.getGameScene().addUINode(playerName);

        // Update text position when player moves
        player.xProperty().addListener((obs, old, newValue) -> playerName.setTranslateX(newValue.doubleValue() - playerName.getLayoutBounds().getWidth() - 10));
        player.yProperty().addListener((obs, old, newValue) -> playerName.setTranslateY(newValue.doubleValue() + player.getBoundingBoxComponent().getHeight() / 2));

        return player;
    }
}
