/**
 * Иконки
 * https://www.flaticon.com/free-icon/chat_134806
 */


package messenger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;

public class Controller {

    boolean conversation = true;

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
        stickMessage(conversation);
        conversation = !conversation;

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


    /**
     * Отобразить сообщение
     *
     * @param isMine
     */
    private void stickMessage(boolean isMine) {
        // Элементы отображения сообщения
        HBox hb = new HBox();
        Label message = new Label(messageField.getText());

        // Стилевое оформление
        String fx_background_msg = "-fx-background-color: ";
        String fx_alignment_hb = "-fx-alignment:";

        if (isMine) {
            fx_background_msg += "#efe4b0;";
            fx_alignment_hb += (Pos.CENTER_LEFT + ";");
        } else {
            fx_background_msg += "#d2d2d2;";
            fx_alignment_hb += (Pos.CENTER_RIGHT + ";");
        }

        hb.setStyle("-fx-padding: 10;" +
                "-fx-border-color: transparent;" +
                "-fx-background-color: white;" +
                "-fx-border-width: 0px;" +
                fx_alignment_hb +
                "-fx-spacing: 30;");

        message.setStyle(fx_background_msg +
                "-fx-background-radius: 5;" +
                "-fx-padding: 10 30 10 30;" +
                "-fx-border-radius: 5;");


        if (isMine)
            hb.getChildren().addAll(prepareSticker(fx_background_msg), message);
        else
            hb.getChildren().addAll(message, prepareSticker(fx_background_msg));

        HBox.setHgrow(message, Priority.ALWAYS);

        // Добавляем контейнер сообщения в скроллинг
        chatMessages.getChildren().add(hb);
        chatScroll.setVvalue(1.0);

        messageField.clear();
        messageField.requestFocus();
    }

    /**
     * Сформировать стикер с иконкой сообщения
     */
    public HBox prepareSticker(String backgroundColor) {
        String iconPath = "file:icons/chat24x24.png";
        ImageView imageView = new ImageView(new Image(iconPath));

        HBox iconHb = new HBox();
        iconHb.setStyle("-fx-padding: 10;" +
                backgroundColor +
                "-fx-border-width: 0px;" +
                "-fx-border-radius: 50%;" +
                "-fx-background-radius: 50%");
        iconHb.getChildren().add(imageView);

        return iconHb;
    }
}
