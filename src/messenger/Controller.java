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
import server.ClientHandler;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import static utils.Share.*;

public class Controller {

    boolean isAuthorized;
    DataInputStream in;
    DataOutputStream out;
    Socket socket;


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
    VBox authPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    VBox chatArea;
    @FXML
    VBox scrollPanel;
    @FXML
    HBox messageArea;
    @FXML
    Label authReply;

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

    @FXML
    public void tryToAuth() {
        if (socket == null || socket.isClosed()) connect();

        try {
            out.writeUTF(PROT_MSG_AUTH + " " + loginField.getText() + " " + passwordField.getText());

            authReply.setVisible(false);
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            socket = new Socket(HOST, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread receiver = new Thread(new Runnable() {
                @Override
                public void run() {

                    String nickname;

                    try {

                        // Цикл авторизации
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith(PROT_MSG_AUTH_OK)) {
                                String[] parts = str.split(SEPARATOR);
                                nickname = parts[1];

                                setAuthorized(true);
                                break;
                            } else {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        authReply.setText(str);
                                        authReply.setVisible(true);
                                    }
                                });
                            }
                        }

                        // Цикл обработки сообщений.
                        // Их формат nick@@color@@message
                        while (true) {
                            String str = in.readUTF();

                            if (str.equals(PROT_MSG_SERVER_CLOSED)) break;

                            String[] parts = str.split(SEPARATOR);

                            // Чтобы избежать ошибки
                            // "Not on FX application thread"
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    stickMessage(parts[0].equals(nickname), parts[1], parts[0], parts[2]);
                                }
                            });
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
     */
    private void stickMessage(boolean isMine, String color, String nick, String text) {
        // Элементы отображения сообщения
        HBox hb = new HBox();
        VBox vb = new VBox();
        Label nickname = new Label(nick);
        Label message = new Label(text);

        // Стилевое оформление
        String fx_bg_conntent = "-fx-background-color: " + color;
        String fx_alignment_hb = "-fx-alignment: ";

        // Свои сообщения по левой стороне окна. Чужие по правой
        fx_alignment_hb += (isMine) ? (Pos.CENTER_LEFT + ";") : (Pos.CENTER_RIGHT + ";");

        hb.setStyle("-fx-padding: 10;" +
                "-fx-border-color: transparent;" +
                "-fx-background-color: white;" +
                "-fx-border-width: 0px;" +
                fx_alignment_hb +
                "-fx-spacing: 30;");

        vb.setStyle(fx_bg_conntent +
                "-fx-background-radius: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-spacing: 5;");

        nickname.setStyle("-fx-font-style: italic;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: baseline-left;" +
                "-fx-padding: 5 20 2 5;");

        message.setStyle("-fx-alignment: baseline-right;" +
                "-fx-padding: 0 5 5 20;");


//        message.setStyle(fx_background_msg +
//                "-fx-background-radius: 5;" +
//                "-fx-padding: 10 30 10 30;" +
//                "-fx-border-radius: 5;");

        vb.getChildren().addAll(nickname, message);

        if (isMine)
            hb.getChildren().addAll(prepareSticker(fx_bg_conntent), vb);
        else
            hb.getChildren().addAll(vb, prepareSticker(fx_bg_conntent));

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

    // Включение/Отключение панелей окна
    private void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        authPanel.setVisible(!isAuthorized);
        authPanel.setManaged(!isAuthorized);
        scrollPanel.setVisible(isAuthorized);
        scrollPanel.setManaged(isAuthorized);
        messageArea.setVisible(isAuthorized);
        messageArea.setManaged(isAuthorized);
    }

}
