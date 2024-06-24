package pl.wsiz.typeracerfx;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.texture.Texture;

public class MyPlayerFactory implements EntityFactory {

    Texture playerTexture = FXGL.texture("black_car.png");

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(PlayerType.PLAYER)
                .at(50, 300)
                .view(playerTexture)
                .buildAndAttach();
    }
}
