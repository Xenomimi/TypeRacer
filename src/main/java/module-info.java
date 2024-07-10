module pl.wsiz.typeracerfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires org.testng;
    requires org.junit.jupiter.api;

    opens assets.textures;
    opens assets.ui.css;

    opens pl.wsiz.typeracerfx to javafx.fxml;
    exports pl.wsiz.typeracerfx;
    exports pl.wsiz.typeracerfx.ui;
    opens pl.wsiz.typeracerfx.ui to javafx.fxml;
}