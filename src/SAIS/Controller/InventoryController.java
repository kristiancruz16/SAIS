package SAIS.Controller;

/**
 * Author: Kristian Louise M. Cruz
 * Written on: February 25, 2021
 * Project: Sales and Inventory System
 **/

import SAIS.Others.DBInitializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {


    @FXML
    private ChoiceBox choiceReport = new ChoiceBox();

    @FXML
    private ChoiceBox choiceComparative = new ChoiceBox();

    @FXML
    private DatePicker dateFrom, dateTo;

    @FXML
    private Label lblComparative, lblDateRange, lblTo;

    @FXML
    private CheckBox chkBoxFilter = new CheckBox();

    @FXML
    private TextField txtFilter = new TextField();

    @FXML
    private Button btnTableView, btnGraphView;

    @FXML
    private AnchorPane displayPane;

    private int counterTableProdHis = 0;
    private String selectedReport = new String(), selectedComparative = new String();

    Connection connection = DBInitializer.initializeConnection();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*add items in ChoiceBox choiceReport*/
        choiceReport.getItems().add("Inventory On Hand");
        choiceReport.getItems().add("Low Stock Report");
        choiceReport.getItems().add("Product History");
        choiceReport.getItems().add("Product Performance");

        /*add items in ChoiceBox choiceComparative*/
        choiceComparative.getItems().add("Daily");
        choiceComparative.getItems().add("Monthly");
        choiceComparative.getItems().add("Yearly");


        /*a selection listener of choiceComparative and referenced the selected row to String selectedComparative*/
        choiceComparative.getSelectionModel().selectedItemProperty().addListener((obsValue, oldValue, newValue) ->
        {
            if(newValue!=null)
            selectedComparative = newValue.toString();
        });

        /*a selection listener of choiceReport and referenced the selected row to String selectedReport
        * a switch case for the selected row to display DatePicker, TextField, ChoiceBox, Labels depending on
        * what the user has selected in the ChoiceBox*/
        choiceReport.getSelectionModel().selectedItemProperty().addListener((obsValue, oldValue, newValue) -> {
            selectedReport = newValue.toString();
            switch (newValue.toString()) {
                case ("Inventory On Hand"), ("Low Stock Report"), ("Product History") -> {
                    choiceComparative.setVisible(false);
                    choiceComparative.setValue("Select");
                    choiceComparative.getSelectionModel().clearSelection();
                    dateFrom.setVisible(false);
                    dateFrom.getEditor().clear();
                    dateTo.getEditor().clear();
                    dateTo.setVisible(false);
                    lblComparative.setVisible(false);
                    lblDateRange.setVisible(false);
                    lblTo.setVisible(false);
                    chkBoxFilter.setLayoutX(253);
                    chkBoxFilter.setLayoutY(25);
                    txtFilter.setLayoutX(253);
                    txtFilter.setLayoutY(53);
                    txtFilter.setText("");
                    btnGraphView.setDisable(true);
                }
                case ("Product Performance") -> {
                    chkBoxFilter.setLayoutX(813);
                    chkBoxFilter.setLayoutY(25);
                    txtFilter.setLayoutX(813);
                    txtFilter.setLayoutY(53);
                    txtFilter.setText("");
                    choiceComparative.setVisible(true);
                    dateFrom.setVisible(true);
                    dateTo.setVisible(true);
                    lblComparative.setVisible(true);
                    lblDateRange.setVisible(true);
                    lblTo.setVisible(true);
                    btnGraphView.setDisable(false);
                }
            }
        });

        /*set editable property, opacity and text of txtFilter whenever there are changes in the selection of chkBoxFilter*/
        chkBoxFilter.selectedProperty().addListener((obsValue, oldValue, newValue) -> {
            /*if chkBoxFilter is marked checked, text field txtFilter enables the Editable Property and set the opacity to 1*/
            if (newValue) {
                txtFilter.setEditable(true);
                txtFilter.setOpacity(1.00);
            } else { // if chkBoxFilter is unmarked, it disables the Editable Property and opacity to .50 and clears the text
                txtFilter.setEditable(false);
                txtFilter.setOpacity(.50);
                txtFilter.setText("");
            }
        });

    }

    /*ActionEvent handler for button btnTableView
    * displays the selected report type in ChoiceBox choiceReport
    * it generates a query and a list of string for columnName and Observable List for Table View display if any
    * to be used as parameters in invoking Display*/
    @FXML
    private void selectedBtnTableView(ActionEvent event) throws IOException {
        String filterSearch = new String(); //used as a reference for the txtFilter is user marked the chkBoxFilter and entered a text to txtFilter

        /*An SQL query for report type Inventory On Hand and Low Stock Report*/
        String query = "SELECT concat(category_id,' - ',category_name), concat(supplier_id_no,' - ',supplier_name), " +
                "product_id, product_name, product_description, product_buying_price, product_selling_price, product_critical_level, " +
                "product_item_on_hand FROM product LEFT JOIN supplier USING (supplier_id_no) LEFT JOIN category USING (category_id)";
        String endQuery = " ORDER BY product_id";

        /*a string that will be appended to query if a user marked the chkBoxFilter and this will be the condition in WHERE statement in the
        * SQL query, if user not marked the chkBoxFilter, txt filter will be blank and will not affect the SQL query*/
        if (chkBoxFilter.isSelected()) {
            filterSearch = " product_id = '" + txtFilter.getText() + "' OR product_name = '" + txtFilter.getText() + "'";
        }
        switch (selectedReport) {
            /*report type that shows the product that has quantity*/
            case ("Inventory On Hand") -> {
                if (filterSearch.equals(""))
                    filterSearch = " product_item_on_hand>0";
                query = query + " WHERE" + filterSearch + endQuery;
                Display(query,getInventoryColumn(),null);
            }
            /*a report type that shows product that is less than 120% of its critical level*/
            case ("Low Stock Report") -> {
                if (filterSearch.equals(""))
                    filterSearch = " product_item_on_hand<(product_critical_level*1.20)";
                query = query + " WHERE" + filterSearch + endQuery;
                Display(query,getInventoryColumn(),null);
            }
            /*a report that shows the movement of the product, it has a beginning balance, number of item added or deducted and the ending balance*/
            case ("Product History") -> {
                if (!filterSearch.equals(""))
                    filterSearch = " WHERE" + filterSearch;
                query = "SELECT product.product_id, product_name, product_description, product_history_id, " +
                        "transaction_date, item_on_hand_history FROM product LEFT JOIN product_history USING (product_id) " + filterSearch +
                        " ORDER BY product_id";

                String isOldIDNo = new String();
                int beginBalance = 0, endBalance;
                ObservableList<List<Object>> obsListHistory = FXCollections.observableArrayList();
                try {
                    ResultSet resultSet = connection.createStatement().executeQuery(query);
                    while (resultSet.next()) {
                        ObservableList<Object> row = FXCollections.observableArrayList();
                        String ProdIDNo = resultSet.getString(1);
                        row.add(ProdIDNo);
                        if (!ProdIDNo.equals(isOldIDNo)) {
                            beginBalance = 0;
                        }
                        for (int i = 2; i <= resultSet.getMetaData().getColumnCount() - 1; i++) {
                            row.add(resultSet.getObject(i));
                        }
                        row.add(beginBalance);
                        endBalance = resultSet.getInt(6);
                        row.add(endBalance - beginBalance);
                        row.add(endBalance);
                        beginBalance = endBalance;
                        isOldIDNo = ProdIDNo;
                        obsListHistory.add(row);
                    }
                    Display("",getHistoryColumn(),obsListHistory);
                }catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }

            /*this case condition doesn't invoke the method Display yet and will be invoking a method ComparativeTypeMethod and
                */
            case ("Product Performance") -> {
                /*condition that checks if filterSearch is blank, if blank the filterSearch will remain blank and will not affect the syntax
                * of SQL query being created and if not the String AND is concatenated to it to comply with the syntax*/
                if (!filterSearch.equals(""))
                    filterSearch += " AND";
                query = " concat(category_id,' - ',category_name), product_has_sale.product_id, product_name, product_description," +
                         " sum(sale_quantity) as Total_Quantity_Sold FROM product_has_sale INNER JOIN sale USING (sale_id_no) "+
                         " INNER JOIN product USING (product_id) INNER JOIN category USING (category_id) WHERE "+filterSearch;
                ComparativeTypeMethod(query);

            }

            /*default condition if user did not select anything in choiceReport*/
            default -> ConfirmationPageController.Display("Warning", "Attention! Please select Summary Type.");

        }

    }

    private void ComparativeTypeMethod (String query) throws IOException {
        //condition that checks if values are not null to proceed
        if(dateFrom.getValue()!=null&&dateTo.getValue()!=null) {
            //condition that checks if valid date range to proceed
            if(dateFrom.getValue().compareTo(dateTo.getValue()) < 1) {
                //creates a copy of the getInventoryColumn
                List<String> column = new ArrayList<>(getInventoryColumn());
                 //removes the Buying Price Column, Selling Price Column, Critical Level column and Item On Hand
                column.remove(1);//removes Supplier column
                for (int i = 0 ; i<4;i++)
                    column.remove(4);
                column.add(0,"Sale Date");
                column.add(5,"Total Quantity Sold");

                /*a case condition that appends the correct type of sale(specific date, monthly or yearly) to the  query depending on what
                * is selected, once query is appended it will be invoking the Display method with parameters of the new query,
                * new List named column and null observable list*/
                switch (selectedComparative) {
                    case ("Daily") -> {
                        query = "SELECT sale_date," + query + " (sale_date >= '"+dateFrom.getValue()+"' AND " +
                                "sale_date <= '"+dateTo.getValue()+"') GROUP BY sale_date,product_id ORDER BY sale_date";
                        Display(query,column,null);
                    }
                    case ("Monthly") -> {
                        query = "SELECT date_format(sale_date,'%M %Y')," + query + " (sale_date >= '"+dateFrom.getValue()+"' AND " +
                                "sale_date <= '"+dateTo.getValue()+"') GROUP BY month(sale_date),product_id ORDER BY month(sale_date)";
                        Display(query,column,null);
                    }
                    case ("Yearly") -> {
                        query = "SELECT date_format(sale_date,'%Y')," + query + " (sale_date >= '"+dateFrom.getValue()+"' AND " +
                                "sale_date<= '"+dateTo.getValue()+"') GROUP BY year(sale_date),product_id ORDER BY year(sale_date)";
                        Display(query,column,null);

                    }
                    default ->   ConfirmationPageController.Display("Warning", "Please select Comparative Type.");
                }
            }else
                ConfirmationPageController.Display("Warning", "Attention! You have entered an invalid date range.");
        }
        else
            ConfirmationPageController.Display("Warning", "Attention! Please enter date range.");

    }

    /*this method is invoked to set a query, columnName and observableList(if any) to ReportDisplayController in displaying
    * the tableView to AnchorPane displayPane */
    private  void Display (String query, List<String> columnName, ObservableList<List<Object>> observableList) {
        AnchorPane paneLoader;
        ReportDisplayController.setQuery(query, columnName);
        ReportDisplayController.setObsList(observableList);
        try {
            displayPane.getChildren().clear();
            paneLoader = FXMLLoader.load(getClass().getResource("/SAIS/DisplayPage/ReportDisplay.fxml"));
            displayPane.getChildren().add(paneLoader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*ActionEvent handler for button btnGraphView
    * this creates a string of date format that will be referenced to columnDate and dateIdentifier and will be used
    * as a parameter in invoking GraphDisplay that will be appended to the SQL Query*/
    @FXML
    private void selectedBtnGraphView (ActionEvent event) throws IOException, ParseException {
        /*referenced a string depending on the date comparative type selected and used as a parameter in invoking GraphComparativeType*/
        String columnDate, dateIdentifier;
        /*reference to a List of Strings with values of set of dates with an initial value of 0*/
        List <List<String>> dateRangeList = new ArrayList<>();
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        long difference; // stores difference of the no. of days/months/year from dateFrom and dateTo
        if(dateTo.getValue()!=null&&dateFrom.getValue()!=null){
            if(dateFrom.getValue().compareTo(dateTo.getValue()) < 1) {
                String stringDate = String.valueOf(dateFrom.getValue());
                switch (selectedComparative) {
                    case ("Daily") -> {
                        columnDate = "sale_date";
                        dateIdentifier = columnDate;
                        difference = ChronoUnit.DAYS.between(
                                dateFrom.getValue(),
                                dateTo.getValue());
                        Calendar calendar = Calendar.getInstance();
                        calendar.getTime();

                        /*a for loop that create a set of specific dates with an initial value of 0*/
                        for (int i = 0; i <= difference; i++) {
                            List<String> datesArray = new ArrayList<>();
                            try {
                                calendar.setTime(sdFormat.parse(stringDate));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            datesArray.add(new SimpleDateFormat("yyyy-MM-dd").format(sdFormat.parse(stringDate)));
                            datesArray.add(String.valueOf(0));
                            dateRangeList.add(datesArray);
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            stringDate = sdFormat.format(calendar.getTime());
                        }
                        GraphDisplay(columnDate, dateIdentifier, dateRangeList);
                    }
                    case ("Monthly") -> {
                        columnDate = "date_format(sale_date, '%M %Y')";
                        dateIdentifier = "month(sale_date)";
                        difference = ChronoUnit.MONTHS.between(
                                YearMonth.from(dateFrom.getValue()),
                                YearMonth.from(dateTo.getValue()));
                        Calendar calendar = Calendar.getInstance();
                        calendar.getTime();
                        /*a for loop that create a set of months with an initial value of 0*/
                        for (int i = 0; i <= difference; i++) {
                            List<String> datesArray = new ArrayList<>();
                            try {
                                calendar.setTime(sdFormat.parse(stringDate));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            datesArray.add(new SimpleDateFormat("MMMM YYYY").format(sdFormat.parse(stringDate)));
                            datesArray.add(String.valueOf(0));
                            dateRangeList.add(datesArray);
                            calendar.add(Calendar.MONTH, 1);
                            stringDate = sdFormat.format(calendar.getTime());
                        }
                        GraphDisplay(columnDate, dateIdentifier, dateRangeList);
                    }
                    case ("Yearly") -> {
                        columnDate = "date_format(sale_date, '%Y')";
                        dateIdentifier = "year(sale_date)";
                        difference = ChronoUnit.YEARS.between(
                                dateFrom.getValue(),
                                dateTo.getValue());
                        Calendar calendar = Calendar.getInstance();
                        calendar.getTime();
                        /*a for loop that create a set of years with an initial value of 0*/
                        for (int i = 0; i <= difference; i++) {
                            List<String> datesArray = new ArrayList<>();
                            try {
                                calendar.setTime(sdFormat.parse(stringDate));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            datesArray.add(new SimpleDateFormat("YYYY").format(sdFormat.parse(stringDate)));
                            datesArray.add(String.valueOf(0));
                            dateRangeList.add(datesArray);
                            calendar.add(Calendar.YEAR, 1);
                            stringDate = sdFormat.format(calendar.getTime());
                        }
                        GraphDisplay(columnDate, dateIdentifier, dateRangeList);
                    }
                    default ->ConfirmationPageController.Display("Warning", "Please select Comparative Type.");
                }

            }else  ConfirmationPageController.Display("Warning", "Attention! You have entered an invalid date range.");

        }else  ConfirmationPageController.Display("Warning", "Attention! Please enter date range.");

    }

    private void GraphDisplay (String columnDate, String dateIdentifier, List<List<String>> dateRangeList) {
        //an arraylist that holds the name of the title for the object of XYChart.Series
        List<String> seriesName = new ArrayList<>();
        Connection connection = DBInitializer.initializeConnection();
        String filterSearch = new String();
        if (chkBoxFilter.isSelected()) {
            filterSearch = " AND product_id = '" + txtFilter.getText() + "' OR product_name = '" + txtFilter.getText() + "'";
        }
        /*a query to select 3 products with the highest total quantity sold that will be used as a name for the XYChart.Series*/
        String seriesQuery = "SELECT product_has_sale.product_id, product_name, product_description, sum(sale_quantity) as Total_Quantity_Sold " +
                "FROM product_has_sale INNER JOIN sale USING (sale_id_no)  INNER JOIN product USING (product_id) " +
                "WHERE  (sale_date >= '"+dateFrom.getValue()+"' AND sale_date <= '"+dateTo.getValue()+"') " +
                    filterSearch+"GROUP BY product_id ORDER BY Total_Quantity_Sold DESC LIMIT 3";
        /*query that filters the totals the quantity sold of each product on a specified date range*/
        String query = "SELECT "+columnDate+", product_has_sale.product_id, product_name, product_description, sum(sale_quantity) as Total_Quantity_Sold " +
                "FROM product_has_sale INNER JOIN sale USING (sale_id_no)  INNER JOIN product USING (product_id) " +
                "WHERE  (sale_date >= '"+dateFrom.getValue()+"' AND sale_date <= '"+dateTo.getValue()+"') " +
                filterSearch+" GROUP BY "+dateIdentifier+", product_id " +
                "ORDER BY Total_Quantity_Sold DESC";

        //define the X and Y axis
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        //create the chart
        final LineChart<String, Number> lineChart =
                new LineChart<String, Number>(xAxis, yAxis);
        lineChart.setTitle(choiceReport.getValue().toString());

        //creates an object of XYChart.Series, an arraylist is used just in case an object of series is need to be created
        //more than once
        List<XYChart.Series> series = new ArrayList<>();

        try {

            /*filter the database with the 3 most number of quantity sold*/
            ResultSet resultSet = connection.createStatement().executeQuery(seriesQuery);


                int i = 0;
                /*stores the column name with most number of sold quantity to seriesName*/
                while (resultSet.next()) {
                    if (i < 3)
                        seriesName.add(resultSet.getString(1)+" "+resultSet.getString(2)+" "+resultSet.getString(3) );
                    i++;
                }
                /*creates a list of object of XYChart.Series and set its name from the list seriesName*/
                for (int j = 0; j < seriesName.size(); j++) {
                    series.add(new XYChart.Series());
                    series.get(j).setName(seriesName.get(j));
                }

                /*creates a loop with number of times depending on the size of seriesName*/
                for (i = 0; i < seriesName.size(); i++) {

                    /*filter the database given the SQL query*/

                    resultSet = connection.createStatement().executeQuery(query);
                    while (resultSet.next()) {
                        /*proceed if the value of the column 3 index is equals to the series name*/
                        if (seriesName.get(i).equals(resultSet.getString(2)+" "+resultSet.getString(3)+" "+resultSet.getString(4) )) {
                            String dateHolder = resultSet.getString(1); // holds the date of the record from the database
                            /*a for loop that search if the value of the dateRangeList is equals to dateHolder
                             * if true it replaces the value 0 of the dateRangeList with the total_quantity of resultSet.getString(5)*/
                            for (int j = 0; j < dateRangeList.size(); j++) {
                                if (dateRangeList.get(j).contains(dateHolder)) {
                                    dateRangeList.get(j).remove(1); //removes the value 0
                                    dateRangeList.get(j).add(1, resultSet.getString(5)); //replace the value 0 that was removed
                                }
                            }
                        }

                    }
                    /*a loop that creates the x and y axis of this particular seriesName*/
                    for (int k = 0; k < dateRangeList.size(); k++) {
                        Double value = Double.parseDouble(dateRangeList.get(k).get(1));
                        //populating the series of the particular seriesName of the loop with data from dateRangeList
                        series.get(i).getData().add(new XYChart.Data(dateRangeList.get(k).get(0), value));
                    }

                    /*a for loop restores the original value of index 1 which is 0 of dateRangeList
                    * Purpose of doing this is because not to retain the old values when the new value for that index is 0.
                    Problem encountered is if the previous series has a value on that date and current series has 0 value
                    * the previous value will still be in the dateRangeList. Below for loop solve that problem
                    * (an easier solution for deep copying the original contents of dateRangeList)*/
                    for (int k=0; k<dateRangeList.size();k++) {
                        dateRangeList.get(k).remove(1);
                        dateRangeList.get(k).add("0");
                    }
                }

            // clears the AnchorPane if there is existing node
            displayPane.getChildren().clear();
            //add the object of XYChart.Series to LineChart
            for (i = 0; i < series.size(); i++)
                lineChart.getData().add(series.get(i));
            lineChart.setPrefWidth(1000);
            lineChart.setLayoutX((displayPane.getPrefWidth() / 2) - (lineChart.getPrefWidth() / 2));
            lineChart.setLayoutY(35);
            // adds the LineChart to the AnchorPane displayPane
            displayPane.getChildren().addAll(lineChart);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private List<String> getInventoryColumn (){
        List<String> inventoryColumn = new ArrayList<>();
        inventoryColumn.add("Category");
        inventoryColumn.add("Supplier");
        inventoryColumn.add("Product ID No");
        inventoryColumn.add("Product Name");
        inventoryColumn.add("Description");
        inventoryColumn.add("Buying Price");
        inventoryColumn.add("Selling Price");
        inventoryColumn.add("Critical Level");
        inventoryColumn.add("Item on Hand");
        return  inventoryColumn;
    }

    private List<String> getHistoryColumn () {
        List<String> historyColumn = new ArrayList<>();
        historyColumn.add("Product ID No");
        historyColumn.add("Product Name");
        historyColumn.add("Description");
        historyColumn.add("Transaction ID");
        historyColumn.add("Transaction Date");
        historyColumn.add("Begin Balance");
        historyColumn.add("+In/-Out");
        historyColumn.add("End Balance");
        return historyColumn;
    }
}

