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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ReportDisplayController implements Initializable {
    private static String query;
    private static List<String> columnNames = new ArrayList<>();
    private static ObservableList<List<Object>> observableList;
    @FXML
    private TableView<List<Object>> tableDisplay = new TableView<>();


    /*a setter method that is invoked from ReportsController*/
    public static void setQuery(String query, List<String> columnNames) {
        ReportDisplayController.query = query;
        ReportDisplayController.columnNames = columnNames;
    }

    public static void setObsList (ObservableList<List<Object>> observableList) {
        ReportDisplayController.observableList = observableList;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Connection connection = DBInitializer.initializeConnection();
        try {

            //a loop that create the objects for TableColumn and a callback for that column
            for (int i=0;i<columnNames.size();i++){
                TableColumn<List<Object>,Object> column = new TableColumn<>(columnNames.get(i));
                int index = i;
                column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(index)));
                column.setReorderable(false);
                column.setPrefWidth(tableDisplay.getPrefWidth()/columnNames.size());
                tableDisplay.getColumns().add(column);
            }
            if (observableList==null) {
                //display the date from the database to the table view
                ObservableList<List<Object>> obsList = FXCollections.observableArrayList();
                ResultSet resultSet = connection.createStatement().executeQuery(query);
                while (resultSet.next()) {
                    ObservableList<Object> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        row.add(resultSet.getObject(i));
                    }
                    obsList.add(row);
                }
                tableDisplay.setItems(obsList);
            }else {
                /*//creates a copy of the observableList to be used to display the list in the tableDisplay
                //and after display the observableList will be cleared, creating a copy will not able to affect the display
                //on the tableDisplay if the observableList will be cleared after, if observableList will be the item in the
                tableDisplay once you clear it, it will also clear the item in the tableDisplay*/
                ObservableList<List<Object>> listDisplay = FXCollections.observableArrayList(observableList);
                tableDisplay.setItems(listDisplay);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        observableList=null;
    }

}

