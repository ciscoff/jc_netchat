/**
 * Иконки
 * https://www.flaticon.com/free-icon/chat_134806
 */


package messenger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import static utils.Share.*;

public class Controller implements Initializable {

    boolean conversation = true;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

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
//        stickMessage(conversation);
//        conversation = !conversation;

        try {
            out.writeUTF(messageField.getText());
            messageField.clear();
            messageField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket(HOST, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread receiver = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        while (true) {
                            final String str = in.readUTF();
                            if(str.equals("/serverClosed")) break;

                            String color = str.substring(0, colors[0].length());
                            str.substring(colors[0].length());

                            // Чтобы избежать ошибки
                            // "Not on FX application thread"
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    stickMessage(conversation,
                                            str.substring(colors[0].length()),
                                            str.substring(0, colors[0].length()));
                                }
                            });
                            conversation = !conversation;
                        }
                    } catch (IOException e) {
                        System.out.println("Connection closed");
                    } finally {
                        closeResources(in, out, socket);
                    }
                }
            });
            receiver.setDaemon(true);
            receiver.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отобразить сообщение
     *
     * @param isMine
     */
    private void stickMessage(boolean isMine, String text, String color) {
        // Элементы отображения сообщения
        HBox hb = new HBox();
        Label message = new Label(text);

        // Стилевое оформление
        String fx_background_msg = "-fx-background-color: ";
        String fx_alignment_hb = "-fx-alignment:";

        if (isMine) {
            fx_background_msg += color;
            fx_alignment_hb += (Pos.CENTER_LEFT + ";");
        } else {
            fx_background_msg += color;
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
