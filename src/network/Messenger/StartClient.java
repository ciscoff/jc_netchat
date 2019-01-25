package network.Messenger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StartClient extends Application {
    private final int FrameWidth = 450;
    private final int FrameHeight = 600;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("ChatGui.fxml"));

//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(new Scene(root, FrameWidth, FrameHeight));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
