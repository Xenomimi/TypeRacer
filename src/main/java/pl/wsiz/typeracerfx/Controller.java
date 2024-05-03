package pl.wsiz.typeracerfx;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;

public class Controller {

    @FXML
    private TextArea textArea;

    @FXML
    private TextField textField;

    @FXML
    private TextFlow textFlow;

    public TextArea getTextArea() {
        return textArea;
    }

    public TextField getTextField() {
        return textField;
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }
}
