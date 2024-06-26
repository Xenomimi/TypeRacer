package pl.wsiz.typeracerfx;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.texture.Texture;

public class MyPlayerFactory implements EntityFactory {
    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        System.out.println("Creating new player entity at position: " + data.getX() + ", " + data.getY());
        return FXGL.entityBuilder()
                .at(data.getX(), data.getY())
                .viewWithBBox("black_car.png")  // Make sure this asset exists
                .buildAndAttach();
    }
}
