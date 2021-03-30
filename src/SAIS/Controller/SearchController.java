package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SearchController implements Initializable {


        @FXML
        private TableView<List<Object>> tableData = new TableView<>();

        @FXML
        private ChoiceBox choiceSearchCat;

        @FXML
        private TextField txtSearch;

        private static String selectedBtn = new String();
        private List<String> tempColumn = new ArrayList<>();
        private List<?> tempList= new ArrayList<>(); //stores the arraylist for the action listener of the tableview
        private static String initialQuery; //holds the original query
        private Connection connection;
        private int indexChoice; //holds the index of the selected value for the column Name arraylist

        public static List<?> resultList= new ArrayList<>();
        public static List<String> columnName = new ArrayList<>();
        public static  String searchQuery, endQuery,tableName;


        //a generic method that will create attributes for tableview in search window for all table searches
        public static void setColumnName (List<String> columnName) {

                SearchController.columnName = columnName;
        }

        //a generic method for query  for all table searches
        public static void Query(String query){
                SearchController.searchQuery = query;
               initialQuery = SearchController.searchQuery;
        }

        //a special method for inner join query
        public static void InnerJoinQuery(String startQuery, String endQuery,String tableName){
            SearchController.searchQuery = startQuery;
            SearchController.tableName = tableName;
            initialQuery = SearchController.searchQuery;
            SearchController.endQuery = endQuery;
        }


        public static List<?> getResultList(){
            return resultList; //returns the selected row in the table or null if no selected row to the FXML page that invokes it
        }



        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {

            connection = DBInitializer.initializeConnection();
            selectedBtn=null;
                resultList.clear();

            //displays the formatted table column names in the table view named tableData

            for (int i = 0 ; i < columnName.size() ; i++) {
                TableColumn<List<Object>, Object> column = new TableColumn<>(columnName.get(i));
                int columnIndex = i ;
                column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(columnIndex)));
                column.setPrefWidth(tableData.getPrefWidth()/columnName.size());
                column.setResizable(false);
                column.setReorderable(false);
                tableData.getColumns().add(column);
            }

            /*creates values for choiceSearchCat(ChoiceBox) and creates column of the table view TableData*/
            for(String i: columnName)
                choiceSearchCat.getItems().add(i);


            try {
                TableDisplay(); //invokes this method to display rows
            } catch (IOException e) {
                e.printStackTrace();
            }
              /*captures selected rows in the table and store it in arraylist tempList. tempList will be used whenever
            there is an action event to selectedBtn()*/

            tableData.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                tempList = newValue;
            });


        }

        /*event handler for button selection in the search page if Select button clicked resultList will reference
        to the object of tempList otherwise resultList will be a reference to null value.This reference will be used
        to display its value to the textFields who invokes that search page.
         */
       @FXML
        public void selectedBtn(ActionEvent event) throws IOException {
            resultList.clear(); //clears that previous data stored in resultList arraylist
           if (tempList!=null)
                resultList = tempList;
            Button btnSelected  = (Button) event.getSource();
            selectedBtn = btnSelected.getText();
            endQuery = null;
            tableName=null;
           Stage exit = (Stage) btnSelected.getScene().getWindow();
            if(selectedBtn.equals("Select")){
                if(resultList.size()!=0)
                    exit.close();
                else
                    ConfirmationPageController.Display("Warning","No selected row!");
            }
            else {
                resultList.clear();
                exit.close();
            }
       }

       public static String getSelectedBtn (){
           return selectedBtn;
       }


       /*method for user defined search
        creates a temporary arraylist named tempColumn where the column names in the database will be stored (reference ***)
        using indexChoice (index of selected value in ChoiceBox from columnName arraylist), column name of the database
        can now be concatenated to the searchQuery String along with the text value from txtSearch to complete
        the user defined search.
        */

        @FXML
        private void selectedSearch(ActionEvent event) throws IOException {
                tempColumn.clear();
                try {
                        ResultSet rset = connection.createStatement().executeQuery(searchQuery);
                        for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++)
                                tempColumn.add(rset.getMetaData().getColumnName(i)); //reference*** creates an arraylist of the column name of the table
                        searchQuery = initialQuery;
                        searchQuery += " WHERE " + tempColumn.get(indexChoice) + " = ";
                } catch (SQLException throwables) {
                        throwables.printStackTrace();
                }

                searchQuery += "'"+txtSearch.getText()+"' "; //completes the user defined search
                TableDisplay();
                searchQuery = initialQuery; //makes the searchQuery back to the original query string;

        }

        /*
        display rows in the table view
        this method is invoked at initial loading page and every user defined search
         */
        private void TableDisplay() throws IOException {
            /*checks condition if InnerJoinQuery is invoked and if true special query string is used and if false
            * generic query is used*/
            if(endQuery!=null)
                SearchController.searchQuery += endQuery;
            ObservableList<List<Object>> data = FXCollections.observableArrayList();
            try {
                ResultSet resultSet = connection.createStatement().executeQuery(SearchController.searchQuery);

                while (resultSet.next()) {
                    ObservableList<Object> row = FXCollections.observableArrayList();
                    for (int i = 1 ; i <= resultSet.getMetaData().getColumnCount() ; i++) {
                        row.add(resultSet.getObject(i)); //stores each cell to an observable list of objects named row
                    }
                    data.add(row); //stores rows from database to an observable list of objects named data

                }
                tableData.setItems(data); //stores all rows to the table view named tableData and displays it to the table view

            } catch (SQLException throwables) {
                /*following condition below resolves the issue of SQLException wherein the column name is not being recognized as a valid sql syntax
                * and to resolves table name is append to the column name */
                    if(endQuery!=null){
                        searchQuery = initialQuery;
                        if (tempColumn.get(indexChoice).equals("purchase_date")||tempColumn.get(indexChoice).equals("sale_date")){
                            try {
                                searchQuery += " WHERE " +tableName +"." +tempColumn.get(indexChoice) + " = " +"'"+
                                        java.sql.Date.valueOf(new SimpleDateFormat("yyyy/MM/dd").parse(txtSearch.getText())+"' ");
                            } catch (ParseException e) {
                                ConfirmationPageController.Display("Warning!","Invalid date format! Please enter date format in yyyy-mm-dd.");
                            }
                        }
                        else
                        searchQuery += " WHERE " +tableName +"." +tempColumn.get(indexChoice) + " = " +"'"+txtSearch.getText()+"' ";
                        TableDisplay();
                    }
            }

        }

        /*get the value selected in the ChoiceBox and store the index of the selected value in indexChoice
        which will used for the action event of btnSearch
         */
        @FXML
        private void selectedChoiceBox(ActionEvent event){
               String choice = (String) choiceSearchCat.getValue();
               indexChoice = columnName.indexOf(choice);
               txtSearch.requestFocus();
        }

}




