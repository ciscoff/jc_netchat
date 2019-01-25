package network.Messanger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PopupInput {

    public PopupInput() {

        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);

        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER);

        TextField tf = new TextField();
        Button btn = new Button("Send");

        hb.setStyle("-fx-padding: 2;" +
                "-fx-border-color: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-border-width: 0px;" +
                "-fx-spacing: 10;");

        btn.setStyle("-fx-background-color: #2f8f4f;");

        hb.getChildren().addAll(tf, btn);

        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setScene(new Scene(hb));
        dialogStage.show();
    }
}
