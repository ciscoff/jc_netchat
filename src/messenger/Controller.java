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
    private boolean alignmentSend = true;

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
    public void sendMessage() throws Exception {
        String iconPath = "D:\\aps\\GeekBrains\\quarter1\\jc_chat\\src\\img\\";

        // Элементы отображения сообщения
        HBox hb = new HBox();
        Label message = new Label(messageField.getText());
        ImageView imageView;
        Image image;

        // Стилевое оформление
        String fx_background_msg = "-fx-background-color: ";
        String fx_alignment_hb = "-fx-alignment:";

        if(alignmentSend) {
            iconPath += "chatS.png";
            fx_background_msg += "#efe4b0   ;";
            fx_alignment_hb += (Pos.CENTER_LEFT + ";");
        }
        else {
            iconPath += "chatR.png";
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

        // Добавить картинку
        FileInputStream input = new FileInputStream(iconPath);
        image = new Image(input);
        imageView = new ImageView(image);

        if(alignmentSend)
            hb.getChildren().addAll(imageView, message);
        else
            hb.getChildren().addAll(message, imageView);

        alignmentSend = !alignmentSend;
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
