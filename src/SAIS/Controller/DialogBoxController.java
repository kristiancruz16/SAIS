package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DialogBoxController implements Initializable {

    public static String stringSelectedBtn;
    private static String message,btnText;
    @FXML
    private Button btnConfirm;

    @FXML
    private Label lblMessage;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    lblMessage.setText(message);
    btnConfirm.setText(btnText);
    }
    public static void DialogBoxDisplay(String title,String message,String btnText){
        stringSelectedBtn=null;
        DialogBoxController.message = message;
        DialogBoxController.btnText = btnText;
        try {
            Parent root = FXMLLoader.load(DialogBoxController.class.getResource("/SAIS/DisplayPage/DialogBox.fxml"));
            Scene scene = new Scene(root,380,165);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);
            dialog.setResizable(false);
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selectedBtn(ActionEvent event) {
        Button btnSelected = (Button) event.getSource();
        stringSelectedBtn = btnSelected.getText();
        Stage exit = (Stage) btnSelected.getScene().getWindow();
        exit.close();

    }


    public static String getStringSelectedBtn() {
        return stringSelectedBtn;
    }
}
