package pl.wsiz.typeracerfx;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Controller {

    @FXML
    private TextArea textArea;

    @FXML
    private Text ex_letter;

    @FXML
    private Text time;

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

    public Text getEx_letter() { return ex_letter; }

    public Text getTime() { return time; }

}
