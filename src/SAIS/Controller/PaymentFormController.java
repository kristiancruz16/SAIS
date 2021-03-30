package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PaymentFormController implements Initializable {

    private static Double amountToPay,amountPaid;

    @FXML
    private JFXTextField txtAmountPaid;
    @FXML
    private Label lblAmountToPay,lblChange;
    @FXML
    private Button btnOkCancel;

    private static String selectedBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        amountPaid=0.00;
        selectedBtn = new String();
        txtAmountPaid.setText("0.00");
        lblChange.setText("0.00");
        txtAmountPaid.requestFocus();
        lblAmountToPay.setText(String.format("%.2f",amountToPay));
        txtAmountPaid.focusedProperty().addListener((obsValue,lostFocus,gotFocus) ->{
            if(lostFocus&&!txtAmountPaid.getText().equals("0.00"))
               Calculator();
        });
        txtAmountPaid.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER)
                        btnOkCancel.requestFocus();
        });

    }
    private void Calculator () {
        try {
            amountPaid = Double.parseDouble(txtAmountPaid.getText());
            txtAmountPaid.setText(String.format("%.2f", amountPaid));
            if (amountPaid >= Double.parseDouble(lblAmountToPay.getText())) {
                lblChange.setText(String.format("%.2f", amountPaid - Double.parseDouble(lblAmountToPay.getText())));
                btnOkCancel.requestFocus();

            } else {
                ConfirmationPageController.Display("Warning", "Error! Insufficient payment!");
                txtAmountPaid.setText("0.00");
                txtAmountPaid.requestFocus();
            }
        }catch (NumberFormatException| IOException numEx){
            try {
                ConfirmationPageController.Display("Warning", "Invalid! Please enter numeric values only.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            txtAmountPaid.setText("0.00");
            txtAmountPaid.requestFocus();
        }
    }

    public static void Display(Double amountToPay) throws IOException {
        PaymentFormController.amountToPay = amountToPay;
        selectedBtn = new String();
        Parent root = FXMLLoader.load(PaymentFormController.class.getResource("/SAIS/DisplayPage/PaymentForm.fxml"));
        Stage dialog = new Stage();
        Scene scene = new Scene(root);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Payment Form");
        dialog.setResizable(false);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void selectedBtn(ActionEvent event) throws IOException {
        Button btnSelect = (Button) event.getSource();
        selectedBtn = btnSelect.getText();
        Stage exit = (Stage) btnSelect.getScene().getWindow();
        if (selectedBtn.equals("Ok")) {
            if (Double.parseDouble(txtAmountPaid.getText()) >= amountToPay)
                exit.close();
            else
                ConfirmationPageController.Display("Warning","Payment transaction is not yet completed!");
        }else
         exit.close();
    }

    public static String getSelectedBtn(){
        return selectedBtn;
    }
}
