/**
 * Иконки
 * https://www.flaticon.com/free-icon/chat_134806
 */


package messenger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Controller {
    private boolean alignment = true;

    @FXML
    Button btnSend;
    @FXML
    Button btnClose;
    @FXML
    Button btnHide;
    @FXML
    TextField messageField;
    @FXML
    VBox chatMessages;
    @FXML
    ScrollPane chatScroll;

    @FXML
    public void sendMessage() {
        // Формируем контейнер для сообщения
        Label message = new Label(messageField.getText());
        message.setStyle("-fx-background-color: #2f4f4f;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;");

        HBox hb = new HBox();

        hb.setStyle("-fx-padding: 0;" +
                "-fx-border-color: transparent;" +
                "-fx-background-color: white;" +
                "-fx-border-width: 0px;" +
                "-fx-alignment: " + (alignment ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT) + ";" +
                "-fx-spacing: 5;");
        alignment = !alignment;
        hb.getChildren().add(message);
        HBox.setHgrow(message, Priority.ALWAYS);

        // Добавляем контейнер сообщения в скроллинг
        chatMessages.getChildren().add(hb);
        chatScroll.setVvalue(1.0);

        messageField.clear();
        messageField.requestFocus();
    }


    @FXML
    public void closeWindow() {
        Platform.exit();
    }

    @FXML
    void minimizeWindow() {
        Stage stage = (Stage) btnHide.getScene().getWindow();
        stage.setIconified(true);

    }
}
