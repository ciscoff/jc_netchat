/**
 * Иконки
 * https://www.flaticon.com/free-icon/chat_134806
 */

package network.Messenger;

import domain.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import network.ChatUtilizer;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;

import static utils.Share.*;

public class Controller implements Initializable, ChatUtilizer {

    DataOutputStream out;
    DataInputStream in;
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;
    boolean isAuthorized = false;
    String nickname;
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
    Label lblAuthError;
    @FXML
    ImageView imageConnect;

    private static double xOffset = 0;
    private static double yOffset = 0;
    private static boolean shakeFlagX;
    private static boolean shakeFlagY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    /**
     * Event handler for 'messageField' and 'btnSend'
     */
    @FXML
    public void onMessage() {
        p("onMessage");
        //sendMessage(getClassified(messageField.getText()));
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

    @FXML
    public void tryToAuth() {
        if (socket == null || socket.isClosed()) connect();

        p("tryToAuth");
        sendMessage(new ChatAuthRequest(loginField.getText(), passwordField.getText()));

        /**
         * Нужно добавить обработку ситуации отправки пустой формы !!!
         */

        lblAuthError.setVisible(false);
        loginField.clear();
        passwordField.clear();
    }


    /**
     * TODO: Подключение к серверу, аутентификация, работа в чате
     */
    private void connect() {
        try {
            socket = new Socket(HOST, PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            imageConnect.setImage(new Image("/img/notauth.png"));

            Thread receiver = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Цикл авторизации
                        Controller.this.nickname = authenticationLoop();
                        // Цикл работы в чате
                        conversationLoop();
                    } catch (IOException e) {
                        p("Connection closed");
                    } finally {
                        closeResources(ois, oos, socket);
                    }
                }
            });
            receiver.setDaemon(true);
            receiver.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public void tryToAuth() {
//        if (socket == null || socket.isClosed()) connect();
//
//        try {
//            out.writeUTF(PROT_MSG_AUTH + " " + loginField.getText() + " " + passwordField.getText());
//
//            /**
//             * Нужно добавить обработку ситуации отправки пустой формы !!!
//             */
//
//            lblAuthError.setVisible(false);
//            loginField.clear();
//            passwordField.clear();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // Цикл аутентификации
    @Override
    public String authenticationLoop() throws IOException {
        String nickname = null;

        while (!isAuthorized) {
            try {
                Message message = (Message) ois.readObject();
                if (message.type == MessageType.AUTH_RESPONSE) {
                    switch (((ChatAuthResponse) message).getResponse()) {
                        case AUTH_OK:
                            imageConnect.setImage(new Image("/img/connected.png"));
                            nickname = ((ChatAuthResponse) message).getNick();
                            setAuthorized(true);
                            break;
                        case AUTH_ERROR:
                        case NICK_BUSSY:
                            Platform.runLater(() -> {
                                        lblAuthError.setText(((ChatAuthResponse) message).getMessage());
                                        lblAuthError.setPadding(new Insets(5));
                                        lblAuthError.setStyle("-fx-text-fill: #828282;" +
                                                "-fx-border-width: 2;" +
                                                "-fx-border-radius: 5;" +
                                                "-fx-border-color: white;" +
                                                "-fx-background-radius: 0;" +
                                                "-fx-background-radius: 5;");
                                        lblAuthError.setVisible(true);
                                        shakeFrame();
                                    }
                            );
                            break;
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return nickname;
    }

    // Цикл работы в чате
    @Override
    public void conversationLoop() throws IOException {

        try {
            while (true) {
                Message message = (Message) ois.readObject();

                switch (message.type) {
                    case BROADCAST_SERVER:
                    case UNICAST_SERVER:
                        Platform.runLater(() -> {
                            /**
                             * equals проверяет имя отправителя с именем текущего клиента.
                             * Если имена совпадают, то вернулось свое собственное сообщение и его
                             * нужно отобразить с одной стороны окна чата. Если различаются, то сообщение
                             * чужое и его нужно поместить с другой стороны.
                             */
                            ChatMessageServer cms = (ChatMessageServer)message;
                            stickMessage(cms.getFrom().equals(nickname), cms.getColor(), cms.getFrom(), cms.getMessage());
                        });

                        break;
                    case COMMAND:
                    case NOTIFY:
//                        commandProcessor(message);
                        break;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

}

    @Override
    public void commandProcessor(String command) throws IOException {

        String[] parts = command.split(SEPARATOR);

        switch (parts[PROT_CMD_IDX]) {
            case PROT_MSG_SERVER_CLOSED:
                break;
            case PROT_MSG_IDLE:
                Platform.runLater(() -> {
                    imageConnect.setImage(new Image("/img/disconnect.png"));
                    lblAuthError.setText(parts[1]);
                    lblAuthError.setPadding(new Insets(5));
                    lblAuthError.setStyle("-fx-text-fill: red;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 5;" +
                            "-fx-border-color: red;" +
                            "-fx-background-radius: 0;" +
                            "-fx-background-radius: 5;");
                    lblAuthError.setVisible(true);
                });
                break;
        }
    }

    /**
     * Классифицировать введенное сообщение и упаковать его в соотв инстанс
     */
    private Message getClassified(String raw) {
        // Простое сообщение
        if (!raw.startsWith(PROT_CMD_PREFIX))
            return new ChatMessageClient(MessageType.BROADCAST, raw, nickname, new String(""));

        Message message = null;

        String[] parts = raw.split("\\s", 2);

        // Сообщения с префиксами, например /to nickX Hello all !
        switch (parts[0]) {
            case PROT_MSG_TO:       // nickX Hello all !
                parts = parts[1].split("\\s", 2);
                message = new ChatMessageClient(MessageType.UNICAST, parts[1], nickname, parts[0]);
                break;
            case PROT_MSG_BLOCK:    // nick1 nick2
                message = new ChatNotify(NotifyType.BLOCK, parts[1]);
                break;
            case PROT_MSG_UNBLOCK:  // nick1 nick2
                message = new ChatNotify(NotifyType.UNBLOCK, parts[1]);
                break;
        }

        return message;
    }

    /**
     * Превратить строку вида:
     * /cmd nickTo Hello world !
     * в отформатированную строку вида:
     * /cmd@@nickTo@@Hello world !
     */
//    private String formatRaw(String raw) {
//        String[] parts = raw.split("\\s", 3);
//        String message = null;
//
//        switch (parts[0]) {
//            case PROT_MSG_TO:   // /w nick_to message text
//                message = parts[PROT_CMD_IDX] + SEPARATOR + parts[PROT_NICK_TO] + SEPARATOR + parts[PROT_MSG_BODY];
//                break;
//            default:
//                message = raw;
//        }
//        return message;
//    }


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

    // Втряхнуть окно
    public void shakeFrame() {

        Stage primaryStage = (Stage) mainFrame.getScene().getWindow();
        shakeFlagX = true;
        shakeFlagY = true;

        Timeline timelineX = new Timeline(new KeyFrame(Duration.seconds(0.07), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (shakeFlagX) {
                    primaryStage.setX(primaryStage.getX() + 5);
                } else {
                    primaryStage.setX(primaryStage.getX() - 5);
                }
                shakeFlagX = !shakeFlagX;
            }
        }));

        timelineX.setCycleCount(4);
        timelineX.setAutoReverse(false);
        timelineX.play();

        Timeline timelineY = new Timeline(new KeyFrame(Duration.seconds(0.07), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (shakeFlagY) {
                    primaryStage.setY(primaryStage.getY() + 5);
                } else {
                    primaryStage.setY(primaryStage.getY() - 5);
                }
                shakeFlagY = !shakeFlagY;
            }
        }));

        timelineY.setCycleCount(4);
        timelineY.setAutoReverse(false);
        timelineY.play();
    }

    /**
     * TODO: Передать сообщение на сервер
     */
    private void sendMessage(Message message) {
        try {
            oos.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}