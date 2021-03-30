package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import static SAIS.Controller.LogInController.resStmnt;

public class MainPageController implements Initializable {
    @FXML
    private AnchorPane rightPane;
    @FXML
    private Button btnLogOut;
    @FXML
    private AnchorPane newRightPane;
    @FXML
    private Button btnSale,btnReports,btnInventory,btnReceiveOrder,btnPurchaseOrder,btnCustomer,btnProduct,
    btnSupplier,btnAdmin ;




    @FXML
    private Label lblDate;

    private final HashMap page = new HashMap();



    @FXML
    private void selectLogOut(ActionEvent event) throws SQLException {
        DialogBoxController.DialogBoxDisplay("Logout?","Are you sure you want to Logout?","Ok");
        Stage mainPage = (Stage) btnLogOut.getScene().getWindow();
        try {
            if (DialogBoxController.getStringSelectedBtn().equals("Ok")) {
                mainPage.close();
                System.exit(1);
            }
        }catch (NullPointerException nullEx){}

    }

    //method that is invoked when user clicked a button on the mainpage
    //clear the existing right pane and loads the new page selected by the user
    @FXML
    private void selectedBtn(ActionEvent event) {
        Button btnSelectedIndicator = (Button)event.getSource();
        String btnName = btnSelectedIndicator.getText();
        String url = String.valueOf(page.get(btnName));
        try {
            rightPane.getChildren().clear();
            newRightPane = FXMLLoader.load(getClass().getResource(url));
            rightPane.getChildren().add(newRightPane);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PageInitializer(); //invokes a method that creates a hash map for the url of the different pages of the mainpage

        UserAccess(); //invokes a method that sets access level

        btnLogOut.setText("Log out as " +LogInController.userFName + " " +LogInController.userLName);

        //creates a thread that set the text of lblDate to Date today and updates the text every second
        new Thread ( () -> {
            try {
                while (true) {
                    Platform.runLater(() -> lblDate.setText(new Date().toString()));
                    Thread.sleep(1000);
                }

            }catch (Exception ex){
                ex.printStackTrace();
                }

        }).start();



        //set the default page  of right pane of the split pane
        try {
            rightPane.getChildren().clear();
            newRightPane = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Home.fxml"));
            rightPane.getChildren().add(newRightPane);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

    }

    //Creates a HashMap with a key and value wherein the key is the text of the buttons on the main page
    // and the value is the url of the fxml page of the selected button
    private void PageInitializer(){
        page.put("Home","/SAIS/DisplayPage/Home.fxml");
        page.put("Sale","/SAIS/DisplayPage/Sale.fxml");
        page.put("Inventory","/SAIS/DisplayPage/Inventory.fxml");
        page.put("Reports","/SAIS/DisplayPage/Reports.fxml");
        page.put("Receive Order","/SAIS/DisplayPage/ReceiveOrder.fxml");
        page.put("Purchase Order","/SAIS/DisplayPage/PurchaseOrder.fxml");
        page.put("Customer","/SAIS/DisplayPage/Customer.fxml");
        page.put("Product","/SAIS/DisplayPage/Product.fxml");
        page.put("Supplier","/SAIS/DisplayPage/Supplier.fxml");
        page.put("Admin","/SAIS/DisplayPage/Admin.fxml");
    }


    //method that enables buttons at home according to its access level
    private void UserAccess(){

        try{

                if(resStmnt.getBoolean("sale")==true) {
                    btnSale.setDisable(false);
                }
                if(resStmnt.getBoolean("inventory")==true) {
                    btnInventory.setDisable(false);
                }
                if(resStmnt.getBoolean("reports")==true) {
                    btnReports.setDisable(false);
                }
                if(resStmnt.getBoolean("receive_order")==true) {
                    btnReceiveOrder.setDisable(false);
                }
                if(resStmnt.getBoolean("purchase_order")==true) {
                    btnPurchaseOrder.setDisable(false);
                }
                if(resStmnt.getBoolean("customer")==true) {
                    btnCustomer.setDisable(false);
                }
                if(resStmnt.getBoolean("product")==true) {
                    btnProduct.setDisable(false);
                }
                if(resStmnt.getBoolean("supplier")==true) {
                    btnSupplier.setDisable(false);
                }
                if(resStmnt.getBoolean("admin")==true) {
                    btnAdmin.setDisable(false);
                }

        }catch(Exception ex){
            ex.printStackTrace();
            }


      }

}


