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

public class ConfirmationPageController implements Initializable {


    @FXML
    private Button btnOk;

    @FXML
    private Label lblMessage;

    private static String message;


    @FXML
    public void selectedBtn(ActionEvent event) {
        Stage exit = (Stage) btnOk.getScene().getWindow();
        exit.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblMessage.setText(ConfirmationPageController.message);

    }

    //a generic confirmation page wherein the text of the label and title of the stage can be set by creating a parameters when invoking this method
    public static void Display (String title, String message) throws IOException {
        ConfirmationPageController.message=message;
        Parent root = FXMLLoader.load(ConfirmationPageController.class.getResource("/SAIS/DisplayPage/ConfirmationPage.fxml"));
        Scene scene = new Scene(root,380,165);
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
