package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;



public class LogInController implements  Initializable{
    @FXML
    private Button loginBtn;

    @FXML
    private TextField txtfldUsername;

    @FXML
    private PasswordField passfldPassword;

    protected static String query;
    private static Connection connection;
    private PreparedStatement preparedStatement;
    protected static ResultSet resStmnt;
    protected static String userFName, userLName;
    protected static String userName;


    @FXML
    protected void ctrlLogIn(ActionEvent event) {
        //Creates a connection to the database and makes a query and returns record if there is a match in the username and password
        //given by the user in the database.
        try {
            query = "SELECT * FROM user WHERE username = BINARY ? and password = BINARY ?";
            preparedStatement =connection.prepareStatement(query);
            preparedStatement.setString(1,txtfldUsername.getText());
            preparedStatement.setString(2,passfldPassword.getText());
            resStmnt = preparedStatement.executeQuery();

            if(resStmnt.next()) {
                //records found and displays the main page
                userName = resStmnt.getString("username");
                userFName = resStmnt.getString("user_firstname");
                userLName = resStmnt.getString("user_lastname");


                Stage logIn = (Stage) loginBtn.getScene().getWindow();
                logIn.close();
                Parent root = null;
                Stage base = new Stage();
                try {
                    root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/MainPage.fxml"));
                    base.setTitle("Sales and Inventory Management System");
                    base.setScene(new Scene(root,1500,900));
                    base.setResizable(false);
                    base.show();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            else { //if no records returned, username and password did not match or no existing record
                    ConfirmationPageController.Display("Invalid Login","Invalid username or password!");
                    passfldPassword.requestFocus();
                    passfldPassword.setText("");
                }


        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connection = DBInitializer.initializeConnection();

        txtfldUsername.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER)
                passfldPassword.requestFocus();
        });
        passfldPassword.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER)
                loginBtn.requestFocus();
        });
    }

    public static String getUsername() {
        return userName;
    }

}
