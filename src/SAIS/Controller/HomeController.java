package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class HomeController implements Initializable {
    @FXML
    private Label lblFirstLastName, lblUsername,lblConfirm, lblTodaySales, lblTop1, lblTop2, lblTop3;

    @FXML
    private PasswordField passwordNew, passwordConfirm;

    @FXML
    private CheckBox chkShowPassword;

    @FXML
    private Button btnChange;

    @FXML
    private TextField txtNew, txtConfirm;

    @FXML
    private TableView <List<Object>> tableLowStock = new TableView<>();

    @FXML
    private AnchorPane graphPane;

    private boolean notYetClicked = true;
    private Connection connection = DBInitializer.initializeConnection();
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private String password;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    UserDisplay();
    TodaySales();
    BestSellers();
    TableLowStockReport();
    GraphDisplay();

    }

    private void TodaySales()  {
        String query = "SELECT sum(sale_total_amount) from sale WHERE sale_date = curdate()";
        DecimalFormat decimalFormat = new DecimalFormat("PHP ##,###,###.00");
        try {
            resultSet = connection.createStatement().executeQuery(query);
            if(resultSet.next()){
                lblTodaySales.setText(decimalFormat.format(resultSet.getDouble(1)));
            }

        }catch (SQLException sqlEx) {
        }
    }

    private void BestSellers(){
        String query = "SELECT product_has_sale.product_id, product_name, product_description, sum(sale_quantity) as Total_Quantity_Sold " +
                "FROM product_has_sale INNER JOIN sale USING (sale_id_no)  INNER JOIN product USING (product_id) " +
                "WHERE  (sale_date >= curdate() - INTERVAL 30 DAY AND sale_date <= curdate()) " +
                "GROUP BY month(sale_date), product_id ORDER BY Total_Quantity_Sold DESC LIMIT 3";
        try {
            resultSet = connection.createStatement().executeQuery(query);
            int i = 0;
            while (resultSet.next()){
                getLblTop().get(i).setText(resultSet.getString(1)+ " "+resultSet.getString(2)+" "+resultSet.getString(3));
                i++;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private List<Label> getLblTop (){
        List<Label> lblTop = new ArrayList<>();
        lblTop.add(lblTop1);
        lblTop.add(lblTop2);
        lblTop.add(lblTop3);
        return lblTop;
    }

    private void TableLowStockReport(){
        List <String> columnNames = Arrays.asList("Product ID","Product Name", "Description", "Critical Level","Item on Hand");
        for(int i=0;i<columnNames.size();i++) {
            TableColumn<List<Object>,Object> column = new TableColumn<>(columnNames.get(i));
            int index = i;
            column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(index)));
            column.setReorderable(false);
            column.setPrefWidth(tableLowStock.getPrefWidth()/columnNames.size());
            tableLowStock.getColumns().add(column);
        }
        ObservableList<List<Object>> obsList = FXCollections.observableArrayList();
        String query = "SELECT product_id,product_name,product_description,product_critical_level,product_item_on_hand " +
                "FROM product WHERE product_item_on_hand<(product_critical_level*1.20)";
        try {
            resultSet = connection.createStatement().executeQuery(query);
             while (resultSet.next()){
                    ObservableList<Object> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount();i++) {
                        row.add(resultSet.getObject(i));
                    }
                    obsList.add(row);
            }
             tableLowStock.setItems(obsList);
        }catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    private void GraphDisplay() {
        long difference;
        String stringDate = LocalDate.now().minusDays(7).toString();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<List<String>> dateRangeList = new ArrayList<>();
        difference = ChronoUnit.DAYS.between(
                LocalDate.now().minusDays(7),
                LocalDate.now());
        Calendar calendar = Calendar.getInstance();
        calendar.getTime();
        for (int i = 0; i < difference; i++) {
            List<String> datesArray = new ArrayList<>();
            try {
                calendar.setTime(format.parse(stringDate));
                datesArray.add(new SimpleDateFormat("yyyy-MM-dd").format(format.parse(stringDate)));
                datesArray.add(String.valueOf(0));
                dateRangeList.add(datesArray);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                stringDate = format.format(calendar.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        //define the X and Y axis
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        //create the chart
        final LineChart<String, Number> lineChart =
                new LineChart<String, Number>(xAxis, yAxis);


        //creates an object of XYChart.Series, an arraylist is used just in case an object of series is need to be created
        //more than once
        XYChart.Series series = new XYChart.Series();
        series.setName("Total Sales");

        /*filter the database given the SQL query from GraphComparativeType*/
        String query = "SELECT sale_date, sum(sale_total_amount) FROM sale " +
                "WHERE sale_date >= curdate() - INTERVAL 7 DAY AND sale_date<curdate() GROUP BY sale_date";
        try {
            resultSet = connection.createStatement().executeQuery(query);
            while (resultSet.next()) {
                String dateHolder = resultSet.getString(1); // holds the date of the record from the database
                /*a for loop that search if the value of the dateRangeList is equals to dateHolder
                 * if true it replaces the value 0 of the dateRangeList with the total_amount of resultSet.getString(2)*/
                for (int j = 0; j < dateRangeList.size(); j++) {
                    if (dateRangeList.get(j).contains(dateHolder)) {
                        dateRangeList.get(j).remove(1);
                        dateRangeList.get(j).add(1, resultSet.getString(2));
                    }
                }
            }
            for (int k = 0; k < dateRangeList.size(); k++) {
                Double value = Double.parseDouble(dateRangeList.get(k).get(1));
                //populating the series of the particular seriesName of the loop with data from dateRangeList
                series.getData().add(new XYChart.Data(dateRangeList.get(k).get(0), value));
            }

            //add the object of XYChart.Series to LineChart

            lineChart.getData().add(series);
            lineChart.setPrefHeight(tableLowStock.getPrefHeight()-100);
            lineChart.setLayoutX(25);
            lineChart.setLayoutY(40);
            // adds the LineChart to the AnchorPane displayPane
            graphPane.getChildren().addAll(lineChart);

        }catch (SQLException sqlEx){
            sqlEx.printStackTrace();
        }

    }

    private void UserDisplay() {
        lblFirstLastName.setText("Hi "+LogInController.userFName+ " "+LogInController.userLName);
        lblUsername.setText(LogInController.getUsername());

        chkShowPassword.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(oldValue) {
                txtNew.setVisible(false);
                txtConfirm.setVisible(false);
                passwordNew.setVisible(true);
                passwordConfirm.setVisible(true);
                passwordNew.setText(txtNew.getText());
                passwordConfirm.setText(txtConfirm.getText());
            }
            else{
                txtNew.setVisible(true);
                txtConfirm.setVisible(true);
                txtNew.setText(passwordNew.getText());
                txtConfirm.setText(passwordConfirm.getText());
                passwordNew.setVisible(false);
                passwordConfirm.setVisible(false);
            }
        });
        try {
            resultSet = connection.createStatement().executeQuery("SELECT * from user WHERE username = '"+LogInController.getUsername()+"'");
            if(resultSet.next())
                password = resultSet.getString("password");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        passwordNew.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER){
                passwordConfirm.requestFocus();
            }
        });
        passwordNew.focusedProperty().addListener((obsValue,lostFocus,gotFocus) ->{
            if (lostFocus) {
                if(passwordNew.getText().equals(password)){
                    try {
                        ConfirmationPageController.Display("Warning!","Your password is similar to your previous password.");
                        passwordNew.setText("");
                        passwordNew.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        passwordConfirm.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER){
                btnChange.requestFocus();
            }
        });
        passwordConfirm.focusedProperty().addListener((obsValue,lostFocus,gotFocus) ->{
            if (lostFocus) {
                if(!passwordConfirm.getText().equals(passwordNew.getText())&&!passwordConfirm.getText().equals("")){
                    try {
                        ConfirmationPageController.Display("Warning!","Password does not match");
                        passwordConfirm.setText("");
                        passwordConfirm.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        txtNew.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER){
                txtConfirm.requestFocus();
            }
        });
        txtNew.focusedProperty().addListener((obsValue,lostFocus,gotFocus) ->{
            if (lostFocus) {
                if(txtNew.getText().equals(password)){
                    try {
                        ConfirmationPageController.Display("Warning!","Your password is similar to your previous password.");
                        txtNew.setText("");
                        txtNew.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        txtConfirm.setOnKeyReleased(event -> {
            if(event.getCode()== KeyCode.ENTER){
                btnChange.requestFocus();
            }
        });
        txtConfirm.focusedProperty().addListener((obsValue,lostFocus,gotFocus) ->{
            if (lostFocus) {
                if(!txtConfirm.getText().equals(txtNew.getText())&&!txtConfirm.getText().equals("")){
                    try {
                        ConfirmationPageController.Display("Warning!","Password does not match");
                        txtConfirm.setText("");
                        txtConfirm.requestFocus();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @FXML
    private void selectedBtnChange (ActionEvent event) throws IOException, SQLException {
        DialogBoxController.DialogBoxDisplay("Confirmation","Do you want to save changes?","Ok");
        try {
            if (DialogBoxController.getStringSelectedBtn().equals("Ok")) {
                    if (passwordNew.visibleProperty().getValue()&&!passwordNew.getText().equals("") && !passwordConfirm.getText().equals("")) {
                        password = passwordNew.getText();
                        String query = "UPDATE user SET password = ? WHERE username = '" + LogInController.getUsername() + "'";
                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, password);
                        preparedStatement.executeUpdate();
                        ConfirmationPageController.Display("Confirmation","Attention! New password has been saved.");
                        passwordNew.setText("");
                        passwordConfirm.setText("");
                    }
                    else if (txtNew.visibleProperty().getValue()&&!txtNew.getText().equals("") && !txtConfirm.getText().equals("")) {
                        password = txtNew.getText();
                        String query = "UPDATE user SET password = ? WHERE username = '" + LogInController.getUsername() + "'";
                        preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, password);
                        preparedStatement.executeUpdate();
                        ConfirmationPageController.Display("Confirmation", "Attention! New password has been saved.");
                        txtNew.setText("");
                        txtConfirm.setText("");
                    } else
                    ConfirmationPageController.Display("Warning", "Attention! Cannot proceed with blank textfields.");
            }
        }catch (NullPointerException nullEx) {}
    }

    @FXML
    private void selectedLinkChangePassword(ActionEvent event) {
        if(notYetClicked){
            passwordNew.setVisible(true);
            passwordConfirm.setVisible(true);
            lblConfirm.setVisible(true);
            chkShowPassword.setVisible(true);
            btnChange.setVisible(true);
            notYetClicked = false;
        }else {
            passwordNew.setVisible(false);
            passwordConfirm.setVisible(false);
            passwordNew.setText("");
            passwordConfirm.setText("");
            txtNew.setText("");
            txtConfirm.setText("");
            txtNew.setVisible(false);
            txtConfirm.setVisible(false);
            lblConfirm.setVisible(false);
            chkShowPassword.setVisible(false);
            btnChange.setVisible(false);

            notYetClicked = true;
        }
    }
}
