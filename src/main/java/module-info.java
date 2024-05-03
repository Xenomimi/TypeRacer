module pl.wsiz.typeracerfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;

    opens pl.wsiz.typeracerfx to javafx.fxml;
    exports pl.wsiz.typeracerfx;
}