package messenger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Controller {

    @FXML
    Button btnSend;

    @FXML
    Button btnClose;

    @FXML
    Button btnHide;

    @FXML
    TextField messageField;

    @FXML
    TextArea chatField;

    @FXML
    public void sendMessage() {
        chatField.appendText(messageField.getText() + "\n");
        messageField.clear();
        messageField.requestFocus();
    }

    @FXML
    public void closeWindow(){
        Platform.exit();
    }

    @FXML
    void minimizeWindow(){
        Stage stage = (Stage) btnHide.getScene().getWindow();
        stage.setIconified(true);

    }
}
