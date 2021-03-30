package SAIS;


import SAIS.Controller.PaymentFormController;
import SAIS.Controller.QuantityController;
import SAIS.Controller.SearchController;
import SAIS.Others.DBInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Test extends Application {
    public static void main(String[] args) throws SQLException {
        launch(args);

    }


    @Override
    public void start(Stage primaryStage) throws Exception{
     //   SearchController.setColumnName(setColumnNames());
      //  SearchController.Query("SELECT * FROM product");
        Parent root = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/Sale.fxml"));

        primaryStage.setTitle("Login Page");
        primaryStage.setScene(new Scene(root, 1250, 900));
        primaryStage.setResizable(false);
        primaryStage.show();

       /* PaymentFormController.Display(1000.00);*/
    }

    public List<String> setColumnNames(){
        List <String> columnName = new ArrayList<>();
        columnName.add("Customer ID No");
        columnName.add("First Name");
        columnName.add("Last Name");
        columnName.add("Street");
        columnName.add("City");
        columnName.add("Province");
        columnName.add("Email Address");
        columnName.add("Mobile/Phone No");

        return columnName;
    }


}
