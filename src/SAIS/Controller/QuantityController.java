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
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class QuantityController implements Initializable {

    @FXML
    private Button btnOkCancel;

    @FXML
    private TextField txtQuantity;

    @FXML
    private Label lblMessage;

    private  static String stringSelectedBtn;

    private static String quantity, message, txtValue;




    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    lblMessage.setText(message);
    txtQuantity.setText(txtValue);

    }

    //a generic confirmation page wherein the text of the label,title of the stage and initial value of textField can be set by creating a parameters when invoking this method
    public static void Display(String title, String message,String txtValue) throws IOException {
        stringSelectedBtn = null;
        quantity=null;
        QuantityController.message = message;
        QuantityController.txtValue = txtValue;
        Parent root = FXMLLoader.load(QuantityController.class.getResource("/SAIS/DisplayPage/Quantity.fxml"));
        Scene scene = new Scene(root, 380, 165);
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);
        dialog.setScene(scene);
        dialog.showAndWait();

    }

    @FXML
    private void selectedBtn(ActionEvent event) {
        quantity = txtQuantity.getText();
        Button btnSelected = (Button) event.getSource();
        stringSelectedBtn = btnSelected.getText();
        Stage exit = (Stage) btnOkCancel.getScene().getWindow();
        exit.close();
    }

    public static String getStringSelectedBtn () {

        return stringSelectedBtn;
    }

    public static String getTxtQuantity() {

        return quantity;
    }



}
