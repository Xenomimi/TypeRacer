package pl.wsiz.typeracerfx;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.net.Server;
import javafx.scene.control.CheckBox;
import javafx.util.Duration;

import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGL.*;

public class TwoWayConnectionSample extends GameApplication {

    private boolean isServer;

    private Server<Bundle> server;
    private Optional<Connection<Bundle>> clientConnection = Optional.empty();

    @Override
    protected void initSettings(GameSettings settings) { }

    @Override
    protected void initGame() {
        var cb = new CheckBox();
        cb.selectedProperty().addListener((o, old, isSelected) -> {
            var bundle = new Bundle("CheckBoxData");
            bundle.put("isSelected", isSelected);

            if (isServer) {
                // Aktualizuj stan CheckBoxa lokalnie na serwerze
                cb.setSelected(isSelected);
                // Rozsyłaj stan CheckBoxa do wszystkich klientów
                server.broadcast(bundle);
            } else {
                clientConnection.ifPresent(conn -> {
                    // Wysyłaj stan CheckBoxa do serwera
                    conn.send(bundle);
                });
            }
        });

        addUINode(cb, 100, 100);

        runOnce(() -> {
            getDialogService().showConfirmationBox("Is Server?", answer -> {
                isServer = answer;

                if (isServer) {
                    server = getNetService().newTCPServer(55555);
                    server.setOnConnected(conn -> {
                        conn.addMessageHandlerFX((connection, message) -> {
                            boolean isSelected = message.get("isSelected");

                            // Aktualizuj stan CheckBoxa na serwerze
                            cb.setSelected(isSelected);

                            // Rozsyłaj stan CheckBoxa do wszystkich klientów
                            server.broadcast(message);
                        });
                    });
                    server.startAsync();
                } else {
                    var client = getNetService().newTCPClient("localhost", 55555);
                    client.setOnConnected(connection -> {
                        clientConnection = Optional.of(connection);
                        connection.addMessageHandlerFX((conn, message) -> {
                            boolean isSelected = message.get("isSelected");

                            cb.setSelected(isSelected);
                        });
                    });
                    client.connectAsync();
                }
            });
        }, Duration.seconds(0.2));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
