/**
 * Иконки
 * https://www.flaticon.com/free-icon/chat_134806
 */

package messenger;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

public class Controller implements Initializable {

    boolean isAuthorized;
    DataInputStream in;
    DataOutputStream out;
    Socket socket;

    @FXML
    VBox mainFrame;
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

    private static double xOffset = 0;
    private static double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /**
         * https://stackoverflow.com/questions/18173956/how-to-drag-undecorated-window
         */
        mainFrame.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = mainFrame.getScene().getWindow().getX() - event.getScreenX();
                yOffset = mainFrame.getScene().getWindow().getY() - event.getScreenY();
            }
        });

        mainFrame.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mainFrame.getScene().getWindow().setX(event.getScreenX() + xOffset);
                mainFrame.getScene().getWindow().setY(event.getScreenY() + yOffset);
            }
        });
    }

    @FXML
    public void sendMessage() {

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

            /**
             * Нужно добавить обработку ситуации отправки пустой формы !!!
             */

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
                    final String nickname;

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
                        // Поступают в формате nick@@color@@message
                        while (true) {
                            String str = in.readUTF();

                            if (str.equals(PROT_MSG_SERVER_CLOSED)) break;

                            String[] parts = str.split(SEPARATOR);

                            System.out.println(socket.getInetAddress() + ":" + socket.getPort());

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
        HBox hbPanel = new HBox();
        VBox vb = new VBox();
        Label nickname = new Label(nick);
        Label message = new Label(text);

        // Стилевое оформление
        String fx_bg_content = "-fx-background-color: " + color;
        String fx_alignment_hb = "-fx-alignment: ";

        // Свои сообщения по левой стороне окна. Чужие по правой
        fx_alignment_hb += (isMine) ? (Pos.CENTER_LEFT + ";") : (Pos.CENTER_RIGHT + ";");

        hbPanel.setStyle("-fx-padding: 10;" +
                "-fx-border-color: transparent;" +
                "-fx-background-color: white;" +
                "-fx-border-width: 0px;" +
                "-fx-spacing: 30;" +
                fx_alignment_hb);

        vb.setStyle("-fx-background-radius: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-spacing: 2;" +
                fx_bg_content);

        nickname.setStyle("-fx-font-style: italic;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: baseline-left;" +
                "-fx-padding: 5 30 0 10;" +
                "-fx-font-size: 16px;" +
                "-fx-text-fill: white;");

        message.setStyle("-fx-alignment: baseline-right;" +
                "-fx-padding: 0 10 5 30;" +
                "-fx-font-size: 14px;" +
                "-fx-alignment: baseline-right;");

        vb.getChildren().addAll(nickname, message);

        // Добавить иконку конверта справа или слева
        if (isMine)
            hbPanel.getChildren().addAll(prepareSticker(fx_bg_content), vb);
        else
            hbPanel.getChildren().addAll(vb, prepareSticker(fx_bg_content));

        // Добавляем контейнер сообщения в скроллинг
        chatMessages.getChildren().add(hbPanel);
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
                "-fx-alignment:  baseline-center;" +
                "-fx-border-width: 0px;" +
                "-fx-border-radius: 50%;" +
                "-fx-background-radius: 50%;" +
                backgroundColor);
        iconHb.setMaxSize(40, 40);

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
